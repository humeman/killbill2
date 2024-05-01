package today.tecktip.killbill.frontend.http.requests.data;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;
import today.tecktip.killbill.common.gameserver.games.GameConfig;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;

/**
 * Record representing a game retrieved from the API.
 * @param id Game's UUID
 * @param name Game's name/description
 * @param created Timestamp for when the game was created
 * @param hostId User hosting the game
 * @param config Game config
 * @param map Map string
 * @author cs
 */
public record Game(
    @NotNull UUID id,
    @NotNull String name,
    @NotNull Instant created,
    @NotNull UUID hostId,
    @NotNull GameConfig config,
    @NotNull String map) {
    
    /**
     * Parses a raw API data-type user into a regular Game.
     * @param rawGame Raw game response from API
     * @return Parsed regular Game
     */
    public static Game fromRaw(final RawGame rawGame) {
        try {
            return new Game(
                UUID.fromString(rawGame.id()),
                rawGame.name(),
                Instant.parse(rawGame.created()),
                UUID.fromString(rawGame.hostId()),
                GameConfig.fromJsonNode(rawGame.config()),
                rawGame.map()
            );
        } catch (final Throwable t) {
            throw new CatastrophicException("Unable to parse response from the API: ", t);
        }
    }

    /**
     * Returns a game to its raw API data-type.
     * @return Raw representation, sendable to the API
     */
    public RawGame toRaw() throws JsonProcessingException {
        return new RawGame(
            id.toString(),
            name,
            created.toString(),
            hostId.toString(),
            config.toJsonNode(),
            map
        );
    }

    /**
     * The raw data for a game as received by the API.
     * @param id Game's UUID
     * @param name Game's name/description
     * @param created Timestamp for when the game was created
     * @param hostId User hosting the game
     * @param config Game config
     * @param map Map string
     */
    public static record RawGame(
        @JsonProperty("id") @NotNull String id,
        @JsonProperty("name") @NotNull String name,
        @JsonProperty("created") @NotNull String created,
        @JsonProperty("hostId") @NotNull String hostId,
        @JsonProperty("config") @NotNull JsonNode config,
        @JsonProperty("map") @NotNull String map) {}
}
