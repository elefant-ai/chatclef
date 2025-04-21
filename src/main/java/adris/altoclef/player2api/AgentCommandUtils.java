package adris.altoclef.player2api;

import java.util.ArrayList;
import java.util.List;

import adris.altoclef.AltoClef;
import adris.altoclef.util.ItemTarget;

public class AgentCommandUtils {
    public static ItemTarget[] addPresentItemsToTargets(ItemTarget[] items) {
        List<ItemTarget> resultTargets = new ArrayList<>();
        for (ItemTarget target : items) {
            int count = target.getTargetCount();
            // append to current count so this is workable with the agent
            count += AltoClef.getInstance().getItemStorage().getItemCountInventoryOnly(target.getMatches());

            resultTargets.add(new ItemTarget(target, count));
        }
        return resultTargets.toArray(new ItemTarget[0]);
    }
}
