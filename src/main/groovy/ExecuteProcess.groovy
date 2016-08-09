import java.io.File;

class ExecuteProcess {
    static String executeCmd0(File dir, String cmd) {
        println "execute command '$cmd' in '$dir'"
        def proc = cmd.execute(null, dir)
        def exitCode = proc.waitFor()
        if( exitCode != 0 ) {
            println "${proc.getErrorStream()}"
            println proc.text
            throw new RuntimeException("command '$cmd' has exit code $exitCode");
        }
        println "${proc.getErrorStream()}"
        return proc.text;
    }
    
    
}
