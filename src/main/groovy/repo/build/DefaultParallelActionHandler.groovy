package repo.build

import groovy.transform.CompileStatic

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
@CompileStatic
class DefaultParallelActionHandler implements ActionHandler {
    void beginAction(ActionContext context) {

    }

    void endAction(ActionContext context) {
        synchronized (this) {
            if (!hasParallelParent(context)) {
                logOutputTree(context)
            } else if (RepoManifest.ACTION_FOR_EACH_ITERATION == context.getId()) {
                logOutputTree(context)
            }
        }
    }

    boolean hasParallelParent(ActionContext parentContext) {
        def context = parentContext
        while (context != null) {
            if (RepoManifest.ACTION_FOR_EACH == context.getId()) {
                return true
            }
            context = context.getParent()
        }
        return false
    }

    void logOutputTree(ActionContext context) {
        if (!context.output) {
            for (ActionContext childContext : context.childList) {
                logOutputTree(childContext)
            }
            logOutput(context)
            context.output = true
        }
    }

    void logOutput(ActionContext context) {
        context.processOutList.each({ stream ->
            System.out.write(stream.toByteArray())
        })
    }
}
