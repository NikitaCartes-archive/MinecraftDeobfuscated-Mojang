package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SpruceFoliagePlacer extends FoliagePlacer {
	public static final Codec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance)
				.and(UniformInt.codec(0, 16, 8).fieldOf("trunk_height").forGetter(spruceFoliagePlacer -> spruceFoliagePlacer.trunkHeight))
				.apply(instance, SpruceFoliagePlacer::new)
	);
	private final UniformInt trunkHeight;

	public SpruceFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, UniformInt uniformInt3) {
		super(uniformInt, uniformInt2);
		this.trunkHeight = uniformInt3;
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
		int l,
		BoundingBox boundingBox
	) {
		BlockPos blockPos = foliageAttachment.foliagePos();
		int m = random.nextInt(2);
		int n = 1;
		int o = 0;

		for (int p = l; p >= -j; p--) {
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, m, set, p, foliageAttachment.doubleTrunk(), boundingBox);
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
		return Math.max(4, i - this.trunkHeight.sample(random));
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i == l && k == l && l > 0;
	}
}
