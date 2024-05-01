package today.tecktip.killbill.common.gameserver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import today.tecktip.killbill.common.gameserver.messages.MessageDataType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResponseMethod {
    /**
     * The data type this method handles responses for.
     * @return Response data type
     */
    public MessageDataType type();
}