package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaPineFoliagePlacer extends FoliagePlacer {
	public static final Codec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance)
				.and(UniformInt.codec(0, 16, 8).fieldOf("crown_height").forGetter(megaPineFoliagePlacer -> megaPineFoliagePlacer.crownHeight))
				.apply(instance, MegaPineFoliagePlacer::new)
	);
	private final UniformInt crownHeight;

	public MegaPineFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, UniformInt uniformInt3) {
		super(uniformInt, uniformInt2);
		this.crownHeight = uniformInt3;
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
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
		int m = 0;

		for (int n = blockPos.getY() - j + l; n <= blockPos.getY() + l; n++) {
			int o = blockPos.getY() - n;
			int p = k + foliageAttachment.radiusOffset() + Mth.floor((float)o / (float)j * 3.5F);
			int q;
			if (o > 0 && p == m && (n & 1) == 0) {
				q = p + 1;
			} else {
				q = p;
			}

			this.placeLeavesRow(
				levelSimulatedRW, random, treeConfiguration, new BlockPos(blockPos.getX(), n, blockPos.getZ()), q, set, 0, foliageAttachment.doubleTrunk(), boundingBox
			);
			m = p;
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return this.crownHeight.sample(random);
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i + k >= 7 ? true : i * i + k * k > l * l;
	}
}
