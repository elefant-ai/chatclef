package adris.altoclef.player2api.LLM;

import adris.altoclef.player2api.Character;
import adris.altoclef.player2api.ConversationHistory;
import adris.altoclef.player2api.status.AgentStatus;
import adris.altoclef.player2api.status.WorldStatus;

public class LLMState {
    private ConversationHistory conversationHistory;

    public LLMState( ) {
        this.conversationHistory = new ConversationHistory("");
    }

    public void addUserMessage(String message) {
        conversationHistory.addUserMessage(message);
    }

    public void addAssistantMessage(String message) {
        conversationHistory.addAssistantMessage(message);
    }

  
    public ConversationHistory getConversationHistory(AgentStatus agentStatus, WorldStatus worldStatus, Character character){
        ConversationHistory withStatus = conversationHistory.copyThenWrapLatestWithStatus(worldStatus, agentStatus);
        withStatus.setBaseSystemPrompt(SystemPrompt.getSystemMessage(character));
        return withStatus;
    }

    public boolean isLastMessageFromAssistant(){
        return conversationHistory.isLastMessageFromAssistant();
    }

    public boolean haveSentGreeting(){
        return conversationHistory.hasAssistantMessage();
    }

}
