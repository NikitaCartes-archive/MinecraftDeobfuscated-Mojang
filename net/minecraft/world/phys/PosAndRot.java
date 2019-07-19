/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class PosAndRot {
    private final Vec3 pos;
    private final float xRot;
    private final float yRot;

    public PosAndRot(Vec3 vec3, float f, float g) {
        this.pos = vec3;
        this.xRot = f;
        this.yRot = g;
    }

    public Vec3 pos() {
        return this.pos;
    }

    public float xRot() {
        return this.xRot;
    }

    public float yRot() {
        return this.yRot;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        PosAndRot posAndRot = (PosAndRot)object;
        return Float.compare(posAndRot.xRot, this.xRot) == 0 && Float.compare(posAndRot.yRot, this.yRot) == 0 && Objects.equals(this.pos, posAndRot.pos);
    }

    public int hashCode() {
        return Objects.hash(this.pos, Float.valueOf(this.xRot), Float.valueOf(this.yRot));
    }

    public String toString() {
        return "PosAndRot[" + this.pos + " (" + this.xRot + ", " + this.yRot + ")]";
    }
}

