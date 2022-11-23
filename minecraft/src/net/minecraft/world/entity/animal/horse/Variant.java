package net.minecraft.world.entity.animal.horse;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum Variant implements StringRepresentable {
	WHITE(0, "white"),
	CREAMY(1, "creamy"),
	CHESTNUT(2, "chestnut"),
	BROWN(3, "brown"),
	BLACK(4, "black"),
	GRAY(5, "gray"),
	DARK_BROWN(6, "dark_brown");

	public static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);
	private static final IntFunction<Variant> BY_ID = ByIdMap.continuous(Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
	private final int id;
	private final String name;

	private Variant(int j, String string2) {
		this.id = j;
		this.name = string2;
	}

	public int getId() {
		return this.id;
	}

	public static Variant byId(int i) {
		return (Variant)BY_ID.apply(i);
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
