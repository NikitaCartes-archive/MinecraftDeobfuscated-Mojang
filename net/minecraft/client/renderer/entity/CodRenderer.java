/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cod;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CodRenderer
extends MobRenderer<Cod, CodModel<Cod>> {
    private static final ResourceLocation COD_LOCATION = new ResourceLocation("textures/entity/fish/cod.png");

    public CodRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new CodModel(), 0.3f);
    }

    @Override
    @Nullable
    protected ResourceLocation getTextureLocation(Cod cod) {
        return COD_LOCATION;
    }

    @Override
    protected void setupRotations(Cod cod, float f, float g, float h) {
        super.setupRotations(cod, f, g, h);
        float i = 4.3f * Mth.sin(0.6f * f);
        GlStateManager.rotatef(i, 0.0f, 1.0f, 0.0f);
        if (!cod.isInWater()) {
            GlStateManager.translatef(0.1f, 0.1f, -0.1f);
            GlStateManager.rotatef(90.0f, 0.0f, 0.0f, 1.0f);
        }
    }
}

