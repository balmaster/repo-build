package repo.build

import java.nio.file.FileSystems
import java.nio.file.Files

/**
 */
class GitFeatureTest extends BaseTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp()

        sandbox = new Sandbox(createTempDir())
                .newGitComponent('c1')
                .newGitComponent('c2')
                .newGitComponent('manifest',
                { Sandbox sandbox, File dir ->
                    sandbox.gitInitialCommit(dir)
                    sandbox.buildManifest(dir)
                    Git.add(dir, 'default.xml')
                    Git.commit(dir, 'manifest')
                })
        env = new RepoEnv(createTempDir())
    }

    void testCloneManifest() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')
        env.openManifest()
        assertEquals(2, env.manifest.project.findAll().size)
        assertEquals('master', Git.getBranch(new File(env.basedir, 'manifest')))
    }

    void testUpdateManifest() {
        def dir = new File(env.basedir, 'manifest')
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')
        // change manifest
        def newFile = new File(url, 'test')
        newFile.createNewFile()
        Git.add(url, 'test')
        Git.commit(url, 'test')

        GitFeature.updateManifest(env, Git.getBranch(dir))
        // check new file exists
        assertTrue(new File(dir, 'test').exists())
    }

    void testSync() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        GitFeature.sync(env)
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c1')))
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c2')))
    }

    void testSwitchNone() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        GitFeature.sync(env)
        GitFeature.switch(env, 'feature/1')
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c1')))
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c2')))
    }

    void testSwitchC1() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(dir, 'feature/1')
                })

        GitFeature.sync(env)
        GitFeature.switch(env, 'feature/1')
        assertEquals('feature/1', Git.getBranch(new File(env.basedir, 'c1')))
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c2')))
    }

    void testMergeFeature() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(dir, 'feature/1')
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    Git.add(dir, 'test')
                    Git.commit(dir, 'test')
                })

        GitFeature.sync(env)

        GitFeature.mergeFeature(env, 'feature/1', true)

        assertEquals('prepareBuild', Git.getBranch(new File(env.basedir, 'c1')))
        assertTrue(new File(new File(env.basedir, 'c1'), 'test').exists())
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c2')))
    }

    void testMergeRelease() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(dir, 'feature/1')
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    Git.add(dir, 'test')
                    Git.commit(dir, 'test')
                })

        GitFeature.sync(env)
        GitFeature.switch(env, 'feature/1')

        GitFeature.mergeRelease(env, 'feature/1')

        assertEquals('feature/1', Git.getBranch(new File(env.basedir, 'c1')))
        assertTrue(new File(new File(env.basedir, 'c1'), 'test').exists())
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c2')))
    }
}
