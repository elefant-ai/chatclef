package adris.altoclef.butler;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.eventbus.events.TaskFinishedEvent;
import adris.altoclef.ui.MessagePriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageType;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * The butler system lets authorized players send commands to the bot to execute.
 * <p>
 * This effectively makes the bot function as a servant, or butler.
 * <p>
 * Authorization is defined in "altoclef_butler_whitelist.txt" and "altoclef_butler_blacklist.txt"
 * and depends on the "useButlerWhitelist" and "useButlerBlacklist" settings in "altoclef_settings.json"
 */
public class Butler {

    private static final String BUTLER_MESSAGE_START = "` ";

    private final AltoClef mod;

    private final WhisperChecker whisperChecker = new WhisperChecker();

    private final UserAuth userAuth;

    private String currentUser = null;

    // Utility variables for command logic
    private boolean commandInstantRan = false;
    private boolean commandFinished = false;

    public Butler(AltoClef mod) {
        this.mod = mod;
        userAuth = new UserAuth(mod);

        // Revoke our current user whenever a task finishes.
        EventBus.subscribe(TaskFinishedEvent.class, evt -> {
            if (currentUser != null) {
                currentUser = null;
            }
        });

        // Receive system events
        EventBus.subscribe(ChatMessageEvent.class, evt -> {
            return; // disable butler
            // boolean debug = ButlerConfig.getInstance().whisperFormatDebug;
            // String message = evt.messageContent();
            // String sender = evt.senderName();
            // MessageType messageType = evt.messageType();
            // String receiver = mod.getPlayer().getName().getString();
            // if (sender != null && !Objects.equals(sender, receiver) && shouldAccept(messageType)) {
            //     String wholeMessage = sender + " " + receiver + " " + message;
            //     if (debug) {
            //         Debug.logMessage("RECEIVED WHISPER: \"" + wholeMessage + "\".");
            //     }
            //     this.mod.getButler().receiveMessage(wholeMessage, receiver);
            // }
        });
    }

    private static boolean shouldAccept(MessageType messageType) {
        //#if MC >= 11904
        return messageType.chat().style().isItalic()
                && messageType.chat().style().getColor() != null
                && Objects.equals(messageType.chat().style().getColor().getName(), "gray");
        //#else
        //$$ //it doesnt look like previous versions did any type of checking
        //$$ return true;
        //#endif
    }

    private void receiveMessage(String msg, String receiver) {
        // Format: <USER> whispers to you: <MESSAGE>
        // Format: <USER> whispers: <MESSAGE>
        WhisperChecker.MessageResult result = this.whisperChecker.receiveMessage(mod, receiver, msg);
        if (result != null) {
            this.receiveWhisper(result.from, result.message);
        } else if (ButlerConfig.getInstance().whisperFormatDebug) {
            Debug.logMessage("    Not Parsing: MSG format not found.");
        }
    }

    private void receiveWhisper(String username, String message) {

        boolean debug = ButlerConfig.getInstance().whisperFormatDebug;
        // Ignore messages from other bots.
        if (message.startsWith(BUTLER_MESSAGE_START)) {
            if (debug) {
                Debug.logMessage("    Rejecting: MSG is detected to be sent from another bot.");
            }
            return;
        }

        if (userAuth.isUserAuthorized(username)) {
            executeWhisper(username, message);
        } else {
            if (debug) {
                Debug.logMessage("    Rejecting: User \"" + username + "\" is not authorized.");
            }
            if (ButlerConfig.getInstance().sendAuthorizationResponse) {
                sendWhisper(username, ButlerConfig.getInstance().failedAuthorizationResposne.replace("{from}", username), MessagePriority.UNAUTHORIZED);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isUserAuthorized(String username) {
        return userAuth.isUserAuthorized(username);
    }

    public void onLog(String message, MessagePriority priority) {
        if (currentUser != null) {
            sendWhisper(message, priority);
        }
    }

    public void onLogWarning(String message, MessagePriority priority) {
        if (currentUser != null) {
            sendWhisper("[WARNING:] " + message, priority);
        }
    }

    public void tick() {
        // Nothing for now.
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public boolean hasCurrentUser() {
        return currentUser != null;
    }

    public void executeWhisper(String username, String message) {
        String prevUser = currentUser;
        commandInstantRan = true;
        commandFinished = false;
        currentUser = username;
        sendWhisper("Command Executing: " + message, MessagePriority.TIMELY);

        String prefix = mod.getModSettings().getCommandPrefix();
        AltoClef.getCommandExecutor().execute(prefix + message, () -> {
            // On finish
            sendWhisper("Command Finished: " + message, MessagePriority.TIMELY);
            if (!commandInstantRan) {
                currentUser = null;
            }
            commandFinished = true;
        }, e -> {
            for (String msg : e.getMessage().split("\n")) {
                sendWhisper("TASK FAILED: " + msg, MessagePriority.ASAP);
            }
            e.printStackTrace();
            currentUser = null;
            commandInstantRan = false;
        });
        commandInstantRan = false;
        // Only set the current user if we're still running.
        if (commandFinished) {
            currentUser = prevUser;
        }
    }

    private void sendWhisper(String message, MessagePriority priority) {
        if (currentUser != null) {
            sendWhisper(currentUser, message, priority);
        } else {
            Debug.logWarning("Failed to send butler message as there are no users present: " + message);
        }
    }

    private void sendWhisper(String username, String message, MessagePriority priority) {
      mod.getMessageSender().enqueueWhisper(username, BUTLER_MESSAGE_START + message, priority);
    }
}
