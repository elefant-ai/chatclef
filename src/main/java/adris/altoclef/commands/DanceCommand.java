package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.*;
import adris.altoclef.tasks.movement.BodyLanguageTask;
public class DanceCommand extends Command{
    public DanceCommand() throws CommandException {
        super("dance", "Perform some sort of dance/body language action. Action must be either `greeting`, `nod_head`, `shake_head`, `victory` Examples: `dance greeting` to do a greeting dance. `dance shake_head` shakes head, like saying no.", new Arg<>(String.class, "bodyLanguage"));
    }
    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String bodyLanguage = parser.get(String.class);

        mod.runUserTask(new BodyLanguageTask(bodyLanguage));
    }
    
}
