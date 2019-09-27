/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class TntRenderer
extends EntityRenderer<PrimedTnt> {
    public TntRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(PrimedTnt primedTnt, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0);
        if ((float)primedTnt.getLife() - h + 1.0f < 10.0f) {
            float i = 1.0f - ((float)primedTnt.getLife() - h + 1.0f) / 10.0f;
            i = Mth.clamp(i, 0.0f, 1.0f);
            i *= i;
            i *= i;
            float j = 1.0f + i * 0.3f;
            poseStack.scale(j, j, j);
        }
        int k = primedTnt.getLightColor();
        poseStack.mulPose(Vector3f.YP.rotation(-90.0f, true));
        poseStack.translate(-0.5, -0.5, 0.5);
        if (primedTnt.getLife() / 5 % 2 == 0) {
            TntMinecartRenderer.renderWhiteSolidBlock(Blocks.TNT.defaultBlockState(), poseStack, multiBufferSource, k);
        } else {
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.TNT.defaultBlockState(), poseStack, multiBufferSource, k, 0, 10);
        }
        poseStack.popPose();
        super.render(primedTnt, d, e, f, g, h, poseStack, multiBufferSource);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedTnt primedTnt) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

