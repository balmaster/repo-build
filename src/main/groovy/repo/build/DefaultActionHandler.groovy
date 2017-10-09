package repo.build

import groovy.transform.CompileStatic
import repo.build.filter.OutputFilter

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
@CompileStatic
class DefaultActionHandler implements ActionHandler {
    void beginAction(ActionContext context) {

    }

    void endAction(ActionContext context) {
        synchronized (this) {
            // print output
            def output = new ArrayList<ByteArrayOutputStream>()
            if (!hasParallelParent(context)) {
                buildOutput(context, output)
            } else if (RepoManifest.ACTION_FOR_EACH_ITERATION == context.getId()) {
                buildOutput(context, output)
            } else {
                return
            }
            applyPredicateAndPrint(context, output)
            // print errors
            if (context.parent == null) {
                if (context.getErrorFlag()) {
                    printErrors(context)
                    throw new RepoBuildException("Has errors")
                }
            }
        }
    }

    void printErrors(ActionContext context) {
        if (context.childList.size() > 0) {
            for (def child : context.childList) {
                printErrors(child)
            }
            if (context.errorList.size() > 0) {
                for (def error : context.errorList) {
                    System.out.println(error.getMessage())
                    if (context.options.isDebugMode()) {
                        error.printStackTrace()
                    }
                }
            }
        }
    }

    private static boolean hasParallelParent(ActionContext parentContext) {
        def context = parentContext
        while (context != null) {
            if (RepoManifest.ACTION_FOR_EACH == context.getId()) {
                return true
            }
            context = context.getParent()
        }
        return false
    }

    private void buildOutput(ActionContext context, ArrayList<ByteArrayOutputStream> output) {
        if (!context.output) {
            for (ActionContext childContext : context.childList) {
                buildOutput(childContext, output)
            }
            output.addAll(context.processOutList)
            context.output = true
        }
    }

    private String getCommandName(ActionContext actionContext) {
        if (actionContext.parent != null && actionContext.parent.id != null) {
            return getCommandName(actionContext.parent)
        } else {
            return actionContext.id
        }
    }

    private void applyPredicateAndPrint(ActionContext context, List<ByteArrayOutputStream> output) {
        def actionName = getCommandName(context)
        def filters = context.outputFilter.get(actionName)
        if (filters != null) {
            for (OutputFilter filter : filters) {
                if (!filter.apply(context, output)) return
            }
        }
        log(output)
    }

    static void log(List<ByteArrayOutputStream> output) {
        output.each({ stream ->
            System.out.write(stream.toByteArray())
        })
    }
}
