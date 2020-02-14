package net.minecraft.util;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;

public class BlockFinder {
	public static Optional<BlockPos> findClosestMatchingBlockPos(BlockPos blockPos, int i, int j, Predicate<BlockPos> predicate) {
		if (predicate.test(blockPos)) {
			return Optional.of(blockPos);
		} else {
			int k = Math.max(i, j);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos);

			for (int l = 1; l <= k; l++) {
				for (int m = -l; m <= l; m++) {
					if (m <= i && m >= -i) {
						boolean bl = m == -l || m == l;

						for (int n = -l; n <= l; n++) {
							if (n <= j && n >= -j) {
								boolean bl2 = n == -l || n == l;

								for (int o = -l; o <= l; o++) {
									if (o <= i && o >= -i) {
										boolean bl3 = o == -l || o == l;
										if ((bl || bl2 || bl3) && predicate.test(mutableBlockPos.set(blockPos).move(m, n, o))) {
											return Optional.of(blockPos.offset(m, n, o));
										}
									}
								}
							}
						}
					}
				}
			}

			return Optional.empty();
		}
	}
}
