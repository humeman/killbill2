package today.tecktip.killbill.common.gameserver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;

/**
 * Denotes a class which handles incoming and/or outgoing messages for a particular {@link MessageDataType}.
 * 
 * @author cs
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {
    /**
     * The game type(s) that this is applicable to. Empty list = all types.
     * @return Game types
     */
    public GameType[] gameTypes() default {};
}
