package repo.build

import groovy.transform.CompileStatic
import groovyx.gpars.GParsPool
import org.apache.log4j.Logger

import java.util.concurrent.Future

class RepoManifest {
    static Logger logger = Logger.getLogger(RepoManifest.class)

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

    static void forEach(RepoEnv env, int parallel, Closure filter, Closure<Future<ActionResult>> action) {
        forEach(env, parallel, filter, action,
                { project ->
                    def path = project.@path
                    logger.info("$path")
                },
                {}
        )
    }

    static void forEach(RepoEnv env, int parallel, Closure filter, Closure<Future<ActionResult>> action, Closure logHeader, Closure logFooter) {
        GParsPool.withPool(parallel, {
            env.manifest.project
                    .findAll { filter(it) }
                    .eachParallel { project ->
                def feature = action(project)
                if (logHeader != null) {
                    logHeader(project)
                }
                if(feature != null ) {
                    def actionResult = feature.get()
                }
                if (logFooter != null) {
                    logFooter(project)
                }
            }
        })
    }

    @CompileStatic
    static void forEach(RepoEnv env, int parallel, Closure<Future<ActionResult>> action) {
        forEach(env, parallel, { true }, action)
    }

    static void forEachWithFeatureBranch(RepoEnv env, int parallel, Closure<Future<ActionResult>> action, String branch) {
        def remoteBranch = getRemoteBranch(env, branch)

        forEach(env, parallel,
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
