package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockListReport implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final DataGenerator generator;

	public BlockListReport(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(HashCache hashCache) throws IOException {
		JsonObject jsonObject = new JsonObject();

		for (Block block : Registry.BLOCK) {
			ResourceLocation resourceLocation = Registry.BLOCK.getKey(block);
			JsonObject jsonObject2 = new JsonObject();
			StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
			if (!stateDefinition.getProperties().isEmpty()) {
				JsonObject jsonObject3 = new JsonObject();

				for (Property<?> property : stateDefinition.getProperties()) {
					JsonArray jsonArray = new JsonArray();

					for (Comparable<?> comparable : property.getPossibleValues()) {
						jsonArray.add(Util.getPropertyName(property, comparable));
					}

					jsonObject3.add(property.getName(), jsonArray);
				}

				jsonObject2.add("properties", jsonObject3);
			}

			JsonArray jsonArray2 = new JsonArray();

			for (BlockState blockState : stateDefinition.getPossibleStates()) {
				JsonObject jsonObject4 = new JsonObject();
				JsonObject jsonObject5 = new JsonObject();

				for (Property<?> property2 : stateDefinition.getProperties()) {
					jsonObject5.addProperty(property2.getName(), Util.getPropertyName(property2, blockState.getValue(property2)));
				}

				if (jsonObject5.size() > 0) {
					jsonObject4.add("properties", jsonObject5);
				}

				jsonObject4.addProperty("id", Block.getId(blockState));
				if (blockState == block.defaultBlockState()) {
					jsonObject4.addProperty("default", true);
				}

				jsonArray2.add(jsonObject4);
			}

			jsonObject2.add("states", jsonArray2);
			jsonObject.add(resourceLocation.toString(), jsonObject2);
		}

		Path path = this.generator.getOutputFolder().resolve("reports/blocks.json");
		DataProvider.save(GSON, hashCache, jsonObject, path);
	}

	@Override
	public String getName() {
		return "Block List";
	}
}
