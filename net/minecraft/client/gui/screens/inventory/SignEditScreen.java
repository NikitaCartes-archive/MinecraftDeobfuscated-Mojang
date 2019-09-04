/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class SignEditScreen
extends Screen {
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
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 40, 0xFFFFFF);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(this.width / 2, 0.0f, 50.0f);
        float g = 93.75f;
        RenderSystem.scalef(-93.75f, -93.75f, -93.75f);
        RenderSystem.rotatef(180.0f, 0.0f, 1.0f, 0.0f);
        BlockState blockState = this.sign.getBlockState();
        float h = blockState.getBlock() instanceof StandingSignBlock ? (float)(blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0f : blockState.getValue(WallSignBlock.FACING).toYRot();
        RenderSystem.rotatef(h, 0.0f, 1.0f, 0.0f);
        RenderSystem.translatef(0.0f, -1.0625f, 0.0f);
        this.sign.setCursorInfo(this.line, this.signField.getCursorPos(), this.signField.getSelectionPos(), this.frame / 6 % 2 == 0);
        BlockEntityRenderDispatcher.instance.render(this.sign, -0.5, -0.75, -0.5, 0.0f);
        this.sign.resetCursorInfo();
        RenderSystem.popMatrix();
        super.render(i, j, f);
    }
}

