package net.minecraft.world.level.gameevent;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityPositionSource implements PositionSource {
	public static final Codec<EntityPositionSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					UUIDUtil.CODEC.fieldOf("source_entity").forGetter(EntityPositionSource::getUuid),
					Codec.FLOAT.fieldOf("y_offset").orElse(0.0F).forGetter(entityPositionSource -> entityPositionSource.yOffset)
				)
				.apply(instance, (uUID, float_) -> new EntityPositionSource(Either.right(Either.left(uUID)), float_))
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, EntityPositionSource> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		EntityPositionSource::getId,
		ByteBufCodecs.FLOAT,
		entityPositionSource -> entityPositionSource.yOffset,
		(integer, float_) -> new EntityPositionSource(Either.right(Either.right(integer)), float_)
	);
	private Either<Entity, Either<UUID, Integer>> entityOrUuidOrId;
	private final float yOffset;

	public EntityPositionSource(Entity entity, float f) {
		this(Either.left(entity), f);
	}

	private EntityPositionSource(Either<Entity, Either<UUID, Integer>> either, float f) {
		this.entityOrUuidOrId = either;
		this.yOffset = f;
	}

	@Override
	public Optional<Vec3> getPosition(Level level) {
		if (this.entityOrUuidOrId.left().isEmpty()) {
			this.resolveEntity(level);
		}

		return this.entityOrUuidOrId.left().map(entity -> entity.position().add(0.0, (double)this.yOffset, 0.0));
	}

	private void resolveEntity(Level level) {
		this.entityOrUuidOrId
			.<Optional>map(
				Optional::of,
				either -> Optional.ofNullable((Entity)either.map(uUID -> level instanceof ServerLevel serverLevel ? serverLevel.getEntity(uUID) : null, level::getEntity))
			)
			.ifPresent(entity -> this.entityOrUuidOrId = Either.left(entity));
	}

	private UUID getUuid() {
		return this.entityOrUuidOrId.map(Entity::getUUID, either -> either.map(Function.identity(), integer -> {
				throw new RuntimeException("Unable to get entityId from uuid");
			}));
	}

	private int getId() {
		return this.entityOrUuidOrId.<Integer>map(Entity::getId, either -> either.map(uUID -> {
				throw new IllegalStateException("Unable to get entityId from uuid");
			}, Function.identity()));
	}

	@Override
	public PositionSourceType<EntityPositionSource> getType() {
		return PositionSourceType.ENTITY;
	}

	public static class Type implements PositionSourceType<EntityPositionSource> {
		@Override
		public Codec<EntityPositionSource> codec() {
			return EntityPositionSource.CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, EntityPositionSource> streamCodec() {
			return EntityPositionSource.STREAM_CODEC;
		}
	}
}
