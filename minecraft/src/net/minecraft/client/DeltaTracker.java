package net.minecraft.client;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface DeltaTracker {
	DeltaTracker ZERO = new DeltaTracker.DefaultValue(0.0F);
	DeltaTracker ONE = new DeltaTracker.DefaultValue(1.0F);

	float getGameTimeDeltaTicks();

	float getGameTimeDeltaPartialTick(boolean bl);

	float getRealtimeDeltaTicks();

	@Environment(EnvType.CLIENT)
	public static class DefaultValue implements DeltaTracker {
		private final float value;

		DefaultValue(float f) {
			this.value = f;
		}

		@Override
		public float getGameTimeDeltaTicks() {
			return this.value;
		}

		@Override
		public float getGameTimeDeltaPartialTick(boolean bl) {
			return this.value;
		}

		@Override
		public float getRealtimeDeltaTicks() {
			return this.value;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Timer implements DeltaTracker {
		private float deltaTicks;
		private float deltaTickResidual;
		private float realtimeDeltaTicks;
		private float pausedDeltaTickResidual;
		private long lastMs;
		private long lastUiMs;
		private final float msPerTick;
		private final FloatUnaryOperator targetMsptProvider;
		private boolean paused;
		private boolean frozen;

		public Timer(float f, long l, FloatUnaryOperator floatUnaryOperator) {
			this.msPerTick = 1000.0F / f;
			this.lastUiMs = this.lastMs = l;
			this.targetMsptProvider = floatUnaryOperator;
		}

		public int advanceTime(long l, boolean bl) {
			this.advanceRealTime(l);
			return bl ? this.advanceGameTime(l) : 0;
		}

		private int advanceGameTime(long l) {
			this.deltaTicks = (float)(l - this.lastMs) / this.targetMsptProvider.apply(this.msPerTick);
			this.lastMs = l;
			this.deltaTickResidual = this.deltaTickResidual + this.deltaTicks;
			int i = (int)this.deltaTickResidual;
			this.deltaTickResidual -= (float)i;
			return i;
		}

		private void advanceRealTime(long l) {
			this.realtimeDeltaTicks = (float)(l - this.lastUiMs) / this.msPerTick;
			this.lastUiMs = l;
		}

		public void updatePauseState(boolean bl) {
			if (bl) {
				this.pause();
			} else {
				this.unPause();
			}
		}

		private void pause() {
			if (!this.paused) {
				this.pausedDeltaTickResidual = this.deltaTickResidual;
			}

			this.paused = true;
		}

		private void unPause() {
			if (this.paused) {
				this.deltaTickResidual = this.pausedDeltaTickResidual;
			}

			this.paused = false;
		}

		public void updateFrozenState(boolean bl) {
			this.frozen = bl;
		}

		@Override
		public float getGameTimeDeltaTicks() {
			return this.deltaTicks;
		}

		@Override
		public float getGameTimeDeltaPartialTick(boolean bl) {
			if (!bl && this.frozen) {
				return 1.0F;
			} else {
				return this.paused ? this.pausedDeltaTickResidual : this.deltaTickResidual;
			}
		}

		@Override
		public float getRealtimeDeltaTicks() {
			return this.realtimeDeltaTicks > 7.0F ? 0.5F : this.realtimeDeltaTicks;
		}
	}
}
