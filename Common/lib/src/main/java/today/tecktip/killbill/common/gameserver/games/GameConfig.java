package today.tecktip.killbill.common.gameserver.games;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base game configuration settings, as received while creating a database.
 * @author cs
 */
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class GameConfig {
    /**
     * Object mapper for deserializing/serializing as JSON.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Type of this game.
     */
    private GameType type;

    /**
     * Basic game configuration.
     */
    private BasicGameConfig basicConfig;

    /**
     * Constructs a new game config. Should not be instantiated manually.
     */
    public GameConfig() {  }

    /**
     * Sets the JSON property holding the game type. Called by Jackson.
     * @param type Game type
     */
    @JsonProperty("type")
    public void setType(final GameType type) {
        Objects.requireNonNull(type, "'type' cannot be null");
        this.type = type;
    }

    /**
     * Sets the JSON property holding the basic game config, if present.
     * @param basicConfig Basic config (deserialized)
     */
    @JsonProperty("basicConfig")
    public void setBasicConfig(final BasicGameConfig basicConfig) {
        this.basicConfig = basicConfig;
    }

    /**
     * Gets the type of this game.
     * @return Game type
     */
    @JsonProperty("type")
    public GameType getGameType() {
        return type;
    }

    /**
     * Gets the game's basic configuration (if supplied by the user).
     * @return Basic game config
     */
    @JsonProperty("basicConfig")
    public BasicGameConfig getBasicConfig() {
        return basicConfig;
    }
    
    /**
     * Serializes this config to JSON.
     * @return JSON serialization
     * @throws JsonProcessingException Unable to serialize
     */
    public String toJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }

    /**
     * Serializes this config to JSON.
     * @return JSON serialization
     * @throws JsonProcessingException Unable to serialize
     */
    public JsonNode toJsonNode() throws JsonProcessingException {
        return MAPPER.valueToTree(this);
    }

    /**
     * Reads a JSON string to a {@link GameConfig}.
     * @param json JSON string for the config object
     * @return Parsed config
     * @throws JsonProcessingException Unable to deserialize
     */
    public static GameConfig fromJson(final String json) throws JsonProcessingException {
        return MAPPER.readValue(json, GameConfig.class);
    }
    
    /**
     * Serializes this config to JSON.
     * @return JSON serialization
     * @throws JsonProcessingException Unable to serialize
     */
    public static GameConfig fromJsonNode(final JsonNode node) throws JsonProcessingException {
        return MAPPER.convertValue(node, GameConfig.class);
    }
}
