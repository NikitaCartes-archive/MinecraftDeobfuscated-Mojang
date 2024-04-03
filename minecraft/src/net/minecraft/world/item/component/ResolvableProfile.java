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

public record ResolvableProfile(Optional<String> name, Optional<UUID> id, PropertyMap properties, GameProfile gameProfile) {
	private static final Codec<ResolvableProfile> FULL_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.PLAYER_NAME.optionalFieldOf("name").forGetter(ResolvableProfile::name),
					UUIDUtil.CODEC.optionalFieldOf("id").forGetter(ResolvableProfile::id),
					ExtraCodecs.PROPERTY_MAP.optionalFieldOf("properties", new PropertyMap()).forGetter(ResolvableProfile::properties)
				)
				.apply(instance, ResolvableProfile::new)
	);
	public static final Codec<ResolvableProfile> CODEC = Codec.withAlternative(
		FULL_CODEC, ExtraCodecs.PLAYER_NAME, string -> new ResolvableProfile(Optional.of(string), Optional.empty(), new PropertyMap())
	);
	public static final StreamCodec<ByteBuf, ResolvableProfile> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.stringUtf8(16).apply(ByteBufCodecs::optional),
		ResolvableProfile::name,
		UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional),
		ResolvableProfile::id,
		ByteBufCodecs.GAME_PROFILE_PROPERTIES,
		ResolvableProfile::properties,
		ResolvableProfile::new
	);

	public ResolvableProfile(Optional<String> optional, Optional<UUID> optional2, PropertyMap propertyMap) {
		this(optional, optional2, propertyMap, createProfile(optional, optional2, propertyMap));
	}

	public ResolvableProfile(GameProfile gameProfile) {
		this(Optional.of(gameProfile.getName()), Optional.of(gameProfile.getId()), gameProfile.getProperties(), gameProfile);
	}

	public CompletableFuture<ResolvableProfile> resolve() {
		if (this.isResolved()) {
			return CompletableFuture.completedFuture(this);
		} else {
			return this.id.isPresent() ? SkullBlockEntity.fetchGameProfile((UUID)this.id.get()).thenApply(optional -> {
				GameProfile gameProfile = (GameProfile)optional.orElseGet(() -> new GameProfile((UUID)this.id.get(), (String)this.name.orElse("")));
				return new ResolvableProfile(gameProfile);
			}) : SkullBlockEntity.fetchGameProfile((String)this.name.orElseThrow()).thenApply(optional -> {
				GameProfile gameProfile = (GameProfile)optional.orElseGet(() -> new GameProfile(Util.NIL_UUID, (String)this.name.get()));
				return new ResolvableProfile(gameProfile);
			});
		}
	}

	private static GameProfile createProfile(Optional<String> optional, Optional<UUID> optional2, PropertyMap propertyMap) {
		GameProfile gameProfile = new GameProfile((UUID)optional2.orElse(Util.NIL_UUID), (String)optional.orElse(""));
		gameProfile.getProperties().putAll(propertyMap);
		return gameProfile;
	}

	public boolean isResolved() {
		return !this.properties.isEmpty() ? true : this.id.isPresent() == this.name.isPresent();
	}
}
