package com.example.graphqlshop.common;

public class GraphqlBadRequestException extends RuntimeException {
    public GraphqlBadRequestException(String message) {
        super(message);
    }
}
