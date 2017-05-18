package repo.build
/**
 */
class RepoManifestTest extends BaseTestCase {

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

    void testPropogateComponentError() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')
        env.openManifest()
        try {
            RepoManifest.forEach(context,
                    { ActionContext actionContext, project ->
                        if (project.@path == 'c1') {
                            throw new RepoBuildException('test')
                        }
                    })
            fail()
        }
        catch (RepoBuildException e) {
            assertEquals('Project c1 error test', e.message)
        }
    }


}
