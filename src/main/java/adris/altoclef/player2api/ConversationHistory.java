package adris.altoclef.player2api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import adris.altoclef.player2api.status.ObjectStatus;
import net.fabricmc.loader.api.FabricLoader;

public class ConversationHistory {
    private final List<JsonObject> conversationHistory = new ArrayList<>();
    private final Path historyFile;
    private boolean loadedFromFile = false;
    private static final int MAX_HISTORY = 64;
    private static final int SUMMARY_COUNT = 48;

    /**
     * Constructs conversation history tied to a specific character, naming the file
     * accordingly.
     * 
     * @param initialSystemPrompt base system prompt
     * @param characterName       the full character name
     * @param characterShortName  the short identifier for the character
     */
    public ConversationHistory(String initialSystemPrompt, String characterName, String characterShortName) {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        String fileName = characterName.replaceAll("\\s+", "_") + "_" + characterShortName + ".txt";
        this.historyFile = configDir.resolve(fileName);

        if (Files.exists(historyFile)) {
            loadFromFile();
            setBaseSystemPrompt(initialSystemPrompt);
            loadedFromFile = true;
        } else {
            setBaseSystemPrompt(initialSystemPrompt);
            loadedFromFile = false;
        }
    }

    /**
     * Helper constructor for temporary summarization (no file persistence).
     */
    private ConversationHistory(String initialSystemPrompt) {
        this.historyFile = null;
        setBaseSystemPrompt(initialSystemPrompt);
        loadedFromFile = false;
    }

    /**
     * Whether this history was restored from disk.
     */
    public boolean isLoadedFromFile() {
        return loadedFromFile;
    }

    /**
     * Adds a new message to the history. Summarizes and persists when exceeding
     * MAX_HISTORY.
     */
    public void addHistory(JsonObject text, boolean doCutOff) {
        conversationHistory.add(text);
        if (doCutOff && conversationHistory.size() > MAX_HISTORY) {
            List<JsonObject> toSummarize = new ArrayList<>(conversationHistory.subList(1, SUMMARY_COUNT + 1));
            String summary = summarizeHistory(toSummarize);

            if (summary == "") {
                // 0th index is always system prompt
                conversationHistory.remove(1);
            } else {
                JsonObject systemPrompt = conversationHistory.get(0);
                int tailStart = conversationHistory.size() - (MAX_HISTORY - SUMMARY_COUNT);
                List<JsonObject> tail = new ArrayList<>(
                        conversationHistory.subList(tailStart, conversationHistory.size()));

                conversationHistory.clear();
                conversationHistory.add(systemPrompt);
                JsonObject summaryMsg = new JsonObject();
                summaryMsg.addProperty("role", "assistant");
                summaryMsg.addProperty("content", "Summary of earlier events: " + summary);
                conversationHistory.add(summaryMsg);
                conversationHistory.addAll(tail);
            }
            if (historyFile != null)
                saveToFile();
        } else if (doCutOff && conversationHistory.size() % 8 == 0 && historyFile != null) {
            saveToFile();
        }
    }

    /**
     * Calls LLM to generate a concise summary of provided messages.
     */
    private String summarizeHistory(List<JsonObject> messages) {
        String summarizationPrompt = """
                    Our AI agent that has been chatting with user and playing minecraft.
                    Update agent's memory by summarizing the following conversation in the next response.

                    Use natural language, not JSON format.

                    Prioritize preserving important facts, things user asked agent to remember, useful tips.
                    Do not record stats, inventory, code or docs; limit to 500 chars.
                """;
        ConversationHistory temp = new ConversationHistory(summarizationPrompt);
        for (JsonObject msg : messages)
            temp.addHistory(Utils.deepCopy(msg), false);

        // temp.addUserMessage("Now return the summary.");
        try {
            String resp = Player2APIService.completeConversationToString(temp);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error communicating with API");
            return "";
        }
    }

    /**
     * Persist current history to disk (one JSON per line).
     */
    private void saveToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(historyFile)) {
            for (JsonObject msg : conversationHistory) {
                writer.write(msg.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load history from disk into memory.
     */
    private void loadFromFile() {
        List<JsonObject> loaded = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(historyFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                JsonObject obj = Utils.parseCleanedJson(line);
                // Trim content field if too long
                if (obj.has("content")) {
                    String content = obj.get("content").getAsString();
                    if (content.length() > 500) {
                        obj.addProperty("content", content.substring(0, 500));
                    }
                }
                loaded.add(obj);
                // Enforce maximum history in memory by discarding oldest when exceeding
                if (loaded.size() > MAX_HISTORY) {
                    break;
                }
            }
            conversationHistory.clear();
            conversationHistory.addAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
            conversationHistory.clear();
        }
    }

    /**
     * Adds a user message without triggering cutoff.
     */
    public void addUserMessage(String userText) {
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "user");
        objectToAdd.addProperty("content", userText);
        addHistory(objectToAdd, false);
    }

    /**
     * Sets or updates the base system prompt at index 0.
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
     * Queues a system-level message without cutoff.
     */
    public void addSystemMessage(String systemText) {
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "system");
        objectToAdd.addProperty("content", systemText);
        addHistory(objectToAdd, false);
    }

    /**
     * Queues an assistant message and triggers cutoff logic.
     */
    public void addAssistantMessage(String messageText) {
        JsonObject objectToAdd = new JsonObject();
        objectToAdd.addProperty("role", "assistant");
        objectToAdd.addProperty("content", messageText);
        addHistory(objectToAdd, true);
    }

    /**
     * Exposes the raw conversation JSON list.
     */
    public List<JsonObject> getListJSON() {
        return conversationHistory;
    }

    /**
     * Wraps the latest user message with world/agent/debug status for LLM input.
     */
    public ConversationHistory copyThenWrapLatestWithStatus(String worldStatus, String agentStatus,
            String altoclefStatusMsgs) {
        ConversationHistory copy = new ConversationHistory(
                conversationHistory.get(0).get("content").getAsString());
        for (int i = 1; i < conversationHistory.size() - 1; i++) {
            copy.addHistory(Utils.deepCopy(conversationHistory.get(i)), false);
        }
        if (conversationHistory.size() > 1) {
            JsonObject last = Utils.deepCopy(conversationHistory.get(conversationHistory.size() - 1));
            if ("user".equals(last.get("role").getAsString())) {
                String originalContent = last.get("content").getAsString();
                ObjectStatus msgObj = new ObjectStatus();
                msgObj.add("userMessage", originalContent);
                msgObj.add("worldStatus", worldStatus);
                msgObj.add("agentStatus", agentStatus);
                msgObj.add("gameDebugMessages", altoclefStatusMsgs);
                last.addProperty("content", msgObj.toString());
            }
            copy.addHistory(last, false);
        }
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConversationHistory {\n");
        for (JsonObject message : conversationHistory) {
            String role = message.has("role") ? message.get("role").getAsString() : "unknown";
            String content = message.has("content") ? message.get("content").getAsString() : "";
            sb.append("  [").append(role).append("] ").append(content).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public void clear() {
        if (!conversationHistory.isEmpty()) {
            JsonObject systemPrompt = conversationHistory.get(0);
            conversationHistory.clear();
            conversationHistory.add(systemPrompt);
        }
        if (historyFile != null) {
            try {
                Files.deleteIfExists(historyFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}