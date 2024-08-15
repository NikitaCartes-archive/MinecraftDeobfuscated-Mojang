package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class BlockEntityType<T extends BlockEntity> {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final BlockEntityType<FurnaceBlockEntity> FURNACE = register("furnace", FurnaceBlockEntity::new, Blocks.FURNACE);
	public static final BlockEntityType<ChestBlockEntity> CHEST = register("chest", ChestBlockEntity::new, Blocks.CHEST);
	public static final BlockEntityType<TrappedChestBlockEntity> TRAPPED_CHEST = register("trapped_chest", TrappedChestBlockEntity::new, Blocks.TRAPPED_CHEST);
	public static final BlockEntityType<EnderChestBlockEntity> ENDER_CHEST = register("ender_chest", EnderChestBlockEntity::new, Blocks.ENDER_CHEST);
	public static final BlockEntityType<JukeboxBlockEntity> JUKEBOX = register("jukebox", JukeboxBlockEntity::new, Blocks.JUKEBOX);
	public static final BlockEntityType<DispenserBlockEntity> DISPENSER = register("dispenser", DispenserBlockEntity::new, Blocks.DISPENSER);
	public static final BlockEntityType<DropperBlockEntity> DROPPER = register("dropper", DropperBlockEntity::new, Blocks.DROPPER);
	public static final BlockEntityType<SignBlockEntity> SIGN = register(
		"sign",
		SignBlockEntity::new,
		Blocks.OAK_SIGN,
		Blocks.SPRUCE_SIGN,
		Blocks.BIRCH_SIGN,
		Blocks.ACACIA_SIGN,
		Blocks.CHERRY_SIGN,
		Blocks.JUNGLE_SIGN,
		Blocks.DARK_OAK_SIGN,
		Blocks.OAK_WALL_SIGN,
		Blocks.SPRUCE_WALL_SIGN,
		Blocks.BIRCH_WALL_SIGN,
		Blocks.ACACIA_WALL_SIGN,
		Blocks.CHERRY_WALL_SIGN,
		Blocks.JUNGLE_WALL_SIGN,
		Blocks.DARK_OAK_WALL_SIGN,
		Blocks.CRIMSON_SIGN,
		Blocks.CRIMSON_WALL_SIGN,
		Blocks.WARPED_SIGN,
		Blocks.WARPED_WALL_SIGN,
		Blocks.MANGROVE_SIGN,
		Blocks.MANGROVE_WALL_SIGN,
		Blocks.BAMBOO_SIGN,
		Blocks.BAMBOO_WALL_SIGN
	);
	public static final BlockEntityType<HangingSignBlockEntity> HANGING_SIGN = register(
		"hanging_sign",
		HangingSignBlockEntity::new,
		Blocks.OAK_HANGING_SIGN,
		Blocks.SPRUCE_HANGING_SIGN,
		Blocks.BIRCH_HANGING_SIGN,
		Blocks.ACACIA_HANGING_SIGN,
		Blocks.CHERRY_HANGING_SIGN,
		Blocks.JUNGLE_HANGING_SIGN,
		Blocks.DARK_OAK_HANGING_SIGN,
		Blocks.CRIMSON_HANGING_SIGN,
		Blocks.WARPED_HANGING_SIGN,
		Blocks.MANGROVE_HANGING_SIGN,
		Blocks.BAMBOO_HANGING_SIGN,
		Blocks.OAK_WALL_HANGING_SIGN,
		Blocks.SPRUCE_WALL_HANGING_SIGN,
		Blocks.BIRCH_WALL_HANGING_SIGN,
		Blocks.ACACIA_WALL_HANGING_SIGN,
		Blocks.CHERRY_WALL_HANGING_SIGN,
		Blocks.JUNGLE_WALL_HANGING_SIGN,
		Blocks.DARK_OAK_WALL_HANGING_SIGN,
		Blocks.CRIMSON_WALL_HANGING_SIGN,
		Blocks.WARPED_WALL_HANGING_SIGN,
		Blocks.MANGROVE_WALL_HANGING_SIGN,
		Blocks.BAMBOO_WALL_HANGING_SIGN
	);
	public static final BlockEntityType<SpawnerBlockEntity> MOB_SPAWNER = register("mob_spawner", SpawnerBlockEntity::new, Blocks.SPAWNER);
	public static final BlockEntityType<PistonMovingBlockEntity> PISTON = register("piston", PistonMovingBlockEntity::new, Blocks.MOVING_PISTON);
	public static final BlockEntityType<BrewingStandBlockEntity> BREWING_STAND = register("brewing_stand", BrewingStandBlockEntity::new, Blocks.BREWING_STAND);
	public static final BlockEntityType<EnchantingTableBlockEntity> ENCHANTING_TABLE = register(
		"enchanting_table", EnchantingTableBlockEntity::new, Blocks.ENCHANTING_TABLE
	);
	public static final BlockEntityType<TheEndPortalBlockEntity> END_PORTAL = register("end_portal", TheEndPortalBlockEntity::new, Blocks.END_PORTAL);
	public static final BlockEntityType<BeaconBlockEntity> BEACON = register("beacon", BeaconBlockEntity::new, Blocks.BEACON);
	public static final BlockEntityType<SkullBlockEntity> SKULL = register(
		"skull",
		SkullBlockEntity::new,
		Blocks.SKELETON_SKULL,
		Blocks.SKELETON_WALL_SKULL,
		Blocks.CREEPER_HEAD,
		Blocks.CREEPER_WALL_HEAD,
		Blocks.DRAGON_HEAD,
		Blocks.DRAGON_WALL_HEAD,
		Blocks.ZOMBIE_HEAD,
		Blocks.ZOMBIE_WALL_HEAD,
		Blocks.WITHER_SKELETON_SKULL,
		Blocks.WITHER_SKELETON_WALL_SKULL,
		Blocks.PLAYER_HEAD,
		Blocks.PLAYER_WALL_HEAD,
		Blocks.PIGLIN_HEAD,
		Blocks.PIGLIN_WALL_HEAD
	);
	public static final BlockEntityType<DaylightDetectorBlockEntity> DAYLIGHT_DETECTOR = register(
		"daylight_detector", DaylightDetectorBlockEntity::new, Blocks.DAYLIGHT_DETECTOR
	);
	public static final BlockEntityType<HopperBlockEntity> HOPPER = register("hopper", HopperBlockEntity::new, Blocks.HOPPER);
	public static final BlockEntityType<ComparatorBlockEntity> COMPARATOR = register("comparator", ComparatorBlockEntity::new, Blocks.COMPARATOR);
	public static final BlockEntityType<BannerBlockEntity> BANNER = register(
		"banner",
		BannerBlockEntity::new,
		Blocks.WHITE_BANNER,
		Blocks.ORANGE_BANNER,
		Blocks.MAGENTA_BANNER,
		Blocks.LIGHT_BLUE_BANNER,
		Blocks.YELLOW_BANNER,
		Blocks.LIME_BANNER,
		Blocks.PINK_BANNER,
		Blocks.GRAY_BANNER,
		Blocks.LIGHT_GRAY_BANNER,
		Blocks.CYAN_BANNER,
		Blocks.PURPLE_BANNER,
		Blocks.BLUE_BANNER,
		Blocks.BROWN_BANNER,
		Blocks.GREEN_BANNER,
		Blocks.RED_BANNER,
		Blocks.BLACK_BANNER,
		Blocks.WHITE_WALL_BANNER,
		Blocks.ORANGE_WALL_BANNER,
		Blocks.MAGENTA_WALL_BANNER,
		Blocks.LIGHT_BLUE_WALL_BANNER,
		Blocks.YELLOW_WALL_BANNER,
		Blocks.LIME_WALL_BANNER,
		Blocks.PINK_WALL_BANNER,
		Blocks.GRAY_WALL_BANNER,
		Blocks.LIGHT_GRAY_WALL_BANNER,
		Blocks.CYAN_WALL_BANNER,
		Blocks.PURPLE_WALL_BANNER,
		Blocks.BLUE_WALL_BANNER,
		Blocks.BROWN_WALL_BANNER,
		Blocks.GREEN_WALL_BANNER,
		Blocks.RED_WALL_BANNER,
		Blocks.BLACK_WALL_BANNER
	);
	public static final BlockEntityType<StructureBlockEntity> STRUCTURE_BLOCK = register("structure_block", StructureBlockEntity::new, Blocks.STRUCTURE_BLOCK);
	public static final BlockEntityType<TheEndGatewayBlockEntity> END_GATEWAY = register("end_gateway", TheEndGatewayBlockEntity::new, Blocks.END_GATEWAY);
	public static final BlockEntityType<CommandBlockEntity> COMMAND_BLOCK = register(
		"command_block", CommandBlockEntity::new, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK
	);
	public static final BlockEntityType<ShulkerBoxBlockEntity> SHULKER_BOX = register(
		"shulker_box",
		ShulkerBoxBlockEntity::new,
		Blocks.SHULKER_BOX,
		Blocks.BLACK_SHULKER_BOX,
		Blocks.BLUE_SHULKER_BOX,
		Blocks.BROWN_SHULKER_BOX,
		Blocks.CYAN_SHULKER_BOX,
		Blocks.GRAY_SHULKER_BOX,
		Blocks.GREEN_SHULKER_BOX,
		Blocks.LIGHT_BLUE_SHULKER_BOX,
		Blocks.LIGHT_GRAY_SHULKER_BOX,
		Blocks.LIME_SHULKER_BOX,
		Blocks.MAGENTA_SHULKER_BOX,
		Blocks.ORANGE_SHULKER_BOX,
		Blocks.PINK_SHULKER_BOX,
		Blocks.PURPLE_SHULKER_BOX,
		Blocks.RED_SHULKER_BOX,
		Blocks.WHITE_SHULKER_BOX,
		Blocks.YELLOW_SHULKER_BOX
	);
	public static final BlockEntityType<BedBlockEntity> BED = register(
		"bed",
		BedBlockEntity::new,
		Blocks.RED_BED,
		Blocks.BLACK_BED,
		Blocks.BLUE_BED,
		Blocks.BROWN_BED,
		Blocks.CYAN_BED,
		Blocks.GRAY_BED,
		Blocks.GREEN_BED,
		Blocks.LIGHT_BLUE_BED,
		Blocks.LIGHT_GRAY_BED,
		Blocks.LIME_BED,
		Blocks.MAGENTA_BED,
		Blocks.ORANGE_BED,
		Blocks.PINK_BED,
		Blocks.PURPLE_BED,
		Blocks.WHITE_BED,
		Blocks.YELLOW_BED
	);
	public static final BlockEntityType<ConduitBlockEntity> CONDUIT = register("conduit", ConduitBlockEntity::new, Blocks.CONDUIT);
	public static final BlockEntityType<BarrelBlockEntity> BARREL = register("barrel", BarrelBlockEntity::new, Blocks.BARREL);
	public static final BlockEntityType<SmokerBlockEntity> SMOKER = register("smoker", SmokerBlockEntity::new, Blocks.SMOKER);
	public static final BlockEntityType<BlastFurnaceBlockEntity> BLAST_FURNACE = register("blast_furnace", BlastFurnaceBlockEntity::new, Blocks.BLAST_FURNACE);
	public static final BlockEntityType<LecternBlockEntity> LECTERN = register("lectern", LecternBlockEntity::new, Blocks.LECTERN);
	public static final BlockEntityType<BellBlockEntity> BELL = register("bell", BellBlockEntity::new, Blocks.BELL);
	public static final BlockEntityType<JigsawBlockEntity> JIGSAW = register("jigsaw", JigsawBlockEntity::new, Blocks.JIGSAW);
	public static final BlockEntityType<CampfireBlockEntity> CAMPFIRE = register("campfire", CampfireBlockEntity::new, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
	public static final BlockEntityType<BeehiveBlockEntity> BEEHIVE = register("beehive", BeehiveBlockEntity::new, Blocks.BEE_NEST, Blocks.BEEHIVE);
	public static final BlockEntityType<SculkSensorBlockEntity> SCULK_SENSOR = register("sculk_sensor", SculkSensorBlockEntity::new, Blocks.SCULK_SENSOR);
	public static final BlockEntityType<CalibratedSculkSensorBlockEntity> CALIBRATED_SCULK_SENSOR = register(
		"calibrated_sculk_sensor", CalibratedSculkSensorBlockEntity::new, Blocks.CALIBRATED_SCULK_SENSOR
	);
	public static final BlockEntityType<SculkCatalystBlockEntity> SCULK_CATALYST = register("sculk_catalyst", SculkCatalystBlockEntity::new, Blocks.SCULK_CATALYST);
	public static final BlockEntityType<SculkShriekerBlockEntity> SCULK_SHRIEKER = register("sculk_shrieker", SculkShriekerBlockEntity::new, Blocks.SCULK_SHRIEKER);
	public static final BlockEntityType<ChiseledBookShelfBlockEntity> CHISELED_BOOKSHELF = register(
		"chiseled_bookshelf", ChiseledBookShelfBlockEntity::new, Blocks.CHISELED_BOOKSHELF
	);
	public static final BlockEntityType<BrushableBlockEntity> BRUSHABLE_BLOCK = register(
		"brushable_block", BrushableBlockEntity::new, Blocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_GRAVEL
	);
	public static final BlockEntityType<DecoratedPotBlockEntity> DECORATED_POT = register("decorated_pot", DecoratedPotBlockEntity::new, Blocks.DECORATED_POT);
	public static final BlockEntityType<CrafterBlockEntity> CRAFTER = register("crafter", CrafterBlockEntity::new, Blocks.CRAFTER);
	public static final BlockEntityType<TrialSpawnerBlockEntity> TRIAL_SPAWNER = register("trial_spawner", TrialSpawnerBlockEntity::new, Blocks.TRIAL_SPAWNER);
	public static final BlockEntityType<VaultBlockEntity> VAULT = register("vault", VaultBlockEntity::new, Blocks.VAULT);
	private final BlockEntityType.BlockEntitySupplier<? extends T> factory;
	private final Set<Block> validBlocks;
	private final Holder.Reference<BlockEntityType<?>> builtInRegistryHolder = BuiltInRegistries.BLOCK_ENTITY_TYPE.createIntrusiveHolder(this);

	@Nullable
	public static ResourceLocation getKey(BlockEntityType<?> blockEntityType) {
		return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType);
	}

	private static <T extends BlockEntity> BlockEntityType<T> register(
		String string, BlockEntityType.BlockEntitySupplier<? extends T> blockEntitySupplier, Block... blocks
	) {
		if (blocks.length == 0) {
			LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", string);
		}

		Util.fetchChoiceType(References.BLOCK_ENTITY, string);
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, string, new BlockEntityType<>(blockEntitySupplier, Set.of(blocks)));
	}

	private BlockEntityType(BlockEntityType.BlockEntitySupplier<? extends T> blockEntitySupplier, Set<Block> set) {
		this.factory = blockEntitySupplier;
		this.validBlocks = set;
	}

	@Nullable
	public T create(BlockPos blockPos, BlockState blockState) {
		return (T)this.factory.create(blockPos, blockState);
	}

	public boolean isValid(BlockState blockState) {
		return this.validBlocks.contains(blockState.getBlock());
	}

	@Deprecated
	public Holder.Reference<BlockEntityType<?>> builtInRegistryHolder() {
		return this.builtInRegistryHolder;
	}

	@Nullable
	public T getBlockEntity(BlockGetter blockGetter, BlockPos blockPos) {
		BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
		return (T)(blockEntity != null && blockEntity.getType() == this ? blockEntity : null);
	}

	@FunctionalInterface
	interface BlockEntitySupplier<T extends BlockEntity> {
		T create(BlockPos blockPos, BlockState blockState);
	}
}
