/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(value=EnvType.CLIENT)
public class ArmorStandRenderer
extends LivingEntityRenderer<ArmorStand, ArmorStandArmorModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = new ResourceLocation("textures/entity/armorstand/wood.png");

    public ArmorStandRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new ArmorStandModel(), 0.0f);
        this.addLayer(new HumanoidArmorLayer<ArmorStand, ArmorStandArmorModel, ArmorStandArmorModel>(this, new ArmorStandArmorModel(0.5f), new ArmorStandArmorModel(1.0f)));
        this.addLayer(new ItemInHandLayer<ArmorStand, ArmorStandArmorModel>(this));
        this.addLayer(new ElytraLayer<ArmorStand, ArmorStandArmorModel>(this));
        this.addLayer(new CustomHeadLayer<ArmorStand, ArmorStandArmorModel>(this));
    }

    @Override
    protected ResourceLocation getTextureLocation(ArmorStand armorStand) {
        return DEFAULT_SKIN_LOCATION;
    }

    @Override
    protected void setupRotations(ArmorStand armorStand, float f, float g, float h) {
        GlStateManager.rotatef(180.0f - g, 0.0f, 1.0f, 0.0f);
        float i = (float)(armorStand.level.getGameTime() - armorStand.lastHit) + h;
        if (i < 5.0f) {
            GlStateManager.rotatef(Mth.sin(i / 1.5f * (float)Math.PI) * 3.0f, 0.0f, 1.0f, 0.0f);
        }
    }

    @Override
    protected boolean shouldShowName(ArmorStand armorStand) {
        return armorStand.isCustomNameVisible();
    }

    @Override
    public void render(ArmorStand armorStand, double d, double e, double f, float g, float h) {
        if (armorStand.isMarker()) {
            this.onlySolidLayers = true;
        }
        super.render(armorStand, d, e, f, g, h);
        if (armorStand.isMarker()) {
            this.onlySolidLayers = false;
        }
    }

    @Override
    protected /* synthetic */ boolean shouldShowName(LivingEntity livingEntity) {
        return this.shouldShowName((ArmorStand)livingEntity);
    }
}

