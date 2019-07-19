/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface GlyphProviderBuilder {
    @Nullable
    public GlyphProvider create(ResourceManager var1);
}

