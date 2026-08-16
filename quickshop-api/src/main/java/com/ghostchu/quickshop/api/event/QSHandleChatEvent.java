package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.obj.QUser;

/**
 * QuickShop chat handling event
 *
 * @author Ghost_chu
 */
public class QSHandleChatEvent extends AbstractQSEvent {

  private final QUser sender;
  private String message;

  public QSHandleChatEvent(final QUser sender, final String message) {

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
  public void setMessage(final String message) {

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

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof QSHandleChatEvent)) return false;
    final QSHandleChatEvent other = (QSHandleChatEvent)o;
    if(!other.canEqual((Object)this)) return false;
    if(!super.equals(o)) return false;
    final Object thisSender = this.getSender();
    final Object otherSender = other.getSender();
    if(thisSender == null? otherSender != null : !thisSender.equals(otherSender)) return false;
    final Object thisMessage = this.getMessage();
    final Object otherMessage = other.getMessage();
    if(thisMessage == null? otherMessage != null : !thisMessage.equals(otherMessage)) {
      return false;
  }
    return true;
  }

  protected boolean canEqual(final Object other) {

    return other instanceof QSHandleChatEvent;
  }

  @Override
  public int hashCode() {

    final int PRIME = 59;
    int result = super.hashCode();
    final Object senderValue = this.getSender();
    result = result * PRIME + (senderValue == null? 43 : senderValue.hashCode());
    final Object messageValue = this.getMessage();
    result = result * PRIME + (messageValue == null? 43 : messageValue.hashCode());
    return result;
  }

  @Override
  public String toString() {

    return "QSHandleChatEvent(sender=" + this.getSender() + ", message=" + this.getMessage() + ")";
  }
}
