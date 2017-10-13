package repo.build.maven

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
class Build {
    Map<MavenArtifactRef, MavenComponent> componentMap
    Map<MavenArtifactRef, BuildTask> buildTaskMap

    Build(Set<MavenComponent> components) {
        this.componentMap = components.collectEntries {
            [new MavenArtifactRef(it), it]
        }

        this.buildTaskMap = componentMap.collectEntries {
            [it.key, new BuildTask(it.value)]
        }
    }

    BuildState execute(int parallelism) {
        def pool = new ForkJoinPool(parallelism)
        pool.invoke(new RecursiveTask<BuildState>() {
            @Override
            protected BuildState compute() {
                return buildTaskMap.collect { it.value.join() }
                        .any { it != BuildState.SUCCESS } ? BuildState.ERROR : BuildState.SUCCESS
            }
        })

    }

    private class BuildTask extends RecursiveTask<BuildState> {
        MavenComponent component

        BuildTask(MavenComponent component) {
            this.component = component
        }

        protected BuildState compute() {
            // wait build deps
            def isFail = component
                    .getModules()
                    .collectMany { it.dependencies }
                    .unique()
                    .findAll { buildTaskMap.containsKey(it) }
                    .collect { buildTaskMap.get(it) }
                    .collect { it.join() }
                    .any { it != BuildState.SUCCESS }
            if (isFail) {
                return BuildState.DEPS_ERROR
            } else {
                // build component
                try {

                    return BuildState.SUCCESS
                } catch (Exception e) {
                    return BuildState.ERROR
                }
            }
        }
    }

}
