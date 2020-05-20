package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class SpruceFoliagePlacer extends FoliagePlacer {
	public static final Codec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance)
				.<Integer, Integer>and(
					instance.group(
						Codec.INT.fieldOf("trunk_height").forGetter(spruceFoliagePlacer -> spruceFoliagePlacer.trunkHeight),
						Codec.INT.fieldOf("trunk_height_random").forGetter(spruceFoliagePlacer -> spruceFoliagePlacer.trunkHeightRandom)
					)
				)
				.apply(instance, SpruceFoliagePlacer::new)
	);
	private final int trunkHeight;
	private final int trunkHeightRandom;

	public SpruceFoliagePlacer(int i, int j, int k, int l, int m, int n) {
		super(i, j, k, l);
		this.trunkHeight = m;
		this.trunkHeightRandom = n;
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
	}

	@Override
	protected void createFoliage(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k,
		Set<BlockPos> set,
		int l
	) {
		BlockPos blockPos = foliageAttachment.foliagePos();
		int m = random.nextInt(2);
		int n = 1;
		int o = 0;

		for (int p = l; p >= -j; p--) {
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, m, set, p, foliageAttachment.doubleTrunk());
			if (m >= n) {
				m = o;
				o = 1;
				n = Math.min(n + 1, k + foliageAttachment.radiusOffset());
			} else {
				m++;
			}
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return i - this.trunkHeight - random.nextInt(this.trunkHeightRandom + 1);
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i == l && k == l && l > 0;
	}
}
