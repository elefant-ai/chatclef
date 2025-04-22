package adris.altoclef.player2api.LLM.State;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import adris.altoclef.player2api.Character;
import adris.altoclef.player2api.LLM.Event.Event;

// Contains all state information to be pased to the agent.
public class LLMState {
    private Deque<Event> pvsEvents;
    private Optional<CharacterState> curCharState; // .none() if not loaded
    public LLMState(){
        curCharState = Optional.empty();
        pvsEvents = new ArrayDeque<>();
    }
}
