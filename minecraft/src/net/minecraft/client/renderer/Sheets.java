package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.WoodType;

@Environment(EnvType.CLIENT)
public class Sheets {
	public static final ResourceLocation SHULKER_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/shulker_boxes.png");
	public static final ResourceLocation BED_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/beds.png");
	public static final ResourceLocation BANNER_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/banner_patterns.png");
	public static final ResourceLocation SHIELD_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/shield_patterns.png");
	public static final ResourceLocation SIGN_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/signs.png");
	public static final ResourceLocation CHEST_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/chest.png");
	public static final ResourceLocation ARMOR_TRIMS_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/armor_trims.png");
	public static final ResourceLocation DECORATED_POT_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/decorated_pot.png");
	private static final RenderType SHULKER_BOX_SHEET_TYPE = RenderType.entityCutoutNoCull(SHULKER_SHEET);
	private static final RenderType BED_SHEET_TYPE = RenderType.entitySolid(BED_SHEET);
	private static final RenderType BANNER_SHEET_TYPE = RenderType.entityNoOutline(BANNER_SHEET);
	private static final RenderType SHIELD_SHEET_TYPE = RenderType.entityNoOutline(SHIELD_SHEET);
	private static final RenderType SIGN_SHEET_TYPE = RenderType.entityCutoutNoCull(SIGN_SHEET);
	private static final RenderType CHEST_SHEET_TYPE = RenderType.entityCutout(CHEST_SHEET);
	private static final RenderType ARMOR_TRIMS_SHEET_TYPE = RenderType.armorCutoutNoCull(ARMOR_TRIMS_SHEET);
	private static final RenderType ARMOR_TRIMS_DECAL_SHEET_TYPE = RenderType.createArmorDecalCutoutNoCull(ARMOR_TRIMS_SHEET);
	private static final RenderType SOLID_BLOCK_SHEET = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
	private static final RenderType CUTOUT_BLOCK_SHEET = RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
	private static final RenderType TRANSLUCENT_ITEM_CULL_BLOCK_SHEET = RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
	public static final Material DEFAULT_SHULKER_TEXTURE_LOCATION = new Material(SHULKER_SHEET, ResourceLocation.withDefaultNamespace("entity/shulker/shulker"));
	public static final List<Material> SHULKER_TEXTURE_LOCATION = (List<Material>)Stream.of(
			"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
		)
		.map(string -> new Material(SHULKER_SHEET, ResourceLocation.withDefaultNamespace("entity/shulker/shulker_" + string)))
		.collect(ImmutableList.toImmutableList());
	public static final Map<WoodType, Material> SIGN_MATERIALS = (Map<WoodType, Material>)WoodType.values()
		.collect(Collectors.toMap(Function.identity(), Sheets::createSignMaterial));
	public static final Map<WoodType, Material> HANGING_SIGN_MATERIALS = (Map<WoodType, Material>)WoodType.values()
		.collect(Collectors.toMap(Function.identity(), Sheets::createHangingSignMaterial));
	public static final Material BANNER_BASE = new Material(BANNER_SHEET, ResourceLocation.withDefaultNamespace("entity/banner/base"));
	public static final Material SHIELD_BASE = new Material(SHIELD_SHEET, ResourceLocation.withDefaultNamespace("entity/shield/base"));
	private static final Map<ResourceLocation, Material> BANNER_MATERIALS = new HashMap();
	private static final Map<ResourceLocation, Material> SHIELD_MATERIALS = new HashMap();
	public static final Map<ResourceKey<DecoratedPotPattern>, Material> DECORATED_POT_MATERIALS = (Map<ResourceKey<DecoratedPotPattern>, Material>)BuiltInRegistries.DECORATED_POT_PATTERN
		.holders()
		.collect(Collectors.toMap(Holder.Reference::key, reference -> createDecoratedPotMaterial(((DecoratedPotPattern)reference.value()).assetId())));
	public static final Material DECORATED_POT_BASE = createDecoratedPotMaterial(ResourceLocation.withDefaultNamespace("decorated_pot_base"));
	public static final Material DECORATED_POT_SIDE = createDecoratedPotMaterial(ResourceLocation.withDefaultNamespace("decorated_pot_side"));
	public static final Material[] BED_TEXTURES = (Material[])Arrays.stream(DyeColor.values())
		.sorted(Comparator.comparingInt(DyeColor::getId))
		.map(dyeColor -> new Material(BED_SHEET, ResourceLocation.withDefaultNamespace("entity/bed/" + dyeColor.getName())))
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

	public static RenderType hangingSignSheet() {
		return SIGN_SHEET_TYPE;
	}

	public static RenderType chestSheet() {
		return CHEST_SHEET_TYPE;
	}

	public static RenderType armorTrimsSheet(boolean bl) {
		return bl ? ARMOR_TRIMS_DECAL_SHEET_TYPE : ARMOR_TRIMS_SHEET_TYPE;
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

	private static Material createSignMaterial(WoodType woodType) {
		return new Material(SIGN_SHEET, ResourceLocation.withDefaultNamespace("entity/signs/" + woodType.name()));
	}

	private static Material createHangingSignMaterial(WoodType woodType) {
		return new Material(SIGN_SHEET, ResourceLocation.withDefaultNamespace("entity/signs/hanging/" + woodType.name()));
	}

	public static Material getSignMaterial(WoodType woodType) {
		return (Material)SIGN_MATERIALS.get(woodType);
	}

	public static Material getHangingSignMaterial(WoodType woodType) {
		return (Material)HANGING_SIGN_MATERIALS.get(woodType);
	}

	public static Material getBannerMaterial(Holder<BannerPattern> holder) {
		return (Material)BANNER_MATERIALS.computeIfAbsent(holder.value().assetId(), resourceLocation -> {
			ResourceLocation resourceLocation2 = resourceLocation.withPrefix("entity/banner/");
			return new Material(BANNER_SHEET, resourceLocation2);
		});
	}

	public static Material getShieldMaterial(Holder<BannerPattern> holder) {
		return (Material)SHIELD_MATERIALS.computeIfAbsent(holder.value().assetId(), resourceLocation -> {
			ResourceLocation resourceLocation2 = resourceLocation.withPrefix("entity/shield/");
			return new Material(SHIELD_SHEET, resourceLocation2);
		});
	}

	private static Material chestMaterial(String string) {
		return new Material(CHEST_SHEET, ResourceLocation.withDefaultNamespace("entity/chest/" + string));
	}

	private static Material createDecoratedPotMaterial(ResourceLocation resourceLocation) {
		return new Material(DECORATED_POT_SHEET, resourceLocation.withPrefix("entity/decorated_pot/"));
	}

	@Nullable
	public static Material getDecoratedPotMaterial(@Nullable ResourceKey<DecoratedPotPattern> resourceKey) {
		return resourceKey == null ? null : (Material)DECORATED_POT_MATERIALS.get(resourceKey);
	}

	public static Material chooseMaterial(BlockEntity blockEntity, ChestType chestType, boolean bl) {
		if (blockEntity instanceof EnderChestBlockEntity) {
			return ENDER_CHEST_LOCATION;
		} else if (bl) {
			return chooseMaterial(chestType, CHEST_XMAS_LOCATION, CHEST_XMAS_LOCATION_LEFT, CHEST_XMAS_LOCATION_RIGHT);
		} else {
			return blockEntity instanceof TrappedChestBlockEntity
				? chooseMaterial(chestType, CHEST_TRAP_LOCATION, CHEST_TRAP_LOCATION_LEFT, CHEST_TRAP_LOCATION_RIGHT)
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
