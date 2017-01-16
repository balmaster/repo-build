package repo.build

import org.apache.maven.shared.invoker.InvocationRequest
import repo.build.maven.MavenComponent
import repo.build.maven.MavenComponentRef

/**
 */
class MavenFeature {
    static void updateParent(RepoEnv env, String featureBranch, String parentComponent) {
        def parentComponentBranch = RepoManifest.getBranch(parentComponent)
        if (!featureBranch.equals(parentComponentBranch)) {
            throw new RepoBuildException("parent component must switched to feature branch $featureBranch")
        }
        // get parent artifact
        def parentPom = XmlUtils.parse(new File(env.basedir, parentComponentBranch + "/pom.xml"))
        def groupId = parentPom.groupId.text()
        def artifactId = parentPom.artifactId.text()
        def version = parentPom.version.text()
        // для всех компонентов в кторых ест фича бранч
        RepoManifest.forEachWithFeatureBranch(env, { project ->
            // пропускаем parent
            if (!parentComponent.equals(project.@path)) {
                def componentPomFile = new File(env.basedir, project.@path + "/pom.xml")
                def componentPom = XmlUtils.parse(componentPomFile)
                def parentGroupId = componentPom?.parent?.groupId?.text()
                def parentArtifactId = componentPom?.parent?.artifactId?.text()
                def parentVersion = componentPom?.parent?.version?.text()
                if (groupId.equals(parentGroupId)
                        && artifactId.equals(parentArtifactId)
                        && !version.equals(parentVersion)) {
                    // если группа, артефакт совпадают а версия нет - подменяем версию parent
                    Maven.execute(componentPomFile,
                            { InvocationRequest req ->
                                req.setGoals(Arrays.asList("versions:update-parent"))
                                req.setInteractive(false)
                                Properties properties = new Properties();
                                properties.put("parentVersion", version)
                                req.setProperties(properties)
                            }
                    )
                }
            }
        }, featureBranch)
    }

    static void updateVersions(RepoEnv env, String includes) {
        // получаем компоненты и зависимости
        def componentsMap = getComponentsMap(env.basedir)
        // формируем граф зависимостей
        def greph = ComponentDependencyGraph.build(componentsMap)
        def sortedComponents = greph.sort()
        sortedComponents.each {
            // maven build with skipTests
            Maven.execute(new File(it.basedir, "pom.xml"),
                    { InvocationRequest req ->
                        req.setGoals(Arrays.asList("clean", "install"))
                        req.setInteractive(false)
                        Properties properties = new Properties();
                        properties.put("skipTest", 'true')
                        req.setProperties(properties)
                    }
            )
            // call version plugin
            Maven.execute(new File(it.basedir, "pom.xml"),
                    { InvocationRequest req ->
                        req.setGoals(Arrays.asList("versions:update-properties"))
                        req.setInteractive(false)
                        Properties properties = new Properties();
                        properties.put("allowSnapshots", "true")
                        properties.put("includes", "$includes")
                        req.setProperties(properties)
                    }
            )
            // check modify pom.xml
            if (Git.isFileModified(env, it.basedir, "pom.xml")) {
                // if it modifies - commit vup
                Git.add(env, it.basedir, "pom.xml")
                Git.commit(env, it.basedir, "update dependencies to last feature snapshot")
            }
        }

    }

    static Map<MavenComponentRef, MavenComponent> getComponentsMap(File basedir) {
        List<MavenComponent> components = getComponents(basedir)
        Map<MavenComponentRef, MavenComponent> result = new HashMap<>();
        for (MavenComponent c : components) {
            result.put(new MavenComponentRef(c.getGroupId(), c.getArtifactId()))
        }
        return result
    }

    static List<MavenComponent> getComponents(File basedir) {
        List<MavenComponent> result = new ArrayList<>()
        def pomFile = new File(basedir, "pom.xml")
        Maven.execute(pomFile,
                { req ->
                    req.setGoals(Collections.singletonList("dependency:list"))
                    req.setInteractive(false)
                    Properties properties = new Properties();
                    properties.put("outputFile", '${project.build.directory}/dependencies')
                    req.setProperties(properties)
                },
                { res ->
                    // собираем компоненты
                    Pom.getModules(pomFile).each {
                        File componentBasedir = new File(basedir, it)
                        MavenComponent component = new MavenComponent()
                        component.setBasedir(componentBasedir)
                        component.setDependencies(getComponentDependencies(componentBasedir))
                        result.add(component)
                    }
                }
        )
        return result
    }

    static Set<MavenComponentRef> getComponentDependencies(File basedir) {
        return getModuleDependencies(basedir)
    }

    static Set<MavenComponentRef> getModuleDependencies(File basedir) {
        Set<MavenComponentRef> result = new HashSet<>()
        result.addAll(readDependencies(basedir))
        Pom.getModules(new File(basedir, "pom.xml")).each {
            File moduleBasedir = new File(basedir, it)
            result.addAll(getModuleDependencies(moduleBasedir))
        }
        return result
    }

    static List<MavenComponentRef> readDependencies(File basedir) {
        //TODO: fix target
        def lines = new File(basedir, "target/dependencies").readLines()
        List<MavenComponentRef> result = new ArrayList<>()
        lines.each {
            def tokens = it.split(":")
            if (tokens.length > 3) {
                result.add(new MavenComponentRef(tokens[0].trim(), tokens[1].trim()))
            }
        }
        return result
    }
}
