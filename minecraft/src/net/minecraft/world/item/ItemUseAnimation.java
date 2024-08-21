package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum ItemUseAnimation implements StringRepresentable {
	NONE(0, "none"),
	EAT(1, "eat"),
	DRINK(2, "drink"),
	BLOCK(3, "block"),
	BOW(4, "bow"),
	SPEAR(5, "spear"),
	CROSSBOW(6, "crossbow"),
	SPYGLASS(7, "spyglass"),
	TOOT_HORN(8, "toot_horn"),
	BRUSH(9, "brush");

	private static final IntFunction<ItemUseAnimation> BY_ID = ByIdMap.continuous(ItemUseAnimation::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final Codec<ItemUseAnimation> CODEC = StringRepresentable.fromEnum(ItemUseAnimation::values);
	public static final StreamCodec<ByteBuf, ItemUseAnimation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ItemUseAnimation::getId);
	private final int id;
	private final String name;

	private ItemUseAnimation(final int j, final String string2) {
		this.id = j;
		this.name = string2;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
