package adris.altoclef.player2api.LLM.State;

import java.util.Optional;

import adris.altoclef.player2api.LLM.Event.Event;


public interface AtomicState {
    // Used for current state:
    String getSummary();

    // If this state changes, should we have an event?     
    Optional<Event> onChange(AtomicState oldState, AtomicState newState);
}
