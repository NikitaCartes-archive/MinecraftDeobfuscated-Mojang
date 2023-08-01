package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.StringRepresentable;

public enum HumanoidArm implements OptionEnum, StringRepresentable {
	LEFT(0, "left", "options.mainHand.left"),
	RIGHT(1, "right", "options.mainHand.right");

	public static final Codec<HumanoidArm> CODEC = StringRepresentable.fromEnum(HumanoidArm::values);
	private final int id;
	private final String name;
	private final String translationKey;

	private HumanoidArm(int j, String string2, String string3) {
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
