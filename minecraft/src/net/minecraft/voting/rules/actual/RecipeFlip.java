package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum RecipeFlip implements StringRepresentable {
	NORMAL_ONLY("normal_only"),
	FLIPPED_ONLY("flipped_only"),
	BOTH("both");

	public static final Codec<RecipeFlip> CODEC = StringRepresentable.fromEnum(RecipeFlip::values);
	private final String id;
	private final Component displayName;

	private RecipeFlip(String string2) {
		this.id = string2;
		this.displayName = Component.translatable("rule.recipe_flip." + string2);
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}
}
