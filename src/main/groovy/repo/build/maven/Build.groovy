package repo.build.maven

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.ComponentDependencyGraph
import repo.build.Maven
import repo.build.MavenFeature
import repo.build.RepoBuildException

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
//@CompileStatic
class Build {
    Map<MavenArtifactRef, MavenComponent> componentMap
    Map<MavenArtifactRef, BuildTask> buildTaskMap
    ActionContext context
    Map<MavenArtifactRef, MavenComponent> moduleToComponentMap

    Build(ActionContext context, List<MavenComponent> components) {
        this.context = context
        this.componentMap = components.collectEntries {
            [new MavenArtifactRef(it), it]
        }
        this.moduleToComponentMap = ComponentDependencyGraph
                .getModuleToComponentMap(components)

        this.buildTaskMap = componentMap.collectEntries {
            [it.key, new BuildTask(it.value)]
        } as Map<MavenArtifactRef, BuildTask>
    }

    boolean execute(ActionContext context) {
        // check circular dependencies
        def graph = ComponentDependencyGraph.build(componentMap.values())
        if (graph.hasCycles()) {
            for (def entry : graph.cycleRefs) {
                def component = entry.getKey()
                def error = new RepoBuildException("Project ${component.@path} has circular ref to ${entry.getValue()}")
                context.addError(error)
            }
            throw new RepoBuildException("project has circular dependencies")
        }
        // do parallel build
        def pool = new ForkJoinPool(context.getParallel())
        return pool.invoke(new RecursiveTask<Boolean>() {
            @Override
            protected Boolean compute() {
                // execute all build tasks
                buildTaskMap.each { pool.execute(it.value) }
                // wait build for all components
                return !componentMap
                        .collect { buildTaskMap.get(it.key).join() }
                        .any { it != BuildState.SUCCESS }
            }
        })
    }

    //@CompileStatic
    private class BuildTask extends RecursiveTask<BuildState> {
        MavenComponent component
        BuildState state

        BuildTask(MavenComponent component) {
            this.component = component
        }

        protected BuildState compute() {
            println("build component $component.path")
            // wait build deps
            def isFail = component
                    .getModules()
                    .collectMany { it.dependencies }
                    .findAll { moduleToComponentMap.containsKey(it) }
                    .unique()
                    .collect { new MavenArtifactRef(moduleToComponentMap.get(it)) }
                    .collect { buildTaskMap.get(it) }
                    .findAll { it != this }
                    .collect { println("wait build $it.component.path"); it.join() }
                    .any { it != BuildState.SUCCESS }
            if (isFail) {
                state = BuildState.DEPS_ERROR
                context.setErrorFlag()
                context.addError(new RepoBuildException(" component ${component.path} build $state"))
            } else {
                // build component
                try {
                    def pomFile = new File(component.basedir, 'pom.xml')
                    def p = new Properties()
                    Maven.execute(context, pomFile, ['clean', 'install'])
                    state = BuildState.SUCCESS
                } catch (Exception e) {
                    state = BuildState.ERROR
                    context.setErrorFlag()
                    context.addError(new RepoBuildException(" component ${component.path} build $state", e))
                }
            }
            return state
        }
    }

}
