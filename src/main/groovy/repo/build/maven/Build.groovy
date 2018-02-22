package repo.build.maven

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.ComponentDependencyGraph
import repo.build.Maven
import repo.build.RepoBuildException

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
//@CompileStatic
class Build {
    Collection<MavenComponent> components
    ActionContext context

    Build(ActionContext context, Collection<MavenComponent> components) {
        this.context = context
        this.components = components
    }

    boolean execute(ActionContext context) {
        // check circular dependencies
        def graph = ComponentDependencyGraph.build(components)
        if (graph.hasCycles()) {
            for (def entry : graph.cycleRefs) {
                def component = entry.getKey()
                def error = new RepoBuildException("Project ${component.@path} has circular ref to ${entry.getValue()}")
                context.addError(error)
            }
            throw new RepoBuildException("project has circular dependencies")
        }

        Map<MavenArtifactRef, MavenComponent> componentMap = components.collectEntries {
            [new MavenArtifactRef(it), it]
        }

        Map<MavenArtifactRef, BuildState> buildStates = componentMap.collectEntries {
            [it.key, BuildState.NEW]
        } as Map<MavenArtifactRef, BuildState>

        Map<MavenArtifactRef, MavenComponent> moduleToComponentMap =
                ComponentDependencyGraph.getModuleToComponentMap(components)

        ConcurrentMap<MavenArtifactRef, List<MavenArtifactRef>> buildDeps = new ConcurrentHashMap<>()
        buildStates.keySet().forEach { MavenArtifactRef key ->
            def depsTasks = componentMap.get(key).modules
                    .collectMany { it.dependencies }
                    .findAll { moduleToComponentMap.containsKey(it) }
                    .unique()
                    .collect { new MavenArtifactRef(moduleToComponentMap.get(it)) }
                    .findAll { !key.equals(it) }
            buildDeps.put(key, depsTasks)
            return
        }

        Set<MavenArtifactRef> tasks = new HashSet<>(buildStates.keySet())

        def pool = Executors.newFixedThreadPool(context.getParallel())
        while (!tasks.isEmpty()) {
            def iter = tasks.iterator()
            while (iter.hasNext()) {
                def key = iter.next()
                def component = componentMap.get(key)
                def deps = buildDeps.get(key)
                if (!deps.any { buildStates.get(it) != BuildState.SUCCESS }) {
                    pool.execute {
                        // build component
                        try {
                            def pomFile = new File(component.basedir, 'pom.xml')
                            Maven.execute(context, pomFile, ['clean', 'install'])
                            buildStates.put(key, BuildState.SUCCESS)
                        } catch (Exception e) {
                            buildStates.put(key, BuildState.ERROR)
                            context.setErrorFlag()
                            context.addError(new RepoBuildException(" component ${component.path} build ERROR", e))
                        }
                    }
                    iter.remove()
                } else if (deps.any {
                    buildStates.get(it) == BuildState.DEPS_ERROR ||
                            buildStates.get(it) == BuildState.ERROR
                }) {
                    buildStates.put(key, BuildState.DEPS_ERROR)
                    context.setErrorFlag()
                    context.addError(new RepoBuildException(" component ${component.path} build DEPS_ERROR"))
                    iter.remove()
                }
            }
            // throttle cpu
            Thread.sleep(500L)
        }
        return !context.getErrorFlag()
    }
}