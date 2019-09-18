/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.Tickable;

@Environment(value=EnvType.CLIENT)
public interface TickableTextureObject
extends TextureObject,
Tickable {
}

