package repo.build

import org.apache.maven.shared.invoker.InvocationRequest

/**
 */
class MavenFeatureTest extends BaseTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp()
        sandbox = new Sandbox(new RepoEnv(createTempDir()))
                .newGitComponent('parent',
                { Sandbox sandbox, File dir ->
                    def ant = new AntBuilder()
                    ant.copy(todir: dir) {
                        fileset(dir: 'src/test/resources/parent') {
                            include(name: '**/**')
                        }
                    }
                    Git.add(sandbox.context, dir, '*.*')
                    Git.commit(sandbox.context, dir, 'add')
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                })
                .newGitComponent('c1',
                { Sandbox sandbox, File dir ->
                    def ant = new AntBuilder()
                    ant.copy(todir: dir) {
                        fileset(dir: 'src/test/resources/c1') {
                            include(name: '**/**')
                        }
                    }
                    Git.add(sandbox.context, dir, '*.*')
                    Git.commit(sandbox.context, dir, 'add')
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                })
                .newGitComponent('c2',
                { Sandbox sandbox, File dir ->
                    def ant = new AntBuilder()
                    ant.copy(todir: dir) {
                        fileset(dir: 'src/test/resources/c2') {
                            include(name: '**/**')
                        }
                    }
                    Git.add(sandbox.context, dir, '*.*')
                    Git.commit(sandbox.context, dir, 'add')
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                })
                .newGitComponent('c3',
                { Sandbox sandbox, File dir ->
                    def ant = new AntBuilder()
                    ant.copy(todir: dir) {
                        fileset(dir: 'src/test/resources/c3') {
                            include(name: '**/**')
                        }
                    }
                    Git.add(sandbox.context, dir, '*.*')
                    Git.commit(sandbox.context, dir, 'add')
                    Git.createBranch(sandbox.context, dir, 'feature/1')
                })
                .newGitComponent('manifest',
                { Sandbox sandbox, File dir ->
                    sandbox.gitInitialCommit(dir)
                    sandbox.buildManifest(dir)
                    Git.add(sandbox.context, dir, 'default.xml')
                    Git.commit(sandbox.context, dir, 'manifest')
                })
    }

    void testUpdateParent() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        // build parent
        cleanInstallParent()

        // update parent version to 1.1.0-SNAPSHOT on master
        sandbox.component('parent',
                { Sandbox sandbox, File dir ->
                    Maven.execute(sandbox.context, new File(dir, 'pom.xml'),
                            { InvocationRequest req ->
                                req.setGoals(Arrays.asList("versions:set"))
                                req.setInteractive(false)
                                Properties properties = new Properties();
                                properties.put("newVersion", '1.1.0-SNAPSHOT')
                                properties.put('generateBackupPoms', 'false')
                                req.setProperties(properties)
                            }
                    )
                    Git.add(sandbox.context, dir, 'pom.xml')
                    Git.commit(sandbox.context, dir, 'vup')
                })

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')
        GitFeature.featureMergeRelease(context, 'feature/1')

        MavenFeature.updateParent(context, 'feature/1', 'parent', false, true)

        // check parent version
        def c1Pom = new XmlParser().parse(new File(env.basedir, 'c1/pom.xml'))
        assertEquals('1.1.0-SNAPSHOT', c1Pom.parent.version.text())
        def c2Pom = new XmlParser().parse(new File(env.basedir, 'c2/pom.xml'))
        assertEquals('1.1.0-SNAPSHOT', c2Pom.parent.version.text())
        def c3Pom = new XmlParser().parse(new File(env.basedir, 'c3/pom.xml'))
        assertEquals('1.1.0-SNAPSHOT', c3Pom.parent.version.text())
    }


    void testUpdateVersions() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        // build parent
        cleanInstallParent()
        // update c1 version to 1.1.0-SNAPSHOT on master
        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Maven.execute(sandbox.context, new File(dir, 'pom.xml'),
                            { InvocationRequest req ->
                                req.setGoals(Arrays.asList("versions:set"))
                                req.setInteractive(false)
                                Properties properties = new Properties();
                                properties.put("newVersion", '1.1.0-SNAPSHOT')
                                properties.put('generateBackupPoms', 'false')
                                req.setProperties(properties)
                            }
                    )
                    Git.addUpdated(sandbox.context, dir)
                    Git.commit(sandbox.context, dir, 'vup')
                })
        // update c2 version to 2.1.0-SNAPSHOT on master
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Maven.execute(sandbox.context, new File(dir, 'pom.xml'),
                            { InvocationRequest req ->
                                req.setGoals(Arrays.asList("versions:set"))
                                req.setInteractive(false)
                                Properties properties = new Properties();
                                properties.put("newVersion", '2.1.0-SNAPSHOT')
                                properties.put('generateBackupPoms', 'false')
                                req.setProperties(properties)
                            }
                    )
                    Git.addUpdated(sandbox.context, dir)
                    Git.commit(sandbox.context, dir, 'vup')
                })

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')
        GitFeature.featureMergeRelease(context, 'feature/1')

        MavenFeature.updateVersions(context, 'feature/1', 'test.repo-build:*', null, true)

        // check parent version
        def c2Pom = new XmlParser().parse(new File(env.basedir, 'c2/pom.xml'))
        assertEquals('1.1.0-SNAPSHOT', c2Pom.properties."c1.version".text())
    }

    void testUpdateVersionsContinueFromComponent() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')

        // build parent
        cleanInstallParent()
        // update c1 version to 1.1.0-SNAPSHOT on master
        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Maven.execute(sandbox.context, new File(dir, 'pom.xml'),
                            { InvocationRequest req ->
                                req.setGoals(Arrays.asList("versions:set"))
                                req.setInteractive(false)
                                Properties properties = new Properties();
                                properties.put("newVersion", '1.1.0-SNAPSHOT')
                                properties.put('generateBackupPoms', 'false')
                                req.setProperties(properties)
                            }
                    )
                    Git.addUpdated(sandbox.context, dir)
                    Git.commit(sandbox.context, dir, 'vup')
                })
        // update c2 version to 2.1.0-SNAPSHOT on master
        sandbox.component('c2',
                { Sandbox sandbox, File dir ->
                    Maven.execute(sandbox.context, new File(dir, 'pom.xml'),
                            { InvocationRequest req ->
                                req.setGoals(Arrays.asList("versions:set"))
                                req.setInteractive(false)
                                Properties properties = new Properties();
                                properties.put("newVersion", '2.1.0-SNAPSHOT')
                                properties.put('generateBackupPoms', 'false')
                                req.setProperties(properties)
                            }
                    )
                    Git.addUpdated(sandbox.context, dir)
                    Git.commit(sandbox.context, dir, 'vup')
                })

        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')
        GitFeature.featureMergeRelease(context, 'feature/1')

        MavenFeature.updateVersions(context, 'feature/1', 'test.repo-build:*', null, true)

        sandbox.component('c1',
                { Sandbox sandbox, File dir ->
                    Maven.execute(sandbox.context, new File(dir, 'pom.xml'),
                            { InvocationRequest req ->
                                req.setGoals(Arrays.asList("clean"))
                                req.setInteractive(false)
                            }
                    )
                })

        MavenFeature.updateVersions(context, 'feature/1', 'test.repo-build:*', 'c2', true)

        // check parent version
        def c2Pom = new XmlParser().parse(new File(env.basedir, 'c2/pom.xml'))
        assertEquals('1.1.0-SNAPSHOT', c2Pom.properties."c1.version".text())

        def c1Target = new File(env.basedir, 'c1/target')
        assertFalse(c1Target.exists())

    }

    private Sandbox cleanInstallParent() {
        sandbox.component('parent',
                { Sandbox sandbox, File dir ->
                    Maven.execute(sandbox.context, new File(dir, 'pom.xml'),
                            { InvocationRequest req ->
                                req.setGoals(Arrays.asList('clean', 'install'))
                                req.setInteractive(false)
                                Properties properties = new Properties()
                                properties.put('skipTests', 'true')
                                req.setProperties(properties)
                            }
                    )
                })
    }

    void testGetComponentsMap() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')
        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')
        Pom.generateXml(context, 'feature/1', new File(env.basedir, 'pom.xml'))

        def components = MavenFeature.getComponentsMap(env.basedir)
        assertEquals(7, components.size())
    }

    void testSortComponents() {
        def url = new File(sandbox.env.basedir, 'manifest')
        GitFeature.cloneManifest(context, url.getAbsolutePath(), 'master')
        GitFeature.sync(context)
        GitFeature.switch(context, 'feature/1')
        Pom.generateXml(context, 'feature/1', new File(env.basedir, 'pom.xml'))

        def components = MavenFeature.getComponentsMap(env.basedir)
        def sortedComponents = MavenFeature.sortComponents(components)
        assertEquals(4, sortedComponents.size())
        assertEquals('parent', sortedComponents.get(0).getArtifactId())
        assertEquals('c1-parent', sortedComponents.get(1).getArtifactId())
        assertEquals('c2-parent', sortedComponents.get(2).getArtifactId())
        assertEquals('c3-parent', sortedComponents.get(3).getArtifactId())
    }

}
