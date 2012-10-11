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

import java.util.Map.Entry;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.CoreConstants;
import com.xunlei.netty.httpserver.component.XLHttpRequest;

/**
 * This class is tied to the <code>fullRequest</code> conversion word.
 * <p>
 * It has been removed from the {@link ch.qos.logback.access.PatternLayout} since it needs further testing before wide use.
 * <p>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class FullRequestConverter extends AccessConverter {

    @Override
    public String convert(IAccessEvent ae) {
        StringBuilder buf = new StringBuilder();
        buf.append(ae.getRequestURL());
        buf.append(CoreConstants.LINE_SEPARATOR);

        XLHttpRequest req = ((AccessEvent) ae).getHttpRequest();
        for (Entry<String, String> e : req.getHeaders()) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(CoreConstants.LINE_SEPARATOR);
        }

        buf.append(CoreConstants.LINE_SEPARATOR);
        buf.append(ae.getRequestContent());
        return buf.toString();
    }

}
