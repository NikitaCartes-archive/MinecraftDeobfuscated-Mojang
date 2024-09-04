package net.minecraft.client.resources.model;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ModelDiscovery {
	static final Logger LOGGER = LogUtils.getLogger();
	public static final String INVENTORY_MODEL_PREFIX = "item/";
	private final Map<ResourceLocation, UnbakedModel> inputModels;
	final UnbakedModel missingModel;
	private final Map<ModelResourceLocation, UnbakedModel> topModels = new HashMap();
	private final Map<ResourceLocation, UnbakedModel> referencedModels = new HashMap();

	public ModelDiscovery(Map<ResourceLocation, UnbakedModel> map, UnbakedModel unbakedModel) {
		this.inputModels = map;
		this.missingModel = unbakedModel;
		this.registerTopModel(MissingBlockModel.VARIANT, unbakedModel);
		this.referencedModels.put(MissingBlockModel.LOCATION, unbakedModel);
	}

	private static Set<ModelResourceLocation> listMandatoryModels() {
		Set<ModelResourceLocation> set = new HashSet();
		BuiltInRegistries.ITEM.listElements().forEach(reference -> {
			ResourceLocation resourceLocation = ((Item)reference.value()).components().get(DataComponents.ITEM_MODEL);
			if (resourceLocation != null) {
				set.add(ModelResourceLocation.inventory(resourceLocation));
			}

			if (reference.value() instanceof BundleItem bundleItem) {
				set.add(ModelResourceLocation.inventory(bundleItem.openFrontModel()));
				set.add(ModelResourceLocation.inventory(bundleItem.openBackModel()));
			}
		});
		set.add(ItemRenderer.TRIDENT_MODEL);
		set.add(ItemRenderer.SPYGLASS_MODEL);
		return set;
	}

	private void registerTopModel(ModelResourceLocation modelResourceLocation, UnbakedModel unbakedModel) {
		this.topModels.put(modelResourceLocation, unbakedModel);
	}

	public void registerStandardModels(BlockStateModelLoader.LoadedModels loadedModels) {
		this.referencedModels.put(SpecialModels.BUILTIN_GENERATED, SpecialModels.GENERATED_MARKER);
		this.referencedModels.put(SpecialModels.BUILTIN_BLOCK_ENTITY, SpecialModels.BLOCK_ENTITY_MARKER);
		Set<ModelResourceLocation> set = listMandatoryModels();
		loadedModels.models().forEach((modelResourceLocation, loadedModel) -> {
			this.registerTopModel(modelResourceLocation, loadedModel.model());
			set.remove(modelResourceLocation);
		});
		this.inputModels
			.keySet()
			.forEach(
				resourceLocation -> {
					if (resourceLocation.getPath().startsWith("item/")) {
						ModelResourceLocation modelResourceLocation = ModelResourceLocation.inventory(
							resourceLocation.withPath((UnaryOperator<String>)(string -> string.substring("item/".length())))
						);
						this.registerTopModel(modelResourceLocation, new ItemModel(resourceLocation));
						set.remove(modelResourceLocation);
					}
				}
			);
		if (!set.isEmpty()) {
			LOGGER.warn("Missing mandatory models: {}", set.stream().map(modelResourceLocation -> "\n\t" + modelResourceLocation).collect(Collectors.joining()));
		}
	}

	public void discoverDependencies() {
		this.topModels.values().forEach(unbakedModel -> unbakedModel.resolveDependencies(new ModelDiscovery.ResolverImpl()));
	}

	public Map<ModelResourceLocation, UnbakedModel> getTopModels() {
		return this.topModels;
	}

	public Map<ResourceLocation, UnbakedModel> getReferencedModels() {
		return this.referencedModels;
	}

	UnbakedModel getBlockModel(ResourceLocation resourceLocation) {
		return (UnbakedModel)this.referencedModels.computeIfAbsent(resourceLocation, this::loadBlockModel);
	}

	private UnbakedModel loadBlockModel(ResourceLocation resourceLocation) {
		UnbakedModel unbakedModel = (UnbakedModel)this.inputModels.get(resourceLocation);
		if (unbakedModel == null) {
			LOGGER.warn("Missing block model: '{}'", resourceLocation);
			return this.missingModel;
		} else {
			return unbakedModel;
		}
	}

	@Environment(EnvType.CLIENT)
	class ResolverImpl implements UnbakedModel.Resolver {
		private final List<ResourceLocation> stack = new ArrayList();
		private final Set<ResourceLocation> resolvedModels = new HashSet();

		@Override
		public UnbakedModel resolve(ResourceLocation resourceLocation) {
			if (this.stack.contains(resourceLocation)) {
				ModelDiscovery.LOGGER.warn("Detected model loading loop: {}->{}", this.stacktraceToString(), resourceLocation);
				return ModelDiscovery.this.missingModel;
			} else {
				UnbakedModel unbakedModel = ModelDiscovery.this.getBlockModel(resourceLocation);
				if (this.resolvedModels.add(resourceLocation)) {
					this.stack.add(resourceLocation);
					unbakedModel.resolveDependencies(this);
					this.stack.remove(resourceLocation);
				}

				return unbakedModel;
			}
		}

		private String stacktraceToString() {
			return (String)this.stack.stream().map(ResourceLocation::toString).collect(Collectors.joining("->"));
		}
	}
}
