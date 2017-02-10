package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class GrepSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('grep')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)

    }

    def "with expr"() {
        def repoBuild = new RepoBuild('grep', '-e', 'exp1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.grep(_, 'exp1')
    }

}
