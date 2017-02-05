package repo.build

/**
 */
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

    static void sync(RepoEnv env) {
        def manifestDir = GitFeature.getManifestDir(env)
        if (manifestDir.exists()) {
            def manifestBranch = Git.getBranch(manifestDir)
            if ("HEAD" == manifestBranch) {
                throw new RepoBuildException("manifest branch must be local, use: repo-build -b <manifestBranch> init")
            }
            updateManifest(env, manifestBranch)
            env.openManifest()
            fetchUpdate(env)
        } else {
            throw new RepoBuildException("manifest dir $manifestDir not found")
        }
    }

    static void mergeRelease(RepoEnv env, String featureBranch) {
        // для всех компонентов в кторых ест фича бранч
        RepoManifest.forEachWithFeatureBranch(env,
                { project ->
                    def manifestBranch = RepoManifest.getBranch(env, project.@path)
                    Git.merge(manifestBranch, new File(env.basedir, project.@path))
                }, featureBranch)
    }

    static void 'switch'(RepoEnv env, String branch) {
        def remoteBranch = RepoManifest.getRemoteBranch(env, branch)
        RepoManifest.forEach(env,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    // переключаемся только если есть локальная или удаленная фича ветка
                    if (Git.branchPresent(dir, branch) || Git.branchPresent(dir, remoteBranch)) {
                        Git.checkoutUpdate(branch, remoteBranch, new File(env.basedir, project.@path))
                    }
                }
        )
    }

    static void fetchUpdate(RepoEnv env) {
        def remoteName = RepoManifest.getRemoteName(env)
        def remoteBaseUrl = RepoManifest.getRemoteBaseUrl(env)
        RepoManifest.forEach(env,
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

    static void mergeFeature(RepoEnv env, String branch, Boolean mergeAbort) {
        def remoteBranch = RepoManifest.getRemoteBranch(env, branch)
        RepoManifest.forEachWithFeatureBranch(env,
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

    static void createFeatureBundles(RepoEnv env, File targetDir, String branch) {
        def remoteBranch = RepoManifest.getRemoteBranch(env, branch)

        RepoManifest.forEachWithFeatureBranch(env,
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

    static void createManifestBundles(RepoEnv env, File targetDir) {

        RepoManifest.forEach(env,
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

    static void forEachWithProjectDirExists(RepoEnv env, Closure action) {
        RepoManifest.forEach(env,
                { Node project -> return RepoManifest.projectDirExists(env, project) },
                action
        )
    }

    static Map<String, String> status(RepoEnv env) {
        Map<String, String> result = new HashMap<>()
        forEachWithProjectDirExists(env,
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

    static Map<String, String> grep(RepoEnv env, String exp) {
        Map<String, String> result = new HashMap<>()
        forEachWithProjectDirExists(env,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    result.put(project.@path, Git.grep(dir, exp))
                }
        )
        return result
    }

    static void mergeAbort(RepoEnv env) {
        forEachWithProjectDirExists(env,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.mergeAbort(dir)
                }
        )
    }

    static void stash(RepoEnv env) {
        forEachWithProjectDirExists(env,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.stash(dir)
                }
        )
    }

    static void stashPop(RepoEnv env) {
        forEachWithProjectDirExists(env,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.stashPop(dir)
                }
        )
    }

    static void pushFeatureBranch(RepoEnv env, String featureBranch, boolean setUpstream) {
        RepoManifest.forEach(env,
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

    static void pushManifestBranch(RepoEnv env, boolean setUpstream) {
        forEachWithProjectDirExists(env,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    def manifestBranch = RepoManifest.getBranch(env, project.@path)
                    Git.pushBranch(dir, project.@remote, manifestBranch, setUpstream)
                }
        )
    }

}
