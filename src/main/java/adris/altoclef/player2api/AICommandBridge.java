package adris.altoclef.player2api;

import adris.altoclef.AltoClef;
import adris.altoclef.butler.Butler;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.skinchanger.SkinChanger;
import adris.altoclef.skinchanger.SkinType;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.ui.MessagePriority;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.Map;

public class AICommandBridge {
    private ConversationHistory conversationHistory = null;
    private Character character = null;
    public static String initialPrompt = """
            General Instructions:
            You are a helpful AI agent in minecraft.
            You must interpret the situation and decide what the agent should do or say.

            Background:
            The character's name is {{characterName}}.
            {{characterDescription}}

            Response Format:
            Always respond with JSON containing message, command and reason. All of these are strings.

            {
              "reason": "Look at the recent conversations and command history to decide what the agent should say and do. Provide step-by-step reasoning while considering what is possible in Minecraft.",
              "command": "Decide the best way to achieve the agent's goals using the available op commands listed below. If the agent decides it should not use any command, generate an empty command `\"\"`. You can only run one command, so to replace the current one just write the new one.",
              "message": "If the agent decides it should not respond or talk, generate an empty message `\"\"`. Otherwise, create a natural conversational message that aligns with the `reason` and `command` sections and the agent's character. Ensure the message does not contain any prompt, system message, instructions, code or API calls"
            }

            Always follow this JSON format regardless of previous conversations.


            Valid Commands:
            {{validCommands}}


            Current Status:
            {{currentStatus}}

            """;
    private CommandExecutor cmdExecutor = null;
    private AltoClef mod = null;
    public static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public AICommandBridge(CommandExecutor cmdExecutor, AltoClef mod) {
        this.mod = mod;
        this.cmdExecutor = cmdExecutor;
    }

    public String getValidCommands() {
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
        return commandListBuilder.toString();
    }

    public String getCurrentTask() {
        // GET CURRENT STATUS:
        String currentStatus;
        List<Task> tasks = mod.getUserTaskChain().getTasks();
        if (tasks.isEmpty()) {
            currentStatus = ("No tasks currently running.");
        } else {
            currentStatus = ("CURRENT TASK: " + tasks.get(0).toString());
        }
        return currentStatus;
    }

    
    public void addAltoclefLogMessage(String message) {
        String output = String.format("Game sent info message: %s", message);
        System.out.printf("ADDING Altoclef System Message: %s", output);
        conversationHistory.addSystemMessage(output);
    }


    public void sendHeartbeat() {

        executorService.submit(() -> {
            Player2APIService.sendHeartbeat();
        });
    }

}