package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.*;
import adris.altoclef.tasks.movement.BodyLanguageTask;
public class DanceCommand extends Command{
    public DanceCommand() throws CommandException {
        super("dance", "Perform some sort of dance/body language action. Action must be either `greeting`, `sad`, `celebration` Examples: `dance greeting` to do a greeting dance. `dance sad` to look down when sad.", new Arg(String.class, "username", null, 2), new Arg(String.class, "item"), new Arg(Integer.class, "count", 1, 1));
    }
    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String bodyLanguage = parser.get(String.class);

        mod.runUserTask(new BodyLanguageTask(bodyLanguage));
    }
    
}
