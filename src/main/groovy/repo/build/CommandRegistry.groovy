package repo.build

import repo.build.RepoBuildException
import repo.build.command.AbstractCommand

public class CommandRegistry {
    Map<String, AbstractCommand> commandMap = new HashMap<>()

    public void register(AbstractCommand command) {
        if (commandMap.containsKey(command.name)) {
            throw new RepoBuildException("Command with name ${command.name} already registered")
        }
        commandMap.put(command.name, command)
    }

    public AbstractCommand get(String name) {
        if (!commandMap.containsKey(command.name)) {
            throw new RepoBuildException("Command with name ${command.name} not registered")
        }
        return commandMap.get(name)
    }

    public Set<AbstractCommand> getCommands() {
        return commandMap.values()
    }
}
