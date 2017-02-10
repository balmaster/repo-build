package repo.build.command

import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import repo.build.CliOptions
import repo.build.RepoEnv

public abstract class AbstractCommand {
    static Logger logger = LogManager.getLogger(AbstractCommand)

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
