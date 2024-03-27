package net.minecraft.client.renderer.texture.atlas;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record SpriteSourceType(MapCodec<? extends SpriteSource> codec) {
}
