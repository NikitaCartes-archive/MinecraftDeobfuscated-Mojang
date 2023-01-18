/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class ObjectSelectionList<E extends Entry<E>>
extends AbstractSelectionList<E> {
    private static final Component USAGE_NARRATION = Component.translatable("narration.selection.usage");

    public ObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (this.getItemCount() == 0) {
            return null;
        }
        if (this.isFocused() && focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
            FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)focusNavigationEvent;
            Entry entry = (Entry)this.nextEntry(arrowNavigation.direction());
            if (entry != null) {
                return ComponentPath.path(this, ComponentPath.leaf(entry));
            }
            return null;
        }
        if (!this.isFocused()) {
            Entry entry2 = (Entry)this.getSelected();
            if (entry2 == null) {
                entry2 = (Entry)this.nextEntry(focusNavigationEvent.getVerticalDirectionForInitialFocus());
            }
            if (entry2 == null) {
                return null;
            }
            return ComponentPath.path(this, ComponentPath.leaf(entry2));
        }
        return null;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        Entry entry = (Entry)this.getHovered();
        if (entry != null) {
            this.narrateListElementPosition(narrationElementOutput.nest(), entry);
            entry.updateNarration(narrationElementOutput);
        } else {
            Entry entry2 = (Entry)this.getSelected();
            if (entry2 != null) {
                this.narrateListElementPosition(narrationElementOutput.nest(), entry2);
                entry2.updateNarration(narrationElementOutput);
            }
        }
        if (this.isFocused()) {
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry<E extends Entry<E>>
    extends AbstractSelectionList.Entry<E>
    implements NarrationSupplier {
        public abstract Component getNarration();

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(NarratedElementType.TITLE, this.getNarration());
        }
    }
}

