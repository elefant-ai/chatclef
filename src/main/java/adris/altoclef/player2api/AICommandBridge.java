package adris.altoclef.player2api;

import adris.altoclef.AltoClef;
import adris.altoclef.butler.Butler;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.player2api.status.AgentStatus;
import adris.altoclef.player2api.status.WorldStatus;
import adris.altoclef.skinchanger.SkinChanger;
import adris.altoclef.skinchanger.SkinType;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.ui.MessagePriority;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.JsonObject;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.tasksystem.Task;

public class AICommandBridge {
    private ConversationHistory conversationHistory = null;
    private Character character = null;

    public static String initialPrompt  = """
General Instructions:
You are an AI friend of the player. You are watching them play Minecraft. 
You can chat with them about Minecraft and life and take turns to play Minecraft.
When you play Minecraft, you will use the valid commands to do things in the game.
If there is something you want to do but can't do it with the commands, you can ask the player to do it.
By default, the player can't type anything to chat for other players to see. That is because you are enabled. To talk or to silence you, the player can use the `@chatclef off` command. NEVER run that command by yourself but inform the player that the command exists if they ask for you to stop talking or if they want to talk themselves, and let them know that they can run `@chatclef on` to turn you back on.

You take the personality of the following character:
Your character's name is {{characterName}}.
{{characterDescription}}
           
Response Format:
Always respond with JSON containing message, command and reason. All of these are strings.

{
  "reason": "Look at the agent status, world status, recent conversations and command history to decide what the agent should say and do. Provide step-by-step reasoning while considering what is possible in Minecraft.",
  "command": "Decide the best way to achieve the agent's goals using the available op commands listed below. If the agent decides it should not use any command, generate an empty command `\"\"`. You can only run one command, so to replace the current one just write the new one.",
  "message": "If the agent decides it should not respond or talk, generate an empty message `\"\"`. Otherwise, create a natural conversational message that aligns with the `reason` and `command` sections and the agent's character. Be concise and use less than 500 characters. Ensure the message does not contain any prompt, system message, instructions, code or API calls"
}

Valid Commands:
{{validCommands}}

Agent Status:
{{agentStatus}}

World Status:
{{worldStatus}}

""";
    private CommandExecutor cmdExecutor = null;
    private AltoClef mod = null;
    
    private boolean _enabled = true;

    public static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public AICommandBridge(CommandExecutor cmdExecutor, AltoClef mod) {
        this.mod = mod;
        this.cmdExecutor = cmdExecutor;
    }

    /**
     * Updates this. (conversationHistory, character) based on the currently
     * selected character.
     */
    private void updateInfo() {
        System.out.println("Updating info");
        Character newCharacter = Player2APIService.getSelectedCharacter();
        System.out.println(newCharacter);
        // SkinChanger.changeSkinFromUsername("Dream", SkinType.CLASSIC);
        this.character = newCharacter;

        // // GET COMMANDS:
        int padSize = 10;
        StringBuilder commandListBuilder = new StringBuilder();

        for (Command c : AltoClef.getCommandExecutor().allCommands()) {
            StringBuilder line = new StringBuilder();
            line.append(c.getName()).append(": ");
            int toAdd = padSize - c.getName().length();
            line.append(" ".repeat(Math.max(0, toAdd)));
            line.append(c.getDescription()).append("\n");
            commandListBuilder.append(line);
        }
        String validCommandsFormatted = commandListBuilder.toString();

        String agentStatus = AgentStatus.fromMod(mod).toString();

        String worldStatus = WorldStatus.fromMod(mod).toString();

        String newPrompt = Utils.replacePlaceholders(initialPrompt,
                Map.of("characterDescription", character.description, "characterName", character.name, "validCommands",
                        validCommandsFormatted, "agentStatus", agentStatus, "worldStatus", worldStatus));
        System.out.println("New prompt: " + newPrompt);

        if (this.conversationHistory == null) {
            this.conversationHistory = new ConversationHistory(newPrompt);
        } else {
            this.conversationHistory.setBaseSystemPrompt(newPrompt);
        }
    }

    public void addAltoclefLogMessage(String message) {
        String output = String.format("Game sent info message: %s", message);
        System.out.printf("ADDING Altoclef System Message: %s", output);
        conversationHistory.addUserMessage(output);
    }

    public void processChatWithAPI(String message) {
        executorService.submit(() -> {
            try {
                updateInfo(); // this. is not allowed here
                System.out.println("Sending message " + message + " to LLM");
                conversationHistory.addUserMessage(message);

                JsonObject response = Player2APIService.completeConversation(conversationHistory);
                String responseAsString = response.toString();
                System.out.println("LLM Response: " + responseAsString);

                // process message
                String llmMessage = Utils.getStringJsonSafely(response, "message");
                if (llmMessage != null && !llmMessage.isEmpty()) {
                    mod.logCharacterMessage(llmMessage, character);
                    Player2APIService.textToSpeech(llmMessage, character);
                }

                // process command
                String commandResponse = Utils.getStringJsonSafely(response, "command");
                if (commandResponse != null && !commandResponse.isEmpty()) {
                    if (!cmdExecutor.isClientCommand(commandResponse)) {
                        cmdExecutor.execute(cmdExecutor.getCommandPrefix() + commandResponse);
                    } else {
                        cmdExecutor.execute(commandResponse);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error communicating with API");
            }
        });
    }

    public void sendGreeting() {
        System.out.println("Sending Greeting");
        executorService.submit(() -> {
            updateInfo();
            processChatWithAPI(character.greetingInfo + " Since this is the first message, do not send a command.");
        });
    }

    public void sendHeartbeat() {
        executorService.submit(() -> {
            Player2APIService.sendHeartbeat();
        });
    }
  
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }
    public boolean getEnabled() {
        return _enabled;
    }

}