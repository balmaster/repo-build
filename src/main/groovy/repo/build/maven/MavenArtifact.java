package repo.build.maven;

import java.io.File;
import java.util.Set;

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
public class MavenArtifact {
    File basedir;
    String groupId;
    String artifactId;
    Set<MavenArtifactRef> dependencies;

    public File getBasedir() {
        return basedir;
    }

    public void setBasedir( File basedir ) {
        this.basedir = basedir;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId( String groupId ) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId( String artifactId ) {
        this.artifactId = artifactId;
    }

    public Set<MavenArtifactRef> getDependencies() {
        return dependencies;
    }

    public void setDependencies( Set<MavenArtifactRef> dependencies ) {
        this.dependencies = dependencies;
    }
}
