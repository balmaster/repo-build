package repo.build

/**
 */
class GitFeature {
    static void mergeRelease(RepoEnv env, String featureBranch) {
        // для всех компонентов в кторых ест фича бранч
        RepoManifest.forEachWithFeatureBranch(env,
                { project ->
                    def manifestBranch = RepoManifest.getBranch(env, project.@path)
                    Git.merge(env, manifestBranch, new File(env.basedir, project.@path))
                }, featureBranch)
    }

    static void switchToBranch(RepoEnv env, String branch) {
        def remoteBranch = RepoManifest.getRemoteBranch(env, branch)
        RepoManifest.forEach(env,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    // переключаемся только если есть локальная или удаленная фича ветка
                    if (Git.branchPresent(dir, branch) || Git.branchPresent(dir, remoteBranch)) {
                        Git.checkoutUpdate(env, branch, remoteBranch, new File(env.basedir, project.@path))
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
                        Git.fetch(env, remoteName, dir)
                    } else {
                        dir.mkdirs()
                        Git.clone(env, "$remoteBaseUrl/$name", remoteName, dir)
                    }
                    Git.checkoutUpdate(env, branch, remoteBranch, dir)
                    Git.user(dir, env.props.getProperty("git.user.name"), env.props.getProperty("git.user.email"))
                })
    }

    static void mergeFeatureBranch(RepoEnv env, String branch, Boolean mergeAbort) {
        def remoteBranch = RepoManifest.getRemoteBranch(env, branch)
        RepoManifest.forEachWithFeatureBranch(env,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    println "branch $remoteBranch found in ${project.@path}"
                    def startCommit = project.@revision.replaceFirst("refs/heads", env.manifest.remote[0].@name)
                    if (mergeAbort) {
                        try {
                            Git.mergeAbort(env, dir)
                        } catch (Exception e) {
                            // skip
                        }
                    }
                    Git.mergeFeatureBranch(env, branch, remoteBranch, startCommit, dir)
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
                    Git.createFeatureBundle(env, remoteBranch, dir, bundleFile)
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
                    Git.createFeatureBundle(env, remoteBranch, dir, bundleFile)
                }
        )
    }

    static void createFeatureBundles(RepoEnv env, File targetDir) {
        RepoManifest.forEach(env,
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    def branch = RepoManifest.getBranch(env, project.@path)
                    def remoteBranch = RepoManifest.getRemoteBranch(env, branch)

                    def gitName = new File(project.@name).getName().split("\\.").first()
                    println gitName
                    def bundleFile = new File(targetDir, "${gitName}.bundle")
                    Git.createFeatureBundle(env, remoteBranch, dir, bundleFile)
                }
        )
    }

    static void status(RepoEnv env) {
        RepoManifest.forEach(env,
                { Node project -> return RepoManifest.projectDirExists(env, project) },
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    def branch = RepoManifest.getBranch(env, project.@path)
                    def remoteBranch = RepoManifest.getRemoteBranch(env, branch)
                    Git.status(env, dir)
                    Git.logUnpushed(env, dir, remoteBranch)
                }
        )
    }

    static void grep(RepoEnv env, String exp) {
        RepoManifest.forEach(env,
                { Node project -> return RepoManifest.projectDirExists(env, project) },
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.grep(env, dir, exp)
                }
        )
    }

    static void mergeAbort(RepoEnv env) {
        RepoManifest.forEach(env,
                { Node project -> return RepoManifest.projectDirExists(env, project) },
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.mergeAbort(env, dir)
                }
        )
    }

    static void stash(RepoEnv env) {
        RepoManifest.forEach(env,
                { Node project -> return RepoManifest.projectDirExists(env, project) },
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.stash(env, dir)
                }
        )
    }

    static void stashPop(RepoEnv env) {
        RepoManifest.forEach(env,
                { Node project -> return RepoManifest.projectDirExists(env, project) },
                { Node project ->
                    def dir = new File(env.basedir, project.@path)
                    Git.stashPop(env, dir)
                }
        )
    }
}
