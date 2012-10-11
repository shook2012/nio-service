package com.xunlei.command;

/**
 * SSH命令类
 * 
 * @author ZengDong
 * @since 2010-9-2 下午08:13:46
 */
public class CommandSsh extends CommandService {

    private boolean first = true;

    /**
     * 构造方法
     * 
     * @param commandString 命令字符串
     */
    public CommandSsh(String commandString) {
        super(commandString);
    }

    /**
     * 构造方法
     * 
     * @param commandString 命令字符串
     * @param useShell 是否使用Shell
     */
    public CommandSsh(String commandString, boolean useShell) {
        super(commandString, useShell);
    }

    /**
     * 构造方法
     * 
     * @param command 命令数组
     */
    public CommandSsh(String[] command) {
        super(command);
    }

    /**
     * 检查是否存在信任关系
     */
    protected void checkIsSshTrust() {
        if (first && processingLine.endsWith("password:")) {
            throw new RuntimeException(processingLine);
        }
        first = false;
    }

    /**
     * 处理一行
     */
    @Override
    protected void processLine() {
        checkIsSshTrust();
    }
}
