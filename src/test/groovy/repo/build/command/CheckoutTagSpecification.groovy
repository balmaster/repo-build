package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class CheckoutTagSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('checkout-tag')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with tag"() {
        def repoBuild = new RepoBuild('checkout-tag', '-T', '1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.checkoutTag(_, '1')
    }

}
