/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

@Environment(value=EnvType.CLIENT)
public class SlimeRenderer
extends MobRenderer<Slime, SlimeModel<Slime>> {
    private static final ResourceLocation SLIME_LOCATION = new ResourceLocation("textures/entity/slime/slime.png");

    public SlimeRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SlimeModel(16), 0.25f);
        this.addLayer(new SlimeOuterLayer<Slime>(this));
    }

    @Override
    public void render(Slime slime, double d, double e, double f, float g, float h) {
        this.shadowRadius = 0.25f * (float)slime.getSize();
        super.render(slime, d, e, f, g, h);
    }

    @Override
    protected void scale(Slime slime, float f) {
        float g = 0.999f;
        RenderSystem.scalef(0.999f, 0.999f, 0.999f);
        float h = slime.getSize();
        float i = Mth.lerp(f, slime.oSquish, slime.squish) / (h * 0.5f + 1.0f);
        float j = 1.0f / (i + 1.0f);
        RenderSystem.scalef(j * h, 1.0f / j * h, j * h);
    }

    @Override
    protected ResourceLocation getTextureLocation(Slime slime) {
        return SLIME_LOCATION;
    }
}

