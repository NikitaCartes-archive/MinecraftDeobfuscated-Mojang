package net.minecraft.world.item.component;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public record ResolvableProfile(String name, Optional<UUID> id, PropertyMap properties, GameProfile gameProfile) {
	public static final Codec<ResolvableProfile> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.PLAYER_NAME.fieldOf("name").forGetter(ResolvableProfile::name),
					ExtraCodecs.strictOptionalField(UUIDUtil.CODEC, "id").forGetter(ResolvableProfile::id),
					ExtraCodecs.strictOptionalField(ExtraCodecs.PROPERTY_MAP, "properties", new PropertyMap()).forGetter(ResolvableProfile::properties)
				)
				.apply(instance, ResolvableProfile::new)
	);
	public static final StreamCodec<ByteBuf, ResolvableProfile> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.stringUtf8(16),
		ResolvableProfile::name,
		UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional),
		ResolvableProfile::id,
		ByteBufCodecs.GAME_PROFILE_PROPERTIES,
		ResolvableProfile::properties,
		ResolvableProfile::new
	);

	public ResolvableProfile(String string, Optional<UUID> optional, PropertyMap propertyMap) {
		this(string, optional, propertyMap, createProfile(string, optional, propertyMap));
	}

	public ResolvableProfile(GameProfile gameProfile) {
		this(gameProfile.getName(), Optional.ofNullable(gameProfile.getId()), gameProfile.getProperties(), gameProfile);
	}

	public CompletableFuture<ResolvableProfile> resolve() {
		return this.isResolved() ? CompletableFuture.completedFuture(this) : SkullBlockEntity.fetchGameProfile(this.name).thenApply(optional -> {
			GameProfile gameProfile = (GameProfile)optional.orElseGet(() -> new GameProfile(Util.NIL_UUID, this.name));
			return new ResolvableProfile(gameProfile);
		});
	}

	private static GameProfile createProfile(String string, Optional<UUID> optional, PropertyMap propertyMap) {
		GameProfile gameProfile = new GameProfile((UUID)optional.orElse(Util.NIL_UUID), string);
		gameProfile.getProperties().putAll(propertyMap);
		return gameProfile;
	}

	public boolean isResolved() {
		return this.id.isPresent() || !this.properties.isEmpty();
	}
}
