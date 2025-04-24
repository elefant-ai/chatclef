package adris.altoclef.player2api.eventqueue;

import java.util.List;

import adris.altoclef.player2api.AICommandBridge;
import adris.altoclef.player2api.LLM.LLMState;
import adris.altoclef.player2api.actions.GameAction;

public class Greeting extends Event {

    private String msg;

    public Greeting() {
    }

    @Override
    public List<GameAction> immediateHandle() {
        System.out.println("[Events/Greeting]: Adding Greeting event");
        return List.of();
    }

    @Override
    public LLMState updateState(LLMState pvs) {
        pvs.addUserMessage(msg);
        return pvs;
    }
}