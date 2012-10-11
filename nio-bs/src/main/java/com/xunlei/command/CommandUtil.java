package com.xunlei.command;

/**
 * ����ߣ���ҪΪʹ��SSH��¼Զ�̻���
 * 
 * @author ZengDong
 * @since 2010-8-9 ����05:15:56
 */
public class CommandUtil {

    /**
     * ʹ��ssh����Զ�̵���remote��������
     * 
     * @param remoteHost Զ�̼����������IP
     * @param ori_cmd ԭʼ����
     * @return
     */
    public static CommandService ssh(String remoteHost, String ori_cmd) {
        String cmd = getSshCmd(remoteHost, ori_cmd);
        return new CommandSsh(cmd);
    }

    /**
     * ����Զ�̼������ԭʼ���������ssh����
     * 
     * @param remoteHost Զ�̼����
     * @param ori_cmd ԭʼ����
     * @return ssh����
     */
    public static String getSshCmd(String remoteHost, String ori_cmd) {
        return new StringBuilder(5 + ori_cmd.length()).append("ssh ").append(remoteHost).append(" '").append(ori_cmd).append("'").toString();
    }
}
