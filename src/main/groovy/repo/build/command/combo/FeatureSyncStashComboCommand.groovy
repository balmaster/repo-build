package repo.build.command.combo

import repo.build.command.AbstractCommand
import repo.build.command.ComboCommand
import repo.build.command.StashCommand
import repo.build.command.StashPopCommand
import repo.build.command.SwitchCommand
import repo.build.command.SyncCommand

class FeatureSyncStashComboCommand extends ComboCommand {

    FeatureSyncStashComboCommand() {
        super('fss', 'stash sync switch stash-pop combo',
                new StashCommand(),
                new SyncCommand(),
                new SwitchCommand(),
                new StashPopCommand()
        )
    }
}
