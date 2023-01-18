package net.minecraft.client.gui;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

@Environment(EnvType.CLIENT)
public interface ComponentPath {
	static ComponentPath leaf(GuiEventListener guiEventListener) {
		return new ComponentPath.Leaf(guiEventListener);
	}

	@Nullable
	static ComponentPath path(ContainerEventHandler containerEventHandler, @Nullable ComponentPath componentPath) {
		return componentPath == null ? null : new ComponentPath.Path(containerEventHandler, componentPath);
	}

	static ComponentPath path(GuiEventListener guiEventListener, ContainerEventHandler... containerEventHandlers) {
		ComponentPath componentPath = leaf(guiEventListener);

		for (ContainerEventHandler containerEventHandler : containerEventHandlers) {
			componentPath = path(containerEventHandler, componentPath);
		}

		return componentPath;
	}

	GuiEventListener component();

	void applyFocus(boolean bl);

	@Environment(EnvType.CLIENT)
	public static record Leaf(GuiEventListener component) implements ComponentPath {
		@Override
		public void applyFocus(boolean bl) {
			this.component.setFocused(bl);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Path(ContainerEventHandler component, ComponentPath childPath) implements ComponentPath {
		@Override
		public void applyFocus(boolean bl) {
			if (!bl) {
				this.component.setFocused(null);
			} else {
				this.component.setFocused(this.childPath.component());
			}

			this.childPath.applyFocus(bl);
		}
	}
}
