package today.tecktip.killbill.common.gameserver;

import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeCommandMethod;
import today.tecktip.killbill.common.gameserver.MessageHandler.MessageCommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.MessageData.MessageDataParseMethod;

/**
 * A class which loads UDP commands, parse methods, and responses.
 * Implemented as a classpath scanner on desktop/Spring, but since that is
 * incompatible with Android, it is manually loaded there.
 * 
 * @author cs
 */
public interface CommandLoader {
    /**
     * Gets the parse method associated with the specified game type and data type.
     * @param gameType Game type to retrieve parse method for
     * @param dataType Data type to retrieve parse method for
     * @return Parse method or null if not found
     */
    public MessageDataParseMethod parseMethodFor(final GameType gameType, final MessageDataType dataType) throws IllegalArgumentException;

    /**
     * Gets the command method associated with the specified game type and data type.
     * @param gameType Game type to retrieve command method for
     * @param dataType Data type to retrieve command method for
     * @return Command method or null if not found
     */
    public MessageCommandMethod commandMethodFor(final GameType gameType, final MessageDataType dataType) throws IllegalArgumentException;

    /**
     * Gets the response method associated with the specified game type and data type.
     * @param gameType Game type to retrieve response method for
     * @param dataType Data type to retrieve response method for
     * @return Response method or null if not found
     */
    public MessageCommandMethod responseMethodFor(final GameType gameType, final MessageDataType dataType) throws IllegalArgumentException;

    /**
     * Gets the invoke method associated with the specified game type and data type.
     * @param gameType Game type to retrieve invoke method for
     * @param dataType Data type to retrieve invoke method for
     * @return Invoke method or null if not found
     */
    public InvokeCommandMethod invokeMethodFor(final GameType gameType, final MessageDataType dataType) throws IllegalArgumentException;

    /**
     * Iterates over each game type registered to a command annotation.
     * @param commandAnnotation Command annotation instance
     * @param action Action to take for each game type
     */
    public void forGameType(final Command commandAnnotation, final GameTypeIteratorMethod action) throws Exception;

    /**
     * Lambda interface for {@link #forGameType}.
     */
    public static interface GameTypeIteratorMethod {
        /**
         * Runs an action for a game type.
         * @param gameType Game type
         */
        public void run(final GameType gameType);
    }
}
