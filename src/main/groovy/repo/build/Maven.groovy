package repo.build

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.InvocationResult
import org.apache.maven.shared.invoker.Invoker

/**
 */
class Maven {

    static void execute(File pomFile, Closure handleRequest, Closure handleResult) {
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

    static void execute(File pomFile, Closure handleRequest) {
        execute(pomFile, handleRequest, {})
    }
}


