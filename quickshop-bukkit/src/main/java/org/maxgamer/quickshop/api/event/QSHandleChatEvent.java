package org.maxgamer.quickshop.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class QSHandleChatEvent extends AbstractQSEvent {
    private final Player sender;
    private String message;

    /**
     * Getting the chat sender
     *
     * @return The chat sender
     */
    public Player getSender() {
        return sender;
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
}
