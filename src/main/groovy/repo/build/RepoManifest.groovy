package repo.build

import groovy.transform.CompileStatic
import groovyx.gpars.GParsPool
import org.apache.log4j.Logger

class RepoManifest {
    static Logger logger = Logger.getLogger(RepoManifest.class)

    static String getRemoteName(ActionContext context) {
        return context.env.manifest.remote[0].@name
    }

    static String getRemoteBaseUrl(ActionContext context) {
        if (context.options.getManifestRemote()){
            return context.options.getManifestRemote()
        } else {
            return context.env.manifest.remote[0].@fetch
        }
    }

    static boolean projectDirExists(ActionContext context, project) {
        def dir = new File(context.env.basedir, project.@path)
        return dir.exists()
    }

    @CompileStatic
    static String getRemoteBranch(ActionContext context, String branch) {
        def remoteName = getRemoteName(context)
        return "$remoteName/$branch"
    }

    static void forEach(ActionContext parentContext, Closure filter, Closure action) {
        forEach(parentContext, filter, action,
                { ActionContext actionContext, project ->
                    def path = project.@path
                    actionContext.newChildWriteOut("$path\n")
                },
                { ActionContext actionContext, project ->
                    actionContext.newChildWriteOut("\n")
                }
        )
    }

    public final static String ACTION_FOR_EACH = 'repoManifestForEach'
    public final static String ACTION_FOR_EACH_ITERATION = 'repoManifestForEachIteraction'

    static void forEach(ActionContext parentContext, Closure filter, Closure action, Closure logHeader, Closure logFooter) {
        def context = parentContext.newChild(ACTION_FOR_EACH)
        context.withCloseable {
            GParsPool.withPool(context.getParallel(), {
                parentContext.env.manifest.project
                        .eachParallel { project ->
                    def actionContext = context.newChild(ACTION_FOR_EACH_ITERATION)
                    actionContext.withCloseable {
                        try {
                            if (filter(actionContext, project)) {
                                if (logHeader != null) {
                                    logHeader(actionContext, project)
                                }
                                action(actionContext, project)
                                if (logFooter != null) {
                                    logFooter(actionContext, project)
                                }
                            }
                        }
                        catch (Exception e) {
                            def componentError = new RepoBuildException("Component ${project.@path} error ${e.message}", e)
                            if (actionContext.options.hasFae()) {
                                actionContext.addError(componentError)
                            } else {
                                throw componentError
                            }
                        }
                    }
                }
            })
        }
    }


    @CompileStatic
    static void forEach(ActionContext parentContext, Closure action) {
        forEach(parentContext,
                { ActionContext actionContext, Node project ->
                    return true
                },
                action)
    }

    static void forEachWithBranch(ActionContext parentContext, Closure action, String branch) {
        def remoteBranch = getRemoteBranch(parentContext, branch)

        forEach(parentContext,
                { ActionContext actionContext, project ->
                    Git.branchPresent(actionContext, new File(actionContext.env.basedir, project.@path), remoteBranch)
                },
                action
        )
    }

    static String getBranch(ActionContext context, String projectPath) {
        return context.env.manifest.project
                .findAll {
            (projectPath == it.@path)
        }
        .first()
                .@revision
                .replaceFirst("refs/heads/", "")
    }

}
