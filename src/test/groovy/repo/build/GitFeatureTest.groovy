package repo.build

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.springframework.boot.test.rule.OutputCapture
import repo.build.filter.OutputFilter
import repo.build.filter.UnpushedStatusFilter

/**
 */
class GitFeatureTest extends BaseTestCase {

    @Rule
    public OutputCapture outputCapture = new OutputCapture()

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
    void testCloneManifest() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')
        env.openManifest()
        assertEquals(2, env.manifest.project.findAll().size)
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'manifest')))
    }

    @Test
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

    @Test
    void testSync() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        GitFeature.sync(context)
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c1')))
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c2')))
    }

    @Test
    void testSwitchNone() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c1')))
        assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c2')))
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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
            assertEquals('Component c1 error task task/1 exists but feature/1 not exists', e.message)
            assertEquals('master', Git.getBranch(context, new File(env.basedir, 'c1')))
        }
    }

    @Test
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

    @Test
    void testFeatureMergeRelease() {
        def context = new ActionContext(env, null, options, new DefaultActionHandler())
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

    @Test
    void testTaskMergeFeature() {
        def context = new ActionContext(env, null, options, new DefaultActionHandler())
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

    @Test
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

        outputCapture.reset()

        GitFeature.status(context)
        def splitedOutput = outputCapture.toString().split('\n')
        assertEquals(9, splitedOutput.size())
        assertEquals("should contain 1 c1", 1,
                getByValueFromOutput("c1", splitedOutput).size())
        assertEquals("should contain 2 master branch", 2,
                getByValueFromOutput("master", splitedOutput).size())
        assertEquals("should contain 1 new file", 1,
                getByValueFromOutput("?? new", splitedOutput).size())
        assertEquals("should contain 1 c2 ", 1,
                getByValueFromOutput("c2", splitedOutput).size())
        assertEquals("should contain 1 empty string", 1,
                getByValueFromOutput("", splitedOutput).size())
        assertEquals("should contain 2 remote ref repository name", 2,
                containsValueFromFromOutput("refs/remotes/origin/master", splitedOutput).length)
        assertEquals("should contain 1 unpushed", 1,
                containsValueFromFromOutput("unpushed", splitedOutput).size())

        outputCapture.reset()

        ArrayList<OutputFilter> predicates = new ArrayList<>()
        predicates.add(new UnpushedStatusFilter())
        context.outputFilter.put(GitFeature.ACTION_STATUS, predicates)
        GitFeature.status(context)
        splitedOutput = outputCapture.toString().split('\n')

        assertEquals(5, splitedOutput.size())
        assertEquals("should contain 1 c1", 1,
                getByValueFromOutput("c1", splitedOutput).size())
        assertEquals("should contain 1 master branch", 1,
                getByValueFromOutput("master", splitedOutput).size())
        assertEquals("should contain 1 new file", 1,
                getByValueFromOutput("?? new", splitedOutput).size())
        assertEquals("should contain 1 remote ref repository name", 1,
                containsValueFromFromOutput("refs/remotes/origin/master", splitedOutput).length)
        assertEquals("should contain 1 unpushed", 1,
                containsValueFromFromOutput("unpushed", splitedOutput).size())
    }

    @Test
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

        outputCapture.reset()

        GitFeature.status(context)
        def splitedOutput = outputCapture.toString().split('\n')
        assertEquals(9, splitedOutput.size())
        assertEquals("should contain 1 c1", 1,
                getByValueFromOutput("c1", splitedOutput).size())
        assertEquals("should contain 1 master branch", 1,
                getByValueFromOutput("master", splitedOutput).size())
        assertEquals("should contain new file", 1,
                getByValueFromOutput("?? new", splitedOutput).size())
        assertEquals("should contain 1 c2 ", 1, getByValueFromOutput("c2", splitedOutput).size())
        assertEquals("should contain 1 empty string", 2,
                getByValueFromOutput("", splitedOutput).size())
        assertEquals("should contain 1 remote ref repository name", 1,
                containsValueFromFromOutput("refs/remotes/origin/master", splitedOutput).length)
        assertEquals("should contain 1 Branch not pushed", 1,
                containsValueFromFromOutput("Branch not pushed", splitedOutput).size())

        outputCapture.reset()

        ArrayList<OutputFilter> predicates = new ArrayList<>()
        predicates.add(new UnpushedStatusFilter())
        context.outputFilter.put(GitFeature.ACTION_STATUS, predicates)
        GitFeature.status(context)

        splitedOutput = outputCapture.toString().split('\n')
        assertEquals(5, splitedOutput.size())
        assertEquals("should contain 1 c1", 1,
                getByValueFromOutput("c1", splitedOutput).size())
        assertEquals("should contain 1 newBranch", 1,
                getByValueFromOutput("newBranch", splitedOutput).size())
        assertEquals("should contain 1 new file", 1,
                getByValueFromOutput("?? new", splitedOutput).size())
        assertEquals("should contain 1 empty string", 1,
                getByValueFromOutput("", splitedOutput).size())
        assertEquals("should contain 1 Branch not pushed", 1,
                containsValueFromFromOutput("Branch not pushed", splitedOutput).size())
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    /**
     * {@link repo.build.GitFeature#releaseMergeRelease(ActionContext context, String oneManifestBranch, String twoManifestBranch, String regexp, Closure versionClosure) }
     */
    @Test
    void testReleaseMergeRelease() {
        //init
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        //component
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Git.createBranch(context, dir, 'develop/1.0')
                    Git.createBranch(context, dir, 'develop/2.0')

                    //some changes
                    Git.checkout(context, dir, 'develop/1.0')
                    def newFile = new File(dir, 'test')
                    newFile.createNewFile()
                    newFile.text = 'TEST123'
                    Git.add(context, dir, 'test')
                    Git.commit(context, dir, 'test')
                })

        //manifest
        sandbox.component('manifest',
                { Sandbox sandbox, File dir ->
                    //change default branch to develop/1.0 on c2 component in manifest
                    Git.createBranch(context, dir, '1.0')
                    Git.checkout(context, dir, '1.0')
                    sandbox.changeDefaultBranchComponentOnManifest(dir, 'c2', 'develop/1.0')
                    Git.add(context, dir, 'default.xml')
                    Git.commit(context, dir, 'vup')

                    //change default branch to develop/2.0 on c2 component in manifest
                    Git.createBranch(context, dir, '2.0')
                    Git.checkout(context, dir, '2.0')
                    sandbox.changeDefaultBranchComponentOnManifest(dir, 'c2', 'develop/2.0')
                    Git.add(context, dir, 'default.xml')
                    Git.commit(context, dir, 'vup')
                })

        //expected call function
        GitFeature.releaseMergeRelease(context, '1.0', '2.0', /(\d+\.\d+)/,
                {
                    List list -> return list[0]+".0"
                })

        Git.checkout(context, new File(context.env.basedir, 'c2'), 'develop/2.0')
        assertEquals('TEST123', new File(context.env.basedir, 'c2/test').text)
    }

    //TODO we can use hamcrest matchers
    private static String[] getByValueFromOutput(String value, String[] output) {
        if (output.size() == 0) return null
        def list = Arrays.asList(output)
        def found = list.findAll({
            (it == value)
        })
        return found
    }

    private static String[] containsValueFromFromOutput(String value, String[] output) {
        if (output.size() == 0) return null
        def list = Arrays.asList(output)
        def found = list.findAll({
            (it.contains(value))
        })
        return found
    }
}
