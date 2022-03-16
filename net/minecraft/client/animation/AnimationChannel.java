/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.animation;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public record AnimationChannel(Target target, Keyframe[] keyframes) {

    @Environment(value=EnvType.CLIENT)
    public static interface Target {
        public void apply(ModelPart var1, Vector3f var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Interpolations {
        public static final Interpolation LINEAR = (vector3f, f, keyframes, i, j, g) -> {
            Vector3f vector3f2 = keyframes[i].target();
            Vector3f vector3f3 = keyframes[j].target();
            vector3f.set(Mth.lerp(f, vector3f2.x(), vector3f3.x()) * g, Mth.lerp(f, vector3f2.y(), vector3f3.y()) * g, Mth.lerp(f, vector3f2.z(), vector3f3.z()) * g);
            return vector3f;
        };
        public static final Interpolation CATMULLROM = (vector3f, f, keyframes, i, j, g) -> {
            Vector3f vector3f2 = keyframes[Math.max(0, i - 1)].target();
            Vector3f vector3f3 = keyframes[i].target();
            Vector3f vector3f4 = keyframes[j].target();
            Vector3f vector3f5 = keyframes[Math.min(keyframes.length - 1, j + 1)].target();
            vector3f.set(Mth.catmullrom(f, vector3f2.x(), vector3f3.x(), vector3f4.x(), vector3f5.x()) * g, Mth.catmullrom(f, vector3f2.y(), vector3f3.y(), vector3f4.y(), vector3f5.y()) * g, Mth.catmullrom(f, vector3f2.z(), vector3f3.z(), vector3f4.z(), vector3f5.z()) * g);
            return vector3f;
        };
    }

    @Environment(value=EnvType.CLIENT)
    public static class Targets {
        public static final Target POSITION = ModelPart::offsetPos;
        public static final Target ROTATION = ModelPart::offsetRotation;
        public static final Target SCALE = ModelPart::offsetScale;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Interpolation {
        public Vector3f apply(Vector3f var1, float var2, Keyframe[] var3, int var4, int var5, float var6);
    }
}

