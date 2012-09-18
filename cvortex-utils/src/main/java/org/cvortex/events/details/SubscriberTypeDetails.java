package org.cvortex.events.details;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

class SubscriberTypeDetails {

    private final Map<Class<?>, Method> eventsMap;
    
    SubscriberTypeDetails(Map<Class<?>, Method> eventsMap) {
        this.eventsMap = Collections.unmodifiableMap(eventsMap);
    }
    
    Method getMethod(Object e) {
        return eventsMap.get(e.getClass());
    }
    
    Map<Class<?>, Method> getEventsMap() {
        return eventsMap;
    }
    
}
