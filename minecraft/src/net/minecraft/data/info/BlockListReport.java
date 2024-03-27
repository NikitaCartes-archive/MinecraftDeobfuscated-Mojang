package net.minecraft.data.info;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockListReport implements DataProvider {
	private final PackOutput output;
	private final CompletableFuture<HolderLookup.Provider> registries;

	public BlockListReport(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		this.output = packOutput;
		this.registries = completableFuture;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("blocks.json");
		return this.registries
			.thenCompose(
				provider -> {
					JsonObject jsonObject = new JsonObject();
					RegistryOps<JsonElement> registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
					provider.lookupOrThrow(Registries.BLOCK)
						.listElements()
						.forEach(
							reference -> {
								JsonObject jsonObject2 = new JsonObject();
								StateDefinition<Block, BlockState> stateDefinition = ((Block)reference.value()).getStateDefinition();
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
									if (blockState == ((Block)reference.value()).defaultBlockState()) {
										jsonObject4.addProperty("default", true);
									}

									jsonArray2.add(jsonObject4);
								}

								jsonObject2.add("states", jsonArray2);
								String string = reference.getRegisteredName();
								JsonElement jsonElement = BlockTypes.CODEC
									.codec()
									.encodeStart(registryOps, (Block)reference.value())
									.getOrThrow(string2 -> new AssertionError("Failed to serialize block " + string + " (is type registered in BlockTypes?): " + string2));
								jsonObject2.add("definition", jsonElement);
								jsonObject.add(string, jsonObject2);
							}
						);
					return DataProvider.saveStable(cachedOutput, jsonObject, path);
				}
			);
	}

	@Override
	public final String getName() {
		return "Block List";
	}
}
