package com.lightframework.util.shell;

public class CommandLine {

    private String command;

    private CommandLine(){}

    public String getCommand() {
        return command;
    }

    public static CommandLine buildForCommand(String command){
        return build(command,false);
    }

    public static CommandLine buildForScript(String script){
        return build(script,true);
    }

    public static CommandLine build(String command,boolean script){
        CommandLine commandLine = new CommandLine();
        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            commandLine.command = "cmd /c " + command.trim();
        }else {
            if(script){
                commandLine.command = "sh " + command.trim();
            }else {
                commandLine.command = command.trim();
            }
        }
        return commandLine;
    }

}
