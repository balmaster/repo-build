package repo.build
/**
 */
class GitFeatureTest extends BaseTestCase {

    @Override
    protected void setUp() throws Exception {
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

    void testCloneManifest() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')
        env.openManifest()
        assertEquals(2, env.manifest.project.findAll().size)
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'manifest')))
    }

    void testUpdateManifest() {
        def dir = new File(env.basedir, 'manifest')
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')
        // change manifest
        def newFile = new File(url, 'test')
        newFile.createNewFile()
        Git.add(context, url, 'test')
        Git.commit(context, url, 'test')

        GitFeature.updateManifest(context, Git.getBranch(context, dir))
        // check new file exists
        assertTrue(new File(dir, 'test').exists())
    }

    void testSync() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        GitFeature.sync(context)
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c1')))
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c2')))
    }

    void testSwitchNone() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c1')))
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c2')))
    }

    void testSwitchC1() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                })

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')
        assertEquals('feature/1', Git.getBranch(context, new File(env.basedir, 'c1')))
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c2')))
    }

    void testSwitchManifest() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                    Git.createBranch(sandbox.context, dir, 'feature/2')
                })

        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                })

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')
        GitFeature.switch(context, 'feature/2')
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c2')))
    }

    void testSwitchTask() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                    Git.createBranch(sandbox.context, dir, 'task/1')
                })

        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                })

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1', 'task/1')
        assertEquals('task/1', Git.getBranch(context, new File(env.basedir, 'c1')))
    }

    void testSwitchTaskFeatureDasntExists() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'task/1')
                })

        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                })

        GitFeature.sync(context)
        try {
            GitFeature.switch(context, 'feature/1', 'task/1')
            fail()
        }
        catch (Exception e) {
            assertEquals('Project c1 error task task/1 exists but feature/1 not exists', e.message)
            assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c1')))
        }
    }


    void testReleaseMergeFeature() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    Git.add(sandbox.context, dir, 'test')
                    Git.commit(sandbox.context, dir, 'test')
                })

        GitFeature.sync(context)

        GitFeature.releaseMergeFeature(context, 'feature/1', true)

        assertEquals('prepareBuild', Git.getBranch(context, new File(env.basedir, 'c1')))
        assertTrue(new File(new File(env.basedir, 'c1'), 'test').exists())
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c2')))
    }

    void testFeatureMergeRelease() {
        def context = new ActionContext(env, null, options, new DefaultParallelActionHandler())
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(context, dir, 'feature/1')
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    Git.add(context, dir, 'test')
                    Git.commit(context, dir, 'test')
                })

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')

        GitFeature.featureMergeRelease(context, 'feature/1')

        assertEquals('feature/1', Git.getBranch(context, new File(env.basedir, 'c1')))
        assertTrue(new File(new File(env.basedir, 'c1'), 'test').exists())
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c2')))
    }

    void testTaskMergeFeature() {
        def context = new ActionContext(env, null, options, new DefaultParallelActionHandler())
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    // create task branch
                    def c1Dir = new File(sandbox.env.basedir, 'c1')
                    Git.createBranch(context, c1Dir, 'task/1')
                    Git.createBranch(context, dir, 'feature/1')
                    Git.checkout(context, dir, 'feature/1')
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    newFile.text = 'test'
                    Git.add(context, dir, 'test')
                    Git.commit(context, dir, 'test')
                })

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1', 'task/1')

        GitFeature.taskMergeFeature(context, 'task/1', 'feature/1')

        def c1Dir = new File(env.basedir, 'c1')
        def testFile = new File(c1Dir, 'test')

        assertEquals('task/1', Git.getBranch(context, new File(env.basedir, 'c1')))
        assertEquals('test', testFile.text)
        assertTrue(new File(new File(env.basedir, 'c1'), 'test').exists())
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c2')))
    }

    void testStatus() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        GitFeature.sync(context)

        def c1Dir = new File(env.basedir, 'c1')
        def newFile = new File(c1Dir, 'new')
        newFile.text = 'new'

        def unpushedFile = new File(c1Dir, 'unpushed')
        unpushedFile.text = 'unpushed'
        Git.add(context, c1Dir, 'unpushed')
        Git.commit(context, c1Dir, 'unpushed')

        def result = GitFeature.status(context)
        assertTrue(result.get('c1').contains('?? new'))
        assertTrue(result.get('c1').contains('unpushed'))
        assertEquals('\n', result.get('c2'))
    }

    void testStatusUnpushed() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        GitFeature.sync(context)

        def c1Dir = new File(env.basedir, 'c1')

        Git.createBranch(context, c1Dir, 'newBranch')
        Git.checkout(context, c1Dir, 'newBranch')

        def newFile = new File(c1Dir, 'new')
        newFile.text = 'new'

        def unpushedFile = new File(c1Dir, 'unpushed')
        unpushedFile.text = 'unpushed'
        Git.add(context, c1Dir, 'unpushed')
        Git.commit(context, c1Dir, 'unpushed')

        def result = GitFeature.status(context)
        assertTrue(result.get('c1').contains('?? new'))
        assertTrue(result.get('c1').contains('Branch not pushed'))
        assertEquals('\n', result.get('c2'))
    }

    void testGrep() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    newFile.text = 'TEST123'
                    Git.add(sandbox.context, dir, 'test')
                    Git.commit(sandbox.context, dir, 'test')
                })

        GitFeature.sync(context)

        def result = GitFeature.grep(context, '123')
        assertEquals('test:TEST123\n', result.get('c1'))
        assertEquals('', result.get('c2'))

    }

    void testStash() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    newFile.text = 'TEST123'
                    Git.add(sandbox.context, dir, 'test')
                    Git.commit(sandbox.context, dir, 'test')
                })

        GitFeature.sync(context)

        def file = new File(env.basedir, 'c1/test')
        assertEquals('TEST123', file.text)
        // update file
        file.text = '123TEST'
        GitFeature.stash(context)
        assertEquals('TEST123', file.text)
        GitFeature.stashPop(context)
        assertEquals('123TEST', file.text)
    }


    void testPushFeature() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                })

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')

        // modify branch
        def c1Dir = new File(env.basedir, 'c1')
        def c1File = new File(c1Dir, 'README.md')
        c1File.text = 'update'
        Git.add(context, c1Dir, 'README.md')
        Git.commit(context, c1Dir, 'update')

        // create branch
        def c2Dir = new File(env.basedir, 'c2')
        Git.createBranch(context, c2Dir, 'feature/1')
        Git.checkout(context, c2Dir, 'feature/1')
        def c2File = new File(c2Dir, 'README.md')
        c2File.text = 'update'
        Git.add(context, c2Dir, 'README.md')
        Git.commit(context, c2Dir, 'update')

        GitFeature.pushFeatureBranch(context, 'feature/1', true)

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.checkout(sandbox.context, dir, 'feature/1')
                    assertEquals('update', new File(dir, 'README.md').text)
                })
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.checkout(sandbox.context, dir, 'feature/1')
                    assertEquals('update', new File(dir, 'README.md').text)
                })
    }


    void testPushManifest() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        // for enable push to master
        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'master1')
                    Git.checkout(sandbox.context, dir, 'master1')
                })
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(sandbox.context, dir, 'master1')
                    Git.checkout(sandbox.context, dir, 'master1')
                })


        GitFeature.sync(context)

        // modify branch
        def c1Dir = new File(env.basedir, 'c1')
        def c1File = new File(c1Dir, 'README.md')
        c1File.text = 'update'
        Git.add(context, c1Dir, 'README.md')
        Git.commit(context, c1Dir, 'update')

        // create branch
        def c2Dir = new File(env.basedir, 'c2')
        def c2File = new File(c2Dir, 'README.md')
        c2File.text = 'update'
        Git.add(context, c2Dir, 'README.md')
        Git.commit(context, c2Dir, 'update')

        GitFeature.pushManifestBranch(context, true)

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Git.checkout(sandbox.context, dir, 'master')
                    assertEquals('update', new File(dir, 'README.md').text)
                })
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.checkout(sandbox.context, dir, 'master')
                    assertEquals('update', new File(dir, 'README.md').text)
                })
    }


    void testPushTag() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        GitFeature.sync(context)

        GitFeature.addTagToCurrentHeads(context, '1')
        GitFeature.pushTag(context, '1')


        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    assertTrue(Git.tagPresent(sandbox.context, dir, '1'))
                })
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    assertTrue(Git.tagPresent(sandbox.context, dir, '1'))
                })

    }

    void testCheckoutTag() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        GitFeature.sync(context)

        GitFeature.addTagToCurrentHeads(context, '1')
        GitFeature.pushTag(context, '1')

        GitFeature.sync(context)

        // modify branch
        def c1Dir = new File(env.basedir, 'c1')
        def c1File = new File(c1Dir, 'README.md')
        c1File.text = 'update'
        Git.add(context, c1Dir, 'README.md')
        Git.commit(context, c1Dir, 'update')

        GitFeature.checkoutTag(context, '1')

        assertEquals('', new File(c1Dir, 'README.md').text)
    }


    void testCheckoutTagWithNewComponent() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        GitFeature.sync(context)

        GitFeature.addTagToCurrentHeads(context, '1')
        GitFeature.pushTag(context, '1')

        sandbox
                .newGitComponent('c3')
                .component('manifest',
                { Sandbox sandbox, File dir ->
                    sandbox.buildManifest(dir)
                    Git.add(sandbox.context, dir, 'default.xml')
                    Git.commit(sandbox.context, dir, 'add_c3')
                })

        GitFeature.sync(context)

        // modify branch
        def c1Dir = new File(env.basedir, 'c1')
        def c1File = new File(c1Dir, 'README.md')
        c1File.text = 'update'
        Git.add(context, c1Dir, 'README.md')
        Git.commit(context, c1Dir, 'update')

        GitFeature.checkoutTag(context, '1')

        assertEquals('', new File(c1Dir, 'README.md').text)
    }

}
