package net.minecraft.world.level.border;

public interface BorderChangeListener {
	void onBorderSizeSet(WorldBorder worldBorder, double d);

	void onBorderSizeLerping(WorldBorder worldBorder, double d, double e, long l);

	void onBorderCenterSet(WorldBorder worldBorder, double d, double e);

	void onBorderSetWarningTime(WorldBorder worldBorder, int i);

	void onBorderSetWarningBlocks(WorldBorder worldBorder, int i);

	void onBorderSetDamagePerBlock(WorldBorder worldBorder, double d);

	void onBorderSetDamageSafeZOne(WorldBorder worldBorder, double d);

	public static class DelegateBorderChangeListener implements BorderChangeListener {
		private final WorldBorder worldBorder;

		public DelegateBorderChangeListener(WorldBorder worldBorder) {
			this.worldBorder = worldBorder;
		}

		@Override
		public void onBorderSizeSet(WorldBorder worldBorder, double d) {
			this.worldBorder.setSize(d);
		}

		@Override
		public void onBorderSizeLerping(WorldBorder worldBorder, double d, double e, long l) {
			this.worldBorder.lerpSizeBetween(d, e, l);
		}

		@Override
		public void onBorderCenterSet(WorldBorder worldBorder, double d, double e) {
			this.worldBorder.setCenter(d, e);
		}

		@Override
		public void onBorderSetWarningTime(WorldBorder worldBorder, int i) {
			this.worldBorder.setWarningTime(i);
		}

		@Override
		public void onBorderSetWarningBlocks(WorldBorder worldBorder, int i) {
			this.worldBorder.setWarningBlocks(i);
		}

		@Override
		public void onBorderSetDamagePerBlock(WorldBorder worldBorder, double d) {
			this.worldBorder.setDamagePerBlock(d);
		}

		@Override
		public void onBorderSetDamageSafeZOne(WorldBorder worldBorder, double d) {
			this.worldBorder.setDamageSafeZone(d);
		}
	}
}
