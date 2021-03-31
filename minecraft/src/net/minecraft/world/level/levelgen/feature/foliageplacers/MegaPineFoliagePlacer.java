package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaPineFoliagePlacer extends FoliagePlacer {
	public static final Codec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance)
				.and(IntProvider.codec(0, 24).fieldOf("crown_height").forGetter(megaPineFoliagePlacer -> megaPineFoliagePlacer.crownHeight))
				.apply(instance, MegaPineFoliagePlacer::new)
	);
	private final IntProvider crownHeight;

	public MegaPineFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3) {
		super(intProvider, intProvider2);
		this.crownHeight = intProvider3;
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
	}

	@Override
	protected void createFoliage(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		Random random,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k,
		int l
	) {
		BlockPos blockPos = foliageAttachment.pos();
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
				levelSimulatedReader, biConsumer, random, treeConfiguration, new BlockPos(blockPos.getX(), n, blockPos.getZ()), q, 0, foliageAttachment.doubleTrunk()
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
