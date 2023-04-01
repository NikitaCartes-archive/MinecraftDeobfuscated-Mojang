package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum AutoJumpAlternatives implements StringRepresentable {
	OFF("off", Component.translatable("rule.autojump.off")),
	YES("yes", Component.translatable("rule.autoJump.yes")),
	ON("on", Component.translatable("rule.autoJump.on")),
	TRUE("true", Component.translatable("rule.autoJump.true")),
	OF_COURSE("of_course", Component.translatable("rule.autoJump.of_course")),
	ALSO_DEFAULT_VANILLA_TO_TRUE("also_default_vanilla_to_true", Component.translatable("rule.autoJump.also_default_vanilla_to_true"));

	public static final Codec<AutoJumpAlternatives> CODEC = StringRepresentable.fromEnum(AutoJumpAlternatives::values);
	private final String id;
	private final Component name;

	private AutoJumpAlternatives(String string2, Component component) {
		this.id = string2;
		this.name = component;
	}

	public Component getName() {
		return this.name;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}
}
