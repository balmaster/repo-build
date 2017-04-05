package repo.build

class GitFeature {

    static File getManifestDir(RepoEnv env) {
        return new File(env.basedir, 'manifest')
    }

    static void cloneManifest(RepoEnv env, String url, String branch) {
        def dir = getManifestDir(env)
        dir.mkdirs()
        Git.clone(url, "origin", dir)
        Git.checkoutUpdate(branch, "origin/$branch", dir)
    }

    static void updateManifest(RepoEnv env, String branch) {
        def dir = getManifestDir(env)
        Git.fetch("origin", dir)
        Git.checkoutUpdate(branch, "origin/$branch", dir)
    }

    static void sync(RepoEnv env, int parallel) {
        def manifestDir = GitFeature.getManifestDir(env)
        if (manifestDir.exists()) {
            def manifestBranch = Git.getBranch(manifestDir)
            if ("HEAD" == manifestBranch) {
                throw new RepoBuildException("manifest branch must be local, use: repo-build -b <manifestBranch> init")
            }
            updateManifest(env, manifestBranch)
            env.openManifest()
            fetchUpdate(env, parallel)
        } else {
            throw new RepoBuildException("manifest dir $manifestDir not found")
        }
    }

    static void mergeRelease(RepoEnv env, int parallel, String featureBranch) {
        RepoManifest.forEachWithFeatureBranch(env, parallel,
                { project ->
                    def manifestBranch = RepoManifest.getBranch(env, project.@path)
                    Git.merge(manifestBranch, new File(env.basedir, project.@path))
                }, featureBranch)
    }

    static void mergeFeature(RepoEnv env, int parallel, String featureBranch) {
        RepoManifest.forEachWithFeatureBranch(env, parallel,
                { project ->
                    // check current components branch
                    def dir = new File(env.basedir, project.@path)
                    def manifestBranch = RepoManifest.getBranch(env, project.@path)
                    if (Git.getBranch(dir) != manifestBranch) {
                        throw new RepoBuildException("Component ${project.@path} must be set to branch $manifestBranch")
                    }
                    Git.merge(featureBranch, dir)
                }, featureBranch)
    }

    static void 'switch'(RepoEnv env, int parallel, String branch) {
        def remoteBranch = RepoManifest.getRemoteBranch(env, branch)
        RepoManifest.forEach(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    // переключаемся только если есть локальная или удаленная фича ветка
                    if (Git.branchPresent(dir, branch) || Git.branchPresent(dir, remoteBranch)) {
                        Git.checkoutUpdate(branch, remoteBranch, new File(env.basedir, project.@path))
                    }
                }
        )
    }

    static void fetchUpdate(RepoEnv env, int parallel) {
        def remoteName = RepoManifest.getRemoteName(env)
        def remoteBaseUrl = RepoManifest.getRemoteBaseUrl(env)
        RepoManifest.forEach(env, parallel,
                { Node project ->
                    def branch = RepoManifest.getBranch(env, project.@path)
                    def remoteBranch = RepoManifest.getRemoteBranch(env, branch)
                    def dir = new File(env.basedir, project.@path)
                    def name = project.@name
                    if (new File(dir, ".git").exists()) {
                        Git.fetch(remoteName, dir)
                    } else {
                        dir.mkdirs()
                        Git.clone("$remoteBaseUrl/$name", remoteName, dir)
                    }
                    Git.checkoutUpdate(branch, remoteBranch, dir)
                    Git.user(dir, env.props.getProperty("git.user.name"), env.props.getProperty("git.user.email"))
                })
    }

    static void mergeFeature(RepoEnv env, int parallel, String branch, Boolean mergeAbort) {
        def remoteBranch = RepoManifest.getRemoteBranch(env, branch)
        RepoManifest.forEachWithFeatureBranch(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    println "branch $remoteBranch found in ${project.@path}"
                    def startCommit = project.@revision.replaceFirst("refs/heads", env.manifest.remote[0].@name)
                    if (mergeAbort) {
                        try {
                            Git.mergeAbort(dir)
                        } catch (Exception e) {
                            // skip
                        }
                    }
                    Git.mergeFeatureBranch(branch, remoteBranch, startCommit, dir)
                },
                branch
        )
    }

    static void createFeatureBundles(RepoEnv env, int parallel, File targetDir, String branch) {
        def remoteBranch = RepoManifest.getRemoteBranch(env, branch)

        RepoManifest.forEachWithFeatureBranch(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    def gitName = new File(project.@name).getName().split("\\.").first()
                    println gitName
                    def bundleFile = new File(targetDir, "${gitName}.bundle")
                    Git.createFeatureBundle(remoteBranch, dir, bundleFile)
                },
                branch
        )
    }

    static void createManifestBundles(RepoEnv env, int parallel, File targetDir) {

        RepoManifest.forEach(env, parallel,
                { Node project ->
                    def remoteBranch = RepoManifest.getRemoteBranch(env, RepoManifest.getBranch(env, project.@path))
                    def dir = new File(env.basedir, project.@path)
                    def gitName = new File(project.@name).getName().split("\\.").first()
                    println gitName
                    def bundleFile = new File(targetDir, "${gitName}.bundle")
                    Git.createFeatureBundle(remoteBranch, dir, bundleFile)
                }
        )
    }

    static void forEachWithProjectDirExists(RepoEnv env, int parallel, Closure action) {
        RepoManifest.forEach(env, parallel,
                { Node project -> return RepoManifest.projectDirExists(env, project) },
                action
        )
    }

    static Map<String, String> status(RepoEnv env, int parallel) {
        Map<String, String> result = new HashMap<>()
        forEachWithProjectDirExists(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    def branch = Git.getBranch(dir)
                    def remoteName = RepoManifest.getRemoteName(env)
                    def remoteBranch = "$remoteName/$branch"
                    def status = Git.status(dir)
                    def unpushed = Git.logUnpushed(dir, remoteBranch)
                    result.put(project.@path, status + '\n' + unpushed)
                }
        )
        return result
    }

    static Map<String, String> grep(RepoEnv env, int parallel, String exp) {
        Map<String, String> result = new HashMap<>()
        forEachWithProjectDirExists(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    def grepResult = Git.grep(dir, exp)
                    synchronized (result) {
                        result.put(project.@path, grepResult)
                    }
                }
        )
        return result
    }

    static void mergeAbort(RepoEnv env, int parallel) {
        forEachWithProjectDirExists(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.mergeAbort(dir)
                }
        )
    }

    static void stash(RepoEnv env, int parallel) {
        forEachWithProjectDirExists(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.stash(dir)
                }
        )
    }

    static void stashPop(RepoEnv env, int parallel) {
        forEachWithProjectDirExists(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.stashPop(dir)
                }
        )
    }

    static void pushFeatureBranch(RepoEnv env, int parallel, String featureBranch, boolean setUpstream) {
        RepoManifest.forEach(env, parallel,
                // use only existing local componentn whith have featureBranch
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    return RepoManifest.projectDirExists(env, project) && Git.branchPresent(dir, featureBranch)
                },
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.pushBranch(dir, project.@remote, featureBranch, setUpstream)
                }
        )
    }

    static void pushManifestBranch(RepoEnv env, int parallel, boolean setUpstream) {
        forEachWithProjectDirExists(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    def manifestBranch = RepoManifest.getBranch(env, project.@path)
                    Git.pushBranch(dir, project.@remote, manifestBranch, setUpstream)
                }
        )
    }

    static void addTagToCurrentHeads(RepoEnv env, int parallel, String tag) {
        forEachWithProjectDirExists(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.addTagToCurrentHead(dir, tag)
                }
        )
    }

    static void pushTag(RepoEnv env, int parallel, String tag) {
        forEachWithProjectDirExists(env, parallel,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.pushTag(dir, project.@remote, tag)
                }
        )
    }

    static void checkoutTag(RepoEnv env, int parallel, String tag) {
        RepoManifest.forEach(env, parallel,
                // use only existing local componentn whith have tag
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    return RepoManifest.projectDirExists(env, project) && Git.tagPresent(dir, tag)
                },
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.checkoutTag(dir, tag)
                }
        )
    }

}
