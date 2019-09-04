/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractHorseRenderer<T extends AbstractHorse, M extends HorseModel<T>>
extends MobRenderer<T, M> {
    private final float scale;

    public AbstractHorseRenderer(EntityRenderDispatcher entityRenderDispatcher, M horseModel, float f) {
        super(entityRenderDispatcher, horseModel, 0.75f);
        this.scale = f;
    }

    @Override
    protected void scale(T abstractHorse, float f) {
        RenderSystem.scalef(this.scale, this.scale, this.scale);
        super.scale(abstractHorse, f);
    }
}

