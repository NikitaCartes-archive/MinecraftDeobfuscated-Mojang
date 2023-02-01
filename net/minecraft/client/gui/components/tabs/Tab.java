/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.tabs;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public interface Tab {
    public Component getTabTitle();

    public void visitChildren(Consumer<AbstractWidget> var1);

    public void doLayout(ScreenRectangle var1);

    default public void tick() {
    }
}

