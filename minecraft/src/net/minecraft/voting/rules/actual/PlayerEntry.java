package net.minecraft.voting.rules.actual;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public record PlayerEntry(UUID id, String name, Component displayName) {
	public static final Codec<PlayerEntry> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(PlayerEntry::id),
					Codec.STRING.fieldOf("name").forGetter(PlayerEntry::name),
					ExtraCodecs.COMPONENT.fieldOf("display_name").forGetter(PlayerEntry::displayName)
				)
				.apply(instance, PlayerEntry::new)
	);

	public static PlayerEntry from(ServerPlayer serverPlayer) {
		GameProfile gameProfile = serverPlayer.getGameProfile();
		return new PlayerEntry(gameProfile.getId(), gameProfile.getName(), serverPlayer.getDisplayName());
	}

	public GameProfile profile() {
		return new GameProfile(this.id, this.name);
	}
}
