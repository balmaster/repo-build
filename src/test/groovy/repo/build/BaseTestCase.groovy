package repo.build

import java.nio.file.FileSystems
import java.nio.file.Files

/**
 */
abstract class BaseTestCase extends GroovyTestCase {
    protected Sandbox sandbox
    protected RepoEnv env
    protected ActionContext context
    protected CliOptions options

    protected void setUp() throws Exception {
        super.setUp()
        env = new RepoEnv(createTempDir())
        def cli = CliBuilderFactory.build(null)
        options = new CliOptions(cli.parse(getArgs()))
        context = new ActionContext(env, null, options, new DefaultParallelActionHandler())
    }

    String getArgs() {
        return "-j 2"
    }

    File createTempDir() {
        return Files.createTempDirectory(
                FileSystems.getDefault().getPath('target'), 'sandbox').toFile()
    }


}
