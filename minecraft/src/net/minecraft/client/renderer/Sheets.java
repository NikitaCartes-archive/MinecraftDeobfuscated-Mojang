package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.WoodType;

@Environment(EnvType.CLIENT)
public class Sheets {
	public static final ResourceLocation SHULKER_SHEET = new ResourceLocation("textures/atlas/shulker_boxes.png");
	public static final ResourceLocation BED_SHEET = new ResourceLocation("textures/atlas/beds.png");
	public static final ResourceLocation BANNER_SHEET = new ResourceLocation("textures/atlas/banner_patterns.png");
	public static final ResourceLocation SHIELD_SHEET = new ResourceLocation("textures/atlas/shield_patterns.png");
	public static final ResourceLocation SIGN_SHEET = new ResourceLocation("textures/atlas/signs.png");
	public static final ResourceLocation CHEST_SHEET = new ResourceLocation("textures/atlas/chest.png");
	private static final RenderType SHULKER_BOX_SHEET_TYPE = RenderType.entityCutoutNoCull(SHULKER_SHEET);
	private static final RenderType BED_SHEET_TYPE = RenderType.entitySolid(BED_SHEET);
	private static final RenderType BANNER_SHEET_TYPE = RenderType.entityNoOutline(BANNER_SHEET);
	private static final RenderType SHIELD_SHEET_TYPE = RenderType.entityNoOutline(SHIELD_SHEET);
	private static final RenderType SIGN_SHEET_TYPE = RenderType.entityCutoutNoCull(SIGN_SHEET);
	private static final RenderType CHEST_SHEET_TYPE = RenderType.entityCutout(CHEST_SHEET);
	private static final RenderType SOLID_BLOCK_SHEET = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
	private static final RenderType CUTOUT_BLOCK_SHEET = RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
	private static final RenderType TRANSLUCENT_ITEM_CULL_BLOCK_SHEET = RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
	private static final RenderType TRANSLUCENT_CULL_BLOCK_SHEET = RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
	public static final Material DEFAULT_SHULKER_TEXTURE_LOCATION = new Material(SHULKER_SHEET, new ResourceLocation("entity/shulker/shulker"));
	public static final List<Material> SHULKER_TEXTURE_LOCATION = (List<Material>)Stream.of(
			"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
		)
		.map(string -> new Material(SHULKER_SHEET, new ResourceLocation("entity/shulker/shulker_" + string)))
		.collect(ImmutableList.toImmutableList());
	public static final Map<WoodType, Material> SIGN_MATERIALS = (Map<WoodType, Material>)WoodType.values()
		.collect(Collectors.toMap(Function.identity(), Sheets::createSignMaterial));
	public static final Map<BannerPattern, Material> BANNER_MATERIALS = (Map<BannerPattern, Material>)Arrays.stream(BannerPattern.values())
		.collect(Collectors.toMap(Function.identity(), Sheets::createBannerMaterial));
	public static final Map<BannerPattern, Material> SHIELD_MATERIALS = (Map<BannerPattern, Material>)Arrays.stream(BannerPattern.values())
		.collect(Collectors.toMap(Function.identity(), Sheets::createShieldMaterial));
	public static final Material[] BED_TEXTURES = (Material[])Arrays.stream(DyeColor.values())
		.sorted(Comparator.comparingInt(DyeColor::getId))
		.map(dyeColor -> new Material(BED_SHEET, new ResourceLocation("entity/bed/" + dyeColor.getName())))
		.toArray(Material[]::new);
	public static final Material CHEST_TRAP_LOCATION = chestMaterial("trapped");
	public static final Material CHEST_TRAP_LOCATION_LEFT = chestMaterial("trapped_left");
	public static final Material CHEST_TRAP_LOCATION_RIGHT = chestMaterial("trapped_right");
	public static final Material CHEST_XMAS_LOCATION = chestMaterial("christmas");
	public static final Material CHEST_XMAS_LOCATION_LEFT = chestMaterial("christmas_left");
	public static final Material CHEST_XMAS_LOCATION_RIGHT = chestMaterial("christmas_right");
	public static final Material CHEST_LOCATION = chestMaterial("normal");
	public static final Material CHEST_LOCATION_LEFT = chestMaterial("normal_left");
	public static final Material CHEST_LOCATION_RIGHT = chestMaterial("normal_right");
	public static final Material ENDER_CHEST_LOCATION = chestMaterial("ender");

	public static RenderType bannerSheet() {
		return BANNER_SHEET_TYPE;
	}

	public static RenderType shieldSheet() {
		return SHIELD_SHEET_TYPE;
	}

	public static RenderType bedSheet() {
		return BED_SHEET_TYPE;
	}

	public static RenderType shulkerBoxSheet() {
		return SHULKER_BOX_SHEET_TYPE;
	}

	public static RenderType signSheet() {
		return SIGN_SHEET_TYPE;
	}

	public static RenderType chestSheet() {
		return CHEST_SHEET_TYPE;
	}

	public static RenderType solidBlockSheet() {
		return SOLID_BLOCK_SHEET;
	}

	public static RenderType cutoutBlockSheet() {
		return CUTOUT_BLOCK_SHEET;
	}

	public static RenderType translucentItemSheet() {
		return TRANSLUCENT_ITEM_CULL_BLOCK_SHEET;
	}

	public static RenderType translucentCullBlockSheet() {
		return TRANSLUCENT_CULL_BLOCK_SHEET;
	}

	public static void getAllMaterials(Consumer<Material> consumer) {
		consumer.accept(DEFAULT_SHULKER_TEXTURE_LOCATION);
		SHULKER_TEXTURE_LOCATION.forEach(consumer);
		BANNER_MATERIALS.values().forEach(consumer);
		SHIELD_MATERIALS.values().forEach(consumer);
		SIGN_MATERIALS.values().forEach(consumer);

		for (Material material : BED_TEXTURES) {
			consumer.accept(material);
		}

		consumer.accept(CHEST_TRAP_LOCATION);
		consumer.accept(CHEST_TRAP_LOCATION_LEFT);
		consumer.accept(CHEST_TRAP_LOCATION_RIGHT);
		consumer.accept(CHEST_XMAS_LOCATION);
		consumer.accept(CHEST_XMAS_LOCATION_LEFT);
		consumer.accept(CHEST_XMAS_LOCATION_RIGHT);
		consumer.accept(CHEST_LOCATION);
		consumer.accept(CHEST_LOCATION_LEFT);
		consumer.accept(CHEST_LOCATION_RIGHT);
		consumer.accept(ENDER_CHEST_LOCATION);
	}

	private static Material createSignMaterial(WoodType woodType) {
		return new Material(SIGN_SHEET, new ResourceLocation("entity/signs/" + woodType.name()));
	}

	public static Material getSignMaterial(WoodType woodType) {
		return (Material)SIGN_MATERIALS.get(woodType);
	}

	private static Material createBannerMaterial(BannerPattern bannerPattern) {
		return new Material(BANNER_SHEET, bannerPattern.location(true));
	}

	public static Material getBannerMaterial(BannerPattern bannerPattern) {
		return (Material)BANNER_MATERIALS.get(bannerPattern);
	}

	private static Material createShieldMaterial(BannerPattern bannerPattern) {
		return new Material(SHIELD_SHEET, bannerPattern.location(false));
	}

	public static Material getShieldMaterial(BannerPattern bannerPattern) {
		return (Material)SHIELD_MATERIALS.get(bannerPattern);
	}

	private static Material chestMaterial(String string) {
		return new Material(CHEST_SHEET, new ResourceLocation("entity/chest/" + string));
	}

	public static Material chooseMaterial(BlockEntity blockEntity, ChestType chestType, boolean bl) {
		if (bl) {
			return chooseMaterial(chestType, CHEST_XMAS_LOCATION, CHEST_XMAS_LOCATION_LEFT, CHEST_XMAS_LOCATION_RIGHT);
		} else if (blockEntity instanceof TrappedChestBlockEntity) {
			return chooseMaterial(chestType, CHEST_TRAP_LOCATION, CHEST_TRAP_LOCATION_LEFT, CHEST_TRAP_LOCATION_RIGHT);
		} else {
			return blockEntity instanceof EnderChestBlockEntity
				? ENDER_CHEST_LOCATION
				: chooseMaterial(chestType, CHEST_LOCATION, CHEST_LOCATION_LEFT, CHEST_LOCATION_RIGHT);
		}
	}

	private static Material chooseMaterial(ChestType chestType, Material material, Material material2, Material material3) {
		switch (chestType) {
			case LEFT:
				return material2;
			case RIGHT:
				return material3;
			case SINGLE:
			default:
				return material;
		}
	}
}
