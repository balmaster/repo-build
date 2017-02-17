package repo.build

import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.fusesource.jansi.AnsiOutputStream

@CompileStatic
class ExecuteProcess {
    static Logger logger = Logger.getLogger(ExecuteProcess.class)
    
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

        def final StringWriter writer = new StringWriter()
        def final outStream = new AnsiOutputStream(System.out)

        process.consumeProcessOutput(new OutputStream() {
                    @Override
                    void write(int b) throws IOException {
                        writer.write(b)
                        outStream.write(b)
                    }
                }, System.err)

        def exitValue = process.waitFor()
        // for read process output
        Thread.sleep(100)

        if( checkErrorCode && exitValue != 0 ) {
            throw new RepoBuildException("name '$cmd' has exit code $exitValue");
        }
        return writer.buffer.toString()
    }
}
