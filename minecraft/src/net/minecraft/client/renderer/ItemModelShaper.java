package net.minecraft.client.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemModelShaper {
	private final Map<ResourceLocation, BakedModel> modelToBakedModel = new HashMap();
	private final Supplier<BakedModel> missingModel;
	private final Function<ResourceLocation, BakedModel> modelGetter;

	public ItemModelShaper(ModelManager modelManager) {
		this.missingModel = modelManager::getMissingModel;
		this.modelGetter = resourceLocation -> modelManager.getModel(ModelResourceLocation.inventory(resourceLocation));
	}

	public BakedModel getItemModel(ItemStack itemStack) {
		ResourceLocation resourceLocation = itemStack.get(DataComponents.ITEM_MODEL);
		return resourceLocation == null ? (BakedModel)this.missingModel.get() : this.getItemModel(resourceLocation);
	}

	public BakedModel getItemModel(ResourceLocation resourceLocation) {
		return (BakedModel)this.modelToBakedModel.computeIfAbsent(resourceLocation, this.modelGetter);
	}

	public void invalidateCache() {
		this.modelToBakedModel.clear();
	}
}
