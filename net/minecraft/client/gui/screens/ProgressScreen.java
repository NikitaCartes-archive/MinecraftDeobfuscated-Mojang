/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProgressListener;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ProgressScreen
extends Screen
implements ProgressListener {
    @Nullable
    private Component header;
    @Nullable
    private Component stage;
    private int progress;
    private boolean stop;
    private final boolean clearScreenAfterStop;

    public ProgressScreen(boolean bl) {
        super(GameNarrator.NO_TITLE);
        this.clearScreenAfterStop = bl;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void progressStartNoAbort(Component component) {
        this.progressStart(component);
    }

    @Override
    public void progressStart(Component component) {
        this.header = component;
        this.progressStage(Component.translatable("progress.working"));
    }

    @Override
    public void progressStage(Component component) {
        this.stage = component;
        this.progressStagePercentage(0);
    }

    @Override
    public void progressStagePercentage(int i) {
        this.progress = i;
    }

    @Override
    public void stop() {
        this.stop = true;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (this.stop) {
            if (this.clearScreenAfterStop) {
                this.minecraft.setScreen(null);
            }
            return;
        }
        this.renderBackground(poseStack);
        if (this.header != null) {
            ProgressScreen.drawCenteredString(poseStack, this.font, this.header, this.width / 2, 70, 0xFFFFFF);
        }
        if (this.stage != null && this.progress != 0) {
            ProgressScreen.drawCenteredString(poseStack, this.font, Component.empty().append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, 0xFFFFFF);
        }
        super.render(poseStack, i, j, f);
    }
}

