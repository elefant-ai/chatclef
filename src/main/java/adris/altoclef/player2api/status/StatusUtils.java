package adris.altoclef.player2api.status;

import java.util.HashMap;
import java.util.List;

import adris.altoclef.AltoClef;
import net.minecraft.item.ItemStack;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.tasksystem.Task;


public class StatusUtils{
    public static String getInventoryString(AltoClef mod){
        HashMap<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < mod.getPlayer().getInventory().size(); ++i) {
            ItemStack stack = mod.getPlayer().getInventory().getStack(i);
            if (!stack.isEmpty()) {
                String name = ItemHelper.stripItemName(stack.getItem());
                if (!counts.containsKey(name)) counts.put(name, 0);
                counts.put(name, counts.get(name) + stack.getCount());
            }
        }
        // Print
        String output = "{\n";
        for (String name : counts.keySet()) {
            output += (name + " : " + counts.get(name) + "\n");
        }
        output += "\n}";
        return output;
    }


    public static String getTaskStatus(AltoClef mod){
        List<Task> tasks = mod.getUserTaskChain().getTasks();
        if (tasks.isEmpty()) {
            return ("No tasks currently running.");
        } else {
            return ("CURRENT TASK: " + tasks.get(0).toString());
        }

    }
}