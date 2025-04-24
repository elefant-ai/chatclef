package adris.altoclef.player2api.eventqueue;

import adris.altoclef.player2api.LLM.LLMState;

public class UserMessage extends Event {

    private String msg;

    public UserMessage(String msg) {
        this.msg = msg;
    }

    @Override
    public LLMState handle(LLMState pvs) {
        pvs.addUserMessage(msg);
        return pvs;
    }
}