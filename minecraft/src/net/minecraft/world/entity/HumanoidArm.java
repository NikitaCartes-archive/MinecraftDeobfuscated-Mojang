package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.StringRepresentable;

public enum HumanoidArm implements OptionEnum, StringRepresentable {
	LEFT(0, "left", "options.mainHand.left"),
	RIGHT(1, "right", "options.mainHand.right");

	public static final Codec<HumanoidArm> CODEC = StringRepresentable.fromEnum(HumanoidArm::values);
	public static final IntFunction<HumanoidArm> BY_ID = ByIdMap.continuous(HumanoidArm::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	private final int id;
	private final String name;
	private final String translationKey;

	private HumanoidArm(final int j, final String string2, final String string3) {
		this.id = j;
		this.name = string2;
		this.translationKey = string3;
	}

	public HumanoidArm getOpposite() {
		return this == LEFT ? RIGHT : LEFT;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getKey() {
		return this.translationKey;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
