package com.xunlei.springutil;

import java.io.File;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.xunlei.logback.LogFormatFactory;
import com.xunlei.util.EmptyChecker;
import com.xunlei.util.Log;
import com.xunlei.util.StringTools;

/**
 * �����ʼ���ģ���࣬��Ҫ�ڳ�ʼ��ʱע��һ��JavaMailSender��ʵ��
 * 
 * @since 2010-12-16
 * @author hujiachao
 */
public class MailTemplate {

    private static Logger log = Log.getLogger();
    private static final LogFormatFactory logformat = LogFormatFactory.getInstance("|");
    /**
     * �ʼ�����ʹ�õ�Ĭ�ϱ��뷽ʽ
     */
    private String defaultCharset = "GBK";
    /**
     * �����������ַ���������Ҫ�ͷ��ʼ����ʺ�ƥ�䣬�����ʼ�������ȥ
     */
    private String fromAddress;
    /**
     * ���������ƣ��� Ѹ�׸�������
     */
    private String fromName = "";
    /**
     * ���Ͳ��ɹ�ʱ��������Դ���
     */
    private int maxRetryTime = 0;
    /**
     * ���Ͳ��ɹ�ʱ�����Լ������λΪ����
     */
    private long retryIdle = 0;
    /**
     * ���Լ���ӱ�ϵͳ
     */
    private int retryIdleFactor = 2;
    /**
     * Spring�ṩ���ʼ�������Ķ���
     */
    private JavaMailSender sender;

    /**
     * ���췽��
     * 
     * @param sender
     * @param fromAddress
     */
    public MailTemplate(JavaMailSender sender, String fromAddress) {
        this.sender = sender;
        this.fromAddress = fromAddress;
    }

    /**
     * ���췽��
     * 
     * @param sender
     * @param fromAddress
     * @param fromName
     * @param defaultCharset
     * @param maxTryTime
     * @param retryIdle
     */
    public MailTemplate(JavaMailSender sender, String fromAddress, String fromName, String defaultCharset, int maxTryTime, long retryIdle) {
        this.sender = sender;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.defaultCharset = defaultCharset;
        this.maxRetryTime = maxTryTime;
        this.retryIdle = retryIdle;
    }

    /**
     * �����ʼ��������岿��
     * 
     * @param to �ռ����б�
     * @param subject �ʼ�����
     * @param content �ʼ���������
     * @param isMime �Ƿ���MIME�ʼ�
     * @param asHtml �ʼ������Ƿ���HTML����
     * @param attachments �����б�
     * @return ���ͳɹ�����trueʧ�ܷ���false
     */
    private boolean send(String[] to, String subject, String content, boolean isMime, boolean asHtml, File... attachments) {
        boolean r = false;
        Exception ex = null;
        String result = "OK";
        long idle = retryIdle;
        long firstNano = System.nanoTime(); // ���ڶ�����������Ψһ�Ա�ʶ
        // ��������Դ�����������
        for (int i = 0; i <= maxRetryTime; i++) {
            try {
                // ���ύ���ʼ���������У��
                if (EmptyChecker.isEmpty(to)) {
                    throw new NullPointerException("To is null");
                }
                if (StringTools.isEmpty(subject)) {
                    throw new NullPointerException("Subject is empty");
                }
                if (StringTools.isEmpty(content)) {
                    throw new NullPointerException("Content is empty");
                }
                // ���Ƿ���MIME�ʼ����зֱ���
                if (isMime) {
                    MimeMessage msg = sender.createMimeMessage();
                    MimeMessageHelper mail = new MimeMessageHelper(msg, true, defaultCharset);
                    if (StringTools.isEmpty(fromName)) {
                        mail.setFrom(fromAddress);
                    } else {
                        mail.setFrom(fromAddress, fromName);
                    }
                    mail.setTo(to);
                    mail.setSubject(subject);
                    mail.setText(content, asHtml);
                    // ����Ƕ���������ʼ���
                    if (null != attachments) {
                        for (File file : attachments) {
                            mail.addInline(file.getName(), file);
                        }
                    }
                    sender.send(msg);
                } else {
                    SimpleMailMessage mail = new SimpleMailMessage();
                    mail.setFrom(fromAddress);
                    mail.setTo(to);
                    mail.setSubject(subject);
                    mail.setText(content);
                    sender.send(mail);
                }
                r = true;
            } catch (Exception e) {
                ex = e;
                result = e.getClass().getName() + ": " + StringTools.removeNewLines(e.getMessage());
            } finally {
                if (r) {
                    Object[] args = { firstNano, result, isMime ? "HTML" : "TEXT", i + 1, to, subject, StringTools.removeNewLines(content) };
                    log.debug(logformat.getFormat(args), args);
                } else {
                    Object[] args = { firstNano, result, isMime ? "HTML" : "TEXT", i + 1, to, subject, StringTools.removeNewLines(content),
                            null == ex ? "OK" : StringTools.removeNewLines(ex.getMessage()) };
                    log.error(logformat.getFormat(args), args);
                }
            }
            if (r || ex instanceof NullPointerException) {
                break;
            }
            try {
                Thread.sleep(idle);
                idle *= retryIdleFactor; // ��һ������ʱ�����ӳ�
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return r;
    }

    /**
     * ����MIME�ʼ�
     * 
     * @param to �ռ��������ַ
     * @param subject �ʼ�����
     * @param content �ʼ���������
     */
    public boolean sendMimeMail(String to, String subject, String content, File... attachments) {
        return send(new String[] { to }, subject, content, true, true, attachments);
    }

    /**
     * ����MIME�ʼ�
     * 
     * @param to �ռ����б�
     * @param subject �ʼ�����
     * @param content �ʼ���������
     */
    public boolean sendMimeMail(String[] to, String subject, String content, File... attachments) {
        return send(to, subject, content, true, true, attachments);
    }

    /**
     * ����MIME�ʼ�
     * 
     * @param to �ռ����б�
     * @param subject �ʼ�����
     * @param content �ʼ���������
     * @param asHtml �ʼ������Ƿ�����HTML��ʽ���ͣ�Ĭ��Ϊtrue�������false������ڷ��ʹ������Ĵ��ı��ʼ�
     */
    public boolean sendMimeMail(String[] to, String subject, String content, boolean asHtml, File... attachments) {
        return send(to, subject, content, true, asHtml, attachments);
    }

    /**
     * ����MIME�ʼ�
     * 
     * @param to �ռ����б�
     * @param subject �ʼ�����
     * @param content �ʼ���������
     * @param asHtml �ʼ������Ƿ�����HTML��ʽ���ͣ�Ĭ��Ϊtrue�������false������ڷ��ʹ������Ĵ��ı��ʼ�
     */
    public boolean sendMimeMail(String to, String subject, String content, boolean asHtml, File... attachments) {
        return send(new String[] { to }, subject, content, true, asHtml, attachments);
    }

    /**
     * ���ʹ��ı��ʼ�
     * 
     * @param to �ռ��������ַ
     * @param subject �ʼ�����
     * @param content �ʼ���������
     */
    public boolean sendTextMail(String to, String subject, String content) {
        return send(new String[] { to }, subject, content, false, false);
    }

    /**
     * ���ʹ��ı��ʼ�
     * 
     * @param to �ռ����б�
     * @param subject �ʼ�����
     * @param content �ʼ���������
     */
    public boolean sendTextMail(String[] to, String subject, String content) {
        return send(to, subject, content, false, false);
    }

    /**
     * ����Ĭ�ϵ��ַ�
     * 
     * @param defaultCharset
     */
    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    /**
     * ���÷����˵�ַ
     * 
     * @param fromAddress
     */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    /**
     * ���÷���������
     * 
     * @param fromName
     */
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    /**
     * ����������Դ���
     * 
     * @param maxRetryTime
     */
    public void setMaxRetryTime(int maxRetryTime) {
        this.maxRetryTime = maxRetryTime;
    }

    /**
     * ����������Լ������λ����
     * 
     * @param retryIdle
     */
    public void setRetryIdle(long retryIdle) {
        this.retryIdle = retryIdle;
    }

    /**
     * �������Լ���ӱ�����
     * 
     * @param retryIdleFactor
     */
    public void setRetryIdleFactor(int retryIdleFactor) {
        this.retryIdleFactor = retryIdleFactor;
    }

    /**
     * ���÷�����
     * 
     * @param sender
     */
    public void setSender(JavaMailSender sender) {
        this.sender = sender;
    }
}
