package net.minecraft.client.resources.model;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ModelDiscovery {
	static final Logger LOGGER = LogUtils.getLogger();
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

	private void registerItemTopModel(ResourceLocation resourceLocation) {
		ModelResourceLocation modelResourceLocation = ModelResourceLocation.inventory(resourceLocation);
		ResourceLocation resourceLocation2 = resourceLocation.withPrefix("item/");
		UnbakedModel unbakedModel = this.getBlockModel(resourceLocation2);
		this.registerTopModel(modelResourceLocation, unbakedModel);
	}

	private void registerSpecialItemTopModel(ModelResourceLocation modelResourceLocation) {
		ResourceLocation resourceLocation = modelResourceLocation.id().withPrefix("item/");
		UnbakedModel unbakedModel = this.getBlockModel(resourceLocation);
		this.registerTopModel(modelResourceLocation, unbakedModel);
	}

	private void registerTopModel(ModelResourceLocation modelResourceLocation, UnbakedModel unbakedModel) {
		this.topModels.put(modelResourceLocation, unbakedModel);
	}

	public void registerStandardModels(BlockStateModelLoader.LoadedModels loadedModels) {
		this.referencedModels.put(SpecialModels.BUILTIN_GENERATED, SpecialModels.GENERATED_MARKER);
		this.referencedModels.put(SpecialModels.BUILTIN_BLOCK_ENTITY, SpecialModels.BLOCK_ENTITY_MARKER);
		loadedModels.models().forEach((modelResourceLocation, loadedModel) -> this.registerTopModel(modelResourceLocation, loadedModel.model()));

		for (ResourceLocation resourceLocation : BuiltInRegistries.ITEM.keySet()) {
			this.registerItemTopModel(resourceLocation);
		}

		this.registerSpecialItemTopModel(ItemRenderer.TRIDENT_IN_HAND_MODEL);
		this.registerSpecialItemTopModel(ItemRenderer.SPYGLASS_IN_HAND_MODEL);
		this.registerSpecialItemTopModel(ItemRenderer.getBundleOpenFrontModelLocation((BundleItem)Items.BUNDLE));
		this.registerSpecialItemTopModel(ItemRenderer.getBundleOpenBackModelLocation((BundleItem)Items.BUNDLE));
	}

	public void discoverDependencies() {
		this.topModels.values().forEach(unbakedModel -> unbakedModel.resolveDependencies(new ModelDiscovery.ResolverImpl(), UnbakedModel.ResolutionContext.TOP));
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
		private UnbakedModel.ResolutionContext context = UnbakedModel.ResolutionContext.TOP;

		@Override
		public UnbakedModel resolve(ResourceLocation resourceLocation) {
			return this.resolve(resourceLocation, false);
		}

		@Override
		public UnbakedModel resolveForOverride(ResourceLocation resourceLocation) {
			if (this.context == UnbakedModel.ResolutionContext.OVERRIDE) {
				ModelDiscovery.LOGGER.warn("Re-entrant override in {}->{}", this.stacktraceToString(), resourceLocation);
			}

			this.context = UnbakedModel.ResolutionContext.OVERRIDE;
			UnbakedModel unbakedModel = this.resolve(resourceLocation, true);
			this.context = UnbakedModel.ResolutionContext.TOP;
			return unbakedModel;
		}

		private boolean isReferenceRecursive(ResourceLocation resourceLocation, boolean bl) {
			if (this.stack.isEmpty()) {
				return false;
			} else if (!this.stack.contains(resourceLocation)) {
				return false;
			} else if (bl) {
				ResourceLocation resourceLocation2 = (ResourceLocation)this.stack.getLast();
				return !resourceLocation2.equals(resourceLocation);
			} else {
				return true;
			}
		}

		private UnbakedModel resolve(ResourceLocation resourceLocation, boolean bl) {
			if (this.isReferenceRecursive(resourceLocation, bl)) {
				ModelDiscovery.LOGGER.warn("Detected model loading loop: {}->{}", this.stacktraceToString(), resourceLocation);
				return ModelDiscovery.this.missingModel;
			} else {
				UnbakedModel unbakedModel = ModelDiscovery.this.getBlockModel(resourceLocation);
				if (this.resolvedModels.add(resourceLocation)) {
					this.stack.add(resourceLocation);
					unbakedModel.resolveDependencies(this, this.context);
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
