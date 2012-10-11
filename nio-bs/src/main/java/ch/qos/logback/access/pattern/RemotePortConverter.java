package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;

public class RemotePortConverter extends AccessConverter {

    @Override
    public String convert(IAccessEvent accessEvent) {
        return Integer.toString(((AccessEvent) accessEvent).getRemotePort());
    }
}
