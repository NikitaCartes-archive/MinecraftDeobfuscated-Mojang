package net.minecraft.data.models.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class TextureMapping {
	private final Map<TextureSlot, ResourceLocation> slots = Maps.<TextureSlot, ResourceLocation>newHashMap();
	private final Set<TextureSlot> forcedSlots = Sets.<TextureSlot>newHashSet();

	public TextureMapping put(TextureSlot textureSlot, ResourceLocation resourceLocation) {
		this.slots.put(textureSlot, resourceLocation);
		return this;
	}

	public TextureMapping putForced(TextureSlot textureSlot, ResourceLocation resourceLocation) {
		this.slots.put(textureSlot, resourceLocation);
		this.forcedSlots.add(textureSlot);
		return this;
	}

	public Stream<TextureSlot> getForced() {
		return this.forcedSlots.stream();
	}

	public TextureMapping copySlot(TextureSlot textureSlot, TextureSlot textureSlot2) {
		this.slots.put(textureSlot2, (ResourceLocation)this.slots.get(textureSlot));
		return this;
	}

	public TextureMapping copyForced(TextureSlot textureSlot, TextureSlot textureSlot2) {
		this.slots.put(textureSlot2, (ResourceLocation)this.slots.get(textureSlot));
		this.forcedSlots.add(textureSlot2);
		return this;
	}

	public ResourceLocation get(TextureSlot textureSlot) {
		for (TextureSlot textureSlot2 = textureSlot; textureSlot2 != null; textureSlot2 = textureSlot2.getParent()) {
			ResourceLocation resourceLocation = (ResourceLocation)this.slots.get(textureSlot2);
			if (resourceLocation != null) {
				return resourceLocation;
			}
		}

		throw new IllegalStateException("Can't find texture for slot " + textureSlot);
	}

	public TextureMapping copyAndUpdate(TextureSlot textureSlot, ResourceLocation resourceLocation) {
		TextureMapping textureMapping = new TextureMapping();
		textureMapping.slots.putAll(this.slots);
		textureMapping.forcedSlots.addAll(this.forcedSlots);
		textureMapping.put(textureSlot, resourceLocation);
		return textureMapping;
	}

	public static TextureMapping cube(Block block) {
		ResourceLocation resourceLocation = getBlockTexture(block);
		return cube(resourceLocation);
	}

	public static TextureMapping defaultTexture(Block block) {
		ResourceLocation resourceLocation = getBlockTexture(block);
		return defaultTexture(resourceLocation);
	}

	public static TextureMapping defaultTexture(ResourceLocation resourceLocation) {
		return new TextureMapping().put(TextureSlot.TEXTURE, resourceLocation);
	}

	public static TextureMapping cube(ResourceLocation resourceLocation) {
		return new TextureMapping().put(TextureSlot.ALL, resourceLocation);
	}

	public static TextureMapping cross(Block block) {
		return singleSlot(TextureSlot.CROSS, getBlockTexture(block));
	}

	public static TextureMapping cross(ResourceLocation resourceLocation) {
		return singleSlot(TextureSlot.CROSS, resourceLocation);
	}

	public static TextureMapping plant(Block block) {
		return singleSlot(TextureSlot.PLANT, getBlockTexture(block));
	}

	public static TextureMapping plant(ResourceLocation resourceLocation) {
		return singleSlot(TextureSlot.PLANT, resourceLocation);
	}

	public static TextureMapping rail(Block block) {
		return singleSlot(TextureSlot.RAIL, getBlockTexture(block));
	}

	public static TextureMapping rail(ResourceLocation resourceLocation) {
		return singleSlot(TextureSlot.RAIL, resourceLocation);
	}

	public static TextureMapping wool(Block block) {
		return singleSlot(TextureSlot.WOOL, getBlockTexture(block));
	}

	public static TextureMapping wool(ResourceLocation resourceLocation) {
		return singleSlot(TextureSlot.WOOL, resourceLocation);
	}

	public static TextureMapping stem(Block block) {
		return singleSlot(TextureSlot.STEM, getBlockTexture(block));
	}

	public static TextureMapping attachedStem(Block block, Block block2) {
		return new TextureMapping().put(TextureSlot.STEM, getBlockTexture(block)).put(TextureSlot.UPPER_STEM, getBlockTexture(block2));
	}

	public static TextureMapping pattern(Block block) {
		return singleSlot(TextureSlot.PATTERN, getBlockTexture(block));
	}

	public static TextureMapping fan(Block block) {
		return singleSlot(TextureSlot.FAN, getBlockTexture(block));
	}

	public static TextureMapping crop(ResourceLocation resourceLocation) {
		return singleSlot(TextureSlot.CROP, resourceLocation);
	}

	public static TextureMapping pane(Block block, Block block2) {
		return new TextureMapping().put(TextureSlot.PANE, getBlockTexture(block)).put(TextureSlot.EDGE, getBlockTexture(block2, "_top"));
	}

	public static TextureMapping singleSlot(TextureSlot textureSlot, ResourceLocation resourceLocation) {
		return new TextureMapping().put(textureSlot, resourceLocation);
	}

	public static TextureMapping column(Block block) {
		return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.END, getBlockTexture(block, "_top"));
	}

	public static TextureMapping cubeTop(Block block) {
		return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.TOP, getBlockTexture(block, "_top"));
	}

	public static TextureMapping logColumn(Block block) {
		return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block)).put(TextureSlot.END, getBlockTexture(block, "_top"));
	}

	public static TextureMapping column(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return new TextureMapping().put(TextureSlot.SIDE, resourceLocation).put(TextureSlot.END, resourceLocation2);
	}

	public static TextureMapping cubeBottomTop(Block block) {
		return new TextureMapping()
			.put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
			.put(TextureSlot.TOP, getBlockTexture(block, "_top"))
			.put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
	}

	public static TextureMapping cubeBottomTopWithWall(Block block) {
		ResourceLocation resourceLocation = getBlockTexture(block);
		return new TextureMapping()
			.put(TextureSlot.WALL, resourceLocation)
			.put(TextureSlot.SIDE, resourceLocation)
			.put(TextureSlot.TOP, getBlockTexture(block, "_top"))
			.put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
	}

	public static TextureMapping columnWithWall(Block block) {
		ResourceLocation resourceLocation = getBlockTexture(block);
		return new TextureMapping()
			.put(TextureSlot.WALL, resourceLocation)
			.put(TextureSlot.SIDE, resourceLocation)
			.put(TextureSlot.END, getBlockTexture(block, "_top"));
	}

	public static TextureMapping door(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return new TextureMapping().put(TextureSlot.TOP, resourceLocation).put(TextureSlot.BOTTOM, resourceLocation2);
	}

	public static TextureMapping door(Block block) {
		return new TextureMapping().put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
	}

	public static TextureMapping particle(Block block) {
		return new TextureMapping().put(TextureSlot.PARTICLE, getBlockTexture(block));
	}

	public static TextureMapping particle(ResourceLocation resourceLocation) {
		return new TextureMapping().put(TextureSlot.PARTICLE, resourceLocation);
	}

	public static TextureMapping fire0(Block block) {
		return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(block, "_0"));
	}

	public static TextureMapping fire1(Block block) {
		return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(block, "_1"));
	}

	public static TextureMapping lantern(Block block) {
		return new TextureMapping().put(TextureSlot.LANTERN, getBlockTexture(block));
	}

	public static TextureMapping torch(Block block) {
		return new TextureMapping().put(TextureSlot.TORCH, getBlockTexture(block));
	}

	public static TextureMapping torch(ResourceLocation resourceLocation) {
		return new TextureMapping().put(TextureSlot.TORCH, resourceLocation);
	}

	public static TextureMapping particleFromItem(Item item) {
		return new TextureMapping().put(TextureSlot.PARTICLE, getItemTexture(item));
	}

	public static TextureMapping commandBlock(Block block) {
		return new TextureMapping()
			.put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
			.put(TextureSlot.FRONT, getBlockTexture(block, "_front"))
			.put(TextureSlot.BACK, getBlockTexture(block, "_back"));
	}

	public static TextureMapping orientableCube(Block block) {
		return new TextureMapping()
			.put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
			.put(TextureSlot.FRONT, getBlockTexture(block, "_front"))
			.put(TextureSlot.TOP, getBlockTexture(block, "_top"))
			.put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
	}

	public static TextureMapping orientableCubeOnlyTop(Block block) {
		return new TextureMapping()
			.put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
			.put(TextureSlot.FRONT, getBlockTexture(block, "_front"))
			.put(TextureSlot.TOP, getBlockTexture(block, "_top"));
	}

	public static TextureMapping orientableCubeSameEnds(Block block) {
		return new TextureMapping()
			.put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
			.put(TextureSlot.FRONT, getBlockTexture(block, "_front"))
			.put(TextureSlot.END, getBlockTexture(block, "_end"));
	}

	public static TextureMapping top(Block block) {
		return new TextureMapping().put(TextureSlot.TOP, getBlockTexture(block, "_top"));
	}

	public static TextureMapping craftingTable(Block block, Block block2) {
		return new TextureMapping()
			.put(TextureSlot.PARTICLE, getBlockTexture(block, "_front"))
			.put(TextureSlot.DOWN, getBlockTexture(block2))
			.put(TextureSlot.UP, getBlockTexture(block, "_top"))
			.put(TextureSlot.NORTH, getBlockTexture(block, "_front"))
			.put(TextureSlot.EAST, getBlockTexture(block, "_side"))
			.put(TextureSlot.SOUTH, getBlockTexture(block, "_side"))
			.put(TextureSlot.WEST, getBlockTexture(block, "_front"));
	}

	public static TextureMapping fletchingTable(Block block, Block block2) {
		return new TextureMapping()
			.put(TextureSlot.PARTICLE, getBlockTexture(block, "_front"))
			.put(TextureSlot.DOWN, getBlockTexture(block2))
			.put(TextureSlot.UP, getBlockTexture(block, "_top"))
			.put(TextureSlot.NORTH, getBlockTexture(block, "_front"))
			.put(TextureSlot.SOUTH, getBlockTexture(block, "_front"))
			.put(TextureSlot.EAST, getBlockTexture(block, "_side"))
			.put(TextureSlot.WEST, getBlockTexture(block, "_side"));
	}

	public static TextureMapping campfire(Block block) {
		return new TextureMapping().put(TextureSlot.LIT_LOG, getBlockTexture(block, "_log_lit")).put(TextureSlot.FIRE, getBlockTexture(block, "_fire"));
	}

	public static TextureMapping candleCake(Block block, boolean bl) {
		return new TextureMapping()
			.put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAKE, "_side"))
			.put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAKE, "_bottom"))
			.put(TextureSlot.TOP, getBlockTexture(Blocks.CAKE, "_top"))
			.put(TextureSlot.SIDE, getBlockTexture(Blocks.CAKE, "_side"))
			.put(TextureSlot.CANDLE, getBlockTexture(block, bl ? "_lit" : ""));
	}

	public static TextureMapping cauldron(ResourceLocation resourceLocation) {
		return new TextureMapping()
			.put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAULDRON, "_side"))
			.put(TextureSlot.SIDE, getBlockTexture(Blocks.CAULDRON, "_side"))
			.put(TextureSlot.TOP, getBlockTexture(Blocks.CAULDRON, "_top"))
			.put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAULDRON, "_bottom"))
			.put(TextureSlot.INSIDE, getBlockTexture(Blocks.CAULDRON, "_inner"))
			.put(TextureSlot.CONTENT, resourceLocation);
	}

	public static TextureMapping layer0(Item item) {
		return new TextureMapping().put(TextureSlot.LAYER0, getItemTexture(item));
	}

	public static TextureMapping layer0(Block block) {
		return new TextureMapping().put(TextureSlot.LAYER0, getBlockTexture(block));
	}

	public static TextureMapping layer0(ResourceLocation resourceLocation) {
		return new TextureMapping().put(TextureSlot.LAYER0, resourceLocation);
	}

	public static ResourceLocation getBlockTexture(Block block) {
		ResourceLocation resourceLocation = Registry.BLOCK.getKey(block);
		return new ResourceLocation(resourceLocation.getNamespace(), "block/" + resourceLocation.getPath());
	}

	public static ResourceLocation getBlockTexture(Block block, String string) {
		ResourceLocation resourceLocation = Registry.BLOCK.getKey(block);
		return new ResourceLocation(resourceLocation.getNamespace(), "block/" + resourceLocation.getPath() + string);
	}

	public static ResourceLocation getItemTexture(Item item) {
		ResourceLocation resourceLocation = Registry.ITEM.getKey(item);
		return new ResourceLocation(resourceLocation.getNamespace(), "item/" + resourceLocation.getPath());
	}

	public static ResourceLocation getItemTexture(Item item, String string) {
		ResourceLocation resourceLocation = Registry.ITEM.getKey(item);
		return new ResourceLocation(resourceLocation.getNamespace(), "item/" + resourceLocation.getPath() + string);
	}
}
