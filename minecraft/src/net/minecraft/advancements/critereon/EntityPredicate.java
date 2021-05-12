package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
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
		MobEffectsPredicate.ANY,
		NbtPredicate.ANY,
		EntityFlagsPredicate.ANY,
		EntityEquipmentPredicate.ANY,
		PlayerPredicate.ANY,
		FishingHookPredicate.ANY,
		null,
		null
	);
	private final EntityTypePredicate entityType;
	private final DistancePredicate distanceToPlayer;
	private final LocationPredicate location;
	private final MobEffectsPredicate effects;
	private final NbtPredicate nbt;
	private final EntityFlagsPredicate flags;
	private final EntityEquipmentPredicate equipment;
	private final PlayerPredicate player;
	private final FishingHookPredicate fishingHook;
	private final EntityPredicate vehicle;
	private final EntityPredicate targetedEntity;
	@Nullable
	private final String team;
	@Nullable
	private final ResourceLocation catType;

	private EntityPredicate(
		EntityTypePredicate entityTypePredicate,
		DistancePredicate distancePredicate,
		LocationPredicate locationPredicate,
		MobEffectsPredicate mobEffectsPredicate,
		NbtPredicate nbtPredicate,
		EntityFlagsPredicate entityFlagsPredicate,
		EntityEquipmentPredicate entityEquipmentPredicate,
		PlayerPredicate playerPredicate,
		FishingHookPredicate fishingHookPredicate,
		@Nullable String string,
		@Nullable ResourceLocation resourceLocation
	) {
		this.entityType = entityTypePredicate;
		this.distanceToPlayer = distancePredicate;
		this.location = locationPredicate;
		this.effects = mobEffectsPredicate;
		this.nbt = nbtPredicate;
		this.flags = entityFlagsPredicate;
		this.equipment = entityEquipmentPredicate;
		this.player = playerPredicate;
		this.fishingHook = fishingHookPredicate;
		this.vehicle = this;
		this.targetedEntity = this;
		this.team = string;
		this.catType = resourceLocation;
	}

	EntityPredicate(
		EntityTypePredicate entityTypePredicate,
		DistancePredicate distancePredicate,
		LocationPredicate locationPredicate,
		MobEffectsPredicate mobEffectsPredicate,
		NbtPredicate nbtPredicate,
		EntityFlagsPredicate entityFlagsPredicate,
		EntityEquipmentPredicate entityEquipmentPredicate,
		PlayerPredicate playerPredicate,
		FishingHookPredicate fishingHookPredicate,
		EntityPredicate entityPredicate,
		EntityPredicate entityPredicate2,
		@Nullable String string,
		@Nullable ResourceLocation resourceLocation
	) {
		this.entityType = entityTypePredicate;
		this.distanceToPlayer = distancePredicate;
		this.location = locationPredicate;
		this.effects = mobEffectsPredicate;
		this.nbt = nbtPredicate;
		this.flags = entityFlagsPredicate;
		this.equipment = entityEquipmentPredicate;
		this.player = playerPredicate;
		this.fishingHook = fishingHookPredicate;
		this.vehicle = entityPredicate;
		this.targetedEntity = entityPredicate2;
		this.team = string;
		this.catType = resourceLocation;
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
			} else if (!this.effects.matches(entity)) {
				return false;
			} else if (!this.nbt.matches(entity)) {
				return false;
			} else if (!this.flags.matches(entity)) {
				return false;
			} else if (!this.equipment.matches(entity)) {
				return false;
			} else if (!this.player.matches(entity)) {
				return false;
			} else if (!this.fishingHook.matches(entity)) {
				return false;
			} else if (!this.vehicle.matches(serverLevel, vec3, entity.getVehicle())) {
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

				return this.catType == null || entity instanceof Cat && ((Cat)entity).getResourceLocation().equals(this.catType);
			}
		}
	}

	public static EntityPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entity");
			EntityTypePredicate entityTypePredicate = EntityTypePredicate.fromJson(jsonObject.get("type"));
			DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
			LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
			MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
			NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
			EntityFlagsPredicate entityFlagsPredicate = EntityFlagsPredicate.fromJson(jsonObject.get("flags"));
			EntityEquipmentPredicate entityEquipmentPredicate = EntityEquipmentPredicate.fromJson(jsonObject.get("equipment"));
			PlayerPredicate playerPredicate = PlayerPredicate.fromJson(jsonObject.get("player"));
			FishingHookPredicate fishingHookPredicate = FishingHookPredicate.fromJson(jsonObject.get("fishing_hook"));
			EntityPredicate entityPredicate = fromJson(jsonObject.get("vehicle"));
			EntityPredicate entityPredicate2 = fromJson(jsonObject.get("targeted_entity"));
			String string = GsonHelper.getAsString(jsonObject, "team", null);
			ResourceLocation resourceLocation = jsonObject.has("catType") ? new ResourceLocation(GsonHelper.getAsString(jsonObject, "catType")) : null;
			return new EntityPredicate.Builder()
				.entityType(entityTypePredicate)
				.distance(distancePredicate)
				.located(locationPredicate)
				.effects(mobEffectsPredicate)
				.nbt(nbtPredicate)
				.flags(entityFlagsPredicate)
				.equipment(entityEquipmentPredicate)
				.player(playerPredicate)
				.fishingHook(fishingHookPredicate)
				.team(string)
				.vehicle(entityPredicate)
				.targetedEntity(entityPredicate2)
				.catType(resourceLocation)
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
			jsonObject.add("effects", this.effects.serializeToJson());
			jsonObject.add("nbt", this.nbt.serializeToJson());
			jsonObject.add("flags", this.flags.serializeToJson());
			jsonObject.add("equipment", this.equipment.serializeToJson());
			jsonObject.add("player", this.player.serializeToJson());
			jsonObject.add("fishing_hook", this.fishingHook.serializeToJson());
			jsonObject.add("vehicle", this.vehicle.serializeToJson());
			jsonObject.add("targeted_entity", this.targetedEntity.serializeToJson());
			jsonObject.addProperty("team", this.team);
			if (this.catType != null) {
				jsonObject.addProperty("catType", this.catType.toString());
			}

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
		private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
		private NbtPredicate nbt = NbtPredicate.ANY;
		private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
		private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
		private PlayerPredicate player = PlayerPredicate.ANY;
		private FishingHookPredicate fishingHook = FishingHookPredicate.ANY;
		private EntityPredicate vehicle = EntityPredicate.ANY;
		private EntityPredicate targetedEntity = EntityPredicate.ANY;
		private String team;
		private ResourceLocation catType;

		public static EntityPredicate.Builder entity() {
			return new EntityPredicate.Builder();
		}

		public EntityPredicate.Builder of(EntityType<?> entityType) {
			this.entityType = EntityTypePredicate.of(entityType);
			return this;
		}

		public EntityPredicate.Builder of(Tag<EntityType<?>> tag) {
			this.entityType = EntityTypePredicate.of(tag);
			return this;
		}

		public EntityPredicate.Builder of(ResourceLocation resourceLocation) {
			this.catType = resourceLocation;
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

		public EntityPredicate.Builder player(PlayerPredicate playerPredicate) {
			this.player = playerPredicate;
			return this;
		}

		public EntityPredicate.Builder fishingHook(FishingHookPredicate fishingHookPredicate) {
			this.fishingHook = fishingHookPredicate;
			return this;
		}

		public EntityPredicate.Builder vehicle(EntityPredicate entityPredicate) {
			this.vehicle = entityPredicate;
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

		public EntityPredicate.Builder catType(@Nullable ResourceLocation resourceLocation) {
			this.catType = resourceLocation;
			return this;
		}

		public EntityPredicate build() {
			return new EntityPredicate(
				this.entityType,
				this.distanceToPlayer,
				this.location,
				this.effects,
				this.nbt,
				this.flags,
				this.equipment,
				this.player,
				this.fishingHook,
				this.vehicle,
				this.targetedEntity,
				this.team,
				this.catType
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
