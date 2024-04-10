package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum CraftingBookCategory implements StringRepresentable {
	BUILDING("building", 0),
	REDSTONE("redstone", 1),
	EQUIPMENT("equipment", 2),
	MISC("misc", 3);

	public static final Codec<CraftingBookCategory> CODEC = StringRepresentable.fromEnum(CraftingBookCategory::values);
	public static final IntFunction<CraftingBookCategory> BY_ID = ByIdMap.continuous(CraftingBookCategory::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final StreamCodec<ByteBuf, CraftingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CraftingBookCategory::id);
	private final String name;
	private final int id;

	private CraftingBookCategory(final String string2, final int j) {
		this.name = string2;
		this.id = j;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	private int id() {
		return this.id;
	}
}
