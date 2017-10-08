package repo.build.maven

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 */
@EqualsAndHashCode(includes = ["groupId", "artifactId"])
@ToString(includeNames = true, includeFields = true)
class MavenComponent {
    String path
    File basedir
    String groupId
    String artifactId
    String version
    Set<MavenArtifact> modules
    MavenArtifactRef parent
    boolean isParent = false
}
