package com.github.karlnicholas.merchloan.query.api;

import com.github.karlnicholas.merchloan.query.message.MQProducers;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ApiHandler {
    private final MQProducers mqProducers;

    public ApiHandler(MQProducers mqProducers) {
        this.mqProducers = mqProducers;
    }

    public Mono<ServerResponse> getRequest(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(mqProducers::queryServiceRequest)
                , String.class);
    }

    public Mono<ServerResponse> getAccount(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(mqProducers::queryAccount)
                , String.class);
    }

    public Mono<ServerResponse> getLoan(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(mqProducers::queryLoan)
                , String.class);
    }

    public Mono<ServerResponse> getStatement(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(mqProducers::queryStatement)
                , String.class);
    }

    public Mono<ServerResponse> getStatements(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(UUID.fromString(serverRequest.pathVariable("id"))).map(mqProducers::queryStatements)
                , String.class);
    }

    public Mono<ServerResponse> getCheckRequests() {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                Mono.fromSupplier(mqProducers::queryCheckRequest)
                , Boolean.class);
    }
}
