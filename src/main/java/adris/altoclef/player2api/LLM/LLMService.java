package adris.altoclef.player2api.LLM;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import adris.altoclef.player2api.Character;
import adris.altoclef.player2api.ConversationHistory;
import adris.altoclef.player2api.Player2APIService;
import adris.altoclef.player2api.Utils;
import adris.altoclef.player2api.actions.AltoclefCommand;
import adris.altoclef.player2api.actions.GameAction;
import adris.altoclef.player2api.actions.SendAssistantMessage;
import adris.altoclef.player2api.status.AgentStatus;
import adris.altoclef.player2api.status.WorldStatus;

public class LLMService {
    public static List<GameAction> GetGreetingResponse(LLMState state, AgentStatus agentStatus,
            WorldStatus worldStatus) {
        List<GameAction> gameActions = new ArrayList<>();

        try {
            Character character = Player2APIService.getSelectedCharacter();
            state.addUserMessage("Greet the user from these instructions:" + character.greetingInfo);
            ConversationHistory history = state.getConversationHistory(agentStatus, worldStatus, character);
            System.out.printf("[LLMService/GetGreetingResponse]: History: %s", history.toString());
            JsonObject response = Player2APIService.completeConversation(history);

            String responseAsString = response.toString();
            System.out.printf("[LLMService/GetGreetingResponse]: LLM Response: %s", responseAsString);

            // process message
            String llmMessage = Utils.getStringJsonSafely(response, "message");
            if (llmMessage != null && !llmMessage.isEmpty()) {
                gameActions.add(new SendAssistantMessage(llmMessage, character));

                // TODO: note if we do this here, then it will block. need to have two actions
                // maybe, game actions and blocking actions.
                // Player2APIService.textToSpeech(llmMessage, character);
            }
            return gameActions;

        } catch (Exception e) {
            System.err.println("[LLMService/GetGreetingResponse]: Error completing conversation");
            e.printStackTrace();
            return gameActions;
        }

    }

    public static List<GameAction> GetResponse(LLMState state, AgentStatus agentStatus, WorldStatus worldStatus) {
        // update character

        List<GameAction> gameActions = new ArrayList<>();
        try {
            Character character = Player2APIService.getSelectedCharacter();

            ConversationHistory history = state.getConversationHistory(agentStatus, worldStatus, character);

            System.out.printf("[LLMService/GetResponse]: History: %s", history.toString());
            JsonObject response = Player2APIService.completeConversation(history);

            String responseAsString = response.toString();
            System.out.printf("[LLMService/GetResponse]: LLM Response: %s", responseAsString);

            // process message
            String llmMessage = Utils.getStringJsonSafely(response, "message");
            if (llmMessage != null && !llmMessage.isEmpty()) {
                gameActions.add(new SendAssistantMessage(llmMessage, character));

                // TODO: note if we do this here, then it will block. need to have two actions
                // maybe, game actions and blocking actions.
                // Player2APIService.textToSpeech(llmMessage, character);
            }

            // process command
            String commandResponse = Utils.getStringJsonSafely(response, "command");
            if (commandResponse != null && !commandResponse.isEmpty()) {
                gameActions.add(new AltoclefCommand(commandResponse));
            }
            return gameActions;

        } catch (Exception e) {
            System.err.println("[LLMService/GetResponse]: Error completing conversation");
            e.printStackTrace();
            return gameActions;
        }
    }
}
