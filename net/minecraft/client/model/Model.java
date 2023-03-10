/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public abstract class Model {
    protected final Function<ResourceLocation, RenderType> renderType;

    public Model(Function<ResourceLocation, RenderType> function) {
        this.renderType = function;
    }

    public final RenderType renderType(ResourceLocation resourceLocation) {
        return this.renderType.apply(resourceLocation);
    }

    public abstract void renderToBuffer(PoseStack var1, VertexConsumer var2, int var3, int var4, float var5, float var6, float var7, float var8);
}

