package com.xunlei.util;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 曾东
 * @since 2012-5-29 下午12:28:04
 */
public class NumberStringUtil {

    // Constants used by factory methods to specify a style of format.
    public static final int NUMBERSTYLE = 0;
    public static final int CURRENCYSTYLE = 1;
    public static final int PERCENTSTYLE = 2;
    // public static final int SCIENTIFICSTYLE = 3; // 暂时不支持
    public static final int INTEGERSTYLE = 4;

    public static class NumberFormatFacotryConfig {

        // @formatter:off
        private Locale desiredLocale = Locale.getDefault();
        private int choice = NUMBERSTYLE;

        private boolean groupingUsed = true;
        private boolean parseIntegerOnly = false;
        private int maximumIntegerDigits = 40;
        private int minimumIntegerDigits = 1;
        private int maximumFractionDigits = 3; // invariant, >= minFractionDigits
        private int minimumFractionDigits = 0;
        private String invalidNumberFormat = "";
        // @formatter:on

        private NumberFormatFacotryConfig(int choice) {
            this.choice = choice;
        }

        public static NumberFormatFacotryConfig number() {
            return new NumberFormatFacotryConfig(NUMBERSTYLE);
        }

        public static NumberFormatFacotryConfig currency() {
            return new NumberFormatFacotryConfig(CURRENCYSTYLE);
        }

        public static NumberFormatFacotryConfig percent() {
            return new NumberFormatFacotryConfig(PERCENTSTYLE);
        }

        // public static NumberFormatFacotryConfig scientific() {
        // return new NumberFormatFacotryConfig(SCIENTIFICSTYLE);
        // }

        public static NumberFormatFacotryConfig integer() {
            return new NumberFormatFacotryConfig(INTEGERSTYLE);
        }

        public NumberFormatFacotryConfig locale(Locale locale) {
            this.desiredLocale = locale;
            return this;
        }

        public NumberFormatFacotryConfig groupingUsed(boolean groupingUsed) {
            this.groupingUsed = groupingUsed;
            return this;
        }

        public NumberFormatFacotryConfig parseIntegerOnly(boolean parseIntegerOnly) {
            this.parseIntegerOnly = parseIntegerOnly;
            return this;
        }

        public NumberFormatFacotryConfig maximumIntegerDigits(int maximumIntegerDigits) {
            this.maximumIntegerDigits = maximumIntegerDigits;
            return this;
        }

        public NumberFormatFacotryConfig minimumIntegerDigits(int minimumIntegerDigits) {
            this.minimumIntegerDigits = minimumIntegerDigits;
            return this;
        }

        public NumberFormatFacotryConfig maximumFractionDigits(int maximumFractionDigits) {
            this.maximumFractionDigits = maximumFractionDigits;
            return this;
        }

        public NumberFormatFacotryConfig minimumFractionDigits(int minimumFractionDigits) {
            this.minimumFractionDigits = minimumFractionDigits;
            return this;
        }

        public NumberFormatFacotryConfig invalidNumberFormat(String invalidNumberFormat) {
            this.invalidNumberFormat = invalidNumberFormat;
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + choice;
            result = prime * result + ((desiredLocale == null) ? 0 : desiredLocale.hashCode());
            result = prime * result + (groupingUsed ? 1231 : 1237);
            result = prime * result + ((invalidNumberFormat == null) ? 0 : invalidNumberFormat.hashCode());
            result = prime * result + maximumFractionDigits;
            result = prime * result + maximumIntegerDigits;
            result = prime * result + minimumFractionDigits;
            result = prime * result + minimumIntegerDigits;
            result = prime * result + (parseIntegerOnly ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NumberFormatFacotryConfig other = (NumberFormatFacotryConfig) obj;
            if (choice != other.choice)
                return false;
            if (desiredLocale == null) {
                if (other.desiredLocale != null)
                    return false;
            } else if (!desiredLocale.equals(other.desiredLocale))
                return false;
            if (groupingUsed != other.groupingUsed)
                return false;
            if (invalidNumberFormat == null) {
                if (other.invalidNumberFormat != null)
                    return false;
            } else if (!invalidNumberFormat.equals(other.invalidNumberFormat))
                return false;
            if (maximumFractionDigits != other.maximumFractionDigits)
                return false;
            if (maximumIntegerDigits != other.maximumIntegerDigits)
                return false;
            if (minimumFractionDigits != other.minimumFractionDigits)
                return false;
            if (minimumIntegerDigits != other.minimumIntegerDigits)
                return false;
            if (parseIntegerOnly != other.parseIntegerOnly)
                return false;
            return true;
        }
    }

    /**
     * 数字格式工厂
     */
    private final ThreadLocal<NumberFormat> numberFormatFactory;
    private final NumberFormatFacotryConfig config;

    private NumberStringUtil(final NumberFormatFacotryConfig c) {
        this.config = c;
        this.numberFormatFactory = new ThreadLocal<NumberFormat>() {

            @Override
            protected NumberFormat initialValue() {
                NumberFormat f = NumberFormat.getPercentInstance();
                switch (c.choice) {
                case NUMBERSTYLE:
                    f = NumberFormat.getNumberInstance(c.desiredLocale);
                    break;
                case CURRENCYSTYLE:
                    f = NumberFormat.getCurrencyInstance(c.desiredLocale);
                    break;
                case PERCENTSTYLE:
                    f = NumberFormat.getPercentInstance(c.desiredLocale);
                    break;
                // case SCIENTIFICSTYLE:
                case INTEGERSTYLE:
                    f = NumberFormat.getIntegerInstance(c.desiredLocale);
                    break;
                }
                f.setGroupingUsed(c.groupingUsed);
                f.setMaximumFractionDigits(c.maximumFractionDigits);
                f.setMaximumIntegerDigits(c.maximumIntegerDigits);
                f.setMinimumFractionDigits(c.minimumFractionDigits);
                f.setMinimumIntegerDigits(c.minimumIntegerDigits);
                f.setParseIntegerOnly(c.parseIntegerOnly);
                return f;
            }
        };
    }

    private static final ConcurrentHashMap<NumberFormatFacotryConfig, NumberStringUtil> allNumberStringUtil = new ConcurrentHashMap<NumberFormatFacotryConfig, NumberStringUtil>(2);
    public static final NumberStringUtil DEFAULT_PERCENT = getInstance(NumberFormatFacotryConfig.percent().maximumFractionDigits(2));

    public String format(double v) {
        return numberFormatFactory.get().format(v);
    }

    public String formatByDivide(double a, double b) {
        if (b == 0)
            return config.invalidNumberFormat;
        return numberFormatFactory.get().format(a / b);
    }

    public static void main(String[] args) {
        System.out.println(DEFAULT_PERCENT.formatByDivide(2, 3));
        System.out.println(DEFAULT_PERCENT.formatByDivide(2322323, 3223213223L));
    }

    public static NumberStringUtil getInstance(NumberFormatFacotryConfig config) {
        NumberStringUtil psu = allNumberStringUtil.get(config);
        if (psu == null) {
            psu = new NumberStringUtil(config);
            allNumberStringUtil.putIfAbsent(config, psu);
        }
        return psu;
    }
}
