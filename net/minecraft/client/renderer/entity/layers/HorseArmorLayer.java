/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class HorseArmorLayer
extends RenderLayer<Horse, HorseModel<Horse>> {
    private final HorseModel<Horse> model = new HorseModel(RenderType::entitySolid, 0.1f);

    public HorseArmorLayer(RenderLayerParent<Horse, HorseModel<Horse>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Horse horse, float f, float g, float h, float j, float k, float l, float m) {
        float q;
        float p;
        float o;
        ItemStack itemStack = horse.getArmor();
        if (!(itemStack.getItem() instanceof HorseArmorItem)) {
            return;
        }
        HorseArmorItem horseArmorItem = (HorseArmorItem)itemStack.getItem();
        ((HorseModel)this.getParentModel()).copyPropertiesTo(this.model);
        this.model.prepareMobModel(horse, f, g, h);
        this.model.setupAnim(horse, f, g, j, k, l, m);
        if (horseArmorItem instanceof DyeableHorseArmorItem) {
            int n = ((DyeableHorseArmorItem)horseArmorItem).getColor(itemStack);
            o = (float)(n >> 16 & 0xFF) / 255.0f;
            p = (float)(n >> 8 & 0xFF) / 255.0f;
            q = (float)(n & 0xFF) / 255.0f;
        } else {
            o = 1.0f;
            p = 1.0f;
            q = 1.0f;
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(horseArmorItem.getTexture()));
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, o, p, q);
    }
}

