/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ThrownTrident;

@Environment(value=EnvType.CLIENT)
public class ThrownTridentRenderer
extends EntityRenderer<ThrownTrident> {
    public static final ResourceLocation TRIDENT_LOCATION = new ResourceLocation("textures/entity/trident.png");
    private final TridentModel model;

    public ThrownTridentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TridentModel(context.getLayer(ModelLayers.TRIDENT));
    }

    @Override
    public void render(ThrownTrident thrownTrident, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(g, thrownTrident.yRotO, thrownTrident.yRot) - 90.0f));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(g, thrownTrident.xRotO, thrownTrident.xRot) + 90.0f));
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(multiBufferSource, this.model.renderType(this.getTextureLocation(thrownTrident)), false, thrownTrident.isFoil());
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
        super.render(thrownTrident, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownTrident thrownTrident) {
        return TRIDENT_LOCATION;
    }
}

