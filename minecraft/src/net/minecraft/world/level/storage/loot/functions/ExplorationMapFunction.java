package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ExplorationMapFunction extends LootItemConditionalFunction {
	static final Logger LOGGER = LogUtils.getLogger();
	public static final TagKey<Structure> DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
	public static final String DEFAULT_DECORATION_NAME = "mansion";
	public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
	public static final byte DEFAULT_ZOOM = 2;
	public static final int DEFAULT_SEARCH_RADIUS = 50;
	public static final boolean DEFAULT_SKIP_EXISTING = true;
	final TagKey<Structure> destination;
	final MapDecoration.Type mapDecoration;
	final byte zoom;
	final int searchRadius;
	final boolean skipKnownStructures;

	ExplorationMapFunction(LootItemCondition[] lootItemConditions, TagKey<Structure> tagKey, MapDecoration.Type type, byte b, int i, boolean bl) {
		super(lootItemConditions);
		this.destination = tagKey;
		this.mapDecoration = type;
		this.zoom = b;
		this.searchRadius = i;
		this.skipKnownStructures = bl;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.EXPLORATION_MAP;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.ORIGIN);
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (!itemStack.is(Items.MAP)) {
			return itemStack;
		} else {
			Vec3 vec3 = lootContext.getParamOrNull(LootContextParams.ORIGIN);
			if (vec3 != null) {
				ServerLevel serverLevel = lootContext.getLevel();
				BlockPos blockPos = serverLevel.findNearestMapStructure(this.destination, BlockPos.containing(vec3), this.searchRadius, this.skipKnownStructures);
				if (blockPos != null) {
					ItemStack itemStack2 = MapItem.create(serverLevel, blockPos.getX(), blockPos.getZ(), this.zoom, true, true);
					MapItem.renderBiomePreviewMap(serverLevel, itemStack2);
					MapItemSavedData.addTargetDecoration(itemStack2, blockPos, "+", this.mapDecoration);
					return itemStack2;
				}
			}

			return itemStack;
		}
	}

	public static ExplorationMapFunction.Builder makeExplorationMap() {
		return new ExplorationMapFunction.Builder();
	}

	public static class Builder extends LootItemConditionalFunction.Builder<ExplorationMapFunction.Builder> {
		private TagKey<Structure> destination = ExplorationMapFunction.DEFAULT_DESTINATION;
		private MapDecoration.Type mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
		private byte zoom = 2;
		private int searchRadius = 50;
		private boolean skipKnownStructures = true;

		protected ExplorationMapFunction.Builder getThis() {
			return this;
		}

		public ExplorationMapFunction.Builder setDestination(TagKey<Structure> tagKey) {
			this.destination = tagKey;
			return this;
		}

		public ExplorationMapFunction.Builder setMapDecoration(MapDecoration.Type type) {
			this.mapDecoration = type;
			return this;
		}

		public ExplorationMapFunction.Builder setZoom(byte b) {
			this.zoom = b;
			return this;
		}

		public ExplorationMapFunction.Builder setSearchRadius(int i) {
			this.searchRadius = i;
			return this;
		}

		public ExplorationMapFunction.Builder setSkipKnownStructures(boolean bl) {
			this.skipKnownStructures = bl;
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new ExplorationMapFunction(this.getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<ExplorationMapFunction> {
		public void serialize(JsonObject jsonObject, ExplorationMapFunction explorationMapFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, explorationMapFunction, jsonSerializationContext);
			if (!explorationMapFunction.destination.equals(ExplorationMapFunction.DEFAULT_DESTINATION)) {
				jsonObject.addProperty("destination", explorationMapFunction.destination.location().toString());
			}

			if (explorationMapFunction.mapDecoration != ExplorationMapFunction.DEFAULT_DECORATION) {
				jsonObject.add("decoration", jsonSerializationContext.serialize(explorationMapFunction.mapDecoration.toString().toLowerCase(Locale.ROOT)));
			}

			if (explorationMapFunction.zoom != 2) {
				jsonObject.addProperty("zoom", explorationMapFunction.zoom);
			}

			if (explorationMapFunction.searchRadius != 50) {
				jsonObject.addProperty("search_radius", explorationMapFunction.searchRadius);
			}

			if (!explorationMapFunction.skipKnownStructures) {
				jsonObject.addProperty("skip_existing_chunks", explorationMapFunction.skipKnownStructures);
			}
		}

		public ExplorationMapFunction deserialize(
			JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions
		) {
			TagKey<Structure> tagKey = readStructure(jsonObject);
			String string = jsonObject.has("decoration") ? GsonHelper.getAsString(jsonObject, "decoration") : "mansion";
			MapDecoration.Type type = ExplorationMapFunction.DEFAULT_DECORATION;

			try {
				type = MapDecoration.Type.valueOf(string.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException var10) {
				ExplorationMapFunction.LOGGER
					.error("Error while parsing loot table decoration entry. Found {}. Defaulting to {}", string, ExplorationMapFunction.DEFAULT_DECORATION);
			}

			byte b = GsonHelper.getAsByte(jsonObject, "zoom", (byte)2);
			int i = GsonHelper.getAsInt(jsonObject, "search_radius", 50);
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "skip_existing_chunks", true);
			return new ExplorationMapFunction(lootItemConditions, tagKey, type, b, i, bl);
		}

		private static TagKey<Structure> readStructure(JsonObject jsonObject) {
			if (jsonObject.has("destination")) {
				String string = GsonHelper.getAsString(jsonObject, "destination");
				return TagKey.create(Registries.STRUCTURE, new ResourceLocation(string));
			} else {
				return ExplorationMapFunction.DEFAULT_DESTINATION;
			}
		}
	}
}
