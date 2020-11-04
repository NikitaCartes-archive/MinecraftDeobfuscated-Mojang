/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(value=EnvType.CLIENT)
public class WitherBossRenderer
extends MobRenderer<WitherBoss, WitherBossModel<WitherBoss>> {
    private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
    private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");

    public WitherBossRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherBossModel(context.getLayer(ModelLayers.WITHER)), 1.0f);
        this.addLayer(new WitherArmorLayer(this, context.getModelSet()));
    }

    @Override
    protected int getBlockLightLevel(WitherBoss witherBoss, BlockPos blockPos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(WitherBoss witherBoss) {
        int i = witherBoss.getInvulnerableTicks();
        if (i <= 0 || i <= 80 && i / 5 % 2 == 1) {
            return WITHER_LOCATION;
        }
        return WITHER_INVULNERABLE_LOCATION;
    }

    @Override
    protected void scale(WitherBoss witherBoss, PoseStack poseStack, float f) {
        float g = 2.0f;
        int i = witherBoss.getInvulnerableTicks();
        if (i > 0) {
            g -= ((float)i - f) / 220.0f * 0.5f;
        }
        poseStack.scale(g, g, g);
    }
}

