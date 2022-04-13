package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class TreeDecorator {
	public static final Codec<TreeDecorator> CODEC = Registry.TREE_DECORATOR_TYPES.byNameCodec().dispatch(TreeDecorator::type, TreeDecoratorType::codec);

	protected abstract TreeDecoratorType<?> type();

	public abstract void place(TreeDecorator.Context context);

	public static final class Context {
		private final LevelSimulatedReader level;
		private final BiConsumer<BlockPos, BlockState> decorationSetter;
		private final RandomSource random;
		private final ObjectArrayList<BlockPos> logs;
		private final ObjectArrayList<BlockPos> leaves;
		private final ObjectArrayList<BlockPos> roots;

		public Context(
			LevelSimulatedReader levelSimulatedReader,
			BiConsumer<BlockPos, BlockState> biConsumer,
			RandomSource randomSource,
			Set<BlockPos> set,
			Set<BlockPos> set2,
			Set<BlockPos> set3
		) {
			this.level = levelSimulatedReader;
			this.decorationSetter = biConsumer;
			this.random = randomSource;
			this.roots = new ObjectArrayList<>(set3);
			this.logs = new ObjectArrayList<>(set);
			this.leaves = new ObjectArrayList<>(set2);
			this.logs.sort(Comparator.comparingInt(Vec3i::getY));
			this.leaves.sort(Comparator.comparingInt(Vec3i::getY));
			this.roots.sort(Comparator.comparingInt(Vec3i::getY));
		}

		public void placeVine(BlockPos blockPos, BooleanProperty booleanProperty) {
			this.setBlock(blockPos, Blocks.VINE.defaultBlockState().setValue(booleanProperty, Boolean.valueOf(true)));
		}

		public void setBlock(BlockPos blockPos, BlockState blockState) {
			this.decorationSetter.accept(blockPos, blockState);
		}

		public boolean isAir(BlockPos blockPos) {
			return this.level.isStateAtPosition(blockPos, BlockBehaviour.BlockStateBase::isAir);
		}

		public LevelSimulatedReader level() {
			return this.level;
		}

		public RandomSource random() {
			return this.random;
		}

		public ObjectArrayList<BlockPos> logs() {
			return this.logs;
		}

		public ObjectArrayList<BlockPos> leaves() {
			return this.leaves;
		}

		public ObjectArrayList<BlockPos> roots() {
			return this.roots;
		}
	}
}
