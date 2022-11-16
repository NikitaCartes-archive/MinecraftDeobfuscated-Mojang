/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture.atlas;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;

@Environment(value=EnvType.CLIENT)
public record SpriteSourceType(Codec<? extends SpriteSource> codec) {
}

