package net.minecraft.data.models.model;

import java.util.function.UnaryOperator;
import net.minecraft.core.registries.BuiltInRegistries;
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
		ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
		return resourceLocation.withPath((UnaryOperator<String>)(string2 -> "block/" + string2 + string));
	}

	public static ResourceLocation getModelLocation(Block block) {
		ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
		return resourceLocation.withPrefix("block/");
	}

	public static ResourceLocation getModelLocation(Item item) {
		ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(item);
		return resourceLocation.withPrefix("item/");
	}

	public static ResourceLocation getModelLocation(Item item, String string) {
		ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(item);
		return resourceLocation.withPath((UnaryOperator<String>)(string2 -> "item/" + string2 + string));
	}
}
