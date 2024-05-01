package today.tecktip.killbill.common.gameserver.messages;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import today.tecktip.killbill.common.gameserver.MessageHandler;

/**
 * Represents a base outgoing message.
 * @param success True if the command was successful, false if an error occurred
 * @param messageId Outgoing message ID to be acked, if applicable
 * @param ackMessageId Message ID acknowledged, if applicable
 * @param viability Time after which the message will be dropped from any retries
 * @param data Message's data payload
 * 
 * @author cs
 */

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public record OutgoingMessage(
    @JsonProperty("createdAt") long createdAt,
    @JsonProperty("success") Boolean success,
    @JsonProperty("messageId") UUID messageId,
    @JsonProperty("ackMessageId") UUID ackMessageId,
    @JsonProperty("viability") Integer viability,
    @JsonProperty("data") MessageData data,
    @JsonProperty("key") String key
) {
    /**
     * Construct a new OutgoingMessage.
     * @param success True if the command was successful, false if an error occurred
     * @param messageId Outgoing message ID to be acked, if applicable
     * @param ackMessageId Message ID acknowledged, if applicable
     * @param data Message's data payload
     */
    public OutgoingMessage {
        Objects.requireNonNull(data, "'data' cannot be null. Use EmptyMessage for no data.");
    }

    /**
     * Creates a new builder for an empty OutgoingMessage.
     * @return Outgoing message builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builds an {@link OutgoingMessage}.
     */
    public static class Builder {
        private long createdAt = -1;
        private Boolean success;
        private UUID messageId;
        private UUID ackMessageId;
        private MessageData data;
        private Integer viability;
        private String key;

        /**
         * Creates a new OutgoingMessageBuilder.
         */
        public Builder() {
            viability = MessageHandler.DEFAULT_OUTGOING_MESSAGE_VIABILITY;
        }

        /**
         * Marks the command as successful.
         * @return Builder for chaining
         */
        public Builder success() {
            success = true;
            return this;
        }

        /**
         * Marks the command as unsuccessful.
         * @return Builder for chaining
         */
        public Builder failure() {
            success = false;
            return this;
        }

        /**
         * Defines a message ID the client should ack.
         * @param messageId Message ID to apply to builder
         * @return Builder for chaining
         */
        public Builder messageId(final UUID messageId) {
            this.messageId = messageId;
            return this;
        }

        /**
         * Generates and attaches a random message ID for clients to ack.
         * @return Builder for chaining
         */
        public Builder randomMessageId() {
            this.messageId = UUID.randomUUID();
            return this;
        }

        /**
         * Defines a message ID this response is acking.
         * @param ackMessageId Ack Message ID to apply to builder
         * @return Builder for chaining
         */
        public Builder ackMessageId(final UUID ackMessageId) {
            this.ackMessageId = ackMessageId;
            return this;
        }

        /**
         * Sets the data for this message.
         * @param data Message data to apply to builder
         * @return Builder for chaining
         */
        public Builder data(final MessageData data) {
            this.data = data;
            return this;
        }

        /**
         * Specifies how long the message may be valid for, assuming a retry is necessary (either by the client or the server).
         * @param viabilityMs Milliseconds after which this message will be discarded
         * @return Builder for chaining
         */
        public Builder viability(final int viabilityMs) {
            this.viability = viabilityMs;
            return this;
        }

        /**
         * Sets the API key for this message.
         * @param key API key
         * @return Builder for chaining
         */
        public Builder setKey(final String key) {
            this.key = key;
            return this;
        }

        /**
         * Sets the API key for this message to whatever the handler has set.
         * @param handler Message handler
         * @return Builder for chaining
         */
        public Builder setKey(final MessageHandler handler) {
            key = handler.getKey();
            return this;
        }

        /**
         * Overrides the createdAt field for this message.
         * @param created Timestamp to apply
         * @return Builder for chaining
         */
        public Builder setCreatedAt(final Instant created) {
            createdAt = created.toEpochMilli();
            return this;
        }

        /**
         * Builds an {@link OutgoingMessage} with the configured data.
         * @return Generated outgoing message
         */
        public OutgoingMessage build() {
            return new OutgoingMessage(createdAt >= 0 ? createdAt : Instant.now().toEpochMilli(), success, messageId, ackMessageId, viability, data, key);
        }
    }
}
