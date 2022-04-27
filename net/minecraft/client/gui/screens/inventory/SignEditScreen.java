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
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

@Environment(value=EnvType.CLIENT)
public class SignEditScreen
extends Screen {
    private final SignBlockEntity sign;
    private int frame;
    private int line;
    private TextFieldHelper signField;
    private WoodType woodType;
    private SignRenderer.SignModel signModel;
    private final String[] messages = (String[])IntStream.range(0, 4).mapToObj(i -> signBlockEntity.getMessage(i, bl)).map(Component::getString).toArray(String[]::new);

    public SignEditScreen(SignBlockEntity signBlockEntity, boolean bl) {
        super(Component.translatable("sign.edit"));
        this.sign = signBlockEntity;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, button -> this.onDone()));
        this.sign.setEditable(false);
        this.signField = new TextFieldHelper(() -> this.messages[this.line], string -> {
            this.messages[this.line] = string;
            this.sign.setMessage(this.line, Component.literal(string));
        }, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), string -> this.minecraft.font.width((String)string) <= 90);
        BlockState blockState = this.sign.getBlockState();
        this.woodType = SignRenderer.getWoodType(blockState.getBlock());
        this.signModel = SignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null) {
            clientPacketListener.send(new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.messages[0], this.messages[1], this.messages[2], this.messages[3]));
        }
        this.sign.setEditable(true);
    }

    @Override
    public void tick() {
        ++this.frame;
        if (!this.sign.getType().isValid(this.sign.getBlockState())) {
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
            this.signField.setCursorToEnd();
            return true;
        }
        if (i == 264 || i == 257 || i == 335) {
            this.line = this.line + 1 & 3;
            this.signField.setCursorToEnd();
            return true;
        }
        if (this.signField.keyPressed(i)) {
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        int s;
        int r;
        String string;
        int p;
        Lighting.setupForFlatItems();
        this.renderBackground(poseStack);
        SignEditScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 40, 0xFFFFFF);
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
        Material material = Sheets.getSignMaterial(this.woodType);
        VertexConsumer vertexConsumer = material.buffer(bufferSource, this.signModel::renderType);
        this.signModel.stick.visible = bl;
        this.signModel.root.render(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        float k = 0.010416667f;
        poseStack.translate(0.0, 0.3333333432674408, 0.046666666865348816);
        poseStack.scale(0.010416667f, -0.010416667f, 0.010416667f);
        int l = this.sign.getColor().getTextColor();
        int m = this.signField.getCursorPos();
        int n = this.signField.getSelectionPos();
        int o = this.line * 10 - this.messages.length * 5;
        Matrix4f matrix4f = poseStack.last().pose();
        for (p = 0; p < this.messages.length; ++p) {
            string = this.messages[p];
            if (string == null) continue;
            if (this.font.isBidirectional()) {
                string = this.font.bidirectionalShaping(string);
            }
            float q = -this.minecraft.font.width(string) / 2;
            this.minecraft.font.drawInBatch(string, q, p * 10 - this.messages.length * 5, l, false, matrix4f, bufferSource, false, 0, 0xF000F0, false);
            if (p != this.line || m < 0 || !bl2) continue;
            r = this.minecraft.font.width(string.substring(0, Math.max(Math.min(m, string.length()), 0)));
            s = r - this.minecraft.font.width(string) / 2;
            if (m < string.length()) continue;
            this.minecraft.font.drawInBatch("_", s, o, l, false, matrix4f, bufferSource, false, 0, 0xF000F0, false);
        }
        bufferSource.endBatch();
        for (p = 0; p < this.messages.length; ++p) {
            string = this.messages[p];
            if (string == null || p != this.line || m < 0) continue;
            int t = this.minecraft.font.width(string.substring(0, Math.max(Math.min(m, string.length()), 0)));
            r = t - this.minecraft.font.width(string) / 2;
            if (bl2 && m < string.length()) {
                SignEditScreen.fill(poseStack, r, o - 1, r + 1, o + this.minecraft.font.lineHeight, 0xFF000000 | l);
            }
            if (n == m) continue;
            s = Math.min(m, n);
            int u = Math.max(m, n);
            int v = this.minecraft.font.width(string.substring(0, s)) - this.minecraft.font.width(string) / 2;
            int w = this.minecraft.font.width(string.substring(0, u)) - this.minecraft.font.width(string) / 2;
            int x = Math.min(v, w);
            int y = Math.max(v, w);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableTexture();
            RenderSystem.enableColorLogicOp();
            RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f, x, o + this.minecraft.font.lineHeight, 0.0f).color(0, 0, 255, 255).endVertex();
            bufferBuilder.vertex(matrix4f, y, o + this.minecraft.font.lineHeight, 0.0f).color(0, 0, 255, 255).endVertex();
            bufferBuilder.vertex(matrix4f, y, o, 0.0f).color(0, 0, 255, 255).endVertex();
            bufferBuilder.vertex(matrix4f, x, o, 0.0f).color(0, 0, 255, 255).endVertex();
            BufferUploader.drawWithShader(bufferBuilder.end());
            RenderSystem.disableColorLogicOp();
            RenderSystem.enableTexture();
        }
        poseStack.popPose();
        Lighting.setupFor3DItems();
        super.render(poseStack, i, j, f);
    }
}

