/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;

@Environment(value=EnvType.CLIENT)
public abstract class ObjectSelectionList<E extends AbstractSelectionList.Entry<E>>
extends AbstractSelectionList<E> {
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

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry<E extends Entry<E>>
    extends AbstractSelectionList.Entry<E> {
        @Override
        public boolean changeFocus(boolean bl) {
            return false;
        }
    }
}

