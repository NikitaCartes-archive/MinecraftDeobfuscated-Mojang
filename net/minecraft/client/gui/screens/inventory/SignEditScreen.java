/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class SignEditScreen
extends Screen {
    private final SignRenderer.SignModel signModel = new SignRenderer.SignModel();
    private final SignBlockEntity sign;
    private int frame;
    private int line;
    private TextFieldHelper signField;

    public SignEditScreen(SignBlockEntity signBlockEntity) {
        super(new TranslatableComponent("sign.edit", new Object[0]));
        this.sign = signBlockEntity;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, I18n.get("gui.done", new Object[0]), button -> this.onDone()));
        this.sign.setEditable(false);
        this.signField = new TextFieldHelper(this.minecraft, () -> this.sign.getMessage(this.line).getString(), string -> this.sign.setMessage(this.line, new TextComponent((String)string)), 90);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null) {
            clientPacketListener.send(new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.sign.getMessage(0), this.sign.getMessage(1), this.sign.getMessage(2), this.sign.getMessage(3)));
        }
        this.sign.setEditable(true);
    }

    @Override
    public void tick() {
        ++this.frame;
        if (!this.sign.getType().isValid(this.sign.getBlockState().getBlock())) {
            this.onDone();
        }
    }

    private void onDone() {
        this.sign.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean charTyped(char c, int i) {
        this.signField.charTyped(c);
        return true;
    }

    @Override
    public void onClose() {
        this.onDone();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 265) {
            this.line = this.line - 1 & 3;
            this.signField.setEnd();
            return true;
        }
        if (i == 264 || i == 257 || i == 335) {
            this.line = this.line + 1 & 3;
            this.signField.setEnd();
            return true;
        }
        if (this.signField.keyPressed(i)) {
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(int i, int j, float f) {
        int u;
        int t;
        String string;
        int r;
        Lighting.setupForFlatItems();
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 40, 0xFFFFFF);
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(this.width / 2, 0.0, 50.0);
        float g = 93.75f;
        poseStack.scale(93.75f, -93.75f, 93.75f);
        poseStack.translate(0.0, -1.3125, 0.0);
        BlockState blockState = this.sign.getBlockState();
        boolean bl = blockState.getBlock() instanceof StandingSignBlock;
        if (!bl) {
            poseStack.translate(0.0, -0.3125, 0.0);
        }
        boolean bl2 = this.frame / 6 % 2 == 0;
        float h = 0.6666667f;
        poseStack.pushPose();
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        Material material = SignRenderer.getMaterial(blockState.getBlock());
        VertexConsumer vertexConsumer = material.buffer(bufferSource, this.signModel::renderType);
        this.signModel.sign.render(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
        if (bl) {
            this.signModel.stick.render(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        float k = 0.010416667f;
        poseStack.translate(0.0, 0.3333333432674408, 0.046666666865348816);
        poseStack.scale(0.010416667f, -0.010416667f, 0.010416667f);
        int l = this.sign.getColor().getTextColor();
        String[] strings = new String[4];
        for (int m = 0; m < strings.length; ++m) {
            strings[m] = this.sign.getRenderMessage(m, component -> {
                List<Component> list = ComponentRenderUtils.wrapComponents(component, 90, this.minecraft.font, false, true);
                return list.isEmpty() ? "" : list.get(0).getColoredString();
            });
        }
        Matrix4f matrix4f = poseStack.last().pose();
        int n = this.signField.getCursorPos();
        int o = this.signField.getSelectionPos();
        int p = this.minecraft.font.isBidirectional() ? -1 : 1;
        int q = this.line * 10 - this.sign.messages.length * 5;
        for (r = 0; r < strings.length; ++r) {
            string = strings[r];
            if (string == null) continue;
            float s = -this.minecraft.font.width(string) / 2;
            this.minecraft.font.drawInBatch(string, s, r * 10 - this.sign.messages.length * 5, l, false, matrix4f, bufferSource, false, 0, 0xF000F0);
            if (r != this.line || n < 0 || !bl2) continue;
            t = this.minecraft.font.width(string.substring(0, Math.max(Math.min(n, string.length()), 0)));
            u = (t - this.minecraft.font.width(string) / 2) * p;
            if (n < string.length()) continue;
            this.minecraft.font.drawInBatch("_", u, q, l, false, matrix4f, bufferSource, false, 0, 0xF000F0);
        }
        bufferSource.endBatch();
        for (r = 0; r < strings.length; ++r) {
            string = strings[r];
            if (string == null || r != this.line || n < 0) continue;
            int v = this.minecraft.font.width(string.substring(0, Math.max(Math.min(n, string.length()), 0)));
            t = (v - this.minecraft.font.width(string) / 2) * p;
            if (bl2 && n < string.length()) {
                SignEditScreen.fill(matrix4f, t, q - 1, t + 1, q + this.minecraft.font.lineHeight, 0xFF000000 | l);
            }
            if (o == n) continue;
            u = Math.min(n, o);
            int w = Math.max(n, o);
            int x = (this.minecraft.font.width(string.substring(0, u)) - this.minecraft.font.width(string) / 2) * p;
            int y = (this.minecraft.font.width(string.substring(0, w)) - this.minecraft.font.width(string) / 2) * p;
            int z = Math.min(x, y);
            int aa = Math.max(x, y);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            RenderSystem.disableTexture();
            RenderSystem.enableColorLogicOp();
            RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f, z, q + this.minecraft.font.lineHeight, 0.0f).color(0, 0, 255, 255).endVertex();
            bufferBuilder.vertex(matrix4f, aa, q + this.minecraft.font.lineHeight, 0.0f).color(0, 0, 255, 255).endVertex();
            bufferBuilder.vertex(matrix4f, aa, q, 0.0f).color(0, 0, 255, 255).endVertex();
            bufferBuilder.vertex(matrix4f, z, q, 0.0f).color(0, 0, 255, 255).endVertex();
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
            RenderSystem.disableColorLogicOp();
            RenderSystem.enableTexture();
        }
        poseStack.popPose();
        Lighting.setupFor3DItems();
        super.render(i, j, f);
    }
}

