package com.example.graphqlshop.common;

public class GraphqlNotFoundException extends RuntimeException {
    public GraphqlNotFoundException(String message) {
        super(message);
    }
}
