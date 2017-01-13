package repo.build.maven;

import java.io.File;
import java.util.Set;

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
public class MavenComponent {
    private File basedir;
    private String groupId;
    private String artifactId;
    private String version;
    private Set<MavenComponentRef> dependencies;

    public File getBasedir() {
        return basedir;
    }

    public void setBasedir( File basedir ) {
        this.basedir = basedir;
    }

    public Set<MavenComponentRef> getDependencies() {
        return dependencies;
    }

    public void setDependencies( Set<MavenComponentRef> dependencies ) {
        this.dependencies = dependencies;
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

    public String getVersion() {
        return version;
    }

    public void setVersion( String version ) {
        this.version = version;
    }
}
