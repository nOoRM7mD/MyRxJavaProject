package com.example.lenovo.myrxjavaproject.model;

/**
 * As every response will have a error node,
 * define the error node in BaseResponse class and extend this class in other models.
 */

public class BaseResponse {
    String error;

    public String getError() {
        return error;
    }
}
