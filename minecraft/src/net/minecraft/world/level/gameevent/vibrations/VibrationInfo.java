package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record VibrationInfo(
	Holder<GameEvent> gameEvent, float distance, Vec3 pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid, @Nullable Entity entity
) {
	public static final Codec<VibrationInfo> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BuiltInRegistries.GAME_EVENT.holderByNameCodec().fieldOf("game_event").forGetter(VibrationInfo::gameEvent),
					Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("distance").forGetter(VibrationInfo::distance),
					Vec3.CODEC.fieldOf("pos").forGetter(VibrationInfo::pos),
					UUIDUtil.CODEC.lenientOptionalFieldOf("source").forGetter(vibrationInfo -> Optional.ofNullable(vibrationInfo.uuid())),
					UUIDUtil.CODEC.lenientOptionalFieldOf("projectile_owner").forGetter(vibrationInfo -> Optional.ofNullable(vibrationInfo.projectileOwnerUuid()))
				)
				.apply(
					instance,
					(holder, float_, vec3, optional, optional2) -> new VibrationInfo(holder, float_, vec3, (UUID)optional.orElse(null), (UUID)optional2.orElse(null))
				)
	);

	public VibrationInfo(Holder<GameEvent> holder, float f, Vec3 vec3, @Nullable UUID uUID, @Nullable UUID uUID2) {
		this(holder, f, vec3, uUID, uUID2, null);
	}

	public VibrationInfo(Holder<GameEvent> holder, float f, Vec3 vec3, @Nullable Entity entity) {
		this(holder, f, vec3, entity == null ? null : entity.getUUID(), getProjectileOwner(entity), entity);
	}

	@Nullable
	private static UUID getProjectileOwner(@Nullable Entity entity) {
		if (entity instanceof Projectile projectile && projectile.getOwner() != null) {
			return projectile.getOwner().getUUID();
		}

		return null;
	}

	public Optional<Entity> getEntity(ServerLevel serverLevel) {
		return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map(serverLevel::getEntity));
	}

	public Optional<Entity> getProjectileOwner(ServerLevel serverLevel) {
		return this.getEntity(serverLevel)
			.filter(entity -> entity instanceof Projectile)
			.map(entity -> (Projectile)entity)
			.map(Projectile::getOwner)
			.or(() -> Optional.ofNullable(this.projectileOwnerUuid).map(serverLevel::getEntity));
	}
}
