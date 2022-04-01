package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityPositionSource implements PositionSource {
	public static final Codec<EntityPositionSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.INT.fieldOf("source_entity_id").forGetter(entityPositionSource -> entityPositionSource.sourceEntityId))
				.apply(instance, EntityPositionSource::new)
	);
	final int sourceEntityId;
	private Optional<Entity> sourceEntity = Optional.empty();

	public EntityPositionSource(int i) {
		this.sourceEntityId = i;
	}

	@Override
	public Optional<BlockPos> getPosition(Level level) {
		if (!this.sourceEntity.isPresent()) {
			this.sourceEntity = Optional.ofNullable(level.getEntity(this.sourceEntityId));
		}

		return this.sourceEntity.map(Entity::blockPosition);
	}

	@Override
	public PositionSourceType<?> getType() {
		return PositionSourceType.ENTITY;
	}

	public static class Type implements PositionSourceType<EntityPositionSource> {
		public EntityPositionSource read(FriendlyByteBuf friendlyByteBuf) {
			return new EntityPositionSource(friendlyByteBuf.readVarInt());
		}

		public void write(FriendlyByteBuf friendlyByteBuf, EntityPositionSource entityPositionSource) {
			friendlyByteBuf.writeVarInt(entityPositionSource.sourceEntityId);
		}

		@Override
		public Codec<EntityPositionSource> codec() {
			return EntityPositionSource.CODEC;
		}
	}
}
