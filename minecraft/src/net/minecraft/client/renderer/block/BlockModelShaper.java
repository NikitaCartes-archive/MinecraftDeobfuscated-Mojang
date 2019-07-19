package net.minecraft.client.renderer.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

@Environment(EnvType.CLIENT)
public class BlockModelShaper {
	private final Map<BlockState, BakedModel> modelByStateCache = Maps.<BlockState, BakedModel>newIdentityHashMap();
	private final ModelManager modelManager;

	public BlockModelShaper(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public TextureAtlasSprite getParticleIcon(BlockState blockState) {
		return this.getBlockModel(blockState).getParticleIcon();
	}

	public BakedModel getBlockModel(BlockState blockState) {
		BakedModel bakedModel = (BakedModel)this.modelByStateCache.get(blockState);
		if (bakedModel == null) {
			bakedModel = this.modelManager.getMissingModel();
		}

		return bakedModel;
	}

	public ModelManager getModelManager() {
		return this.modelManager;
	}

	public void rebuildCache() {
		this.modelByStateCache.clear();

		for (Block block : Registry.BLOCK) {
			block.getStateDefinition().getPossibleStates().forEach(blockState -> {
				BakedModel var10000 = (BakedModel)this.modelByStateCache.put(blockState, this.modelManager.getModel(stateToModelLocation(blockState)));
			});
		}
	}

	public static ModelResourceLocation stateToModelLocation(BlockState blockState) {
		return stateToModelLocation(Registry.BLOCK.getKey(blockState.getBlock()), blockState);
	}

	public static ModelResourceLocation stateToModelLocation(ResourceLocation resourceLocation, BlockState blockState) {
		return new ModelResourceLocation(resourceLocation, statePropertiesToString(blockState.getValues()));
	}

	public static String statePropertiesToString(Map<Property<?>, Comparable<?>> map) {
		StringBuilder stringBuilder = new StringBuilder();

		for (Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
			if (stringBuilder.length() != 0) {
				stringBuilder.append(',');
			}

			Property<?> property = (Property<?>)entry.getKey();
			stringBuilder.append(property.getName());
			stringBuilder.append('=');
			stringBuilder.append(getValue(property, (Comparable<?>)entry.getValue()));
		}

		return stringBuilder.toString();
	}

	private static <T extends Comparable<T>> String getValue(Property<T> property, Comparable<?> comparable) {
		return property.getName((T)comparable);
	}
}
