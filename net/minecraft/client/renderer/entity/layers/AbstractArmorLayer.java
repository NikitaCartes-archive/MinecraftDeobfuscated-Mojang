/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
extends RenderLayer<T, M> {
    protected final A innerModel;
    protected final A outerModel;
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();

    protected AbstractArmorLayer(RenderLayerParent<T, M> renderLayerParent, A humanoidModel, A humanoidModel2) {
        super(renderLayerParent);
        this.innerModel = humanoidModel;
        this.outerModel = humanoidModel2;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m) {
        this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, f, g, h, j, k, l, m, EquipmentSlot.CHEST, i);
        this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, f, g, h, j, k, l, m, EquipmentSlot.LEGS, i);
        this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, f, g, h, j, k, l, m, EquipmentSlot.FEET, i);
        this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, f, g, h, j, k, l, m, EquipmentSlot.HEAD, i);
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, float f, float g, float h, float i, float j, float k, float l, EquipmentSlot equipmentSlot, int m) {
        ItemStack itemStack = ((LivingEntity)livingEntity).getItemBySlot(equipmentSlot);
        if (!(itemStack.getItem() instanceof ArmorItem)) {
            return;
        }
        ArmorItem armorItem = (ArmorItem)itemStack.getItem();
        if (armorItem.getSlot() != equipmentSlot) {
            return;
        }
        A humanoidModel = this.getArmorModel(equipmentSlot);
        ((HumanoidModel)this.getParentModel()).copyPropertiesTo(humanoidModel);
        ((HumanoidModel)humanoidModel).prepareMobModel(livingEntity, f, g, h);
        this.setPartVisibility(humanoidModel, equipmentSlot);
        ((HumanoidModel)humanoidModel).setupAnim(livingEntity, f, g, i, j, k, l);
        boolean bl = this.usesInnerModel(equipmentSlot);
        boolean bl2 = itemStack.hasFoil();
        if (armorItem instanceof DyeableArmorItem) {
            int n = ((DyeableArmorItem)armorItem).getColor(itemStack);
            float o = (float)(n >> 16 & 0xFF) / 255.0f;
            float p = (float)(n >> 8 & 0xFF) / 255.0f;
            float q = (float)(n & 0xFF) / 255.0f;
            this.renderModel(poseStack, multiBufferSource, m, armorItem, bl2, humanoidModel, bl, o, p, q, null);
            this.renderModel(poseStack, multiBufferSource, m, armorItem, bl2, humanoidModel, bl, 1.0f, 1.0f, 1.0f, "overlay");
        } else {
            this.renderModel(poseStack, multiBufferSource, m, armorItem, bl2, humanoidModel, bl, 1.0f, 1.0f, 1.0f, null);
        }
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorItem armorItem, boolean bl, A humanoidModel, boolean bl2, float f, float g, float h, @Nullable String string) {
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBuffer(multiBufferSource, RenderType.entityCutoutNoCull(this.getArmorLocation(armorItem, bl2, string)), false, bl);
        ((AgeableListModel)humanoidModel).renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h);
    }

    public A getArmorModel(EquipmentSlot equipmentSlot) {
        return this.usesInnerModel(equipmentSlot) ? this.innerModel : this.outerModel;
    }

    private boolean usesInnerModel(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.LEGS;
    }

    private ResourceLocation getArmorLocation(ArmorItem armorItem, boolean bl, @Nullable String string) {
        String string2 = "textures/models/armor/" + armorItem.getMaterial().getName() + "_layer_" + (bl ? 2 : 1) + (string == null ? "" : "_" + string) + ".png";
        return ARMOR_LOCATION_CACHE.computeIfAbsent(string2, ResourceLocation::new);
    }

    protected abstract void setPartVisibility(A var1, EquipmentSlot var2);

    protected abstract void hideAllArmor(A var1);
}

