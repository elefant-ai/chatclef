package adris.altoclef.player2api.LLM.State;

import java.util.List;
import java.util.Optional;

import adris.altoclef.player2api.LLM.Event.Event;


public abstract class AtomicState<T extends AtomicState<T>>  {
    // Used for current state:
    public abstract String getSummary();

    // If this state changes, should we have an event?     
    public abstract List<Event> onChange(T oldState, T newState);
}
