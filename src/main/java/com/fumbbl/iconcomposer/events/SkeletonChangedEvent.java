package com.fumbbl.iconcomposer.events;

import javafx.event.Event;
import javafx.event.EventType;

public class SkeletonChangedEvent extends Event {

    public static final EventType<SkeletonChangedEvent> SKELETON_CHANGED = new EventType<>(EventType.ROOT, "SKELETON_CHANGED");

    public SkeletonChangedEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
}
