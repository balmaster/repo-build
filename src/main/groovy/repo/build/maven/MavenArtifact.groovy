package repo.build.maven;

import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;

import java.io.File;
import java.util.Set;

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
@EqualsAndHashCode(includes = ["groupId", "artifactId"])
@ToString(includeNames = true, includeFields = true)
class MavenArtifact {
    File basedir;
    String groupId;
    String artifactId;
    Set<MavenArtifactRef> dependencies;
}
