package today.tecktip.killbill.frontend.gameserver;

import java.util.HashMap;
import java.util.Map;

import today.tecktip.killbill.common.gameserver.CommandLoader;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeCommandMethod;
import today.tecktip.killbill.common.gameserver.MessageHandler.MessageCommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.CommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.annotations.ResponseMethod;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageData.MessageDataParseMethod;
import today.tecktip.killbill.frontend.gameserver.commands.ConnectCommand;
import today.tecktip.killbill.frontend.gameserver.commands.DisconnectCommand;
import today.tecktip.killbill.frontend.gameserver.commands.General;
import today.tecktip.killbill.frontend.gameserver.commands.HeartbeatCommand;
import today.tecktip.killbill.frontend.gameserver.commands.PingCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicBombCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangeEntityStateCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangeGameStateCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangeOtherPlayerStateCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangePlayerStateCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicDroppedItemCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicInteractCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicProjectileCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicRecvEntityStateCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicRecvGameStateCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicRecvPlayerStateCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicSendChatCommand;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicSendStateCommand;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;

/**
 * Since Android doesn't let us do classpath scanning >:(
 * @author cs
 */
public class HardcodedCommandLoader implements CommandLoader {
    /**
     * Registered methods annotated with {@link ParseMethod}.
     */
    private Map<GameType, Map<MessageDataType, MessageDataParseMethod>> parseMethods;

    /**
     * Registered methods annotated with {@link CommandMethod}.
     */
    private Map<GameType, Map<MessageDataType, MessageCommandMethod>> commandMethods;

    /**
     * Registered methods annotated with {@link ResponseMethod}.
     */
    private Map<GameType, Map<MessageDataType, MessageCommandMethod>> responseMethods;

    /**
     * Registered methods annotated with {@link InvokeMethod}.
     */
    private Map<GameType, Map<MessageDataType, InvokeCommandMethod>> invokeMethods;

    public HardcodedCommandLoader() {
        parseMethods = new HashMap<>();
        commandMethods = new HashMap<>();
        responseMethods = new HashMap<>();
        invokeMethods = new HashMap<>();

        for (GameType type : GameType.values()) {
            parseMethods.put(type, new HashMap<>());
            commandMethods.put(type, new HashMap<>());
            responseMethods.put(type, new HashMap<>());
            invokeMethods.put(type, new HashMap<>());
        }
    }

    public void populate() {
        ConnectCommand connectCommand = new ConnectCommand();
        DisconnectCommand disconnectCommand = new DisconnectCommand();
        General generalCommands = new General();
        HeartbeatCommand heartbeatCommand = new HeartbeatCommand();
        PingCommand pingCommand = new PingCommand();

        // Global parse methods
        for (final Map.Entry<GameType, Map<MessageDataType, MessageDataParseMethod>> entry : parseMethods.entrySet()) {
            entry.getValue().put(MessageDataType.RESP_CONNECT, connectCommand::parse);
            entry.getValue().put(MessageDataType.AUTHENTICATION_FAILURE, generalCommands::parseAuthenticationFailure);
            entry.getValue().put(MessageDataType.ILLEGAL_STATE_EXCEPTION, generalCommands::parseIllegalStateException);
            entry.getValue().put(MessageDataType.INTERNAL_SERVER_ERROR, generalCommands::parseInternalServerError);
            entry.getValue().put(MessageDataType.INVALID_ARGUMENT_EXCEPTION, generalCommands::parseInvalidArgumentException);
            entry.getValue().put(MessageDataType.EMPTY, generalCommands::parseEmpty);
            entry.getValue().put(MessageDataType.RESP_PING, pingCommand::parse);
        }

        // No command methods

        // Invoke methods
        for (final Map.Entry<GameType, Map<MessageDataType, InvokeCommandMethod>> entry : invokeMethods.entrySet()) {
            entry.getValue().put(MessageDataType.COMMAND_CONNECT, connectCommand::connect);
            entry.getValue().put(MessageDataType.COMMAND_DISCONNECT, disconnectCommand::disconnect);
            entry.getValue().put(MessageDataType.COMMAND_HEARTBEAT, heartbeatCommand::invoke);
            entry.getValue().put(MessageDataType.COMMAND_PING, pingCommand::invoke);
        }

        // Response methods
        for (final Map.Entry<GameType, Map<MessageDataType, MessageCommandMethod>> entry : responseMethods.entrySet()) {
            entry.getValue().put(MessageDataType.RESP_CONNECT, connectCommand::handleResponse);
            entry.getValue().put(MessageDataType.RESP_PING, pingCommand::handleResponse);
        }


        /*    ----- BASIC GAME -----     */
        BasicChangeGameStateCommand changeGameStateCommand = new BasicChangeGameStateCommand();
        BasicChangePlayerStateCommand changePlayerStateCommand = new BasicChangePlayerStateCommand();
        BasicRecvGameStateCommand recvGameStateCommand = new BasicRecvGameStateCommand();
        BasicRecvPlayerStateCommand recvPlayerStateCommand = new BasicRecvPlayerStateCommand();
        BasicSendChatCommand sendChatCommand = new BasicSendChatCommand();
        BasicBombCommand bombCommand = new BasicBombCommand();
        BasicChangeEntityStateCommand changeEntityStateCommand = new BasicChangeEntityStateCommand();
        BasicChangeOtherPlayerStateCommand changeOtherPlayerStateCommand = new BasicChangeOtherPlayerStateCommand();
        BasicDroppedItemCommand droppedItemCommand = new BasicDroppedItemCommand();
        BasicInteractCommand interactCommand = new BasicInteractCommand();
        BasicProjectileCommand projectileCommand = new BasicProjectileCommand();
        BasicRecvEntityStateCommand recvEntityStateCommand = new BasicRecvEntityStateCommand();
        BasicSendStateCommand sendStateCommand = new BasicSendStateCommand();

        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_GAME_STATE, recvGameStateCommand::parse);
        parseMethods.get(GameType.BASIC).put(MessageDataType.RESP_GET_GAME_STATE, recvGameStateCommand::parseResp);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_PLAYER_STATE, recvPlayerStateCommand::parse);
        parseMethods.get(GameType.BASIC).put(MessageDataType.RESP_GET_PLAYER_STATE, recvPlayerStateCommand::parseResp);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_CHAT, sendChatCommand::parse);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE, sendChatCommand::parseSystem);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_BOMB, bombCommand::parse);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM, droppedItemCommand::parse);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_REMOVE_DROPPED_ITEM, droppedItemCommand::parseRemove);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_INTERACTION, interactCommand::parse);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_PROJECTILE, projectileCommand::parse);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_ENTITY_STATE, recvEntityStateCommand::parse);
        parseMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_REMOVE_ENTITY, recvEntityStateCommand::parseRemove);
        parseMethods.get(GameType.BASIC).put(MessageDataType.RESP_GET_ENTITY_STATE, recvEntityStateCommand::parseGet);

        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_GAME_STATE, recvGameStateCommand::run);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_PLAYER_STATE, recvPlayerStateCommand::run);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_CHAT, sendChatCommand::run);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_SYSTEM_MESSAGE, sendChatCommand::runSystem);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_BOMB, bombCommand::run);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_NEW_DROPPED_ITEM, droppedItemCommand::run);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_REMOVE_DROPPED_ITEM, droppedItemCommand::runRemove);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_INTERACTION, interactCommand::run);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_PROJECTILE, projectileCommand::run);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_ENTITY_STATE, recvEntityStateCommand::run);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_REMOVE_ENTITY, recvEntityStateCommand::runRemove);
        commandMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_RECV_PROJECTILE, projectileCommand::run);

        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_CHANGE_GAME_STATE, changeGameStateCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_CHANGE_PLAYER_STATE, changePlayerStateCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_GET_GAME_STATE, recvGameStateCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_GET_PLAYER_STATE, recvPlayerStateCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_SEND_CHAT, sendChatCommand::run);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_CREATE_BOMB, bombCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_CHANGE_ENTITY_STATE, changeEntityStateCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_SUMMON_ENTITY, changeEntityStateCommand::sendSummon);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_CHANGE_OTHER_PLAYER_STATE, changeOtherPlayerStateCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_CREATE_DROPPED_ITEM, droppedItemCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_REMOVE_DROPPED_ITEM, droppedItemCommand::sendRemove);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_INTERACT, interactCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_CREATE_PROJECTILE, projectileCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_GET_ENTITY_STATE, recvEntityStateCommand::send);
        invokeMethods.get(GameType.BASIC).put(MessageDataType.COMMAND_SEND_STATE, sendStateCommand::send);

        responseMethods.get(GameType.BASIC).put(MessageDataType.RESP_GET_GAME_STATE, recvGameStateCommand::runResponse);
        responseMethods.get(GameType.BASIC).put(MessageDataType.RESP_GET_PLAYER_STATE, recvPlayerStateCommand::runResponse);
        responseMethods.get(GameType.BASIC).put(MessageDataType.RESP_GET_ENTITY_STATE, recvEntityStateCommand::runResp);
    }

    @Override
    public MessageDataParseMethod parseMethodFor(final GameType gameType, final MessageDataType dataType) throws IllegalArgumentException {
        return parseMethods.get(gameType).get(dataType);
    }

    @Override
    public MessageCommandMethod commandMethodFor(final GameType gameType, final MessageDataType dataType) throws IllegalArgumentException {
        return commandMethods.get(gameType).get(dataType);
    }

    @Override
    public MessageCommandMethod responseMethodFor(final GameType gameType, final MessageDataType dataType) throws IllegalArgumentException {
        return responseMethods.get(gameType).get(dataType);
    }

    @Override
    public InvokeCommandMethod invokeMethodFor(final GameType gameType, final MessageDataType dataType) throws IllegalArgumentException {
        return invokeMethods.get(gameType).get(dataType);
    }

    @Override
    public void forGameType(final Command commandAnnotation, final GameTypeIteratorMethod action) {
        if (commandAnnotation.gameTypes().length == 0) {
            for (GameType gameType : GameType.values()) action.run(gameType);
        } else {
            for (GameType gameType : commandAnnotation.gameTypes()) action.run(gameType);
        }
    }
}
