package repo.build

import groovy.transform.CompileStatic
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.InvocationResult
import org.apache.maven.shared.invoker.Invoker

/**
 */
class Maven {

    static void execute(ActionContext context, File pomFile, Closure handleRequest, Closure handleResult) {
        InvocationRequest request = new DefaultInvocationRequest()
        request.setPomFile(pomFile)
        handleRequest(request)

        Invoker invoker = new DefaultInvoker()
        try {
            InvocationResult result = invoker.execute(request)
            if (result.exitCode != 0) {
                throw new RepoBuildException("exitCode: " + result.exitCode)
            }
            handleResult(result)
        }
        catch (Exception e) {
            throw new RepoBuildException(e.getMessage(), e)
        }
    }

    static void execute(ActionContext context, File pomFile, Closure handleRequest) {
        execute(context, pomFile, handleRequest, {})
    }

    @CompileStatic
    static void execute(ActionContext context,
                        File pomFile,
                        List<String> goals,
                        Map<String, String> p) {
        execute(context, pomFile,
                { InvocationRequest req ->
                    MavenFeature.initInvocationRequest(req, context.getOptions())
                    req.setGoals(goals)
                    req.setInteractive(false)
                    req.getProperties().putAll(p)
                }
        )
    }
}


