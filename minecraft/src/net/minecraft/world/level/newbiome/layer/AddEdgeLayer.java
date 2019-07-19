package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public class AddEdgeLayer {
	public static enum CoolWarm implements CastleTransformer {
		INSTANCE;

		@Override
		public int apply(Context context, int i, int j, int k, int l, int m) {
			return m != 1 || i != 3 && j != 3 && l != 3 && k != 3 && i != 4 && j != 4 && l != 4 && k != 4 ? m : 2;
		}
	}

	public static enum HeatIce implements CastleTransformer {
		INSTANCE;

		@Override
		public int apply(Context context, int i, int j, int k, int l, int m) {
			return m != 4 || i != 1 && j != 1 && l != 1 && k != 1 && i != 2 && j != 2 && l != 2 && k != 2 ? m : 3;
		}
	}

	public static enum IntroduceSpecial implements C0Transformer {
		INSTANCE;

		@Override
		public int apply(Context context, int i) {
			if (!Layers.isShallowOcean(i) && context.nextRandom(13) == 0) {
				i |= 1 + context.nextRandom(15) << 8 & 3840;
			}

			return i;
		}
	}
}
