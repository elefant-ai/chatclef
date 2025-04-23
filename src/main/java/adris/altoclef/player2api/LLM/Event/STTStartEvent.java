package adris.altoclef.player2api.LLM.Event;

import java.util.List;

import adris.altoclef.player2api.Player2APIService;
import adris.altoclef.player2api.LLM.State.LLMState;

public class STTStartEvent extends Event implements InstantEvent {
    public STTStartEvent() {
        super(false);
    }

    @Override
    public List<Event> handle(LLMState state) {
        Player2APIService.startSTT();
        System.out.println("[STT] Start");
        return List.of();
    }
}