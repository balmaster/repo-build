package repo.build

/**
 */
class PomTest extends BaseTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp()

        sandbox = new Sandbox(new RepoEnv(createTempDir()))
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
