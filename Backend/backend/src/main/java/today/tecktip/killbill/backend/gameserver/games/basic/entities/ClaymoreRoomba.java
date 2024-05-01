package today.tecktip.killbill.backend.gameserver.games.basic.entities;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import today.tecktip.killbill.backend.gameserver.SpringMessageHandler;
import today.tecktip.killbill.backend.gameserver.games.GameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicEntityState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameState;
import today.tecktip.killbill.backend.gameserver.games.basic.BasicGameUserState;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicBombCommand.BasicBombCommandData;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicBombCommand.BombType;
import today.tecktip.killbill.backend.gameserver.games.basic.commands.BasicBombCommand.RecvBombContext;
import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.data.TileCoordinates;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.maploader.directives.EntityDirective.EntityType;

/**
 * Claymore Roomba entity which does what you would expect.
 * @author cs
 */
public class ClaymoreRoomba extends BasicEntityState {
    /**
     * Logs go here
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Employee.class);
    
    public static float PATHFINDING_RANGE_SQUARED = (float) Math.pow(10, 2);
    public static float TILES_PER_SECOND = 4f;
    public static float ATTACK_RANGE_SQUARED = (float) Math.pow(1.5, 2);

    private TileCoordinates pathfindingTo;

    public ClaymoreRoomba(int id, int rotation, BasicGameState parent, Coordinates spawnPoint) {
        super(id, parent, EntityType.CLAYMORE_ROOMBA, spawnPoint, rotation, 2, null);
        pathfindingTo = null;
    }

    @Override
    public boolean onTick(float delta) {
        // If we're already heading somewhere, keep doing that
        if (pathfindingTo != null) {
            if (getState() == 0) {
                // Update to the pathfinding state
                setState(1);
            }

            // Move towards that destination. If we're sufficiently close, snap to it
            float xOffset = (float) (pathfindingTo.x() - getCoordinates().x());
            float yOffset = (float) (pathfindingTo.y() - getCoordinates().y());

            // If both are under 0.1, snap
            if (Math.abs(xOffset) < 0.1f && Math.abs(yOffset) < 0.1f) {
                setCoordinates(new Coordinates(pathfindingTo.x(), pathfindingTo.y()));
                pathfindingTo = null;
            } 
            // Far away. Move towards the destination at our specified speed
            else {
                float speed = TILES_PER_SECOND * delta;

                // Math or something (need to adjust arctan to work in all 4 quadrants)
                int factor = 1;
                if (xOffset < 0) factor = -1;

                // Get the angle
                double angle = Math.atan(yOffset / xOffset);

                // Use more math to get our differences per X and Y
                float xOffsetThisTick = speed * ((float) Math.cos(angle)) * factor;
                float yOffsetThisTick = speed * ((float) Math.sin(angle)) * factor;

                // MOVE
                setCoordinates(getCoordinates().offsetXY(xOffsetThisTick, yOffsetThisTick));

                // Set the rotation
                setRotation(((int) (angle * 180 / Math.PI) + 90) + (factor > 0 ? 180 : 0));
            }
        }
        // Pathfind if available
        else {
            BasicGameUserState closestPlayer = null;
            float closestRange = -1;
            for (final GameUserState rUserState : parent.getConnectedUsers().values()) {
                if (rUserState.isInConnectCooldown()) continue;
                BasicGameUserState userState = (BasicGameUserState) rUserState;
                if (!userState.isInitialized()) continue;
                if (!userState.getPlayerType().equals(BasicPlayerType.PLAYER)) continue;
                float range = userState.getDistanceSquared(getCoordinates());
                if (range < PATHFINDING_RANGE_SQUARED) {
                    // Within 1.5 blocks? Kill :)
                    if (range < ATTACK_RANGE_SQUARED) {
                        // Become bomb
                        try {
                            SpringMessageHandler.get().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_RECV_BOMB)
                                .run(
                                    SpringMessageHandler.get(),
                                    new RecvBombContext(
                                        parent, 
                                        new BasicBombCommandData(MessageDataType.COMMAND_RECV_BOMB, BombType.CLAYMORE_ROOMBA, getCoordinates(), userState.getUser().id()),
                                        null
                                    )
                                );
                        } catch (final MessageFailure e) {
                            LOGGER.error("Failed to turn Claymore Roomba into a bomb.", e);
                        }

                        return true;
                    }
                    else {
                        // Mark the distance if smaller
                        if (closestPlayer == null || range < closestRange) {
                            closestPlayer = userState;
                            closestRange = range;
                        }
                    }
                }
            }

            if (closestPlayer != null) {
                // Find a path
                List<TileCoordinates> path = parent.getPathfindingGrid().bestPath(
                    (int) (getCoordinates().x()), 
                    (int) (getCoordinates().y()),
                    (int) (closestPlayer.getCoordinates().x()),
                    (int) (closestPlayer.getCoordinates().y())
                ); 

                if (path != null && path.size() != 0) {
                    pathfindingTo = parent.getPathfindingGrid().nextTile(new TileCoordinates((int) (getCoordinates().x()), (int) (getCoordinates().y())), path);
                } 
            } else {
                if (getState() != 0) {
                    // No longer pathfinding
                    setState(0);
                }
            }
        }

        sync();
        return false;
    }
}
