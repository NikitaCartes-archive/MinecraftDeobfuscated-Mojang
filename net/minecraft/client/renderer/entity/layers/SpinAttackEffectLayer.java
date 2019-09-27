/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class SpinAttackEffectLayer<T extends LivingEntity>
extends RenderLayer<T, PlayerModel<T>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident_riptide.png");
    private final ModelPart box = new ModelPart(64, 64, 0, 0);

    public SpinAttackEffectLayer(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
        super(renderLayerParent);
        this.box.addBox(-8.0f, -16.0f, -8.0f, 16.0f, 32.0f, 16.0f);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m) {
        if (!((LivingEntity)livingEntity).isAutoSpinAttack()) {
            return;
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(TEXTURE));
        OverlayTexture.setDefault(vertexConsumer);
        for (int n = 0; n < 3; ++n) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotation(j * (float)(-(45 + n * 5)), true));
            float o = 0.75f * (float)n;
            poseStack.scale(o, o, o);
            poseStack.translate(0.0, -0.2f + 0.6f * (float)n, 0.0);
            this.box.render(poseStack, vertexConsumer, m, i, null);
            poseStack.popPose();
        }
        vertexConsumer.unsetDefaultOverlayCoords();
    }
}

