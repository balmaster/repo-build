package repo.build

import java.nio.file.FileSystems
import java.nio.file.Files

/**
 */
abstract class BaseTestCase extends GroovyTestCase {
    protected Sandbox sandbox
    protected RepoEnv env
    protected ActionContext context

    protected void setUp() throws Exception {
        super.setUp()
        env = new RepoEnv(createTempDir())
        context = new ActionContext(env, null, 2, new DefaultParallelActionHandler())
    }

    File createTempDir() {
        return Files.createTempDirectory(
                FileSystems.getDefault().getPath('target'), 'sandbox').toFile()
    }


}
