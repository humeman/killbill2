package today.tecktip.killbill.common.gameserver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import today.tecktip.killbill.common.gameserver.MessageHandler.InvokeCommandMethod;
import today.tecktip.killbill.common.gameserver.MessageHandler.MessageCommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.Command;
import today.tecktip.killbill.common.gameserver.annotations.CommandMethod;
import today.tecktip.killbill.common.gameserver.annotations.InvokeMethod;
import today.tecktip.killbill.common.gameserver.annotations.ParseMethod;
import today.tecktip.killbill.common.gameserver.annotations.ResponseMethod;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageData;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.gameserver.messages.MessageData.MessageDataParseMethod;

/**
 * A classpath scanner for the command annotation system.
 * Designed to make the UDP server act more closely to the HTTP side of Spring.
 * 
 * @author cs
 */
public class ClasspathCommandLoader implements CommandLoader {
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathCommandLoader.class);

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

    /**
     * List of all instantiated command objects.
     */
    private List<Object> instantiatedCommands;

    /**
     * Constructs an empty CommandLoader.
     */
    public ClasspathCommandLoader() {
        parseMethods = new HashMap<>();
        commandMethods = new HashMap<>();
        responseMethods = new HashMap<>();
        invokeMethods = new HashMap<>();
        instantiatedCommands = new ArrayList<>();

        for (GameType type : GameType.values()) {
            parseMethods.put(type, new HashMap<>());
            commandMethods.put(type, new HashMap<>());
            responseMethods.put(type, new HashMap<>());
            invokeMethods.put(type, new HashMap<>());
        }
    }
    
    /**
     * Scans the classpath (starting at a particular package) for classes with a particular annotation.
     * @param packageName Package where commands may be located
     * @throws IOException Failed to read files
     * @throws ClassNotFoundException Failed to load class
     */
    public void load(final String packageName) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        try (ScanResult scanResult = 
            new ClassGraph()
                .enableAllInfo()
                .acceptPackages(packageName)
                .scan()) {
            
            for (final ClassInfo commandClassInfo : scanResult.getClassesWithAnnotation(Command.class)) {
                Class<?> commandClass = commandClassInfo.loadClass();
                Command commandAnnotation = commandClass.getAnnotation(Command.class);
                Object instantiatedCommand = commandClass.getDeclaredConstructor().newInstance();
                instantiatedCommands.add(instantiatedCommand);

                final StringBuilder debugBuilder = new StringBuilder("Found command " + commandClassInfo.getSimpleName() + ":");
                
                for (final MethodInfo methodInfo : commandClassInfo.getDeclaredMethodInfo()) {
                    if (methodInfo.hasAnnotation(ParseMethod.class)) {
                        Method method = methodInfo.loadClassAndGetMethod();
                        ParseMethod parseAnnotation = method.getAnnotation(ParseMethod.class);
                        debugBuilder.append(" parse=" + parseAnnotation.type());
                        
                        forGameType(commandAnnotation, (gameType) -> {
                            if (parseMethods.get(gameType).containsKey(parseAnnotation.type())) {
                                throw new RuntimeException("Duplicate parse method: game=" + gameType + ", type=" + parseAnnotation.type());
                            }
                            parseMethods.get(gameType).put(parseAnnotation.type(), (node) -> {
                                try {
                                    return (MessageData) method.invoke(instantiatedCommand, node);
                                } catch (final IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        });
                    }

                    else if (methodInfo.hasAnnotation(CommandMethod.class)) {
                        Method method = methodInfo.loadClassAndGetMethod();
                        CommandMethod commandMethodAnnotation = method.getAnnotation(CommandMethod.class);
                        debugBuilder.append(" cmd=" + commandMethodAnnotation.type());
                        
                        forGameType(commandAnnotation, (gameType) -> {
                            if (commandMethods.get(gameType).containsKey(commandMethodAnnotation.type())) {
                                throw new RuntimeException("Duplicate command method: game=" + gameType + ", type=" + commandMethodAnnotation.type());
                            }
                            commandMethods.get(gameType).put(commandMethodAnnotation.type(), (handler, message, context) -> {
                                try {
                                    method.invoke(instantiatedCommand, handler, message, context);
                                } catch (final IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        });
                    }

                    else if (methodInfo.hasAnnotation(ResponseMethod.class)) {
                        Method method = methodInfo.loadClassAndGetMethod();
                        ResponseMethod responseAnnotation = method.getAnnotation(ResponseMethod.class);
                        debugBuilder.append(" resp=" + responseAnnotation.type());
                        
                        forGameType(commandAnnotation, (gameType) -> {
                            if (responseMethods.get(gameType).containsKey(responseAnnotation.type())) {
                                throw new RuntimeException("Duplicate response method: game=" + gameType + ", type=" + responseAnnotation.type());
                            }
                            responseMethods.get(gameType).put(responseAnnotation.type(), (handler, message, context) -> {
                                try {
                                    method.invoke(instantiatedCommand, handler, message, context);
                                } catch (final IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        });
                    }

                    else if (methodInfo.hasAnnotation(InvokeMethod.class)) {
                        Method method = methodInfo.loadClassAndGetMethod();
                        InvokeMethod invokeAnnotation = method.getAnnotation(InvokeMethod.class);
                        debugBuilder.append(" invoke=" + invokeAnnotation.type());
                        
                        forGameType(commandAnnotation, (gameType) -> {
                            if (invokeMethods.get(gameType).containsKey(invokeAnnotation.type())) {
                                throw new RuntimeException("Duplicate invoke method: game=" + gameType + ", type=" + invokeAnnotation.type());
                            }
                            invokeMethods.get(gameType).put(invokeAnnotation.type(), (handler, context) -> {
                                try {
                                    method.invoke(instantiatedCommand, handler, context);
                                } catch (final IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        });
                    }
                }

                LOGGER.error(debugBuilder.toString());
                System.err.println(debugBuilder.toString());
            }
        }
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
