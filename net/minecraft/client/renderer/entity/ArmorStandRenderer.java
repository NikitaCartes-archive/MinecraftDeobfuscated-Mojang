/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ArmorStandRenderer
extends LivingEntityRenderer<ArmorStand, ArmorStandArmorModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = new ResourceLocation("textures/entity/armorstand/wood.png");

    public ArmorStandRenderer(EntityRendererProvider.Context context) {
        super(context, new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0f);
        this.addLayer(new HumanoidArmorLayer<ArmorStand, ArmorStandArmorModel, ArmorStandArmorModel>(this, new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)), new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)), context.getModelManager()));
        this.addLayer(new ItemInHandLayer<ArmorStand, ArmorStandArmorModel>(this, context.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<ArmorStand, ArmorStandArmorModel>(this, context.getModelSet()));
        this.addLayer(new CustomHeadLayer<ArmorStand, ArmorStandArmorModel>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(ArmorStand armorStand) {
        return DEFAULT_SKIN_LOCATION;
    }

    @Override
    protected void setupRotations(ArmorStand armorStand, PoseStack poseStack, float f, float g, float h) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - g));
        float i = (float)(armorStand.level.getGameTime() - armorStand.lastHit) + h;
        if (i < 5.0f) {
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(i / 1.5f * (float)Math.PI) * 3.0f));
        }
    }

    @Override
    protected boolean shouldShowName(ArmorStand armorStand) {
        float f;
        double d = this.entityRenderDispatcher.distanceToSqr(armorStand);
        float f2 = f = armorStand.isCrouching() ? 32.0f : 64.0f;
        if (d >= (double)(f * f)) {
            return false;
        }
        return armorStand.isCustomNameVisible();
    }

    @Override
    @Nullable
    protected RenderType getRenderType(ArmorStand armorStand, boolean bl, boolean bl2, boolean bl3) {
        if (!armorStand.isMarker()) {
            return super.getRenderType(armorStand, bl, bl2, bl3);
        }
        ResourceLocation resourceLocation = this.getTextureLocation(armorStand);
        if (bl2) {
            return RenderType.entityTranslucent(resourceLocation, false);
        }
        if (bl) {
            return RenderType.entityCutoutNoCull(resourceLocation, false);
        }
        return null;
    }
}

