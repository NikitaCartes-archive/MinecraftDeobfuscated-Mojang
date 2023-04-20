package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class AbstractContainerEventHandler implements ContainerEventHandler {
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

	@Nullable
	@Override
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
