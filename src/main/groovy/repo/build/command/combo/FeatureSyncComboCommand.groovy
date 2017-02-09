package repo.build.command.combo

import repo.build.command.AbstractCommand
import repo.build.command.ComboCommand
import repo.build.command.SwitchCommand
import repo.build.command.SyncCommand

class FeatureSyncComboCommand extends ComboCommand {

    FeatureSyncComboCommand() {
        super('fs', 'sequentially execute sync switch commands',
                new SyncCommand(),
                new SwitchCommand())
    }
}
