package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {
    private String state;
    private Object res;
    private String error;

    public Response() {

    }

    public Response(String state, Object res, String error) {
        this.state = state;
        this.res = res;
        this.error = error;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("res")
    public Object getRes() {
        return res;
    }

    public void setRes(Object res) {
        this.res = res;
    }

    @JsonProperty("error")
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
