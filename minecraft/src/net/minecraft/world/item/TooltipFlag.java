package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TooltipFlag {
	boolean isAdvanced();

	@Environment(EnvType.CLIENT)
	public static enum Default implements TooltipFlag {
		NORMAL(false),
		ADVANCED(true);

		private final boolean advanced;

		private Default(boolean bl) {
			this.advanced = bl;
		}

		@Override
		public boolean isAdvanced() {
			return this.advanced;
		}
	}
}
