package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExplorationMapFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
	private final String destination;
	private final MapDecoration.Type mapDecoration;
	private final byte zoom;
	private final int searchRadius;
	private final boolean skipKnownStructures;

	private ExplorationMapFunction(LootItemCondition[] lootItemConditions, String string, MapDecoration.Type type, byte b, int i, boolean bl) {
		super(lootItemConditions);
		this.destination = string;
		this.mapDecoration = type;
		this.zoom = b;
		this.searchRadius = i;
		this.skipKnownStructures = bl;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.BLOCK_POS);
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.getItem() != Items.MAP) {
			return itemStack;
		} else {
			BlockPos blockPos = lootContext.getParamOrNull(LootContextParams.BLOCK_POS);
			if (blockPos != null) {
				ServerLevel serverLevel = lootContext.getLevel();
				BlockPos blockPos2 = serverLevel.findNearestMapFeature(this.destination, blockPos, this.searchRadius, this.skipKnownStructures);
				if (blockPos2 != null) {
					ItemStack itemStack2 = MapItem.create(serverLevel, blockPos2.getX(), blockPos2.getZ(), this.zoom, true, true);
					MapItem.renderBiomePreviewMap(serverLevel, itemStack2);
					MapItemSavedData.addTargetDecoration(itemStack2, blockPos2, "+", this.mapDecoration);
					itemStack2.setHoverName(new TranslatableComponent("filled_map." + this.destination.toLowerCase(Locale.ROOT)));
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
		private String destination = "Buried_Treasure";
		private MapDecoration.Type mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
		private byte zoom = 2;
		private int searchRadius = 50;
		private boolean skipKnownStructures = true;

		protected ExplorationMapFunction.Builder getThis() {
			return this;
		}

		public ExplorationMapFunction.Builder setDestination(String string) {
			this.destination = string;
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
		protected Serializer() {
			super(new ResourceLocation("exploration_map"), ExplorationMapFunction.class);
		}

		public void serialize(JsonObject jsonObject, ExplorationMapFunction explorationMapFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, explorationMapFunction, jsonSerializationContext);
			if (!explorationMapFunction.destination.equals("Buried_Treasure")) {
				jsonObject.add("destination", jsonSerializationContext.serialize(explorationMapFunction.destination));
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
			String string = jsonObject.has("destination") ? GsonHelper.getAsString(jsonObject, "destination") : "Buried_Treasure";
			string = Feature.STRUCTURES_REGISTRY.containsKey(string.toLowerCase(Locale.ROOT)) ? string : "Buried_Treasure";
			String string2 = jsonObject.has("decoration") ? GsonHelper.getAsString(jsonObject, "decoration") : "mansion";
			MapDecoration.Type type = ExplorationMapFunction.DEFAULT_DECORATION;

			try {
				type = MapDecoration.Type.valueOf(string2.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException var10) {
				ExplorationMapFunction.LOGGER
					.error("Error while parsing loot table decoration entry. Found {}. Defaulting to " + ExplorationMapFunction.DEFAULT_DECORATION, string2);
			}

			byte b = GsonHelper.getAsByte(jsonObject, "zoom", (byte)2);
			int i = GsonHelper.getAsInt(jsonObject, "search_radius", 50);
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "skip_existing_chunks", true);
			return new ExplorationMapFunction(lootItemConditions, string, type, b, i, bl);
		}
	}
}
