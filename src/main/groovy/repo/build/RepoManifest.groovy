package repo.build

import java.io.File;

class RepoManifest {
    static final String BUILD = "build"

    static String getRemoteBranch(RepoEnv env, String branch) {
        def remoteName = env.manifest.remote[0].@name
        return "$remoteName/$branch"
    }

    static void forEach(RepoEnv env, Closure filter, Closure action) {
        env.manifest.project
                .findAll {  filter(it) }
                .each { action(it) }
    }

    static void forEachWithFeatureBranch(RepoEnv env, Closure action, String branch) {
        def remoteBranch = getRemoteBranch(env, branch)

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

    static void switchToBranch( RepoEnv env, String branch ) {
        def remoteBranch = getRemoteBranch(env, branch)

        RepoManifest.forEach(env, { project ->
            !BUILD.equals(project.@path)
        }, { Node project ->
            Git.checkoutUpdate(env, branch, remoteBranch, new File(env.basedir,project.@path))
        })
    }

    static void mergeFeatureBranch( RepoEnv env, String branch ) {
        def remoteBranch = getRemoteBranch(env, branch)

        forEachWithFeatureBranch(env, { Node project ->
            def dir = new File(env.basedir, project.@path)
            println "branch $remoteBranch found in ${project.@path}"
            def startCommit = project.@revision.replaceFirst("refs/heads", env.manifest.remote[0].@name)
            Git.mergeFeatureBranch(env, branch, remoteBranch, startCommit, dir)
        }, branch)
    }

    static void createFeatureBundles( RepoEnv env, String branch, File targetDir ) {
        def remoteBranch = getRemoteBranch(env, branch)

        forEachWithFeatureBranch(env, { Node project ->
            def dir = new File(env.basedir, project.@path)
            def gitName = new File(project.@name).getName().split("\\.").first()
            println gitName
            def bundleFile = new File(targetDir,"${gitName}.bundle")
            Git.createFeatureBundle(env, remoteBranch, dir, bundleFile)
        }, branch)
    }
}
