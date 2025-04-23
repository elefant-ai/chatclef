package adris.altoclef.player2api.LLM.Event;

import java.util.List;

import adris.altoclef.player2api.LLM.State.LLMState;

public class CancelEvent extends Event {

    public CancelEvent() {
        super(false);
    }

    @Override
    public List<Event> handle(LLMState state) {
        return List.of();
    }
}