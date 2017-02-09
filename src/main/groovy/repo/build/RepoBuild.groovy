package repo.build

import groovy.transform.CompileStatic
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager
import repo.build.command.BuildPomCommand
import repo.build.command.ExportBundlesCommand
import repo.build.command.FeatureMergeReleaseCommand
import repo.build.command.FeatureUpdateParentCommand
import repo.build.command.FeatureUpdateVersionsCommand
import repo.build.command.GrepCommand
import repo.build.command.InitCommand
import repo.build.command.MergeAbortCommand
import repo.build.command.PrepareMergeCommand
import repo.build.command.PushFeatureCommand
import repo.build.command.PushManifestCommand
import repo.build.command.ReleaseMergeFeatureCommand
import repo.build.command.ReleaseUpdateParentCommand
import repo.build.command.ReleaseUpdateVersionsCommand
import repo.build.command.StashCommand
import repo.build.command.StashPopCommand
import repo.build.command.StatusCommand
import repo.build.command.SwitchCommand
import repo.build.command.SyncCommand
import repo.build.command.combo.FeatureSyncComboCommand
import repo.build.command.combo.FeatureSyncStashComboCommand

@CompileStatic
class RepoBuild {

    static Logger logger = LogManager.getLogger(RepoBuild.class)

    final CliBuilder cli
    final String[] args
    final CommandRegistry commandRegistry
    final CliOptions options
    final RepoEnv env

    RepoBuild(String[] args) {
        this.commandRegistry = new CommandRegistry()
        commandRegistry.register(new BuildPomCommand())
        commandRegistry.register(new ExportBundlesCommand())
        commandRegistry.register(new FeatureMergeReleaseCommand())
        commandRegistry.register(new FeatureUpdateParentCommand())
        commandRegistry.register(new FeatureUpdateVersionsCommand())
        commandRegistry.register(new GrepCommand())
        commandRegistry.register(new InitCommand())
        commandRegistry.register(new InitCommand())
        commandRegistry.register(new MergeAbortCommand())
        commandRegistry.register(new PrepareMergeCommand())
        commandRegistry.register(new PushFeatureCommand())
        commandRegistry.register(new PushManifestCommand())
        commandRegistry.register(new ReleaseMergeFeatureCommand())
        commandRegistry.register(new ReleaseUpdateParentCommand())
        commandRegistry.register(new ReleaseUpdateVersionsCommand())
        commandRegistry.register(new StashCommand())
        commandRegistry.register(new StashPopCommand())
        commandRegistry.register(new StatusCommand())
        commandRegistry.register(new SwitchCommand())
        commandRegistry.register(new SyncCommand())
        // combo
        commandRegistry.register(new FeatureSyncComboCommand())
        commandRegistry.register(new FeatureSyncStashComboCommand())
        this.cli = CliBuilderFactory.build(commandRegistry)
        this.args = args
        this.options = new CliOptions(cli.parse(args))
        this.env = new RepoEnv(options.getRepoBasedir())
    }

    static void main(String[] args) {
        def repoBuild = new RepoBuild(args)
        try {
            repoBuild.execute()
        }
        catch (Exception e) {
            if (repoBuild.options.isDebugMode()) {
                logger.error(e.message, e)
            } else {
                logger.error(e.message)
            }
            System.exit(1);
        }
    }

    void execute() {
        def commands = options.getArguments()
        if (commands.size() > 0) {
            commands.each {
                executeCommand(it)
            }
        } else {
            cli.usage()
        }
    }

    def executeCommand(String commandName) {
        logger.info("--- do command: $commandName")
        def command = commandRegistry.get(commandName)
        command.execute(env, options)
    }

}
