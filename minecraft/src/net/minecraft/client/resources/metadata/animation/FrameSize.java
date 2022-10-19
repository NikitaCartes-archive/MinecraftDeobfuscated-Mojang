package net.minecraft.client.resources.metadata.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record FrameSize(int width, int height) {
}
