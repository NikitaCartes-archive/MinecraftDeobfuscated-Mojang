/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

    private int setupBobbingItem(ItemEntity itemEntity, double d, double e, double f, float g, BakedModel bakedModel) {
        ItemStack itemStack = itemEntity.getItem();
        Item item = itemStack.getItem();
        if (item == null) {
            return 0;
        }
        boolean bl = bakedModel.isGui3d();
        int i = this.getRenderAmount(itemStack);
        float h = 0.25f;
        float j = Mth.sin(((float)itemEntity.getAge() + g) / 10.0f + itemEntity.bobOffs) * 0.1f + 0.1f;
        float k = bakedModel.getTransforms().getTransform((ItemTransforms.TransformType)ItemTransforms.TransformType.GROUND).scale.y();
        RenderSystem.translatef((float)d, (float)e + j + 0.25f * k, (float)f);
        if (bl || this.entityRenderDispatcher.options != null) {
            float l = (((float)itemEntity.getAge() + g) / 20.0f + itemEntity.bobOffs) * 57.295776f;
            RenderSystem.rotatef(l, 0.0f, 1.0f, 0.0f);
        }
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        return i;
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
    public void render(ItemEntity itemEntity, double d, double e, double f, float g, float h) {
        float p;
        float o;
        ItemStack itemStack = itemEntity.getItem();
        int i = itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
        this.random.setSeed(i);
        boolean bl = false;
        if (this.bindTexture(itemEntity)) {
            this.entityRenderDispatcher.textureManager.getTexture(this.getTextureLocation(itemEntity)).pushFilter(false, false);
            bl = true;
        }
        RenderSystem.enableRescaleNormal();
        RenderSystem.alphaFunc(516, 0.1f);
        RenderSystem.enableBlend();
        Lighting.turnOn();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.pushMatrix();
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.level, null);
        int j = this.setupBobbingItem(itemEntity, d, e, f, h, bakedModel);
        float k = bakedModel.getTransforms().ground.scale.x();
        float l = bakedModel.getTransforms().ground.scale.y();
        float m = bakedModel.getTransforms().ground.scale.z();
        boolean bl2 = bakedModel.isGui3d();
        if (!bl2) {
            float n = -0.0f * (float)(j - 1) * 0.5f * k;
            o = -0.0f * (float)(j - 1) * 0.5f * l;
            p = -0.09375f * (float)(j - 1) * 0.5f * m;
            RenderSystem.translatef(n, o, p);
        }
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(itemEntity));
        }
        for (int q = 0; q < j; ++q) {
            if (bl2) {
                RenderSystem.pushMatrix();
                if (q > 0) {
                    o = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    p = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    float r = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    RenderSystem.translatef(o, p, r);
                }
                bakedModel.getTransforms().apply(ItemTransforms.TransformType.GROUND);
                this.itemRenderer.render(itemStack, bakedModel);
                RenderSystem.popMatrix();
                continue;
            }
            RenderSystem.pushMatrix();
            if (q > 0) {
                o = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                p = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                RenderSystem.translatef(o, p, 0.0f);
            }
            bakedModel.getTransforms().apply(ItemTransforms.TransformType.GROUND);
            this.itemRenderer.render(itemStack, bakedModel);
            RenderSystem.popMatrix();
            RenderSystem.translatef(0.0f * k, 0.0f * l, 0.09375f * m);
        }
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }
        RenderSystem.popMatrix();
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableBlend();
        this.bindTexture(itemEntity);
        if (bl) {
            this.entityRenderDispatcher.textureManager.getTexture(this.getTextureLocation(itemEntity)).popFilter();
        }
        super.render(itemEntity, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(ItemEntity itemEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

