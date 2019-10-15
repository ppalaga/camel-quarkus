package org.apache.camel.quarkus.component.fhir;

import java.util.function.BooleanSupplier;
import org.eclipse.microprofile.config.ConfigProvider;

public final class FhirFlags {
    private FhirFlags() {
    }

    public static final class Dstu2Enabled implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            return ConfigProvider.getConfig().getOptionalValue("camel.fhir.enable-dstu2", Boolean.class).orElse(Boolean.TRUE);
        }
    }

    public static final class Dstu3Enabled implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            return ConfigProvider.getConfig().getOptionalValue("camel.fhir.enable-dstu3", Boolean.class).orElse(Boolean.TRUE);
        }
    }

    public static final class R4Enabled implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            return ConfigProvider.getConfig().getOptionalValue("camel.fhir.enable-r4", Boolean.class).orElse(Boolean.TRUE);
        }
    }
}