package repo.build

/**
 */
class PomTest extends BaseTestCase {

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

    void testBuildPomNoModules() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')
        GitFeature.sync(env)

        def pomFile = new File(sandbox.basedir, 'pom.xml')
        Pom.generateXml(env, 'master', pomFile)
        assertTrue(pomFile.exists())
        def pom = new XmlParser().parse(pomFile)
        assertEquals(0, pom.project.modules.module.findAll().size)
    }

    void testBuildPomHasModules() {
        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    def newFile = new File(dir, 'pom.xml')
                    newFile.createNewFile()
                    Git.add(dir, 'pom.xml')
                    Git.commit(dir, 'pom')
                })

        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')
        GitFeature.sync(env)

        def pomFile = new File(sandbox.basedir, 'pom.xml')
        Pom.generateXml(env, 'master', pomFile)
        assertTrue(pomFile.exists())
        def pom = new XmlParser().parse(pomFile)
        assertEquals(1, pom.modules.module.findAll().size)
    }

}
