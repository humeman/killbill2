package today.tecktip.killbill.frontend.screens.game;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.data.TileCoordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.messages.IncomingMessage;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.KillBillGame.Platform;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.config.Keybinds.KeyType;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.game.items.ItemGenerator;
import today.tecktip.killbill.frontend.game.objects.Chest;
import today.tecktip.killbill.frontend.game.objects.DroppedItem;
import today.tecktip.killbill.frontend.game.objects.DummyEntity;
import today.tecktip.killbill.frontend.game.objects.DummyPlayer;
import today.tecktip.killbill.common.maploader.KillBillMap;
import today.tecktip.killbill.common.maploader.MapDirective.DirectiveType;
import today.tecktip.killbill.common.maploader.MapLoader;
import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.common.maploader.directives.ChestDirective;
import today.tecktip.killbill.common.maploader.directives.ObjectDirective;
import today.tecktip.killbill.common.maploader.directives.RoomDirective;
import today.tecktip.killbill.common.maploader.directives.RoomDirective.WallOverride;
import today.tecktip.killbill.common.maploader.directives.TileDirective;
import today.tecktip.killbill.frontend.game.objects.Player;
import today.tecktip.killbill.frontend.game.objects.TextureObject;
import today.tecktip.killbill.frontend.game.objects.Tile;
import today.tecktip.killbill.frontend.game.objects.explosives.Bomb;
import today.tecktip.killbill.frontend.game.objects.explosives.Projectile;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.LocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalEntityState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameUserState;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalGameState.BasicGameRunState;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicBombCommand.BasicBombCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicInteractCommand.BasicInteractCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicProjectileCommand.BasicProjectileCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicSendChatCommand.BasicRecvChatCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicSendChatCommand.BasicRecvSystemMessageCommandData;
import today.tecktip.killbill.frontend.screens.GameScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.ScaledSize;
import today.tecktip.killbill.frontend.ui.Size.XScaledSize;
import today.tecktip.killbill.frontend.ui.UiElement;
import today.tecktip.killbill.frontend.ui.elements.Button;
import today.tecktip.killbill.frontend.ui.elements.Image;
import today.tecktip.killbill.frontend.ui.elements.Label;
import today.tecktip.killbill.frontend.ui.renderers.ChatDisplay;
import today.tecktip.killbill.frontend.ui.renderers.ChatDisplay.PlayerChatMessage;
import today.tecktip.killbill.frontend.ui.renderers.ChatDisplay.SystemChatMessage;
import today.tecktip.killbill.frontend.ui.renderers.EffectDisplay;
import today.tecktip.killbill.frontend.ui.renderers.HealthDisplay;
import today.tecktip.killbill.frontend.ui.renderers.InventoryDisplay;

/**
 * UDP-connected game screen.
 * @author cs
 */
public class UdpGameScreen extends GameScreen {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpGameScreen.class);

    /**
     * True if the {@link #init} method has been called.
     */
    private boolean initialized;

    /**
     * The user that's playing on this instance.
     */
    private BasicLocalGameUserState userState;

    /**
     * The player instance of the user that's playing.
     */
    private Player player;

    /**
     * The game state that the game is connected to.
     */
    private BasicLocalGameState gameState;

    /**
     * Other players in the game.
     */
    private List<DummyPlayer> players;

    /**
     * Entities in the game.
     */
    private List<DummyEntity> entities;

    /**
     * Chat display
     */
    private ChatDisplay chatDisplay;

    private HealthDisplay healthDisplay;

    private InventoryDisplay inventoryDisplay;

    private EffectDisplay effectDisplay;

    private boolean readySkip;

    private boolean paused;

    private boolean dead;

    private float dieTimer;

    private List<UiElement> pauseElements;

    private List<UiElement> dieElements;

    private Image dieBg;
    private Button spectateButton;

    private boolean leaving;

    /**
     * Constructs an uninitialized UDP game screen.
     */
    public UdpGameScreen() {
        super(new Color(1, 1, 1, 1));
        initialized = false;
        healthDisplay = null;
        inventoryDisplay = null;
        effectDisplay = null;
        readySkip = false;
        paused = false;
        dead = false;
        pauseElements = new ArrayList<>();
        dieElements = new ArrayList<>();
        leaving = false;
    }

    /**
     * Initializes the game screen.
     * @param userState User playing right now
     * @param gameState Active game state
     */
    public void init(final BasicLocalGameUserState userState, final BasicLocalGameState gameState) {
        this.userState = userState;
        this.gameState = gameState;
        players = new ArrayList<>();
        entities = new ArrayList<>();
        initialized = true;
        leaving = false;
        paused = false;
        dead = false;
    }

    public boolean isPaused() {
        return paused || dead;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        chatDisplay = new ChatDisplay(
            true, 
            new ScaledLocation(0.1f, 0.45f),
            new ScaledSize(0.8f, 0.1f),
            48
        );
        getUiRenderer().add(chatDisplay);

        debugDisplay.addGenerator(
            (delta) -> {
                if (KillBillGame.get().getUdpClient() != null) {
                    return KillBillGame.get().getUdpClient().getDebugString();
                }
                return "No UDP client.";
            }
        );

        setInputProcessor(
            new InputAdapter() {
                @Override
                public boolean scrolled(final float x, final float y) {
                    if (y < 0) {
                        int newIndex = player.getHeldItemIndex() - 1;
                        if (newIndex < 0) newIndex = GlobalGameConfig.INVENTORY_SIZE - 1;

                        player.setHeldItem(newIndex);
                        if (player.getHeldItem() != null)
                            player.getHeldItem().resetCooldown();
                        return true;
                    }
                    else if (y > 0) {
                        player.setHeldItem((player.getHeldItemIndex() + 1) % GlobalGameConfig.INVENTORY_SIZE);
                        if (player.getHeldItem() != null)
                            player.getHeldItem().resetCooldown();
                        return true;
                    }

                    return false;
                }
            }
        );

        pauseElements.clear();
        pauseElements.add(
            Image.newBuilder()
                .setLocation(new FixedLocation(0, 0))
                .setSize(new ScaledSize(1, 1))
                .setTint(new Color(.1f, .1f, .4f, .5f))
                .setTexture("ui_square")
                .build());
        pauseElements.add(
            Label.newBuilder()
                .setLocation(new ScaledLocation(0.5f, 0.75f))
                .setCentered(true)
                .setText("menu")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setShadow(Color.BLACK, 4, 4)
                    .setScaledSize(72)
                )
                .build());
        pauseElements.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.35f, 0.45f))
                .setSize(new XScaledSize(0.3f, 0.33f))
                .setTexture("ui_button")
                .setText("resume")
                .setOnPress(this::unpause)
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(56)
                )
                .build());
        pauseElements.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.35f, 0.2f))
                .setSize(new XScaledSize(0.3f, 0.33f))
                .setTexture("ui_button")
                .setText("leave")
                .setOnPress(this::leave)
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(56)
                )
                .build());
        pauseElements.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.05f, .05f))
                .setSize(new XScaledSize(0.075f, 1))
                .setTexture("ui_mini_button_debug")
                .setOnPress(
                    () -> {
                        KillBillGame.get().setDebugEnabled(!KillBillGame.get().isDebugEnabled());
                    }
                )
                .build());

        for (final UiElement e : pauseElements) {
            e.setVisible(false);
            uiRenderer.add(e);
        }

        dieElements.clear();
        dieBg = Image.newBuilder()
            .setLocation(new FixedLocation(0, 0))
            .setSize(new ScaledSize(1, 1))
            .setTint(new Color(.6f, .1f, .1f, 0f))
            .setTexture("ui_square")
            .build();
        dieElements.add(dieBg);
        dieElements.add(
            Label.newBuilder()
                .setLocation(new ScaledLocation(0.5f, 0.75f))
                .setCentered(true)
                .setText("you died")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setShadow(Color.BLACK, 4, 4)
                    .setScaledSize(72)
                )
                .build());
        spectateButton = Button.newBuilder()
            .setLocation(new ScaledLocation(0.35f, 0.45f))
            .setSize(new XScaledSize(0.3f, 0.33f))
            .setTexture("ui_button")
            .setText("spectate")
            .setOnPress(this::spectate)
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                .setScaledSize(56)
            )
            .build();
        spectateButton.setVisible(false);
        dieElements.add(spectateButton);
        dieElements.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.35f, 0.2f))
                .setSize(new XScaledSize(0.3f, 0.33f))
                .setTexture("ui_button")
                .setText("leave")
                .setOnPress(this::leave)
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(56)
                )
                .build());

        for (final UiElement e : dieElements) {
            e.setVisible(false);
            uiRenderer.add(e);
        }
    }

    public void die() {
        if (dead) return;

        for (final UiElement e : dieElements) {
            e.setVisible(true);
        }
        dead = true;
        dieTimer = 0f;

        if (KillBillGame.get().getPlatform().equals(Platform.ANDROID)) {
            touchInputDisplay.setEnabled(false);
        }
    }

    public void spectate() {
        inventoryDisplay.setEnabled(false);
        healthDisplay.setEnabled(false);
        effectDisplay.setEnabled(false);
        player.setSpectator();
        for (final UiElement e : dieElements) {
            e.setVisible(false);
        }
        dead = false;

        if (KillBillGame.get().getPlatform().equals(Platform.ANDROID)) {
            touchInputDisplay.setEnabled(true);
        }
    }

    public void pause() {
        for (final UiElement e : pauseElements) {
            e.setVisible(true);
        }
        paused = true;

        if (KillBillGame.get().getPlatform().equals(Platform.ANDROID)) {
            touchInputDisplay.setEnabled(false);
        }
    }

    public void unpause() {
        for (final UiElement e : pauseElements) {
            e.setVisible(false);
        }
        paused = false;

        if (KillBillGame.get().getPlatform().equals(Platform.ANDROID)) {
            touchInputDisplay.setEnabled(true);
        }
    }

    public void leave() {
        leaving = true;
        try {
            KillBillGame.get().getUdpClient().disconnect();
        } catch (final IOException | InterruptedException e) {
            Gdx.app.error(getClass().getSimpleName(), "Failed to send disconnect.", e);
        }

        KillBillGame.get().changeScreen(Screens.USER_SCREEN);
    }

    @Override
    public void onSwitch() {
        player = new Player(
            0, 
            0, 
            1, 
            1, 
            userState
        );
        KillBillGame.get().setPlayer(player);

        if (healthDisplay != null) {
            uiRenderer.remove(healthDisplay);
        }

        healthDisplay = new HealthDisplay(player);
        uiRenderer.add(healthDisplay);

        if (effectDisplay != null) {
            uiRenderer.remove(effectDisplay);
        }

        effectDisplay = new EffectDisplay(player);
        uiRenderer.add(effectDisplay);

        if (inventoryDisplay != null) {
            uiRenderer.remove(inventoryDisplay);
        }
        
        inventoryDisplay = new InventoryDisplay(player);
        getUiRenderer().add(inventoryDisplay);

        loadMap();

        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_GAME_STATE, msg -> { updateGameState(); });
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.RESP_GET_GAME_STATE, msg -> { updateGameState(); });
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_ENTITY_STATE, msg -> { updateEntities(); });
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.RESP_GET_ENTITY_STATE, msg -> { updateEntities(); });
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_REMOVE_ENTITY, msg -> { updateEntities(); });
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_CHAT, this::recvPlayerChat);
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_INTERACTION, this::recvInteraction);
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE, this::recvSystemChat);
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM, this::recvAddItem);
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_REMOVE_DROPPED_ITEM, this::recvRemoveItem);
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_BOMB, this::recvBomb);
        KillBillGame.get().getUdpClient().setCallback(MessageDataType.COMMAND_RECV_PROJECTILE, this::recvProjectile);
        updateGameState();

        if (paused) unpause();

        // Request all our state now that we're ready
        try {
            KillBillGame.get().getUdpClient().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_SEND_STATE)
                .run(
                    KillBillGame.get().getUdpClient(),
                    null
                );
        } catch (final MessageFailure e) {
            throw new CatastrophicException("Failed to request game state from server.", e);
        }
        
        super.onSwitch();
    }

    @Override
    public void onSwitchOff() {
        super.onSwitchOff();
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_GAME_STATE);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.RESP_GET_GAME_STATE);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_ENTITY_STATE);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.RESP_GET_ENTITY_STATE);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_REMOVE_ENTITY);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_CHAT);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_INTERACTION);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_REMOVE_DROPPED_ITEM);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_BOMB);
        KillBillGame.get().getUdpClient().clearCallback(MessageDataType.COMMAND_RECV_PROJECTILE);
    }

    /**
     * Syncs the dummy player list and win state with the game state.
     */
    public void updateGameState() {
        readySkip = false;

        // Check if the game ended
        if (gameState.getState().equals(BasicGameRunState.ENDED)) {
            // Game over! Disconnect, then swap to the end screen
            try {
                KillBillGame.get().getUdpClient().disconnect();
            } catch (final Throwable e) {
                System.err.println(e);
            }

            Screens.GAME_END_SCREEN.init(userState.getOriginalTeam().equals(gameState.getWinningTeam()));
            KillBillGame.get().changeScreen(Screens.GAME_END_SCREEN);
            LocalGameState.destroy();
            
            return;
        }

        for (final Map.Entry<UUID, LocalGameUserState> entry : gameState.getConnectedUsers().entrySet()) {
            final BasicLocalGameUserState u = (BasicLocalGameUserState) entry.getValue();

            if (!u.isReady()) {
                readySkip = true;
                continue;
            }

            // Check if this is the active player
            if (u.equals(player.getUserState())) continue;

            boolean exists = false;
            for (final DummyPlayer dPlayer : players) {
                if (dPlayer.getUserState().equals(u)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                // Make a dummy
                final DummyPlayer dPlayer = new DummyPlayer(
                    1, 
                    1, 
                    u
                );

                // Add them in
                players.add(dPlayer);
                gameRenderer.addObject(dPlayer);
            }
        }

        List<DummyPlayer> toRemove = null;
        for (final DummyPlayer dPlayer : players) {
            if (!gameState.getConnectedUsers().containsKey(dPlayer.getUserState().getUserId())) {
                if (toRemove == null) {
                    toRemove = new ArrayList<>();
                }
                toRemove.add(dPlayer);
            }
        }

        if (toRemove != null) {
            for (final DummyPlayer dPlayer : toRemove) {
                gameRenderer.removeObject(dPlayer);
                players.remove(dPlayer);
            }
        }
    }

    public void updateEntities() {
        for (final Map.Entry<Integer, BasicLocalEntityState> entry : gameState.getEntities().entrySet()) {
            final BasicLocalEntityState entity = entry.getValue();

            boolean exists = false;
            for (final DummyEntity dEntity : entities) {
                if (dEntity.getEntityState().equals(entity)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                // Make a dummy
                final DummyEntity dEntity = DummyEntity.createFrom(entity);

                // Add them in
                entities.add(dEntity);
                gameRenderer.addObject(dEntity);
            }
        }

        List<DummyEntity> toRemove = null;
        for (final DummyEntity dEntity : entities) {
            if (!gameState.getEntities().containsKey(dEntity.getEntityState().getId())) {
                if (toRemove == null) {
                    toRemove = new ArrayList<>();
                }
                toRemove.add(dEntity);
            }
        }

        if (toRemove != null) {
            for (final DummyEntity dEntity : toRemove) {
                gameRenderer.removeObject(dEntity);
                entities.remove(dEntity);
            }
        }

    }

    public void enableChat() {
        chatDisplay.runKey(KillBillGame.get().getKeybinds().getKeys(KeyType.CHAT)[0]);
    }

    /**
     * True if the {@link #init} method has been called.
     * @return Initialization state
     */
    public boolean isInitialized() {
        return initialized;
    }

    private static TileCoordinates[] CORNER_LOCATIONS = new TileCoordinates[] {
        new TileCoordinates(0, 0),
        new TileCoordinates(0, 1),
        new TileCoordinates(1, 0),
        new TileCoordinates(1, 1)
    };
    
    private static String[] WALL_SUFFIXES = new String[] {
        "_bl",
        "_tl",
        "_br",
        "_tr",
        "_t",
        "_b",
        "_l",
        "_r"
    };


    public void loadMap() {
        if (!initialized) throw new IllegalStateException("Map is not initialized.");

        // Empty it out
        gameRenderer.getObjects().clear();

        // Grab our map
        final KillBillMap map = MapLoader.load(List.of(new ByteArrayInputStream(gameState.getGame().map().getBytes())));

        // Create rooms
        map.forEachDirectiveOfType(
            room -> {
                int lowX = room.getLocation().x();
                int lowY = room.getLocation().y();

                int highX = lowX + room.getSize().x();
                int highY = lowY + room.getSize().y();
                // Render the floors
                for (int x = lowX; x < highX; x++) {
                    for (int y = lowY; y < highY; y++) {
                        if (isExcluded(x, y, room) || isWall(x, y, room)) continue;
                        gameRenderer.addObject(
                            new TextureObject(
                                x, y, 1, 1,
                                KillBillGame.get().getTextureLoader().get(room.getFloorTexture()),
                                new ObjectFlag[]{}
                            )
                        );
                    }
                }

                for (final TileCoordinates extraFloors : room.getExtraFloors()) {
                    gameRenderer.addObject(
                        new TextureObject(
                            extraFloors.x(), extraFloors.y(), 1, 1,
                            KillBillGame.get().getTextureLoader().get(room.getFloorTexture()),
                            new ObjectFlag[]{}
                        )
                    );
                }

                // Render the walls
                // Corners
                int suffixI = 0;
                for (final TileCoordinates mult : CORNER_LOCATIONS) {
                    int x = lowX + (room.getSize().x() - 1) * mult.x();
                    int y = lowY + (room.getSize().y() - 1) * mult.y();
                    if (!isExcluded(x, y, room)) {
                        gameRenderer.addObject(
                            new Tile(
                                x, y, 1, 1,
                                getWallOverride(
                                    x, y,
                                    KillBillGame.get().getTextureLoader().get(room.getWallTexture() + WALL_SUFFIXES[suffixI]),
                                    room
                                ),
                                new ObjectFlag[] {ObjectFlag.SOLID}
                            )
                        );
                    }
                    suffixI++;
                }

                // Now top, bottom lines
                for (int x = lowX + 1; x < highX - 1; x++) {
                    if (!isExcluded(x, lowY, room)) {
                        gameRenderer.addObject(
                            new Tile(
                                x, lowY, 1, 1,
                                getWallOverride(
                                    x, lowY,
                                    KillBillGame.get().getTextureLoader().get(room.getWallTexture() + WALL_SUFFIXES[suffixI]),
                                    room
                                ),
                                new ObjectFlag[] {ObjectFlag.SOLID}
                            )
                        );
                    }

                    if (!isExcluded(x, highY - 1, room)) {
                        gameRenderer.addObject(
                            new Tile(
                                x, highY - 1, 1, 1,
                                getWallOverride(
                                    x, highY - 1,
                                    KillBillGame.get().getTextureLoader().get(room.getWallTexture() + WALL_SUFFIXES[suffixI]),
                                    room
                                ),
                                new ObjectFlag[] {ObjectFlag.SOLID}
                            )
                        );
                    }
                }

                suffixI += 2;

                // And left, right lines
                for (int y = lowY + 1; y < highY - 1; y++) {
                    if (!isExcluded(lowX, y, room)) {
                        gameRenderer.addObject(
                            new Tile(
                                lowX, y, 1, 1,
                                getWallOverride(
                                    lowX, y,
                                    KillBillGame.get().getTextureLoader().get(room.getWallTexture() + WALL_SUFFIXES[suffixI]),
                                    room
                                ),
                                new ObjectFlag[] {ObjectFlag.SOLID}
                            )
                        );
                    }

                    if (!isExcluded(highX - 1, y, room)) {
                        gameRenderer.addObject(
                            new Tile(
                                highX - 1, y, 1, 1,
                                getWallOverride(
                                    highX - 1, y,
                                    KillBillGame.get().getTextureLoader().get(room.getWallTexture() + WALL_SUFFIXES[suffixI]),
                                    room
                                ),
                                new ObjectFlag[] {ObjectFlag.SOLID}
                            )
                        );
                    }
                }
            }, RoomDirective.class);

        // Add in all our tiles
        map.forEachDirectiveOfType(
            tile -> {
                for (final TileCoordinates c : tile.getLocations()) {
                    Tile t = new Tile(
                        c.x(),
                        c.y(),
                        tile.getSize().x(),
                        tile.getSize().y(),
                        KillBillGame.get().getTextureLoader().get(tile.getTexture()),
                        tile.getFlags().toArray(new ObjectFlag[tile.getFlags().size()])
                    );
                    t.setRotation(tile.getRotation());
                    gameRenderer.addObject(t);
                }
            }, TileDirective.class);

        // Then decoration tiles
        map.forEachDirectiveOfType(
            object -> {
                for (final Coordinates c : object.getLocations()) {
                    TextureObject o = new TextureObject(
                        (float) c.x(),
                        (float) c.y(),
                        (float) object.getSize().x(),
                        (float) object.getSize().y(),
                        KillBillGame.get().getTextureLoader().get(object.getTexture()),
                        object.getFlags().toArray(new ObjectFlag[object.getFlags().size()])
                    );
                    o.setRotation(object.getRotation());
                    gameRenderer.addObject(o);
                }
            }, ObjectDirective.class);

        // And chests
        map.forEachDirectiveOfType(
            chest -> {
                Chest ch = new Chest(
                    (float) chest.getLocation().x(),
                    (float) chest.getLocation().y(),
                    (float) chest.getSize().x(),
                    (float) chest.getSize().y(),
                    KillBillGame.get().getTextureLoader().get(chest.getTexture()),
                    chest.getFlags().toArray(new ObjectFlag[chest.getFlags().size()]),
                    chest
                );
                ch.setRotation(chest.getRotation());
                gameRenderer.addObject(ch);
            }, ChestDirective.class);

        // Now the player
        gameRenderer.addObject(player);
    }

    private boolean isExcluded(int x, int y, RoomDirective r) {
        for (final TileCoordinates c : r.getWallExclusions()) {
            if (c.x() == x && c.y() == y) return true;
        }
        return false;
    }

    private boolean isWall(int x, int y, RoomDirective r) {
        return (x == r.getLocation().x() || x == r.getLocation().x() + r.getSize().x() - 1 ||
            y == r.getLocation().y() || y == r.getLocation().y() + r.getSize().y() - 1)
            && !isExcluded(x, y, r);
    }

    private Texture getWallOverride(int x, int y, Texture normal, RoomDirective r) {
        for (final WallOverride o : r.getWallOverrides()) {
            if (o.coordinates().x() == x && o.coordinates().y() == y) return KillBillGame.get().getTextureLoader().get(o.texture());
        }
        return normal;
    }


    @Override
    public void drawFirst(final float delta) {
        if (!leaving && !KillBillGame.get().getUdpClient().isConnected() && gameState.getState().equals(BasicGameRunState.PLAYING)) {
            Screens.UDP_ERROR_SCREEN.init("connection lost :(");
            KillBillGame.get().changeScreen(Screens.UDP_ERROR_SCREEN);
            return;
        }

        // Steal the game lock
        if (!gameState.acquireLock()) return;

        try {
            if (dead) {
                if (dieTimer < 0.5f) {
                    dieTimer += delta;
                    dieBg.setTint(new Color(dieBg.getTint().r, dieBg.getTint().g, dieBg.getTint().b, 0.6f * (dieTimer / 0.5f)));
                } else if (dieBg.getTint().a < 0.6f) {
                    dieBg.setTint(new Color(dieBg.getTint().r, dieBg.getTint().g, dieBg.getTint().b, 0.6f));
                }

                if (player.getUserState().getPlayerType().equals(BasicPlayerType.SPECTATOR)) {
                    spectateButton.setVisible(true);
                }
            }

            if (readySkip) updateGameState();

            // Sync all player locations and such with their game states
            for (final DummyPlayer dPlayer : players) {
                dPlayer.updateState();
            }

            // Same for entities
            List<DummyEntity> toRemove = null;
            for (final DummyEntity dEntity : entities) {
                if (!gameState.getEntities().containsKey(dEntity.getEntityState().getId()))  {
                    if (toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(dEntity);
                } else {
                    dEntity.updateState();
                }
            }

            if (toRemove != null) {
                for (final DummyEntity dEntity : toRemove) {
                    gameRenderer.removeObject(dEntity);
                    entities.remove(dEntity);
                }
            }

            // Let objects update their stuff (player movement and such happens here)
            super.drawFirst(delta);

            try {
                gameState.sync();
            } catch (final MessageFailure e) {
                LOGGER.error("Message failure after attempting to sync local game state.", e);
            }

            if (KillBillGame.get().getKeybinds().isKeyJustPressed(KeyType.CLOSE)) {
                if (dead) spectate();
                else {
                    if (paused) unpause();
                    else pause();
                }
            }
        } finally {
            gameState.releaseLock();
        }
    }
    
    public void recvPlayerChat(final IncomingMessage message) {
        BasicRecvChatCommandData data = (BasicRecvChatCommandData) message.data();
        // Determine sender
        final LocalGameUserState user = gameState.getUser(data.getUserId());
        
        chatDisplay.addMessage(
            new PlayerChatMessage(user.getUser(), data.getMessage(), message.createdAt())
        );
    }
    
    public void recvSystemChat(final IncomingMessage message) {
        BasicRecvSystemMessageCommandData data = (BasicRecvSystemMessageCommandData) message.data();
        
        chatDisplay.addMessage(
            new SystemChatMessage(data.getMessage(), message.createdAt())
        );
    }

    public void recvAddItem(final IncomingMessage message) {
        refreshItems();
    }

    public void recvRemoveItem(final IncomingMessage message) {
        refreshItems();
    }

    public void recvBomb(final IncomingMessage message) {
        gameRenderer.addObject(new Bomb((BasicBombCommandData) message.data()));
    }

    public void recvProjectile(final IncomingMessage message) {
        gameRenderer.addObject(new Projectile((BasicProjectileCommandData) message.data()));
    }

    public void refreshItems() {
        // Find any dropped items which are no longer in the state
        List<DroppedItem> toRemove = new ArrayList<>();
        List<String> existingItems = new ArrayList<>();
        gameRenderer.forEachObject(
            object -> {
                if (object instanceof DroppedItem) {
                    DroppedItem i = (DroppedItem) object;

                    if (!gameState.getDroppedItems().containsKey(i.getItem().getId())) {
                        // Kill
                        toRemove.add(i);
                    } else {
                        existingItems.add(i.getItem().getId());
                    }
                }
                return false;
            }
        );

        for (final DroppedItem i : toRemove) {
            gameRenderer.removeObject(i);
        }

        // Add in any new ones
        gameState.getDroppedItems().forEach((id, dItem) -> {
            if (!existingItems.contains(id)) {
                gameRenderer.addObject(
                    new DroppedItem(
                        (float) dItem.getLocation().x(),
                        (float) dItem.getLocation().y(), 
                        0.5f,
                        0.5f,
                        ItemGenerator.generate(dItem.getType()).getHeldTexture(),
                        dItem)
                );
            }
        }); 
    }

    public void recvInteraction(final IncomingMessage message) {
        BasicInteractCommandData data = (BasicInteractCommandData) message.data();
        
        if (data.getDirectiveType().equals(DirectiveType.CHEST)) {
            getGameRenderer().forEachObject(
                object -> {
                    if (object instanceof Chest) {
                        ((Chest) object).runUdpInteraction(data);
                    }
                    return false;
                }
            );
        }
    }
}
