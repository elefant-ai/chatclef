package adris.altoclef.player2api.events;

import java.util.ArrayList;
import java.util.List;

public class HostileMobEvent extends Event {
    private final List<String> mobs;

    public HostileMobEvent(List<String> initialMobs) {
        super("hostile_mob");
        this.mobs = new ArrayList<>(initialMobs);
    }

    @Override
    public void combineWith(Event other) {
        if (other instanceof HostileMobEvent) {
            mobs.addAll(((HostileMobEvent) other).mobs);
        }
    }

    @Override
    public void handle(boolean isFinalCall) {
        System.out.println("Handling hostile mobs: " + mobs + ", isFinal: " + isFinalCall);
    }
}
