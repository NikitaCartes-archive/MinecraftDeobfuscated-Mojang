package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemModelShaper {
	public final Int2ObjectMap<ModelResourceLocation> shapes = new Int2ObjectOpenHashMap<>(256);
	private final Int2ObjectMap<BakedModel> shapesCache = new Int2ObjectOpenHashMap<>(256);
	private final ModelManager modelManager;
	private int version;

	public ItemModelShaper(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public BakedModel getItemModel(ItemStack itemStack) {
		BakedModel bakedModel = this.getItemModel(itemStack.getItem());
		return bakedModel == null ? this.modelManager.getMissingModel() : bakedModel;
	}

	@Nullable
	public BakedModel getItemModel(Item item) {
		int i = Rules.REPLACE_ITEM_MODEL.versionId();
		if (this.version != i) {
			this.rebuildCache();
			this.version = i;
		}

		return this.shapesCache.get(getIndex(item));
	}

	private static int getIndex(Item item) {
		return Item.getId(item);
	}

	public void register(Item item, ModelResourceLocation modelResourceLocation) {
		this.shapes.put(getIndex(item), modelResourceLocation);
	}

	public ModelManager getModelManager() {
		return this.modelManager;
	}

	public void rebuildCache() {
		this.shapesCache.clear();

		for (Entry<ModelResourceLocation> entry : this.shapes.int2ObjectEntrySet()) {
			int i = entry.getIntKey();
			Item item = Item.byId(i);
			Item item2 = Rules.REPLACE_ITEM_MODEL.replace(item);
			ModelResourceLocation modelResourceLocation;
			if (item == item2) {
				modelResourceLocation = (ModelResourceLocation)entry.getValue();
			} else {
				int j = Item.getId(item2);
				modelResourceLocation = this.shapes.get(j);
			}

			this.shapesCache.put(i, this.modelManager.getModel(modelResourceLocation));
		}
	}
}
