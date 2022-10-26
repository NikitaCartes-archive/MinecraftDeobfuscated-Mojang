/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationChannel;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public record Keyframe(float timestamp, Vector3f target, AnimationChannel.Interpolation interpolation) {
}

