package repo.build.command

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repo.build.CliOptions
import repo.build.RepoEnv

abstract class AbstractCommand {
    static Logger logger = LoggerFactory.getLogger(AbstractCommand)

    String name
    String description

    AbstractCommand(String name, String description) {
        this.name = name
        this.description = description
    }

    @Override
    String toString() {
        return "$name - $description"
    }

    abstract void execute(RepoEnv env, CliOptions options)


}
