package com.xunlei.command;

/**
 * SSH������
 * 
 * @author ZengDong
 * @since 2010-9-2 ����08:13:46
 */
public class CommandSsh extends CommandService {

    private boolean first = true;

    /**
     * ���췽��
     * 
     * @param commandString �����ַ���
     */
    public CommandSsh(String commandString) {
        super(commandString);
    }

    /**
     * ���췽��
     * 
     * @param commandString �����ַ���
     * @param useShell �Ƿ�ʹ��Shell
     */
    public CommandSsh(String commandString, boolean useShell) {
        super(commandString, useShell);
    }

    /**
     * ���췽��
     * 
     * @param command ��������
     */
    public CommandSsh(String[] command) {
        super(command);
    }

    /**
     * ����Ƿ�������ι�ϵ
     */
    protected void checkIsSshTrust() {
        if (first && processingLine.endsWith("password:")) {
            throw new RuntimeException(processingLine);
        }
        first = false;
    }

    /**
     * ����һ��
     */
    @Override
    protected void processLine() {
        checkIsSshTrust();
    }
}
