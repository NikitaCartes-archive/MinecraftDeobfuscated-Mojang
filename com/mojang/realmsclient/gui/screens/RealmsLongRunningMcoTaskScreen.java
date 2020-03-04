/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Sets;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.util.HashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsLongRunningMcoTaskScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Screen lastScreen;
    private volatile String title = "";
    private volatile boolean error;
    private volatile String errorMessage;
    private volatile boolean aborted;
    private int animTicks;
    private final LongRunningTask task;
    private final int buttonLength = 212;
    public static final String[] SYMBOLS = new String[]{"\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _"};

    public RealmsLongRunningMcoTaskScreen(Screen screen, LongRunningTask longRunningTask) {
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
        NarrationHelper.repeatedly(this.title);
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
        this.addButton(new Button(this.width / 2 - 106, RealmsLongRunningMcoTaskScreen.row(12), 212, 20, I18n.get("gui.cancel", new Object[0]), button -> this.cancelOrBackButtonClicked()));
    }

    private void cancelOrBackButtonClicked() {
        this.aborted = true;
        this.task.abortTask();
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title, this.width / 2, RealmsLongRunningMcoTaskScreen.row(3), 0xFFFFFF);
        if (!this.error) {
            this.drawCenteredString(this.font, SYMBOLS[this.animTicks % SYMBOLS.length], this.width / 2, RealmsLongRunningMcoTaskScreen.row(8), 0x808080);
        }
        if (this.error) {
            this.drawCenteredString(this.font, this.errorMessage, this.width / 2, RealmsLongRunningMcoTaskScreen.row(8), 0xFF0000);
        }
        super.render(i, j, f);
    }

    public void error(String string) {
        this.error = true;
        this.errorMessage = string;
        NarrationHelper.now(string);
        this.buttonsClear();
        this.addButton(new Button(this.width / 2 - 106, this.height / 4 + 120 + 12, 200, 20, I18n.get("gui.back", new Object[0]), button -> this.cancelOrBackButtonClicked()));
    }

    public void buttonsClear() {
        HashSet set = Sets.newHashSet(this.buttons);
        this.children.removeIf(set::contains);
        this.buttons.clear();
    }

    public void setTitle(String string) {
        this.title = string;
    }

    public boolean aborted() {
        return this.aborted;
    }
}

