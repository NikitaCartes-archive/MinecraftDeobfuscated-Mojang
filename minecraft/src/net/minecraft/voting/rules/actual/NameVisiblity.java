package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum NameVisiblity implements StringRepresentable {
	NONE("none"),
	NORMAL("normal"),
	SEE_THROUGH("see_through");

	public static final Codec<NameVisiblity> CODEC = StringRepresentable.fromEnum(NameVisiblity::values);
	private final String name;
	private final Component displayName;

	private NameVisiblity(String string2) {
		this.name = string2;
		this.displayName = Component.translatable("rule.name_visibility." + string2);
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public Component getDisplayName() {
		return this.displayName;
	}
}
