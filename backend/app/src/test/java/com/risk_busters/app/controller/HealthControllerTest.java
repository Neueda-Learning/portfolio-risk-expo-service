package com.risk_busters.app.controller;

import com.risk_busters.app.dto.HealthReadinessResponseDTO;
import com.risk_busters.app.service.LimitService;
import com.risk_busters.app.service.PortfolioRiskService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class HealthControllerTest {

    @Test
    void readinessReturnsUpWhenDatabaseAndControllerChecksPass() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        HandlerMethod healthHandlerMethod = mock(HandlerMethod.class);
        HandlerMethod portfolioHandlerMethod = mock(HandlerMethod.class);
        HandlerMethod limitHandlerMethod = mock(HandlerMethod.class);

        HealthController controller = new HealthController(dataSource, applicationContext, handlerMapping);
        PortfolioController portfolioController = new PortfolioController(mock(PortfolioRiskService.class));
        LimitController limitController = new LimitController(mock(LimitService.class));

        Map<String, Object> controllers = new LinkedHashMap<>();
        controllers.put("healthController", controller);
        controllers.put("portfolioController", portfolioController);
        controllers.put("limitController", limitController);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenReturn(statement);
        when(statement.execute()).thenReturn(true);

        doReturn(HealthController.class).when(healthHandlerMethod).getBeanType();
        doReturn(PortfolioController.class).when(portfolioHandlerMethod).getBeanType();
        doReturn(LimitController.class).when(limitHandlerMethod).getBeanType();

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
        handlerMethods.put(null, healthHandlerMethod);
        handlerMethods.put(mock(RequestMappingInfo.class), portfolioHandlerMethod);
        handlerMethods.put(mock(RequestMappingInfo.class), limitHandlerMethod);
        when(handlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        setPrivateApplicationName(controller);

        ResponseEntity<HealthReadinessResponseDTO> response = controller.readiness();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().getStatus());
        assertEquals("UP", response.getBody().getChecks().get("application"));
        assertEquals("UP", response.getBody().getChecks().get("controllers"));
        assertEquals("UP", response.getBody().getChecks().get("database"));
    }

    @Test
    void readinessReturnsDownWhenControllerMappingsAreMissing() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        HandlerMethod healthHandlerMethod = mock(HandlerMethod.class);

        HealthController controller = new HealthController(dataSource, applicationContext, handlerMapping);
        PortfolioController portfolioController = new PortfolioController(mock(PortfolioRiskService.class));
        LimitController limitController = new LimitController(mock(LimitService.class));

        Map<String, Object> controllers = new LinkedHashMap<>();
        controllers.put("healthController", controller);
        controllers.put("portfolioController", portfolioController);
        controllers.put("limitController", limitController);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenReturn(statement);
        when(statement.execute()).thenReturn(true);

        doReturn(HealthController.class).when(healthHandlerMethod).getBeanType();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
        handlerMethods.put(null, healthHandlerMethod);
        when(handlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        setPrivateApplicationName(controller);

        ResponseEntity<HealthReadinessResponseDTO> response = controller.readiness();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DOWN", response.getBody().getStatus());
        assertEquals("UP", response.getBody().getChecks().get("application"));
        assertEquals("DOWN", response.getBody().getChecks().get("controllers"));
        assertEquals("UP", response.getBody().getChecks().get("database"));
    }

    @Test
    void readinessReturnsDownWhenDatabaseCheckFails() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        HandlerMethod healthHandlerMethod = mock(HandlerMethod.class);

        HealthController controller = new HealthController(dataSource, applicationContext, handlerMapping);
        Map<String, Object> controllers = new LinkedHashMap<>();
        controllers.put("healthController", controller);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(controllers);
        doReturn(HealthController.class).when(healthHandlerMethod).getBeanType();

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
        handlerMethods.put(null, healthHandlerMethod);
        when(handlerMapping.getHandlerMethods()).thenReturn(handlerMethods);
        when(dataSource.getConnection()).thenThrow(new SQLException("Database unavailable"));

        setPrivateApplicationName(controller);

        ResponseEntity<HealthReadinessResponseDTO> response = controller.readiness();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DOWN", response.getBody().getStatus());
        assertEquals("UP", response.getBody().getChecks().get("application"));
        assertEquals("UP", response.getBody().getChecks().get("controllers"));
        assertEquals("DOWN", response.getBody().getChecks().get("database"));
    }

    private void setPrivateApplicationName(HealthController controller) throws Exception {
        var field = HealthController.class.getDeclaredField("applicationName");
        field.setAccessible(true);
        field.set(controller, "portfolio-risk-expo-service");
    }
}

