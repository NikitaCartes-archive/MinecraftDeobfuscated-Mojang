/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(value=EnvType.CLIENT)
public class ElytraLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    private static final ResourceLocation WINGS_LOCATION = new ResourceLocation("textures/entity/elytra.png");
    private final ElytraModel<T> elytraModel;

    public ElytraLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.elytraModel = new ElytraModel(entityModelSet.bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        AbstractClientPlayer abstractClientPlayer;
        ItemStack itemStack = ((LivingEntity)livingEntity).getItemBySlot(EquipmentSlot.CHEST);
        if (!itemStack.is(Items.ELYTRA)) {
            return;
        }
        ResourceLocation resourceLocation = livingEntity instanceof AbstractClientPlayer ? ((abstractClientPlayer = (AbstractClientPlayer)livingEntity).isElytraLoaded() && abstractClientPlayer.getElytraTextureLocation() != null ? abstractClientPlayer.getElytraTextureLocation() : (abstractClientPlayer.isCapeLoaded() && abstractClientPlayer.getCloakTextureLocation() != null && abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE) ? abstractClientPlayer.getCloakTextureLocation() : WINGS_LOCATION)) : WINGS_LOCATION;
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 0.125f);
        ((EntityModel)this.getParentModel()).copyPropertiesTo(this.elytraModel);
        this.elytraModel.setupAnim(livingEntity, f, g, j, k, l);
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource, RenderType.armorCutoutNoCull(resourceLocation), false, itemStack.hasFoil());
        this.elytraModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}

