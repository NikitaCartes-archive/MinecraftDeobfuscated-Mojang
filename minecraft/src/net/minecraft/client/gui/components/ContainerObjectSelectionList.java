package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

@Environment(EnvType.CLIENT)
public abstract class ContainerObjectSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
	public ContainerObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
	}

	@Override
	public boolean changeFocus(boolean bl) {
		boolean bl2 = super.changeFocus(bl);
		if (bl2) {
			this.ensureVisible(this.getFocused());
		}

		return bl2;
	}

	@Override
	protected boolean isSelectedItem(int i) {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements ContainerEventHandler {
		@Nullable
		private GuiEventListener focused;
		private boolean dragging;

		@Override
		public boolean isDragging() {
			return this.dragging;
		}

		@Override
		public void setDragging(boolean bl) {
			this.dragging = bl;
		}

		@Override
		public void setFocused(@Nullable GuiEventListener guiEventListener) {
			this.focused = guiEventListener;
		}

		@Nullable
		@Override
		public GuiEventListener getFocused() {
			return this.focused;
		}
	}
}
