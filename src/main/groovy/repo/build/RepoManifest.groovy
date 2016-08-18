package repo.build
class RepoManifest {
    static final String BUILD = "build"

    static void forEach(RepoEnv env, Closure filter, Closure action) {
        env.manifest.project
                .findAll {  filter(it) }
                .each { action(it) }
    }

    static void forEachWithFeatureBranch(RepoEnv env, Closure action, String branch) {
        def remoteBranch = Git.getRemoteBranch(env, branch)

        forEach(env, {   project ->
            Git.branchPresent(new File(env.basedir,project.@path), remoteBranch) && !BUILD.equals(project.@path)
        },
        action)
    }


    static String getBranch(RepoEnv env, String projectPath) {
        return env.manifest.project
                .findAll {
                    projectPath.equals(it.@path)
                }
                .first()
                .@revision
                .replaceFirst("refs/heads/","")
    }
}
