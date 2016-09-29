package com.medavox.repeats.events;

import com.medavox.repeats.ui.fragments.FragmentTextViews;

/**
 * @author Adam Howard
@date 28/07/2016
 * Describes a change to a UI element (a TextView). Received & handled by fragments which need to be
 * changed by external logic.
 */
public class UIMessageEvent extends Event {

    public static final String BROADCAST = "all receivers";
    private String messageText;
    private String recipientID;
    private FragmentTextViews textView;

    /**Creates a new UIMessageEvent.
     * @param caller the Object calling this constructor. passing 'this' suffices.
     * @param recipientID a string identifying which UI fragment is being targeted.
     * @param textView the TextView belonging to the fragment which needs to be changed
     * @param messageText the text to use on the TextView*/
    public UIMessageEvent(Object caller, String recipientID, FragmentTextViews textView, String messageText) {
        super(caller);
        this.textView = textView;
        this.messageText = messageText;
        this.recipientID = recipientID;
    }

    /**Types of message which can be broadcast.*/
    public enum BroadcastMessages implements FragmentTextViews {
        UPDATE;
    }

    public UIMessageEvent(Object caller, BroadcastMessages bm) {
        super(caller);
        this.textView = bm;
        this.recipientID = BROADCAST;//broadcast message
        this.messageText = null;
    }
    public String getRecipientID() {
        return recipientID;
    }

    public String getMessageText() {
        return messageText;
    }

    /**Returns an enum which must be subtyped. Can be used for conveying which View the UI message is for,
     * or what the broadcast message is.*/
    public FragmentTextViews getDetails() {
        return textView;
    }

    public String toString() {
        String superString = super.toString();
        return superString+"; recipientID: "+recipientID+"; text view: "+textView+"; message text:\""+messageText+"\"";
    }
}
