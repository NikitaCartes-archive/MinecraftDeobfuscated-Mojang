/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;

public record ModCheck(Confidence confidence, String description) {
    public static ModCheck identify(String string, Supplier<String> supplier, String string2, Class<?> class_) {
        String string3 = supplier.get();
        if (!string.equals(string3)) {
            return new ModCheck(Confidence.DEFINITELY, string2 + " brand changed to '" + string3 + "'");
        }
        if (class_.getSigners() == null) {
            return new ModCheck(Confidence.VERY_LIKELY, string2 + " jar signature invalidated");
        }
        return new ModCheck(Confidence.PROBABLY_NOT, string2 + " jar signature and brand is untouched");
    }

    public boolean shouldReportAsModified() {
        return this.confidence.shouldReportAsModified;
    }

    public ModCheck merge(ModCheck modCheck) {
        return new ModCheck((Confidence)((Object)ObjectUtils.max((Comparable[])new Confidence[]{this.confidence, modCheck.confidence})), this.description + "; " + modCheck.description);
    }

    public String fullDescription() {
        return this.confidence.description + " " + this.description;
    }

    public static enum Confidence {
        PROBABLY_NOT("Probably not.", false),
        VERY_LIKELY("Very likely;", true),
        DEFINITELY("Definitely;", true);

        final String description;
        final boolean shouldReportAsModified;

        private Confidence(String string2, boolean bl) {
            this.description = string2;
            this.shouldReportAsModified = bl;
        }
    }
}

