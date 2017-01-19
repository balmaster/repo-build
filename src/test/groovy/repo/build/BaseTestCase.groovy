package repo.build

import java.nio.file.FileSystems
import java.nio.file.Files

/**
 */
abstract class BaseTestCase extends GroovyTestCase {
    protected Sandbox sandbox
    protected RepoEnv env

    File createTempDir() {
        return Files.createTempDirectory(
                FileSystems.getDefault().getPath('target'), 'sandbox').toFile()
    }


}
