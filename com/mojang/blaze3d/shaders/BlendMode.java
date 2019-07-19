/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class BlendMode {
    private static BlendMode lastApplied;
    private final int srcColorFactor;
    private final int srcAlphaFactor;
    private final int dstColorFactor;
    private final int dstAlphaFactor;
    private final int blendFunc;
    private final boolean separateBlend;
    private final boolean opaque;

    private BlendMode(boolean bl, boolean bl2, int i, int j, int k, int l, int m) {
        this.separateBlend = bl;
        this.srcColorFactor = i;
        this.dstColorFactor = j;
        this.srcAlphaFactor = k;
        this.dstAlphaFactor = l;
        this.opaque = bl2;
        this.blendFunc = m;
    }

    public BlendMode() {
        this(false, true, 1, 0, 1, 0, 32774);
    }

    public BlendMode(int i, int j, int k) {
        this(false, false, i, j, i, j, k);
    }

    public BlendMode(int i, int j, int k, int l, int m) {
        this(true, false, i, j, k, l, m);
    }

    public void apply() {
        if (this.equals(lastApplied)) {
            return;
        }
        if (lastApplied == null || this.opaque != lastApplied.isOpaque()) {
            lastApplied = this;
            if (this.opaque) {
                GlStateManager.disableBlend();
                return;
            }
            GlStateManager.enableBlend();
        }
        GlStateManager.blendEquation(this.blendFunc);
        if (this.separateBlend) {
            GlStateManager.blendFuncSeparate(this.srcColorFactor, this.dstColorFactor, this.srcAlphaFactor, this.dstAlphaFactor);
        } else {
            GlStateManager.blendFunc(this.srcColorFactor, this.dstColorFactor);
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof BlendMode)) {
            return false;
        }
        BlendMode blendMode = (BlendMode)object;
        if (this.blendFunc != blendMode.blendFunc) {
            return false;
        }
        if (this.dstAlphaFactor != blendMode.dstAlphaFactor) {
            return false;
        }
        if (this.dstColorFactor != blendMode.dstColorFactor) {
            return false;
        }
        if (this.opaque != blendMode.opaque) {
            return false;
        }
        if (this.separateBlend != blendMode.separateBlend) {
            return false;
        }
        if (this.srcAlphaFactor != blendMode.srcAlphaFactor) {
            return false;
        }
        return this.srcColorFactor == blendMode.srcColorFactor;
    }

    public int hashCode() {
        int i = this.srcColorFactor;
        i = 31 * i + this.srcAlphaFactor;
        i = 31 * i + this.dstColorFactor;
        i = 31 * i + this.dstAlphaFactor;
        i = 31 * i + this.blendFunc;
        i = 31 * i + (this.separateBlend ? 1 : 0);
        i = 31 * i + (this.opaque ? 1 : 0);
        return i;
    }

    public boolean isOpaque() {
        return this.opaque;
    }

    public static int stringToBlendFunc(String string) {
        String string2 = string.trim().toLowerCase(Locale.ROOT);
        if ("add".equals(string2)) {
            return 32774;
        }
        if ("subtract".equals(string2)) {
            return 32778;
        }
        if ("reversesubtract".equals(string2)) {
            return 32779;
        }
        if ("reverse_subtract".equals(string2)) {
            return 32779;
        }
        if ("min".equals(string2)) {
            return 32775;
        }
        if ("max".equals(string2)) {
            return 32776;
        }
        return 32774;
    }

    public static int stringToBlendFactor(String string) {
        String string2 = string.trim().toLowerCase(Locale.ROOT);
        string2 = string2.replaceAll("_", "");
        string2 = string2.replaceAll("one", "1");
        string2 = string2.replaceAll("zero", "0");
        if ("0".equals(string2 = string2.replaceAll("minus", "-"))) {
            return 0;
        }
        if ("1".equals(string2)) {
            return 1;
        }
        if ("srccolor".equals(string2)) {
            return 768;
        }
        if ("1-srccolor".equals(string2)) {
            return 769;
        }
        if ("dstcolor".equals(string2)) {
            return 774;
        }
        if ("1-dstcolor".equals(string2)) {
            return 775;
        }
        if ("srcalpha".equals(string2)) {
            return 770;
        }
        if ("1-srcalpha".equals(string2)) {
            return 771;
        }
        if ("dstalpha".equals(string2)) {
            return 772;
        }
        if ("1-dstalpha".equals(string2)) {
            return 773;
        }
        return -1;
    }
}

