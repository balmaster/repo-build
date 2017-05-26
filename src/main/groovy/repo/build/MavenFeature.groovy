package repo.build

import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.apache.maven.shared.invoker.InvocationRequest
import repo.build.maven.MavenArtifact
import repo.build.maven.MavenArtifactRef
import repo.build.maven.MavenComponent

/**
 */
class MavenFeature {
    static Logger logger = Logger.getLogger(MavenFeature.class)
    static final String P_USER_SETTINGS_FILE = 'maven.user.settings.file'
    static final String P_LOCAL_REPOSITORY_DIR = 'maven.local.repository.dir'

    static void forEachWithFeatureBranchAndPom(ActionContext parentContext, Closure action, String branch) {
        def remoteBranch = RepoManifest.getRemoteBranch(parentContext, branch)

        RepoManifest.forEach(parentContext,
                { ActionContext actionContext, project ->
                    def dir = new File(actionContext.env.basedir, project.@path)
                    def pomFile = new File(dir, 'pom.xml')
                    return Git.branchPresent(actionContext, dir, remoteBranch) && pomFile.exists()
                },
                action
        )
    }

    public static final String ACTION_UPDATE_PARENT = 'mavenFeatureUpdateParent'

    static void updateParent(ActionContext parentContext,
                             String featureBranch,
                             String parentComponent,
                             boolean updateRelease,
                             boolean allowSnapshots,
                             Map<String, String> p) {
        def context = parentContext.newChild(ACTION_UPDATE_PARENT)
        context.withCloseable {
            def parentBranch = Git.getBranch(context, new File(context.env.basedir, parentComponent))
            if (featureBranch != parentBranch) {
                throw new RepoBuildException("parent component must switched to feature branch $featureBranch")
            }
            // get parent artifact
            def parentPomFile = new File(context.env.basedir, parentComponent + "/pom.xml")
            def parentPom = XmlUtils.parse(parentPomFile)
            def groupId = parentPom.groupId.text()
            def artifactId = parentPom.artifactId.text()
            String version = parentPom.version.text()

            // rebuild parent
            Maven.execute(context, parentPomFile,
                    { InvocationRequest req ->
                        initInvocationRequest(req, p)
                        req.setGoals(Arrays.asList("clean", "install"))
                        req.setInteractive(false)
                        Properties properties = new Properties();
                        properties.put("skipTest", 'true')
                        properties.putAll(p)
                        req.setProperties(properties)
                    }
            )

            // для всех компонентов в кторых ест фича бранч
            forEachWithFeatureBranchAndPom(context,
                    { ActionContext actionContext, project ->
                        // пропускаем parent
                        if (parentComponent != project.@path) {
                            def dir = new File(actionContext.env.basedir, project.@path)
                            def componentPomFile = new File(dir, "/pom.xml")
                            def componentPom = XmlUtils.parse(componentPomFile)
                            def parentGroupId = componentPom?.parent?.groupId?.text()
                            def parentArtifactId = componentPom?.parent?.artifactId?.text()
                            def parentVersion = componentPom?.parent?.version?.text()
                            if (groupId == parentGroupId
                                    && artifactId == parentArtifactId
                                    && version != parentVersion
                                    // its SNAPSHOT or updateReleases enabled
                                    && (version.contains('SNAPSHOT') || updateRelease)
                            ) {
                                // если группа, артефакт совпадают а версия нет - подменяем версию parent
                                Maven.execute(actionContext, componentPomFile,
                                        { InvocationRequest req ->
                                            initInvocationRequest(req, p)
                                            req.setGoals(Arrays.asList("versions:update-parent"))
                                            req.setInteractive(false)
                                            Properties properties = new Properties();
                                            //properties.put("parentVersion", version)
                                            properties.put('generateBackupPoms', 'false')
                                            properties.put('allowSnapshots', Boolean.toString(allowSnapshots))
                                            properties.putAll(p)
                                            req.setProperties(properties)
                                        }
                                )
                                // check modify pom.xml
                                if (Git.isFileModified(actionContext, dir, "pom.xml")) {
                                    // if it modifies - commit vup
                                    Git.add(actionContext, dir, "pom.xml")
                                    Git.commit(actionContext, dir, "update_parent")
                                }
                            }
                        }
                    }, featureBranch)
        }
    }

    @CompileStatic
    static void initInvocationRequest(InvocationRequest req, Map<String, String> properties) {
        if (properties.containsKey(P_LOCAL_REPOSITORY_DIR)) {
            req.setLocalRepositoryDirectory(new File(properties.get(P_LOCAL_REPOSITORY_DIR)))
        }
        if (properties.containsKey(P_USER_SETTINGS_FILE)) {
            req.setUserSettingsFile(new File(properties.get(P_USER_SETTINGS_FILE)))
        }
    }

    @CompileStatic
    static void versionsUpdateProperties(ActionContext context,
                                         File pomFile,
                                         String includes,
                                         boolean allowSnapshots,
                                         Map<String, String> p) {
        // call version plugin
        Maven.execute(context, pomFile,
                { InvocationRequest req ->
                    initInvocationRequest(req, p)
                    req.setGoals(Arrays.asList("versions:update-properties"))
                    req.setInteractive(false)
                    Properties properties = new Properties();
                    properties.put("allowSnapshots", Boolean.toString(allowSnapshots))
                    properties.put("includes", includes)
                    properties.put('generateBackupPoms', 'false')
                    properties.putAll(p)
                    req.setProperties(properties)
                }
        )
    }

    @CompileStatic
    static void versionsUseLastVersions(ActionContext context,
                                        File pomFile,
                                        String includes,
                                        boolean allowSnapshots,
                                        Map<String, String> p) {
        Maven.execute(context, pomFile,
                { InvocationRequest req ->
                    initInvocationRequest(req, p)
                    req.setGoals(Arrays.asList("versions:use-latest-versions"))
                    req.setInteractive(false)
                    Properties properties = new Properties();
                    properties.put("allowSnapshots", Boolean.toString(allowSnapshots))
                    properties.put("includes", includes)
                    properties.put('generateBackupPoms', 'false')
                    properties.putAll(p)
                    req.setProperties(properties)
                }
        )
    }

    @CompileStatic
    static void build(ActionContext context,
                      File pomFile,
                      List<String> goals,
                      Map<String, String> p) {
        Maven.execute(context, pomFile,
                { InvocationRequest req ->
                    initInvocationRequest(req, p)
                    req.setGoals(goals)
                    req.setInteractive(false)
                    Properties properties = new Properties();
                    properties.putAll(p)
                    req.setProperties(properties)
                }
        )
    }

    public static final String ACTION_UPDATE_VERSIONS = 'mavenFeatureUpdateVersons'

    @CompileStatic
    static void updateVersions(ActionContext parentContext,
                               String featureBranch,
                               String includes,
                               String continueFromComponent,
                               boolean allowSnapshots,
                               Map<String, String> p) {
        def context = parentContext.newChild(ACTION_UPDATE_VERSIONS)
        Pom.generateXml(context, featureBranch, new File(context.env.basedir, 'pom.xml'))

        // получаем компоненты и зависимости
        def componentsMap = getComponentsMap(context.env.basedir)
        // формируем граф зависимостей
        List<MavenComponent> sortedComponents = sortComponents(componentsMap)
        context.writeOut("sort component by dependency tree\n")
        sortedComponents.each {
            context.writeOut(it.groupId + ':' + it.artifactId + '\n')
        }

        boolean found = continueFromComponent == null
        sortedComponents.each {
            if (continueFromComponent == it.path) {
                found = true
            }
            if (found) {
                def pomFile = new File(it.basedir, "pom.xml")
                versionsUpdateProperties(context, pomFile, includes, allowSnapshots, p)
                // maven build with skipTests
                build(context, pomFile, ['clean', 'install'], ['skipTests': 'true'])
                // commit only if component has featureBranch
                if (Git.getBranch(context, it.basedir) == featureBranch) {
                    // check modify pom.xml
                    if (Git.isFileModified(context, it.basedir, "pom.xml")) {
                        // if it modifies - commit vup
                        Git.add(context, it.basedir, "pom.xml")
                        // TODO, UGLY: _ fix bug on Linux with commit -m
                        Git.commit(context, it.basedir, "update_dependencies_to_last_versions")
                    }
                }
            }
        }
    }

    @CompileStatic
    static List<MavenComponent> sortComponents(Map<MavenArtifactRef, MavenComponent> componentsMap) {
        def graph = ComponentDependencyGraph.build(componentsMap)
        return graph.sort()
    }

    @CompileStatic
    static Map<MavenArtifactRef, MavenComponent> getComponentsMap(File basedir) {
        List<MavenComponent> components = getComponents(basedir)
        Map<MavenArtifactRef, MavenComponent> result = new HashMap<>();
        for (MavenComponent c : components) {
            for (MavenArtifact m : c.getModules()) {
                // map all component modules into host component
                result.put(new MavenArtifactRef(m.getGroupId(), m.getArtifactId()), c)
            }
        }
        return result
    }

    static List<MavenComponent> getComponents(File basedir) {
        List<MavenComponent> result = new ArrayList<>()
        def pomFile = new File(basedir, "pom.xml")
        // собираем компонентыs
        Pom.getModules(pomFile).each {
            File componentBasedir = new File(basedir, it)
            def project = new XmlParser().parse(new File(componentBasedir, 'pom.xml'))
            MavenComponent component = new MavenComponent()
            component.setPath(it)
            component.setBasedir(componentBasedir)
            component.setModules(getComponentModules(componentBasedir))
            component.setGroupId(getProjectGroup(project))
            component.setArtifactId(project.artifactId.text())
            result.add(component)
        }
        return result
    }

    static String getProjectGroup(Node project) {
        if (project.groupId) {
            return project.groupId.text()
        } else {
            return project.parent.groupId.text()
        }
    }

    static Set<MavenArtifact> getComponentModules(File basedir) {
        Set<MavenArtifact> result = new HashSet<>()
        def module = new MavenArtifact()
        def project = new XmlParser().parse(new File(basedir, 'pom.xml'))
        module.basedir = basedir
        module.setGroupId(getProjectGroup(project))
        module.setArtifactId(project.artifactId.text())
        module.setDependencies(getProjectDependencies(project))
        if (project.parent) {
            module.getDependencies().add(
                    new MavenArtifactRef(
                            project.parent.groupId.text(),
                            project.parent.artifactId.text()))
        }
        result.add(module)
        Pom.getModules(new File(basedir, "pom.xml")).each {
            File moduleBasedir = new File(basedir, it)
            result.addAll(getComponentModules(moduleBasedir))
        }
        return result
    }

    static Set<MavenArtifactRef> getProjectDependencies(Node project) {
        Set<MavenArtifactRef> result = new HashSet<>()
        def parseDependencies = { Node dependencies ->
            dependencies.dependency.each {
                def groupId = eval(it.groupId.text(), project)
                result.add(new MavenArtifactRef(groupId, it.artifactId.text()))
            }
        }
        if (project.dependencyManagement.dependencies) {
            parseDependencies(project.dependencyManagement.dependencies)
        }
        if (project.dependencies) {
            parseDependencies(project.dependencies)
        }
        return result
    }

    static String eval(Object expr, Node project) {
        if (expr.equals('${project.groupId}')) {
            return getProjectGroup(project)
        } else if (expr.equals('${parent.groupId}')) {
            return project.parent.groupId.text()
        } else {
            return expr
        }
    }
}
