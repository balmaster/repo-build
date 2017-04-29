package repo.build.command.combo

import repo.build.command.ComboCommand
import repo.build.command.SwitchCommand
import repo.build.command.SyncCommand

class FeatureSyncComboCommand extends ComboCommand {

    FeatureSyncComboCommand() {
        super('fs', 'sync switch combo',
                new SyncCommand(),
                new SwitchCommand())
    }
}
