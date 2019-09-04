/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TropicalFishRenderer
extends MobRenderer<TropicalFish, EntityModel<TropicalFish>> {
    private final TropicalFishModelA<TropicalFish> modelA = new TropicalFishModelA();
    private final TropicalFishModelB<TropicalFish> modelB = new TropicalFishModelB();

    public TropicalFishRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new TropicalFishModelA(), 0.15f);
        this.addLayer(new TropicalFishPatternLayer(this));
    }

    @Override
    @Nullable
    protected ResourceLocation getTextureLocation(TropicalFish tropicalFish) {
        return tropicalFish.getBaseTextureLocation();
    }

    @Override
    public void render(TropicalFish tropicalFish, double d, double e, double f, float g, float h) {
        this.model = tropicalFish.getBaseVariant() == 0 ? this.modelA : this.modelB;
        float[] fs = tropicalFish.getBaseColor();
        RenderSystem.color3f(fs[0], fs[1], fs[2]);
        super.render(tropicalFish, d, e, f, g, h);
    }

    @Override
    protected void setupRotations(TropicalFish tropicalFish, float f, float g, float h) {
        super.setupRotations(tropicalFish, f, g, h);
        float i = 4.3f * Mth.sin(0.6f * f);
        RenderSystem.rotatef(i, 0.0f, 1.0f, 0.0f);
        if (!tropicalFish.isInWater()) {
            RenderSystem.translatef(0.2f, 0.1f, 0.0f);
            RenderSystem.rotatef(90.0f, 0.0f, 0.0f, 1.0f);
        }
    }
}

