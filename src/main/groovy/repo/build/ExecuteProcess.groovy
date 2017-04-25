package repo.build

import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.fusesource.jansi.AnsiOutputStream

@CompileStatic
class ExecuteProcess {
    static Logger logger = Logger.getLogger(ExecuteProcess.class)
    static Object outSync = new Object();
    public static final String ACTION_EXECUTE_CMD0 = 'executeProcessExecuteCmd0'

    static String executeCmd0(File dir, String cmd) {
        return executeCmd0(dir, cmd, true)
    }

    static String executeCmd0(File dir, String cmd, boolean checkErrorCode) {
        executeCmd0(dir, cmd.split(' '), checkErrorCode)
    }

    static String executeCmd0(File dir, String[] args, boolean checkErrorCode) {
        String cmd = args.join(' ')
        logger.debug("execute '$cmd' in '$dir'")

        ProcessBuilder builder = new ProcessBuilder()
                .command(args)
                .redirectErrorStream(true)
                .directory(dir)

        Process process = builder.start()

        final StringWriter writer = new StringWriter()
        final ByteArrayOutputStream bufferStream = new ByteArrayOutputStream(10000);

        process.consumeProcessOutput(new OutputStream() {
            @Override
            void write(int b) throws IOException {
                writer.write(b)
                bufferStream.write(b)
            }
        }, System.err)

        def exitValue = process.waitFor()
        // for read process output
        Thread.sleep(100)

        def outStream = new AnsiOutputStream(System.out)
        if (bufferStream.size() > 0) {
            synchronized (outSync) {
                for (int b : bufferStream.toByteArray()) {
                    outStream.write(b);
                }
            }
        }

        if (checkErrorCode && exitValue != 0) {
            throw new RepoBuildException("name '$cmd' has exit code $exitValue");
        }
        return writer.buffer.toString()
    }

    static String executeCmd0(ActionContext parentContext, File dir, String cmd, boolean checkErrorCode) {
        def context = parentContext.newChild(ACTION_EXECUTE_CMD0)
        context.withCloseable {
            logger.debug("execute '$cmd' in '$dir'")

            ProcessBuilder builder = new ProcessBuilder()
                    .command(cmd.split(' '))
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

}
