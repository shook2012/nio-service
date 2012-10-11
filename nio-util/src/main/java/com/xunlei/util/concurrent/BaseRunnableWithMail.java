//package com.xunlei.util.concurrent;
//
//import com.xunlei.springutil.MailTemplate;
//import com.xunlei.util.EmptyChecker;
//import com.xunlei.util.HttpUtil;
//
///**
// * BaseRunnable抽象类的子类，完成发功邮件的功能
// * 
// * @author ZengDong
// * @since 2011-6-2 下午06:18:04
// */
//public abstract class BaseRunnableWithMail extends BaseRunnable {
//
//    /**
//     * 重写父类的afterProcess方法，在完成父类本身的afterProcess方法后再发送邮件
//     */
//    @Override
//    public void afterProcess() {
//        super.afterProcess();
//        this.sendMail();
//    }
//
//    /**
//     * 获得邮件内容
//     * 
//     * @return 邮件的内容
//     */
//    public String getMailContent() {
//        return this.toString();// TODO:其他更好的信息？
//    }
//
//    /**
//     * 获得邮件的主题
//     * 
//     * @return 邮件主题
//     */
//    public String getMailSubject() {
//        return "[" + HttpUtil.getLocalSampleIP() + "]" + getClass().getSimpleName() + " process result";// TODO:应该打服务器ip等信息
//    }
//
//    /**
//     * 获得Mail模板
//     * 
//     * @return MailTemplate
//     */
//    public abstract MailTemplate getMailTemplate();
//
//    /**
//     * 获得Mail的邮件目的列表
//     * 
//     * @return
//     */
//    public abstract String[] getMailTo();
//
//    /**
//     * 发功邮件
//     */
//    public void sendMail() {
//        String[] mailTo = getMailTo();
//        if (EmptyChecker.isNotEmpty(mailTo)) {
//            getMailTemplate().sendTextMail(getMailTo(), getMailSubject(), getMailContent());
//        }
//    }
//}
