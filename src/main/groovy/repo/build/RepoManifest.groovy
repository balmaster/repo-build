package repo.build

import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class RepoManifest {
    static Logger logger = LogManager.getLogger(RepoManifest.class)

    static String getRemoteName(RepoEnv env) {
        return env.manifest.remote[0].@name
    }

    static String getRemoteBaseUrl(RepoEnv env) {
        return env.manifest.remote[0].@fetch
    }

    static boolean projectDirExists(env, project) {
        def dir = new File(env.basedir, project.@path)
        return dir.exists()
    }

    @CompileStatic
    static String getRemoteBranch(RepoEnv env, String branch) {
        def remoteName = getRemoteName(env)
        return "$remoteName/$branch"
    }

    static void forEach(RepoEnv env, Closure filter, Closure action) {
        forEach(env, filter, action,
                { project ->
                    def path = project.@path
                    logger.info("$path")
                },
                {}
        )
    }

    static void forEach(RepoEnv env, Closure filter, Closure action, Closure logHeader, Closure logFooter) {
        env.manifest.project
                .findAll { filter(it) }
                .each { project ->
            def path = project.@path
            if (logHeader != null) {
                logHeader(project)
            }
            action(project)
            if (logFooter != null) {
                logFooter(project)
            }
        }
    }

    @CompileStatic
    static void forEach(RepoEnv env, Closure action) {
        forEach(env, { true }, action)
    }

    static void forEachWithFeatureBranch(RepoEnv env, Closure action, String branch) {
        def remoteBranch = getRemoteBranch(env, branch)

        forEach(env,
                { project ->
                    Git.branchPresent(new File(env.basedir, project.@path), remoteBranch)
                },
                action
        )
    }

    static String getBranch(RepoEnv env, String projectPath) {
        return env.manifest.project
                .findAll {
            projectPath.equals(it.@path)
        }
        .first()
                .@revision
                .replaceFirst("refs/heads/", "")
    }

}
