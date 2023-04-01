package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum TieResolutionStrategy implements StringRepresentable {
	PICK_LOW("pick_low"),
	PICK_HIGH("pick_high"),
	PICK_RANDOM("pick_random"),
	PICK_ALL("pick_all"),
	PICK_NONE("pick_none"),
	FAIL("fail");

	public static final Codec<TieResolutionStrategy> CODEC = StringRepresentable.fromEnum(TieResolutionStrategy::values);
	private final String id;
	private final Component displayName;

	private TieResolutionStrategy(String string2) {
		this.id = string2;
		this.displayName = Component.translatable("rule.tie_strategy." + string2);
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public Component getDisplayName() {
		return this.displayName;
	}
}
