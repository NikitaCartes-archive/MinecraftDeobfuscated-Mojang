package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public abstract class ObjectSelectionList<E extends AbstractSelectionList.Entry<E>> extends AbstractSelectionList<E> {
	private boolean inFocus;

	public ObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
	}

	@Override
	public boolean changeFocus(boolean bl) {
		if (!this.inFocus && this.getItemCount() == 0) {
			return false;
		} else {
			this.inFocus = !this.inFocus;
			if (this.inFocus && this.getSelected() == null && this.getItemCount() > 0) {
				this.moveSelection(1);
			} else if (this.inFocus && this.getSelected() != null) {
				this.moveSelection(0);
			}

			return this.inFocus;
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> {
		@Override
		public boolean changeFocus(boolean bl) {
			return false;
		}
	}
}
