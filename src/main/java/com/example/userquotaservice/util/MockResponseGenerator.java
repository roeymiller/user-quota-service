package com.example.userquotaservice.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to generate mock responses for different return types
 */
public class MockResponseGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(MockResponseGenerator.class);
    
    /**
     * Generates an appropriate mock response based on the return type
     * 
     * @param returnType The type that needs to be mocked
     * @return A mock response of the appropriate type
     */
    public static Object generateMockResponse(Class<?> returnType) {
        logger.debug("Generating mock response for type: {}", returnType.getName());
        
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        
        if (returnType == List.class || returnType == Collection.class || returnType == ArrayList.class) {
            return Collections.emptyList();
        }
        
        if (returnType == Set.class) {
            return Collections.emptySet();
        }
        
        if (returnType == Map.class || returnType == HashMap.class) {
            return Collections.emptyMap();
        }
        
        if (returnType == Optional.class) {
            return Optional.empty();
        }
        
        if (returnType == Long.class || returnType == long.class) {
            return 1L;
        }
        
        if (returnType == Integer.class || returnType == int.class) {
            return 1;
        }
        
        if (returnType == Double.class || returnType == double.class) {
            return 1.0;
        }
        
        if (returnType == Float.class || returnType == float.class) {
            return 1.0f;
        }
        
        if (returnType == Boolean.class || returnType == boolean.class) {
            return true;
        }
        
        if (returnType == String.class) {
            return "mock-response";
        }
        
        // Check specific entities based on class name
        String className = returnType.getSimpleName();
        
        if (className.contains("User")) {
            try {
                Object user = returnType.getDeclaredConstructor().newInstance();
                // Try to set id using reflection
                try {
                    java.lang.reflect.Method setId = returnType.getMethod("setId", Long.class);
                    setId.invoke(user, 999L);
                } catch (Exception e) {
                    logger.debug("Could not set id for User class");
                }
                
                // Try to set first name and last name if available
                try {
                    java.lang.reflect.Method setFirstName = returnType.getMethod("setFirstName", String.class);
                    setFirstName.invoke(user, "MockUser");
                } catch (Exception e) {
                    logger.debug("Could not set firstName for User class");
                }
                
                try {
                    java.lang.reflect.Method setLastName = returnType.getMethod("setLastName", String.class);
                    setLastName.invoke(user, "FromNotARealDB");
                } catch (Exception e) {
                    logger.debug("Could not set lastName for User class");
                }
                
                return user;
            } catch (Exception e) {
                logger.debug("Could not create User instance, using fallback");
            }
        }
        
        if (className.contains("Quota") || className.contains("Resource")) {
            try {
                Object resource = returnType.getDeclaredConstructor().newInstance();
                // Try to set id using reflection
                try {
                    java.lang.reflect.Method setId = returnType.getMethod("setId", Long.class);
                    setId.invoke(resource, 888L);
                } catch (Exception e) {
                    logger.debug("Could not set id for Resource class");
                }
                
                // Try to set resource name if available
                try {
                    java.lang.reflect.Method setResourceName = returnType.getMethod("setResourceName", String.class);
                    setResourceName.invoke(resource, "MockResource");
                } catch (Exception e) {
                    logger.debug("Could not set resourceName for Resource class");
                }
                
                return resource;
            } catch (Exception e) {
                logger.debug("Could not create Resource instance, using fallback");
            }
        }
        
        try {
            // Try to return an empty instance for other types if they have a no-arg constructor
            Object instance = returnType.getDeclaredConstructor().newInstance();
            
            // Try to set id generally if it exists
            try {
                java.lang.reflect.Method setId = returnType.getMethod("setId", Long.class);
                setId.invoke(instance, 777L);
            } catch (Exception e) {
                logger.debug("Could not set id for generic class {}", returnType.getName());
            }
            
            return instance;
        } catch (Exception e) {
            logger.debug("Could not create instance of {}, returning null", returnType.getName());
            // For any other type, return null
            return null;
        }
    }
} 