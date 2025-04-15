package adris.altoclef.player2api;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ConversationHistory {
    private final List<JsonObject> conversationHistory = new ArrayList<>();

    public ConversationHistory(String initialSystemPrompt) {
        setBaseSystemPrompt(initialSystemPrompt); // Ensures system message always exists
    }

    public void addHistory(JsonObject text) {
        conversationHistory.add(text);
        if (conversationHistory.size() > 64) {
            // 0th index is always system prompt
            conversationHistory.remove(1);
        }
    }

    public void addUserMessage(String userText) {
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "user");
        objectToAdd.addProperty("content", userText);
        addHistory(objectToAdd);
    }

    /**
     * Sets or updates the first system message (base system prompt).
     * If there is no system message, it adds one at the start.
     *
     * @param newPrompt The new base system prompt.
     */
    public void setBaseSystemPrompt(String newPrompt) {
        if (!conversationHistory.isEmpty() && "system".equals(conversationHistory.get(0).get("role").getAsString())) {
            conversationHistory.get(0).addProperty("content", newPrompt);
        } else {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", newPrompt);
            conversationHistory.add(0, systemMessage);
        }
    }

    /**
     * Adds a new system message at the end of the conversation history.
     *
     * @param systemText The system message to add.
     */
    public void addSystemMessage(String systemText) {
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "system");
        objectToAdd.addProperty("content", systemText);
        addHistory(objectToAdd);
    }


    /**
     * Adds a new assistant message at the end of the conversation history.
     *
     * @param messageText The system message to add.
     */
    public void addAssistantMessage(String messageText){
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "assistant");
        objectToAdd.addProperty("content", messageText);
        addHistory(objectToAdd);
    }

    public List<JsonObject> getListJSON() {
        return conversationHistory;
    }
}