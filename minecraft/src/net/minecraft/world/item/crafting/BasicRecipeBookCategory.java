package net.minecraft.world.item.crafting;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum BasicRecipeBookCategory implements RecipeBookCategory {
	CRAFTING_BUILDING_BLOCKS(0),
	CRAFTING_REDSTONE(1),
	CRAFTING_EQUIPMENT(2),
	CRAFTING_MISC(3),
	FURNACE_FOOD(4),
	FURNACE_BLOCKS(5),
	FURNACE_MISC(6),
	BLAST_FURNACE_BLOCKS(7),
	BLAST_FURNACE_MISC(8),
	SMOKER_FOOD(9),
	STONECUTTER(10),
	SMITHING(11),
	CAMPFIRE(12);

	public static final IntFunction<BasicRecipeBookCategory> BY_ID = ByIdMap.continuous(
		basicRecipeBookCategory -> basicRecipeBookCategory.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO
	);
	public static final StreamCodec<ByteBuf, BasicRecipeBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(
		BY_ID, basicRecipeBookCategory -> basicRecipeBookCategory.id
	);
	private final int id;

	private BasicRecipeBookCategory(final int j) {
		this.id = j;
	}
}
