package com.medavox.repeats.events;

/**
 * @author Adam Howard
@date 24/07/16
 * An event which has a subtype from a defined group.
 * Extend this to create a family of related events (using a single Class) relating to a single concern, with differing EventTypes.
 */
public abstract class TypedEvent extends Event {
    /**The subtype of event, eg DEVICE_DICONNECTED, DEVICE_DISCOVERED, DEVICE_ERROR.
     * Classes implementing TypedEent should use an enum here.*/
    EventType eventType;
    public TypedEvent(Object caller, EventType type) {
        super(caller);
        this.eventType = type;
    }

    /**The Event's sub-type. Useful for describing a family of related events.
     * For instance, a CupboardEvent whose EventTypes consist of CupboardEventType.OPENED,
     * CupboardEventType.CLOSED, and CupboardEventType.ITEM_REMOVED.
     * Should be implemented with an Enum.*/
    public interface EventType{};

    /**Returns the Event's sub-type.
     * Implementing classes should provide a version of this method
     * whose signature's return type implements EventType,
     * to avoid users of this class having to cast this method to the implementer.
     * Example: public ExampleImplementingEventType getEventType {return event} */
    public <T extends EventType> EventType getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "Event class: \'"+this.getClass().getSimpleName()+"\'; subtype: \'"+eventType+"\'; caller: \'"+getCaller()+"\'";
    }
}
