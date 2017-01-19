package repo.build.maven;

/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
public class MavenArtifactRef {
    String groupId;
    String artifactId;

    public MavenArtifactRef( String groupId, String artifactId ) {
        this.groupId = groupId;
        this.artifactId = artifactId;
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

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        MavenArtifactRef that = ( MavenArtifactRef ) o;

        if ( groupId != null ? !groupId.equals( that.groupId ) : that.groupId != null ) return false;
        return artifactId != null ? artifactId.equals( that.artifactId ) : that.artifactId == null;

    }

    @Override
    public int hashCode() {
        int result = groupId != null ? groupId.hashCode() : 0;
        result = 31 * result + ( artifactId != null ? artifactId.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString() {
        return "MavenArtifactRef{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                '}';
    }
}
