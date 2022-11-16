/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class ReceivingLevelScreen
extends Screen {
    private static final Component DOWNLOADING_TERRAIN_TEXT = Component.translatable("multiplayer.downloadingTerrain");
    private static final long CHUNK_LOADING_START_WAIT_LIMIT_MS = 30000L;
    private boolean loadingPacketsReceived = false;
    private boolean oneTickSkipped = false;
    private final long createdAt = System.currentTimeMillis();

    public ReceivingLevelScreen() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(0);
        ReceivingLevelScreen.drawCenteredString(poseStack, this.font, DOWNLOADING_TERRAIN_TEXT, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() > this.createdAt + 30000L) {
            this.onClose();
            return;
        }
        if (this.oneTickSkipped) {
            boolean bl;
            if (this.minecraft.player == null) {
                return;
            }
            BlockPos blockPos = this.minecraft.player.blockPosition();
            boolean bl2 = bl = this.minecraft.level != null && this.minecraft.level.isOutsideBuildHeight(blockPos.getY());
            if (bl || this.minecraft.levelRenderer.isChunkCompiled(blockPos) || this.minecraft.player.isSpectator() || !this.minecraft.player.isAlive()) {
                this.onClose();
            }
        } else {
            this.oneTickSkipped = this.loadingPacketsReceived;
        }
    }

    public void loadingPacketsReceived() {
        this.loadingPacketsReceived = true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

