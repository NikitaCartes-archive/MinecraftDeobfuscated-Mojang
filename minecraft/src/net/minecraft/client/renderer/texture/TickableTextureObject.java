package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TickableTextureObject extends TextureObject, Tickable {
}
