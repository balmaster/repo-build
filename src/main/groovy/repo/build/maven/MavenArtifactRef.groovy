package repo.build.maven

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString;

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
@EqualsAndHashCode(includes = ["groupId", "artifactId"])
@ToString(includeNames = true, includeFields = true)
class MavenArtifactRef {
    String groupId;
    String artifactId;

    public MavenArtifactRef(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public MavenArtifactRef(MavenComponent component) {
        this(component.groupId, component.artifactId)
    }
}
