/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(value=EnvType.CLIENT)
public class ReceivingLevelScreen
extends Screen {
    private static final Component DOWNLOADING_TERRAIN_TEXT = new TranslatableComponent("multiplayer.downloadingTerrain");
    private static final long CHUNK_LOADING_START_WAIT_LIMIT_MS = 2000L;
    private boolean loadingPacketsReceived = false;
    private boolean oneTickSkipped = false;
    private final long createdAt = System.currentTimeMillis();

    public ReceivingLevelScreen() {
        super(NarratorChatListener.NO_TITLE);
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
        boolean bl2;
        boolean bl;
        boolean bl3 = bl = this.oneTickSkipped || System.currentTimeMillis() > this.createdAt + 2000L;
        if (!bl || this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        BlockPos blockPos = this.minecraft.player.blockPosition();
        boolean bl4 = bl2 = this.minecraft.level != null && this.minecraft.level.isOutsideBuildHeight(blockPos.getY());
        if (bl2 || this.minecraft.levelRenderer.isChunkCompiled(blockPos)) {
            this.onClose();
        }
        if (this.loadingPacketsReceived) {
            this.oneTickSkipped = true;
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

