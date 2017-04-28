package repo.build

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
class DefaultParallelActionHandler implements ActionHandler {
    void beginAction(ActionContext context) {

    }

    void endAction(ActionContext context) {
        if (!hasParallelParent(context)) {
            logOutputTree(context)
        } else if (RepoManifest.ACTION_FOR_EACH == context.getId()) {
            logOutputTree(context)
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
        for (ActionContext childContext : context.childList) {
            logOutputTree(childContext)
        }
        logOutput(context)
    }

    void logOutput(ActionContext context) {
        context.processOutList.forEach({ stream ->
            System.out.write(stream.toByteArray())
        })
    }
}
