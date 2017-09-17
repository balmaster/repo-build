package repo.build

import org.junit.Before
import org.junit.Test
/**
 */
class PomTest extends BaseTestCase {

    @Before
    void setUp() throws Exception {
        super.setUp()
        sandbox = new Sandbox(new RepoEnv(createTempDir()), options)
                .newGitComponent('c1')
                .newGitComponent('c2')
                .newGitComponent('manifest',
                { Sandbox sandbox, File dir ->
                    sandbox.gitInitialCommit(dir)
                    sandbox.buildManifest(dir)
                    Git.add(sandbox.context, dir, 'default.xml')
                    Git.commit(sandbox.context, dir, 'manifest')
                })
    }

    @Test
    void testBuildPomNoModules() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')
        GitFeature.sync(context)

        def pomFile = new File(sandbox.env.basedir, 'pom.xml')
        Pom.generateXml(context, 'master', pomFile)
        assertTrue(pomFile.exists())
        def pom = new XmlParser().parse(pomFile)
        assertEquals(0, pom.project.modules.module.findAll().size)
    }

    @Test
    void testBuildPomHasModules() {
        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    def newFile = new File(dir, 'pom.xml')
                    newFile.createNewFile()
                    Git.add(sandbox.context, dir, 'pom.xml')
                    Git.commit(sandbox.context, dir, 'pom')
                })

        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')
        GitFeature.sync(context)

        def pomFile = new File(sandbox.env.basedir, 'pom.xml')
        Pom.generateXml(context, 'master', pomFile)
        assertTrue(pomFile.exists())
        def pom = new XmlParser().parse(pomFile)
        assertEquals(1, pom.modules.module.findAll().size)
    }

}
