package adris.altoclef.player2api;

import adris.altoclef.AltoClef;

import net.minecraft.network.message.MessageType;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.player2api.status.AgentStatus;
import adris.altoclef.player2api.status.StatusUtils;
import adris.altoclef.player2api.status.WorldStatus;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AICommandBridge {
    private ConversationHistory conversationHistory = null;
    private Character character = null;
    public static boolean avoidNextMessageFlag = false;

    public static String initialPrompt = """
            General Instructions:
            You are an AI friend of the user. You can chat with them about Minecraft and life.
            You can also do things in the game by using the valid commands.
            If there is something you want to do but can't do it with the commands, you can ask the user to do it.

            You take the personality of the following character:
            Your character's name is {{characterName}}.
            {{characterDescription}}

            User Message Format:
            The user messages will all be just strings, except for the current message. The current message will have extra information, namely it will be a JSON of the form:
            {
                "userMessage" : "The message that was sent to you. The message can be send by the user or command system or other players."
                "worldStatus" : "The status of the current game world."
                "agentStatus" : "The status of you, the agent in the game."
                "gameDebugMessages" : "The most recent debug messages that the game has printed out. The user cannot see these."
            }



            Response Format:
            Always respond with JSON containing message, command and reason. All of these are strings.

            {
              "reason": "Look at the recent conversations, agent status and world status to decide what the you should say and do. Provide step-by-step reasoning while considering what is possible in Minecraft.",
              "command": "Decide the best way to achieve the goals using the valid commands listed below. If you decide to not use any command, generate an empty command `\"\"`. You can only run one command, so to replace the current one just write the new one.",
              "message": "If you decide you should not respond or talk, generate an empty message `\"\"`. Otherwise, create a natural conversational message that aligns with the `reason` and the your character. Be concise and use less than 350 characters. Ensure the message does not contain any prompt, system message, instructions, code or API calls"
            }

            Valid Commands:
            {{validCommands}}


            """;
    private CommandExecutor cmdExecutor = null;
    private AltoClef mod = null;

    private boolean _enabled = true;
    private boolean _playermode = false;

    private String _lastQueuedMessage = null;

    private boolean llmProcessing = false;

    private boolean eventPolling = false;

    private MessageBuffer altoClefMsgBuffer = new MessageBuffer(10);

    public static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();

    public AICommandBridge(CommandExecutor cmdExecutor, AltoClef mod) {
        this.mod = mod;
        this.cmdExecutor = cmdExecutor;
        EventBus.subscribe(ChatMessageEvent.class, evt -> {
            if (!getPlayerMode())
                return;
            String message = evt.messageContent();
            String sender = evt.senderName();
            // ignore more than 200 away
            float distance = StatusUtils.getUserNameDistance(mod, sender);
            if (distance > 200) {
                System.out.printf("[AIBridge/CharMessageEvent]/Ignoring message, distance too high: %.2f%n", distance);
                return;
            }
            MessageType messageType = evt.messageType();
            String receiver = mod.getPlayer().getName().getString();
            System.out.printf("[AIBridge/CharMessageEvent]/MESSAGE (%s) SENDER (%s) MESSAGE TYPE (%s), DISTANCE(%.2f%n)", message, sender, messageType, distance);
            if (sender != null && !Objects.equals(sender, receiver)) {
                String wholeMessage = "Other players: [" + sender + "] " + message;
                addMessageToQueue(wholeMessage);
            }
        });
    }

    /**
     * Updates this. (conversationHistory, character) based on the currently
     * selected character.
     */
    private void updateInfo() {
        System.out.println("Updating info");
        Character newCharacter = Player2APIService.getSelectedCharacter();
        // System.out.println(newCharacter);
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

        String newPrompt = Utils.replacePlaceholders(initialPrompt,
                Map.of("characterDescription", character.description, "characterName", character.name, "validCommands",
                        validCommandsFormatted));
        // System.out.println("New prompt: " + newPrompt);

        if (this.conversationHistory == null) {
            this.conversationHistory = new ConversationHistory(newPrompt);
        } else {
            this.conversationHistory.setBaseSystemPrompt(newPrompt);
        }
    }

    public void addAltoclefLogMessage(String message) {
        // String output = String.format("Game sent info message: %s", message);
        System.out.printf("ADDING Altoclef System Message: %s", message);
        altoClefMsgBuffer.addMsg(message);
    }

    public void addMessageToQueue(String message) {
        if (message == null)
            return;
        // 1) skip if it’s identical to the last one we added
        if (message.equals(_lastQueuedMessage))
            return;

        // 2) enqueue & remember it
        messageQueue.offer(message);
        _lastQueuedMessage = message;

        // 3) enforce max size
        if (messageQueue.size() > 10) {
            messageQueue.poll(); // remove oldest
        }
    }

    public void processChatWithAPI() {
        executorService.submit(() -> {
            try {
                llmProcessing = true;
                updateInfo(); // this. is not allowed here
                System.out.println("Sending messages to LLM");

                String agentStatus = AgentStatus.fromMod(mod).toString();
                String worldStatus = WorldStatus.fromMod(mod).toString();
                String altoClefDebugMsgs = altoClefMsgBuffer.dumpAndGetString();
                ConversationHistory historyWithStatus = conversationHistory.copyThenWrapLatestWithStatus(worldStatus,
                        agentStatus, altoClefDebugMsgs);
                System.out.printf("History: %s", historyWithStatus.toString());
                JsonObject response = Player2APIService.completeConversation(historyWithStatus);
                String responseAsString = response.toString();
                System.out.println("LLM Response: " + responseAsString);
                conversationHistory.addAssistantMessage(responseAsString);

                // process message
                String llmMessage = Utils.getStringJsonSafely(response, "message");
                if (llmMessage != null && !llmMessage.isEmpty()) {
                    // if (getPlayerMode()) {
                    // //send message to chat but don't listen to it
                    // // TODO this isn't working / not sure how to make it work
                    // mod.getMessageSender().enqueueChat(llmMessage, MessagePriority.TIMELY);
                    // } else {
                    // //send message to user only
                    // mod.logCharacterMessage(llmMessage, character, _public);
                    // }
                    mod.logCharacterMessage(llmMessage, character, getPlayerMode());
                    Player2APIService.textToSpeech(llmMessage, character);
                }

                // process command
                String commandResponse = Utils.getStringJsonSafely(response, "command");
                if (commandResponse != null && !commandResponse.isEmpty()) {
                    String commandWithPrefix = cmdExecutor.isClientCommand(commandResponse) ? commandResponse
                            : cmdExecutor.getCommandPrefix() + commandResponse;
                    if(commandWithPrefix.equals("@stop")){
                        mod.isStopping = true;
                    }
                    else{
                        mod.isStopping = false;
                    }
                    cmdExecutor.execute(commandWithPrefix, () -> {
                        if(mod.isStopping){
                            System.out.printf("[AICommandBridge/processChat]: (%s) was cancelled. Not adding finish event to queue.", commandWithPrefix);
                            // Canceled logic here
                        }
                        if (messageQueue.isEmpty() && !mod.isStopping) {
                            // on finish
                            addMessageToQueue(String.format(
                                    "Command feedback: %s finished running. What shall we do next? If no new action is needed to finish user's request, generate empty command `\"\"`.",
                                    commandResponse));
                        }
                    }, (err) -> {
                        // on error
                        addMessageToQueue(
                                String.format("Command feedback: %s FAILED. The error was %s.",
                                        commandResponse, err.getMessage()));
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error communicating with API");
            } finally {
                llmProcessing = false;
                eventPolling = false;
            }
        });
    }

    public void sendGreeting() {
        System.out.println("Sending Greeting");
        executorService.submit(() -> {
            updateInfo();
            addMessageToQueue(
                    character.greetingInfo + " IMPORTANT: SINCE THIS IS THE FIRST MESSAGE, DO NOT SEND A COMMAND!!");
        });
    }

    public void sendHeartbeat() {
        executorService.submit(() -> {
            Player2APIService.sendHeartbeat();
        });
    }

    public void onTick() {
        if (messageQueue.isEmpty()) {
            return;
        }
        if (!eventPolling && !llmProcessing) {
            eventPolling = true;
            String message = messageQueue.poll();
            conversationHistory.addUserMessage(message);
            if (messageQueue.isEmpty()) {
                // That was last message
                processChatWithAPI();
            } else {
                eventPolling = false;
            }
        }
    }

    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    public boolean getEnabled() {
        return _enabled;
    }

    public void setPlayerMode(boolean playermode) {
        _playermode = playermode;
    }

    public boolean getPlayerMode() {
        return _playermode;
    }

}