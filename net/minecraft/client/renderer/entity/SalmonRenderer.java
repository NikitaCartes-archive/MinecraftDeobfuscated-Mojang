/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Salmon;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SalmonRenderer
extends MobRenderer<Salmon, SalmonModel<Salmon>> {
    private static final ResourceLocation SALMON_LOCATION = new ResourceLocation("textures/entity/fish/salmon.png");

    public SalmonRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SalmonModel(), 0.4f);
    }

    @Override
    @Nullable
    protected ResourceLocation getTextureLocation(Salmon salmon) {
        return SALMON_LOCATION;
    }

    @Override
    protected void setupRotations(Salmon salmon, float f, float g, float h) {
        super.setupRotations(salmon, f, g, h);
        float i = 1.0f;
        float j = 1.0f;
        if (!salmon.isInWater()) {
            i = 1.3f;
            j = 1.7f;
        }
        float k = i * 4.3f * Mth.sin(j * 0.6f * f);
        GlStateManager.rotatef(k, 0.0f, 1.0f, 0.0f);
        GlStateManager.translatef(0.0f, 0.0f, -0.4f);
        if (!salmon.isInWater()) {
            GlStateManager.translatef(0.2f, 0.1f, 0.0f);
            GlStateManager.rotatef(90.0f, 0.0f, 0.0f, 1.0f);
        }
    }
}

