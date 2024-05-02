package today.tecktip.killbill.frontend.screens.game;

import java.time.Instant;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.config.Keybinds.KeyType;
import today.tecktip.killbill.frontend.game.effects.SpeedEffect;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.data.TileCoordinates;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.maploader.KillBillMap;
import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.common.maploader.directives.ChestDirective;
import today.tecktip.killbill.common.maploader.directives.EntityDirective;
import today.tecktip.killbill.common.maploader.directives.ObjectDirective;
import today.tecktip.killbill.common.maploader.directives.RoomDirective;
import today.tecktip.killbill.common.maploader.directives.TileDirective;
import today.tecktip.killbill.common.maploader.directives.RoomDirective.WallOverride;
import today.tecktip.killbill.frontend.game.objects.Chest;
import today.tecktip.killbill.frontend.game.objects.Player;
import today.tecktip.killbill.frontend.game.objects.TextureObject;
import today.tecktip.killbill.frontend.game.objects.Tile;
import today.tecktip.killbill.frontend.resources.MapPackageLoader.MapPackage;
import today.tecktip.killbill.frontend.screens.GameScreen;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.ScaledSize;
import today.tecktip.killbill.frontend.ui.renderers.ChatDisplay;
import today.tecktip.killbill.frontend.ui.renderers.EffectDisplay;
import today.tecktip.killbill.frontend.ui.renderers.ChatDisplay.SystemChatMessage;

public class DevGameScreen extends GameScreen {

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

    private Player player;

    private MapPackage mapPackage;

    private int mapI;

    /**
     * Chat display
     */
    private ChatDisplay chatDisplay;


    public DevGameScreen() {
        super(
            new Color(1, 1, 1, 1)
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = new Player(
            0, 
            0, 
            0.95f, 
            0.95f, 
            null
        );
        player.applyEffect(new SpeedEffect(999999999, 5));

        uiRenderer.add(
            new EffectDisplay(player)
        );

        setInputProcessor(
            new InputAdapter() {
                @Override
                public boolean scrolled(final float x, final float y) {
                    if (y < 0) {
                        int newIndex = player.getHeldItemIndex() - 1;
                        if (newIndex < 0) newIndex = GlobalGameConfig.INVENTORY_SIZE - 1;

                        player.setHeldItem(newIndex);
                        player.getHeldItem().resetCooldown();
                        return true;
                    }
                    else if (y > 0) {
                        player.setHeldItem((player.getHeldItemIndex() + 1) % GlobalGameConfig.INVENTORY_SIZE);
                        player.getHeldItem().resetCooldown();
                        return true;
                    }

                    return false;
                }
            }
        );

        chatDisplay = new ChatDisplay(
            true, 
            new ScaledLocation(0.1f, 0.45f),
            new ScaledSize(0.8f, 0.1f),
            48
        );
        getUiRenderer().add(chatDisplay);
    }

    @Override
    public void onSwitch() {
        Coordinates spawn = mapPackage.config().getBasicConfig().getPlayerConfig().get(BasicPlayerType.PLAYER).get(0).getSpawnpoint();
        player.setLocation((float) spawn.x() * GlobalGameConfig.GRID_SIZE, (float) spawn.y() * GlobalGameConfig.GRID_SIZE);
        KillBillGame.get().setPlayer(player);
        loadMap();
        super.onSwitch();
    }

    public void setMapPackage(int i) {
        mapI = i;
        mapPackage = KillBillGame.get().getMapLoader().get(i);
    }

    public void loadMap() {
        // Empty it out
        gameRenderer.clearObjects();

        // Grab our map
        final KillBillMap map = mapPackage.map();

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

        // And dummy entities
        map.forEachDirectiveOfType(
            entity -> {
                TextureObject o = new TextureObject(
                    (float) entity.getLocation().x(),
                    (float) entity.getLocation().y(),
                    (float) entity.getSize().x(),
                    (float) entity.getSize().y(),
                    KillBillGame.get().getTextureLoader().get("dev_entity_marker"),
                    new ObjectFlag[]{}
                );
                o.setRotation(entity.getRotation());
                gameRenderer.addObject(o);
            }, EntityDirective.class);
            
        // Spawnpoints
        mapPackage.config().getBasicConfig().getPlayerConfig()
            .forEach((playerType, configs) -> {
                configs.forEach(
                    config -> {
                    gameRenderer.addObject(
                        new TextureObject(
                            (float) config.getSpawnpoint().x(),
                            (float) config.getSpawnpoint().y(),
                            (float) 1,
                            (float) 1,
                            KillBillGame.get().getTextureLoader().get("dev_spawnpoint_marker"),
                            new ObjectFlag[]{}
                        ));
                    }
                );
            });

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
        if (KillBillGame.get().getKeybinds().isKeyJustPressed(KeyType.RELOAD_MAP)) {
            try {
                KillBillGame.get().getTextureLoader().load();
                KillBillGame.get().getMapLoader().load();
                mapPackage = KillBillGame.get().getMapLoader().get(mapI);
                loadMap();
                chatDisplay.addMessage(new SystemChatMessage("Reloaded map: " + mapPackage.map().getDisplayName(), Instant.now()));
            } catch (final Throwable t) {
                chatDisplay.addMessage(new SystemChatMessage("Reload failed. Check console for more.", Instant.now()));
                Gdx.app.error(DevGameScreen.class.getSimpleName(), "Map reload failed.", t);
            }
        }

        if (KillBillGame.get().getKeybinds().isKeyJustPressed(KeyType.NOCLIP)) {
            // If already solid:
            if (player.hasFlag(ObjectFlag.SOLID)) {
                chatDisplay.addMessage(new SystemChatMessage("Enabled noclip.", Instant.now()));
                player.unsetFlag(ObjectFlag.SOLID);
            } else {
                chatDisplay.addMessage(new SystemChatMessage("Disabled noclip.", Instant.now()));
                player.setFlag(ObjectFlag.SOLID);
            }
        }

        super.drawFirst(delta);
    }
    
}
