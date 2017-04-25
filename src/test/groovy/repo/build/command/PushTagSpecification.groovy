package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class PushTagSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('push-tag')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with tag"() {
        def repoBuild = new RepoBuild('push-tag', '-T', '1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.pushTag(_, '1')
    }

    def "with parallel with tag"() {
        def repoBuild = new RepoBuild('push-tag', '-j', '2', '-T', '1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.pushTag(_, '1')
    }

}
