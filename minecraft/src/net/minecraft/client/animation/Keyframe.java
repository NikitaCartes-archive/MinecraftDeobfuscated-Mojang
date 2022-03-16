package net.minecraft.client.animation;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record Keyframe(float timestamp, Vector3f target, AnimationChannel.Interpolation interpolation) {
}
