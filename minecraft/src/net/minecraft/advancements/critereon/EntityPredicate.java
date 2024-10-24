package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public record EntityPredicate(
	Optional<EntityTypePredicate> entityType,
	Optional<DistancePredicate> distanceToPlayer,
	Optional<MovementPredicate> movement,
	EntityPredicate.LocationWrapper location,
	Optional<MobEffectsPredicate> effects,
	Optional<NbtPredicate> nbt,
	Optional<EntityFlagsPredicate> flags,
	Optional<EntityEquipmentPredicate> equipment,
	Optional<EntitySubPredicate> subPredicate,
	Optional<Integer> periodicTick,
	Optional<EntityPredicate> vehicle,
	Optional<EntityPredicate> passenger,
	Optional<EntityPredicate> targetedEntity,
	Optional<String> team,
	Optional<SlotsPredicate> slots
) {
	public static final Codec<EntityPredicate> CODEC = Codec.recursive(
		"EntityPredicate",
		codec -> RecordCodecBuilder.create(
				instance -> instance.group(
							EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(EntityPredicate::entityType),
							DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(EntityPredicate::distanceToPlayer),
							MovementPredicate.CODEC.optionalFieldOf("movement").forGetter(EntityPredicate::movement),
							EntityPredicate.LocationWrapper.CODEC.forGetter(EntityPredicate::location),
							MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(EntityPredicate::effects),
							NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(EntityPredicate::nbt),
							EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(EntityPredicate::flags),
							EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntityPredicate::equipment),
							EntitySubPredicate.CODEC.optionalFieldOf("type_specific").forGetter(EntityPredicate::subPredicate),
							ExtraCodecs.POSITIVE_INT.optionalFieldOf("periodic_tick").forGetter(EntityPredicate::periodicTick),
							codec.optionalFieldOf("vehicle").forGetter(EntityPredicate::vehicle),
							codec.optionalFieldOf("passenger").forGetter(EntityPredicate::passenger),
							codec.optionalFieldOf("targeted_entity").forGetter(EntityPredicate::targetedEntity),
							Codec.STRING.optionalFieldOf("team").forGetter(EntityPredicate::team),
							SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntityPredicate::slots)
						)
						.apply(instance, EntityPredicate::new)
			)
	);
	public static final Codec<ContextAwarePredicate> ADVANCEMENT_CODEC = Codec.withAlternative(ContextAwarePredicate.CODEC, CODEC, EntityPredicate::wrap);

	public static ContextAwarePredicate wrap(EntityPredicate.Builder builder) {
		return wrap(builder.build());
	}

	public static Optional<ContextAwarePredicate> wrap(Optional<EntityPredicate> optional) {
		return optional.map(EntityPredicate::wrap);
	}

	public static List<ContextAwarePredicate> wrap(EntityPredicate.Builder... builders) {
		return Stream.of(builders).map(EntityPredicate::wrap).toList();
	}

	public static ContextAwarePredicate wrap(EntityPredicate entityPredicate) {
		LootItemCondition lootItemCondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, entityPredicate).build();
		return new ContextAwarePredicate(List.of(lootItemCondition));
	}

	public boolean matches(ServerPlayer serverPlayer, @Nullable Entity entity) {
		return this.matches(serverPlayer.serverLevel(), serverPlayer.position(), entity);
	}

	public boolean matches(ServerLevel serverLevel, @Nullable Vec3 vec3, @Nullable Entity entity) {
		if (entity == null) {
			return false;
		} else if (this.entityType.isPresent() && !((EntityTypePredicate)this.entityType.get()).matches(entity.getType())) {
			return false;
		} else {
			if (vec3 == null) {
				if (this.distanceToPlayer.isPresent()) {
					return false;
				}
			} else if (this.distanceToPlayer.isPresent()
				&& !((DistancePredicate)this.distanceToPlayer.get()).matches(vec3.x, vec3.y, vec3.z, entity.getX(), entity.getY(), entity.getZ())) {
				return false;
			}

			if (this.movement.isPresent()) {
				Vec3 vec32 = entity.getKnownMovement();
				Vec3 vec33 = vec32.scale(20.0);
				if (!((MovementPredicate)this.movement.get()).matches(vec33.x, vec33.y, vec33.z, (double)entity.fallDistance)) {
					return false;
				}
			}

			if (this.location.located.isPresent() && !((LocationPredicate)this.location.located.get()).matches(serverLevel, entity.getX(), entity.getY(), entity.getZ())
				)
			 {
				return false;
			} else {
				if (this.location.steppingOn.isPresent()) {
					Vec3 vec32 = Vec3.atCenterOf(entity.getOnPos());
					if (!((LocationPredicate)this.location.steppingOn.get()).matches(serverLevel, vec32.x(), vec32.y(), vec32.z())) {
						return false;
					}
				}

				if (this.location.affectsMovement.isPresent()) {
					Vec3 vec32 = Vec3.atCenterOf(entity.getBlockPosBelowThatAffectsMyMovement());
					if (!((LocationPredicate)this.location.affectsMovement.get()).matches(serverLevel, vec32.x(), vec32.y(), vec32.z())) {
						return false;
					}
				}

				if (this.effects.isPresent() && !((MobEffectsPredicate)this.effects.get()).matches(entity)) {
					return false;
				} else if (this.flags.isPresent() && !((EntityFlagsPredicate)this.flags.get()).matches(entity)) {
					return false;
				} else if (this.equipment.isPresent() && !((EntityEquipmentPredicate)this.equipment.get()).matches(entity)) {
					return false;
				} else if (this.subPredicate.isPresent() && !((EntitySubPredicate)this.subPredicate.get()).matches(entity, serverLevel, vec3)) {
					return false;
				} else if (this.vehicle.isPresent() && !((EntityPredicate)this.vehicle.get()).matches(serverLevel, vec3, entity.getVehicle())) {
					return false;
				} else if (this.passenger.isPresent()
					&& entity.getPassengers().stream().noneMatch(entityx -> ((EntityPredicate)this.passenger.get()).matches(serverLevel, vec3, entityx))) {
					return false;
				} else if (this.targetedEntity.isPresent()
					&& !((EntityPredicate)this.targetedEntity.get()).matches(serverLevel, vec3, entity instanceof Mob ? ((Mob)entity).getTarget() : null)) {
					return false;
				} else if (this.periodicTick.isPresent() && entity.tickCount % (Integer)this.periodicTick.get() != 0) {
					return false;
				} else {
					if (this.team.isPresent()) {
						Team team = entity.getTeam();
						if (team == null || !((String)this.team.get()).equals(team.getName())) {
							return false;
						}
					}

					return this.slots.isPresent() && !((SlotsPredicate)this.slots.get()).matches(entity)
						? false
						: !this.nbt.isPresent() || ((NbtPredicate)this.nbt.get()).matches(entity);
				}
			}
		}
	}

	public static LootContext createContext(ServerPlayer serverPlayer, Entity entity) {
		LootParams lootParams = new LootParams.Builder(serverPlayer.serverLevel())
			.withParameter(LootContextParams.THIS_ENTITY, entity)
			.withParameter(LootContextParams.ORIGIN, serverPlayer.position())
			.create(LootContextParamSets.ADVANCEMENT_ENTITY);
		return new LootContext.Builder(lootParams).create(Optional.empty());
	}

	public static class Builder {
		private Optional<EntityTypePredicate> entityType = Optional.empty();
		private Optional<DistancePredicate> distanceToPlayer = Optional.empty();
		private Optional<MovementPredicate> movement = Optional.empty();
		private Optional<LocationPredicate> located = Optional.empty();
		private Optional<LocationPredicate> steppingOnLocation = Optional.empty();
		private Optional<LocationPredicate> movementAffectedBy = Optional.empty();
		private Optional<MobEffectsPredicate> effects = Optional.empty();
		private Optional<NbtPredicate> nbt = Optional.empty();
		private Optional<EntityFlagsPredicate> flags = Optional.empty();
		private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
		private Optional<EntitySubPredicate> subPredicate = Optional.empty();
		private Optional<Integer> periodicTick = Optional.empty();
		private Optional<EntityPredicate> vehicle = Optional.empty();
		private Optional<EntityPredicate> passenger = Optional.empty();
		private Optional<EntityPredicate> targetedEntity = Optional.empty();
		private Optional<String> team = Optional.empty();
		private Optional<SlotsPredicate> slots = Optional.empty();

		public static EntityPredicate.Builder entity() {
			return new EntityPredicate.Builder();
		}

		public EntityPredicate.Builder of(HolderGetter<EntityType<?>> holderGetter, EntityType<?> entityType) {
			this.entityType = Optional.of(EntityTypePredicate.of(holderGetter, entityType));
			return this;
		}

		public EntityPredicate.Builder of(HolderGetter<EntityType<?>> holderGetter, TagKey<EntityType<?>> tagKey) {
			this.entityType = Optional.of(EntityTypePredicate.of(holderGetter, tagKey));
			return this;
		}

		public EntityPredicate.Builder entityType(EntityTypePredicate entityTypePredicate) {
			this.entityType = Optional.of(entityTypePredicate);
			return this;
		}

		public EntityPredicate.Builder distance(DistancePredicate distancePredicate) {
			this.distanceToPlayer = Optional.of(distancePredicate);
			return this;
		}

		public EntityPredicate.Builder moving(MovementPredicate movementPredicate) {
			this.movement = Optional.of(movementPredicate);
			return this;
		}

		public EntityPredicate.Builder located(LocationPredicate.Builder builder) {
			this.located = Optional.of(builder.build());
			return this;
		}

		public EntityPredicate.Builder steppingOn(LocationPredicate.Builder builder) {
			this.steppingOnLocation = Optional.of(builder.build());
			return this;
		}

		public EntityPredicate.Builder movementAffectedBy(LocationPredicate.Builder builder) {
			this.movementAffectedBy = Optional.of(builder.build());
			return this;
		}

		public EntityPredicate.Builder effects(MobEffectsPredicate.Builder builder) {
			this.effects = builder.build();
			return this;
		}

		public EntityPredicate.Builder nbt(NbtPredicate nbtPredicate) {
			this.nbt = Optional.of(nbtPredicate);
			return this;
		}

		public EntityPredicate.Builder flags(EntityFlagsPredicate.Builder builder) {
			this.flags = Optional.of(builder.build());
			return this;
		}

		public EntityPredicate.Builder equipment(EntityEquipmentPredicate.Builder builder) {
			this.equipment = Optional.of(builder.build());
			return this;
		}

		public EntityPredicate.Builder equipment(EntityEquipmentPredicate entityEquipmentPredicate) {
			this.equipment = Optional.of(entityEquipmentPredicate);
			return this;
		}

		public EntityPredicate.Builder subPredicate(EntitySubPredicate entitySubPredicate) {
			this.subPredicate = Optional.of(entitySubPredicate);
			return this;
		}

		public EntityPredicate.Builder periodicTick(int i) {
			this.periodicTick = Optional.of(i);
			return this;
		}

		public EntityPredicate.Builder vehicle(EntityPredicate.Builder builder) {
			this.vehicle = Optional.of(builder.build());
			return this;
		}

		public EntityPredicate.Builder passenger(EntityPredicate.Builder builder) {
			this.passenger = Optional.of(builder.build());
			return this;
		}

		public EntityPredicate.Builder targetedEntity(EntityPredicate.Builder builder) {
			this.targetedEntity = Optional.of(builder.build());
			return this;
		}

		public EntityPredicate.Builder team(String string) {
			this.team = Optional.of(string);
			return this;
		}

		public EntityPredicate.Builder slots(SlotsPredicate slotsPredicate) {
			this.slots = Optional.of(slotsPredicate);
			return this;
		}

		public EntityPredicate build() {
			return new EntityPredicate(
				this.entityType,
				this.distanceToPlayer,
				this.movement,
				new EntityPredicate.LocationWrapper(this.located, this.steppingOnLocation, this.movementAffectedBy),
				this.effects,
				this.nbt,
				this.flags,
				this.equipment,
				this.subPredicate,
				this.periodicTick,
				this.vehicle,
				this.passenger,
				this.targetedEntity,
				this.team,
				this.slots
			);
		}
	}

	public static record LocationWrapper(Optional<LocationPredicate> located, Optional<LocationPredicate> steppingOn, Optional<LocationPredicate> affectsMovement) {
		public static final MapCodec<EntityPredicate.LocationWrapper> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						LocationPredicate.CODEC.optionalFieldOf("location").forGetter(EntityPredicate.LocationWrapper::located),
						LocationPredicate.CODEC.optionalFieldOf("stepping_on").forGetter(EntityPredicate.LocationWrapper::steppingOn),
						LocationPredicate.CODEC.optionalFieldOf("movement_affected_by").forGetter(EntityPredicate.LocationWrapper::affectsMovement)
					)
					.apply(instance, EntityPredicate.LocationWrapper::new)
		);
	}
}
