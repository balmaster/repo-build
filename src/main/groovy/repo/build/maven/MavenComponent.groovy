package repo.build.maven

/**
 */
class MavenComponent {
    File basedir;
    String groupId;
    String artifactId;
    String version;
    Set<MavenArtifact> modules;

    File getBasedir() {
        return basedir
    }

    void setBasedir(File basedir) {
        this.basedir = basedir
    }

    String getGroupId() {
        return groupId
    }

    void setGroupId(String groupId) {
        this.groupId = groupId
    }

    String getArtifactId() {
        return artifactId
    }

    void setArtifactId(String artifactId) {
        this.artifactId = artifactId
    }

    String getVersion() {
        return version
    }

    void setVersion(String version) {
        this.version = version
    }

    Set<MavenArtifact> getModules() {
        return modules
    }

    void setModules(Set<MavenArtifact> modules) {
        this.modules = modules
    }


    @Override
    public String toString() {
        return "MavenComponent{" +
                "basedir=" + basedir +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
