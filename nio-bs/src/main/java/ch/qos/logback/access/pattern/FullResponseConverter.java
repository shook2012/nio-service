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
import org.jboss.netty.handler.codec.http.HttpResponse;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.CoreConstants;

public class FullResponseConverter extends AccessConverter {

    @Override
    public String convert(IAccessEvent ae) {
        StringBuilder buf = new StringBuilder();

        HttpResponse resp = ((AccessEvent) ae).getHttpResponse();
        buf.append(resp.getProtocolVersion().getText());
        buf.append(' ');
        buf.append(resp.getStatus());
        buf.append(CoreConstants.LINE_SEPARATOR);

        for (Entry<String, String> e : resp.getHeaders()) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(CoreConstants.LINE_SEPARATOR);
        }
        buf.append(CoreConstants.LINE_SEPARATOR);
        buf.append(ae.getResponseContent());
        buf.append(CoreConstants.LINE_SEPARATOR);
        return buf.toString();
    }
}
