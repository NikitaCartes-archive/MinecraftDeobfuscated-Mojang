package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityPositionSource implements PositionSource {
	public static final Codec<EntityPositionSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("source_entity_id").forGetter(entityPositionSource -> entityPositionSource.sourceEntityId),
					Codec.FLOAT.fieldOf("y_offset").forGetter(entityPositionSource -> entityPositionSource.yOffset)
				)
				.apply(instance, EntityPositionSource::new)
	);
	final int sourceEntityId;
	private Optional<Entity> sourceEntity = Optional.empty();
	final float yOffset;

	public EntityPositionSource(Entity entity, float f) {
		this(entity.getId(), f);
	}

	EntityPositionSource(int i, float f) {
		this.sourceEntityId = i;
		this.yOffset = f;
	}

	@Override
	public Optional<Vec3> getPosition(Level level) {
		if (this.sourceEntity.isEmpty()) {
			this.sourceEntity = Optional.ofNullable(level.getEntity(this.sourceEntityId));
		}

		return this.sourceEntity.map(entity -> entity.position().add(0.0, (double)this.yOffset, 0.0));
	}

	@Override
	public PositionSourceType<?> getType() {
		return PositionSourceType.ENTITY;
	}

	public static class Type implements PositionSourceType<EntityPositionSource> {
		public EntityPositionSource read(FriendlyByteBuf friendlyByteBuf) {
			return new EntityPositionSource(friendlyByteBuf.readVarInt(), friendlyByteBuf.readFloat());
		}

		public void write(FriendlyByteBuf friendlyByteBuf, EntityPositionSource entityPositionSource) {
			friendlyByteBuf.writeVarInt(entityPositionSource.sourceEntityId);
			friendlyByteBuf.writeFloat(entityPositionSource.yOffset);
		}

		@Override
		public Codec<EntityPositionSource> codec() {
			return EntityPositionSource.CODEC;
		}
	}
}
