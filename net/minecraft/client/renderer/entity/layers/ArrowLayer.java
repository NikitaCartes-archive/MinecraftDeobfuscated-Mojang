/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;

@Environment(value=EnvType.CLIENT)
public class ArrowLayer<T extends LivingEntity, M extends PlayerModel<T>>
extends StuckInBodyLayer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public ArrowLayer(EntityRendererProvider.Context context, LivingEntityRenderer<T, M> livingEntityRenderer) {
        super(livingEntityRenderer);
        this.dispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    protected int numStuck(T livingEntity) {
        return ((LivingEntity)livingEntity).getArrowCount();
    }

    @Override
    protected void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Entity entity, float f, float g, float h, float j) {
        float k = Mth.sqrt(f * f + h * h);
        Arrow arrow = new Arrow(entity.level, entity.getX(), entity.getY(), entity.getZ());
        arrow.setYRot((float)(Math.atan2(f, h) * 57.2957763671875));
        arrow.setXRot((float)(Math.atan2(g, k) * 57.2957763671875));
        arrow.yRotO = arrow.getYRot();
        arrow.xRotO = arrow.getXRot();
        this.dispatcher.render(arrow, 0.0, 0.0, 0.0, 0.0f, j, poseStack, multiBufferSource, i);
    }
}

