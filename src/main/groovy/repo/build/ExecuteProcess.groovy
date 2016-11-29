package repo.build

import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@CompileStatic
class ExecuteProcess {
    static Logger logger = LogManager.getLogger(ExecuteProcess.class)
    
    static String executeCmd0(File dir, String cmd) {
        return executeCmd0(dir, cmd, true)
    }

    static String executeCmd0(File dir, String cmd, boolean checkErrorCode) {
        executeCmd0(dir, cmd.split(' '), checkErrorCode)
    }

    static String executeCmd0(File dir, String[] args, boolean checkErrorCode) {
        String cmd = args.join(' ')
        logger.debug("execute command '$cmd' in '$dir'")

        ProcessBuilder builder = new ProcessBuilder()
                .command(args)
                .redirectErrorStream(true)
                .directory(dir)

        Process process = builder.start()

        def final StringWriter writer = new StringWriter()

        process.consumeProcessOutput(new OutputStream() {
                    @Override
                    void write(int b) throws IOException {
                        writer.write(b)
                        System.out.write(b)
                    }
                }, System.err)

        def exitValue = process.waitFor()
        if( checkErrorCode && exitValue != 0 ) {
            throw new RepoBuildException("command '$cmd' has exit code $exitValue");
        }
        return writer.buffer.toString()
    }
}
