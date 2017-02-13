package repo.build.command

import com.google.common.base.Joiner
import groovy.transform.CompileStatic
import groovy.transform.ToString
import repo.build.CliOptions
import repo.build.CommandRegistry
import repo.build.RepoEnv

@CompileStatic
class ComboCommand extends AbstractCommand {
    AbstractCommand[] commands

    public ComboCommand(String name, String description, AbstractCommand... commands) {
        super(name, description)
        this.commands = commands
    }

    void execute(RepoEnv env, CliOptions options) {
        for (def command in commands) {
            logger.info("-- do subcommand: ${command.name}")
            command.execute(env, options)
        }
    }

}
