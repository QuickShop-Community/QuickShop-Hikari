package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.obj.QUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * QuickShop chat handling event
 *
 * @author Ghost_chu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QSHandleChatEvent extends AbstractQSEvent {
    private final QUser sender;
    private String message;

    public QSHandleChatEvent(QUser sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    /**
     * Getting the player chat content
     *
     * @return The chat content
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the new player chat content that pass to the QuickShop
     *
     * @param message The new chat content
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getting the chat sender
     *
     * @return The chat sender
     */
    public QUser getSender() {
        return sender;
    }
}
