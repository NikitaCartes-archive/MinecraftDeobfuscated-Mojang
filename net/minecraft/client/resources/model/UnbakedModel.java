/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface UnbakedModel {
    public Collection<ResourceLocation> getDependencies();

    public Collection<ResourceLocation> getTextures(Function<ResourceLocation, UnbakedModel> var1, Set<String> var2);

    @Nullable
    public BakedModel bake(ModelBakery var1, Function<ResourceLocation, TextureAtlasSprite> var2, ModelState var3);
}

