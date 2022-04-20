/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.time.Duration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RepeatedNarrator;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsLongRunningMcoTaskScreen
extends RealmsScreen
implements ErrorCallback {
    private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Screen lastScreen;
    private volatile Component title = CommonComponents.EMPTY;
    @Nullable
    private volatile Component errorMessage;
    private volatile boolean aborted;
    private int animTicks;
    private final LongRunningTask task;
    private final int buttonLength = 212;
    private Button cancelOrBackButton;
    public static final String[] SYMBOLS = new String[]{"\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _"};

    public RealmsLongRunningMcoTaskScreen(Screen screen, LongRunningTask longRunningTask) {
        super(NarratorChatListener.NO_TITLE);
        this.lastScreen = screen;
        this.task = longRunningTask;
        longRunningTask.setScreen(this);
        Thread thread = new Thread((Runnable)longRunningTask, "Realms-long-running-task");
        thread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    @Override
    public void tick() {
        super.tick();
        REPEATED_NARRATOR.narrate(this.title);
        ++this.animTicks;
        this.task.tick();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.cancelOrBackButtonClicked();
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void init() {
        this.task.init();
        this.cancelOrBackButton = this.addRenderableWidget(new Button(this.width / 2 - 106, RealmsLongRunningMcoTaskScreen.row(12), 212, 20, CommonComponents.GUI_CANCEL, button -> this.cancelOrBackButtonClicked()));
    }

    private void cancelOrBackButtonClicked() {
        this.aborted = true;
        this.task.abortTask();
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        RealmsLongRunningMcoTaskScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, RealmsLongRunningMcoTaskScreen.row(3), 0xFFFFFF);
        Component component = this.errorMessage;
        if (component == null) {
            RealmsLongRunningMcoTaskScreen.drawCenteredString(poseStack, this.font, SYMBOLS[this.animTicks % SYMBOLS.length], this.width / 2, RealmsLongRunningMcoTaskScreen.row(8), 0x808080);
        } else {
            RealmsLongRunningMcoTaskScreen.drawCenteredString(poseStack, this.font, component, this.width / 2, RealmsLongRunningMcoTaskScreen.row(8), 0xFF0000);
        }
        super.render(poseStack, i, j, f);
    }

    @Override
    public void error(Component component) {
        this.errorMessage = component;
        NarratorChatListener.INSTANCE.sayNow(component);
        this.minecraft.execute(() -> {
            this.removeWidget(this.cancelOrBackButton);
            this.cancelOrBackButton = this.addRenderableWidget(new Button(this.width / 2 - 106, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_BACK, button -> this.cancelOrBackButtonClicked()));
        });
    }

    public void setTitle(Component component) {
        this.title = component;
    }

    public boolean aborted() {
        return this.aborted;
    }
}

