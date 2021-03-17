/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public enum MeasurementCategory {
    EVENT_LOOP("eventLoops"),
    MAIL_BOX("mailBoxes");

    private final String name;

    private MeasurementCategory(String string2) {
        this.name = string2;
    }

    public String getName() {
        return this.name;
    }
}

