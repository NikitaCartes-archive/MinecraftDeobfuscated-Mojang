/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsLongConfirmationScreen
extends RealmsScreen {
    private final Type type;
    private final String line2;
    private final String line3;
    protected final BooleanConsumer callback;
    protected final String yesButton;
    protected final String noButton;
    private final String okButton;
    private final boolean yesNoQuestion;

    public RealmsLongConfirmationScreen(BooleanConsumer booleanConsumer, Type type, String string, String string2, boolean bl) {
        this.callback = booleanConsumer;
        this.type = type;
        this.line2 = string;
        this.line3 = string2;
        this.yesNoQuestion = bl;
        this.yesButton = I18n.get("gui.yes", new Object[0]);
        this.noButton = I18n.get("gui.no", new Object[0]);
        this.okButton = I18n.get("mco.gui.ok", new Object[0]);
    }

    @Override
    public void init() {
        NarrationHelper.now(this.type.text, this.line2, this.line3);
        if (this.yesNoQuestion) {
            this.addButton(new Button(this.width / 2 - 105, RealmsLongConfirmationScreen.row(8), 100, 20, this.yesButton, button -> this.callback.accept(true)));
            this.addButton(new Button(this.width / 2 + 5, RealmsLongConfirmationScreen.row(8), 100, 20, this.noButton, button -> this.callback.accept(false)));
        } else {
            this.addButton(new Button(this.width / 2 - 50, RealmsLongConfirmationScreen.row(8), 100, 20, this.okButton, button -> this.callback.accept(true)));
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.callback.accept(false);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.type.text, this.width / 2, RealmsLongConfirmationScreen.row(2), this.type.colorCode);
        this.drawCenteredString(this.font, this.line2, this.width / 2, RealmsLongConfirmationScreen.row(4), 0xFFFFFF);
        this.drawCenteredString(this.font, this.line3, this.width / 2, RealmsLongConfirmationScreen.row(6), 0xFFFFFF);
        super.render(i, j, f);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        Warning("Warning!", 0xFF0000),
        Info("Info!", 8226750);

        public final int colorCode;
        public final String text;

        private Type(String string2, int j) {
            this.text = string2;
            this.colorCode = j;
        }
    }
}

