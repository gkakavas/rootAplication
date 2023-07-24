package com.example.app.utils.user;

public class Response<T> {
    private T data;

    public Response (T data){
        this.data = data;
    }

    public T getData(){
        return this.data;
    }
}
