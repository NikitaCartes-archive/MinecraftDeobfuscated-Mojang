package net.minecraft.world.item;

public interface TooltipFlag {
	TooltipFlag.Default NORMAL = new TooltipFlag.Default(false, false);
	TooltipFlag.Default ADVANCED = new TooltipFlag.Default(true, false);

	boolean isAdvanced();

	boolean isCreative();

	public static record Default(boolean advanced, boolean creative) implements TooltipFlag {
		@Override
		public boolean isAdvanced() {
			return this.advanced;
		}

		@Override
		public boolean isCreative() {
			return this.creative;
		}

		public TooltipFlag.Default asCreative() {
			return new TooltipFlag.Default(this.advanced, true);
		}
	}
}
