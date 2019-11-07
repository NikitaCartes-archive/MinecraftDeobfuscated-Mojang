/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
    public void render(PrimedTnt primedTnt, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0);
        if ((float)primedTnt.getLife() - g + 1.0f < 10.0f) {
            float h = 1.0f - ((float)primedTnt.getLife() - g + 1.0f) / 10.0f;
            h = Mth.clamp(h, 0.0f, 1.0f);
            h *= h;
            h *= h;
            float j = 1.0f + h * 0.3f;
            poseStack.scale(j, j, j);
        }
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0f));
        poseStack.translate(-0.5, -0.5, 0.5);
        TntMinecartRenderer.renderWhiteSolidBlock(Blocks.TNT.defaultBlockState(), poseStack, multiBufferSource, i, primedTnt.getLife() / 5 % 2 == 0);
        poseStack.popPose();
        super.render(primedTnt, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedTnt primedTnt) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

