/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

public class ChestLidController {
    private boolean shouldBeOpen;
    private float openness;
    private float oOpenness;

    public void tickLid() {
        this.oOpenness = this.openness;
        float f = 0.1f;
        if (!this.shouldBeOpen && this.openness > 0.0f) {
            this.openness = Math.max(this.openness - 0.1f, 0.0f);
        } else if (this.shouldBeOpen && this.openness < 1.0f) {
            this.openness = Math.min(this.openness + 0.1f, 1.0f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public float getOpenness(float f) {
        return Mth.lerp(f, this.oOpenness, this.openness);
    }

    public void shouldBeOpen(boolean bl) {
        this.shouldBeOpen = bl;
    }
}

