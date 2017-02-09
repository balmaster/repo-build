package repo.build.command

import groovy.transform.CompileStatic
import repo.build.CliOptions
import repo.build.RepoEnv

@CompileStatic
public abstract class AbstractCommand {
    String name
    String description

    AbstractCommand(String name, String description) {
        this.name = name
        this.description = description
    }

    @Override
    public String toString() {
        return "$name - $description"
    }

    public abstract void execute(RepoEnv env, CliOptions options)


}
