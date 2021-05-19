package net.minecraft.client.gui.narration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface NarratableEntry extends NarrationSupplier {
	NarratableEntry.NarrationPriority narrationPriority();

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
