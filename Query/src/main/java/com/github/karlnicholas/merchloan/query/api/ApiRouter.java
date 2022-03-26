package com.github.karlnicholas.merchloan.query.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
public class ApiRouter implements WebFluxConfigurer {

    @Bean
    public RouterFunction<ServerResponse> monoRouterFunction(ApiHandler apiHandler) {

        return RouterFunctions
                .nest(path("/api/query").and(accept(MediaType.APPLICATION_JSON)),
                        route(GET("/request/{id}"), apiHandler::getRequest)
                                .andRoute(GET("/account/{id}"), apiHandler::getAccount)
                                .andRoute(GET("/loan/{id}"), apiHandler::getLoan)
                                .andRoute(GET("/statement/{id}"), apiHandler::getStatement)
                                .andRoute(GET("/statements/{id}"), apiHandler::getStatements)
                                .andRoute(GET("/checkrequests/{businessDate}"), apiHandler::getCheckRequests)
                );

    }
}