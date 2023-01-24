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
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class HumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
extends RenderLayer<T, M> {
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
    private final A innerModel;
    private final A outerModel;
    private final TextureAtlas armorTrimAtlas;

    public HumanoidArmorLayer(RenderLayerParent<T, M> renderLayerParent, A humanoidModel, A humanoidModel2, ModelManager modelManager) {
        super(renderLayerParent);
        this.innerModel = humanoidModel;
        this.outerModel = humanoidModel2;
        this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, EquipmentSlot.CHEST, i, this.getArmorModel(EquipmentSlot.CHEST));
        this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, EquipmentSlot.LEGS, i, this.getArmorModel(EquipmentSlot.LEGS));
        this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, EquipmentSlot.FEET, i, this.getArmorModel(EquipmentSlot.FEET));
        this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, EquipmentSlot.HEAD, i, this.getArmorModel(EquipmentSlot.HEAD));
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int i, A humanoidModel) {
        ItemStack itemStack = ((LivingEntity)livingEntity).getItemBySlot(equipmentSlot);
        Item item = itemStack.getItem();
        if (!(item instanceof ArmorItem)) {
            return;
        }
        ArmorItem armorItem = (ArmorItem)item;
        if (armorItem.getSlot() != equipmentSlot) {
            return;
        }
        ((HumanoidModel)this.getParentModel()).copyPropertiesTo(humanoidModel);
        this.setPartVisibility(humanoidModel, equipmentSlot);
        boolean bl = this.usesInnerModel(equipmentSlot);
        boolean bl2 = itemStack.hasFoil();
        if (armorItem instanceof DyeableArmorItem) {
            int j = ((DyeableArmorItem)armorItem).getColor(itemStack);
            float f = (float)(j >> 16 & 0xFF) / 255.0f;
            float g = (float)(j >> 8 & 0xFF) / 255.0f;
            float h = (float)(j & 0xFF) / 255.0f;
            this.renderModel(poseStack, multiBufferSource, i, armorItem, bl2, humanoidModel, bl, f, g, h, null);
            this.renderModel(poseStack, multiBufferSource, i, armorItem, bl2, humanoidModel, bl, 1.0f, 1.0f, 1.0f, "overlay");
        } else {
            this.renderModel(poseStack, multiBufferSource, i, armorItem, bl2, humanoidModel, bl, 1.0f, 1.0f, 1.0f, null);
            if (((LivingEntity)livingEntity).level.enabledFeatures().contains(FeatureFlags.UPDATE_1_20)) {
                ArmorTrim.getTrim(((LivingEntity)livingEntity).level.registryAccess(), itemStack).ifPresent(armorTrim -> this.renderTrim(poseStack, multiBufferSource, i, (ArmorTrim)armorTrim, bl2, humanoidModel, bl, 1.0f, 1.0f, 1.0f));
            }
        }
    }

    protected void setPartVisibility(A humanoidModel, EquipmentSlot equipmentSlot) {
        ((HumanoidModel)humanoidModel).setAllVisible(false);
        switch (equipmentSlot) {
            case HEAD: {
                ((HumanoidModel)humanoidModel).head.visible = true;
                ((HumanoidModel)humanoidModel).hat.visible = true;
                break;
            }
            case CHEST: {
                ((HumanoidModel)humanoidModel).body.visible = true;
                ((HumanoidModel)humanoidModel).rightArm.visible = true;
                ((HumanoidModel)humanoidModel).leftArm.visible = true;
                break;
            }
            case LEGS: {
                ((HumanoidModel)humanoidModel).body.visible = true;
                ((HumanoidModel)humanoidModel).rightLeg.visible = true;
                ((HumanoidModel)humanoidModel).leftLeg.visible = true;
                break;
            }
            case FEET: {
                ((HumanoidModel)humanoidModel).rightLeg.visible = true;
                ((HumanoidModel)humanoidModel).leftLeg.visible = true;
            }
        }
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorItem armorItem, boolean bl, A humanoidModel, boolean bl2, float f, float g, float h, @Nullable String string) {
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource, RenderType.armorCutoutNoCull(this.getArmorLocation(armorItem, bl2, string)), false, bl);
        ((AgeableListModel)humanoidModel).renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, 1.0f);
    }

    private void renderTrim(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorTrim armorTrim, boolean bl, A humanoidModel, boolean bl2, float f, float g, float h) {
        TextureAtlasSprite textureAtlasSprite = this.armorTrimAtlas.getSprite(bl2 ? armorTrim.innerTexture() : armorTrim.outerTexture());
        VertexConsumer vertexConsumer = textureAtlasSprite.wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, Sheets.armorTrimsSheet(), true, bl));
        ((AgeableListModel)humanoidModel).renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, 1.0f);
    }

    private A getArmorModel(EquipmentSlot equipmentSlot) {
        return this.usesInnerModel(equipmentSlot) ? this.innerModel : this.outerModel;
    }

    private boolean usesInnerModel(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.LEGS;
    }

    private ResourceLocation getArmorLocation(ArmorItem armorItem, boolean bl, @Nullable String string) {
        String string2 = "textures/models/armor/" + armorItem.getMaterial().getName() + "_layer_" + (bl ? 2 : 1) + (String)(string == null ? "" : "_" + string) + ".png";
        return ARMOR_LOCATION_CACHE.computeIfAbsent(string2, ResourceLocation::new);
    }
}

