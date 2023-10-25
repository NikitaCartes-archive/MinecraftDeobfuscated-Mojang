package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;

public class BlockTypes {
	public static final MapCodec<Block> CODEC = BuiltInRegistries.BLOCK_TYPE.byNameCodec().dispatchMap(Block::codec, MapCodec::codec);

	public static MapCodec<? extends Block> bootstrap(Registry<MapCodec<? extends Block>> registry) {
		MapCodec<? extends Block> mapCodec = Registry.register(registry, "block", Block.CODEC);
		Registry.register(registry, "air", AirBlock.CODEC);
		Registry.register(registry, "amethyst", AmethystBlock.CODEC);
		Registry.register(registry, "amethyst_cluster", AmethystClusterBlock.CODEC);
		Registry.register(registry, "anvil", AnvilBlock.CODEC);
		Registry.register(registry, "attached_stem", AttachedStemBlock.CODEC);
		Registry.register(registry, "azalea", AzaleaBlock.CODEC);
		Registry.register(registry, "bamboo_sapling", BambooSaplingBlock.CODEC);
		Registry.register(registry, "bamboo_stalk", BambooStalkBlock.CODEC);
		Registry.register(registry, "banner", BannerBlock.CODEC);
		Registry.register(registry, "barrel", BarrelBlock.CODEC);
		Registry.register(registry, "barrier", BarrierBlock.CODEC);
		Registry.register(registry, "base_coral_fan", BaseCoralFanBlock.CODEC);
		Registry.register(registry, "base_coral_plant", BaseCoralPlantBlock.CODEC);
		Registry.register(registry, "base_coral_wall_fan", BaseCoralWallFanBlock.CODEC);
		Registry.register(registry, "beacon", BeaconBlock.CODEC);
		Registry.register(registry, "bed", BedBlock.CODEC);
		Registry.register(registry, "beehive", BeehiveBlock.CODEC);
		Registry.register(registry, "beetroot", BeetrootBlock.CODEC);
		Registry.register(registry, "bell", BellBlock.CODEC);
		Registry.register(registry, "big_dripleaf", BigDripleafBlock.CODEC);
		Registry.register(registry, "big_dripleaf_stem", BigDripleafStemBlock.CODEC);
		Registry.register(registry, "blast_furnace", BlastFurnaceBlock.CODEC);
		Registry.register(registry, "brewing_stand", BrewingStandBlock.CODEC);
		Registry.register(registry, "brushable", BrushableBlock.CODEC);
		Registry.register(registry, "bubble_column", BubbleColumnBlock.CODEC);
		Registry.register(registry, "budding_amethyst", BuddingAmethystBlock.CODEC);
		Registry.register(registry, "button", ButtonBlock.CODEC);
		Registry.register(registry, "cactus", CactusBlock.CODEC);
		Registry.register(registry, "cake", CakeBlock.CODEC);
		Registry.register(registry, "calibrated_sculk_sensor", CalibratedSculkSensorBlock.CODEC);
		Registry.register(registry, "campfire", CampfireBlock.CODEC);
		Registry.register(registry, "candle_cake", CandleCakeBlock.CODEC);
		Registry.register(registry, "candle", CandleBlock.CODEC);
		Registry.register(registry, "carpet", CarpetBlock.CODEC);
		Registry.register(registry, "carrot", CarrotBlock.CODEC);
		Registry.register(registry, "cartography_table", CartographyTableBlock.CODEC);
		Registry.register(registry, "carved_pumpkin", EquipableCarvedPumpkinBlock.CODEC);
		Registry.register(registry, "cauldron", CauldronBlock.CODEC);
		Registry.register(registry, "cave_vines", CaveVinesBlock.CODEC);
		Registry.register(registry, "cave_vines_plant", CaveVinesPlantBlock.CODEC);
		Registry.register(registry, "ceiling_hanging_sign", CeilingHangingSignBlock.CODEC);
		Registry.register(registry, "chain", ChainBlock.CODEC);
		Registry.register(registry, "cherry_leaves", CherryLeavesBlock.CODEC);
		Registry.register(registry, "chest", ChestBlock.CODEC);
		Registry.register(registry, "chiseled_book_shelf", ChiseledBookShelfBlock.CODEC);
		Registry.register(registry, "chorus_flower", ChorusFlowerBlock.CODEC);
		Registry.register(registry, "chorus_plant", ChorusPlantBlock.CODEC);
		Registry.register(registry, "cocoa", CocoaBlock.CODEC);
		Registry.register(registry, "colored_falling", ColoredFallingBlock.CODEC);
		Registry.register(registry, "command", CommandBlock.CODEC);
		Registry.register(registry, "comparator", ComparatorBlock.CODEC);
		Registry.register(registry, "composter", ComposterBlock.CODEC);
		Registry.register(registry, "concrete_powder", ConcretePowderBlock.CODEC);
		Registry.register(registry, "conduit", ConduitBlock.CODEC);
		Registry.register(registry, "copper_bulb_block", CopperBulbBlock.CODEC);
		Registry.register(registry, "coral", CoralBlock.CODEC);
		Registry.register(registry, "coral_fan", CoralFanBlock.CODEC);
		Registry.register(registry, "coral_plant", CoralPlantBlock.CODEC);
		Registry.register(registry, "coral_wall_fan", CoralWallFanBlock.CODEC);
		Registry.register(registry, "crafter", CrafterBlock.CODEC);
		Registry.register(registry, "crafting_table", CraftingTableBlock.CODEC);
		Registry.register(registry, "crop", CropBlock.CODEC);
		Registry.register(registry, "crying_obsidian", CryingObsidianBlock.CODEC);
		Registry.register(registry, "daylight_detector", DaylightDetectorBlock.CODEC);
		Registry.register(registry, "dead_bush", DeadBushBlock.CODEC);
		Registry.register(registry, "decorated_pot", DecoratedPotBlock.CODEC);
		Registry.register(registry, "detector_rail", DetectorRailBlock.CODEC);
		Registry.register(registry, "dirt_path", DirtPathBlock.CODEC);
		Registry.register(registry, "dispenser", DispenserBlock.CODEC);
		Registry.register(registry, "door", DoorBlock.CODEC);
		Registry.register(registry, "double_plant", DoublePlantBlock.CODEC);
		Registry.register(registry, "dragon_egg", DragonEggBlock.CODEC);
		Registry.register(registry, "drop_experience", DropExperienceBlock.CODEC);
		Registry.register(registry, "dropper", DropperBlock.CODEC);
		Registry.register(registry, "enchantment_table", EnchantmentTableBlock.CODEC);
		Registry.register(registry, "ender_chest", EnderChestBlock.CODEC);
		Registry.register(registry, "end_gateway", EndGatewayBlock.CODEC);
		Registry.register(registry, "end_portal", EndPortalBlock.CODEC);
		Registry.register(registry, "end_portal_frame", EndPortalFrameBlock.CODEC);
		Registry.register(registry, "end_rod", EndRodBlock.CODEC);
		Registry.register(registry, "farm", FarmBlock.CODEC);
		Registry.register(registry, "fence", FenceBlock.CODEC);
		Registry.register(registry, "fence_gate", FenceGateBlock.CODEC);
		Registry.register(registry, "fire", FireBlock.CODEC);
		Registry.register(registry, "fletching_table", FletchingTableBlock.CODEC);
		Registry.register(registry, "flower", FlowerBlock.CODEC);
		Registry.register(registry, "flower_pot", FlowerPotBlock.CODEC);
		Registry.register(registry, "frogspawn", FrogspawnBlock.CODEC);
		Registry.register(registry, "frosted_ice", FrostedIceBlock.CODEC);
		Registry.register(registry, "fungus", FungusBlock.CODEC);
		Registry.register(registry, "furnace", FurnaceBlock.CODEC);
		Registry.register(registry, "glazed_terracotta", GlazedTerracottaBlock.CODEC);
		Registry.register(registry, "glow_lichen", GlowLichenBlock.CODEC);
		Registry.register(registry, "grass", GrassBlock.CODEC);
		Registry.register(registry, "grindstone", GrindstoneBlock.CODEC);
		Registry.register(registry, "half_transparent", HalfTransparentBlock.CODEC);
		Registry.register(registry, "hanging_roots", HangingRootsBlock.CODEC);
		Registry.register(registry, "hay", HayBlock.CODEC);
		Registry.register(registry, "honey", HoneyBlock.CODEC);
		Registry.register(registry, "hopper", HopperBlock.CODEC);
		Registry.register(registry, "huge_mushroom", HugeMushroomBlock.CODEC);
		Registry.register(registry, "ice", IceBlock.CODEC);
		Registry.register(registry, "infested", InfestedBlock.CODEC);
		Registry.register(registry, "infested_rotated_pillar", InfestedRotatedPillarBlock.CODEC);
		Registry.register(registry, "iron_bars", IronBarsBlock.CODEC);
		Registry.register(registry, "jack_o_lantern", CarvedPumpkinBlock.CODEC);
		Registry.register(registry, "jigsaw", JigsawBlock.CODEC);
		Registry.register(registry, "jukebox", JukeboxBlock.CODEC);
		Registry.register(registry, "kelp", KelpBlock.CODEC);
		Registry.register(registry, "kelp_plant", KelpPlantBlock.CODEC);
		Registry.register(registry, "ladder", LadderBlock.CODEC);
		Registry.register(registry, "lantern", LanternBlock.CODEC);
		Registry.register(registry, "lava_cauldron", LavaCauldronBlock.CODEC);
		Registry.register(registry, "layered_cauldron", LayeredCauldronBlock.CODEC);
		Registry.register(registry, "leaves", LeavesBlock.CODEC);
		Registry.register(registry, "lectern", LecternBlock.CODEC);
		Registry.register(registry, "lever", LeverBlock.CODEC);
		Registry.register(registry, "light", LightBlock.CODEC);
		Registry.register(registry, "lightning_rod", LightningRodBlock.CODEC);
		Registry.register(registry, "liquid", LiquidBlock.CODEC);
		Registry.register(registry, "loom", LoomBlock.CODEC);
		Registry.register(registry, "magma", MagmaBlock.CODEC);
		Registry.register(registry, "mangrove_leaves", MangroveLeavesBlock.CODEC);
		Registry.register(registry, "mangrove_propagule", MangrovePropaguleBlock.CODEC);
		Registry.register(registry, "mangrove_roots", MangroveRootsBlock.CODEC);
		Registry.register(registry, "moss", MossBlock.CODEC);
		Registry.register(registry, "moving_piston", MovingPistonBlock.CODEC);
		Registry.register(registry, "mud", MudBlock.CODEC);
		Registry.register(registry, "mushroom", MushroomBlock.CODEC);
		Registry.register(registry, "mycelium", MyceliumBlock.CODEC);
		Registry.register(registry, "nether_portal", NetherPortalBlock.CODEC);
		Registry.register(registry, "netherrack", NetherrackBlock.CODEC);
		Registry.register(registry, "nether_sprouts", NetherSproutsBlock.CODEC);
		Registry.register(registry, "nether_wart", NetherWartBlock.CODEC);
		Registry.register(registry, "note", NoteBlock.CODEC);
		Registry.register(registry, "nylium", NyliumBlock.CODEC);
		Registry.register(registry, "observer", ObserverBlock.CODEC);
		Registry.register(registry, "piglinwallskull", PiglinWallSkullBlock.CODEC);
		Registry.register(registry, "pink_petals", PinkPetalsBlock.CODEC);
		Registry.register(registry, "piston_base", PistonBaseBlock.CODEC);
		Registry.register(registry, "piston_head", PistonHeadBlock.CODEC);
		Registry.register(registry, "pitcher_crop", PitcherCropBlock.CODEC);
		Registry.register(registry, "player_head", PlayerHeadBlock.CODEC);
		Registry.register(registry, "player_wall_head", PlayerWallHeadBlock.CODEC);
		Registry.register(registry, "pointed_dripstone", PointedDripstoneBlock.CODEC);
		Registry.register(registry, "potato", PotatoBlock.CODEC);
		Registry.register(registry, "powder_snow", PowderSnowBlock.CODEC);
		Registry.register(registry, "powered", PoweredBlock.CODEC);
		Registry.register(registry, "powered_rail", PoweredRailBlock.CODEC);
		Registry.register(registry, "pressure_plate", PressurePlateBlock.CODEC);
		Registry.register(registry, "pumpkin", PumpkinBlock.CODEC);
		Registry.register(registry, "rail", RailBlock.CODEC);
		Registry.register(registry, "redstone_lamp", RedstoneLampBlock.CODEC);
		Registry.register(registry, "redstone_ore", RedStoneOreBlock.CODEC);
		Registry.register(registry, "redstone_torch", RedstoneTorchBlock.CODEC);
		Registry.register(registry, "redstone_wall_torch", RedstoneWallTorchBlock.CODEC);
		Registry.register(registry, "redstone_wire", RedStoneWireBlock.CODEC);
		Registry.register(registry, "repeater", RepeaterBlock.CODEC);
		Registry.register(registry, "respawn_anchor", RespawnAnchorBlock.CODEC);
		Registry.register(registry, "rooted_dirt", RootedDirtBlock.CODEC);
		Registry.register(registry, "roots", RootsBlock.CODEC);
		Registry.register(registry, "rotated_pillar", RotatedPillarBlock.CODEC);
		Registry.register(registry, "sapling", SaplingBlock.CODEC);
		Registry.register(registry, "scaffolding", ScaffoldingBlock.CODEC);
		Registry.register(registry, "sculk_catalyst", SculkCatalystBlock.CODEC);
		Registry.register(registry, "sculk", SculkBlock.CODEC);
		Registry.register(registry, "sculk_sensor", SculkSensorBlock.CODEC);
		Registry.register(registry, "sculk_shrieker", SculkShriekerBlock.CODEC);
		Registry.register(registry, "sculk_vein", SculkVeinBlock.CODEC);
		Registry.register(registry, "seagrass", SeagrassBlock.CODEC);
		Registry.register(registry, "sea_pickle", SeaPickleBlock.CODEC);
		Registry.register(registry, "shulker_box", ShulkerBoxBlock.CODEC);
		Registry.register(registry, "skull", SkullBlock.CODEC);
		Registry.register(registry, "slab", SlabBlock.CODEC);
		Registry.register(registry, "slime", SlimeBlock.CODEC);
		Registry.register(registry, "small_dripleaf", SmallDripleafBlock.CODEC);
		Registry.register(registry, "smithing_table", SmithingTableBlock.CODEC);
		Registry.register(registry, "smoker", SmokerBlock.CODEC);
		Registry.register(registry, "sniffer_egg", SnifferEggBlock.CODEC);
		Registry.register(registry, "snow_layer", SnowLayerBlock.CODEC);
		Registry.register(registry, "snowy_dirt", SnowyDirtBlock.CODEC);
		Registry.register(registry, "soul_fire", SoulFireBlock.CODEC);
		Registry.register(registry, "soul_sand", SoulSandBlock.CODEC);
		Registry.register(registry, "spawner", SpawnerBlock.CODEC);
		Registry.register(registry, "sponge", SpongeBlock.CODEC);
		Registry.register(registry, "spore_blossom", SporeBlossomBlock.CODEC);
		Registry.register(registry, "stained_glass_pane", StainedGlassPaneBlock.CODEC);
		Registry.register(registry, "stained_glass", StainedGlassBlock.CODEC);
		Registry.register(registry, "stair", StairBlock.CODEC);
		Registry.register(registry, "standing_sign", StandingSignBlock.CODEC);
		Registry.register(registry, "stem", StemBlock.CODEC);
		Registry.register(registry, "stonecutter", StonecutterBlock.CODEC);
		Registry.register(registry, "structure", StructureBlock.CODEC);
		Registry.register(registry, "structure_void", StructureVoidBlock.CODEC);
		Registry.register(registry, "sugar_cane", SugarCaneBlock.CODEC);
		Registry.register(registry, "sweet_berry_bush", SweetBerryBushBlock.CODEC);
		Registry.register(registry, "tall_flower", TallFlowerBlock.CODEC);
		Registry.register(registry, "tall_grass", TallGrassBlock.CODEC);
		Registry.register(registry, "tall_seagrass", TallSeagrassBlock.CODEC);
		Registry.register(registry, "target", TargetBlock.CODEC);
		Registry.register(registry, "tinted_glass", TintedGlassBlock.CODEC);
		Registry.register(registry, "tnt", TntBlock.CODEC);
		Registry.register(registry, "torchflower_crop", TorchflowerCropBlock.CODEC);
		Registry.register(registry, "torch", TorchBlock.CODEC);
		Registry.register(registry, "transparent", TransparentBlock.CODEC);
		Registry.register(registry, "trapdoor", TrapDoorBlock.CODEC);
		Registry.register(registry, "trapped_chest", TrappedChestBlock.CODEC);
		Registry.register(registry, "trip_wire_hook", TripWireHookBlock.CODEC);
		Registry.register(registry, "tripwire", TripWireBlock.CODEC);
		Registry.register(registry, "turtle_egg", TurtleEggBlock.CODEC);
		Registry.register(registry, "twisting_vines_plant", TwistingVinesPlantBlock.CODEC);
		Registry.register(registry, "twisting_vines", TwistingVinesBlock.CODEC);
		Registry.register(registry, "vine", VineBlock.CODEC);
		Registry.register(registry, "wall_banner", WallBannerBlock.CODEC);
		Registry.register(registry, "wall_hanging_sign", WallHangingSignBlock.CODEC);
		Registry.register(registry, "wall_sign", WallSignBlock.CODEC);
		Registry.register(registry, "wall_skull", WallSkullBlock.CODEC);
		Registry.register(registry, "wall_torch", WallTorchBlock.CODEC);
		Registry.register(registry, "wall", WallBlock.CODEC);
		Registry.register(registry, "waterlily", WaterlilyBlock.CODEC);
		Registry.register(registry, "weathering_copper_bulb", WeatheringCopperBulbBlock.CODEC);
		Registry.register(registry, "weathering_copper_door", WeatheringCopperDoorBlock.CODEC);
		Registry.register(registry, "weathering_copper_full", WeatheringCopperFullBlock.CODEC);
		Registry.register(registry, "weathering_copper_grate", WeatheringCopperGrateBlock.CODEC);
		Registry.register(registry, "weathering_copper_slab", WeatheringCopperSlabBlock.CODEC);
		Registry.register(registry, "weathering_copper_stair", WeatheringCopperStairBlock.CODEC);
		Registry.register(registry, "weathering_copper_trap_door", WeatheringCopperTrapDoorBlock.CODEC);
		Registry.register(registry, "web", WebBlock.CODEC);
		Registry.register(registry, "weeping_vines_plant", WeepingVinesPlantBlock.CODEC);
		Registry.register(registry, "weeping_vines", WeepingVinesBlock.CODEC);
		Registry.register(registry, "weighted_pressure_plate", WeightedPressurePlateBlock.CODEC);
		Registry.register(registry, "wet_sponge", WetSpongeBlock.CODEC);
		Registry.register(registry, "wither_rose", WitherRoseBlock.CODEC);
		Registry.register(registry, "wither_skull", WitherSkullBlock.CODEC);
		Registry.register(registry, "wither_wall_skull", WitherWallSkullBlock.CODEC);
		Registry.register(registry, "wool_carpet", WoolCarpetBlock.CODEC);
		return mapCodec;
	}
}
