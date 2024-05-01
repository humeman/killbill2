package today.tecktip.killbill.common.gameserver.games;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration data structure for the BASIC game type.
 * @author cs
 */
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class BasicGameConfig {
    /**
     * Player configuration map.
     */
    private Map<BasicPlayerType, List<BasicPlayerConfig>> playerConfig;
    
    /**
     * Constructs a new {@link BasicGameConfig}. This constructor should not be called directly.
     */
    public BasicGameConfig() {  }

    /**
     * Sets the playerConfig JSON property.
     * @param playerConfig Map of all player configs
     */
    @JsonProperty("playerConfig")
    public void setPlayerConfig(final Map<BasicPlayerType, List<BasicPlayerConfig>> playerConfig) {
        this.playerConfig = playerConfig;
    }

    /**
     * Gets the player configurations for this game, mapped by player type.
     * @return Map of all player configs
     */
    @JsonProperty("playerConfig")
    public Map<BasicPlayerType, List<BasicPlayerConfig>> getPlayerConfig() {
        return playerConfig;
    }

    /**
     * The three types of players that can be registered in the BASIC game type.
     */
    public static enum BasicPlayerType {
        /**
         * A regular old player.
         */
        PLAYER,

        /**
         * The enemy Bill player.
         */
        BILL,

        /**
         * Dead people.
         */
        SPECTATOR
    }
}
