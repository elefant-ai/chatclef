package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.resources.CollectMeatTask;

import adris.altoclef.util.helpers.ItemHelper;

public class MeatCommand extends Command {
    public MeatCommand() throws CommandException {
        super("meat", "Collects a certain amount of meat", new Arg<>(Integer.class, "count"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        int count = parser.get(Integer.class);
        // agent integration
        count += AltoClef.getInstance().getItemStorage().getItemCountInventoryOnly(ItemHelper.COOKED_FOODS );

        mod.runUserTask(new CollectMeatTask(count), this::finish);
    }
}