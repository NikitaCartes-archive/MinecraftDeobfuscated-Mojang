package net.minecraft.client.gui.narration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.TabOrderedElement;

@Environment(EnvType.CLIENT)
public interface NarratableEntry extends TabOrderedElement, NarrationSupplier {
	NarratableEntry.NarrationPriority narrationPriority();

	default boolean isActive() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public static enum NarrationPriority {
		NONE,
		HOVERED,
		FOCUSED;

		public boolean isTerminal() {
			return this == FOCUSED;
		}
	}
}
