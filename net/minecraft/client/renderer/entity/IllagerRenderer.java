/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.monster.AbstractIllager;

@Environment(value=EnvType.CLIENT)
public abstract class IllagerRenderer<T extends AbstractIllager>
extends MobRenderer<T, IllagerModel<T>> {
    protected IllagerRenderer(EntityRenderDispatcher entityRenderDispatcher, IllagerModel<T> illagerModel, float f) {
        super(entityRenderDispatcher, illagerModel, f);
        this.addLayer(new CustomHeadLayer(this));
    }

    public IllagerRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new IllagerModel(0.0f, 0.0f, 64, 64), 0.5f);
        this.addLayer(new CustomHeadLayer(this));
    }

    @Override
    protected void scale(T abstractIllager, float f) {
        float g = 0.9375f;
        RenderSystem.scalef(0.9375f, 0.9375f, 0.9375f);
    }
}

