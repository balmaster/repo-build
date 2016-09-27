package repo.build

import java.io.File

import org.apache.logging.log4j.Logger;

import groovy.transform.CompileStatic;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

class RepoManifest {
    static Logger logger = LogManager.getLogger(RepoManifest.class)

    static String getRemoteName(RepoEnv env) {
        return env.manifest.remote[0].@name
    }

    static String getRemoteBaseUrl(RepoEnv env) {
        return env.manifest.remote[0].@fetch
    }

    @CompileStatic
    static String getRemoteBranch(RepoEnv env, String branch) {
        def remoteName = getRemoteName(env)
        return "$remoteName/$branch"
    }

    static void forEach(RepoEnv env, Closure filter, Closure action) {
        env.manifest.project
                .findAll {  filter(it) }
                .each { action(it) }
    }

    @CompileStatic
    static void forEach(RepoEnv env, Closure action) {
        forEach(env, { true } , action)
    }

    static void forEachWithFeatureBranch(RepoEnv env, Closure action, String branch) {
        def remoteBranch = getRemoteBranch(env, branch)

        forEach(env, {   project ->
            Git.branchPresent(new File(env.basedir,project.@path), remoteBranch)
        },
        action)
    }

    static void forEachManifestBranch( RepoEnv env ) {
        def remoteName = getRemoteName(env)
        def remoteBaseUrl = getRemoteBaseUrl(env)
        RepoManifest.forEach(env, { Node project ->
            def branch = getBranch(env, project.@path)
            def remoteBranch = getRemoteBranch(env, branch)
            def dir = new File(env.basedir,project.@path)
            def name = project.@name
            if(new File(dir, ".git").exists()) {
                Git.fetch(env, remoteName, dir)
            } else {
                dir.mkdirs()
                Git.clone(env, "$remoteBaseUrl/$name", remoteName, dir)
            }
            Git.checkoutUpdate(env, branch, remoteBranch, dir)
        })
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
        RepoManifest.forEach(env,  { Node project ->
            Git.checkoutUpdate(env, branch, remoteBranch, new File(env.basedir,project.@path))
        })
    }

    static void fetchUpdate( RepoEnv env ) {
        def remoteName = getRemoteName(env)
        def remoteBaseUrl = getRemoteBaseUrl(env)
        RepoManifest.forEach(env, { Node project ->
            def branch = getBranch(env, project.@path)
            def remoteBranch = getRemoteBranch(env, branch)
            def dir = new File(env.basedir,project.@path)
            def name = project.@name
            if(new File(dir, ".git").exists()) {
                Git.fetch(env, remoteName, dir)
            } else {
                dir.mkdirs()
                Git.clone(env, "$remoteBaseUrl/$name", remoteName, dir)
            }
            Git.checkoutUpdate(env, branch, remoteBranch, dir)
        })
    }

    static void mergeFeatureBranch( RepoEnv env, String branch, Boolean mergeAbort ) {
        def remoteBranch = getRemoteBranch(env, branch)
        forEachWithFeatureBranch(env, { Node project ->
            def dir = new File(env.basedir, project.@path)
            println "branch $remoteBranch found in ${project.@path}"
            def startCommit = project.@revision.replaceFirst("refs/heads", env.manifest.remote[0].@name)
            if(mergeAbort) {
                try {
                    Git.mergeAbort( env, dir)
                } catch (Exception e) {
                    // skip
                }
            }
            Git.mergeFeatureBranch(env, branch, remoteBranch, startCommit, dir)
        }, branch)
    }

    static void createFeatureBundles( RepoEnv env, File targetDir, String branch ) {
        def remoteBranch = getRemoteBranch(env, branch)

        forEachWithFeatureBranch(env, { Node project ->
            def dir = new File(env.basedir, project.@path)
            def gitName = new File(project.@name).getName().split("\\.").first()
            println gitName
            def bundleFile = new File(targetDir,"${gitName}.bundle")
            Git.createFeatureBundle(env, remoteBranch, dir, bundleFile)
        }, branch)
    }

    static void createManifestBundles( RepoEnv env, File targetDir) {

        forEach(env, { Node project ->
            def remoteBranch = getRemoteBranch(env, getBranch(env, project.@path))
            def dir = new File(env.basedir, project.@path)
            def gitName = new File(project.@name).getName().split("\\.").first()
            println gitName
            def bundleFile = new File(targetDir,"${gitName}.bundle")
            Git.createFeatureBundle(env, remoteBranch, dir, bundleFile)
        })
    }

    static void createFeatureBundles( RepoEnv env, File targetDir ) {
        forEach(env, { Node project ->
            def dir = new File(env.basedir, project.@path)
            def branch = getBranch(env, project.@path)
            def remoteBranch = getRemoteBranch(env, branch)

            def gitName = new File(project.@name).getName().split("\\.").first()
            println gitName
            def bundleFile = new File(targetDir,"${gitName}.bundle")
            Git.createFeatureBundle(env, remoteBranch, dir, bundleFile)
        })
    }
}
