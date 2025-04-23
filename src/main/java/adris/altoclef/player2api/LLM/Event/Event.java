package adris.altoclef.player2api.LLM.Event;

import java.util.List;

import adris.altoclef.player2api.LLM.State.LLMState;

public abstract class Event {
    protected final boolean shouldSave;
    public Event(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public abstract List<Event> handle(LLMState state);
}
