package com.github.karlnicholas.merchloan.query.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
public class ApiRouter implements WebFluxConfigurer {

    @Bean
    public RouterFunction<ServerResponse> monoRouterFunction(ApiHandler apiHandler) {

        return RouterFunctions
                .nest(path("/api/query").and(accept(APPLICATION_JSON)),
                        route(GET("/id/{id}"), apiHandler::getId)
                                .andRoute(GET("/account/{id}"), apiHandler::getAccount)
                                .andRoute(GET("/lender/{id}"), apiHandler::getLender)
                                .andRoute(GET("/lenderbyname"), apiHandler::getLenderLender)
                                .andRoute(GET("/loan/{id}"), apiHandler::getLoan)
                                .andRoute(GET("/ledger/{id}"), apiHandler::getLedger)
                );

    }

}