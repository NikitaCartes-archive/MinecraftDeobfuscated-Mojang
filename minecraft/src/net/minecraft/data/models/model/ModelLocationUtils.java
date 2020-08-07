package net.minecraft.data.models.model;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelLocationUtils {
	@Deprecated
	public static ResourceLocation decorateBlockModelLocation(String string) {
		return new ResourceLocation("minecraft", "block/" + string);
	}

	public static ResourceLocation decorateItemModelLocation(String string) {
		return new ResourceLocation("minecraft", "item/" + string);
	}

	public static ResourceLocation getModelLocation(Block block, String string) {
		ResourceLocation resourceLocation = Registry.BLOCK.getKey(block);
		return new ResourceLocation(resourceLocation.getNamespace(), "block/" + resourceLocation.getPath() + string);
	}

	public static ResourceLocation getModelLocation(Block block) {
		ResourceLocation resourceLocation = Registry.BLOCK.getKey(block);
		return new ResourceLocation(resourceLocation.getNamespace(), "block/" + resourceLocation.getPath());
	}

	public static ResourceLocation getModelLocation(Item item) {
		ResourceLocation resourceLocation = Registry.ITEM.getKey(item);
		return new ResourceLocation(resourceLocation.getNamespace(), "item/" + resourceLocation.getPath());
	}

	public static ResourceLocation getModelLocation(Item item, String string) {
		ResourceLocation resourceLocation = Registry.ITEM.getKey(item);
		return new ResourceLocation(resourceLocation.getNamespace(), "item/" + resourceLocation.getPath() + string);
	}
}
