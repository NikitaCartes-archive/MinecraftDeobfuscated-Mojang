package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerPredicate implements EntitySubPredicate {
	public static final int LOOKING_AT_RANGE = 100;
	private final MinMaxBounds.Ints level;
	@Nullable
	private final GameType gameType;
	private final Map<Stat<?>, MinMaxBounds.Ints> stats;
	private final Object2BooleanMap<ResourceLocation> recipes;
	private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements;
	private final EntityPredicate lookingAt;

	private static PlayerPredicate.AdvancementPredicate advancementPredicateFromJson(JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive()) {
			boolean bl = jsonElement.getAsBoolean();
			return new PlayerPredicate.AdvancementDonePredicate(bl);
		} else {
			Object2BooleanMap<String> object2BooleanMap = new Object2BooleanOpenHashMap<>();
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "criterion data");
			jsonObject.entrySet().forEach(entry -> {
				boolean bl = GsonHelper.convertToBoolean((JsonElement)entry.getValue(), "criterion test");
				object2BooleanMap.put((String)entry.getKey(), bl);
			});
			return new PlayerPredicate.AdvancementCriterionsPredicate(object2BooleanMap);
		}
	}

	PlayerPredicate(
		MinMaxBounds.Ints ints,
		@Nullable GameType gameType,
		Map<Stat<?>, MinMaxBounds.Ints> map,
		Object2BooleanMap<ResourceLocation> object2BooleanMap,
		Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> map2,
		EntityPredicate entityPredicate
	) {
		this.level = ints;
		this.gameType = gameType;
		this.stats = map;
		this.recipes = object2BooleanMap;
		this.advancements = map2;
		this.lookingAt = entityPredicate;
	}

	@Override
	public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
		if (!(entity instanceof ServerPlayer serverPlayer)) {
			return false;
		} else if (!this.level.matches(serverPlayer.experienceLevel)) {
			return false;
		} else if (this.gameType != null && this.gameType != serverPlayer.gameMode.getGameModeForPlayer()) {
			return false;
		} else {
			StatsCounter statsCounter = serverPlayer.getStats();

			for (Entry<Stat<?>, MinMaxBounds.Ints> entry : this.stats.entrySet()) {
				int i = statsCounter.getValue((Stat<?>)entry.getKey());
				if (!((MinMaxBounds.Ints)entry.getValue()).matches(i)) {
					return false;
				}
			}

			RecipeBook recipeBook = serverPlayer.getRecipeBook();

			for (it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<ResourceLocation> entry2 : this.recipes.object2BooleanEntrySet()) {
				if (recipeBook.contains((ResourceLocation)entry2.getKey()) != entry2.getBooleanValue()) {
					return false;
				}
			}

			if (!this.advancements.isEmpty()) {
				PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
				ServerAdvancementManager serverAdvancementManager = serverPlayer.getServer().getAdvancements();

				for (Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry3 : this.advancements.entrySet()) {
					Advancement advancement = serverAdvancementManager.getAdvancement((ResourceLocation)entry3.getKey());
					if (advancement == null || !((PlayerPredicate.AdvancementPredicate)entry3.getValue()).test(playerAdvancements.getOrStartProgress(advancement))) {
						return false;
					}
				}
			}

			if (this.lookingAt != EntityPredicate.ANY) {
				Vec3 vec32 = serverPlayer.getEyePosition();
				Vec3 vec33 = serverPlayer.getViewVector(1.0F);
				Vec3 vec34 = vec32.add(vec33.x * 100.0, vec33.y * 100.0, vec33.z * 100.0);
				EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
					serverPlayer.level(), serverPlayer, vec32, vec34, new AABB(vec32, vec34).inflate(1.0), entityx -> !entityx.isSpectator(), 0.0F
				);
				if (entityHitResult == null || entityHitResult.getType() != HitResult.Type.ENTITY) {
					return false;
				}

				Entity entity2 = entityHitResult.getEntity();
				if (!this.lookingAt.matches(serverPlayer, entity2) || !serverPlayer.hasLineOfSight(entity2)) {
					return false;
				}
			}

			return true;
		}
	}

	public static PlayerPredicate fromJson(JsonObject jsonObject) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("level"));
		String string = GsonHelper.getAsString(jsonObject, "gamemode", "");
		GameType gameType = GameType.byName(string, null);
		Map<Stat<?>, MinMaxBounds.Ints> map = Maps.<Stat<?>, MinMaxBounds.Ints>newHashMap();
		JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "stats", null);
		if (jsonArray != null) {
			for (JsonElement jsonElement : jsonArray) {
				JsonObject jsonObject2 = GsonHelper.convertToJsonObject(jsonElement, "stats entry");
				ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject2, "type"));
				StatType<?> statType = BuiltInRegistries.STAT_TYPE.get(resourceLocation);
				if (statType == null) {
					throw new JsonParseException("Invalid stat type: " + resourceLocation);
				}

				ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject2, "stat"));
				Stat<?> stat = getStat(statType, resourceLocation2);
				MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject2.get("value"));
				map.put(stat, ints2);
			}
		}

		Object2BooleanMap<ResourceLocation> object2BooleanMap = new Object2BooleanOpenHashMap<>();
		JsonObject jsonObject3 = GsonHelper.getAsJsonObject(jsonObject, "recipes", new JsonObject());

		for (Entry<String, JsonElement> entry : jsonObject3.entrySet()) {
			ResourceLocation resourceLocation3 = new ResourceLocation((String)entry.getKey());
			boolean bl = GsonHelper.convertToBoolean((JsonElement)entry.getValue(), "recipe present");
			object2BooleanMap.put(resourceLocation3, bl);
		}

		Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> map2 = Maps.<ResourceLocation, PlayerPredicate.AdvancementPredicate>newHashMap();
		JsonObject jsonObject4 = GsonHelper.getAsJsonObject(jsonObject, "advancements", new JsonObject());

		for (Entry<String, JsonElement> entry2 : jsonObject4.entrySet()) {
			ResourceLocation resourceLocation4 = new ResourceLocation((String)entry2.getKey());
			PlayerPredicate.AdvancementPredicate advancementPredicate = advancementPredicateFromJson((JsonElement)entry2.getValue());
			map2.put(resourceLocation4, advancementPredicate);
		}

		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("looking_at"));
		return new PlayerPredicate(ints, gameType, map, object2BooleanMap, map2, entityPredicate);
	}

	private static <T> Stat<T> getStat(StatType<T> statType, ResourceLocation resourceLocation) {
		Registry<T> registry = statType.getRegistry();
		T object = registry.get(resourceLocation);
		if (object == null) {
			throw new JsonParseException("Unknown object " + resourceLocation + " for stat type " + BuiltInRegistries.STAT_TYPE.getKey(statType));
		} else {
			return statType.get(object);
		}
	}

	private static <T> ResourceLocation getStatValueId(Stat<T> stat) {
		return stat.getType().getRegistry().getKey(stat.getValue());
	}

	@Override
	public JsonObject serializeCustomData() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("level", this.level.serializeToJson());
		if (this.gameType != null) {
			jsonObject.addProperty("gamemode", this.gameType.getName());
		}

		if (!this.stats.isEmpty()) {
			JsonArray jsonArray = new JsonArray();
			this.stats.forEach((stat, ints) -> {
				JsonObject jsonObjectx = new JsonObject();
				jsonObjectx.addProperty("type", BuiltInRegistries.STAT_TYPE.getKey(stat.getType()).toString());
				jsonObjectx.addProperty("stat", getStatValueId(stat).toString());
				jsonObjectx.add("value", ints.serializeToJson());
				jsonArray.add(jsonObjectx);
			});
			jsonObject.add("stats", jsonArray);
		}

		if (!this.recipes.isEmpty()) {
			JsonObject jsonObject2 = new JsonObject();
			this.recipes.forEach((resourceLocation, boolean_) -> jsonObject2.addProperty(resourceLocation.toString(), boolean_));
			jsonObject.add("recipes", jsonObject2);
		}

		if (!this.advancements.isEmpty()) {
			JsonObject jsonObject2 = new JsonObject();
			this.advancements.forEach((resourceLocation, advancementPredicate) -> jsonObject2.add(resourceLocation.toString(), advancementPredicate.toJson()));
			jsonObject.add("advancements", jsonObject2);
		}

		jsonObject.add("looking_at", this.lookingAt.serializeToJson());
		return jsonObject;
	}

	@Override
	public EntitySubPredicate.Type type() {
		return EntitySubPredicate.Types.PLAYER;
	}

	static class AdvancementCriterionsPredicate implements PlayerPredicate.AdvancementPredicate {
		private final Object2BooleanMap<String> criterions;

		public AdvancementCriterionsPredicate(Object2BooleanMap<String> object2BooleanMap) {
			this.criterions = object2BooleanMap;
		}

		@Override
		public JsonElement toJson() {
			JsonObject jsonObject = new JsonObject();
			this.criterions.forEach(jsonObject::addProperty);
			return jsonObject;
		}

		public boolean test(AdvancementProgress advancementProgress) {
			for (it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<String> entry : this.criterions.object2BooleanEntrySet()) {
				CriterionProgress criterionProgress = advancementProgress.getCriterion((String)entry.getKey());
				if (criterionProgress == null || criterionProgress.isDone() != entry.getBooleanValue()) {
					return false;
				}
			}

			return true;
		}
	}

	static class AdvancementDonePredicate implements PlayerPredicate.AdvancementPredicate {
		private final boolean state;

		public AdvancementDonePredicate(boolean bl) {
			this.state = bl;
		}

		@Override
		public JsonElement toJson() {
			return new JsonPrimitive(this.state);
		}

		public boolean test(AdvancementProgress advancementProgress) {
			return advancementProgress.isDone() == this.state;
		}
	}

	interface AdvancementPredicate extends Predicate<AdvancementProgress> {
		JsonElement toJson();
	}

	public static class Builder {
		private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
		@Nullable
		private GameType gameType;
		private final Map<Stat<?>, MinMaxBounds.Ints> stats = Maps.<Stat<?>, MinMaxBounds.Ints>newHashMap();
		private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap<>();
		private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.<ResourceLocation, PlayerPredicate.AdvancementPredicate>newHashMap();
		private EntityPredicate lookingAt = EntityPredicate.ANY;

		public static PlayerPredicate.Builder player() {
			return new PlayerPredicate.Builder();
		}

		public PlayerPredicate.Builder setLevel(MinMaxBounds.Ints ints) {
			this.level = ints;
			return this;
		}

		public PlayerPredicate.Builder addStat(Stat<?> stat, MinMaxBounds.Ints ints) {
			this.stats.put(stat, ints);
			return this;
		}

		public PlayerPredicate.Builder addRecipe(ResourceLocation resourceLocation, boolean bl) {
			this.recipes.put(resourceLocation, bl);
			return this;
		}

		public PlayerPredicate.Builder setGameType(GameType gameType) {
			this.gameType = gameType;
			return this;
		}

		public PlayerPredicate.Builder setLookingAt(EntityPredicate entityPredicate) {
			this.lookingAt = entityPredicate;
			return this;
		}

		public PlayerPredicate.Builder checkAdvancementDone(ResourceLocation resourceLocation, boolean bl) {
			this.advancements.put(resourceLocation, new PlayerPredicate.AdvancementDonePredicate(bl));
			return this;
		}

		public PlayerPredicate.Builder checkAdvancementCriterions(ResourceLocation resourceLocation, Map<String, Boolean> map) {
			this.advancements.put(resourceLocation, new PlayerPredicate.AdvancementCriterionsPredicate(new Object2BooleanOpenHashMap<>(map)));
			return this;
		}

		public PlayerPredicate build() {
			return new PlayerPredicate(this.level, this.gameType, this.stats, this.recipes, this.advancements, this.lookingAt);
		}
	}
}
