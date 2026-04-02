package com.lightframework.util.command.application;

public class ApplicationResult {

    ApplicationResult(){}

    private boolean success = true;

    private String content;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}