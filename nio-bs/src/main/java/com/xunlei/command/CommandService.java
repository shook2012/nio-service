package com.xunlei.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import com.xunlei.util.DateUtil;
import com.xunlei.util.Log;
import com.xunlei.util.StringTools;

/**
 * ��������࣬��Ҫ��������ִ�С���ء��ж�
 * 
 * @author ����
 * @since 2009-1-4 ����03:56:27
 */
public class CommandService {

    /**
     * ����ִ�н����ģʽ
     * 
     * @author ZengDong
     */
    protected enum CmdMode {
        /**
         * ʹ�ø����ݵķ�ʽ����ִ�н�������Դ���ˢ�µ�ǰ�е����
         */
        compatible,
        /**
         * ������ִ�н��
         */
        ignore,
        /**
         * ʹ�ü򵥷�ʽ����ִ�н��
         */
        simple;
    }

    /**
     * ���ڼ�� ͨ���ж� ��ǰִ�������Ƿ��������,�������Ƿ��ж��������
     * 
     * @author ZengDong
     */
    protected class CmdMonitor {

        /**
         * �Ƿ����ύ�ж�
         */
        private boolean interrupt;
        /**
         * ����ж�ʱ�ύ����Ϣ
         */
        private String interuptedMsg = "";
        /**
         * ���ʱ�ύ�жϵ���Ϣ
         */
        private String interuptMsg = "";
        /**
         * �ϴλ�������ʱ��
         */
        protected long lastProcessTime;
        /**
         * �ϴλ�������ʱ��
         */
        protected String lastProcessTimeStr;
        /**
         * ������������
         */
        private Process process;
        /**
         * ִ��execute()����ʹ�õ��߳�
         */
        @SuppressWarnings("unused")
        private Thread processThread;
        @SuppressWarnings("unused")
        private BufferedReader reader;

        /**
         * ��������췽��
         * 
         * @param process �����
         * @param reader
         */
        public CmdMonitor(Process process, BufferedReader reader) {
            this.process = process;
            this.processThread = Thread.currentThread();
            lastProcessTime = System.currentTimeMillis();
            this.reader = reader;
            lastProcessTimeStr = now();
        }

        /**
         * ִ���жϣ��ж���ϢΪmsg
         * 
         * @param msg �ж���Ϣ
         */
        public void interrupt(String msg) {
            interrupt = true;
            this.interuptMsg = MessageFormat.format("EXECUTE {0} - currentTime:{1},lastProcessTime:{2}\n", msg, now(), lastProcessTimeStr);

            // try { // TODO:processThread�᲻���Ѿ���ִ������worker��cmd��?
            // processThread.interrupt();
            // } catch (Exception e) {
            // log.error("try to interrupt thread encount exception", e);
            // }
            // try {// ͬʱҲ�ر�reader
            // reader.close();
            // } catch (IOException e) {
            // log.error("try to close reader encount exception", e);
            // }

            try {
                process.getInputStream().close();
            } catch (IOException e) {
                log.error("try to close reader encount exception", e);
            }
            try {
                process.destroy();
            } catch (Exception e) {
                log.error("try to destory process encount exception", e);
            }
        }

        /**
         * ��ص�ǰ�����Ƿ񳬹���ָ����ʱ�䣬��������ж�
         * 
         * @param tolerantSec ָ����ʱ��
         * @return
         */
        private boolean monitor(int tolerantSec) {
            if (System.currentTimeMillis() - lastProcessTime > tolerantSec * 1000) {
                interrupt("TIMEOUT(>" + tolerantSec + ")");
                return true;
            }
            return false;
        }

        /**
         * ��õ�ǰʱ��
         * 
         * @return
         */
        public String now() {
            return DateUtil.UNSAFE_DF_DEFAULT.format(new Date());
        }

        /**
         * ���������
         */
        @Override
        public String toString() {
            return interuptedMsg + interuptMsg;
        }

        /**
         * �����ϴλ���������ʱ��
         */
        public void updateLastProcessTime() {
            lastProcessTime = System.currentTimeMillis();
            lastProcessTimeStr = now();
        }
    }

    private final static Logger log = Log.getLogger();
    /**
     * ��ǰ����ϵͳ������
     */
    private static final int OS_TYPE = getOsType();

    /**
     * ��õ�ǰ����ϵͳ�ı�ţ�0Ϊlinux��1Ϊwindows
     * 
     * @return
     */
    private static int getOsType() {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            return 0;
        } else if (osName.startsWith("Windows")) {
            return 1;// �������ϵ�����win98,win95
        }
        return -1;
    }

    /**
     * ����������л���
     */
    private StringBuilder _readLineBuffer = new StringBuilder();

    private int _readLineRemainChar = -1;
    /**
     * cmd����ģʽ,Ĭ��ʹ�ü��ݷ�ʽ
     */
    protected CmdMode cmdMode = CmdMode.compatible;
    /**
     * cmd���
     */
    protected CmdMonitor cmdMonitor;
    /**
     * Ҫ����ִ�е���������
     */
    protected String[] commandArray;
    /**
     * Ҫ����ִ�е��������� �� �ַ�����ʾ
     */
    protected String commandArrayStr;
    /**
     * ��������ʱ�� ���ַ�����ʾ
     */
    protected String createTime = DateUtil.UNSAFE_DF_DEFAULT.format(new Date()) + " ";
    /**
     * ��ǰ����ˢ�µ���(�ᱻ��һ�и���)
     */
    protected String flushingLine = "";
    /**
     * ��ǰִ�е����Ƿ�Ҫ����ˢ(����һ�лس�����)
     */
    protected boolean flushingProcessingLing;
    /**
     * �Ƿ�ִ����
     */
    protected boolean processComplete;
    /**
     * ִ���о�����
     */
    protected StringBuilder processingDetail = new StringBuilder();
    /**
     * ִ���е�ǰ��ȡ����
     */
    protected String processingLine;
    /**
     * ��ǰִ�е���������ȡһ��ʱ���Զ��ۼ�
     */
    protected int processingLineIndex = 0;
    /**
     * ִ�н��
     */
    protected boolean success = false;
    /**
     * �Ƿ�ʹ��linux��bin/sh��ִ�е�ǰ���� ��������Ҫ��Ϊ�˽��: Java����ʹ��Runtime.exec�Ա��س�����ý����ض�����������������ض�����߹ܵ�����������ý����������� ע��:��Java�е��ñ��س�����ƻ�ƽ̨�����Թ��� �����ؼ���:ʹ��Runtime.exec�ض��򱾵س������
     * http://hi.baidu.com/javaroad/blog/item/a56d74e7ce7fba28b8382053.html
     */
    private boolean useShell = false;

    /**
     * ����[/bin/sh,-c,cmd�ַ���]��ʽִ������
     */
    public CommandService(String commandString) {
        this(commandString, true);
    }

    /**
     * �޷������ڴ�ʱ�Ĵ������
     */
    public static CantAllocateMemErrorHandler cantAllocateMemErrorHandler;

    /**
     * <pre>
     * ����cmd�ַ���
     * 1.userShell = true,�����linuxϵͳ������[/bin/sh,-c,cmd�ַ���]
     * 2.userShell = false,��������Ԫ
     * </pre>
     * 
     * @param commandString �������е��ַ�����ʾ
     * @param useShell �Ƿ�ʹ��Shell
     */
    public CommandService(String commandString, boolean useShell) {
        this.useShell = useShell;
        if (this.useShell) {
            this.commandArray = buildOsSpcArgs(commandString);
        } else {
            this.commandArray = buildArgs(commandString);
        }
        init();
    }

    /**
     * ֱ�Ӵ���cmd[]
     */
    public CommandService(String[] command) {
        this.commandArray = command;
        this.useShell = false;
        init();
    }

    /**
     * ���ַ�������ת��Ϊ�����ʽ������
     * 
     * @param commandString Ҫת�����ַ�������
     * @return
     */
    private String[] buildArgs(String commandString) {
        StringTokenizer st = new StringTokenizer(commandString);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            cmdarray[i] = st.nextToken();
        }
        return cmdarray;
    }

    /**
     * ���ݲ���ϵͳ�Ĳ�ͬ���ַ�������ת��Ϊ�����ʽ������
     * 
     * @param commandString Ҫת�����ַ�������
     * @return
     */
    private String[] buildOsSpcArgs(String commandString) {
        switch (OS_TYPE) {
        case 0:
            String[] tmp = { "/bin/sh", "-c", commandString };
            return tmp;
        case 1:
            String[] tmp1 = { "cmd.exe", "/c", commandString };
            return tmp1;
        }
        return null;
    }

    /**
     * ��鵱ǰ�����Ƿ�Ҫ ׼��״̬,�����Ѿ�ִ����
     */
    public void checkIsPreparing() {
        if (processComplete) {
            throw new IllegalStateException("cmd is already completed:" + commandArrayStr);
        }
    }

    /**
     * �����ͬ�������vo
     */
    public CommandService copy() {
        CommandService cmd = new CommandService(this.commandArray);
        cmd.useShell = this.useShell;
        return cmd;
    }

    /**
     * ִ�е�ǰ����
     * 
     * @return
     */
    public CommandService execute() {
        return execute("UTF-8");
    }

    public CommandService execute(String charset) {
        return execute("UTF-8", log);
    }

    /**
     * ִ�е�ǰ����
     * 
     * @param charset ����ı����ʽ
     * @return
     */
    public synchronized CommandService execute(String charset, Logger log) {
        checkIsPreparing();
        try {
            if (StringTools.isEmpty(charset)) {
                charset = "UTF-8";
            }
            log.info("RUN CMD:{} ({})", commandArrayStr, cmdMode);
            Process process = new ProcessBuilder(commandArray).redirectErrorStream(true).start();
            BufferedReader reader = null;
            try {
                if (cmdMode != CmdMode.ignore) {
                    InputStreamReader innerIs = new InputStreamReader(process.getInputStream(), charset);

                    if (cmdMode == CmdMode.simple) {
                        LineNumberReader lr = new LineNumberReader(innerIs);
                        reader = lr;
                        cmdMonitor = new CmdMonitor(process, reader);

                        while ((processingLine = readLineSimple(lr)) != null) {
                            cmdMonitor.updateLastProcessTime();
                            processLine();
                            if (!flushingProcessingLing) {
                                processingLineIndex++;
                                // lineReader.getLineNumber()
                                // ���Է���lineReader.getLineNumber()��ʵ��������Ҫ��־��processingLineIndex
                            }
                        }
                    } else {
                        reader = new BufferedReader(innerIs);
                        cmdMonitor = new CmdMonitor(process, reader);
                        // ��windowsƽ̨�ϣ����б����ó����DOS�����ڳ���ִ����Ϻ������������Զ��رգ��Ӷ�����JavaӦ�ó���������waitfor()��
                        // ���¸������һ�����ܵ�ԭ���ǣ��ÿ�ִ�г���ı�׼����Ƚ϶࣬�����д��ڵı�׼���������������
                        // ����İ취�ǣ�����Java�ṩ��Process���ṩ�ķ�����Java������ػ񱻵��ó����DOS���д��ڵı�׼�����
                        // ��waitfor()����֮ǰ�������ڵı�׼����������е����ݡ�
                        while ((processingLine = readLineCompatible(reader)) != null) {
                            cmdMonitor.updateLastProcessTime();
                            processLine();
                            if (!flushingProcessingLing) {
                                processingLineIndex++;
                            }
                        }
                    }
                }

                int existCode = process.waitFor();// ����һֱ�����ȴ����

                if (success = existCode == 0) {
                    success = true;
                    log.info("RUN CMD OK:{}", commandArrayStr);
                } else {
                    log.error("RUN CMD ERR:{},CODE:{}", commandArrayStr, existCode);
                }
            } catch (Exception e) {
                if (isInterrupt()) {
                    cmdMonitor.interuptedMsg = "[INTERRUPTED(" + e.getClass().getSimpleName() + ")]";
                    log.error("RUN CMD {}:{}", new Object[] { cmdMonitor, commandArrayStr, e });
                } else {
                    log.error("RUN CMD EXCEPTION:{}", commandArrayStr, e);
                }
            } finally {
                processComplete = true;
                if (process != null) {
                    try {
                        reader.close();
                        process.getInputStream().close();
                    } catch (Exception e2) {
                    }
                    process.destroy();
                }
            }
        } catch (IOException e) {
            log.error("RUN CMD ERR:{}", commandArrayStr, e);
            String error = e.toString().toLowerCase();
            if (cantAllocateMemErrorHandler != null && error.contains("cannot allocate memory")) {
                log.error("\n====Waiting to resolve the fatal Exception====\n");
                cantAllocateMemErrorHandler.handleCantAllocateMemError(e);
            }
        }
        return this;
    }

    /**
     * �������ģʽ
     * 
     * @return
     */
    public CmdMode getCmdMode() {
        return cmdMode;
    }

    /**
     * ����������е��ַ�����ʾ
     * 
     * @return
     */
    public String getCommandArrayStr() {
        return commandArrayStr;
    }

    /**
     * ���ִ���еĽ��
     * 
     * @return
     */
    public StringBuilder getProcessingDetail() {
        return processingDetail;
    }

    /**
     * ��ʼ����������������ʽת��Ϊ�ַ�����ʽ��������successΪfalse
     */
    private void init() {
        commandArrayStr = Arrays.toString(commandArray);
        success = false;
    }

    /**
     * �жϵ�ǰ����
     * 
     * @param interuptMsg
     */
    public void interrupt(String interuptMsg) {
        if (cmdMode == CmdMode.ignore) {
            throw new IllegalAccessError("cant interrupt cmd with [ignore] CmdMode:" + commandArrayStr);
        }
        if (cmdMonitor == null) {
            throw new IllegalStateException("cmd isnot started:" + commandArrayStr);
        }
        cmdMonitor.interrupt(interuptMsg);
    }

    /**
     * ��ǰ�����Ƿ��ж�
     * 
     * @return
     */
    public boolean isInterrupt() {
        if (cmdMonitor == null) {
            return false;
        }
        return cmdMonitor.interrupt;
    }

    /**
     * ��ǰ�����Ƿ������
     * 
     * @return
     */
    public boolean isProcessComplete() {
        return processComplete;
    }

    /**
     * ��ǰ�����Ƿ�ִ�гɹ�
     * 
     * @return
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * ��ص�ǰ�����Ƿ񳬹���ָ����ʱ��
     * 
     * @param tolerantSec ָ�����ж�ʱ��
     * @return
     */
    public boolean monitor(int tolerantSec) {
        if (cmdMonitor == null) {
            return false;
        }
        return cmdMonitor.monitor(tolerantSec);
    }

    protected void processLine() {
    }

    /**
     * ���Լ�����ǰ�������Ƿ��� ֻ�� /r��β��(Ҳ���� ��ǰ�лᱻ��һ�и���)
     * 
     * @param br ������
     * @return
     * @throws IOException
     */
    private String readLineCompatible(BufferedReader br) throws IOException {
        _readLineBuffer.delete(0, _readLineBuffer.length());
        int readingChar = -1;
        int lastChar = -1;
        if (_readLineRemainChar != -1) {
            lastChar = _readLineRemainChar;
            if (_readLineRemainChar != 13 && _readLineRemainChar != 10) {
                _readLineBuffer.append((char) _readLineRemainChar);
            }
        }
        flushingProcessingLing = false;
        while ((readingChar = br.read()) != -1) {
            char c = (char) readingChar;
            updateLastProcessTimeForChar(c);
            if (readingChar == 10) {// \n
                if (lastChar == 13) { // \r\n
                    _readLineRemainChar = -1;
                    break;
                } else if (lastChar == 10) { // \n\n
                    _readLineRemainChar = readingChar;
                    break;
                } else {
                    lastChar = readingChar;
                    continue;
                }
            } else if (readingChar == 13) {// \r
                if (lastChar == 10) {// \n\r
                    _readLineRemainChar = -1;
                    break;
                } else {// \r\r �� A\r
                    lastChar = readingChar;
                    continue;
                }
            } else {
                if (lastChar == 10) {// ��A����/n
                    _readLineRemainChar = readingChar;
                    break;
                } else if (lastChar == 13) {// ��A����/r,˵����ǰ����Ҫ��ˢ�¸��ǵ�
                    _readLineRemainChar = readingChar;
                    flushingProcessingLing = true;
                    break;
                } else {
                    _readLineBuffer.append(c);
                    continue;
                }
            }
        }
        if (readingChar == -1) {
            if (_readLineBuffer.length() == 0) {
                return null;
            } else {
                _readLineRemainChar = -1;
            }
        }
        String result = _readLineBuffer.toString();
        if (!flushingProcessingLing) {
            processingDetail.append(result).append('\n');
            flushingLine = "";
        } else {
            flushingLine = result;
        }
        return result;
    }

    /**
     * �򵥵ش���ǰ�����
     * 
     * @param lineReader �����ݶ�ȡ��
     * @return
     * @throws IOException
     */
    private String readLineSimple(LineNumberReader lineReader) throws IOException {
        // _readLineBuffer= null;
        // flushingLine = "";
        // flushingProcessingLing = false;
        processingLine = lineReader.readLine();
        processingDetail.append(processingLine).append('\n');
        return processingLine;
    }

    /**
     * ��������ģʽ
     * 
     * @param cmdMode
     */
    public void setCmdMode(CmdMode cmdMode) {
        checkIsPreparing();
        this.cmdMode = cmdMode;
    }

    /**
     * Ĭ�ϴ�ӡ������Ϣ
     */
    @Override
    public String toString() {
        return toString(0);
    }

    /**
     * ��ô�ӡ��Ϣ
     * 
     * @param type 0-��ӡ����,1-ֻ��ӡ�����Լ����һ��,2-ֻ��ӡ����
     * @return Ҫ��ӡ����Ϣ
     */
    public String toString(int type) {
        // if (commandString == null)
        // return createTime + Arrays.toString(commandArray);
        // return createTime + commandString;

        StringBuilder tmp = new StringBuilder();
        if (!isProcessComplete()) {
            long span = cmdMonitor == null ? 0 : System.currentTimeMillis() - cmdMonitor.lastProcessTime;// �����ϴν��յ���Ϣ��ʱ��
            tmp.append(">>>(").append(span).append("MS)");
        }
        tmp.append(createTime);
        tmp.append(commandArrayStr);
        tmp.append('\n');

        if (type == 0) {
            tmp.append(processingDetail);
            tmp.append(flushingLine).append('\n');
        } else if (type == 1) {
            if (processingLine != null) {
                tmp.append(processingLine).append('\n');
            }
        }

        if (cmdMonitor != null) {
            tmp.append(cmdMonitor);
        }
        return tmp.toString();
    }

    /**
     * �����ر����һЩ������д���ͬ��updateʱ���
     * 
     * @param c
     */
    protected void updateLastProcessTimeForChar(char c) {
        // if (c == '-')
        // cmdMonitor.updateLastProcessTime();
    }
}
