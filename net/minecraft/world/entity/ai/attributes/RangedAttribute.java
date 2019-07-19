/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.attributes;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.BaseAttribute;
import org.jetbrains.annotations.Nullable;

public class RangedAttribute
extends BaseAttribute {
    private final double minValue;
    private final double maxValue;
    private String importLegacyName;

    public RangedAttribute(@Nullable Attribute attribute, String string, double d, double e, double f) {
        super(attribute, string, d);
        this.minValue = e;
        this.maxValue = f;
        if (e > f) {
            throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
        }
        if (d < e) {
            throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
        }
        if (d > f) {
            throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
        }
    }

    public RangedAttribute importLegacyName(String string) {
        this.importLegacyName = string;
        return this;
    }

    public String getImportLegacyName() {
        return this.importLegacyName;
    }

    @Override
    public double sanitizeValue(double d) {
        d = Mth.clamp(d, this.minValue, this.maxValue);
        return d;
    }
}

