package net.minecraft.world.item;

public interface TooltipFlag {
	boolean isAdvanced();

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
