package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;

public interface WeatheringCopper extends ChangeOverTimeBlock<WeatheringCopper.WeatherState> {
	Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(
		() -> ImmutableBiMap.<Block, Block>builder()
				.put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER)
				.put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER)
				.put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER)
				.put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER)
				.put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER)
				.put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER)
				.put(Blocks.CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER)
				.put(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WEATHERED_CHISELED_COPPER)
				.put(Blocks.WEATHERED_CHISELED_COPPER, Blocks.OXIDIZED_CHISELED_COPPER)
				.put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB)
				.put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB)
				.put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB)
				.put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS)
				.put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS)
				.put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS)
				.put(Blocks.COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR)
				.put(Blocks.EXPOSED_COPPER_DOOR, Blocks.WEATHERED_COPPER_DOOR)
				.put(Blocks.WEATHERED_COPPER_DOOR, Blocks.OXIDIZED_COPPER_DOOR)
				.put(Blocks.COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR)
				.put(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WEATHERED_COPPER_TRAPDOOR)
				.put(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.OXIDIZED_COPPER_TRAPDOOR)
				.put(Blocks.COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE)
				.put(Blocks.EXPOSED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE)
				.put(Blocks.WEATHERED_COPPER_GRATE, Blocks.OXIDIZED_COPPER_GRATE)
				.put(Blocks.COPPER_BULB, Blocks.EXPOSED_COPPER_BULB)
				.put(Blocks.EXPOSED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB)
				.put(Blocks.WEATHERED_COPPER_BULB, Blocks.OXIDIZED_COPPER_BULB)
				.build()
	);
	Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> ((BiMap)NEXT_BY_BLOCK.get()).inverse());

	static Optional<Block> getPrevious(Block block) {
		return Optional.ofNullable((Block)((BiMap)PREVIOUS_BY_BLOCK.get()).get(block));
	}

	static Block getFirst(Block block) {
		Block block2 = block;

		for (Block block3 = (Block)((BiMap)PREVIOUS_BY_BLOCK.get()).get(block); block3 != null; block3 = (Block)((BiMap)PREVIOUS_BY_BLOCK.get()).get(block3)) {
			block2 = block3;
		}

		return block2;
	}

	static Optional<BlockState> getPrevious(BlockState blockState) {
		return getPrevious(blockState.getBlock()).map(block -> block.withPropertiesOf(blockState));
	}

	static Optional<Block> getNext(Block block) {
		return Optional.ofNullable((Block)((BiMap)NEXT_BY_BLOCK.get()).get(block));
	}

	static BlockState getFirst(BlockState blockState) {
		return getFirst(blockState.getBlock()).withPropertiesOf(blockState);
	}

	@Override
	default Optional<BlockState> getNext(BlockState blockState) {
		return getNext(blockState.getBlock()).map(block -> block.withPropertiesOf(blockState));
	}

	@Override
	default float getChanceModifier() {
		return this.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75F : 1.0F;
	}

	public static enum WeatherState implements StringRepresentable {
		UNAFFECTED("unaffected"),
		EXPOSED("exposed"),
		WEATHERED("weathered"),
		OXIDIZED("oxidized");

		public static final Codec<WeatheringCopper.WeatherState> CODEC = StringRepresentable.fromEnum(WeatheringCopper.WeatherState::values);
		private final String name;

		private WeatherState(String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
