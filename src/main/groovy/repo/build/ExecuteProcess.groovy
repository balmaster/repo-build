package repo.build

import groovy.transform.CompileStatic
import org.apache.log4j.Logger

@CompileStatic
class ExecuteProcess {
    static Logger logger = Logger.getLogger(ExecuteProcess.class)
    static Object outSync = new Object();
    public static final String ACTION_EXECUTE_CMD0 = 'executeProcessExecuteCmd0'

    static String executeCmd0(ActionContext parentContext, File dir, String[] cmd, boolean checkErrorCode) {
        def context = parentContext.newChild(ACTION_EXECUTE_CMD0)
        context.withCloseable {
            logger.debug("execute '$cmd' in '$dir'")

            ProcessBuilder builder = new ProcessBuilder()
                    .command(cmd)
                    .redirectErrorStream(true)
                    .directory(dir)

            Process process = builder.start()

            StringWriter writer = new StringWriter()
            def writeOutHandler = context.newWriteOutHandler()
            process.consumeProcessOutput(new OutputStream() {
                @Override
                void write(int b) throws IOException {
                    writer.write(b)
                    writeOutHandler(b)
                }
            }, System.err)

            def exitValue = process.waitFor()
            // for read process output
            Thread.sleep(100)

            if (checkErrorCode && exitValue != 0) {
                throw new RepoBuildException("name '$cmd' has exit code $exitValue");
            }
            return writer.buffer.toString()
        }
    }

    static String executeCmd0(ActionContext parentContext, File dir, String cmd, boolean checkErrorCode) {
        return executeCmd0(parentContext, dir, cmd.split(' '), checkErrorCode);
    }
}
