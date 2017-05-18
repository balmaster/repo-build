package repo.build

import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import repo.build.command.AddTagToCurrentHeadsCommand
import repo.build.command.BuildPomCommand
import repo.build.command.CheckoutTagCommand
import repo.build.command.ExportBundlesCommand
import repo.build.command.FeatureMergeReleaseCommand
import repo.build.command.FeatureUpdateParentCommand
import repo.build.command.FeatureUpdateVersionsCommand
import repo.build.command.GrepCommand
import repo.build.command.InitCommand
import repo.build.command.MergeAbortCommand
import repo.build.command.MvnBuildCommand
import repo.build.command.PrepareMergeCommand
import repo.build.command.PushFeatureCommand
import repo.build.command.PushManifestCommand
import repo.build.command.PushTagCommand
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

    static Logger logger = Logger.getLogger(RepoBuild.class)

    final CliBuilder cli
    final String[] args
    final CommandRegistry commandRegistry
    final CliOptions options
    final RepoEnv env

    RepoBuild(String[] args) {
        this.commandRegistry = new CommandRegistry()
        commandRegistry.registerCommand(new BuildPomCommand())
        commandRegistry.registerCommand(new ExportBundlesCommand())
        commandRegistry.registerCommand(new FeatureMergeReleaseCommand())
        commandRegistry.registerCommand(new FeatureUpdateParentCommand())
        commandRegistry.registerCommand(new FeatureUpdateVersionsCommand())
        commandRegistry.registerCommand(new GrepCommand())
        commandRegistry.registerCommand(new InitCommand())
        commandRegistry.registerCommand(new MergeAbortCommand())
        commandRegistry.registerCommand(new PrepareMergeCommand())
        commandRegistry.registerCommand(new PushFeatureCommand())
        commandRegistry.registerCommand(new PushManifestCommand())
        commandRegistry.registerCommand(new ReleaseMergeFeatureCommand())
        commandRegistry.registerCommand(new ReleaseUpdateParentCommand())
        commandRegistry.registerCommand(new ReleaseUpdateVersionsCommand())
        commandRegistry.registerCommand(new StashCommand())
        commandRegistry.registerCommand(new StashPopCommand())
        commandRegistry.registerCommand(new StatusCommand())
        commandRegistry.registerCommand(new SwitchCommand())
        commandRegistry.registerCommand(new SyncCommand())
        commandRegistry.registerCommand(new AddTagToCurrentHeadsCommand())
        commandRegistry.registerCommand(new PushTagCommand())
        commandRegistry.registerCommand(new CheckoutTagCommand())
        commandRegistry.registerCommand(new MvnBuildCommand())
        // combo
        commandRegistry.registerCommand(new FeatureSyncComboCommand())
        commandRegistry.registerCommand(new FeatureSyncStashComboCommand())
        this.cli = CliBuilderFactory.build(commandRegistry)
        this.args = args
        this.options = new CliOptions(cli.parse(args))
        this.env = new RepoEnv(options.getRepoBasedir())
    }

    static void main(String[] args) {
        def repoBuild = new RepoBuild(args)
        try {
            repoBuild.execute()
            repoBuild.waitIfRequired()
        }
        catch (Exception e) {
            if (repoBuild.options.isDebugMode()) {
                logger.error(e.message, e)
            } else {
                logger.error(e.message)
            }
            repoBuild.waitIfRequired()
            System.exit(1);
        }
    }

    void waitIfRequired() {
        if(options.isWaitBeforeExit()) {
            System.out.println('Press <Enter> to continue')
            System.in.read()
        }
    }

    void execute() {
        def commands = options.getArguments()
        if (commands.size() > 0) {
            commands.each { String commandName ->
                executeCommand(commandName)
            }
        } else {
            cli.usage()
        }
    }

    def executeCommand(String commandName) {
        logger.info("--- do command: $commandName")
        def command = commandRegistry.getCommand(commandName)
        command.execute(env, options)
    }

}
