package repo.build.maven;

import java.io.File;
import java.util.Set;

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
public class MavenComponent {
    private File basedir;
    private MavenArtifact artifact;
    private Set<MavenArtifact> dependencies;

    public File getBasedir() {
        return basedir;
    }

    public void setBasedir( File basedir ) {
        this.basedir = basedir;
    }

    public Set<MavenArtifact> getDependencies() {
        return dependencies;
    }

    public void setDependencies( Set<MavenArtifact> dependencies ) {
        this.dependencies = dependencies;
    }

    public MavenArtifact getArtifact() {
        return artifact;
    }

    public void setArtifact( MavenArtifact artifact ) {
        this.artifact = artifact;
    }

    @Override
    public String toString() {
        return "MavenComponent{" +
                "basedir=" + basedir +
                ", artifact=" + artifact +
                '}';
    }
}
