package com.example.userquotaservice.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import com.example.userquotaservice.service.DataOperationService;
import com.example.userquotaservice.service.DatabaseSelectorService;
import com.example.userquotaservice.util.MockResponseGenerator;

/**
 * Factory for creating proxies that check if real database operations should be performed
 */
@Component
public class DataServiceProxyFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(DataServiceProxyFactory.class);
    
    private final DatabaseSelectorService databaseSelectorService;
    
    public DataServiceProxyFactory(DatabaseSelectorService databaseSelectorService) {
        this.databaseSelectorService = databaseSelectorService;
        logger.info("DataServiceProxyFactory initialized");
    }
    
    /**
     * Create a proxy that will only perform real operations if MySQL is active
     * 
     * @param <T> The type of service to proxy
     * @param realService The actual service implementation
     * @param serviceInterface The interface or class that the service implements
     * @return A proxy instance that wraps the real service
     */
    @SuppressWarnings("unchecked")
    public <T extends DataOperationService> T createServiceProxy(T realService, Class<T> serviceInterface) {
        logger.debug("Creating proxy for service: {}", realService.getClass().getSimpleName());
        
        // Use CGLIB for proxying concrete classes
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(serviceInterface);
        enhancer.setCallback(new DatabaseAwareMethodInterceptor<>(realService, databaseSelectorService));
        
        return (T) enhancer.create();
    }
    
    /**
     * MethodInterceptor that checks if real operations should be performed based on the active database provider
     */
    private static class DatabaseAwareMethodInterceptor<T extends DataOperationService> implements MethodInterceptor {
        private final T target;
        private final DatabaseSelectorService databaseSelectorService;
        private final Logger targetLogger;
        
        public DatabaseAwareMethodInterceptor(T target, DatabaseSelectorService databaseSelectorService) {
            this.target = target;
            this.databaseSelectorService = databaseSelectorService;
            this.targetLogger = LoggerFactory.getLogger(target.getClass());
        }
        
        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            // Skip interception for the shouldPerformRealOperations method itself and Object methods
            if (method.getName().equals("shouldPerformRealOperations") || 
                method.getDeclaringClass() == Object.class) {
                return method.invoke(target, args);
            }
            
            // Check if the active provider is MySQL
            boolean isRealDbActive = "MySQL".equals(databaseSelectorService.getActiveProvider().getDatabaseName());
            
            if (!isRealDbActive) {
                // If NotARealDB is active, just log the method call and return a mock response
                String methodName = method.getName();
                String argsString = args != null ? Arrays.toString(args) : "[]";
                targetLogger.info("🔶 MOCK OPERATION: {} with args: {}", methodName, argsString);
                
                // Special handling for resource consumption
                if (methodName.equals("consume") && args != null && args.length >= 2) {
                    String resourceName = (String) args[0];
                    Long userId = (Long) args[1];
                    targetLogger.info("🔶 Mock consuming resource {} for user {}", resourceName, userId);
                    // Return null since it's a void method
                    return null;
                }
                
                // Special handling for threshold queries
                if ((methodName.equals("getThreshold") || methodName.contains("Threshold")) && args != null && args.length >= 1) {
                    String resourceName = (String) args[0];
                    // Return threshold based on resource
                    if (resourceName.contains("One")) {
                        targetLogger.info("🔶 Mock returning threshold 5 for {}", resourceName);
                        return 5;
                    } else if (resourceName.contains("Two")) {
                        targetLogger.info("🔶 Mock returning threshold 3 for {}", resourceName);
                        return 3;
                    }
                    return 10; // Default
                }
                
                // Special handling for user queries
                if (methodName.equals("getUser") || methodName.contains("User")) {
                    targetLogger.info("🔶 Mock returning user for id: {}", args[0]);
                    // Use MockResponseGenerator to get a user object
                    return MockResponseGenerator.generateMockResponse(method.getReturnType());
                }
                
                // Return appropriate mock value based on return type
                return MockResponseGenerator.generateMockResponse(method.getReturnType());
            }
            
            // Otherwise perform the real operation
            return method.invoke(target, args);
        }
    }
} 