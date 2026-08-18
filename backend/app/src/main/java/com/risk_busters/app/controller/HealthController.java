package com.risk_busters.app.controller;

import com.risk_busters.app.dto.HealthReadinessResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;
    private final ApplicationContext applicationContext;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Value("${spring.application.name:portfolio-risk-expo-service}")
    private String applicationName;

    public HealthController(
            DataSource dataSource,
            ApplicationContext applicationContext,
            RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.dataSource = dataSource;
        this.applicationContext = applicationContext;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @GetMapping("/readiness")
    public ResponseEntity<HealthReadinessResponseDTO> readiness() {
        Map<String, String> checks = new LinkedHashMap<>();
        checks.put("application", "UP");
        checks.put("controllers", areControllersReady() ? "UP" : "DOWN");

        // A successful lightweight DB query indicates the service is ready to process requests.
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("SELECT 1")) {
            statement.execute();
            checks.put("database", "UP");

            String status = "UP".equals(checks.get("controllers")) ? "UP" : "DOWN";
            HttpStatus httpStatus = "UP".equals(status) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(httpStatus).body(buildResponse(status, checks));
        } catch (SQLException ex) {
            checks.put("database", "DOWN");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(buildResponse("DOWN", checks));
        }
    }

    private boolean areControllersReady() {
        Map<String, Object> controllerBeans = applicationContext.getBeansWithAnnotation(RestController.class);
        Set<Class<?>> expectedControllerTypes = new HashSet<>();

        for (Object bean : controllerBeans.values()) {
            Class<?> beanType = AopUtils.getTargetClass(bean);
            if (beanType.getName().startsWith("com.risk_busters.app.controller")) {
                expectedControllerTypes.add(beanType);
            }
        }

        if (expectedControllerTypes.isEmpty()) {
            return false;
        }

        Set<Class<?>> mappedControllerTypes = new HashSet<>();
        for (HandlerMethod handlerMethod : requestMappingHandlerMapping.getHandlerMethods().values()) {
            mappedControllerTypes.add(handlerMethod.getBeanType());
        }

        return mappedControllerTypes.containsAll(expectedControllerTypes);
    }

    private HealthReadinessResponseDTO buildResponse(String status, Map<String, String> checks) {
        return HealthReadinessResponseDTO.builder()
                .service(applicationName)
                .status(status)
                .timestamp(Instant.now())
                .checks(checks)
                .build();
    }
}



