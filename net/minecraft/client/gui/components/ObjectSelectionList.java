/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(value=EnvType.CLIENT)
public abstract class ObjectSelectionList<E extends Entry<E>>
extends AbstractSelectionList<E> {
    private static final Component USAGE_NARRATION = new TranslatableComponent("narration.selection.usage");
    private boolean inFocus;

    public ObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }

    @Override
    public boolean changeFocus(boolean bl) {
        if (!this.inFocus && this.getItemCount() == 0) {
            return false;
        }
        boolean bl2 = this.inFocus = !this.inFocus;
        if (this.inFocus && this.getSelected() == null && this.getItemCount() > 0) {
            this.moveSelection(AbstractSelectionList.SelectionDirection.DOWN);
        } else if (this.inFocus && this.getSelected() != null) {
            this.refreshSelection();
        }
        return this.inFocus;
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
        @Override
        public boolean changeFocus(boolean bl) {
            return false;
        }

        public abstract Component getNarration();

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(NarratedElementType.TITLE, this.getNarration());
        }
    }
}

