package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
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
	Optional<LocationPredicate> location,
	Optional<LocationPredicate> steppingOnLocation,
	Optional<MobEffectsPredicate> effects,
	Optional<NbtPredicate> nbt,
	Optional<EntityFlagsPredicate> flags,
	Optional<EntityEquipmentPredicate> equipment,
	Optional<EntitySubPredicate> subPredicate,
	Optional<EntityPredicate> vehicle,
	Optional<EntityPredicate> passenger,
	Optional<EntityPredicate> targetedEntity,
	Optional<String> team
) {
	public static final Codec<EntityPredicate> CODEC = ExtraCodecs.recursive(
		codec -> RecordCodecBuilder.create(
				instance -> instance.group(
							ExtraCodecs.strictOptionalField(EntityTypePredicate.CODEC, "type").forGetter(EntityPredicate::entityType),
							ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(EntityPredicate::distanceToPlayer),
							ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "location").forGetter(EntityPredicate::location),
							ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "stepping_on").forGetter(EntityPredicate::steppingOnLocation),
							ExtraCodecs.strictOptionalField(MobEffectsPredicate.CODEC, "effects").forGetter(EntityPredicate::effects),
							ExtraCodecs.strictOptionalField(NbtPredicate.CODEC, "nbt").forGetter(EntityPredicate::nbt),
							ExtraCodecs.strictOptionalField(EntityFlagsPredicate.CODEC, "flags").forGetter(EntityPredicate::flags),
							ExtraCodecs.strictOptionalField(EntityEquipmentPredicate.CODEC, "equipment").forGetter(EntityPredicate::equipment),
							ExtraCodecs.strictOptionalField(EntitySubPredicate.CODEC, "type_specific").forGetter(EntityPredicate::subPredicate),
							ExtraCodecs.strictOptionalField(codec, "vehicle").forGetter(EntityPredicate::vehicle),
							ExtraCodecs.strictOptionalField(codec, "passenger").forGetter(EntityPredicate::passenger),
							ExtraCodecs.strictOptionalField(codec, "targeted_entity").forGetter(EntityPredicate::targetedEntity),
							ExtraCodecs.strictOptionalField(Codec.STRING, "team").forGetter(EntityPredicate::team)
						)
						.apply(instance, EntityPredicate::new)
			)
	);

	public static Optional<EntityPredicate> of(
		Optional<EntityTypePredicate> optional,
		Optional<DistancePredicate> optional2,
		Optional<LocationPredicate> optional3,
		Optional<LocationPredicate> optional4,
		Optional<MobEffectsPredicate> optional5,
		Optional<NbtPredicate> optional6,
		Optional<EntityFlagsPredicate> optional7,
		Optional<EntityEquipmentPredicate> optional8,
		Optional<EntitySubPredicate> optional9,
		Optional<EntityPredicate> optional10,
		Optional<EntityPredicate> optional11,
		Optional<EntityPredicate> optional12,
		Optional<String> optional13
	) {
		return optional.isEmpty()
				&& optional2.isEmpty()
				&& optional3.isEmpty()
				&& optional4.isEmpty()
				&& optional5.isEmpty()
				&& optional6.isEmpty()
				&& optional7.isEmpty()
				&& optional8.isEmpty()
				&& optional9.isEmpty()
				&& optional10.isEmpty()
				&& optional11.isEmpty()
				&& optional12.isEmpty()
				&& optional13.isEmpty()
			? Optional.empty()
			: Optional.of(
				new EntityPredicate(
					optional, optional2, optional3, optional4, optional5, optional6, optional7, optional8, optional9, optional10, optional11, optional12, optional13
				)
			);
	}

	public static Optional<ContextAwarePredicate> fromJson(JsonObject jsonObject, String string, DeserializationContext deserializationContext) {
		JsonElement jsonElement = jsonObject.get(string);
		return fromElement(string, deserializationContext, jsonElement);
	}

	public static List<ContextAwarePredicate> fromJsonArray(JsonObject jsonObject, String string, DeserializationContext deserializationContext) {
		JsonElement jsonElement = jsonObject.get(string);
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonArray jsonArray = GsonHelper.convertToJsonArray(jsonElement, string);
			List<ContextAwarePredicate> list = new ArrayList(jsonArray.size());

			for (int i = 0; i < jsonArray.size(); i++) {
				fromElement(string + "[" + i + "]", deserializationContext, jsonArray.get(i)).ifPresent(list::add);
			}

			return List.copyOf(list);
		} else {
			return List.of();
		}
	}

	private static Optional<ContextAwarePredicate> fromElement(String string, DeserializationContext deserializationContext, @Nullable JsonElement jsonElement) {
		Optional<Optional<ContextAwarePredicate>> optional = ContextAwarePredicate.fromElement(
			string, deserializationContext, jsonElement, LootContextParamSets.ADVANCEMENT_ENTITY
		);
		if (optional.isPresent()) {
			return (Optional<ContextAwarePredicate>)optional.get();
		} else {
			Optional<EntityPredicate> optional2 = fromJson(jsonElement);
			return wrap(optional2);
		}
	}

	public static Optional<ContextAwarePredicate> wrap(EntityPredicate.Builder builder) {
		return wrap(builder.build());
	}

	public static Optional<ContextAwarePredicate> wrap(Optional<EntityPredicate> optional) {
		return optional.map(EntityPredicate::wrap);
	}

	public static List<ContextAwarePredicate> wrap(EntityPredicate.Builder... builders) {
		return Stream.of(builders).flatMap(builder -> wrap(builder).stream()).toList();
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

			if (this.location.isPresent() && !((LocationPredicate)this.location.get()).matches(serverLevel, entity.getX(), entity.getY(), entity.getZ())) {
				return false;
			} else {
				if (this.steppingOnLocation.isPresent()) {
					Vec3 vec32 = Vec3.atCenterOf(entity.getOnPos());
					if (!((LocationPredicate)this.steppingOnLocation.get()).matches(serverLevel, vec32.x(), vec32.y(), vec32.z())) {
						return false;
					}
				}

				if (this.effects.isPresent() && !((MobEffectsPredicate)this.effects.get()).matches(entity)) {
					return false;
				} else if (this.nbt.isPresent() && !((NbtPredicate)this.nbt.get()).matches(entity)) {
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
				} else {
					if (this.team.isPresent()) {
						Team team = entity.getTeam();
						if (team == null || !((String)this.team.get()).equals(team.getName())) {
							return false;
						}
					}

					return true;
				}
			}
		}
	}

	public static Optional<EntityPredicate> fromJson(@Nullable JsonElement jsonElement) {
		return jsonElement != null && !jsonElement.isJsonNull()
			? Optional.of(Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, jsonElement), JsonParseException::new))
			: Optional.empty();
	}

	public JsonElement serializeToJson() {
		return Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
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
		private Optional<LocationPredicate> location = Optional.empty();
		private Optional<LocationPredicate> steppingOnLocation = Optional.empty();
		private Optional<MobEffectsPredicate> effects = Optional.empty();
		private Optional<NbtPredicate> nbt = Optional.empty();
		private Optional<EntityFlagsPredicate> flags = Optional.empty();
		private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
		private Optional<EntitySubPredicate> subPredicate = Optional.empty();
		private Optional<EntityPredicate> vehicle = Optional.empty();
		private Optional<EntityPredicate> passenger = Optional.empty();
		private Optional<EntityPredicate> targetedEntity = Optional.empty();
		private Optional<String> team = Optional.empty();

		public static EntityPredicate.Builder entity() {
			return new EntityPredicate.Builder();
		}

		public EntityPredicate.Builder of(EntityType<?> entityType) {
			this.entityType = Optional.of(EntityTypePredicate.of(entityType));
			return this;
		}

		public EntityPredicate.Builder of(TagKey<EntityType<?>> tagKey) {
			this.entityType = Optional.of(EntityTypePredicate.of(tagKey));
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

		public EntityPredicate.Builder located(LocationPredicate.Builder builder) {
			this.location = builder.build();
			return this;
		}

		public EntityPredicate.Builder steppingOn(LocationPredicate.Builder builder) {
			this.steppingOnLocation = builder.build();
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
			this.flags = builder.build();
			return this;
		}

		public EntityPredicate.Builder equipment(EntityEquipmentPredicate.Builder builder) {
			this.equipment = builder.build();
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

		public EntityPredicate.Builder vehicle(EntityPredicate.Builder builder) {
			this.vehicle = builder.build();
			return this;
		}

		public EntityPredicate.Builder passenger(EntityPredicate.Builder builder) {
			this.passenger = builder.build();
			return this;
		}

		public EntityPredicate.Builder targetedEntity(EntityPredicate.Builder builder) {
			this.targetedEntity = builder.build();
			return this;
		}

		public EntityPredicate.Builder team(String string) {
			this.team = Optional.of(string);
			return this;
		}

		public Optional<EntityPredicate> build() {
			return EntityPredicate.of(
				this.entityType,
				this.distanceToPlayer,
				this.location,
				this.steppingOnLocation,
				this.effects,
				this.nbt,
				this.flags,
				this.equipment,
				this.subPredicate,
				this.vehicle,
				this.passenger,
				this.targetedEntity,
				this.team
			);
		}
	}
}
