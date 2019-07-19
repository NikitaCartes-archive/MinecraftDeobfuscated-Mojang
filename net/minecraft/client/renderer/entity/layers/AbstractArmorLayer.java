/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
extends RenderLayer<T, M> {
    protected static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    protected final A innerModel;
    protected final A outerModel;
    private float alpha = 1.0f;
    private float red = 1.0f;
    private float green = 1.0f;
    private float blue = 1.0f;
    private boolean colorized;
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();

    protected AbstractArmorLayer(RenderLayerParent<T, M> renderLayerParent, A humanoidModel, A humanoidModel2) {
        super(renderLayerParent);
        this.innerModel = humanoidModel;
        this.outerModel = humanoidModel2;
    }

    @Override
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
        this.renderArmorPiece(livingEntity, f, g, h, i, j, k, l, EquipmentSlot.CHEST);
        this.renderArmorPiece(livingEntity, f, g, h, i, j, k, l, EquipmentSlot.LEGS);
        this.renderArmorPiece(livingEntity, f, g, h, i, j, k, l, EquipmentSlot.FEET);
        this.renderArmorPiece(livingEntity, f, g, h, i, j, k, l, EquipmentSlot.HEAD);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }

    private void renderArmorPiece(T livingEntity, float f, float g, float h, float i, float j, float k, float l, EquipmentSlot equipmentSlot) {
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
        boolean bl = this.usesInnerModel(equipmentSlot);
        this.bindTexture(this.getArmorLocation(armorItem, bl));
        if (armorItem instanceof DyeableArmorItem) {
            int m = ((DyeableArmorItem)armorItem).getColor(itemStack);
            float n = (float)(m >> 16 & 0xFF) / 255.0f;
            float o = (float)(m >> 8 & 0xFF) / 255.0f;
            float p = (float)(m & 0xFF) / 255.0f;
            GlStateManager.color4f(this.red * n, this.green * o, this.blue * p, this.alpha);
            ((HumanoidModel)humanoidModel).render(livingEntity, f, g, i, j, k, l);
            this.bindTexture(this.getArmorLocation(armorItem, bl, "overlay"));
        }
        GlStateManager.color4f(this.red, this.green, this.blue, this.alpha);
        ((HumanoidModel)humanoidModel).render(livingEntity, f, g, i, j, k, l);
        if (!this.colorized && itemStack.isEnchanted()) {
            AbstractArmorLayer.renderFoil(this::bindTexture, livingEntity, humanoidModel, f, g, h, i, j, k, l);
        }
    }

    public A getArmorModel(EquipmentSlot equipmentSlot) {
        return this.usesInnerModel(equipmentSlot) ? this.innerModel : this.outerModel;
    }

    private boolean usesInnerModel(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.LEGS;
    }

    public static <T extends Entity> void renderFoil(Consumer<ResourceLocation> consumer, T entity, EntityModel<T> entityModel, float f, float g, float h, float i, float j, float k, float l) {
        float m = (float)entity.tickCount + h;
        consumer.accept(ENCHANT_GLINT_LOCATION);
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        gameRenderer.resetFogColor(true);
        GlStateManager.enableBlend();
        GlStateManager.depthFunc(514);
        GlStateManager.depthMask(false);
        float n = 0.5f;
        GlStateManager.color4f(0.5f, 0.5f, 0.5f, 1.0f);
        for (int o = 0; o < 2; ++o) {
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            float p = 0.76f;
            GlStateManager.color4f(0.38f, 0.19f, 0.608f, 1.0f);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float q = 0.33333334f;
            GlStateManager.scalef(0.33333334f, 0.33333334f, 0.33333334f);
            GlStateManager.rotatef(30.0f - (float)o * 60.0f, 0.0f, 0.0f, 1.0f);
            GlStateManager.translatef(0.0f, m * (0.001f + (float)o * 0.003f) * 20.0f, 0.0f);
            GlStateManager.matrixMode(5888);
            entityModel.render(entity, f, g, i, j, k, l);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
        GlStateManager.disableBlend();
        gameRenderer.resetFogColor(false);
    }

    private ResourceLocation getArmorLocation(ArmorItem armorItem, boolean bl) {
        return this.getArmorLocation(armorItem, bl, null);
    }

    private ResourceLocation getArmorLocation(ArmorItem armorItem, boolean bl, @Nullable String string) {
        String string2 = "textures/models/armor/" + armorItem.getMaterial().getName() + "_layer_" + (bl ? 2 : 1) + (string == null ? "" : "_" + string) + ".png";
        return ARMOR_LOCATION_CACHE.computeIfAbsent(string2, ResourceLocation::new);
    }

    protected abstract void setPartVisibility(A var1, EquipmentSlot var2);

    protected abstract void hideAllArmor(A var1);
}

