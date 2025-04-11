package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.resources.CollectMeatTask;

public class AttackPlayerOrMobCommand extends Command {
    public AttackPlayerOrMobCommand() throws CommandException {
        super("attack", "Attacks a specified player or mob. Example usages: @attack zombie to attack a zombie, @attack Player", new Arg<>(String.class, "name"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String nameToAttack = parser.get(String.class);
        
        // mod.runUserTask(new CollectMeatTask(parser.get(Integer.class)), this::finish);
    }
}