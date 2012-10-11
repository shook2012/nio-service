//package com.xunlei.util.concurrent;
//
//import com.xunlei.springutil.MailTemplate;
//import com.xunlei.util.EmptyChecker;
//import com.xunlei.util.HttpUtil;
//
///**
// * BaseRunnable����������࣬��ɷ����ʼ��Ĺ���
// * 
// * @author ZengDong
// * @since 2011-6-2 ����06:18:04
// */
//public abstract class BaseRunnableWithMail extends BaseRunnable {
//
//    /**
//     * ��д�����afterProcess����������ɸ��౾���afterProcess�������ٷ����ʼ�
//     */
//    @Override
//    public void afterProcess() {
//        super.afterProcess();
//        this.sendMail();
//    }
//
//    /**
//     * ����ʼ�����
//     * 
//     * @return �ʼ�������
//     */
//    public String getMailContent() {
//        return this.toString();// TODO:�������õ���Ϣ��
//    }
//
//    /**
//     * ����ʼ�������
//     * 
//     * @return �ʼ�����
//     */
//    public String getMailSubject() {
//        return "[" + HttpUtil.getLocalSampleIP() + "]" + getClass().getSimpleName() + " process result";// TODO:Ӧ�ô������ip����Ϣ
//    }
//
//    /**
//     * ���Mailģ��
//     * 
//     * @return MailTemplate
//     */
//    public abstract MailTemplate getMailTemplate();
//
//    /**
//     * ���Mail���ʼ�Ŀ���б�
//     * 
//     * @return
//     */
//    public abstract String[] getMailTo();
//
//    /**
//     * �����ʼ�
//     */
//    public void sendMail() {
//        String[] mailTo = getMailTo();
//        if (EmptyChecker.isNotEmpty(mailTo)) {
//            getMailTemplate().sendTextMail(getMailTo(), getMailSubject(), getMailContent());
//        }
//    }
//}
