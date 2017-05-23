package repo.build.command

import groovy.transform.CompileStatic
import repo.build.CliOptions
import repo.build.RepoBuildException
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
            try {
                command.execute(env, options)
            }
            catch (Exception e) {
                throw new RepoBuildException("subcommand $command error ${e.message}", e)
            }
        }
    }

}
