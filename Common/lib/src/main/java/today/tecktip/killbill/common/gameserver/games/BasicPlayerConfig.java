package today.tecktip.killbill.common.gameserver.games;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import today.tecktip.killbill.common.gameserver.data.Coordinates;

/**
 * Configuration data structure for a player in the BASIC game type.
 * @author cs
 */
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class BasicPlayerConfig {
    /**
     * The ID for this player config.
     */
    private int id;

    /**
     * Where this player spawns.
     */
    private Coordinates spawnpoint;

    /**
     * The prefix for the texture set this player uses.
     */
    private String texturePrefix;
    
    /**
     * The max health this player can have.
     */
    private int maxHealth;

    /**
     * Constructs new basic player configuration.
     */
    public BasicPlayerConfig(
        @JsonProperty(value = "id", required = true) Integer id,
        @JsonProperty(value = "spawnpoint", required = true) List<Double> spawnpoint,
        @JsonProperty(value = "texturePrefix", required = true) String texturePrefix,
        @JsonProperty(value = "maxHealth", required = true) Integer maxHealth
    ) {
        this.id = id;
        this.spawnpoint = Coordinates.fromList(spawnpoint);
        this.texturePrefix = texturePrefix;
        this.maxHealth = maxHealth;
    }

    /**
     * Gets the ID number for this player config.
     * @return ID
     */
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    /**
     * Gets the spawnpoint coordinates as represented in the JSON structure.
     * @return Spawnpoint coordinates as a list of two doubles.
     */
    @JsonProperty("spawnpoint")
    public List<Double> getSpawnpointJson() {
        return spawnpoint.toList();
    }

    /**
     * Gets the spawnpoint for this player.
     * @return Spawn point
     */
    public Coordinates getSpawnpoint() {
        return spawnpoint;
    }

    /**
     * Gets the texture prefix for this player's texture set as a string.
     * @return Texture prefix
     */
    @JsonProperty("texturePrefix")
    public String getTexturePrefix() {
        return texturePrefix;
    }

    /**
     * Gets the max health for this player.
     * @return Max health
     */
    @JsonProperty("maxHealth")
    public int getMaxHealth() {
        return maxHealth;
    }
}
