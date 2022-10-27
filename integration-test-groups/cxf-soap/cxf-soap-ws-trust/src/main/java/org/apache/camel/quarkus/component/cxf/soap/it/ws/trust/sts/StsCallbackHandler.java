package org.apache.camel.quarkus.component.cxf.soap.it.ws.trust.sts;

import java.util.Map;

import org.apache.camel.quarkus.component.cxf.soap.it.ws.trust.common.PasswordCallbackHandler;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(methods = false, fields = false)
public class StsCallbackHandler extends PasswordCallbackHandler {

    public StsCallbackHandler() {
        super(Map.of(
                "mystskey", "stskpass",
                "alice", "clarinet"));
    }
}
