/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class ItemFrameRenderer<T extends ItemFrame>
extends EntityRenderer<T> {
    private static final ModelResourceLocation FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=false");
    private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");
    private static final ModelResourceLocation GLOW_FRAME_LOCATION = new ModelResourceLocation("glow_item_frame", "map=false");
    private static final ModelResourceLocation GLOW_MAP_FRAME_LOCATION = new ModelResourceLocation("glow_item_frame", "map=true");
    private final Minecraft minecraft = Minecraft.getInstance();
    private final ItemRenderer itemRenderer;

    public ItemFrameRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected int getBlockLightLevel(T itemFrame, BlockPos blockPos) {
        return ((ItemFrame)itemFrame).isGlowFrame() ? 5 : super.getBlockLightLevel(itemFrame, blockPos);
    }

    @Override
    public void render(T itemFrame, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        super.render(itemFrame, f, g, poseStack, multiBufferSource, i);
        poseStack.pushPose();
        Direction direction = ((HangingEntity)itemFrame).getDirection();
        Vec3 vec3 = this.getRenderOffset(itemFrame, g);
        poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
        double d = 0.46875;
        poseStack.translate((double)direction.getStepX() * 0.46875, (double)direction.getStepY() * 0.46875, (double)direction.getStepZ() * 0.46875);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(((ItemFrame)itemFrame).xRot));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - ((ItemFrame)itemFrame).yRot));
        boolean bl = ((Entity)itemFrame).isInvisible();
        ItemStack itemStack = ((ItemFrame)itemFrame).getItem();
        if (!bl) {
            BlockRenderDispatcher blockRenderDispatcher = this.minecraft.getBlockRenderer();
            ModelManager modelManager = blockRenderDispatcher.getBlockModelShaper().getModelManager();
            ModelResourceLocation modelResourceLocation = this.getFrameModelResourceLoc(itemFrame, itemStack);
            poseStack.pushPose();
            poseStack.translate(-0.5, -0.5, -0.5);
            blockRenderDispatcher.getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(Sheets.solidBlockSheet()), null, modelManager.getModel(modelResourceLocation), 1.0f, 1.0f, 1.0f, i, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        if (!itemStack.isEmpty()) {
            boolean bl2 = itemStack.is(Items.FILLED_MAP);
            if (bl) {
                poseStack.translate(0.0, 0.0, 0.5);
            } else {
                poseStack.translate(0.0, 0.0, 0.4375);
            }
            int j = bl2 ? ((ItemFrame)itemFrame).getRotation() % 4 * 2 : ((ItemFrame)itemFrame).getRotation();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)j * 360.0f / 8.0f));
            if (bl2) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
                float h = 0.0078125f;
                poseStack.scale(0.0078125f, 0.0078125f, 0.0078125f);
                poseStack.translate(-64.0, -64.0, 0.0);
                Integer integer = MapItem.getMapId(itemStack);
                MapItemSavedData mapItemSavedData = MapItem.getSavedData(integer, ((ItemFrame)itemFrame).level);
                poseStack.translate(0.0, 0.0, -1.0);
                if (mapItemSavedData != null) {
                    int k = this.getLightVal(itemFrame, 15728850, i);
                    this.minecraft.gameRenderer.getMapRenderer().render(poseStack, multiBufferSource, integer, mapItemSavedData, true, k);
                }
            } else {
                int l = this.getLightVal(itemFrame, 0xF000F0, i);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                this.itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.FIXED, l, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, ((Entity)itemFrame).getId());
            }
        }
        poseStack.popPose();
    }

    private int getLightVal(T itemFrame, int i, int j) {
        return ((Entity)itemFrame).getType() == EntityType.GLOW_ITEM_FRAME ? i : j;
    }

    private ModelResourceLocation getFrameModelResourceLoc(T itemFrame, ItemStack itemStack) {
        boolean bl = ((ItemFrame)itemFrame).isGlowFrame();
        if (itemStack.is(Items.FILLED_MAP)) {
            return bl ? GLOW_MAP_FRAME_LOCATION : MAP_FRAME_LOCATION;
        }
        return bl ? GLOW_FRAME_LOCATION : FRAME_LOCATION;
    }

    @Override
    public Vec3 getRenderOffset(T itemFrame, float f) {
        return new Vec3((float)((HangingEntity)itemFrame).getDirection().getStepX() * 0.3f, -0.25, (float)((HangingEntity)itemFrame).getDirection().getStepZ() * 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(T itemFrame) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    protected boolean shouldShowName(T itemFrame) {
        if (!Minecraft.renderNames() || ((ItemFrame)itemFrame).getItem().isEmpty() || !((ItemFrame)itemFrame).getItem().hasCustomHoverName() || this.entityRenderDispatcher.crosshairPickEntity != itemFrame) {
            return false;
        }
        double d = this.entityRenderDispatcher.distanceToSqr((Entity)itemFrame);
        float f = ((Entity)itemFrame).isDiscrete() ? 32.0f : 64.0f;
        return d < (double)(f * f);
    }

    @Override
    protected void renderNameTag(T itemFrame, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        super.renderNameTag(itemFrame, ((ItemFrame)itemFrame).getItem().getHoverName(), poseStack, multiBufferSource, i);
    }
}

