package repo.build

import groovy.transform.CompileStatic
import repo.build.command.AbstractCommand

@CompileStatic
public class CommandRegistry {
    Map<String, AbstractCommand> commandMap = new TreeMap<>()

    public void registerCommand(AbstractCommand command) {
        if (commandMap.containsKey(command.name)) {
            throw new RepoBuildException("Command with name ${command.name} already registered")
        }
        commandMap.put(command.name, command)
    }

    public AbstractCommand getCommand(String name) {
        if (!commandMap.containsKey(name)) {
            throw new RepoBuildException("Command with name ${name} not registered")
        }
        return commandMap.get(name)
    }

    public Collection<AbstractCommand> getCommands() {
        return commandMap.values()
    }
}
