/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.model.SignModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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
    private static final ResourceLocation OAK_TEXTURE = new ResourceLocation("textures/entity/signs/oak.png");
    private static final ResourceLocation SPRUCE_TEXTURE = new ResourceLocation("textures/entity/signs/spruce.png");
    private static final ResourceLocation BIRCH_TEXTURE = new ResourceLocation("textures/entity/signs/birch.png");
    private static final ResourceLocation ACACIA_TEXTURE = new ResourceLocation("textures/entity/signs/acacia.png");
    private static final ResourceLocation JUNGLE_TEXTURE = new ResourceLocation("textures/entity/signs/jungle.png");
    private static final ResourceLocation DARK_OAK_TEXTURE = new ResourceLocation("textures/entity/signs/dark_oak.png");
    private final SignModel signModel = new SignModel();

    @Override
    public void render(SignBlockEntity signBlockEntity, double d, double e, double f, float g, int i) {
        BlockState blockState = signBlockEntity.getBlockState();
        GlStateManager.pushMatrix();
        float h = 0.6666667f;
        if (blockState.getBlock() instanceof StandingSignBlock) {
            GlStateManager.translatef((float)d + 0.5f, (float)e + 0.5f, (float)f + 0.5f);
            GlStateManager.rotatef(-((float)(blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0f), 0.0f, 1.0f, 0.0f);
            this.signModel.getStick().visible = true;
        } else {
            GlStateManager.translatef((float)d + 0.5f, (float)e + 0.5f, (float)f + 0.5f);
            GlStateManager.rotatef(-blockState.getValue(WallSignBlock.FACING).toYRot(), 0.0f, 1.0f, 0.0f);
            GlStateManager.translatef(0.0f, -0.3125f, -0.4375f);
            this.signModel.getStick().visible = false;
        }
        if (i >= 0) {
            this.bindTexture(BREAKING_LOCATIONS[i]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(4.0f, 2.0f, 1.0f);
            GlStateManager.translatef(0.0625f, 0.0625f, 0.0625f);
            GlStateManager.matrixMode(5888);
        } else {
            this.bindTexture(this.getTexture(blockState.getBlock()));
        }
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scalef(0.6666667f, -0.6666667f, -0.6666667f);
        this.signModel.render();
        GlStateManager.popMatrix();
        Font font = this.getFont();
        float j = 0.010416667f;
        GlStateManager.translatef(0.0f, 0.33333334f, 0.046666667f);
        GlStateManager.scalef(0.010416667f, -0.010416667f, 0.010416667f);
        GlStateManager.normal3f(0.0f, 0.0f, -0.010416667f);
        GlStateManager.depthMask(false);
        int k = signBlockEntity.getColor().getTextColor();
        if (i < 0) {
            for (int l = 0; l < 4; ++l) {
                String string = signBlockEntity.getRenderMessage(l, component -> {
                    List<Component> list = ComponentRenderUtils.wrapComponents(component, 90, font, false, true);
                    return list.isEmpty() ? "" : list.get(0).getColoredString();
                });
                if (string == null) continue;
                font.draw(string, -font.width(string) / 2, l * 10 - signBlockEntity.messages.length * 5, k);
                if (l != signBlockEntity.getSelectedLine() || signBlockEntity.getCursorPos() < 0) continue;
                int m = font.width(string.substring(0, Math.max(Math.min(signBlockEntity.getCursorPos(), string.length()), 0)));
                int n = font.isBidirectional() ? -1 : 1;
                int o = (m - font.width(string) / 2) * n;
                int p = l * 10 - signBlockEntity.messages.length * 5;
                if (signBlockEntity.isShowCursor()) {
                    if (signBlockEntity.getCursorPos() < string.length()) {
                        GuiComponent.fill(o, p - 1, o + 1, p + font.lineHeight, 0xFF000000 | k);
                    } else {
                        font.draw("_", o, p, k);
                    }
                }
                if (signBlockEntity.getSelectionPos() == signBlockEntity.getCursorPos()) continue;
                int q = Math.min(signBlockEntity.getCursorPos(), signBlockEntity.getSelectionPos());
                int r = Math.max(signBlockEntity.getCursorPos(), signBlockEntity.getSelectionPos());
                int s = (font.width(string.substring(0, q)) - font.width(string) / 2) * n;
                int t = (font.width(string.substring(0, r)) - font.width(string) / 2) * n;
                this.renderHighlight(Math.min(s, t), p, Math.max(s, t), p + font.lineHeight);
            }
        }
        GlStateManager.depthMask(true);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
        if (i >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }

    private ResourceLocation getTexture(Block block) {
        if (block == Blocks.OAK_SIGN || block == Blocks.OAK_WALL_SIGN) {
            return OAK_TEXTURE;
        }
        if (block == Blocks.SPRUCE_SIGN || block == Blocks.SPRUCE_WALL_SIGN) {
            return SPRUCE_TEXTURE;
        }
        if (block == Blocks.BIRCH_SIGN || block == Blocks.BIRCH_WALL_SIGN) {
            return BIRCH_TEXTURE;
        }
        if (block == Blocks.ACACIA_SIGN || block == Blocks.ACACIA_WALL_SIGN) {
            return ACACIA_TEXTURE;
        }
        if (block == Blocks.JUNGLE_SIGN || block == Blocks.JUNGLE_WALL_SIGN) {
            return JUNGLE_TEXTURE;
        }
        if (block == Blocks.DARK_OAK_SIGN || block == Blocks.DARK_OAK_WALL_SIGN) {
            return DARK_OAK_TEXTURE;
        }
        return OAK_TEXTURE;
    }

    private void renderHighlight(int i, int j, int k, int l) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        GlStateManager.color4f(0.0f, 0.0f, 255.0f, 255.0f);
        GlStateManager.disableTexture();
        GlStateManager.enableColorLogicOp();
        GlStateManager.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(i, l, 0.0).endVertex();
        bufferBuilder.vertex(k, l, 0.0).endVertex();
        bufferBuilder.vertex(k, j, 0.0).endVertex();
        bufferBuilder.vertex(i, j, 0.0).endVertex();
        tesselator.end();
        GlStateManager.disableColorLogicOp();
        GlStateManager.enableTexture();
    }
}

