/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(value=EnvType.CLIENT)
public class WitherArmorLayer
extends EnergySwirlLayer<WitherBoss, WitherBossModel<WitherBoss>> {
    private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final WitherBossModel<WitherBoss> model;

    public WitherArmorLayer(RenderLayerParent<WitherBoss, WitherBossModel<WitherBoss>> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.model = new WitherBossModel(entityModelSet.getLayer(ModelLayers.WITHER_ARMOR));
    }

    @Override
    protected float xOffset(float f) {
        return Mth.cos(f * 0.02f) * 3.0f;
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return WITHER_ARMOR_LOCATION;
    }

    @Override
    protected EntityModel<WitherBoss> model() {
        return this.model;
    }
}

