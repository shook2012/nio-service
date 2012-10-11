/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.access.pattern;

import java.text.MessageFormat;
import java.util.List;
import ch.qos.logback.access.spi.IAccessEvent;
import com.xunlei.util.ValueUtil;

/**
 * ��ӡ������Ӧ�ڲ�
 * 
 * @author ZengDong
 * @since 2010-10-12 ����01:54:02
 */
public class SimplifyResponseConverter extends AccessConverter {

    private int num = 1;

    private boolean calcByChar = false;
    private boolean calcByLine = true;
    private boolean printHead = true;
    private boolean printBottom = true;
    private boolean trim = true;

    private String omitReplacerBegin = "...(";
    private String omitReplacerEnd = ")...";

    private String lineFeedReplacer = "||";
    private boolean noLineFeedReplacer = false;

    @SuppressWarnings("rawtypes")
    @Override
    public void start() { // Ĭ�����ã�1,LHB
        // ��0��������ʾ Ҫ��ӡ���ַ�����/����
        num = ValueUtil.getInteger(getFirstOption(), 1);// Ĭ��1��

        List optionList = getOptionList();
        if (optionList != null) {
            // ��1��������ʾ ��ӡģʽ��
            // L - ������num
            // C - ���ַ�������num
            // H - ��ӡͷ��
            // B - ��ӡβ��
            // K - ��trimԭ����
            if (optionList.size() > 1) {
                String mode = ((String) optionList.get(1)).toUpperCase();
                calcByChar = mode.contains("C");
                calcByLine = mode.contains("L");

                boolean printHead1 = mode.contains("H");
                boolean printBottom1 = mode.contains("B");
                if (printHead1 || printBottom1) {
                    this.printBottom = printBottom1;
                    this.printHead = printHead1;
                }
                trim = !mode.contains("K");
            }

            // ��2��������ʾ ���з��ŵ��滻����
            if (optionList.size() > 2) {
                lineFeedReplacer = (String) optionList.get(2);
            }

            // ��3,4��������ʾ �м侫�򲿷ִ����ַ���
            if (optionList.size() > 4) {
                omitReplacerBegin = (String) optionList.get(3); // ��ʼ����
                omitReplacerEnd = (String) optionList.get(4); // ��������
            }
            noLineFeedReplacer = lineFeedReplacer.contains("\n") || lineFeedReplacer.contains("\r");
        }
        addInfo(this.toString());
    }

    private String simplify(String ori) {
        String out = ori;
        if (trim) {
            out = ori.trim(); // ��ȥ�ո�
        }
        int len = out.length();
        if (calcByChar) {
            // ���ж�ԭ�����Ƿ񹻶�,����ȫ����ӡ
            if (noLineFeedReplacer) {// ��������滻���з�,����жϸ���,���ܷ�ֱ�ӷ���
                if (printBottom && printHead) {
                    if (len <= num * 2 + 10) {
                        return out;
                    }
                } else {
                    if (len < num + 10) {
                        return out;
                    }
                }
            }

            // �ж�����̫��,��ʼ����
            StringBuilder buf = new StringBuilder();
            boolean cut = false;
            int lastIndex = 0;
            if (printHead) {
                int currentNum = 0;
                boolean lastCharIsLineFeed = false;
                for (lastIndex = 0; lastIndex < len; lastIndex++) {
                    char c = out.charAt(lastIndex);
                    if (c == '\n' || c == '\r') {
                        if (!lastCharIsLineFeed) {
                            buf.append(lineFeedReplacer);
                        }
                        lastCharIsLineFeed = true;
                        continue;
                    }
                    lastCharIsLineFeed = false;
                    buf.append(c);
                    if (++currentNum >= num) {
                        cut = true;
                        break;
                    }
                }
            }

            int lastInsertIndex = buf.length();

            if (printBottom) {
                cut = false;
                int currentNum = 0;
                boolean lastCharIsLineFeed = false;
                for (int i = len - 1; i > lastIndex; i--) {
                    char c = out.charAt(i);
                    if (c == '\n' || c == '\r') {
                        if (!lastCharIsLineFeed) {
                            buf.insert(lastInsertIndex, lineFeedReplacer);
                        }
                        lastCharIsLineFeed = true;
                        continue;
                    }
                    lastCharIsLineFeed = false;
                    buf.insert(lastInsertIndex, c);
                    if (++currentNum >= num) {
                        cut = true;
                        break;
                    }
                }
            }

            if (cut) {
                buf.insert(lastInsertIndex, omitReplacerEnd);
                buf.insert(lastInsertIndex, len);
                buf.insert(lastInsertIndex, omitReplacerBegin);
            }
            return buf.toString();
        }
        StringBuilder buf = new StringBuilder();
        boolean cut = false;
        // �ȴ�ӡͷ����
        int lastIndex = 0;
        if (printHead) {
            int currentNum = 0;
            boolean lastCharIsLineFeed = false;
            for (lastIndex = 0; lastIndex < len; lastIndex++) {
                char c = out.charAt(lastIndex);
                if (c == '\n' || c == '\r') {
                    if (!lastCharIsLineFeed) {
                        buf.append(lineFeedReplacer);
                        if (++currentNum >= num) {
                            cut = true;
                            break;
                        }
                    }
                    lastCharIsLineFeed = true;
                    continue;
                }
                lastCharIsLineFeed = false;
                buf.append(c);
            }
        }

        int lastInsertIndex = buf.length();
        if (printBottom) {
            cut = false;
            int currentNum = 0;
            boolean lastCharIsLineFeed = false;
            for (int i = len - 1; i > lastIndex; i--) {
                char c = out.charAt(i);
                if (c == '\n' || c == '\r') {
                    if (!lastCharIsLineFeed) {
                        buf.insert(lastInsertIndex, lineFeedReplacer);
                        if (++currentNum >= num) {
                            cut = true; // �����Ѿ��չ��� bottom������,�����о���
                            break;
                        }
                    }
                    lastCharIsLineFeed = true;
                    continue;
                }
                lastCharIsLineFeed = false;
                buf.insert(lastInsertIndex, c);
            }
        }

        if (cut) {
            buf.insert(lastInsertIndex, omitReplacerEnd);
            buf.insert(lastInsertIndex, len);
            buf.insert(lastInsertIndex, omitReplacerBegin);
        }
        return buf.toString();
    }

    public static void main(String[] args) {
        SimplifyResponseConverter sc = new SimplifyResponseConverter();
        sc.calcByChar = true;
        // sc.printBottom = false;
        sc.printHead = false;
        sc.num = 20;
        sc.noLineFeedReplacer = true;
        // sc.trim = false;
        System.out.println(sc);

        String sample = "123456\r\nabcdefghi\r\n\rASDFGHJKL:\n\r";
        StringBuilder sam = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sam.append(sample);
        }
        System.out.println(sc.simplify(sam.toString()));
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "SimplifyResponseConverter [num={0}, calcByChar={1}, calcByLine={2}, printHead={3}, printBottom={4}, notTrim={5}, omitReplacerBegin={6}, omitReplacerEnd={7}, lineFeedReplacer={8}]",
                num, calcByChar, calcByLine, printHead, printBottom, trim, omitReplacerBegin, omitReplacerEnd, lineFeedReplacer);
    }

    @Override
    public String convert(IAccessEvent ae) {
        return simplify(ae.getResponseContent());
    }
}
