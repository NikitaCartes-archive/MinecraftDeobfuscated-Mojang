package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.minecraft.world.level.ChunkPos;

public interface ChunkTrackingView {
	ChunkTrackingView EMPTY = new ChunkTrackingView() {
		@Override
		public boolean contains(int i, int j, boolean bl) {
			return false;
		}

		@Override
		public void forEach(Consumer<ChunkPos> consumer) {
		}
	};

	static ChunkTrackingView of(ChunkPos chunkPos, int i) {
		return new ChunkTrackingView.Positioned(chunkPos, i);
	}

	static void difference(ChunkTrackingView chunkTrackingView, ChunkTrackingView chunkTrackingView2, Consumer<ChunkPos> consumer, Consumer<ChunkPos> consumer2) {
		if (!chunkTrackingView.equals(chunkTrackingView2)) {
			if (chunkTrackingView instanceof ChunkTrackingView.Positioned positioned
				&& chunkTrackingView2 instanceof ChunkTrackingView.Positioned positioned2
				&& positioned.squareIntersects(positioned2)) {
				int i = Math.min(positioned.minX(), positioned2.minX());
				int j = Math.min(positioned.minZ(), positioned2.minZ());
				int k = Math.max(positioned.maxX(), positioned2.maxX());
				int l = Math.max(positioned.maxZ(), positioned2.maxZ());

				for (int m = i; m <= k; m++) {
					for (int n = j; n <= l; n++) {
						boolean bl = positioned.contains(m, n);
						boolean bl2 = positioned2.contains(m, n);
						if (bl != bl2) {
							if (bl2) {
								consumer.accept(new ChunkPos(m, n));
							} else {
								consumer2.accept(new ChunkPos(m, n));
							}
						}
					}
				}

				return;
			}

			chunkTrackingView.forEach(consumer2);
			chunkTrackingView2.forEach(consumer);
		}
	}

	default boolean contains(ChunkPos chunkPos) {
		return this.contains(chunkPos.x, chunkPos.z);
	}

	default boolean contains(int i, int j) {
		return this.contains(i, j, true);
	}

	boolean contains(int i, int j, boolean bl);

	void forEach(Consumer<ChunkPos> consumer);

	default boolean isInViewDistance(int i, int j) {
		return this.contains(i, j, false);
	}

	static boolean isInViewDistance(int i, int j, int k, int l, int m) {
		return isWithinDistance(i, j, k, l, m, false);
	}

	static boolean isWithinDistance(int i, int j, int k, int l, int m, boolean bl) {
		int n = Math.max(0, Math.abs(l - i) - 1);
		int o = Math.max(0, Math.abs(m - j) - 1);
		long p = (long)Math.max(0, Math.max(n, o) - (bl ? 1 : 0));
		long q = (long)Math.min(n, o);
		long r = q * q + p * p;
		int s = k * k;
		return r < (long)s;
	}

	public static record Positioned(ChunkPos center, int viewDistance) implements ChunkTrackingView {
		int minX() {
			return this.center.x - this.viewDistance - 1;
		}

		int minZ() {
			return this.center.z - this.viewDistance - 1;
		}

		int maxX() {
			return this.center.x + this.viewDistance + 1;
		}

		int maxZ() {
			return this.center.z + this.viewDistance + 1;
		}

		@VisibleForTesting
		protected boolean squareIntersects(ChunkTrackingView.Positioned positioned) {
			return this.minX() <= positioned.maxX() && this.maxX() >= positioned.minX() && this.minZ() <= positioned.maxZ() && this.maxZ() >= positioned.minZ();
		}

		@Override
		public boolean contains(int i, int j, boolean bl) {
			return ChunkTrackingView.isWithinDistance(this.center.x, this.center.z, this.viewDistance, i, j, bl);
		}

		@Override
		public void forEach(Consumer<ChunkPos> consumer) {
			for (int i = this.minX(); i <= this.maxX(); i++) {
				for (int j = this.minZ(); j <= this.maxZ(); j++) {
					if (this.contains(i, j)) {
						consumer.accept(new ChunkPos(i, j));
					}
				}
			}
		}
	}
}
