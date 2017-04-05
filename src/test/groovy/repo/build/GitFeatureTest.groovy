package repo.build
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

        GitFeature.sync(env, 2)
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c1')))
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c2')))
    }

    void testSwitchNone() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        GitFeature.sync(env, 2)
        GitFeature.switch(env, 2, 'feature/1')
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

        GitFeature.sync(env, 2)
        GitFeature.switch(env, 2, 'feature/1')
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

        GitFeature.sync(env, 2)

        GitFeature.mergeFeature(env, 2, 'feature/1', true)

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

        GitFeature.sync(env, 2)
        GitFeature.switch(env, 2, 'feature/1')

        GitFeature.mergeRelease(env, 2, 'feature/1')

        assertEquals('feature/1', Git.getBranch(new File(env.basedir, 'c1')))
        assertTrue(new File(new File(env.basedir, 'c1'), 'test').exists())
        assertEquals('master', Git.getBranch(new File(env.basedir, 'c2')))
    }

    void testStatus() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        GitFeature.sync(env, 2)

        def c1Dir = new File(env.basedir, 'c1')
        def newFile = new File(c1Dir, 'new')
        newFile.text = 'new'

        def unpushedFile = new File(c1Dir, 'unpushed')
        unpushedFile.text = 'unpushed'
        Git.add(c1Dir, 'unpushed')
        Git.commit(c1Dir, 'unpushed')

        def result = GitFeature.status(env, 2 )
        assertTrue(result.get('c1').contains('?? new'))
        assertTrue(result.get('c1').contains('unpushed'))
        assertEquals('\n', result.get('c2'))
    }

    void testGrep() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    newFile.text = 'TEST123'
                    Git.add(dir, 'test')
                    Git.commit(dir, 'test')
                })

        GitFeature.sync(env, 2)

        def result = GitFeature.grep(env, 2, '123')
        assertEquals('test:TEST123\n', result.get('c1'))
        assertEquals('', result.get('c2'))

    }

    void testStash() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    newFile.text = 'TEST123'
                    Git.add(dir, 'test')
                    Git.commit(dir, 'test')
                })

        GitFeature.sync(env, 2)

        def file = new File(env.basedir, 'c1/test')
        assertEquals('TEST123', file.text)
        // update file
        file.text = '123TEST'
        GitFeature.stash(env, 2)
        assertEquals('TEST123', file.text)
        GitFeature.stashPop(env, 2)
        assertEquals('123TEST', file.text)
    }


    void testPushFeature() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(dir, 'feature/1')
                })

        GitFeature.sync(env, 2)
        GitFeature.switch(env, 2, 'feature/1')

        // modify branch
        def c1Dir = new File(env.basedir, 'c1')
        def c1File = new File(c1Dir, 'README.md')
        c1File.text = 'update'
        Git.add(c1Dir, 'README.md')
        Git.commit(c1Dir, 'update')

        // create branch
        def c2Dir = new File(env.basedir, 'c2')
        Git.createBranch(c2Dir, 'feature/1')
        Git.checkout(c2Dir, 'feature/1')
        def c2File = new File(c2Dir, 'README.md')
        c2File.text = 'update'
        Git.add(c2Dir, 'README.md')
        Git.commit(c2Dir, 'update')

        GitFeature.pushFeatureBranch(env, 2, 'feature/1', true)

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.checkout(dir, 'feature/1')
                    assertEquals('update', new File(dir, 'README.md').text)
                })
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.checkout(dir, 'feature/1')
                    assertEquals('update', new File(dir, 'README.md').text)
                })
    }


    void testPushManifest() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        // for enable push to master
        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(dir, 'master1')
                    Git.checkout(dir, 'master1')
                })
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(dir, 'master1')
                    Git.checkout(dir, 'master1')
                })


        GitFeature.sync(env, 2)

        // modify branch
        def c1Dir = new File(env.basedir, 'c1')
        def c1File = new File(c1Dir, 'README.md')
        c1File.text = 'update'
        Git.add(c1Dir, 'README.md')
        Git.commit(c1Dir, 'update')

        // create branch
        def c2Dir = new File(env.basedir, 'c2')
        def c2File = new File(c2Dir, 'README.md')
        c2File.text = 'update'
        Git.add(c2Dir, 'README.md')
        Git.commit(c2Dir, 'update')

        GitFeature.pushManifestBranch(env, 2, true)

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.checkout(dir, 'master')
                    assertEquals('update', new File(dir, 'README.md').text)
                })
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.checkout(dir, 'master')
                    assertEquals('update', new File(dir, 'README.md').text)
                })
    }


    void testPushTag() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        GitFeature.sync(env, 2)

        GitFeature.addTagToCurrentHeads(env, 2, '1')
        GitFeature.pushTag(env, 2, '1')


        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    assertTrue(Git.tagPresent(dir, '1'))
                })
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    assertTrue(Git.tagPresent(dir, '1'))
                })

    }

    void testCheckoutTag() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        GitFeature.sync(env, 2)

        GitFeature.addTagToCurrentHeads(env, 2, '1')
        GitFeature.pushTag(env, 2, '1')

        GitFeature.sync(env, 2)

        // modify branch
        def c1Dir = new File(env.basedir, 'c1')
        def c1File = new File(c1Dir, 'README.md')
        c1File.text = 'update'
        Git.add(c1Dir, 'README.md')
        Git.commit(c1Dir, 'update')

        GitFeature.checkoutTag(env, 2, '1')

        assertEquals('', new File(c1Dir, 'README.md').text)
    }


    void testCheckoutTagWithNewComponent() {
        def url = new File(sandbox.basedir, 'manifest')
        GitFeature.cloneManifest(env, url.getAbsolutePath(), 'master')

        GitFeature.sync(env, 2)

        GitFeature.addTagToCurrentHeads(env, 2, '1')
        GitFeature.pushTag(env, 2, '1')

        sandbox
                .newGitComponent('c3')
                .component('manifest',
                { Sandbox sandbox, File dir ->
                    sandbox.buildManifest(dir)
                    Git.add(dir, 'default.xml')
                    Git.commit(dir, 'add_c3')
                })

        GitFeature.sync(env, 2)

        // modify branch
        def c1Dir = new File(env.basedir, 'c1')
        def c1File = new File(c1Dir, 'README.md')
        c1File.text = 'update'
        Git.add(c1Dir, 'README.md')
        Git.commit(c1Dir, 'update')

        GitFeature.checkoutTag(env, 2, '1')

        assertEquals('', new File(c1Dir, 'README.md').text)
    }

}
