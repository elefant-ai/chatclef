package adris.altoclef.commands;

import java.util.ArrayList;
import java.util.List;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ItemList;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;

public class GetCommand extends Command {

    public GetCommand() throws CommandException {
        super("get", "Get a resource, Get / Craft an item. Examples: `get log 20` gets 20 logs, `get diamond_chestplate 1` gets 1 diamond chestplate.", new Arg<>(ItemList.class, "items"));
    }

    private void getItems(AltoClef mod, ItemTarget... items) {
        Task targetTask;

        List<ItemTarget> resultTargets = new ArrayList<>();
        for (ItemTarget target : items) {
            int count = target.getTargetCount();
            // append to current count so this is workable with the agent
            count += AltoClef.getInstance().getItemStorage().getItemCountInventoryOnly(target.getMatches());

            resultTargets.add(new ItemTarget(target, count));
        }
        items = resultTargets.toArray(new ItemTarget[0]);

        if (items == null || items.length == 0) {
            mod.log("You must specify at least one item!");
            finish();
            return;
        }
        if (items.length == 1) {
            targetTask = TaskCatalogue.getItemTask(items[0]);
        } else {
            targetTask = TaskCatalogue.getSquashedItemTask(items);
        }
        if (targetTask != null) {
            mod.runUserTask(targetTask, this::finish);
        } else {
            finish();
        }
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        ItemList items = parser.get(ItemList.class);
        getItems(mod, items.items);
    }
}
