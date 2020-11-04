/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.WitherSkull;

@Environment(value=EnvType.CLIENT)
public class WitherSkullRenderer
extends EntityRenderer<WitherSkull> {
    private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
    private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");
    private final SkullModel model;

    public WitherSkullRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.getLayer(ModelLayers.WITHER_SKULL));
    }

    public static LayerDefinition createSkullLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    protected int getBlockLightLevel(WitherSkull witherSkull, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void render(WitherSkull witherSkull, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        float h = Mth.rotlerp(witherSkull.yRotO, witherSkull.yRot, g);
        float j = Mth.lerp(g, witherSkull.xRotO, witherSkull.xRot);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(this.getTextureLocation(witherSkull)));
        this.model.setupAnim(0.0f, h, j);
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
        super.render(witherSkull, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(WitherSkull witherSkull) {
        return witherSkull.isDangerous() ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }
}

