package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class TaskMergeFeatureSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('task-merge-feature')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with featureBranch"() {
        def repoBuild = new RepoBuild('task-merge-feature', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with parallel with featureBranch with task branch"() {
        def repoBuild = new RepoBuild('task-merge-feature', '-j', '2', '-f', 'feature/1', '-I', 'task/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.taskMergeFeature(_, 'task/1', 'feature/1')
    }

}
