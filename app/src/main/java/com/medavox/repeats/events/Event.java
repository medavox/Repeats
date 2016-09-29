package com.medavox.repeats.events;

/**@author Adam Howard
@date 22/07/2016
 */
public abstract class Event {

    /**The internal reference to this Event's instantiating object.*/
    protected Object caller;

    /**Create a new Event, specifying the object instantiating it. Passing "this" suffices.
     * This ensures a proper audit trail for event debugging.*/
    public Event(Object caller) {
        this.caller = caller;
    }

    /**Get a reference to the object which instantiated this Event.
     * This is usually also the object which called EventBus.post() with it.
     * @return the object reference provided at construction.
     *      Should be a reference to the object which instantiated this.*/
    public Object getCaller(){
        return caller;
    }

    @Override
    public String toString() {
        return "Event class: \'"+this.getClass()+"\'; caller: \'"+caller+"\'";
    }
}
