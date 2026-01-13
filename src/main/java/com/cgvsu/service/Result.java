package com.cgvsu.service;

//Возможно откажусь от этого подхода и оставлю try catch
public class Result {
    private final ResultState state;
    private final String message;

    public String getMessage(){
        return message;
    }

    public ResultState getState(){
        return state;
    }

    public Result(ResultState state, String message) {
        this.state = state;
        this.message = message;
    }
}
