package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum ItemEntityDespawn implements StringRepresentable {
	DESPAWN_ALL("despawn_all"),
	KEEP_PLAYER_DROPS("keep_player_drops"),
	DESPAWN_NONE("despawn_none");

	private final String id;
	private final Component displayName;
	public static final Codec<ItemEntityDespawn> CODEC = StringRepresentable.fromEnum(ItemEntityDespawn::values);

	private ItemEntityDespawn(String string2) {
		this.id = string2;
		this.displayName = Component.translatable("rule.item_despawn." + string2);
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public Component getDisplayName() {
		return this.displayName;
	}
}
