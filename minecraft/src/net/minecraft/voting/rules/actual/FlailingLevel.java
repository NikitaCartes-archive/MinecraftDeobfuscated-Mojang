package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum FlailingLevel implements StringRepresentable {
	NONE("none", Component.translatable("rule.flailing.none")),
	NORMAL("normal", Component.translatable("rule.flailing.normal")),
	MILD("mild", Component.translatable("rule.flailing.mild")),
	WILD("wild", Component.translatable("rule.flailing.wild").withStyle(ChatFormatting.BOLD)),
	EXTREME("extreme", Component.translatable("rule.flailing.extreme").withStyle(ChatFormatting.BOLD, ChatFormatting.RED)),
	WINDMILL("windmill", Component.translatable("rule.flailing.windmill").withStyle(ChatFormatting.BOLD, ChatFormatting.RED, ChatFormatting.UNDERLINE));

	private final String id;
	private final Component name;
	public static final Codec<FlailingLevel> CODEC = StringRepresentable.fromEnum(FlailingLevel::values);

	private FlailingLevel(String string2, Component component) {
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
