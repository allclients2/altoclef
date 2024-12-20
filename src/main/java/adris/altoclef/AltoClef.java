package adris.altoclef;


import adris.altoclef.butler.Butler;
import adris.altoclef.chains.*;
import adris.altoclef.control.LookAtPos;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.control.InputControls;
import adris.altoclef.control.PlayerExtraController;
import adris.altoclef.control.SlotHandler;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import adris.altoclef.eventbus.events.ClientTickEvent;
import adris.altoclef.eventbus.events.SendChatEvent;
import adris.altoclef.eventbus.events.TitleScreenEntryEvent;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.trackers.*;
import adris.altoclef.trackers.storage.ContainerSubTracker;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.ui.CommandStatusOverlay;
import adris.altoclef.ui.MessageSender;
import adris.altoclef.scanner.BlockScanner;
import adris.altoclef.scanner.DangerousBlockScanner;
import adris.altoclef.ui.AltoClefTickChart;
import adris.altoclef.util.helpers.InputHelper;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.Baritone;
import baritone.altoclef.AltoClefSettings;
import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

//#if MC <= 12001
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import adris.altoclef.util.WindowUtil;
//#endif

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
    private DangerousBlockScanner dangerousBlockScanner;
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

    private Butler butler;
    private SlotHandler slotHandler;

    //TODO refactor this later
    public static AltoClef INSTANCE;

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
        EventBus.subscribe(TitleScreenEntryEvent.class, evt -> onInitializeLoad());
        INSTANCE = this;
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
        mlgBucketChain = new MLGBucketFallChain(taskRunner);
        foodChain = new FoodChain(taskRunner);
        new DeathMenuChain(taskRunner);
        new PlayerInteractionFixChain(taskRunner);
        new UnstuckChain(taskRunner);
        new PreEquipItemChain(taskRunner);
        new WorldSurvivalChain(taskRunner);

        // Trackers
        storageTracker = new ItemStorageTracker(this, trackerManager, container -> containerSubTracker = container);
        entityTracker = new EntityTracker(trackerManager);
        blockScanner = new BlockScanner(this);
        dangerousBlockScanner = new DangerousBlockScanner(this);
        chunkTracker = new SimpleChunkTracker(this);
        miscBlockTracker = new MiscBlockTracker(this);
        craftingRecipeTracker = new CraftingRecipeTracker(trackerManager);

        // Misc managers
        messageSender = new MessageSender();
        inputControls = new InputControls();
        butler = new Butler(this);
        slotHandler = new SlotHandler(this);

        initializeCommands();
        TaskCatalogue.InitRecipes();

        // Load settings
        adris.altoclef.Settings.load(newSettings -> {
            settings = newSettings;
            // Baritone's `acceptableThrowawayItems` should match our own.
            List<Item> baritoneCanPlace = Arrays.stream(settings.getThrowawayItems(this, true))
                    .filter(item -> item instanceof BlockItem && item != Items.WHEAT_SEEDS).toList();
            Debug.logMessage("Garbage: " + baritoneCanPlace);
            getClientBaritoneSettings().acceptableThrowawayItems.value.addAll(baritoneCanPlace);
            // If we should run an idle command...
            if ((!getUserTaskChain().isActive() || getUserTaskChain().isRunningIdleTask()) && getModSettings().shouldRunIdleCommandWhenNotActive()) {
                getUserTaskChain().signalNextTaskToBeIdleTask();
                getCommandExecutor().executeWithPrefix(getModSettings().getIdleCommand());
            }

            // Don't break blocks or place blocks where we are explicitly protected.
            // Oversight (applies to both method calls below): May be checking a different dimension, but this just assumes the current dimension.
            getExtraBaritoneSettings().avoidBlockBreak(blockPos -> settings.isBlockPosBlacklisted(WorldHelper.getCurrentDimension(), WorldHelper.getNetworkName(), blockPos));
            getExtraBaritoneSettings().avoidBlockPlace(blockPos -> settings.isBlockPosBlacklisted(WorldHelper.getCurrentDimension(), WorldHelper.getNetworkName(), blockPos));
        });

        // Receive + cancel chat
        EventBus.subscribe(SendChatEvent.class, evt -> {
            String line = evt.message;
            if (getCommandExecutor().isClientCommand(line)) {
                evt.cancel();
                getCommandExecutor().execute(line);
            }
        });

        // Debug jank/hookup
        Debug.jankModInstance = this;

        // Tick with the client
        EventBus.subscribe(ClientTickEvent.class, evt -> {
            long nanos = System.nanoTime();
            onClientTick();
            altoClefTickChart.pushTickNanos(System.nanoTime()-nanos);
        });

        // Renderers (require settings initialized)
        commandStatusOverlay = new CommandStatusOverlay(this);
        altoClefTickChart = new AltoClefTickChart();

        // Render
        //#if MC>=12001
        EventBus.subscribe(ClientRenderEvent.class, evt -> onClientRenderOverlay(evt.context));
        //#else
        //$$ EventBus.subscribe(ClientRenderEvent.class, evt -> onClientRenderOverlay(evt.context, evt.matrices));
        //#endif

        // Playground
        Playground.IDLE_TEST_INIT_FUNCTION(this);

        // External mod initialization
        runEnqueuedPostInits();
    }

    // Client tick
    private void onClientTick() {
        runEnqueuedPostInits();

        inputControls.onTickPre();

        // Cancel shortcut
        if (InputHelper.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && InputHelper.isKeyPressed(GLFW.GLFW_KEY_K)) {
            userTaskChain.cancel(this);
            if (taskRunner.getCurrentTaskChain() != null) {
                taskRunner.getCurrentTaskChain().stop(this);
            }
        }

        // TODO: should probably move this
        LookAtPos.updateFreeLook(this);

        // TODO: should this go here?
        storageTracker.setDirty();
        containerSubTracker.onServerTick();
        miscBlockTracker.tick();
        trackerManager.tick();
        blockScanner.tick();
        dangerousBlockScanner.tick();
        taskRunner.tick();

        messageSender.tick();

        inputControls.onTickPost();
        butler.tick();
    }

    /// GETTERS AND SETTERS

    //#if MC>=12001
    private void onClientRenderOverlay(DrawContext context) {
        if (settings.shouldShowTaskChain()) {
            commandStatusOverlay.render(this, context.getMatrices());
        }

        if (settings.shouldShowDebugTickMs()) {
            altoClefTickChart.render(this, context, 1, context.getScaledWindowWidth() / 2 - 124);
        }
    }
    //#else
    //$$ private void onClientRenderOverlay(DrawableHelper helper, MatrixStack matrices) {
    //$$     if (settings.shouldShowTaskChain()) {
    //$$         commandStatusOverlay.render(this, matrices);
    //$$     }
    //$$
    //$$     if (settings.shouldShowDebugTickMs()) {
    //$$         altoClefTickChart.render(this, helper, matrices, 1, WindowUtil.getScaledWindowWidth() / 2 - 124);
    //$$     }
    //$$ }
    //#endif

    private void initializeBaritoneSettings() {
        getExtraBaritoneSettings().canWalkOnEndPortal(false);

        var baritoneClientSettings = getClientBaritoneSettings();
        baritoneClientSettings.freeLook.value = false;
        baritoneClientSettings.overshootTraverse.value = false;
        baritoneClientSettings.allowOvershootDiagonalDescend.value = false;

        baritoneClientSettings.allowInventory.value = true;
        baritoneClientSettings.allowParkour.value = false;

        baritoneClientSettings.allowParkourAscend.value = true;
        baritoneClientSettings.allowParkourPlace.value = false;
        baritoneClientSettings.allowDiagonalDescend.value = false;
        baritoneClientSettings.allowDiagonalAscend.value = false;
        baritoneClientSettings.walkOnWaterOnePenalty.value = 32.0;

        baritoneClientSettings.blocksToAvoid.value = new LinkedList<>(List.of(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.SWEET_BERRY_BUSH,
                Blocks.WARPED_ROOTS, Blocks.VINE, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
                Blocks.ROSE_BUSH, Blocks.PEONY

                //#if MC>=11800
                , Blocks.FLOWERING_AZALEA, Blocks.AZALEA,
                Blocks.POWDER_SNOW, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.CAVE_VINES,
                Blocks.CAVE_VINES_PLANT, Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD,
                Blocks.AMETHYST_CLUSTER
                //#endif

                //#if MC>=11900
                , Blocks.SCULK, Blocks.SCULK_VEIN
                //#endif
        ));

        // Let baritone move items to hotbar to use them

        // Don't let baritone scan dropped items, we handle that ourselves.
        baritoneClientSettings.mineScanDroppedItems.value = false;

        // Don't let baritone wait for drops, we handle that ourselves.
        baritoneClientSettings.mineDropLoiterDurationMSThanksLouca.value = 0L;

        // Water bucket placement will be handled by us exclusively
        getExtraBaritoneSettings().configurePlaceBucketButDontFall(true);

        baritoneClientSettings.failureTimeoutMS.value = 500L;

        baritoneClientSettings.mobAvoidanceRadius.reset();
        baritoneClientSettings.mobAvoidanceCoefficient.value = 7.5;
        baritoneClientSettings.avoidance.value = true;


        // For smooth camera turning, baritone 1.20.1 does not have smooth look so we use random look instead.
        //#if MC>12001
        baritoneClientSettings.smoothLook.value = true;
        baritoneClientSettings.smoothLookTicks.value = 4;
        //#else
        //$$ baritoneClientSettings.randomLooking.value = 0.2; //randomly look to convince
        //$$ baritoneClientSettings.randomLooking113.value = 0.03;
        //#endif

        // Give baritone more time to calculate paths. Sometimes they can be really far away.
        baritoneClientSettings.failureTimeoutMS.reset();
        baritoneClientSettings.planAheadFailureTimeoutMS.reset();
        baritoneClientSettings.movementTimeoutTicks.reset();
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

    // Check if its night in-game
    public boolean IsNight() {
        int time = (int) getWorld().getTimeOfDay() % 24000;
        return !(0 <= time && time < 13000);
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
     * Butler stuff
     */
    public Butler getButler() {
        return butler;
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

    private void runEnqueuedPostInits() {
        synchronized (_postInitQueue) {
            while (!_postInitQueue.isEmpty()) {
                _postInitQueue.poll().accept(this);
            }
        }
    }

}
