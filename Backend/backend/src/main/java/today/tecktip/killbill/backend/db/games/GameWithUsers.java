package today.tecktip.killbill.backend.db.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import today.tecktip.killbill.backend.db.gameusers.GameUser;
import today.tecktip.killbill.backend.exceptions.NotFoundException;

/**
 * Represents a game and all of its users.
 * @param game Game
 * @param users Users invited to this game
 * @author cs
 */
public record GameWithUsers(Game game, List<GameUser> users) {

    /**
     * Checks if a user is a player in this game.
     * @param userId User's ID
     * @return True if they are a game user
     */
    public boolean containsUser(final UUID userId) {
        Objects.requireNonNull(userId, "'userId' cannot be null");
        for (GameUser user : users) {
            if (user.userId().equals(userId)) return true;
        }
        return false;
    }

    /**
     * Gets an individual user by ID from the list of users in the game.
     * @param userId User's ID
     * @return Game user matching the specified user ID
     * @throws NotFoundException User not in game
     */
    public GameUser getUser(final UUID userId) {
        Objects.requireNonNull(userId, "'userId' cannot be null");
        for (GameUser user : users) {
            if (user.userId().equals(userId)) return user;
        }
        throw new NotFoundException("User not in game list");
    }

    /**
     * Turns this into a public, friendly representation which can be sent through APIs.
     * @return Friendly representation
     */
    public GameWithUsersPub userFriendly() {
        List<UUID> userIds = new ArrayList<>();

        for (GameUser user : users) {
            userIds.add(user.userId());
        }

        return new GameWithUsersPub(game, userIds);
    }

    /**
     * Data structure for games and users, but more readable when sent over the API.
     * @param game Game object
     * @param users User IDs associated with the game
     */
    public record GameWithUsersPub(Game game, List<UUID> users) {}
}