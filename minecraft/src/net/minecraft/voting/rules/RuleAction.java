package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum RuleAction implements StringRepresentable {
	APPROVE("approve"),
	REPEAL("repeal");

	public static final Codec<RuleAction> CODEC = StringRepresentable.fromEnum(RuleAction::values);
	private final String id;

	private RuleAction(String string2) {
		this.id = string2;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}
}
