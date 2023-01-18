/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractContainerEventHandler
extends GuiComponent
implements ContainerEventHandler {
    @Nullable
    private GuiEventListener focused;
    private boolean isDragging;

    @Override
    public final boolean isDragging() {
        return this.isDragging;
    }

    @Override
    public final void setDragging(boolean bl) {
        this.isDragging = bl;
    }

    @Override
    @Nullable
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }
        if (guiEventListener != null) {
            guiEventListener.setFocused(true);
        }
        this.focused = guiEventListener;
    }
}

