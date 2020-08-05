package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum ShoreLayer implements CastleTransformer {
	INSTANCE;

	private static final IntSet SNOWY = new IntOpenHashSet(new int[]{26, 11, 12, 13, 140, 30, 31, 158, 10});
	private static final IntSet JUNGLES = new IntOpenHashSet(new int[]{168, 169, 21, 22, 23, 149, 151});

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		if (m == 14) {
			if (Layers.isShallowOcean(i) || Layers.isShallowOcean(j) || Layers.isShallowOcean(k) || Layers.isShallowOcean(l)) {
				return 15;
			}
		} else if (JUNGLES.contains(m)) {
			if (!isJungleCompatible(i) || !isJungleCompatible(j) || !isJungleCompatible(k) || !isJungleCompatible(l)) {
				return 23;
			}

			if (Layers.isOcean(i) || Layers.isOcean(j) || Layers.isOcean(k) || Layers.isOcean(l)) {
				return 16;
			}
		} else if (m != 3 && m != 34 && m != 20) {
			if (SNOWY.contains(m)) {
				if (!Layers.isOcean(m) && (Layers.isOcean(i) || Layers.isOcean(j) || Layers.isOcean(k) || Layers.isOcean(l))) {
					return 26;
				}
			} else if (m != 37 && m != 38) {
				if (!Layers.isOcean(m) && m != 7 && m != 6 && (Layers.isOcean(i) || Layers.isOcean(j) || Layers.isOcean(k) || Layers.isOcean(l))) {
					return 16;
				}
			} else if (!Layers.isOcean(i)
				&& !Layers.isOcean(j)
				&& !Layers.isOcean(k)
				&& !Layers.isOcean(l)
				&& (!this.isMesa(i) || !this.isMesa(j) || !this.isMesa(k) || !this.isMesa(l))) {
				return 2;
			}
		} else if (!Layers.isOcean(m) && (Layers.isOcean(i) || Layers.isOcean(j) || Layers.isOcean(k) || Layers.isOcean(l))) {
			return 25;
		}

		return m;
	}

	private static boolean isJungleCompatible(int i) {
		return JUNGLES.contains(i) || i == 4 || i == 5 || Layers.isOcean(i);
	}

	private boolean isMesa(int i) {
		return i == 37 || i == 38 || i == 39 || i == 165 || i == 166 || i == 167;
	}
}
