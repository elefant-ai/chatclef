package adris.altoclef.player2api.LLM.Event;

import java.util.Optional;

public abstract class Event {
    protected final boolean shouldSave;
    public Event(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public abstract Optional<Event[]> handle();
}
