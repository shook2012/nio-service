package com.xunlei.command;

/**
 * 命令工具，主要为使用SSH登录远程机器
 * 
 * @author ZengDong
 * @since 2010-8-9 下午05:15:56
 */
public class CommandUtil {

    /**
     * 使用ssh命令远程调用remote机器命令
     * 
     * @param remoteHost 远程计算机名或者IP
     * @param ori_cmd 原始命令
     * @return
     */
    public static CommandService ssh(String remoteHost, String ori_cmd) {
        String cmd = getSshCmd(remoteHost, ori_cmd);
        return new CommandSsh(cmd);
    }

    /**
     * 根据远程计算机和原始命令来获得ssh命令
     * 
     * @param remoteHost 远程计算机
     * @param ori_cmd 原始命令
     * @return ssh命令
     */
    public static String getSshCmd(String remoteHost, String ori_cmd) {
        return new StringBuilder(5 + ori_cmd.length()).append("ssh ").append(remoteHost).append(" '").append(ori_cmd).append("'").toString();
    }
}
