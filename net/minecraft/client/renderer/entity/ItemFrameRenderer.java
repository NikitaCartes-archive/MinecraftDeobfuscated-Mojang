/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ItemFrameRenderer
extends EntityRenderer<ItemFrame> {
    private static final ResourceLocation MAP_BACKGROUND_LOCATION = new ResourceLocation("textures/map/map_background.png");
    private static final ModelResourceLocation FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=false");
    private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");
    private final Minecraft minecraft = Minecraft.getInstance();
    private final ItemRenderer itemRenderer;

    public ItemFrameRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
        super(entityRenderDispatcher);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(ItemFrame itemFrame, double d, double e, double f, float g, float h) {
        GlStateManager.pushMatrix();
        BlockPos blockPos = itemFrame.getPos();
        double i = (double)blockPos.getX() - itemFrame.x + d;
        double j = (double)blockPos.getY() - itemFrame.y + e;
        double k = (double)blockPos.getZ() - itemFrame.z + f;
        GlStateManager.translated(i + 0.5, j + 0.5, k + 0.5);
        GlStateManager.rotatef(itemFrame.xRot, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotatef(180.0f - itemFrame.yRot, 0.0f, 1.0f, 0.0f);
        this.entityRenderDispatcher.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        BlockRenderDispatcher blockRenderDispatcher = this.minecraft.getBlockRenderer();
        ModelManager modelManager = blockRenderDispatcher.getBlockModelShaper().getModelManager();
        ModelResourceLocation modelResourceLocation = itemFrame.getItem().getItem() == Items.FILLED_MAP ? MAP_FRAME_LOCATION : FRAME_LOCATION;
        GlStateManager.pushMatrix();
        GlStateManager.translatef(-0.5f, -0.5f, -0.5f);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(itemFrame));
        }
        blockRenderDispatcher.getModelRenderer().renderModel(modelManager.getModel(modelResourceLocation), 1.0f, 1.0f, 1.0f, 1.0f);
        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        if (itemFrame.getItem().getItem() == Items.FILLED_MAP) {
            GlStateManager.pushLightingAttributes();
            Lighting.turnOn();
        }
        GlStateManager.translatef(0.0f, 0.0f, 0.4375f);
        this.drawItem(itemFrame);
        if (itemFrame.getItem().getItem() == Items.FILLED_MAP) {
            Lighting.turnOff();
            GlStateManager.popAttributes();
        }
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        this.renderName(itemFrame, d + (double)((float)itemFrame.getDirection().getStepX() * 0.3f), e - 0.25, f + (double)((float)itemFrame.getDirection().getStepZ() * 0.3f));
    }

    @Override
    @Nullable
    protected ResourceLocation getTextureLocation(ItemFrame itemFrame) {
        return null;
    }

    private void drawItem(ItemFrame itemFrame) {
        ItemStack itemStack = itemFrame.getItem();
        if (itemStack.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        boolean bl = itemStack.getItem() == Items.FILLED_MAP;
        int i = bl ? itemFrame.getRotation() % 4 * 2 : itemFrame.getRotation();
        GlStateManager.rotatef((float)i * 360.0f / 8.0f, 0.0f, 0.0f, 1.0f);
        if (bl) {
            GlStateManager.disableLighting();
            this.entityRenderDispatcher.textureManager.bind(MAP_BACKGROUND_LOCATION);
            GlStateManager.rotatef(180.0f, 0.0f, 0.0f, 1.0f);
            float f = 0.0078125f;
            GlStateManager.scalef(0.0078125f, 0.0078125f, 0.0078125f);
            GlStateManager.translatef(-64.0f, -64.0f, 0.0f);
            MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, itemFrame.level);
            GlStateManager.translatef(0.0f, 0.0f, -1.0f);
            if (mapItemSavedData != null) {
                this.minecraft.gameRenderer.getMapRenderer().render(mapItemSavedData, true);
            }
        } else {
            GlStateManager.scalef(0.5f, 0.5f, 0.5f);
            this.itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.FIXED);
        }
        GlStateManager.popMatrix();
    }

    @Override
    protected void renderName(ItemFrame itemFrame, double d, double e, double f) {
        float h;
        if (!Minecraft.renderNames() || itemFrame.getItem().isEmpty() || !itemFrame.getItem().hasCustomHoverName() || this.entityRenderDispatcher.crosshairPickEntity != itemFrame) {
            return;
        }
        double g = itemFrame.distanceToSqr(this.entityRenderDispatcher.camera.getPosition());
        float f2 = h = itemFrame.isVisuallySneaking() ? 32.0f : 64.0f;
        if (g >= (double)(h * h)) {
            return;
        }
        String string = itemFrame.getItem().getHoverName().getColoredString();
        this.renderNameTag(itemFrame, string, d, e, f, 64);
    }
}

