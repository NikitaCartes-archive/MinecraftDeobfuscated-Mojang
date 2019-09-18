package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

public class PlayerPredicate {
	public static final PlayerPredicate ANY = new PlayerPredicate.Builder().build();
	private final MinMaxBounds.Ints level;
	private final GameType gameType;
	private final Map<Stat<?>, MinMaxBounds.Ints> stats;
	private final Object2BooleanMap<ResourceLocation> recipes;
	private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements;

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

	private PlayerPredicate(
		MinMaxBounds.Ints ints,
		GameType gameType,
		Map<Stat<?>, MinMaxBounds.Ints> map,
		Object2BooleanMap<ResourceLocation> object2BooleanMap,
		Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> map2
	) {
		this.level = ints;
		this.gameType = gameType;
		this.stats = map;
		this.recipes = object2BooleanMap;
		this.advancements = map2;
	}

	public boolean matches(Entity entity) {
		if (this == ANY) {
			return true;
		} else if (!(entity instanceof ServerPlayer)) {
			return false;
		} else {
			ServerPlayer serverPlayer = (ServerPlayer)entity;
			if (!this.level.matches(serverPlayer.experienceLevel)) {
				return false;
			} else if (this.gameType != GameType.NOT_SET && this.gameType != serverPlayer.gameMode.getGameModeForPlayer()) {
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

				return true;
			}
		}
	}

	public static PlayerPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "player");
			MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("level"));
			String string = GsonHelper.getAsString(jsonObject, "gamemode", "");
			GameType gameType = GameType.byName(string, GameType.NOT_SET);
			Map<Stat<?>, MinMaxBounds.Ints> map = Maps.<Stat<?>, MinMaxBounds.Ints>newHashMap();
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "stats", null);
			if (jsonArray != null) {
				for (JsonElement jsonElement2 : jsonArray) {
					JsonObject jsonObject2 = GsonHelper.convertToJsonObject(jsonElement2, "stats entry");
					ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject2, "type"));
					StatType<?> statType = Registry.STAT_TYPE.get(resourceLocation);
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

			return new PlayerPredicate(ints, gameType, map, object2BooleanMap, map2);
		} else {
			return ANY;
		}
	}

	private static <T> Stat<T> getStat(StatType<T> statType, ResourceLocation resourceLocation) {
		Registry<T> registry = statType.getRegistry();
		T object = registry.get(resourceLocation);
		if (object == null) {
			throw new JsonParseException("Unknown object " + resourceLocation + " for stat type " + Registry.STAT_TYPE.getKey(statType));
		} else {
			return statType.get(object);
		}
	}

	private static <T> ResourceLocation getStatValueId(Stat<T> stat) {
		return stat.getType().getRegistry().getKey(stat.getValue());
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("level", this.level.serializeToJson());
			if (this.gameType != GameType.NOT_SET) {
				jsonObject.addProperty("gamemode", this.gameType.getName());
			}

			if (!this.stats.isEmpty()) {
				JsonArray jsonArray = new JsonArray();
				this.stats.forEach((stat, ints) -> {
					JsonObject jsonObjectx = new JsonObject();
					jsonObjectx.addProperty("type", Registry.STAT_TYPE.getKey(stat.getType()).toString());
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

			return jsonObject;
		}
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
		private GameType gameType = GameType.NOT_SET;
		private final Map<Stat<?>, MinMaxBounds.Ints> stats = Maps.<Stat<?>, MinMaxBounds.Ints>newHashMap();
		private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap<>();
		private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.<ResourceLocation, PlayerPredicate.AdvancementPredicate>newHashMap();

		public PlayerPredicate build() {
			return new PlayerPredicate(this.level, this.gameType, this.stats, this.recipes, this.advancements);
		}
	}
}
