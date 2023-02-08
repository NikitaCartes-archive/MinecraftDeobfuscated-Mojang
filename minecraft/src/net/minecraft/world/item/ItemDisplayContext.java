package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum ItemDisplayContext implements StringRepresentable {
	NONE(0, "none"),
	THIRD_PERSON_LEFT_HAND(1, "thirdperson_lefthand"),
	THIRD_PERSON_RIGHT_HAND(2, "thirdperson_righthand"),
	FIRST_PERSON_LEFT_HAND(3, "firstperson_lefthand"),
	FIRST_PERSON_RIGHT_HAND(4, "firstperson_righthand"),
	HEAD(5, "head"),
	GUI(6, "gui"),
	GROUND(7, "ground"),
	FIXED(8, "fixed");

	public static final Codec<ItemDisplayContext> CODEC = StringRepresentable.fromEnum(ItemDisplayContext::values);
	public static final IntFunction<ItemDisplayContext> BY_ID = ByIdMap.continuous(ItemDisplayContext::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	private final byte id;
	private final String name;

	private ItemDisplayContext(int j, String string2) {
		this.name = string2;
		this.id = (byte)j;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public byte getId() {
		return this.id;
	}

	public boolean firstPerson() {
		return this == FIRST_PERSON_LEFT_HAND || this == FIRST_PERSON_RIGHT_HAND;
	}
}
