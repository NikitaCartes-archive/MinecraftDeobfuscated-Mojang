package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AttachedToLeavesDecorator extends TreeDecorator {
	public static final Codec<AttachedToLeavesDecorator> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.probability),
					Codec.BOOL.optionalFieldOf("use_logs", Boolean.valueOf(false)).forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.useLogs),
					Codec.intRange(0, 16).fieldOf("exclusion_radius_xz").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.exclusionRadiusXZ),
					Codec.intRange(0, 16).fieldOf("exclusion_radius_y").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.exclusionRadiusY),
					Codec.list(BlockStateProvider.CODEC).fieldOf("block_provider").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.blockProvider),
					Codec.intRange(1, 16).fieldOf("required_empty_blocks").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.requiredEmptyBlocks),
					ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.directions)
				)
				.apply(instance, AttachedToLeavesDecorator::new)
	);
	protected final float probability;
	protected final int exclusionRadiusXZ;
	protected final int exclusionRadiusY;
	protected final List<BlockStateProvider> blockProvider;
	protected final int requiredEmptyBlocks;
	protected final List<Direction> directions;
	protected boolean useLogs;

	public AttachedToLeavesDecorator(float f, boolean bl, int i, int j, List<BlockStateProvider> list, int k, List<Direction> list2) {
		this.probability = f;
		this.useLogs = bl;
		this.exclusionRadiusXZ = i;
		this.exclusionRadiusY = j;
		this.blockProvider = list;
		this.requiredEmptyBlocks = k;
		this.directions = list2;
	}

	@Override
	public void place(TreeDecorator.Context context) {
		Set<BlockPos> set = new HashSet();
		RandomSource randomSource = context.random();

		for (BlockPos blockPos : this.useLogs ? Util.shuffledCopy(context.logs(), randomSource) : Util.shuffledCopy(context.leaves(), randomSource)) {
			Direction direction = Util.getRandom(this.directions, randomSource);
			BlockPos blockPos2 = blockPos.relative(direction);
			if (!set.contains(blockPos2) && randomSource.nextFloat() < this.probability && this.hasRequiredEmptyBlocks(context, blockPos, direction)) {
				BlockPos blockPos3 = blockPos2.offset(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
				BlockPos blockPos4 = blockPos2.offset(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);

				for (BlockPos blockPos5 : BlockPos.betweenClosed(blockPos3, blockPos4)) {
					set.add(blockPos5.immutable());
				}

				for (BlockStateProvider blockStateProvider : this.blockProvider) {
					context.setBlock(blockPos2, blockStateProvider.getState(randomSource, blockPos2));
					blockPos2 = blockPos2.relative(direction);
				}
			}
		}
	}

	private boolean hasRequiredEmptyBlocks(TreeDecorator.Context context, BlockPos blockPos, Direction direction) {
		for (int i = 1; i <= this.requiredEmptyBlocks; i++) {
			BlockPos blockPos2 = blockPos.relative(direction, i);
			if (!context.isAir(blockPos2)) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.ATTACHED_TO_LEAVES;
	}
}
