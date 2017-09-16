package repo.build

import java.nio.file.FileSystems
import java.nio.file.Files
/**
 */
abstract class BaseTestCase extends groovy.test.GroovyAssert {
    protected Sandbox sandbox
    protected RepoEnv env
    protected ActionContext context
    protected CliOptions options

    void setUp() throws Exception {
        env = new RepoEnv(createTempDir())
        def cli = CliBuilderFactory.build(null)
        options = new CliOptions(cli.parse(getArgs()))
        context = new ActionContext(env, null, options, new DefaultParallelActionHandler())
    }

    static String getArgs() {
        return "-j 2"
    }

    static File createTempDir() {
        return Files.createTempDirectory(
                FileSystems.getDefault().getPath('target'), 'sandbox').toFile()
    }


}
