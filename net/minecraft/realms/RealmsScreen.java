/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsLabel;

@Environment(value=EnvType.CLIENT)
public abstract class RealmsScreen
extends Screen {
    public RealmsScreen() {
        super(NarratorChatListener.NO_TITLE);
    }

    protected static int row(int i) {
        return 40 + i * 13;
    }

    @Override
    public void tick() {
        for (AbstractWidget abstractWidget : this.buttons) {
            if (!(abstractWidget instanceof TickableWidget)) continue;
            ((TickableWidget)((Object)abstractWidget)).tick();
        }
    }

    public void narrateLabels() {
        List<String> list = this.children.stream().filter(RealmsLabel.class::isInstance).map(RealmsLabel.class::cast).map(RealmsLabel::getText).collect(Collectors.toList());
        NarrationHelper.now(list);
    }
}

