/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class ItemEntityRenderer
extends EntityRenderer<ItemEntity> {
    private final ItemRenderer itemRenderer;
    private final Random random = new Random();

    public ItemEntityRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
        super(entityRenderDispatcher);
        this.itemRenderer = itemRenderer;
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    private int getRenderAmount(ItemStack itemStack) {
        int i = 1;
        if (itemStack.getCount() > 48) {
            i = 5;
        } else if (itemStack.getCount() > 32) {
            i = 4;
        } else if (itemStack.getCount() > 16) {
            i = 3;
        } else if (itemStack.getCount() > 1) {
            i = 2;
        }
        return i;
    }

    @Override
    public void render(ItemEntity itemEntity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        float t;
        float s;
        poseStack.pushPose();
        ItemStack itemStack = itemEntity.getItem();
        int i = itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
        this.random.setSeed(i);
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.level, null);
        boolean bl = bakedModel.isGui3d();
        int j = this.getRenderAmount(itemStack);
        float k = 0.25f;
        float l = Mth.sin(((float)itemEntity.getAge() + h) / 10.0f + itemEntity.bobOffs) * 0.1f + 0.1f;
        float m = bakedModel.getTransforms().getTransform((ItemTransforms.TransformType)ItemTransforms.TransformType.GROUND).scale.y();
        poseStack.translate(0.0, l + 0.25f * m, 0.0);
        float n = ((float)itemEntity.getAge() + h) / 20.0f + itemEntity.bobOffs;
        poseStack.mulPose(Vector3f.YP.rotation(n, false));
        float o = bakedModel.getTransforms().ground.scale.x();
        float p = bakedModel.getTransforms().ground.scale.y();
        float q = bakedModel.getTransforms().ground.scale.z();
        if (!bl) {
            float r = -0.0f * (float)(j - 1) * 0.5f * o;
            s = -0.0f * (float)(j - 1) * 0.5f * p;
            t = -0.09375f * (float)(j - 1) * 0.5f * q;
            poseStack.translate(r, s, t);
        }
        for (int u = 0; u < j; ++u) {
            poseStack.pushPose();
            if (u > 0) {
                if (bl) {
                    s = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    t = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    float v = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    poseStack.translate(s, t, v);
                } else {
                    s = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    t = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    poseStack.translate(s, t, 0.0);
                }
            }
            this.itemRenderer.render(itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, itemEntity.getLightColor(), bakedModel);
            poseStack.popPose();
            if (bl) continue;
            poseStack.translate(0.0f * o, 0.0f * p, 0.09375f * q);
        }
        poseStack.popPose();
        super.render(itemEntity, d, e, f, g, h, poseStack, multiBufferSource);
    }

    @Override
    public ResourceLocation getTextureLocation(ItemEntity itemEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

