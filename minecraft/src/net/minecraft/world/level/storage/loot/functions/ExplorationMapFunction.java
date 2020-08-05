package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExplorationMapFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final StructureFeature<?> DEFAULT_FEATURE = StructureFeature.BURIED_TREASURE;
	public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
	private final StructureFeature<?> destination;
	private final MapDecoration.Type mapDecoration;
	private final byte zoom;
	private final int searchRadius;
	private final boolean skipKnownStructures;

	private ExplorationMapFunction(
		LootItemCondition[] lootItemConditions, StructureFeature<?> structureFeature, MapDecoration.Type type, byte b, int i, boolean bl
	) {
		super(lootItemConditions);
		this.destination = structureFeature;
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
		if (itemStack.getItem() != Items.MAP) {
			return itemStack;
		} else {
			Vec3 vec3 = lootContext.getParamOrNull(LootContextParams.ORIGIN);
			if (vec3 != null) {
				ServerLevel serverLevel = lootContext.getLevel();
				BlockPos blockPos = serverLevel.findNearestMapFeature(this.destination, new BlockPos(vec3), this.searchRadius, this.skipKnownStructures);
				if (blockPos != null) {
					ItemStack itemStack2 = MapItem.create(serverLevel, blockPos.getX(), blockPos.getZ(), this.zoom, true, true);
					MapItem.renderBiomePreviewMap(serverLevel, itemStack2);
					MapItemSavedData.addTargetDecoration(itemStack2, blockPos, "+", this.mapDecoration);
					itemStack2.setHoverName(new TranslatableComponent("filled_map." + this.destination.getFeatureName().toLowerCase(Locale.ROOT)));
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
		private StructureFeature<?> destination = ExplorationMapFunction.DEFAULT_FEATURE;
		private MapDecoration.Type mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
		private byte zoom = 2;
		private int searchRadius = 50;
		private boolean skipKnownStructures = true;

		protected ExplorationMapFunction.Builder getThis() {
			return this;
		}

		public ExplorationMapFunction.Builder setDestination(StructureFeature<?> structureFeature) {
			this.destination = structureFeature;
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
		public void serialize(JsonObject jsonObject, ExplorationMapFunction explorationMapFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, explorationMapFunction, jsonSerializationContext);
			if (!explorationMapFunction.destination.equals(ExplorationMapFunction.DEFAULT_FEATURE)) {
				jsonObject.add("destination", jsonSerializationContext.serialize(explorationMapFunction.destination.getFeatureName()));
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
			StructureFeature<?> structureFeature = readStructure(jsonObject);
			String string = jsonObject.has("decoration") ? GsonHelper.getAsString(jsonObject, "decoration") : "mansion";
			MapDecoration.Type type = ExplorationMapFunction.DEFAULT_DECORATION;

			try {
				type = MapDecoration.Type.valueOf(string.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException var10) {
				ExplorationMapFunction.LOGGER
					.error("Error while parsing loot table decoration entry. Found {}. Defaulting to " + ExplorationMapFunction.DEFAULT_DECORATION, string);
			}

			byte b = GsonHelper.getAsByte(jsonObject, "zoom", (byte)2);
			int i = GsonHelper.getAsInt(jsonObject, "search_radius", 50);
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "skip_existing_chunks", true);
			return new ExplorationMapFunction(lootItemConditions, structureFeature, type, b, i, bl);
		}

		private static StructureFeature<?> readStructure(JsonObject jsonObject) {
			if (jsonObject.has("destination")) {
				String string = GsonHelper.getAsString(jsonObject, "destination");
				StructureFeature<?> structureFeature = (StructureFeature<?>)StructureFeature.STRUCTURES_REGISTRY.get(string.toLowerCase(Locale.ROOT));
				if (structureFeature != null) {
					return structureFeature;
				}
			}

			return ExplorationMapFunction.DEFAULT_FEATURE;
		}
	}
}
