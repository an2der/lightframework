package com.lightframework.util.shell;

public class CommandResult {

    CommandResult(){}

    private int exitVal;

    private boolean success = true;

    private String content;

    public int getExitVal() {
        return exitVal;
    }

    public void setExitVal(int exitVal) {
        this.exitVal = exitVal;
    }

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