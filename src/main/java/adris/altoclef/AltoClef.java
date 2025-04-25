package adris.altoclef;


import adris.altoclef.butler.Butler;
import adris.altoclef.chains.*;
import adris.altoclef.commands.BlockScanner;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.control.InputControls;
import adris.altoclef.control.PlayerExtraController;
import adris.altoclef.control.SlotHandler;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.*;
import adris.altoclef.multiversion.DrawContextWrapper;
import adris.altoclef.multiversion.RenderLayerVer;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.player2api.AICommandBridge;
import adris.altoclef.player2api.Character;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.trackers.*;
import adris.altoclef.trackers.storage.ContainerSubTracker;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.ui.AltoClefTickChart;
import adris.altoclef.ui.CommandStatusOverlay;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.ui.MessageSender;
import adris.altoclef.ui.ChatclefToggleButton;
import adris.altoclef.util.helpers.InputHelper;
import baritone.Baritone;
import baritone.altoclef.AltoClefSettings;
import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

// import adris.altoclef.ui.ChatclefToggleButton;

import java.util.*;
import java.util.function.Consumer;

/**
 * Central access point for AltoClef
 */
public class AltoClef implements ModInitializer {

    // Static access to altoclef
    private static final Queue<Consumer<AltoClef>> _postInitQueue = new ArrayDeque<>();

    // Central Managers
    private static CommandExecutor commandExecutor;
    private TaskRunner taskRunner;
    private TrackerManager trackerManager;
    private BotBehaviour botBehaviour;
    private PlayerExtraController extraController;
    // Task chains
    private UserTaskChain userTaskChain;
    private FoodChain foodChain;
    private MobDefenseChain mobDefenseChain;
    private MLGBucketFallChain mlgBucketChain;
    // Trackers
    private ItemStorageTracker storageTracker;
    private ContainerSubTracker containerSubTracker;
    private EntityTracker entityTracker;
    private BlockScanner blockScanner;
    private SimpleChunkTracker chunkTracker;
    private MiscBlockTracker miscBlockTracker;
    private CraftingRecipeTracker craftingRecipeTracker;
    // Renderers
    private CommandStatusOverlay commandStatusOverlay;
    private AltoClefTickChart altoClefTickChart;
    // Settings
    private adris.altoclef.Settings settings;
    // Misc managers/input
    private MessageSender messageSender;
    private InputControls inputControls;
    private SlotHandler slotHandler;
    // Butler
    private Butler butler;
    // Pausing
    private boolean paused = false;
    private Task storedTask;

    // AI Command & API
    private AICommandBridge aiBridge;
    private long lastHeartbeatTime = System.nanoTime();

    private static AltoClef instance;



    private boolean inGame = false;

    // Are we in game (playing in a server/world)
    public static boolean inGame() {
        return MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().getNetworkHandler() != null;
    }

    /**
     * Executes commands (ex. `@get`/`@gamer`)
     */
    public static CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // As such, nothing will be loaded here but basic initialization.
        EventBus.subscribe( TitleScreenEntryEvent.class, evt -> onInitializeLoad());
        if (instance != null) {
            throw new IllegalStateException("AltoClef already loaded!");
        }
        instance = this;
//        EventBus.subscribe(ClientLoginEvent.class, evt -> {
//           System.out.println("LOGGED IN");
//        });

    }


    public void onInitializeLoad() {
        // This code should be run after Minecraft loads everything else in.
        // This is the actual start point, controlled by a mixin.

        initializeBaritoneSettings();

        // Central Managers
        commandExecutor = new CommandExecutor(this);
        taskRunner = new TaskRunner(this);
        trackerManager = new TrackerManager(this);
        botBehaviour = new BotBehaviour(this);
        extraController = new PlayerExtraController(this);

        // Task chains
        userTaskChain = new UserTaskChain(taskRunner);
        mobDefenseChain = new MobDefenseChain(taskRunner);
        new DeathMenuChain(taskRunner);
        new PlayerInteractionFixChain(taskRunner);
        mlgBucketChain = new MLGBucketFallChain(taskRunner);
        new UnstuckChain(taskRunner);
        new PreEquipItemChain(taskRunner);
        new WorldSurvivalChain(taskRunner);
        foodChain = new FoodChain(taskRunner);

        // Trackers
        storageTracker = new ItemStorageTracker(this, trackerManager, container -> containerSubTracker = container);
        entityTracker = new EntityTracker(trackerManager);
        blockScanner = new BlockScanner(this);
        chunkTracker = new SimpleChunkTracker(this);
        miscBlockTracker = new MiscBlockTracker(this);
        craftingRecipeTracker = new CraftingRecipeTracker(trackerManager);

        // Renderers
        commandStatusOverlay = new CommandStatusOverlay();
        altoClefTickChart = new AltoClefTickChart(MinecraftClient.getInstance().textRenderer);

        // Misc managers
        messageSender = new MessageSender();
        inputControls = new InputControls();
        slotHandler = new SlotHandler(this);

        butler = new Butler(this);
        aiBridge = new AICommandBridge(commandExecutor, this);

        initializeCommands();

        // Load settings
        adris.altoclef.Settings.load(newSettings -> {
            settings = newSettings;
            // Baritone's `acceptableThrowawayItems` should match our own.
            List<Item> baritoneCanPlace = Arrays.stream(settings.getThrowawayItems(true))
                    .filter(item -> item != Items.SOUL_SAND && item != Items.MAGMA_BLOCK && item != Items.SAND && item
                            != Items.GRAVEL).toList();
            getClientBaritoneSettings().acceptableThrowawayItems.value.addAll(baritoneCanPlace);
            // If we should run an idle command...
            if ((!getUserTaskChain().isActive() || getUserTaskChain().isRunningIdleTask()) && getModSettings().shouldRunIdleCommandWhenNotActive()) {
                getUserTaskChain().signalNextTaskToBeIdleTask();
                getCommandExecutor().executeWithPrefix(getModSettings().getIdleCommand());
            }
            // Don't break blocks or place blocks where we are explicitly protected.
            getExtraBaritoneSettings().avoidBlockBreak(blockPos -> settings.isPositionExplicitlyProtected(blockPos));
            getExtraBaritoneSettings().avoidBlockPlace(blockPos -> settings.isPositionExplicitlyProtected(blockPos));
        });

        // Receive + cancel chat
        EventBus.subscribe(SendChatEvent.class, evt -> {
            String line = evt.message;
            if (getCommandExecutor().isClientCommand(line)) {
                evt.cancel();
                getCommandExecutor().execute(line);
            }
            else if (this.aiBridge.getEnabled()) {
                evt.cancel();
                Debug.logUserMessage(line);
                this.aiBridge.addMessageToQueue("User: "+line);
            }
        });

        // Tick with the client
        EventBus.subscribe(ClientTickEvent.class, evt -> {
            long nanos = System.nanoTime();
            onClientTick();
            altoClefTickChart.pushTickNanos(System.nanoTime()-nanos);
        });

        // Render
        EventBus.subscribe(ClientRenderEvent.class, evt -> onClientRenderOverlay(evt.context));

        // Playground
        Playground.IDLE_TEST_INIT_FUNCTION(this);

        // External mod initialization
        runEnqueuedPostInits();
    }

    public void stop() {
        getUserTaskChain().cancel(this);
        if (taskRunner.getCurrentTaskChain() != null) {
            taskRunner.getCurrentTaskChain().stop();
        }
        // also disable idle, but we can re-enable it as soon as any task runs
        getTaskRunner().disable();
        // Extra reset. Sometimes baritone is laggy and doesn't properly reset our press
        getClientBaritone().getPathingBehavior().forceCancel();
        getClientBaritone().getInputOverrideHandler().clearAllKeys();
    }

    // Client tick

    private void onClientTick() {
        runEnqueuedPostInits();

        inputControls.onTickPre();

        // Cancel shortcut
        if (InputHelper.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && InputHelper.isKeyPressed(GLFW.GLFW_KEY_K)) {
            stop();
        }
        
        // Call heartbeat every 60 seconds
        long now = System.nanoTime();
        if (now - lastHeartbeatTime > 60_000_000_000L) {
            aiBridge.sendHeartbeat();
            lastHeartbeatTime = now;
        }

        if(aiBridge.getEnabled() && inGame && AltoClef.inGame()){
            aiBridge.onTick();
        }

        // TODO: should this go here?
        storageTracker.setDirty();
        containerSubTracker.onServerTick();
        miscBlockTracker.tick();
        trackerManager.tick();
        blockScanner.tick();
        taskRunner.tick();

        messageSender.tick();

        inputControls.onTickPost();
        if(!inGame && AltoClef.inGame() ){
            inGame = true;
            onLogin();
        }

        // Chatclef UI
        if (ChatclefToggleButton.tick()) {
            setChatClefEnabled(!getAiBridge().getEnabled());
        }
    }

    /// GETTERS AND SETTERS

    public AICommandBridge getAiBridge(){
        return this.aiBridge;
    }

    public void setChatClefEnabled(boolean enabled) {
        getAiBridge().setEnabled(enabled);
        if (!enabled) {
            // actually stop tasks
            getUserTaskChain().cancel(this);
            // also disable idle, but we can re-enable it as soon as any task runs
            getTaskRunner().disable();
        }
    }


    private void onClientRenderOverlay(DrawContextWrapper context) {
        context.setRenderLayer(RenderLayerVer.getGuiOverlay());
        if (settings.shouldShowTaskChain()) {
            commandStatusOverlay.render(this, context);
        }

        if (settings.shouldShowDebugTickMs()) {
            altoClefTickChart.render(this, context, 1, context.getScaledWindowWidth() / 2 - 124);
        }

        ChatclefToggleButton.render(context, context.getMatrices(), getAiBridge().getEnabled());
    }
    private void onLogin() {
        // Sends greeting
        this.aiBridge.sendGreeting();
    }
    private void initializeBaritoneSettings() {
        getExtraBaritoneSettings().canWalkOnEndPortal(false);
        getClientBaritoneSettings().freeLook.value = false;
        getClientBaritoneSettings().overshootTraverse.value = false;
        getClientBaritoneSettings().allowOvershootDiagonalDescend.value = true;
        getClientBaritoneSettings().allowInventory.value = true;
        getClientBaritoneSettings().allowParkour.value = false;
        getClientBaritoneSettings().allowParkourAscend.value = false;
        getClientBaritoneSettings().allowParkourPlace.value = false;
        getClientBaritoneSettings().allowDiagonalDescend.value = false;
        getClientBaritoneSettings().allowDiagonalAscend.value = false;
        getClientBaritoneSettings().blocksToAvoid.value = new LinkedList<>(List.of(Blocks.FLOWERING_AZALEA, Blocks.AZALEA,
                Blocks.POWDER_SNOW, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.CAVE_VINES,
                Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.SWEET_BERRY_BUSH,
                Blocks.WARPED_ROOTS, Blocks.VINE, Blocks.SHORT_GRASS, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
                Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD,
                Blocks.AMETHYST_CLUSTER, Blocks.SCULK, Blocks.SCULK_VEIN));

        // dont try to break nether portal block
        getClientBaritoneSettings().blocksToAvoidBreaking.value.add(Blocks.NETHER_PORTAL);
        getClientBaritoneSettings().blocksToDisallowBreaking.value.add(Blocks.NETHER_PORTAL);

        // Let baritone move items to hotbar to use them
        // Reduces a bit of far rendering to save FPS
        getClientBaritoneSettings().fadePath.value = true;
        // Don't let baritone scan dropped items, we handle that ourselves.
        getClientBaritoneSettings().mineScanDroppedItems.value = false;
        // Don't let baritone wait for drops, we handle that ourselves.
        getClientBaritoneSettings().mineDropLoiterDurationMSThanksLouca.value = 0L;

        // Water bucket placement will be handled by us exclusively
        getExtraBaritoneSettings().configurePlaceBucketButDontFall(true);

        // For render smoothing
        getClientBaritoneSettings().randomLooking.value = 0.0;
        getClientBaritoneSettings().randomLooking113.value = 0.0;

        // Give baritone more time to calculate paths. Sometimes they can be really far away.
        // Was: 2000L
        getClientBaritoneSettings().failureTimeoutMS.reset();
        // Was: 5000L
        getClientBaritoneSettings().planAheadFailureTimeoutMS.reset();
        // Was 100
        getClientBaritoneSettings().movementTimeoutTicks.reset();
    }

    // List all command sources here.
    private void initializeCommands() {
        try {
            // This creates the commands. If you want any more commands feel free to initialize new command lists.
            AltoClefCommands.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO refactor codebase to use this instead of passing an argument around
    /**
     * @return the instance of this class or null if it has not been initialized yet
     */
    public static AltoClef getInstance() {
        return instance;
    }

    /**
     * Runs the highest priority task chain
     * (task chains run the task tree)
     */
    public TaskRunner getTaskRunner() {
        return taskRunner;
    }

    /**
     * The user task chain (runs your command. Ex. Get Diamonds, Beat the Game)
     */
    public UserTaskChain getUserTaskChain() {
        return userTaskChain;
    }

    /**
     * Controls bot behaviours, like whether to temporarily "protect" certain blocks or items
     */
    public BotBehaviour getBehaviour() {
        return botBehaviour;
    }

    /**
     * Controls tasks, for pausing and unpausing the bot
     */
    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean pausing) {
        this.paused = pausing;
    }

    /**
     * storages the task you where doing before pausing.
     */
    public void setStoredTask(Task currentTask) {
        this.storedTask = currentTask;
    }

    /**
     * Gets the task you where doing before pausing.
     */
    public Task getStoredTask() {
        return storedTask;
    }

    /**
     * Tracks items in your inventory and in storage containers.
     */
    public ItemStorageTracker getItemStorage() {
        return storageTracker;
    }

    /**
     * Tracks loaded entities
     */
    public EntityTracker getEntityTracker() {
        return entityTracker;
    }

    /**
     * Manages a list of all available recipes
     */
    public CraftingRecipeTracker getCraftingRecipeTracker() {
        return craftingRecipeTracker;
    }

    /**
     * Tracks blocks and their positions - better version of BlockTracker
     */
    public BlockScanner getBlockScanner() {
        return blockScanner;
    }

    /**
     * Tracks of whether a chunk is loaded/visible or not
     */
    public SimpleChunkTracker getChunkTracker() {
        return chunkTracker;
    }

    /**
     * Tracks random block things, like the last nether portal we used
     */
    public MiscBlockTracker getMiscBlockTracker() {
        return miscBlockTracker;
    }

    /**
     * Baritone access (could just be static honestly)
     */
    public Baritone getClientBaritone() {
        if (getPlayer() == null) {
            return (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
        }
        return (Baritone) BaritoneAPI.getProvider().getBaritoneForPlayer(getPlayer());
    }

    /**
     * Baritone settings access (could just be static honestly)
     */
    public Settings getClientBaritoneSettings() {
        return Baritone.settings();
    }

    /**
     * Baritone settings special to AltoClef (could just be static honestly)
     */
    public AltoClefSettings getExtraBaritoneSettings() {
        return AltoClefSettings.getInstance();
    }

    /**
     * AltoClef Settings
     */
    public adris.altoclef.Settings getModSettings() {
        return settings;
    }

    /**
     * Butler controller. Keeps track of users and lets you receive user messages
     */
    public Butler getButler() {
        return butler;
    }

    /**
     * Sends chat messages (avoids auto-kicking)
     */
    public MessageSender getMessageSender() {
        return messageSender;
    }

    /**
     * Does Inventory/container slot actions
     */
    public SlotHandler getSlotHandler() {
        return slotHandler;
    }

    /**
     * Minecraft player client access (could just be static honestly)
     */
    public ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    /**
     * Minecraft world access (could just be static honestly)
     */
    public ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }

    /**
     * Minecraft client interaction controller access (could just be static honestly)
     */
    public ClientPlayerInteractionManager getController() {
        return MinecraftClient.getInstance().interactionManager;
    }

    /**
     * Extra controls not present in ClientPlayerInteractionManager. This REALLY should be made static or combined with something else.
     */
    public PlayerExtraController getControllerExtras() {
        return extraController;
    }

    /**
     * Manual control over input actions (ex. jumping, attacking)
     */
    public InputControls getInputControls() {
        return inputControls;
    }

    /**
     * Run a user task
     */
    public void runUserTask(Task task) {
        runUserTask(task, () -> {
        });
    }

    /**
     * Run a user task
     */
    public void runUserTask(Task task, Runnable onFinish) {
        userTaskChain.runTask(this, task, onFinish);
    }

    /**
     * Cancel currently running user task
     */
    public void cancelUserTask() {
        userTaskChain.cancel(this);
    }

    /**
     * Takes control away to eat food
     */
    public FoodChain getFoodChain() {
        return foodChain;
    }

    /**
     * Takes control away to defend against mobs
     */
    public MobDefenseChain getMobDefenseChain() {
        return mobDefenseChain;
    }

    /**
     * Takes control away to perform bucket saves
     */
    public MLGBucketFallChain getMLGBucketChain() {
        return mlgBucketChain;
    }

    public void log(String message) {
        log(message, MessagePriority.TIMELY);
    }

    /**
     * Logs to the console and also messages any player using the bot as a butler.
     */
    public void log(String message, MessagePriority priority) {
        Debug.logMessage(message);
    }


    public void logCharacterMessage(String message, Character character){
        Debug.logCharacterMessage(message, character);
    }

    public void logWarning(String message) {
        logWarning(message, MessagePriority.TIMELY);
    }

    /**
     * Logs a warning to the console and also alerts any player using the bot as a butler.
     */
    public void logWarning(String message, MessagePriority priority) {
        Debug.logWarning(message);
    }

    private void runEnqueuedPostInits() {
        synchronized (_postInitQueue) {
            while (!_postInitQueue.isEmpty()) {
                _postInitQueue.poll().accept(this);
            }
        }
    }

}
