package repo.build
import java.io.File
import java.io.IOException

import org.apache.logging.log4j.Logger;

import groovy.transform.CompileStatic;;;
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager

@CompileStatic
class ExecuteProcess {
    static Logger logger = LogManager.getLogger(ExecuteProcess.class)
    
    static String executeCmd0(File dir, String cmd) {
        logger.debug("execute command '$cmd' in '$dir'")
        ProcessBuilder builder = new ProcessBuilder( cmd.split(' ') )
        builder.directory = dir

        builder.redirectErrorStream(true)

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
        if( exitValue != 0 ) {
            throw new RuntimeException("command '$cmd' has exit code $exitValue");
        }
        return writer.buffer.toString()
    }
}
