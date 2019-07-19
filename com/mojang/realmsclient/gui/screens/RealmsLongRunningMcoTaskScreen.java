/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.LongRunningTask;
import com.mojang.realmsclient.gui.RealmsConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsLongRunningMcoTaskScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final int BUTTON_CANCEL_ID = 666;
    private final int BUTTON_BACK_ID = 667;
    private final RealmsScreen lastScreen;
    private final LongRunningTask taskThread;
    private volatile String title = "";
    private volatile boolean error;
    private volatile String errorMessage;
    private volatile boolean aborted;
    private int animTicks;
    private final LongRunningTask task;
    private final int buttonLength = 212;
    public static final String[] symbols = new String[]{"\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _"};

    public RealmsLongRunningMcoTaskScreen(RealmsScreen realmsScreen, LongRunningTask longRunningTask) {
        this.lastScreen = realmsScreen;
        this.task = longRunningTask;
        longRunningTask.setScreen(this);
        this.taskThread = longRunningTask;
    }

    public void start() {
        Thread thread = new Thread((Runnable)this.taskThread, "Realms-long-running-task");
        thread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    @Override
    public void tick() {
        super.tick();
        Realms.narrateRepeatedly(this.title);
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
        this.buttonsAdd(new RealmsButton(666, this.width() / 2 - 106, RealmsConstants.row(12), 212, 20, RealmsLongRunningMcoTaskScreen.getLocalizedString("gui.cancel")){

            @Override
            public void onPress() {
                RealmsLongRunningMcoTaskScreen.this.cancelOrBackButtonClicked();
            }
        });
    }

    private void cancelOrBackButtonClicked() {
        this.aborted = true;
        this.task.abortTask();
        Realms.setScreen(this.lastScreen);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.title, this.width() / 2, RealmsConstants.row(3), 0xFFFFFF);
        if (!this.error) {
            this.drawCenteredString(symbols[this.animTicks % symbols.length], this.width() / 2, RealmsConstants.row(8), 0x808080);
        }
        if (this.error) {
            this.drawCenteredString(this.errorMessage, this.width() / 2, RealmsConstants.row(8), 0xFF0000);
        }
        super.render(i, j, f);
    }

    public void error(String string) {
        this.error = true;
        this.errorMessage = string;
        Realms.narrateNow(string);
        this.buttonsClear();
        this.buttonsAdd(new RealmsButton(667, this.width() / 2 - 106, this.height() / 4 + 120 + 12, RealmsLongRunningMcoTaskScreen.getLocalizedString("gui.back")){

            @Override
            public void onPress() {
                RealmsLongRunningMcoTaskScreen.this.cancelOrBackButtonClicked();
            }
        });
    }

    public void setTitle(String string) {
        this.title = string;
    }

    public boolean aborted() {
        return this.aborted;
    }
}

