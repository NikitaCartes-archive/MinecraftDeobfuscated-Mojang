/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.animation;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationChannel;

@Environment(value=EnvType.CLIENT)
public record Keyframe(float timestamp, Vector3f target, AnimationChannel.Interpolation interpolation) {
}

