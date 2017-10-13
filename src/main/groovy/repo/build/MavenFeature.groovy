package repo.build

import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.apache.maven.shared.invoker.InvocationRequest
import repo.build.maven.Build
import repo.build.maven.BuildState
import repo.build.maven.MavenArtifact
import repo.build.maven.MavenArtifactRef
import repo.build.maven.MavenComponent

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask

/**
 */

class MavenFeature {
    static Logger logger = Logger.getLogger(MavenFeature.class)

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

    static void forEachWithPom(ActionContext parentContext, Closure action) {
        RepoManifest.forEach(parentContext,
                { ActionContext actionContext, project ->
                    def dir = new File(actionContext.env.basedir, project.@path)
                    def pomFile = new File(dir, 'pom.xml')
                    return pomFile.exists()
                },
                action
        )
    }

    public static final String ACTION_UPDATE_PARENT = 'mavenFeatureUpdateParent'

    static void updateFeatureParent(ActionContext parentContext,
                                    String featureBranch,
                                    String parentComponent,
                                    boolean updateRelease,
                                    boolean allowSnapshots) {
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
                        initInvocationRequest(req, context.getOptions())
                        req.setGoals(Arrays.asList("clean", "install"))
                        req.setInteractive(false)
                        req.getProperties().put("skipTest", 'true')
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
                                            initInvocationRequest(req, context.getOptions())
                                            req.setGoals(Arrays.asList("versions:update-parent"))
                                            req.setInteractive(false)
                                            //properties.put("parentVersion", version)
                                            req.getProperties().put('generateBackupPoms', 'false')
                                            req.getProperties().put('allowSnapshots', Boolean.toString(allowSnapshots))
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

    /**
     *
     * @param parentContext
     * @param parentComponent
     * @param updateRelease параметр определяет надо ли обновлять ссылки на релизные версии парент артефакта
     * @param allowSnapshots параметр определяет разрешено ли при поиске последней версии использовать снапшотные
     * версии парент артефакта
     * @param p
     */
    static void updateReleaseParent(ActionContext parentContext,
                                    String parentComponent,
                                    boolean updateRelease,
                                    boolean allowSnapshots) {
        def context = parentContext.newChild(ACTION_UPDATE_PARENT)
        context.withCloseable {
            def parentBranch = Git.getBranch(context, new File(context.env.basedir, parentComponent))
            // TODO: check parentBranch = manifestBranch
            // get parent artifact
            def parentPomFile = new File(context.env.basedir, parentComponent + "/pom.xml")
            def parentPom = XmlUtils.parse(parentPomFile)
            def groupId = parentPom.groupId.text()
            def artifactId = parentPom.artifactId.text()
            String version = parentPom.version.text()

            // rebuild parent
            Maven.execute(context, parentPomFile,
                    { InvocationRequest req ->
                        initInvocationRequest(req, context.getOptions())
                        req.setGoals(Arrays.asList("clean", "install"))
                        req.setInteractive(false)
                        req.getProperties().put("skipTest", 'true')
                    }
            )

            // для всех компонентов в кторых ест фича бранч
            forEachWithPom(context,
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
                                            initInvocationRequest(req, context.getOptions())
                                            req.setGoals(Arrays.asList("versions:update-parent"))
                                            req.setInteractive(false)
                                            //properties.put("parentVersion", version)
                                            req.getProperties().put('generateBackupPoms', 'false')
                                            req.getProperties().put('allowSnapshots', Boolean.toString(allowSnapshots))
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
                    })
        }
    }

    @CompileStatic
    static void initInvocationRequest(InvocationRequest req, CliOptions options) {
        if (options.hasMe()) {
            req.setShowErrors(true)
        }
        if (options.hasMfae()) {
            req.setFailureBehavior(InvocationRequest.REACTOR_FAIL_AT_END)
        }
        if (options.getMgs()) {
            req.setGlobalSettingsFile(options.getMgs())
        }
        if (options.getMlr()) {
            req.setLocalRepositoryDirectory(options.getMlr())
        }
        if (options.hasMo()) {
            req.setOffline(true)
        }
        if (options.getMP()) {
            req.setProfiles(options.getMP())
        }
        if (options.getMs()) {
            req.setUserSettingsFile(options.getMs())
        }
        if (options.getMT()) {
            req.setThreads(options.getMT())
        }
        if (options.hasMU()) {
            req.setUpdateSnapshots(true)
        }

        if (options.isDebugMode()) {
            req.setDebug(true)
        }

        Properties properties = new Properties()
        properties.putAll(options.getSystemProperties())
        req.setProperties(properties)
    }

    @CompileStatic
    static void versionsUpdateProperties(ActionContext context,
                                         File pomFile,
                                         String includes,
                                         boolean allowSnapshots) {
        // call version plugin
        Maven.execute(context, pomFile,
                { InvocationRequest req ->
                    initInvocationRequest(req, context.getOptions())
                    req.setGoals(Arrays.asList("versions:update-properties"))
                    req.setInteractive(false)
                    req.getProperties().put("allowSnapshots", Boolean.toString(allowSnapshots))
                    req.getProperties().put("includes", includes)
                    req.getProperties().put('generateBackupPoms', 'false')
                }
        )
    }

    @CompileStatic
    static void versionsUseLastVersions(ActionContext context,
                                        File pomFile,
                                        String includes,
                                        boolean allowSnapshots) {
        Maven.execute(context, pomFile,
                { InvocationRequest req ->
                    initInvocationRequest(req, context.getOptions())
                    req.setGoals(Arrays.asList("versions:use-latest-versions"))
                    req.setInteractive(false)
                    req.getProperties().put("allowSnapshots", Boolean.toString(allowSnapshots))
                    req.getProperties().put("includes", includes)
                    req.getProperties().put('generateBackupPoms', 'false')
                }
        )
    }


    public static final String ACTION_UPDATE_VERSIONS = 'mavenFeatureUpdateVersons'

    @CompileStatic
    static void updateVersions(ActionContext parentContext,
                               String featureBranch,
                               String includes,
                               String continueFromComponent,
                               boolean allowSnapshots) {
        def context = parentContext.newChild(ACTION_UPDATE_VERSIONS)
        Pom.generateXml(context, featureBranch, new File(context.env.basedir, 'pom.xml'))

        // получаем компоненты и зависимости
        def componentsMap = getModuleToComponentMap(context)
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

                // commit only if component has featureBranch
                if (Git.getBranch(context, it.basedir) == featureBranch) {
                    versionsUpdateProperties(context, pomFile, includes, allowSnapshots)
                    // maven execute with skipTests
                    Maven.execute(context, pomFile, ['clean', 'install'], ['skipTests': 'true'])
                    // check modify pom.xml
                    if (Git.isFileModified(context, it.basedir, "pom.xml")) {
                        // if it modifies - commit vup
                        Git.add(context, it.basedir, "pom.xml")
                        // TODO, UGLY: _ fix bug on Linux with commit -m
                        Git.commit(context, it.basedir, "update_dependencies_to_last_versions")
                    }
                } else {
                    // maven execute with skipTests
                    Maven.execute(context, pomFile, ['clean', 'install'], ['skipTests': 'true'])
                }
            }
        }
    }

    static void releaseUpdateVersions(ActionContext parentContext,
                                      String includes,
                                      String continueFromComponent) {
        def context = parentContext.newChild(ACTION_UPDATE_VERSIONS)
        Pom.generateXml(context, "release", new File(context.env.basedir, 'pom.xml'))

        // получаем компоненты и зависимости
        def componentsMap = getModuleToComponentMap(context)
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
                versionsUpdateProperties(context, pomFile, includes, false)
                // maven execute with skipTests
                Maven.execute(context, pomFile, ['clean', 'install'], ['skipTests': 'true'])
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


    @CompileStatic
    static List<MavenComponent> sortComponents(Map<MavenArtifactRef, MavenComponent> modulesMap) {
        def graph = ComponentDependencyGraph.build(modulesMap)
        return graph.sort()
    }

    @CompileStatic
    static Map<MavenArtifactRef, MavenComponent> getModuleToComponentMap(ActionContext context) {
        return getModuleToComponentMap(getComponents(context))
    }

    @CompileStatic
    static Map<MavenArtifactRef, MavenComponent> getModuleToComponentMap(List<MavenComponent> components) {
        Map<MavenArtifactRef, MavenComponent> result = new HashMap<>()
        for (MavenComponent c : components) {
            for (MavenArtifact m : c.getModules()) {
                // map all component modules into host component
                result.put(new MavenArtifactRef(m.getGroupId(), m.getArtifactId()), c)
            }
        }
        return result
    }

    static List<MavenComponent> getComponents(ActionContext context) {
        List<MavenComponent> result = new ArrayList<>()
        forEachWithPom(context, { ActionContext actionContext, project ->
            def dir = new File(actionContext.env.basedir, project.@path)
            def pom = new XmlParser().parse(new File(dir, 'pom.xml'))
            MavenComponent component = new MavenComponent()
            component.setPath(project.@path)
            component.setBasedir(dir)
            component.setModules(getComponentModules(dir))
            component.setGroupId(getProjectGroup(pom))
            component.setArtifactId(pom.artifactId.text())
            def parentNode = pom.'parent'
            if (parentNode.size() > 0) {
                component.setParent(new MavenArtifactRef(parentNode.groupId.text(), parentNode.artifactId.text()))
            }
            synchronized (result) {
                result.add(component)
            }
        })
        return result
    }

    @CompileStatic
    static List<MavenComponent> getParentComponents(List<MavenComponent> components) {
        Map<MavenArtifactRef, MavenComponent> map = components.collectEntries {
            [new MavenArtifactRef(it), it]
        }

        return map.findAll {
            it.value.parent && map.containsKey(it.value.parent)
        }
        .collect {
            map.get(it.value.parent)
        }
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
            def parentRef = new MavenArtifactRef(project.parent.groupId.text(), project.parent.artifactId.text())
            module.getDependencies().add(parentRef)
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
        if (expr == '${project.groupId}') {
            return getProjectGroup(project)
        } else if (expr == '${parent.groupId}') {
            return project.parent.groupId.text()
        } else {
            return expr
        }
    }

    static void purgeLocal(ActionContext parentContext,
                           String manualInclude) {
        def context = parentContext.newChild()
        context.withCloseable {
            // rebuild parent
            Maven.execute(context, null,
                    { InvocationRequest req ->
                        initInvocationRequest(req, context.getOptions())
                        req.setGoals(Arrays.asList("dependency:purge-local-repository"))
                        req.setInteractive(false)
                        req.getProperties().put("manualInclude", manualInclude)
                    }
            )
        }

    }

    public static final String ACTION_BUILD_PARENTS_TREE = 'mavenFeatureBuildParentsTree'

    static final String ACTION_BUILD_PARENTS = 'mavenFeatureBuildParents'

    static void buildParents(ActionContext parentContext) {
        def context = parentContext.newChild(ACTION_BUILD_PARENTS)

        // получаем компоненты и зависимости
        def componentsMap = getModuleToComponentMap(
                getParentComponents(getComponents(context)))
        // формируем граф зависимостей
        List<MavenComponent> sortedComponents = sortComponents(componentsMap)
        context.writeOut("sort parents by dependency tree\n")
        sortedComponents.each {
            context.writeOut(it.groupId + ':' + it.artifactId + '\n')
        }

        sortedComponents.each {
            def pomFile = new File(it.basedir, "pom.xml")
            Maven.execute(context, pomFile, ['clean', 'install'], ['skipTests': 'true'])
        }
    }

    static final String ACTION_BUILD_PARALLEL = 'mavenFeatureBuildParallel'

    static void buildParallel(ActionContext parentContext) {
        def context = parentContext.newChild(ACTION_BUILD_PARALLEL)
        context.withCloseable {
            def build = new Build(getComponents(context))
            def result = build.execute(context.getParallel())
            if (result != BuildState.SUCCESS) {
                throw new RepoBuildException("Build result: $result")
            }
        }
    }
}