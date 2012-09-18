package org.cvortex.events.details;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.cvortex.events.annotations.OnEvent;
import org.cvortex.log.Logger;
import org.cvortex.log.LoggerFactory;

final class SubscriberTypeParser {

    private final Logger logger = LoggerFactory.getLogger(SubscriberTypeParser.class);
    
    private final Map<Class<?>, SubscriberTypeDetails> subscriberTypes = new HashMap<Class<?>, SubscriberTypeDetails>();
    
    SubscriberTypeDetails getSubscriberTypeDetails(Class<?> subscriberType) {
        SubscriberTypeDetails details = subscriberTypes.get(subscriberType);
        if (details == null) {
            details = parseNewType(subscriberType);
            subscriberTypes.put(subscriberType, details);
        }
        return details;
    }
    
    private SubscriberTypeDetails parseNewType(Class<?> clazz) {
        logger.info("Parsing class ", clazz);
        Map<Class<?>, Method> eventsMap = new HashMap<Class<?>, Method>();
        while(true) {
            parseType(clazz, eventsMap);
            if (clazz.getSuperclass() != null) {
                clazz = clazz.getSuperclass();
            } else {
                break;
            }
        }
        if (eventsMap.size() == 0) {
            onError("The class " + clazz + " cannot be used like subscriber: no methods annoteated by @OnEvent annotation.");
        }
        return new SubscriberTypeDetails(eventsMap);
    }
    
    private void parseType(Class<?> clazz, Map<Class<?>, Method> eventsMap) {
        Method[] methods = clazz.getDeclaredMethods();
        if (methods != null) {
            for (Method m: methods) {
                if (m.isAnnotationPresent(OnEvent.class)) {
                    logger.info("Found annotated method ", m.getName());
                    Class<?>[] params = m.getParameterTypes();
                    if (params == null || params.length != 1) {
                        onError("The class " + clazz + " cannot be used like subscriber: method " + m.getName() 
                                + " should contain only one parameter with the handled event type.");
                    }
                    Method anotherMethod = eventsMap.get(params[0]);
                    if (anotherMethod != null) {
                        onError("The class " + clazz + " cannot be used like subscriber due to ambiguous methods: " + m.getName() 
                                + " and " + anotherMethod.getName() + " both have same type parameter.");
                    }
                    logger.info("Accepting ", m.getName(), " with param ", params[0]);
                    eventsMap.put(params[0], m);
                }
            }
        }
    }
    
    private void onError(String message) {
        logger.error("Wrong subscriber: ", message);
        throw new IllegalArgumentException(message);
    }
}
