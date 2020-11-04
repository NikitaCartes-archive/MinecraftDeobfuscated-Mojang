/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractHorseRenderer<T extends AbstractHorse, M extends HorseModel<T>>
extends MobRenderer<T, M> {
    private final float scale;

    public AbstractHorseRenderer(EntityRendererProvider.Context context, M horseModel, float f) {
        super(context, horseModel, 0.75f);
        this.scale = f;
    }

    @Override
    protected void scale(T abstractHorse, PoseStack poseStack, float f) {
        poseStack.scale(this.scale, this.scale, this.scale);
        super.scale(abstractHorse, poseStack, f);
    }
}

