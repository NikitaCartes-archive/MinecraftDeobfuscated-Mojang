/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class SignRenderer
extends BlockEntityRenderer<SignBlockEntity> {
    private final ModelPart sign = new ModelPart(64, 32, 0, 0);
    private final ModelPart stick;

    public SignRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
        this.sign.addBox(-12.0f, -14.0f, -1.0f, 24.0f, 12.0f, 2.0f, 0.0f);
        this.stick = new ModelPart(64, 32, 0, 14);
        this.stick.addBox(-1.0f, -2.0f, -1.0f, 2.0f, 14.0f, 2.0f, 0.0f);
    }

    @Override
    public void render(SignBlockEntity signBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float k;
        BlockState blockState = signBlockEntity.getBlockState();
        poseStack.pushPose();
        float h = 0.6666667f;
        if (blockState.getBlock() instanceof StandingSignBlock) {
            poseStack.translate(0.5, 0.5, 0.5);
            k = -((float)(blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0f);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
            this.stick.visible = true;
        } else {
            poseStack.translate(0.5, 0.5, 0.5);
            k = -blockState.getValue(WallSignBlock.FACING).toYRot();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
            poseStack.translate(0.0, -0.3125, -0.4375);
            this.stick.visible = false;
        }
        TextureAtlasSprite textureAtlasSprite = this.getSprite(this.getTexture(blockState.getBlock()));
        poseStack.pushPose();
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        this.sign.render(poseStack, vertexConsumer, 0.0625f, i, j, textureAtlasSprite);
        this.stick.render(poseStack, vertexConsumer, 0.0625f, i, j, textureAtlasSprite);
        poseStack.popPose();
        Font font = this.renderer.getFont();
        float l = 0.010416667f;
        poseStack.translate(0.0, 0.3333333432674408, 0.046666666865348816);
        poseStack.scale(0.010416667f, -0.010416667f, 0.010416667f);
        int m = signBlockEntity.getColor().getTextColor();
        for (int n = 0; n < 4; ++n) {
            String string = signBlockEntity.getRenderMessage(n, component -> {
                List<Component> list = ComponentRenderUtils.wrapComponents(component, 90, font, false, true);
                return list.isEmpty() ? "" : list.get(0).getColoredString();
            });
            if (string == null) continue;
            float o = -font.width(string) / 2;
            font.drawInBatch(string, o, n * 10 - signBlockEntity.messages.length * 5, m, false, poseStack.getPose(), multiBufferSource, false, 0, i);
            if (n != signBlockEntity.getSelectedLine() || signBlockEntity.getCursorPos() < 0) continue;
            int p = font.width(string.substring(0, Math.max(Math.min(signBlockEntity.getCursorPos(), string.length()), 0)));
            int q = font.isBidirectional() ? -1 : 1;
            int r = (p - font.width(string) / 2) * q;
            int s = n * 10 - signBlockEntity.messages.length * 5;
            if (signBlockEntity.isShowCursor()) {
                if (signBlockEntity.getCursorPos() < string.length()) {
                    GuiComponent.fill(r, s - 1, r + 1, s + font.lineHeight, 0xFF000000 | m);
                } else {
                    font.drawInBatch("_", r, s, m, false, poseStack.getPose(), multiBufferSource, false, 0, i);
                }
            }
            if (signBlockEntity.getSelectionPos() == signBlockEntity.getCursorPos()) continue;
            int t = Math.min(signBlockEntity.getCursorPos(), signBlockEntity.getSelectionPos());
            int u = Math.max(signBlockEntity.getCursorPos(), signBlockEntity.getSelectionPos());
            int v = (font.width(string.substring(0, t)) - font.width(string) / 2) * q;
            int w = (font.width(string.substring(0, u)) - font.width(string) / 2) * q;
            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(poseStack.getPose());
            this.renderHighlight(Math.min(v, w), s, Math.max(v, w), s + font.lineHeight);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.popMatrix();
        }
        poseStack.popPose();
    }

    private ResourceLocation getTexture(Block block) {
        if (block == Blocks.OAK_SIGN || block == Blocks.OAK_WALL_SIGN) {
            return ModelBakery.OAK_SIGN_TEXTURE;
        }
        if (block == Blocks.SPRUCE_SIGN || block == Blocks.SPRUCE_WALL_SIGN) {
            return ModelBakery.SPRUCE_SIGN_TEXTURE;
        }
        if (block == Blocks.BIRCH_SIGN || block == Blocks.BIRCH_WALL_SIGN) {
            return ModelBakery.BIRCH_SIGN_TEXTURE;
        }
        if (block == Blocks.ACACIA_SIGN || block == Blocks.ACACIA_WALL_SIGN) {
            return ModelBakery.ACACIA_SIGN_TEXTURE;
        }
        if (block == Blocks.JUNGLE_SIGN || block == Blocks.JUNGLE_WALL_SIGN) {
            return ModelBakery.JUNGLE_SIGN_TEXTURE;
        }
        if (block == Blocks.DARK_OAK_SIGN || block == Blocks.DARK_OAK_WALL_SIGN) {
            return ModelBakery.DARK_OAK_SIGN_TEXTURE;
        }
        return ModelBakery.OAK_SIGN_TEXTURE;
    }

    private void renderHighlight(int i, int j, int k, int l) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.color4f(0.0f, 0.0f, 1.0f, 1.0f);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(i, l, 0.0).endVertex();
        bufferBuilder.vertex(k, l, 0.0).endVertex();
        bufferBuilder.vertex(k, j, 0.0).endVertex();
        bufferBuilder.vertex(i, j, 0.0).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }
}

