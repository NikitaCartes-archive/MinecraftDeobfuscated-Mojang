package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityPredicate {
	public static final EntityPredicate ANY = new EntityPredicate(
		EntityTypePredicate.ANY,
		DistancePredicate.ANY,
		LocationPredicate.ANY,
		LocationPredicate.ANY,
		MobEffectsPredicate.ANY,
		NbtPredicate.ANY,
		EntityFlagsPredicate.ANY,
		EntityEquipmentPredicate.ANY,
		EntitySubPredicate.ANY,
		null
	);
	private final EntityTypePredicate entityType;
	private final DistancePredicate distanceToPlayer;
	private final LocationPredicate location;
	private final LocationPredicate steppingOnLocation;
	private final MobEffectsPredicate effects;
	private final NbtPredicate nbt;
	private final EntityFlagsPredicate flags;
	private final EntityEquipmentPredicate equipment;
	private final EntitySubPredicate subPredicate;
	private final EntityPredicate vehicle;
	private final EntityPredicate passenger;
	private final EntityPredicate targetedEntity;
	@Nullable
	private final String team;

	private EntityPredicate(
		EntityTypePredicate entityTypePredicate,
		DistancePredicate distancePredicate,
		LocationPredicate locationPredicate,
		LocationPredicate locationPredicate2,
		MobEffectsPredicate mobEffectsPredicate,
		NbtPredicate nbtPredicate,
		EntityFlagsPredicate entityFlagsPredicate,
		EntityEquipmentPredicate entityEquipmentPredicate,
		EntitySubPredicate entitySubPredicate,
		@Nullable String string
	) {
		this.entityType = entityTypePredicate;
		this.distanceToPlayer = distancePredicate;
		this.location = locationPredicate;
		this.steppingOnLocation = locationPredicate2;
		this.effects = mobEffectsPredicate;
		this.nbt = nbtPredicate;
		this.flags = entityFlagsPredicate;
		this.equipment = entityEquipmentPredicate;
		this.subPredicate = entitySubPredicate;
		this.passenger = this;
		this.vehicle = this;
		this.targetedEntity = this;
		this.team = string;
	}

	EntityPredicate(
		EntityTypePredicate entityTypePredicate,
		DistancePredicate distancePredicate,
		LocationPredicate locationPredicate,
		LocationPredicate locationPredicate2,
		MobEffectsPredicate mobEffectsPredicate,
		NbtPredicate nbtPredicate,
		EntityFlagsPredicate entityFlagsPredicate,
		EntityEquipmentPredicate entityEquipmentPredicate,
		EntitySubPredicate entitySubPredicate,
		EntityPredicate entityPredicate,
		EntityPredicate entityPredicate2,
		EntityPredicate entityPredicate3,
		@Nullable String string
	) {
		this.entityType = entityTypePredicate;
		this.distanceToPlayer = distancePredicate;
		this.location = locationPredicate;
		this.steppingOnLocation = locationPredicate2;
		this.effects = mobEffectsPredicate;
		this.nbt = nbtPredicate;
		this.flags = entityFlagsPredicate;
		this.equipment = entityEquipmentPredicate;
		this.subPredicate = entitySubPredicate;
		this.vehicle = entityPredicate;
		this.passenger = entityPredicate2;
		this.targetedEntity = entityPredicate3;
		this.team = string;
	}

	public boolean matches(ServerPlayer serverPlayer, @Nullable Entity entity) {
		return this.matches(serverPlayer.getLevel(), serverPlayer.position(), entity);
	}

	public boolean matches(ServerLevel serverLevel, @Nullable Vec3 vec3, @Nullable Entity entity) {
		if (this == ANY) {
			return true;
		} else if (entity == null) {
			return false;
		} else if (!this.entityType.matches(entity.getType())) {
			return false;
		} else {
			if (vec3 == null) {
				if (this.distanceToPlayer != DistancePredicate.ANY) {
					return false;
				}
			} else if (!this.distanceToPlayer.matches(vec3.x, vec3.y, vec3.z, entity.getX(), entity.getY(), entity.getZ())) {
				return false;
			}

			if (!this.location.matches(serverLevel, entity.getX(), entity.getY(), entity.getZ())) {
				return false;
			} else {
				if (this.steppingOnLocation != LocationPredicate.ANY) {
					Vec3 vec32 = Vec3.atCenterOf(entity.getOnPos());
					if (!this.steppingOnLocation.matches(serverLevel, vec32.x(), vec32.y(), vec32.z())) {
						return false;
					}
				}

				if (!this.effects.matches(entity)) {
					return false;
				} else if (!this.nbt.matches(entity)) {
					return false;
				} else if (!this.flags.matches(entity)) {
					return false;
				} else if (!this.equipment.matches(entity)) {
					return false;
				} else if (!this.subPredicate.matches(entity, serverLevel, vec3)) {
					return false;
				} else if (!this.vehicle.matches(serverLevel, vec3, entity.getVehicle())) {
					return false;
				} else if (this.passenger != ANY && entity.getPassengers().stream().noneMatch(entityx -> this.passenger.matches(serverLevel, vec3, entityx))) {
					return false;
				} else if (!this.targetedEntity.matches(serverLevel, vec3, entity instanceof Mob ? ((Mob)entity).getTarget() : null)) {
					return false;
				} else {
					if (this.team != null) {
						Team team = entity.getTeam();
						if (team == null || !this.team.equals(team.getName())) {
							return false;
						}
					}

					return true;
				}
			}
		}
	}

	public static EntityPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entity");
			EntityTypePredicate entityTypePredicate = EntityTypePredicate.fromJson(jsonObject.get("type"));
			DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
			LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
			LocationPredicate locationPredicate2 = LocationPredicate.fromJson(jsonObject.get("stepping_on"));
			MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
			NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
			EntityFlagsPredicate entityFlagsPredicate = EntityFlagsPredicate.fromJson(jsonObject.get("flags"));
			EntityEquipmentPredicate entityEquipmentPredicate = EntityEquipmentPredicate.fromJson(jsonObject.get("equipment"));
			EntitySubPredicate entitySubPredicate = EntitySubPredicate.fromJson(jsonObject.get("type_specific"));
			EntityPredicate entityPredicate = fromJson(jsonObject.get("vehicle"));
			EntityPredicate entityPredicate2 = fromJson(jsonObject.get("passenger"));
			EntityPredicate entityPredicate3 = fromJson(jsonObject.get("targeted_entity"));
			String string = GsonHelper.getAsString(jsonObject, "team", null);
			return new EntityPredicate.Builder()
				.entityType(entityTypePredicate)
				.distance(distancePredicate)
				.located(locationPredicate)
				.steppingOn(locationPredicate2)
				.effects(mobEffectsPredicate)
				.nbt(nbtPredicate)
				.flags(entityFlagsPredicate)
				.equipment(entityEquipmentPredicate)
				.subPredicate(entitySubPredicate)
				.team(string)
				.vehicle(entityPredicate)
				.passenger(entityPredicate2)
				.targetedEntity(entityPredicate3)
				.build();
		} else {
			return ANY;
		}
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("type", this.entityType.serializeToJson());
			jsonObject.add("distance", this.distanceToPlayer.serializeToJson());
			jsonObject.add("location", this.location.serializeToJson());
			jsonObject.add("stepping_on", this.steppingOnLocation.serializeToJson());
			jsonObject.add("effects", this.effects.serializeToJson());
			jsonObject.add("nbt", this.nbt.serializeToJson());
			jsonObject.add("flags", this.flags.serializeToJson());
			jsonObject.add("equipment", this.equipment.serializeToJson());
			jsonObject.add("type_specific", this.subPredicate.serialize());
			jsonObject.add("vehicle", this.vehicle.serializeToJson());
			jsonObject.add("passenger", this.passenger.serializeToJson());
			jsonObject.add("targeted_entity", this.targetedEntity.serializeToJson());
			jsonObject.addProperty("team", this.team);
			return jsonObject;
		}
	}

	public static LootContext createContext(ServerPlayer serverPlayer, Entity entity) {
		return new LootContext.Builder(serverPlayer.getLevel())
			.withParameter(LootContextParams.THIS_ENTITY, entity)
			.withParameter(LootContextParams.ORIGIN, serverPlayer.position())
			.withRandom(serverPlayer.getRandom())
			.create(LootContextParamSets.ADVANCEMENT_ENTITY);
	}

	public static class Builder {
		private EntityTypePredicate entityType = EntityTypePredicate.ANY;
		private DistancePredicate distanceToPlayer = DistancePredicate.ANY;
		private LocationPredicate location = LocationPredicate.ANY;
		private LocationPredicate steppingOnLocation = LocationPredicate.ANY;
		private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
		private NbtPredicate nbt = NbtPredicate.ANY;
		private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
		private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
		private EntitySubPredicate subPredicate = EntitySubPredicate.ANY;
		private EntityPredicate vehicle = EntityPredicate.ANY;
		private EntityPredicate passenger = EntityPredicate.ANY;
		private EntityPredicate targetedEntity = EntityPredicate.ANY;
		@Nullable
		private String team;

		public static EntityPredicate.Builder entity() {
			return new EntityPredicate.Builder();
		}

		public EntityPredicate.Builder of(EntityType<?> entityType) {
			this.entityType = EntityTypePredicate.of(entityType);
			return this;
		}

		public EntityPredicate.Builder of(TagKey<EntityType<?>> tagKey) {
			this.entityType = EntityTypePredicate.of(tagKey);
			return this;
		}

		public EntityPredicate.Builder entityType(EntityTypePredicate entityTypePredicate) {
			this.entityType = entityTypePredicate;
			return this;
		}

		public EntityPredicate.Builder distance(DistancePredicate distancePredicate) {
			this.distanceToPlayer = distancePredicate;
			return this;
		}

		public EntityPredicate.Builder located(LocationPredicate locationPredicate) {
			this.location = locationPredicate;
			return this;
		}

		public EntityPredicate.Builder steppingOn(LocationPredicate locationPredicate) {
			this.steppingOnLocation = locationPredicate;
			return this;
		}

		public EntityPredicate.Builder effects(MobEffectsPredicate mobEffectsPredicate) {
			this.effects = mobEffectsPredicate;
			return this;
		}

		public EntityPredicate.Builder nbt(NbtPredicate nbtPredicate) {
			this.nbt = nbtPredicate;
			return this;
		}

		public EntityPredicate.Builder flags(EntityFlagsPredicate entityFlagsPredicate) {
			this.flags = entityFlagsPredicate;
			return this;
		}

		public EntityPredicate.Builder equipment(EntityEquipmentPredicate entityEquipmentPredicate) {
			this.equipment = entityEquipmentPredicate;
			return this;
		}

		public EntityPredicate.Builder subPredicate(EntitySubPredicate entitySubPredicate) {
			this.subPredicate = entitySubPredicate;
			return this;
		}

		public EntityPredicate.Builder vehicle(EntityPredicate entityPredicate) {
			this.vehicle = entityPredicate;
			return this;
		}

		public EntityPredicate.Builder passenger(EntityPredicate entityPredicate) {
			this.passenger = entityPredicate;
			return this;
		}

		public EntityPredicate.Builder targetedEntity(EntityPredicate entityPredicate) {
			this.targetedEntity = entityPredicate;
			return this;
		}

		public EntityPredicate.Builder team(@Nullable String string) {
			this.team = string;
			return this;
		}

		public EntityPredicate build() {
			return new EntityPredicate(
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

	public static class Composite {
		public static final EntityPredicate.Composite ANY = new EntityPredicate.Composite(new LootItemCondition[0]);
		private final LootItemCondition[] conditions;
		private final Predicate<LootContext> compositePredicates;

		private Composite(LootItemCondition[] lootItemConditions) {
			this.conditions = lootItemConditions;
			this.compositePredicates = LootItemConditions.andConditions(lootItemConditions);
		}

		public static EntityPredicate.Composite create(LootItemCondition... lootItemConditions) {
			return new EntityPredicate.Composite(lootItemConditions);
		}

		public static EntityPredicate.Composite fromJson(JsonObject jsonObject, String string, DeserializationContext deserializationContext) {
			JsonElement jsonElement = jsonObject.get(string);
			return fromElement(string, deserializationContext, jsonElement);
		}

		public static EntityPredicate.Composite[] fromJsonArray(JsonObject jsonObject, String string, DeserializationContext deserializationContext) {
			JsonElement jsonElement = jsonObject.get(string);
			if (jsonElement != null && !jsonElement.isJsonNull()) {
				JsonArray jsonArray = GsonHelper.convertToJsonArray(jsonElement, string);
				EntityPredicate.Composite[] composites = new EntityPredicate.Composite[jsonArray.size()];

				for (int i = 0; i < jsonArray.size(); i++) {
					composites[i] = fromElement(string + "[" + i + "]", deserializationContext, jsonArray.get(i));
				}

				return composites;
			} else {
				return new EntityPredicate.Composite[0];
			}
		}

		private static EntityPredicate.Composite fromElement(String string, DeserializationContext deserializationContext, @Nullable JsonElement jsonElement) {
			if (jsonElement != null && jsonElement.isJsonArray()) {
				LootItemCondition[] lootItemConditions = deserializationContext.deserializeConditions(
					jsonElement.getAsJsonArray(), deserializationContext.getAdvancementId() + "/" + string, LootContextParamSets.ADVANCEMENT_ENTITY
				);
				return new EntityPredicate.Composite(lootItemConditions);
			} else {
				EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonElement);
				return wrap(entityPredicate);
			}
		}

		public static EntityPredicate.Composite wrap(EntityPredicate entityPredicate) {
			if (entityPredicate == EntityPredicate.ANY) {
				return ANY;
			} else {
				LootItemCondition lootItemCondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, entityPredicate).build();
				return new EntityPredicate.Composite(new LootItemCondition[]{lootItemCondition});
			}
		}

		public boolean matches(LootContext lootContext) {
			return this.compositePredicates.test(lootContext);
		}

		public JsonElement toJson(SerializationContext serializationContext) {
			return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : serializationContext.serializeConditions(this.conditions));
		}

		public static JsonElement toJson(EntityPredicate.Composite[] composites, SerializationContext serializationContext) {
			if (composites.length == 0) {
				return JsonNull.INSTANCE;
			} else {
				JsonArray jsonArray = new JsonArray();

				for (EntityPredicate.Composite composite : composites) {
					jsonArray.add(composite.toJson(serializationContext));
				}

				return jsonArray;
			}
		}
	}
}
