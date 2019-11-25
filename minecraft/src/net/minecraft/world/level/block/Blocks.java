package net.minecraft.world.level.block;

import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.grower.AcaciaTreeGrower;
import net.minecraft.world.level.block.grower.BirchTreeGrower;
import net.minecraft.world.level.block.grower.DarkOakTreeGrower;
import net.minecraft.world.level.block.grower.JungleTreeGrower;
import net.minecraft.world.level.block.grower.OakTreeGrower;
import net.minecraft.world.level.block.grower.SpruceTreeGrower;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class Blocks {
	public static final Block AIR = register("air", new AirBlock(Block.Properties.of(Material.AIR).noCollission().noDrops()));
	public static final Block STONE = register("stone", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5F, 6.0F)));
	public static final Block GRANITE = register("granite", new Block(Block.Properties.of(Material.STONE, MaterialColor.DIRT).strength(1.5F, 6.0F)));
	public static final Block POLISHED_GRANITE = register(
		"polished_granite", new Block(Block.Properties.of(Material.STONE, MaterialColor.DIRT).strength(1.5F, 6.0F))
	);
	public static final Block DIORITE = register("diorite", new Block(Block.Properties.of(Material.STONE, MaterialColor.QUARTZ).strength(1.5F, 6.0F)));
	public static final Block POLISHED_DIORITE = register(
		"polished_diorite", new Block(Block.Properties.of(Material.STONE, MaterialColor.QUARTZ).strength(1.5F, 6.0F))
	);
	public static final Block ANDESITE = register("andesite", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5F, 6.0F)));
	public static final Block POLISHED_ANDESITE = register(
		"polished_andesite", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5F, 6.0F))
	);
	public static final Block GRASS_BLOCK = register(
		"grass_block", new GrassBlock(Block.Properties.of(Material.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS))
	);
	public static final Block DIRT = register("dirt", new Block(Block.Properties.of(Material.DIRT, MaterialColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));
	public static final Block COARSE_DIRT = register(
		"coarse_dirt", new Block(Block.Properties.of(Material.DIRT, MaterialColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL))
	);
	public static final Block PODZOL = register(
		"podzol", new SnowyDirtBlock(Block.Properties.of(Material.DIRT, MaterialColor.PODZOL).strength(0.5F).sound(SoundType.GRAVEL))
	);
	public static final Block COBBLESTONE = register("cobblestone", new Block(Block.Properties.of(Material.STONE).strength(2.0F, 6.0F)));
	public static final Block OAK_PLANKS = register(
		"oak_planks", new Block(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block SPRUCE_PLANKS = register(
		"spruce_planks", new Block(Block.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block BIRCH_PLANKS = register(
		"birch_planks", new Block(Block.Properties.of(Material.WOOD, MaterialColor.SAND).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block JUNGLE_PLANKS = register(
		"jungle_planks", new Block(Block.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block ACACIA_PLANKS = register(
		"acacia_planks", new Block(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_ORANGE).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block DARK_OAK_PLANKS = register(
		"dark_oak_planks", new Block(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block OAK_SAPLING = register(
		"oak_sapling", new SaplingBlock(new OakTreeGrower(), Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	);
	public static final Block SPRUCE_SAPLING = register(
		"spruce_sapling",
		new SaplingBlock(new SpruceTreeGrower(), Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	);
	public static final Block BIRCH_SAPLING = register(
		"birch_sapling",
		new SaplingBlock(new BirchTreeGrower(), Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	);
	public static final Block JUNGLE_SAPLING = register(
		"jungle_sapling",
		new SaplingBlock(new JungleTreeGrower(), Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	);
	public static final Block ACACIA_SAPLING = register(
		"acacia_sapling",
		new SaplingBlock(new AcaciaTreeGrower(), Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	);
	public static final Block DARK_OAK_SAPLING = register(
		"dark_oak_sapling",
		new SaplingBlock(new DarkOakTreeGrower(), Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	);
	public static final Block BEDROCK = register("bedrock", new BedrockBlock(Block.Properties.of(Material.STONE).strength(-1.0F, 3600000.0F).noDrops()));
	public static final Block WATER = register(
		"water", new LiquidBlock(Fluids.WATER, Block.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops())
	);
	public static final Block LAVA = register(
		"lava", new LiquidBlock(Fluids.LAVA, Block.Properties.of(Material.LAVA).noCollission().randomTicks().strength(100.0F).lightLevel(15).noDrops())
	);
	public static final Block SAND = register(
		"sand", new SandBlock(14406560, Block.Properties.of(Material.SAND, MaterialColor.SAND).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block RED_SAND = register(
		"red_sand", new SandBlock(11098145, Block.Properties.of(Material.SAND, MaterialColor.COLOR_ORANGE).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block GRAVEL = register(
		"gravel", new GravelBlock(Block.Properties.of(Material.SAND, MaterialColor.STONE).strength(0.6F).sound(SoundType.GRAVEL))
	);
	public static final Block GOLD_ORE = register("gold_ore", new OreBlock(Block.Properties.of(Material.STONE).strength(3.0F, 3.0F)));
	public static final Block IRON_ORE = register("iron_ore", new OreBlock(Block.Properties.of(Material.STONE).strength(3.0F, 3.0F)));
	public static final Block COAL_ORE = register("coal_ore", new OreBlock(Block.Properties.of(Material.STONE).strength(3.0F, 3.0F)));
	public static final Block OAK_LOG = register(
		"oak_log", new LogBlock(MaterialColor.WOOD, Block.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block SPRUCE_LOG = register(
		"spruce_log", new LogBlock(MaterialColor.PODZOL, Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block BIRCH_LOG = register(
		"birch_log", new LogBlock(MaterialColor.SAND, Block.Properties.of(Material.WOOD, MaterialColor.QUARTZ).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block JUNGLE_LOG = register(
		"jungle_log", new LogBlock(MaterialColor.DIRT, Block.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block ACACIA_LOG = register(
		"acacia_log", new LogBlock(MaterialColor.COLOR_ORANGE, Block.Properties.of(Material.WOOD, MaterialColor.STONE).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block DARK_OAK_LOG = register(
		"dark_oak_log", new LogBlock(MaterialColor.COLOR_BROWN, Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_SPRUCE_LOG = register(
		"stripped_spruce_log", new LogBlock(MaterialColor.PODZOL, Block.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_BIRCH_LOG = register(
		"stripped_birch_log", new LogBlock(MaterialColor.SAND, Block.Properties.of(Material.WOOD, MaterialColor.SAND).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_JUNGLE_LOG = register(
		"stripped_jungle_log", new LogBlock(MaterialColor.DIRT, Block.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_ACACIA_LOG = register(
		"stripped_acacia_log",
		new LogBlock(MaterialColor.COLOR_ORANGE, Block.Properties.of(Material.WOOD, MaterialColor.COLOR_ORANGE).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_DARK_OAK_LOG = register(
		"stripped_dark_oak_log",
		new LogBlock(MaterialColor.COLOR_BROWN, Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_OAK_LOG = register(
		"stripped_oak_log", new LogBlock(MaterialColor.WOOD, Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block OAK_WOOD = register(
		"oak_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block SPRUCE_WOOD = register(
		"spruce_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block BIRCH_WOOD = register(
		"birch_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.SAND).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block JUNGLE_WOOD = register(
		"jungle_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block ACACIA_WOOD = register(
		"acacia_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_GRAY).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block DARK_OAK_WOOD = register(
		"dark_oak_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_OAK_WOOD = register(
		"stripped_oak_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_SPRUCE_WOOD = register(
		"stripped_spruce_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_BIRCH_WOOD = register(
		"stripped_birch_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.SAND).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_JUNGLE_WOOD = register(
		"stripped_jungle_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_ACACIA_WOOD = register(
		"stripped_acacia_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_ORANGE).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block STRIPPED_DARK_OAK_WOOD = register(
		"stripped_dark_oak_wood", new RotatedPillarBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD))
	);
	public static final Block OAK_LEAVES = register(
		"oak_leaves", new LeavesBlock(Block.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion())
	);
	public static final Block SPRUCE_LEAVES = register(
		"spruce_leaves", new LeavesBlock(Block.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion())
	);
	public static final Block BIRCH_LEAVES = register(
		"birch_leaves", new LeavesBlock(Block.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion())
	);
	public static final Block JUNGLE_LEAVES = register(
		"jungle_leaves", new LeavesBlock(Block.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion())
	);
	public static final Block ACACIA_LEAVES = register(
		"acacia_leaves", new LeavesBlock(Block.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion())
	);
	public static final Block DARK_OAK_LEAVES = register(
		"dark_oak_leaves", new LeavesBlock(Block.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion())
	);
	public static final Block SPONGE = register("sponge", new SpongeBlock(Block.Properties.of(Material.SPONGE).strength(0.6F).sound(SoundType.GRASS)));
	public static final Block WET_SPONGE = register("wet_sponge", new WetSpongeBlock(Block.Properties.of(Material.SPONGE).strength(0.6F).sound(SoundType.GRASS)));
	public static final Block GLASS = register("glass", new GlassBlock(Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion()));
	public static final Block LAPIS_ORE = register("lapis_ore", new OreBlock(Block.Properties.of(Material.STONE).strength(3.0F, 3.0F)));
	public static final Block LAPIS_BLOCK = register("lapis_block", new Block(Block.Properties.of(Material.METAL, MaterialColor.LAPIS).strength(3.0F, 3.0F)));
	public static final Block DISPENSER = register("dispenser", new DispenserBlock(Block.Properties.of(Material.STONE).strength(3.5F)));
	public static final Block SANDSTONE = register("sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.SAND).strength(0.8F)));
	public static final Block CHISELED_SANDSTONE = register(
		"chiseled_sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.SAND).strength(0.8F))
	);
	public static final Block CUT_SANDSTONE = register("cut_sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.SAND).strength(0.8F)));
	public static final Block NOTE_BLOCK = register("note_block", new NoteBlock(Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(0.8F)));
	public static final Block WHITE_BED = register(
		"white_bed", new BedBlock(DyeColor.WHITE, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block ORANGE_BED = register(
		"orange_bed", new BedBlock(DyeColor.ORANGE, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block MAGENTA_BED = register(
		"magenta_bed", new BedBlock(DyeColor.MAGENTA, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block LIGHT_BLUE_BED = register(
		"light_blue_bed", new BedBlock(DyeColor.LIGHT_BLUE, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block YELLOW_BED = register(
		"yellow_bed", new BedBlock(DyeColor.YELLOW, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block LIME_BED = register(
		"lime_bed", new BedBlock(DyeColor.LIME, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block PINK_BED = register(
		"pink_bed", new BedBlock(DyeColor.PINK, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block GRAY_BED = register(
		"gray_bed", new BedBlock(DyeColor.GRAY, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block LIGHT_GRAY_BED = register(
		"light_gray_bed", new BedBlock(DyeColor.LIGHT_GRAY, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block CYAN_BED = register(
		"cyan_bed", new BedBlock(DyeColor.CYAN, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block PURPLE_BED = register(
		"purple_bed", new BedBlock(DyeColor.PURPLE, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block BLUE_BED = register(
		"blue_bed", new BedBlock(DyeColor.BLUE, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block BROWN_BED = register(
		"brown_bed", new BedBlock(DyeColor.BROWN, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block GREEN_BED = register(
		"green_bed", new BedBlock(DyeColor.GREEN, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block RED_BED = register(
		"red_bed", new BedBlock(DyeColor.RED, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block BLACK_BED = register(
		"black_bed", new BedBlock(DyeColor.BLACK, Block.Properties.of(Material.WOOL).sound(SoundType.WOOD).strength(0.2F).noOcclusion())
	);
	public static final Block POWERED_RAIL = register(
		"powered_rail", new PoweredRailBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL))
	);
	public static final Block DETECTOR_RAIL = register(
		"detector_rail", new DetectorRailBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL))
	);
	public static final Block STICKY_PISTON = register("sticky_piston", new PistonBaseBlock(true, Block.Properties.of(Material.PISTON).strength(0.5F)));
	public static final Block COBWEB = register("cobweb", new WebBlock(Block.Properties.of(Material.WEB).noCollission().strength(4.0F)));
	public static final Block GRASS = register(
		"grass", new TallGrassBlock(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block FERN = register(
		"fern", new TallGrassBlock(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block DEAD_BUSH = register(
		"dead_bush", new DeadBushBlock(Block.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.WOOD).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block SEAGRASS = register(
		"seagrass", new Seagrass(Block.Properties.of(Material.REPLACEABLE_WATER_PLANT).noCollission().instabreak().sound(SoundType.WET_GRASS))
	);
	public static final Block TALL_SEAGRASS = register(
		"tall_seagrass", new TallSeagrass(Block.Properties.of(Material.REPLACEABLE_WATER_PLANT).noCollission().instabreak().sound(SoundType.WET_GRASS))
	);
	public static final Block PISTON = register("piston", new PistonBaseBlock(false, Block.Properties.of(Material.PISTON).strength(0.5F)));
	public static final Block PISTON_HEAD = register("piston_head", new PistonHeadBlock(Block.Properties.of(Material.PISTON).strength(0.5F).noDrops()));
	public static final Block WHITE_WOOL = register(
		"white_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.SNOW).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block ORANGE_WOOL = register(
		"orange_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_ORANGE).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block MAGENTA_WOOL = register(
		"magenta_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_MAGENTA).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block LIGHT_BLUE_WOOL = register(
		"light_blue_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_LIGHT_BLUE).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block YELLOW_WOOL = register(
		"yellow_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_YELLOW).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block LIME_WOOL = register(
		"lime_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_LIGHT_GREEN).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block PINK_WOOL = register(
		"pink_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_PINK).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block GRAY_WOOL = register(
		"gray_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_GRAY).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block LIGHT_GRAY_WOOL = register(
		"light_gray_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_LIGHT_GRAY).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block CYAN_WOOL = register(
		"cyan_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_CYAN).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block PURPLE_WOOL = register(
		"purple_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_PURPLE).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block BLUE_WOOL = register(
		"blue_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_BLUE).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block BROWN_WOOL = register(
		"brown_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_BROWN).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block GREEN_WOOL = register(
		"green_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_GREEN).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block RED_WOOL = register(
		"red_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_RED).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block BLACK_WOOL = register(
		"black_wool", new Block(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_BLACK).strength(0.8F).sound(SoundType.WOOL))
	);
	public static final Block MOVING_PISTON = register(
		"moving_piston", new MovingPistonBlock(Block.Properties.of(Material.PISTON).strength(-1.0F).dynamicShape().noDrops().noOcclusion())
	);
	public static final Block DANDELION = register(
		"dandelion", new FlowerBlock(MobEffects.SATURATION, 7, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block POPPY = register(
		"poppy", new FlowerBlock(MobEffects.NIGHT_VISION, 5, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block BLUE_ORCHID = register(
		"blue_orchid", new FlowerBlock(MobEffects.SATURATION, 7, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block ALLIUM = register(
		"allium", new FlowerBlock(MobEffects.FIRE_RESISTANCE, 4, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block AZURE_BLUET = register(
		"azure_bluet", new FlowerBlock(MobEffects.BLINDNESS, 8, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block RED_TULIP = register(
		"red_tulip", new FlowerBlock(MobEffects.WEAKNESS, 9, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block ORANGE_TULIP = register(
		"orange_tulip", new FlowerBlock(MobEffects.WEAKNESS, 9, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block WHITE_TULIP = register(
		"white_tulip", new FlowerBlock(MobEffects.WEAKNESS, 9, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block PINK_TULIP = register(
		"pink_tulip", new FlowerBlock(MobEffects.WEAKNESS, 9, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block OXEYE_DAISY = register(
		"oxeye_daisy", new FlowerBlock(MobEffects.REGENERATION, 8, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block CORNFLOWER = register(
		"cornflower", new FlowerBlock(MobEffects.JUMP, 6, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block WITHER_ROSE = register(
		"wither_rose", new WitherRoseBlock(MobEffects.WITHER, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block LILY_OF_THE_VALLEY = register(
		"lily_of_the_valley", new FlowerBlock(MobEffects.POISON, 12, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block BROWN_MUSHROOM = register(
		"brown_mushroom", new MushroomBlock(Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).lightLevel(1))
	);
	public static final Block RED_MUSHROOM = register(
		"red_mushroom", new MushroomBlock(Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	);
	public static final Block GOLD_BLOCK = register(
		"gold_block", new Block(Block.Properties.of(Material.METAL, MaterialColor.GOLD).strength(3.0F, 6.0F).sound(SoundType.METAL))
	);
	public static final Block IRON_BLOCK = register(
		"iron_block", new Block(Block.Properties.of(Material.METAL, MaterialColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL))
	);
	public static final Block BRICKS = register("bricks", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_RED).strength(2.0F, 6.0F)));
	public static final Block TNT = register("tnt", new TntBlock(Block.Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS)));
	public static final Block BOOKSHELF = register("bookshelf", new Block(Block.Properties.of(Material.WOOD).strength(1.5F).sound(SoundType.WOOD)));
	public static final Block MOSSY_COBBLESTONE = register("mossy_cobblestone", new Block(Block.Properties.of(Material.STONE).strength(2.0F, 6.0F)));
	public static final Block OBSIDIAN = register("obsidian", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(50.0F, 1200.0F)));
	public static final Block TORCH = register(
		"torch", new TorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(14).sound(SoundType.WOOD))
	);
	public static final Block WALL_TORCH = register(
		"wall_torch", new WallTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(14).sound(SoundType.WOOD).dropsLike(TORCH))
	);
	public static final Block FIRE = register(
		"fire",
		new FireBlock(Block.Properties.of(Material.FIRE, MaterialColor.FIRE).noCollission().randomTicks().instabreak().lightLevel(15).sound(SoundType.WOOL).noDrops())
	);
	public static final Block SPAWNER = register(
		"spawner", new SpawnerBlock(Block.Properties.of(Material.STONE).strength(5.0F).sound(SoundType.METAL).noOcclusion())
	);
	public static final Block OAK_STAIRS = register("oak_stairs", new StairBlock(OAK_PLANKS.defaultBlockState(), Block.Properties.copy(OAK_PLANKS)));
	public static final Block CHEST = register(
		"chest", new ChestBlock(Block.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD), () -> BlockEntityType.CHEST)
	);
	public static final Block REDSTONE_WIRE = register(
		"redstone_wire", new RedStoneWireBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak())
	);
	public static final Block DIAMOND_ORE = register("diamond_ore", new OreBlock(Block.Properties.of(Material.STONE).strength(3.0F, 3.0F)));
	public static final Block DIAMOND_BLOCK = register(
		"diamond_block", new Block(Block.Properties.of(Material.METAL, MaterialColor.DIAMOND).strength(5.0F, 6.0F).sound(SoundType.METAL))
	);
	public static final Block CRAFTING_TABLE = register(
		"crafting_table", new CraftingTableBlock(Block.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD))
	);
	public static final Block WHEAT = register(
		"wheat", new CropBlock(Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP))
	);
	public static final Block FARMLAND = register(
		"farmland", new FarmBlock(Block.Properties.of(Material.DIRT).randomTicks().strength(0.6F).sound(SoundType.GRAVEL))
	);
	public static final Block FURNACE = register("furnace", new FurnaceBlock(Block.Properties.of(Material.STONE).strength(3.5F).lightLevel(13)));
	public static final Block OAK_SIGN = register(
		"oak_sign", new StandingSignBlock(Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD), WoodType.OAK)
	);
	public static final Block SPRUCE_SIGN = register(
		"spruce_sign",
		new StandingSignBlock(Block.Properties.of(Material.WOOD, SPRUCE_LOG.materialColor).noCollission().strength(1.0F).sound(SoundType.WOOD), WoodType.SPRUCE)
	);
	public static final Block BIRCH_SIGN = register(
		"birch_sign",
		new StandingSignBlock(Block.Properties.of(Material.WOOD, MaterialColor.SAND).noCollission().strength(1.0F).sound(SoundType.WOOD), WoodType.BIRCH)
	);
	public static final Block ACACIA_SIGN = register(
		"acacia_sign",
		new StandingSignBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_ORANGE).noCollission().strength(1.0F).sound(SoundType.WOOD), WoodType.ACACIA)
	);
	public static final Block JUNGLE_SIGN = register(
		"jungle_sign",
		new StandingSignBlock(Block.Properties.of(Material.WOOD, JUNGLE_LOG.materialColor).noCollission().strength(1.0F).sound(SoundType.WOOD), WoodType.JUNGLE)
	);
	public static final Block DARK_OAK_SIGN = register(
		"dark_oak_sign",
		new StandingSignBlock(Block.Properties.of(Material.WOOD, DARK_OAK_LOG.materialColor).noCollission().strength(1.0F).sound(SoundType.WOOD), WoodType.DARK_OAK)
	);
	public static final Block OAK_DOOR = register(
		"oak_door", new DoorBlock(Block.Properties.of(Material.WOOD, OAK_PLANKS.materialColor).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block LADDER = register(
		"ladder", new LadderBlock(Block.Properties.of(Material.DECORATION).strength(0.4F).sound(SoundType.LADDER).noOcclusion())
	);
	public static final Block RAIL = register("rail", new RailBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL)));
	public static final Block COBBLESTONE_STAIRS = register(
		"cobblestone_stairs", new StairBlock(COBBLESTONE.defaultBlockState(), Block.Properties.copy(COBBLESTONE))
	);
	public static final Block OAK_WALL_SIGN = register(
		"oak_wall_sign", new WallSignBlock(Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(OAK_SIGN), WoodType.OAK)
	);
	public static final Block SPRUCE_WALL_SIGN = register(
		"spruce_wall_sign",
		new WallSignBlock(
			Block.Properties.of(Material.WOOD, SPRUCE_LOG.materialColor).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(SPRUCE_SIGN), WoodType.SPRUCE
		)
	);
	public static final Block BIRCH_WALL_SIGN = register(
		"birch_wall_sign",
		new WallSignBlock(
			Block.Properties.of(Material.WOOD, MaterialColor.SAND).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(BIRCH_SIGN), WoodType.BIRCH
		)
	);
	public static final Block ACACIA_WALL_SIGN = register(
		"acacia_wall_sign",
		new WallSignBlock(
			Block.Properties.of(Material.WOOD, MaterialColor.COLOR_ORANGE).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(ACACIA_SIGN), WoodType.ACACIA
		)
	);
	public static final Block JUNGLE_WALL_SIGN = register(
		"jungle_wall_sign",
		new WallSignBlock(
			Block.Properties.of(Material.WOOD, JUNGLE_LOG.materialColor).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(JUNGLE_SIGN), WoodType.JUNGLE
		)
	);
	public static final Block DARK_OAK_WALL_SIGN = register(
		"dark_oak_wall_sign",
		new WallSignBlock(
			Block.Properties.of(Material.WOOD, DARK_OAK_LOG.materialColor).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(DARK_OAK_SIGN),
			WoodType.DARK_OAK
		)
	);
	public static final Block LEVER = register(
		"lever", new LeverBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD))
	);
	public static final Block STONE_PRESSURE_PLATE = register(
		"stone_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.MOBS, Block.Properties.of(Material.STONE).noCollission().strength(0.5F))
	);
	public static final Block IRON_DOOR = register(
		"iron_door", new DoorBlock(Block.Properties.of(Material.METAL, MaterialColor.METAL).strength(5.0F).sound(SoundType.METAL).noOcclusion())
	);
	public static final Block OAK_PRESSURE_PLATE = register(
		"oak_pressure_plate",
		new PressurePlateBlock(
			PressurePlateBlock.Sensitivity.EVERYTHING, Block.Properties.of(Material.WOOD, OAK_PLANKS.materialColor).noCollission().strength(0.5F).sound(SoundType.WOOD)
		)
	);
	public static final Block SPRUCE_PRESSURE_PLATE = register(
		"spruce_pressure_plate",
		new PressurePlateBlock(
			PressurePlateBlock.Sensitivity.EVERYTHING,
			Block.Properties.of(Material.WOOD, SPRUCE_PLANKS.materialColor).noCollission().strength(0.5F).sound(SoundType.WOOD)
		)
	);
	public static final Block BIRCH_PRESSURE_PLATE = register(
		"birch_pressure_plate",
		new PressurePlateBlock(
			PressurePlateBlock.Sensitivity.EVERYTHING,
			Block.Properties.of(Material.WOOD, BIRCH_PLANKS.materialColor).noCollission().strength(0.5F).sound(SoundType.WOOD)
		)
	);
	public static final Block JUNGLE_PRESSURE_PLATE = register(
		"jungle_pressure_plate",
		new PressurePlateBlock(
			PressurePlateBlock.Sensitivity.EVERYTHING,
			Block.Properties.of(Material.WOOD, JUNGLE_PLANKS.materialColor).noCollission().strength(0.5F).sound(SoundType.WOOD)
		)
	);
	public static final Block ACACIA_PRESSURE_PLATE = register(
		"acacia_pressure_plate",
		new PressurePlateBlock(
			PressurePlateBlock.Sensitivity.EVERYTHING,
			Block.Properties.of(Material.WOOD, ACACIA_PLANKS.materialColor).noCollission().strength(0.5F).sound(SoundType.WOOD)
		)
	);
	public static final Block DARK_OAK_PRESSURE_PLATE = register(
		"dark_oak_pressure_plate",
		new PressurePlateBlock(
			PressurePlateBlock.Sensitivity.EVERYTHING,
			Block.Properties.of(Material.WOOD, DARK_OAK_PLANKS.materialColor).noCollission().strength(0.5F).sound(SoundType.WOOD)
		)
	);
	public static final Block REDSTONE_ORE = register(
		"redstone_ore", new RedStoneOreBlock(Block.Properties.of(Material.STONE).randomTicks().lightLevel(9).strength(3.0F, 3.0F))
	);
	public static final Block REDSTONE_TORCH = register(
		"redstone_torch", new RedstoneTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(7).sound(SoundType.WOOD))
	);
	public static final Block REDSTONE_WALL_TORCH = register(
		"redstone_wall_torch",
		new RedstoneWallTorchBlock(Block.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(7).sound(SoundType.WOOD).dropsLike(REDSTONE_TORCH))
	);
	public static final Block STONE_BUTTON = register("stone_button", new StoneButtonBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.5F)));
	public static final Block SNOW = register(
		"snow", new SnowLayerBlock(Block.Properties.of(Material.TOP_SNOW).randomTicks().strength(0.1F).sound(SoundType.SNOW))
	);
	public static final Block ICE = register(
		"ice", new IceBlock(Block.Properties.of(Material.ICE).friction(0.98F).randomTicks().strength(0.5F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block SNOW_BLOCK = register("snow_block", new Block(Block.Properties.of(Material.SNOW).strength(0.2F).sound(SoundType.SNOW)));
	public static final Block CACTUS = register("cactus", new CactusBlock(Block.Properties.of(Material.CACTUS).randomTicks().strength(0.4F).sound(SoundType.WOOL)));
	public static final Block CLAY = register("clay", new Block(Block.Properties.of(Material.CLAY).strength(0.6F).sound(SoundType.GRAVEL)));
	public static final Block SUGAR_CANE = register(
		"sugar_cane", new SugarCaneBlock(Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	);
	public static final Block JUKEBOX = register("jukebox", new JukeboxBlock(Block.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 6.0F)));
	public static final Block OAK_FENCE = register(
		"oak_fence", new FenceBlock(Block.Properties.of(Material.WOOD, OAK_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block PUMPKIN = register(
		"pumpkin", new PumpkinBlock(Block.Properties.of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE).strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block NETHERRACK = register("netherrack", new Block(Block.Properties.of(Material.STONE, MaterialColor.NETHER).strength(0.4F)));
	public static final Block SOUL_SAND = register(
		"soul_sand",
		new SoulsandBlock(Block.Properties.of(Material.SAND, MaterialColor.COLOR_BROWN).randomTicks().strength(0.5F).speedFactor(0.4F).sound(SoundType.SAND))
	);
	public static final Block GLOWSTONE = register(
		"glowstone", new Block(Block.Properties.of(Material.GLASS, MaterialColor.SAND).strength(0.3F).sound(SoundType.GLASS).lightLevel(15))
	);
	public static final Block NETHER_PORTAL = register(
		"nether_portal",
		new NetherPortalBlock(Block.Properties.of(Material.PORTAL).noCollission().randomTicks().strength(-1.0F).sound(SoundType.GLASS).lightLevel(11).noDrops())
	);
	public static final Block CARVED_PUMPKIN = register(
		"carved_pumpkin", new CarvedPumpkinBlock(Block.Properties.of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE).strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block JACK_O_LANTERN = register(
		"jack_o_lantern",
		new CarvedPumpkinBlock(Block.Properties.of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE).strength(1.0F).sound(SoundType.WOOD).lightLevel(15))
	);
	public static final Block CAKE = register("cake", new CakeBlock(Block.Properties.of(Material.CAKE).strength(0.5F).sound(SoundType.WOOL)));
	public static final Block REPEATER = register("repeater", new RepeaterBlock(Block.Properties.of(Material.DECORATION).instabreak().sound(SoundType.WOOD)));
	public static final Block WHITE_STAINED_GLASS = register(
		"white_stained_glass",
		new StainedGlassBlock(DyeColor.WHITE, Block.Properties.of(Material.GLASS, DyeColor.WHITE).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block ORANGE_STAINED_GLASS = register(
		"orange_stained_glass",
		new StainedGlassBlock(DyeColor.ORANGE, Block.Properties.of(Material.GLASS, DyeColor.ORANGE).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block MAGENTA_STAINED_GLASS = register(
		"magenta_stained_glass",
		new StainedGlassBlock(DyeColor.MAGENTA, Block.Properties.of(Material.GLASS, DyeColor.MAGENTA).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block LIGHT_BLUE_STAINED_GLASS = register(
		"light_blue_stained_glass",
		new StainedGlassBlock(DyeColor.LIGHT_BLUE, Block.Properties.of(Material.GLASS, DyeColor.LIGHT_BLUE).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block YELLOW_STAINED_GLASS = register(
		"yellow_stained_glass",
		new StainedGlassBlock(DyeColor.YELLOW, Block.Properties.of(Material.GLASS, DyeColor.YELLOW).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block LIME_STAINED_GLASS = register(
		"lime_stained_glass",
		new StainedGlassBlock(DyeColor.LIME, Block.Properties.of(Material.GLASS, DyeColor.LIME).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block PINK_STAINED_GLASS = register(
		"pink_stained_glass",
		new StainedGlassBlock(DyeColor.PINK, Block.Properties.of(Material.GLASS, DyeColor.PINK).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block GRAY_STAINED_GLASS = register(
		"gray_stained_glass",
		new StainedGlassBlock(DyeColor.GRAY, Block.Properties.of(Material.GLASS, DyeColor.GRAY).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block LIGHT_GRAY_STAINED_GLASS = register(
		"light_gray_stained_glass",
		new StainedGlassBlock(DyeColor.LIGHT_GRAY, Block.Properties.of(Material.GLASS, DyeColor.LIGHT_GRAY).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block CYAN_STAINED_GLASS = register(
		"cyan_stained_glass",
		new StainedGlassBlock(DyeColor.CYAN, Block.Properties.of(Material.GLASS, DyeColor.CYAN).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block PURPLE_STAINED_GLASS = register(
		"purple_stained_glass",
		new StainedGlassBlock(DyeColor.PURPLE, Block.Properties.of(Material.GLASS, DyeColor.PURPLE).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block BLUE_STAINED_GLASS = register(
		"blue_stained_glass",
		new StainedGlassBlock(DyeColor.BLUE, Block.Properties.of(Material.GLASS, DyeColor.BLUE).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block BROWN_STAINED_GLASS = register(
		"brown_stained_glass",
		new StainedGlassBlock(DyeColor.BROWN, Block.Properties.of(Material.GLASS, DyeColor.BROWN).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block GREEN_STAINED_GLASS = register(
		"green_stained_glass",
		new StainedGlassBlock(DyeColor.GREEN, Block.Properties.of(Material.GLASS, DyeColor.GREEN).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block RED_STAINED_GLASS = register(
		"red_stained_glass",
		new StainedGlassBlock(DyeColor.RED, Block.Properties.of(Material.GLASS, DyeColor.RED).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block BLACK_STAINED_GLASS = register(
		"black_stained_glass",
		new StainedGlassBlock(DyeColor.BLACK, Block.Properties.of(Material.GLASS, DyeColor.BLACK).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block OAK_TRAPDOOR = register(
		"oak_trapdoor", new TrapDoorBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block SPRUCE_TRAPDOOR = register(
		"spruce_trapdoor", new TrapDoorBlock(Block.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block BIRCH_TRAPDOOR = register(
		"birch_trapdoor", new TrapDoorBlock(Block.Properties.of(Material.WOOD, MaterialColor.SAND).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block JUNGLE_TRAPDOOR = register(
		"jungle_trapdoor", new TrapDoorBlock(Block.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block ACACIA_TRAPDOOR = register(
		"acacia_trapdoor", new TrapDoorBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_ORANGE).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block DARK_OAK_TRAPDOOR = register(
		"dark_oak_trapdoor", new TrapDoorBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block STONE_BRICKS = register("stone_bricks", new Block(Block.Properties.of(Material.STONE).strength(1.5F, 6.0F)));
	public static final Block MOSSY_STONE_BRICKS = register("mossy_stone_bricks", new Block(Block.Properties.of(Material.STONE).strength(1.5F, 6.0F)));
	public static final Block CRACKED_STONE_BRICKS = register("cracked_stone_bricks", new Block(Block.Properties.of(Material.STONE).strength(1.5F, 6.0F)));
	public static final Block CHISELED_STONE_BRICKS = register("chiseled_stone_bricks", new Block(Block.Properties.of(Material.STONE).strength(1.5F, 6.0F)));
	public static final Block INFESTED_STONE = register("infested_stone", new InfestedBlock(STONE, Block.Properties.of(Material.CLAY).strength(0.0F, 0.75F)));
	public static final Block INFESTED_COBBLESTONE = register(
		"infested_cobblestone", new InfestedBlock(COBBLESTONE, Block.Properties.of(Material.CLAY).strength(0.0F, 0.75F))
	);
	public static final Block INFESTED_STONE_BRICKS = register(
		"infested_stone_bricks", new InfestedBlock(STONE_BRICKS, Block.Properties.of(Material.CLAY).strength(0.0F, 0.75F))
	);
	public static final Block INFESTED_MOSSY_STONE_BRICKS = register(
		"infested_mossy_stone_bricks", new InfestedBlock(MOSSY_STONE_BRICKS, Block.Properties.of(Material.CLAY).strength(0.0F, 0.75F))
	);
	public static final Block INFESTED_CRACKED_STONE_BRICKS = register(
		"infested_cracked_stone_bricks", new InfestedBlock(CRACKED_STONE_BRICKS, Block.Properties.of(Material.CLAY).strength(0.0F, 0.75F))
	);
	public static final Block INFESTED_CHISELED_STONE_BRICKS = register(
		"infested_chiseled_stone_bricks", new InfestedBlock(CHISELED_STONE_BRICKS, Block.Properties.of(Material.CLAY).strength(0.0F, 0.75F))
	);
	public static final Block BROWN_MUSHROOM_BLOCK = register(
		"brown_mushroom_block", new HugeMushroomBlock(Block.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(0.2F).sound(SoundType.WOOD))
	);
	public static final Block RED_MUSHROOM_BLOCK = register(
		"red_mushroom_block", new HugeMushroomBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_RED).strength(0.2F).sound(SoundType.WOOD))
	);
	public static final Block MUSHROOM_STEM = register(
		"mushroom_stem", new HugeMushroomBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOL).strength(0.2F).sound(SoundType.WOOD))
	);
	public static final Block IRON_BARS = register(
		"iron_bars", new IronBarsBlock(Block.Properties.of(Material.METAL, MaterialColor.NONE).strength(5.0F, 6.0F).sound(SoundType.METAL).noOcclusion())
	);
	public static final Block GLASS_PANE = register(
		"glass_pane", new IronBarsBlock(Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block MELON = register(
		"melon", new MelonBlock(Block.Properties.of(Material.VEGETABLE, MaterialColor.COLOR_LIGHT_GREEN).strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block ATTACHED_PUMPKIN_STEM = register(
		"attached_pumpkin_stem",
		new AttachedStemBlock((StemGrownBlock)PUMPKIN, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.WOOD))
	);
	public static final Block ATTACHED_MELON_STEM = register(
		"attached_melon_stem", new AttachedStemBlock((StemGrownBlock)MELON, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.WOOD))
	);
	public static final Block PUMPKIN_STEM = register(
		"pumpkin_stem",
		new StemBlock((StemGrownBlock)PUMPKIN, Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.HARD_CROP))
	);
	public static final Block MELON_STEM = register(
		"melon_stem", new StemBlock((StemGrownBlock)MELON, Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.HARD_CROP))
	);
	public static final Block VINE = register(
		"vine", new VineBlock(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().randomTicks().strength(0.2F).sound(SoundType.GRASS))
	);
	public static final Block OAK_FENCE_GATE = register(
		"oak_fence_gate", new FenceGateBlock(Block.Properties.of(Material.WOOD, OAK_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block BRICK_STAIRS = register("brick_stairs", new StairBlock(BRICKS.defaultBlockState(), Block.Properties.copy(BRICKS)));
	public static final Block STONE_BRICK_STAIRS = register(
		"stone_brick_stairs", new StairBlock(STONE_BRICKS.defaultBlockState(), Block.Properties.copy(STONE_BRICKS))
	);
	public static final Block MYCELIUM = register(
		"mycelium", new MyceliumBlock(Block.Properties.of(Material.GRASS, MaterialColor.COLOR_PURPLE).randomTicks().strength(0.6F).sound(SoundType.GRASS))
	);
	public static final Block LILY_PAD = register(
		"lily_pad", new WaterlilyBlock(Block.Properties.of(Material.PLANT).instabreak().sound(SoundType.GRASS).noOcclusion())
	);
	public static final Block NETHER_BRICKS = register("nether_bricks", new Block(Block.Properties.of(Material.STONE, MaterialColor.NETHER).strength(2.0F, 6.0F)));
	public static final Block NETHER_BRICK_FENCE = register(
		"nether_brick_fence", new FenceBlock(Block.Properties.of(Material.STONE, MaterialColor.NETHER).strength(2.0F, 6.0F))
	);
	public static final Block NETHER_BRICK_STAIRS = register(
		"nether_brick_stairs", new StairBlock(NETHER_BRICKS.defaultBlockState(), Block.Properties.copy(NETHER_BRICKS))
	);
	public static final Block NETHER_WART = register(
		"nether_wart", new NetherWartBlock(Block.Properties.of(Material.PLANT, MaterialColor.COLOR_RED).noCollission().randomTicks().sound(SoundType.NETHER_WART))
	);
	public static final Block ENCHANTING_TABLE = register(
		"enchanting_table", new EnchantmentTableBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_RED).strength(5.0F, 1200.0F))
	);
	public static final Block BREWING_STAND = register(
		"brewing_stand", new BrewingStandBlock(Block.Properties.of(Material.METAL).strength(0.5F).lightLevel(1).noOcclusion())
	);
	public static final Block CAULDRON = register(
		"cauldron", new CauldronBlock(Block.Properties.of(Material.METAL, MaterialColor.STONE).strength(2.0F).noOcclusion())
	);
	public static final Block END_PORTAL = register(
		"end_portal",
		new EndPortalBlock(Block.Properties.of(Material.PORTAL, MaterialColor.COLOR_BLACK).noCollission().lightLevel(15).strength(-1.0F, 3600000.0F).noDrops())
	);
	public static final Block END_PORTAL_FRAME = register(
		"end_portal_frame",
		new EndPortalFrameBlock(
			Block.Properties.of(Material.STONE, MaterialColor.COLOR_GREEN).sound(SoundType.GLASS).lightLevel(1).strength(-1.0F, 3600000.0F).noDrops()
		)
	);
	public static final Block END_STONE = register("end_stone", new Block(Block.Properties.of(Material.STONE, MaterialColor.SAND).strength(3.0F, 9.0F)));
	public static final Block DRAGON_EGG = register(
		"dragon_egg", new DragonEggBlock(Block.Properties.of(Material.EGG, MaterialColor.COLOR_BLACK).strength(3.0F, 9.0F).lightLevel(1).noOcclusion())
	);
	public static final Block REDSTONE_LAMP = register(
		"redstone_lamp", new RedstoneLampBlock(Block.Properties.of(Material.BUILDABLE_GLASS).lightLevel(15).strength(0.3F).sound(SoundType.GLASS))
	);
	public static final Block COCOA = register(
		"cocoa", new CocoaBlock(Block.Properties.of(Material.PLANT).randomTicks().strength(0.2F, 3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block SANDSTONE_STAIRS = register("sandstone_stairs", new StairBlock(SANDSTONE.defaultBlockState(), Block.Properties.copy(SANDSTONE)));
	public static final Block EMERALD_ORE = register("emerald_ore", new OreBlock(Block.Properties.of(Material.STONE).strength(3.0F, 3.0F)));
	public static final Block ENDER_CHEST = register("ender_chest", new EnderChestBlock(Block.Properties.of(Material.STONE).strength(22.5F, 600.0F).lightLevel(7)));
	public static final Block TRIPWIRE_HOOK = register("tripwire_hook", new TripWireHookBlock(Block.Properties.of(Material.DECORATION).noCollission()));
	public static final Block TRIPWIRE = register(
		"tripwire", new TripWireBlock((TripWireHookBlock)TRIPWIRE_HOOK, Block.Properties.of(Material.DECORATION).noCollission())
	);
	public static final Block EMERALD_BLOCK = register(
		"emerald_block", new Block(Block.Properties.of(Material.METAL, MaterialColor.EMERALD).strength(5.0F, 6.0F).sound(SoundType.METAL))
	);
	public static final Block SPRUCE_STAIRS = register("spruce_stairs", new StairBlock(SPRUCE_PLANKS.defaultBlockState(), Block.Properties.copy(SPRUCE_PLANKS)));
	public static final Block BIRCH_STAIRS = register("birch_stairs", new StairBlock(BIRCH_PLANKS.defaultBlockState(), Block.Properties.copy(BIRCH_PLANKS)));
	public static final Block JUNGLE_STAIRS = register("jungle_stairs", new StairBlock(JUNGLE_PLANKS.defaultBlockState(), Block.Properties.copy(JUNGLE_PLANKS)));
	public static final Block COMMAND_BLOCK = register(
		"command_block", new CommandBlock(Block.Properties.of(Material.METAL, MaterialColor.COLOR_BROWN).strength(-1.0F, 3600000.0F).noDrops())
	);
	public static final Block BEACON = register(
		"beacon", new BeaconBlock(Block.Properties.of(Material.GLASS, MaterialColor.DIAMOND).strength(3.0F).lightLevel(15).noOcclusion())
	);
	public static final Block COBBLESTONE_WALL = register("cobblestone_wall", new WallBlock(Block.Properties.copy(COBBLESTONE)));
	public static final Block MOSSY_COBBLESTONE_WALL = register("mossy_cobblestone_wall", new WallBlock(Block.Properties.copy(COBBLESTONE)));
	public static final Block FLOWER_POT = register("flower_pot", new FlowerPotBlock(AIR, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
	public static final Block POTTED_OAK_SAPLING = register(
		"potted_oak_sapling", new FlowerPotBlock(OAK_SAPLING, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_SPRUCE_SAPLING = register(
		"potted_spruce_sapling", new FlowerPotBlock(SPRUCE_SAPLING, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_BIRCH_SAPLING = register(
		"potted_birch_sapling", new FlowerPotBlock(BIRCH_SAPLING, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_JUNGLE_SAPLING = register(
		"potted_jungle_sapling", new FlowerPotBlock(JUNGLE_SAPLING, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_ACACIA_SAPLING = register(
		"potted_acacia_sapling", new FlowerPotBlock(ACACIA_SAPLING, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_DARK_OAK_SAPLING = register(
		"potted_dark_oak_sapling", new FlowerPotBlock(DARK_OAK_SAPLING, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_FERN = register("potted_fern", new FlowerPotBlock(FERN, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
	public static final Block POTTED_DANDELION = register(
		"potted_dandelion", new FlowerPotBlock(DANDELION, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_POPPY = register(
		"potted_poppy", new FlowerPotBlock(POPPY, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_BLUE_ORCHID = register(
		"potted_blue_orchid", new FlowerPotBlock(BLUE_ORCHID, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_ALLIUM = register(
		"potted_allium", new FlowerPotBlock(ALLIUM, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_AZURE_BLUET = register(
		"potted_azure_bluet", new FlowerPotBlock(AZURE_BLUET, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_RED_TULIP = register(
		"potted_red_tulip", new FlowerPotBlock(RED_TULIP, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_ORANGE_TULIP = register(
		"potted_orange_tulip", new FlowerPotBlock(ORANGE_TULIP, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_WHITE_TULIP = register(
		"potted_white_tulip", new FlowerPotBlock(WHITE_TULIP, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_PINK_TULIP = register(
		"potted_pink_tulip", new FlowerPotBlock(PINK_TULIP, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_OXEYE_DAISY = register(
		"potted_oxeye_daisy", new FlowerPotBlock(OXEYE_DAISY, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_CORNFLOWER = register(
		"potted_cornflower", new FlowerPotBlock(CORNFLOWER, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_LILY_OF_THE_VALLEY = register(
		"potted_lily_of_the_valley", new FlowerPotBlock(LILY_OF_THE_VALLEY, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_WITHER_ROSE = register(
		"potted_wither_rose", new FlowerPotBlock(WITHER_ROSE, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_RED_MUSHROOM = register(
		"potted_red_mushroom", new FlowerPotBlock(RED_MUSHROOM, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_BROWN_MUSHROOM = register(
		"potted_brown_mushroom", new FlowerPotBlock(BROWN_MUSHROOM, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_DEAD_BUSH = register(
		"potted_dead_bush", new FlowerPotBlock(DEAD_BUSH, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block POTTED_CACTUS = register(
		"potted_cactus", new FlowerPotBlock(CACTUS, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block CARROTS = register(
		"carrots", new CarrotBlock(Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP))
	);
	public static final Block POTATOES = register(
		"potatoes", new PotatoBlock(Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP))
	);
	public static final Block OAK_BUTTON = register(
		"oak_button", new WoodButtonBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD))
	);
	public static final Block SPRUCE_BUTTON = register(
		"spruce_button", new WoodButtonBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD))
	);
	public static final Block BIRCH_BUTTON = register(
		"birch_button", new WoodButtonBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD))
	);
	public static final Block JUNGLE_BUTTON = register(
		"jungle_button", new WoodButtonBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD))
	);
	public static final Block ACACIA_BUTTON = register(
		"acacia_button", new WoodButtonBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD))
	);
	public static final Block DARK_OAK_BUTTON = register(
		"dark_oak_button", new WoodButtonBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD))
	);
	public static final Block SKELETON_SKULL = register(
		"skeleton_skull", new SkullBlock(SkullBlock.Types.SKELETON, Block.Properties.of(Material.DECORATION).strength(1.0F))
	);
	public static final Block SKELETON_WALL_SKULL = register(
		"skeleton_wall_skull", new WallSkullBlock(SkullBlock.Types.SKELETON, Block.Properties.of(Material.DECORATION).strength(1.0F).dropsLike(SKELETON_SKULL))
	);
	public static final Block WITHER_SKELETON_SKULL = register(
		"wither_skeleton_skull", new WitherSkullBlock(Block.Properties.of(Material.DECORATION).strength(1.0F))
	);
	public static final Block WITHER_SKELETON_WALL_SKULL = register(
		"wither_skeleton_wall_skull", new WitherWallSkullBlock(Block.Properties.of(Material.DECORATION).strength(1.0F).dropsLike(WITHER_SKELETON_SKULL))
	);
	public static final Block ZOMBIE_HEAD = register(
		"zombie_head", new SkullBlock(SkullBlock.Types.ZOMBIE, Block.Properties.of(Material.DECORATION).strength(1.0F))
	);
	public static final Block ZOMBIE_WALL_HEAD = register(
		"zombie_wall_head", new WallSkullBlock(SkullBlock.Types.ZOMBIE, Block.Properties.of(Material.DECORATION).strength(1.0F).dropsLike(ZOMBIE_HEAD))
	);
	public static final Block PLAYER_HEAD = register("player_head", new PlayerHeadBlock(Block.Properties.of(Material.DECORATION).strength(1.0F)));
	public static final Block PLAYER_WALL_HEAD = register(
		"player_wall_head", new PlayerWallHeadBlock(Block.Properties.of(Material.DECORATION).strength(1.0F).dropsLike(PLAYER_HEAD))
	);
	public static final Block CREEPER_HEAD = register(
		"creeper_head", new SkullBlock(SkullBlock.Types.CREEPER, Block.Properties.of(Material.DECORATION).strength(1.0F))
	);
	public static final Block CREEPER_WALL_HEAD = register(
		"creeper_wall_head", new WallSkullBlock(SkullBlock.Types.CREEPER, Block.Properties.of(Material.DECORATION).strength(1.0F).dropsLike(CREEPER_HEAD))
	);
	public static final Block DRAGON_HEAD = register(
		"dragon_head", new SkullBlock(SkullBlock.Types.DRAGON, Block.Properties.of(Material.DECORATION).strength(1.0F))
	);
	public static final Block DRAGON_WALL_HEAD = register(
		"dragon_wall_head", new WallSkullBlock(SkullBlock.Types.DRAGON, Block.Properties.of(Material.DECORATION).strength(1.0F).dropsLike(DRAGON_HEAD))
	);
	public static final Block ANVIL = register(
		"anvil", new AnvilBlock(Block.Properties.of(Material.HEAVY_METAL, MaterialColor.METAL).strength(5.0F, 1200.0F).sound(SoundType.ANVIL))
	);
	public static final Block CHIPPED_ANVIL = register(
		"chipped_anvil", new AnvilBlock(Block.Properties.of(Material.HEAVY_METAL, MaterialColor.METAL).strength(5.0F, 1200.0F).sound(SoundType.ANVIL))
	);
	public static final Block DAMAGED_ANVIL = register(
		"damaged_anvil", new AnvilBlock(Block.Properties.of(Material.HEAVY_METAL, MaterialColor.METAL).strength(5.0F, 1200.0F).sound(SoundType.ANVIL))
	);
	public static final Block TRAPPED_CHEST = register(
		"trapped_chest", new TrappedChestBlock(Block.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD))
	);
	public static final Block LIGHT_WEIGHTED_PRESSURE_PLATE = register(
		"light_weighted_pressure_plate",
		new WeightedPressurePlateBlock(15, Block.Properties.of(Material.METAL, MaterialColor.GOLD).noCollission().strength(0.5F).sound(SoundType.WOOD))
	);
	public static final Block HEAVY_WEIGHTED_PRESSURE_PLATE = register(
		"heavy_weighted_pressure_plate", new WeightedPressurePlateBlock(150, Block.Properties.of(Material.METAL).noCollission().strength(0.5F).sound(SoundType.WOOD))
	);
	public static final Block COMPARATOR = register("comparator", new ComparatorBlock(Block.Properties.of(Material.DECORATION).instabreak().sound(SoundType.WOOD)));
	public static final Block DAYLIGHT_DETECTOR = register(
		"daylight_detector", new DaylightDetectorBlock(Block.Properties.of(Material.WOOD).strength(0.2F).sound(SoundType.WOOD))
	);
	public static final Block REDSTONE_BLOCK = register(
		"redstone_block", new PoweredBlock(Block.Properties.of(Material.METAL, MaterialColor.FIRE).strength(5.0F, 6.0F).sound(SoundType.METAL))
	);
	public static final Block NETHER_QUARTZ_ORE = register(
		"nether_quartz_ore", new OreBlock(Block.Properties.of(Material.STONE, MaterialColor.NETHER).strength(3.0F, 3.0F))
	);
	public static final Block HOPPER = register(
		"hopper", new HopperBlock(Block.Properties.of(Material.METAL, MaterialColor.STONE).strength(3.0F, 4.8F).sound(SoundType.METAL).noOcclusion())
	);
	public static final Block QUARTZ_BLOCK = register("quartz_block", new Block(Block.Properties.of(Material.STONE, MaterialColor.QUARTZ).strength(0.8F)));
	public static final Block CHISELED_QUARTZ_BLOCK = register(
		"chiseled_quartz_block", new Block(Block.Properties.of(Material.STONE, MaterialColor.QUARTZ).strength(0.8F))
	);
	public static final Block QUARTZ_PILLAR = register(
		"quartz_pillar", new RotatedPillarBlock(Block.Properties.of(Material.STONE, MaterialColor.QUARTZ).strength(0.8F))
	);
	public static final Block QUARTZ_STAIRS = register("quartz_stairs", new StairBlock(QUARTZ_BLOCK.defaultBlockState(), Block.Properties.copy(QUARTZ_BLOCK)));
	public static final Block ACTIVATOR_RAIL = register(
		"activator_rail", new PoweredRailBlock(Block.Properties.of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL))
	);
	public static final Block DROPPER = register("dropper", new DropperBlock(Block.Properties.of(Material.STONE).strength(3.5F)));
	public static final Block WHITE_TERRACOTTA = register(
		"white_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_WHITE).strength(1.25F, 4.2F))
	);
	public static final Block ORANGE_TERRACOTTA = register(
		"orange_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_ORANGE).strength(1.25F, 4.2F))
	);
	public static final Block MAGENTA_TERRACOTTA = register(
		"magenta_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_MAGENTA).strength(1.25F, 4.2F))
	);
	public static final Block LIGHT_BLUE_TERRACOTTA = register(
		"light_blue_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(1.25F, 4.2F))
	);
	public static final Block YELLOW_TERRACOTTA = register(
		"yellow_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_YELLOW).strength(1.25F, 4.2F))
	);
	public static final Block LIME_TERRACOTTA = register(
		"lime_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_LIGHT_GREEN).strength(1.25F, 4.2F))
	);
	public static final Block PINK_TERRACOTTA = register(
		"pink_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_PINK).strength(1.25F, 4.2F))
	);
	public static final Block GRAY_TERRACOTTA = register(
		"gray_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_GRAY).strength(1.25F, 4.2F))
	);
	public static final Block LIGHT_GRAY_TERRACOTTA = register(
		"light_gray_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_LIGHT_GRAY).strength(1.25F, 4.2F))
	);
	public static final Block CYAN_TERRACOTTA = register(
		"cyan_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).strength(1.25F, 4.2F))
	);
	public static final Block PURPLE_TERRACOTTA = register(
		"purple_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_PURPLE).strength(1.25F, 4.2F))
	);
	public static final Block BLUE_TERRACOTTA = register(
		"blue_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLUE).strength(1.25F, 4.2F))
	);
	public static final Block BROWN_TERRACOTTA = register(
		"brown_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BROWN).strength(1.25F, 4.2F))
	);
	public static final Block GREEN_TERRACOTTA = register(
		"green_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_GREEN).strength(1.25F, 4.2F))
	);
	public static final Block RED_TERRACOTTA = register(
		"red_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED).strength(1.25F, 4.2F))
	);
	public static final Block BLACK_TERRACOTTA = register(
		"black_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).strength(1.25F, 4.2F))
	);
	public static final Block WHITE_STAINED_GLASS_PANE = register(
		"white_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.WHITE, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block ORANGE_STAINED_GLASS_PANE = register(
		"orange_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.ORANGE, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block MAGENTA_STAINED_GLASS_PANE = register(
		"magenta_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.MAGENTA, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block LIGHT_BLUE_STAINED_GLASS_PANE = register(
		"light_blue_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.LIGHT_BLUE, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block YELLOW_STAINED_GLASS_PANE = register(
		"yellow_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.YELLOW, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block LIME_STAINED_GLASS_PANE = register(
		"lime_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.LIME, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block PINK_STAINED_GLASS_PANE = register(
		"pink_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.PINK, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block GRAY_STAINED_GLASS_PANE = register(
		"gray_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.GRAY, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block LIGHT_GRAY_STAINED_GLASS_PANE = register(
		"light_gray_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.LIGHT_GRAY, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block CYAN_STAINED_GLASS_PANE = register(
		"cyan_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.CYAN, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block PURPLE_STAINED_GLASS_PANE = register(
		"purple_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.PURPLE, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block BLUE_STAINED_GLASS_PANE = register(
		"blue_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.BLUE, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block BROWN_STAINED_GLASS_PANE = register(
		"brown_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.BROWN, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block GREEN_STAINED_GLASS_PANE = register(
		"green_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.GREEN, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block RED_STAINED_GLASS_PANE = register(
		"red_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.RED, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block BLACK_STAINED_GLASS_PANE = register(
		"black_stained_glass_pane",
		new StainedGlassPaneBlock(DyeColor.BLACK, Block.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block ACACIA_STAIRS = register("acacia_stairs", new StairBlock(ACACIA_PLANKS.defaultBlockState(), Block.Properties.copy(ACACIA_PLANKS)));
	public static final Block DARK_OAK_STAIRS = register(
		"dark_oak_stairs", new StairBlock(DARK_OAK_PLANKS.defaultBlockState(), Block.Properties.copy(DARK_OAK_PLANKS))
	);
	public static final Block SLIME_BLOCK = register(
		"slime_block", new SlimeBlock(Block.Properties.of(Material.CLAY, MaterialColor.GRASS).friction(0.8F).sound(SoundType.SLIME_BLOCK).noOcclusion())
	);
	public static final Block BARRIER = register(
		"barrier", new BarrierBlock(Block.Properties.of(Material.BARRIER).strength(-1.0F, 3600000.8F).noDrops().noOcclusion())
	);
	public static final Block IRON_TRAPDOOR = register(
		"iron_trapdoor", new TrapDoorBlock(Block.Properties.of(Material.METAL).strength(5.0F).sound(SoundType.METAL).noOcclusion())
	);
	public static final Block PRISMARINE = register("prismarine", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_CYAN).strength(1.5F, 6.0F)));
	public static final Block PRISMARINE_BRICKS = register(
		"prismarine_bricks", new Block(Block.Properties.of(Material.STONE, MaterialColor.DIAMOND).strength(1.5F, 6.0F))
	);
	public static final Block DARK_PRISMARINE = register(
		"dark_prismarine", new Block(Block.Properties.of(Material.STONE, MaterialColor.DIAMOND).strength(1.5F, 6.0F))
	);
	public static final Block PRISMARINE_STAIRS = register("prismarine_stairs", new StairBlock(PRISMARINE.defaultBlockState(), Block.Properties.copy(PRISMARINE)));
	public static final Block PRISMARINE_BRICK_STAIRS = register(
		"prismarine_brick_stairs", new StairBlock(PRISMARINE_BRICKS.defaultBlockState(), Block.Properties.copy(PRISMARINE_BRICKS))
	);
	public static final Block DARK_PRISMARINE_STAIRS = register(
		"dark_prismarine_stairs", new StairBlock(DARK_PRISMARINE.defaultBlockState(), Block.Properties.copy(DARK_PRISMARINE))
	);
	public static final Block PRISMARINE_SLAB = register(
		"prismarine_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_CYAN).strength(1.5F, 6.0F))
	);
	public static final Block PRISMARINE_BRICK_SLAB = register(
		"prismarine_brick_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.DIAMOND).strength(1.5F, 6.0F))
	);
	public static final Block DARK_PRISMARINE_SLAB = register(
		"dark_prismarine_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.DIAMOND).strength(1.5F, 6.0F))
	);
	public static final Block SEA_LANTERN = register(
		"sea_lantern", new Block(Block.Properties.of(Material.GLASS, MaterialColor.QUARTZ).strength(0.3F).sound(SoundType.GLASS).lightLevel(15))
	);
	public static final Block HAY_BLOCK = register(
		"hay_block", new HayBlock(Block.Properties.of(Material.GRASS, MaterialColor.COLOR_YELLOW).strength(0.5F).sound(SoundType.GRASS))
	);
	public static final Block WHITE_CARPET = register(
		"white_carpet", new WoolCarpetBlock(DyeColor.WHITE, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.SNOW).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block ORANGE_CARPET = register(
		"orange_carpet",
		new WoolCarpetBlock(DyeColor.ORANGE, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_ORANGE).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block MAGENTA_CARPET = register(
		"magenta_carpet",
		new WoolCarpetBlock(DyeColor.MAGENTA, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_MAGENTA).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block LIGHT_BLUE_CARPET = register(
		"light_blue_carpet",
		new WoolCarpetBlock(DyeColor.LIGHT_BLUE, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_LIGHT_BLUE).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block YELLOW_CARPET = register(
		"yellow_carpet",
		new WoolCarpetBlock(DyeColor.YELLOW, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_YELLOW).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block LIME_CARPET = register(
		"lime_carpet",
		new WoolCarpetBlock(DyeColor.LIME, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_LIGHT_GREEN).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block PINK_CARPET = register(
		"pink_carpet",
		new WoolCarpetBlock(DyeColor.PINK, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_PINK).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block GRAY_CARPET = register(
		"gray_carpet",
		new WoolCarpetBlock(DyeColor.GRAY, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_GRAY).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block LIGHT_GRAY_CARPET = register(
		"light_gray_carpet",
		new WoolCarpetBlock(DyeColor.LIGHT_GRAY, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block CYAN_CARPET = register(
		"cyan_carpet",
		new WoolCarpetBlock(DyeColor.CYAN, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_CYAN).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block PURPLE_CARPET = register(
		"purple_carpet",
		new WoolCarpetBlock(DyeColor.PURPLE, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_PURPLE).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block BLUE_CARPET = register(
		"blue_carpet",
		new WoolCarpetBlock(DyeColor.BLUE, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_BLUE).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block BROWN_CARPET = register(
		"brown_carpet",
		new WoolCarpetBlock(DyeColor.BROWN, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_BROWN).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block GREEN_CARPET = register(
		"green_carpet",
		new WoolCarpetBlock(DyeColor.GREEN, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_GREEN).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block RED_CARPET = register(
		"red_carpet", new WoolCarpetBlock(DyeColor.RED, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_RED).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block BLACK_CARPET = register(
		"black_carpet",
		new WoolCarpetBlock(DyeColor.BLACK, Block.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_BLACK).strength(0.1F).sound(SoundType.WOOL))
	);
	public static final Block TERRACOTTA = register("terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_ORANGE).strength(1.25F, 4.2F)));
	public static final Block COAL_BLOCK = register("coal_block", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(5.0F, 6.0F)));
	public static final Block PACKED_ICE = register(
		"packed_ice", new Block(Block.Properties.of(Material.ICE_SOLID).friction(0.98F).strength(0.5F).sound(SoundType.GLASS))
	);
	public static final Block SUNFLOWER = register(
		"sunflower", new TallFlowerBlock(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block LILAC = register(
		"lilac", new TallFlowerBlock(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block ROSE_BUSH = register(
		"rose_bush", new TallFlowerBlock(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block PEONY = register(
		"peony", new TallFlowerBlock(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block TALL_GRASS = register(
		"tall_grass", new DoublePlantBlock(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block LARGE_FERN = register(
		"large_fern", new DoublePlantBlock(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS))
	);
	public static final Block WHITE_BANNER = register(
		"white_banner", new BannerBlock(DyeColor.WHITE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block ORANGE_BANNER = register(
		"orange_banner", new BannerBlock(DyeColor.ORANGE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block MAGENTA_BANNER = register(
		"magenta_banner", new BannerBlock(DyeColor.MAGENTA, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block LIGHT_BLUE_BANNER = register(
		"light_blue_banner", new BannerBlock(DyeColor.LIGHT_BLUE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block YELLOW_BANNER = register(
		"yellow_banner", new BannerBlock(DyeColor.YELLOW, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block LIME_BANNER = register(
		"lime_banner", new BannerBlock(DyeColor.LIME, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block PINK_BANNER = register(
		"pink_banner", new BannerBlock(DyeColor.PINK, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block GRAY_BANNER = register(
		"gray_banner", new BannerBlock(DyeColor.GRAY, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block LIGHT_GRAY_BANNER = register(
		"light_gray_banner", new BannerBlock(DyeColor.LIGHT_GRAY, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block CYAN_BANNER = register(
		"cyan_banner", new BannerBlock(DyeColor.CYAN, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block PURPLE_BANNER = register(
		"purple_banner", new BannerBlock(DyeColor.PURPLE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block BLUE_BANNER = register(
		"blue_banner", new BannerBlock(DyeColor.BLUE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block BROWN_BANNER = register(
		"brown_banner", new BannerBlock(DyeColor.BROWN, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block GREEN_BANNER = register(
		"green_banner", new BannerBlock(DyeColor.GREEN, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block RED_BANNER = register(
		"red_banner", new BannerBlock(DyeColor.RED, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block BLACK_BANNER = register(
		"black_banner", new BannerBlock(DyeColor.BLACK, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block WHITE_WALL_BANNER = register(
		"white_wall_banner",
		new WallBannerBlock(DyeColor.WHITE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(WHITE_BANNER))
	);
	public static final Block ORANGE_WALL_BANNER = register(
		"orange_wall_banner",
		new WallBannerBlock(DyeColor.ORANGE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(ORANGE_BANNER))
	);
	public static final Block MAGENTA_WALL_BANNER = register(
		"magenta_wall_banner",
		new WallBannerBlock(DyeColor.MAGENTA, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(MAGENTA_BANNER))
	);
	public static final Block LIGHT_BLUE_WALL_BANNER = register(
		"light_blue_wall_banner",
		new WallBannerBlock(DyeColor.LIGHT_BLUE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(LIGHT_BLUE_BANNER))
	);
	public static final Block YELLOW_WALL_BANNER = register(
		"yellow_wall_banner",
		new WallBannerBlock(DyeColor.YELLOW, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(YELLOW_BANNER))
	);
	public static final Block LIME_WALL_BANNER = register(
		"lime_wall_banner",
		new WallBannerBlock(DyeColor.LIME, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(LIME_BANNER))
	);
	public static final Block PINK_WALL_BANNER = register(
		"pink_wall_banner",
		new WallBannerBlock(DyeColor.PINK, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(PINK_BANNER))
	);
	public static final Block GRAY_WALL_BANNER = register(
		"gray_wall_banner",
		new WallBannerBlock(DyeColor.GRAY, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(GRAY_BANNER))
	);
	public static final Block LIGHT_GRAY_WALL_BANNER = register(
		"light_gray_wall_banner",
		new WallBannerBlock(DyeColor.LIGHT_GRAY, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(LIGHT_GRAY_BANNER))
	);
	public static final Block CYAN_WALL_BANNER = register(
		"cyan_wall_banner",
		new WallBannerBlock(DyeColor.CYAN, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(CYAN_BANNER))
	);
	public static final Block PURPLE_WALL_BANNER = register(
		"purple_wall_banner",
		new WallBannerBlock(DyeColor.PURPLE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(PURPLE_BANNER))
	);
	public static final Block BLUE_WALL_BANNER = register(
		"blue_wall_banner",
		new WallBannerBlock(DyeColor.BLUE, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(BLUE_BANNER))
	);
	public static final Block BROWN_WALL_BANNER = register(
		"brown_wall_banner",
		new WallBannerBlock(DyeColor.BROWN, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(BROWN_BANNER))
	);
	public static final Block GREEN_WALL_BANNER = register(
		"green_wall_banner",
		new WallBannerBlock(DyeColor.GREEN, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(GREEN_BANNER))
	);
	public static final Block RED_WALL_BANNER = register(
		"red_wall_banner",
		new WallBannerBlock(DyeColor.RED, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(RED_BANNER))
	);
	public static final Block BLACK_WALL_BANNER = register(
		"black_wall_banner",
		new WallBannerBlock(DyeColor.BLACK, Block.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD).dropsLike(BLACK_BANNER))
	);
	public static final Block RED_SANDSTONE = register("red_sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_ORANGE).strength(0.8F)));
	public static final Block CHISELED_RED_SANDSTONE = register(
		"chiseled_red_sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_ORANGE).strength(0.8F))
	);
	public static final Block CUT_RED_SANDSTONE = register(
		"cut_red_sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_ORANGE).strength(0.8F))
	);
	public static final Block RED_SANDSTONE_STAIRS = register(
		"red_sandstone_stairs", new StairBlock(RED_SANDSTONE.defaultBlockState(), Block.Properties.copy(RED_SANDSTONE))
	);
	public static final Block OAK_SLAB = register(
		"oak_slab", new SlabBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block SPRUCE_SLAB = register(
		"spruce_slab", new SlabBlock(Block.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block BIRCH_SLAB = register(
		"birch_slab", new SlabBlock(Block.Properties.of(Material.WOOD, MaterialColor.SAND).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block JUNGLE_SLAB = register(
		"jungle_slab", new SlabBlock(Block.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block ACACIA_SLAB = register(
		"acacia_slab", new SlabBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_ORANGE).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block DARK_OAK_SLAB = register(
		"dark_oak_slab", new SlabBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block STONE_SLAB = register("stone_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2.0F, 6.0F)));
	public static final Block SMOOTH_STONE_SLAB = register(
		"smooth_stone_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2.0F, 6.0F))
	);
	public static final Block SANDSTONE_SLAB = register(
		"sandstone_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.SAND).strength(2.0F, 6.0F))
	);
	public static final Block CUT_SANDSTONE_SLAB = register(
		"cut_sandstone_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.SAND).strength(2.0F, 6.0F))
	);
	public static final Block PETRIFIED_OAK_SLAB = register(
		"petrified_oak_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.WOOD).strength(2.0F, 6.0F))
	);
	public static final Block COBBLESTONE_SLAB = register(
		"cobblestone_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2.0F, 6.0F))
	);
	public static final Block BRICK_SLAB = register("brick_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_RED).strength(2.0F, 6.0F)));
	public static final Block STONE_BRICK_SLAB = register(
		"stone_brick_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2.0F, 6.0F))
	);
	public static final Block NETHER_BRICK_SLAB = register(
		"nether_brick_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.NETHER).strength(2.0F, 6.0F))
	);
	public static final Block QUARTZ_SLAB = register("quartz_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.QUARTZ).strength(2.0F, 6.0F)));
	public static final Block RED_SANDSTONE_SLAB = register(
		"red_sandstone_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_ORANGE).strength(2.0F, 6.0F))
	);
	public static final Block CUT_RED_SANDSTONE_SLAB = register(
		"cut_red_sandstone_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_ORANGE).strength(2.0F, 6.0F))
	);
	public static final Block PURPUR_SLAB = register(
		"purpur_slab", new SlabBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_MAGENTA).strength(2.0F, 6.0F))
	);
	public static final Block SMOOTH_STONE = register("smooth_stone", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2.0F, 6.0F)));
	public static final Block SMOOTH_SANDSTONE = register(
		"smooth_sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.SAND).strength(2.0F, 6.0F))
	);
	public static final Block SMOOTH_QUARTZ = register("smooth_quartz", new Block(Block.Properties.of(Material.STONE, MaterialColor.QUARTZ).strength(2.0F, 6.0F)));
	public static final Block SMOOTH_RED_SANDSTONE = register(
		"smooth_red_sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_ORANGE).strength(2.0F, 6.0F))
	);
	public static final Block SPRUCE_FENCE_GATE = register(
		"spruce_fence_gate", new FenceGateBlock(Block.Properties.of(Material.WOOD, SPRUCE_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block BIRCH_FENCE_GATE = register(
		"birch_fence_gate", new FenceGateBlock(Block.Properties.of(Material.WOOD, BIRCH_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block JUNGLE_FENCE_GATE = register(
		"jungle_fence_gate", new FenceGateBlock(Block.Properties.of(Material.WOOD, JUNGLE_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block ACACIA_FENCE_GATE = register(
		"acacia_fence_gate", new FenceGateBlock(Block.Properties.of(Material.WOOD, ACACIA_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block DARK_OAK_FENCE_GATE = register(
		"dark_oak_fence_gate", new FenceGateBlock(Block.Properties.of(Material.WOOD, DARK_OAK_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block SPRUCE_FENCE = register(
		"spruce_fence", new FenceBlock(Block.Properties.of(Material.WOOD, SPRUCE_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block BIRCH_FENCE = register(
		"birch_fence", new FenceBlock(Block.Properties.of(Material.WOOD, BIRCH_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block JUNGLE_FENCE = register(
		"jungle_fence", new FenceBlock(Block.Properties.of(Material.WOOD, JUNGLE_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block ACACIA_FENCE = register(
		"acacia_fence", new FenceBlock(Block.Properties.of(Material.WOOD, ACACIA_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block DARK_OAK_FENCE = register(
		"dark_oak_fence", new FenceBlock(Block.Properties.of(Material.WOOD, DARK_OAK_PLANKS.materialColor).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	);
	public static final Block SPRUCE_DOOR = register(
		"spruce_door", new DoorBlock(Block.Properties.of(Material.WOOD, SPRUCE_PLANKS.materialColor).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block BIRCH_DOOR = register(
		"birch_door", new DoorBlock(Block.Properties.of(Material.WOOD, BIRCH_PLANKS.materialColor).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block JUNGLE_DOOR = register(
		"jungle_door", new DoorBlock(Block.Properties.of(Material.WOOD, JUNGLE_PLANKS.materialColor).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block ACACIA_DOOR = register(
		"acacia_door", new DoorBlock(Block.Properties.of(Material.WOOD, ACACIA_PLANKS.materialColor).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block DARK_OAK_DOOR = register(
		"dark_oak_door", new DoorBlock(Block.Properties.of(Material.WOOD, DARK_OAK_PLANKS.materialColor).strength(3.0F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block END_ROD = register(
		"end_rod", new EndRodBlock(Block.Properties.of(Material.DECORATION).instabreak().lightLevel(14).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block CHORUS_PLANT = register(
		"chorus_plant", new ChorusPlantBlock(Block.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE).strength(0.4F).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block CHORUS_FLOWER = register(
		"chorus_flower",
		new ChorusFlowerBlock(
			(ChorusPlantBlock)CHORUS_PLANT,
			Block.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE).randomTicks().strength(0.4F).sound(SoundType.WOOD).noOcclusion()
		)
	);
	public static final Block PURPUR_BLOCK = register(
		"purpur_block", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_MAGENTA).strength(1.5F, 6.0F))
	);
	public static final Block PURPUR_PILLAR = register(
		"purpur_pillar", new RotatedPillarBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_MAGENTA).strength(1.5F, 6.0F))
	);
	public static final Block PURPUR_STAIRS = register("purpur_stairs", new StairBlock(PURPUR_BLOCK.defaultBlockState(), Block.Properties.copy(PURPUR_BLOCK)));
	public static final Block END_STONE_BRICKS = register(
		"end_stone_bricks", new Block(Block.Properties.of(Material.STONE, MaterialColor.SAND).strength(3.0F, 9.0F))
	);
	public static final Block BEETROOTS = register(
		"beetroots", new BeetrootBlock(Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP))
	);
	public static final Block GRASS_PATH = register("grass_path", new GrassPathBlock(Block.Properties.of(Material.DIRT).strength(0.65F).sound(SoundType.GRASS)));
	public static final Block END_GATEWAY = register(
		"end_gateway",
		new EndGatewayBlock(Block.Properties.of(Material.PORTAL, MaterialColor.COLOR_BLACK).noCollission().lightLevel(15).strength(-1.0F, 3600000.0F).noDrops())
	);
	public static final Block REPEATING_COMMAND_BLOCK = register(
		"repeating_command_block", new CommandBlock(Block.Properties.of(Material.METAL, MaterialColor.COLOR_PURPLE).strength(-1.0F, 3600000.0F).noDrops())
	);
	public static final Block CHAIN_COMMAND_BLOCK = register(
		"chain_command_block", new CommandBlock(Block.Properties.of(Material.METAL, MaterialColor.COLOR_GREEN).strength(-1.0F, 3600000.0F).noDrops())
	);
	public static final Block FROSTED_ICE = register(
		"frosted_ice", new FrostedIceBlock(Block.Properties.of(Material.ICE).friction(0.98F).randomTicks().strength(0.5F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block MAGMA_BLOCK = register(
		"magma_block", new MagmaBlock(Block.Properties.of(Material.STONE, MaterialColor.NETHER).lightLevel(3).randomTicks().strength(0.5F))
	);
	public static final Block NETHER_WART_BLOCK = register(
		"nether_wart_block", new Block(Block.Properties.of(Material.GRASS, MaterialColor.COLOR_RED).strength(1.0F).sound(SoundType.WOOD))
	);
	public static final Block RED_NETHER_BRICKS = register(
		"red_nether_bricks", new Block(Block.Properties.of(Material.STONE, MaterialColor.NETHER).strength(2.0F, 6.0F))
	);
	public static final Block BONE_BLOCK = register("bone_block", new RotatedPillarBlock(Block.Properties.of(Material.STONE, MaterialColor.SAND).strength(2.0F)));
	public static final Block STRUCTURE_VOID = register(
		"structure_void", new StructureVoidBlock(Block.Properties.of(Material.STRUCTURAL_AIR).noCollission().noDrops())
	);
	public static final Block OBSERVER = register("observer", new ObserverBlock(Block.Properties.of(Material.STONE).strength(3.0F)));
	public static final Block SHULKER_BOX = register(
		"shulker_box", new ShulkerBoxBlock(null, Block.Properties.of(Material.SHULKER_SHELL).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block WHITE_SHULKER_BOX = register(
		"white_shulker_box",
		new ShulkerBoxBlock(DyeColor.WHITE, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.SNOW).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block ORANGE_SHULKER_BOX = register(
		"orange_shulker_box",
		new ShulkerBoxBlock(DyeColor.ORANGE, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_ORANGE).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block MAGENTA_SHULKER_BOX = register(
		"magenta_shulker_box",
		new ShulkerBoxBlock(DyeColor.MAGENTA, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_MAGENTA).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block LIGHT_BLUE_SHULKER_BOX = register(
		"light_blue_shulker_box",
		new ShulkerBoxBlock(
			DyeColor.LIGHT_BLUE, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_LIGHT_BLUE).strength(2.0F).dynamicShape().noOcclusion()
		)
	);
	public static final Block YELLOW_SHULKER_BOX = register(
		"yellow_shulker_box",
		new ShulkerBoxBlock(DyeColor.YELLOW, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_YELLOW).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block LIME_SHULKER_BOX = register(
		"lime_shulker_box",
		new ShulkerBoxBlock(DyeColor.LIME, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_LIGHT_GREEN).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block PINK_SHULKER_BOX = register(
		"pink_shulker_box",
		new ShulkerBoxBlock(DyeColor.PINK, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_PINK).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block GRAY_SHULKER_BOX = register(
		"gray_shulker_box",
		new ShulkerBoxBlock(DyeColor.GRAY, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_GRAY).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block LIGHT_GRAY_SHULKER_BOX = register(
		"light_gray_shulker_box",
		new ShulkerBoxBlock(
			DyeColor.LIGHT_GRAY, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_LIGHT_GRAY).strength(2.0F).dynamicShape().noOcclusion()
		)
	);
	public static final Block CYAN_SHULKER_BOX = register(
		"cyan_shulker_box",
		new ShulkerBoxBlock(DyeColor.CYAN, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_CYAN).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block PURPLE_SHULKER_BOX = register(
		"purple_shulker_box",
		new ShulkerBoxBlock(DyeColor.PURPLE, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.TERRACOTTA_PURPLE).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block BLUE_SHULKER_BOX = register(
		"blue_shulker_box",
		new ShulkerBoxBlock(DyeColor.BLUE, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_BLUE).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block BROWN_SHULKER_BOX = register(
		"brown_shulker_box",
		new ShulkerBoxBlock(DyeColor.BROWN, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_BROWN).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block GREEN_SHULKER_BOX = register(
		"green_shulker_box",
		new ShulkerBoxBlock(DyeColor.GREEN, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_GREEN).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block RED_SHULKER_BOX = register(
		"red_shulker_box",
		new ShulkerBoxBlock(DyeColor.RED, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_RED).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block BLACK_SHULKER_BOX = register(
		"black_shulker_box",
		new ShulkerBoxBlock(DyeColor.BLACK, Block.Properties.of(Material.SHULKER_SHELL, MaterialColor.COLOR_BLACK).strength(2.0F).dynamicShape().noOcclusion())
	);
	public static final Block WHITE_GLAZED_TERRACOTTA = register(
		"white_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.WHITE).strength(1.4F))
	);
	public static final Block ORANGE_GLAZED_TERRACOTTA = register(
		"orange_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.ORANGE).strength(1.4F))
	);
	public static final Block MAGENTA_GLAZED_TERRACOTTA = register(
		"magenta_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.MAGENTA).strength(1.4F))
	);
	public static final Block LIGHT_BLUE_GLAZED_TERRACOTTA = register(
		"light_blue_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.LIGHT_BLUE).strength(1.4F))
	);
	public static final Block YELLOW_GLAZED_TERRACOTTA = register(
		"yellow_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.YELLOW).strength(1.4F))
	);
	public static final Block LIME_GLAZED_TERRACOTTA = register(
		"lime_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.LIME).strength(1.4F))
	);
	public static final Block PINK_GLAZED_TERRACOTTA = register(
		"pink_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.PINK).strength(1.4F))
	);
	public static final Block GRAY_GLAZED_TERRACOTTA = register(
		"gray_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.GRAY).strength(1.4F))
	);
	public static final Block LIGHT_GRAY_GLAZED_TERRACOTTA = register(
		"light_gray_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.LIGHT_GRAY).strength(1.4F))
	);
	public static final Block CYAN_GLAZED_TERRACOTTA = register(
		"cyan_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.CYAN).strength(1.4F))
	);
	public static final Block PURPLE_GLAZED_TERRACOTTA = register(
		"purple_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.PURPLE).strength(1.4F))
	);
	public static final Block BLUE_GLAZED_TERRACOTTA = register(
		"blue_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.BLUE).strength(1.4F))
	);
	public static final Block BROWN_GLAZED_TERRACOTTA = register(
		"brown_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.BROWN).strength(1.4F))
	);
	public static final Block GREEN_GLAZED_TERRACOTTA = register(
		"green_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.GREEN).strength(1.4F))
	);
	public static final Block RED_GLAZED_TERRACOTTA = register(
		"red_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.RED).strength(1.4F))
	);
	public static final Block BLACK_GLAZED_TERRACOTTA = register(
		"black_glazed_terracotta", new GlazedTerracottaBlock(Block.Properties.of(Material.STONE, DyeColor.BLACK).strength(1.4F))
	);
	public static final Block WHITE_CONCRETE = register("white_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.WHITE).strength(1.8F)));
	public static final Block ORANGE_CONCRETE = register("orange_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.ORANGE).strength(1.8F)));
	public static final Block MAGENTA_CONCRETE = register("magenta_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.MAGENTA).strength(1.8F)));
	public static final Block LIGHT_BLUE_CONCRETE = register(
		"light_blue_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.LIGHT_BLUE).strength(1.8F))
	);
	public static final Block YELLOW_CONCRETE = register("yellow_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.YELLOW).strength(1.8F)));
	public static final Block LIME_CONCRETE = register("lime_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.LIME).strength(1.8F)));
	public static final Block PINK_CONCRETE = register("pink_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.PINK).strength(1.8F)));
	public static final Block GRAY_CONCRETE = register("gray_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.GRAY).strength(1.8F)));
	public static final Block LIGHT_GRAY_CONCRETE = register(
		"light_gray_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.LIGHT_GRAY).strength(1.8F))
	);
	public static final Block CYAN_CONCRETE = register("cyan_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.CYAN).strength(1.8F)));
	public static final Block PURPLE_CONCRETE = register("purple_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.PURPLE).strength(1.8F)));
	public static final Block BLUE_CONCRETE = register("blue_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.BLUE).strength(1.8F)));
	public static final Block BROWN_CONCRETE = register("brown_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.BROWN).strength(1.8F)));
	public static final Block GREEN_CONCRETE = register("green_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.GREEN).strength(1.8F)));
	public static final Block RED_CONCRETE = register("red_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.RED).strength(1.8F)));
	public static final Block BLACK_CONCRETE = register("black_concrete", new Block(Block.Properties.of(Material.STONE, DyeColor.BLACK).strength(1.8F)));
	public static final Block WHITE_CONCRETE_POWDER = register(
		"white_concrete_powder", new ConcretePowderBlock(WHITE_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.WHITE).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block ORANGE_CONCRETE_POWDER = register(
		"orange_concrete_powder", new ConcretePowderBlock(ORANGE_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.ORANGE).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block MAGENTA_CONCRETE_POWDER = register(
		"magenta_concrete_powder",
		new ConcretePowderBlock(MAGENTA_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.MAGENTA).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block LIGHT_BLUE_CONCRETE_POWDER = register(
		"light_blue_concrete_powder",
		new ConcretePowderBlock(LIGHT_BLUE_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.LIGHT_BLUE).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block YELLOW_CONCRETE_POWDER = register(
		"yellow_concrete_powder", new ConcretePowderBlock(YELLOW_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.YELLOW).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block LIME_CONCRETE_POWDER = register(
		"lime_concrete_powder", new ConcretePowderBlock(LIME_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.LIME).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block PINK_CONCRETE_POWDER = register(
		"pink_concrete_powder", new ConcretePowderBlock(PINK_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.PINK).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block GRAY_CONCRETE_POWDER = register(
		"gray_concrete_powder", new ConcretePowderBlock(GRAY_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.GRAY).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block LIGHT_GRAY_CONCRETE_POWDER = register(
		"light_gray_concrete_powder",
		new ConcretePowderBlock(LIGHT_GRAY_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.LIGHT_GRAY).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block CYAN_CONCRETE_POWDER = register(
		"cyan_concrete_powder", new ConcretePowderBlock(CYAN_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.CYAN).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block PURPLE_CONCRETE_POWDER = register(
		"purple_concrete_powder", new ConcretePowderBlock(PURPLE_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.PURPLE).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block BLUE_CONCRETE_POWDER = register(
		"blue_concrete_powder", new ConcretePowderBlock(BLUE_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.BLUE).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block BROWN_CONCRETE_POWDER = register(
		"brown_concrete_powder", new ConcretePowderBlock(BROWN_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.BROWN).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block GREEN_CONCRETE_POWDER = register(
		"green_concrete_powder", new ConcretePowderBlock(GREEN_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.GREEN).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block RED_CONCRETE_POWDER = register(
		"red_concrete_powder", new ConcretePowderBlock(RED_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.RED).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block BLACK_CONCRETE_POWDER = register(
		"black_concrete_powder", new ConcretePowderBlock(BLACK_CONCRETE, Block.Properties.of(Material.SAND, DyeColor.BLACK).strength(0.5F).sound(SoundType.SAND))
	);
	public static final Block KELP = register(
		"kelp", new KelpBlock(Block.Properties.of(Material.WATER_PLANT).noCollission().randomTicks().instabreak().sound(SoundType.WET_GRASS))
	);
	public static final Block KELP_PLANT = register(
		"kelp_plant", new KelpPlantBlock((KelpBlock)KELP, Block.Properties.of(Material.WATER_PLANT).noCollission().instabreak().sound(SoundType.WET_GRASS))
	);
	public static final Block DRIED_KELP_BLOCK = register(
		"dried_kelp_block", new Block(Block.Properties.of(Material.GRASS, MaterialColor.COLOR_GREEN).strength(0.5F, 2.5F).sound(SoundType.GRASS))
	);
	public static final Block TURTLE_EGG = register(
		"turtle_egg", new TurtleEggBlock(Block.Properties.of(Material.EGG, MaterialColor.SAND).strength(0.5F).sound(SoundType.METAL).randomTicks().noOcclusion())
	);
	public static final Block DEAD_TUBE_CORAL_BLOCK = register(
		"dead_tube_coral_block", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).strength(1.5F, 6.0F))
	);
	public static final Block DEAD_BRAIN_CORAL_BLOCK = register(
		"dead_brain_coral_block", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).strength(1.5F, 6.0F))
	);
	public static final Block DEAD_BUBBLE_CORAL_BLOCK = register(
		"dead_bubble_coral_block", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).strength(1.5F, 6.0F))
	);
	public static final Block DEAD_FIRE_CORAL_BLOCK = register(
		"dead_fire_coral_block", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).strength(1.5F, 6.0F))
	);
	public static final Block DEAD_HORN_CORAL_BLOCK = register(
		"dead_horn_coral_block", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).strength(1.5F, 6.0F))
	);
	public static final Block TUBE_CORAL_BLOCK = register(
		"tube_coral_block",
		new CoralBlock(DEAD_TUBE_CORAL_BLOCK, Block.Properties.of(Material.STONE, MaterialColor.COLOR_BLUE).strength(1.5F, 6.0F).sound(SoundType.CORAL_BLOCK))
	);
	public static final Block BRAIN_CORAL_BLOCK = register(
		"brain_coral_block",
		new CoralBlock(DEAD_BRAIN_CORAL_BLOCK, Block.Properties.of(Material.STONE, MaterialColor.COLOR_PINK).strength(1.5F, 6.0F).sound(SoundType.CORAL_BLOCK))
	);
	public static final Block BUBBLE_CORAL_BLOCK = register(
		"bubble_coral_block",
		new CoralBlock(DEAD_BUBBLE_CORAL_BLOCK, Block.Properties.of(Material.STONE, MaterialColor.COLOR_PURPLE).strength(1.5F, 6.0F).sound(SoundType.CORAL_BLOCK))
	);
	public static final Block FIRE_CORAL_BLOCK = register(
		"fire_coral_block",
		new CoralBlock(DEAD_FIRE_CORAL_BLOCK, Block.Properties.of(Material.STONE, MaterialColor.COLOR_RED).strength(1.5F, 6.0F).sound(SoundType.CORAL_BLOCK))
	);
	public static final Block HORN_CORAL_BLOCK = register(
		"horn_coral_block",
		new CoralBlock(DEAD_HORN_CORAL_BLOCK, Block.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW).strength(1.5F, 6.0F).sound(SoundType.CORAL_BLOCK))
	);
	public static final Block DEAD_TUBE_CORAL = register(
		"dead_tube_coral", new BaseCoralPlantBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block DEAD_BRAIN_CORAL = register(
		"dead_brain_coral", new BaseCoralPlantBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block DEAD_BUBBLE_CORAL = register(
		"dead_bubble_coral", new BaseCoralPlantBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block DEAD_FIRE_CORAL = register(
		"dead_fire_coral", new BaseCoralPlantBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block DEAD_HORN_CORAL = register(
		"dead_horn_coral", new BaseCoralPlantBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block TUBE_CORAL = register(
		"tube_coral",
		new CoralPlantBlock(
			DEAD_TUBE_CORAL, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_BLUE).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block BRAIN_CORAL = register(
		"brain_coral",
		new CoralPlantBlock(
			DEAD_BRAIN_CORAL, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block BUBBLE_CORAL = register(
		"bubble_coral",
		new CoralPlantBlock(
			DEAD_BUBBLE_CORAL, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block FIRE_CORAL = register(
		"fire_coral",
		new CoralPlantBlock(
			DEAD_FIRE_CORAL, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_RED).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block HORN_CORAL = register(
		"horn_coral",
		new CoralPlantBlock(
			DEAD_HORN_CORAL, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block DEAD_TUBE_CORAL_FAN = register(
		"dead_tube_coral_fan", new BaseCoralFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block DEAD_BRAIN_CORAL_FAN = register(
		"dead_brain_coral_fan", new BaseCoralFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block DEAD_BUBBLE_CORAL_FAN = register(
		"dead_bubble_coral_fan", new BaseCoralFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block DEAD_FIRE_CORAL_FAN = register(
		"dead_fire_coral_fan", new BaseCoralFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block DEAD_HORN_CORAL_FAN = register(
		"dead_horn_coral_fan", new BaseCoralFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak())
	);
	public static final Block TUBE_CORAL_FAN = register(
		"tube_coral_fan",
		new CoralFanBlock(
			DEAD_TUBE_CORAL_FAN, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_BLUE).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block BRAIN_CORAL_FAN = register(
		"brain_coral_fan",
		new CoralFanBlock(
			DEAD_BRAIN_CORAL_FAN, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block BUBBLE_CORAL_FAN = register(
		"bubble_coral_fan",
		new CoralFanBlock(
			DEAD_BUBBLE_CORAL_FAN, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block FIRE_CORAL_FAN = register(
		"fire_coral_fan",
		new CoralFanBlock(
			DEAD_FIRE_CORAL_FAN, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_RED).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block HORN_CORAL_FAN = register(
		"horn_coral_fan",
		new CoralFanBlock(
			DEAD_HORN_CORAL_FAN, Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundType.WET_GRASS)
		)
	);
	public static final Block DEAD_TUBE_CORAL_WALL_FAN = register(
		"dead_tube_coral_wall_fan",
		new BaseCoralWallFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak().dropsLike(DEAD_TUBE_CORAL_FAN))
	);
	public static final Block DEAD_BRAIN_CORAL_WALL_FAN = register(
		"dead_brain_coral_wall_fan",
		new BaseCoralWallFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak().dropsLike(DEAD_BRAIN_CORAL_FAN))
	);
	public static final Block DEAD_BUBBLE_CORAL_WALL_FAN = register(
		"dead_bubble_coral_wall_fan",
		new BaseCoralWallFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak().dropsLike(DEAD_BUBBLE_CORAL_FAN))
	);
	public static final Block DEAD_FIRE_CORAL_WALL_FAN = register(
		"dead_fire_coral_wall_fan",
		new BaseCoralWallFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak().dropsLike(DEAD_FIRE_CORAL_FAN))
	);
	public static final Block DEAD_HORN_CORAL_WALL_FAN = register(
		"dead_horn_coral_wall_fan",
		new BaseCoralWallFanBlock(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).noCollission().instabreak().dropsLike(DEAD_HORN_CORAL_FAN))
	);
	public static final Block TUBE_CORAL_WALL_FAN = register(
		"tube_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_TUBE_CORAL_WALL_FAN,
			Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_BLUE).noCollission().instabreak().sound(SoundType.WET_GRASS).dropsLike(TUBE_CORAL_FAN)
		)
	);
	public static final Block BRAIN_CORAL_WALL_FAN = register(
		"brain_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_BRAIN_CORAL_WALL_FAN,
			Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.WET_GRASS).dropsLike(BRAIN_CORAL_FAN)
		)
	);
	public static final Block BUBBLE_CORAL_WALL_FAN = register(
		"bubble_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_BUBBLE_CORAL_WALL_FAN,
			Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundType.WET_GRASS).dropsLike(BUBBLE_CORAL_FAN)
		)
	);
	public static final Block FIRE_CORAL_WALL_FAN = register(
		"fire_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_FIRE_CORAL_WALL_FAN,
			Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_RED).noCollission().instabreak().sound(SoundType.WET_GRASS).dropsLike(FIRE_CORAL_FAN)
		)
	);
	public static final Block HORN_CORAL_WALL_FAN = register(
		"horn_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_HORN_CORAL_WALL_FAN,
			Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundType.WET_GRASS).dropsLike(HORN_CORAL_FAN)
		)
	);
	public static final Block SEA_PICKLE = register(
		"sea_pickle",
		new SeaPickleBlock(Block.Properties.of(Material.WATER_PLANT, MaterialColor.COLOR_GREEN).lightLevel(3).sound(SoundType.SLIME_BLOCK).noOcclusion())
	);
	public static final Block BLUE_ICE = register(
		"blue_ice", new HalfTransparentBlock(Block.Properties.of(Material.ICE_SOLID).strength(2.8F).friction(0.989F).sound(SoundType.GLASS))
	);
	public static final Block CONDUIT = register(
		"conduit", new ConduitBlock(Block.Properties.of(Material.GLASS, MaterialColor.DIAMOND).strength(3.0F).lightLevel(15).noOcclusion())
	);
	public static final Block BAMBOO_SAPLING = register(
		"bamboo_sapling",
		new BambooSaplingBlock(Block.Properties.of(Material.BAMBOO_SAPLING).randomTicks().instabreak().noCollission().strength(1.0F).sound(SoundType.BAMBOO_SAPLING))
	);
	public static final Block BAMBOO = register(
		"bamboo",
		new BambooBlock(Block.Properties.of(Material.BAMBOO, MaterialColor.PLANT).randomTicks().instabreak().strength(1.0F).sound(SoundType.BAMBOO).noOcclusion())
	);
	public static final Block POTTED_BAMBOO = register(
		"potted_bamboo", new FlowerPotBlock(BAMBOO, Block.Properties.of(Material.DECORATION).instabreak().noOcclusion())
	);
	public static final Block VOID_AIR = register("void_air", new AirBlock(Block.Properties.of(Material.AIR).noCollission().noDrops()));
	public static final Block CAVE_AIR = register("cave_air", new AirBlock(Block.Properties.of(Material.AIR).noCollission().noDrops()));
	public static final Block BUBBLE_COLUMN = register(
		"bubble_column", new BubbleColumnBlock(Block.Properties.of(Material.BUBBLE_COLUMN).noCollission().noDrops())
	);
	public static final Block POLISHED_GRANITE_STAIRS = register(
		"polished_granite_stairs", new StairBlock(POLISHED_GRANITE.defaultBlockState(), Block.Properties.copy(POLISHED_GRANITE))
	);
	public static final Block SMOOTH_RED_SANDSTONE_STAIRS = register(
		"smooth_red_sandstone_stairs", new StairBlock(SMOOTH_RED_SANDSTONE.defaultBlockState(), Block.Properties.copy(SMOOTH_RED_SANDSTONE))
	);
	public static final Block MOSSY_STONE_BRICK_STAIRS = register(
		"mossy_stone_brick_stairs", new StairBlock(MOSSY_STONE_BRICKS.defaultBlockState(), Block.Properties.copy(MOSSY_STONE_BRICKS))
	);
	public static final Block POLISHED_DIORITE_STAIRS = register(
		"polished_diorite_stairs", new StairBlock(POLISHED_DIORITE.defaultBlockState(), Block.Properties.copy(POLISHED_DIORITE))
	);
	public static final Block MOSSY_COBBLESTONE_STAIRS = register(
		"mossy_cobblestone_stairs", new StairBlock(MOSSY_COBBLESTONE.defaultBlockState(), Block.Properties.copy(MOSSY_COBBLESTONE))
	);
	public static final Block END_STONE_BRICK_STAIRS = register(
		"end_stone_brick_stairs", new StairBlock(END_STONE_BRICKS.defaultBlockState(), Block.Properties.copy(END_STONE_BRICKS))
	);
	public static final Block STONE_STAIRS = register("stone_stairs", new StairBlock(STONE.defaultBlockState(), Block.Properties.copy(STONE)));
	public static final Block SMOOTH_SANDSTONE_STAIRS = register(
		"smooth_sandstone_stairs", new StairBlock(SMOOTH_SANDSTONE.defaultBlockState(), Block.Properties.copy(SMOOTH_SANDSTONE))
	);
	public static final Block SMOOTH_QUARTZ_STAIRS = register(
		"smooth_quartz_stairs", new StairBlock(SMOOTH_QUARTZ.defaultBlockState(), Block.Properties.copy(SMOOTH_QUARTZ))
	);
	public static final Block GRANITE_STAIRS = register("granite_stairs", new StairBlock(GRANITE.defaultBlockState(), Block.Properties.copy(GRANITE)));
	public static final Block ANDESITE_STAIRS = register("andesite_stairs", new StairBlock(ANDESITE.defaultBlockState(), Block.Properties.copy(ANDESITE)));
	public static final Block RED_NETHER_BRICK_STAIRS = register(
		"red_nether_brick_stairs", new StairBlock(RED_NETHER_BRICKS.defaultBlockState(), Block.Properties.copy(RED_NETHER_BRICKS))
	);
	public static final Block POLISHED_ANDESITE_STAIRS = register(
		"polished_andesite_stairs", new StairBlock(POLISHED_ANDESITE.defaultBlockState(), Block.Properties.copy(POLISHED_ANDESITE))
	);
	public static final Block DIORITE_STAIRS = register("diorite_stairs", new StairBlock(DIORITE.defaultBlockState(), Block.Properties.copy(DIORITE)));
	public static final Block POLISHED_GRANITE_SLAB = register("polished_granite_slab", new SlabBlock(Block.Properties.copy(POLISHED_GRANITE)));
	public static final Block SMOOTH_RED_SANDSTONE_SLAB = register("smooth_red_sandstone_slab", new SlabBlock(Block.Properties.copy(SMOOTH_RED_SANDSTONE)));
	public static final Block MOSSY_STONE_BRICK_SLAB = register("mossy_stone_brick_slab", new SlabBlock(Block.Properties.copy(MOSSY_STONE_BRICKS)));
	public static final Block POLISHED_DIORITE_SLAB = register("polished_diorite_slab", new SlabBlock(Block.Properties.copy(POLISHED_DIORITE)));
	public static final Block MOSSY_COBBLESTONE_SLAB = register("mossy_cobblestone_slab", new SlabBlock(Block.Properties.copy(MOSSY_COBBLESTONE)));
	public static final Block END_STONE_BRICK_SLAB = register("end_stone_brick_slab", new SlabBlock(Block.Properties.copy(END_STONE_BRICKS)));
	public static final Block SMOOTH_SANDSTONE_SLAB = register("smooth_sandstone_slab", new SlabBlock(Block.Properties.copy(SMOOTH_SANDSTONE)));
	public static final Block SMOOTH_QUARTZ_SLAB = register("smooth_quartz_slab", new SlabBlock(Block.Properties.copy(SMOOTH_QUARTZ)));
	public static final Block GRANITE_SLAB = register("granite_slab", new SlabBlock(Block.Properties.copy(GRANITE)));
	public static final Block ANDESITE_SLAB = register("andesite_slab", new SlabBlock(Block.Properties.copy(ANDESITE)));
	public static final Block RED_NETHER_BRICK_SLAB = register("red_nether_brick_slab", new SlabBlock(Block.Properties.copy(RED_NETHER_BRICKS)));
	public static final Block POLISHED_ANDESITE_SLAB = register("polished_andesite_slab", new SlabBlock(Block.Properties.copy(POLISHED_ANDESITE)));
	public static final Block DIORITE_SLAB = register("diorite_slab", new SlabBlock(Block.Properties.copy(DIORITE)));
	public static final Block BRICK_WALL = register("brick_wall", new WallBlock(Block.Properties.copy(BRICKS)));
	public static final Block PRISMARINE_WALL = register("prismarine_wall", new WallBlock(Block.Properties.copy(PRISMARINE)));
	public static final Block RED_SANDSTONE_WALL = register("red_sandstone_wall", new WallBlock(Block.Properties.copy(RED_SANDSTONE)));
	public static final Block MOSSY_STONE_BRICK_WALL = register("mossy_stone_brick_wall", new WallBlock(Block.Properties.copy(MOSSY_STONE_BRICKS)));
	public static final Block GRANITE_WALL = register("granite_wall", new WallBlock(Block.Properties.copy(GRANITE)));
	public static final Block STONE_BRICK_WALL = register("stone_brick_wall", new WallBlock(Block.Properties.copy(STONE_BRICKS)));
	public static final Block NETHER_BRICK_WALL = register("nether_brick_wall", new WallBlock(Block.Properties.copy(NETHER_BRICKS)));
	public static final Block ANDESITE_WALL = register("andesite_wall", new WallBlock(Block.Properties.copy(ANDESITE)));
	public static final Block RED_NETHER_BRICK_WALL = register("red_nether_brick_wall", new WallBlock(Block.Properties.copy(RED_NETHER_BRICKS)));
	public static final Block SANDSTONE_WALL = register("sandstone_wall", new WallBlock(Block.Properties.copy(SANDSTONE)));
	public static final Block END_STONE_BRICK_WALL = register("end_stone_brick_wall", new WallBlock(Block.Properties.copy(END_STONE_BRICKS)));
	public static final Block DIORITE_WALL = register("diorite_wall", new WallBlock(Block.Properties.copy(DIORITE)));
	public static final Block SCAFFOLDING = register(
		"scaffolding", new ScaffoldingBlock(Block.Properties.of(Material.DECORATION, MaterialColor.SAND).noCollission().sound(SoundType.SCAFFOLDING).dynamicShape())
	);
	public static final Block LOOM = register("loom", new LoomBlock(Block.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final Block BARREL = register("barrel", new BarrelBlock(Block.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final Block SMOKER = register("smoker", new SmokerBlock(Block.Properties.of(Material.STONE).strength(3.5F).lightLevel(13)));
	public static final Block BLAST_FURNACE = register("blast_furnace", new BlastFurnaceBlock(Block.Properties.of(Material.STONE).strength(3.5F).lightLevel(13)));
	public static final Block CARTOGRAPHY_TABLE = register(
		"cartography_table", new CartographyTableBlock(Block.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD))
	);
	public static final Block FLETCHING_TABLE = register(
		"fletching_table", new FletchingTableBlock(Block.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD))
	);
	public static final Block GRINDSTONE = register(
		"grindstone", new GrindstoneBlock(Block.Properties.of(Material.HEAVY_METAL, MaterialColor.METAL).strength(2.0F, 6.0F).sound(SoundType.STONE))
	);
	public static final Block LECTERN = register("lectern", new LecternBlock(Block.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final Block SMITHING_TABLE = register(
		"smithing_table", new SmithingTableBlock(Block.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD))
	);
	public static final Block STONECUTTER = register("stonecutter", new StonecutterBlock(Block.Properties.of(Material.STONE).strength(3.5F)));
	public static final Block BELL = register("bell", new BellBlock(Block.Properties.of(Material.METAL, MaterialColor.GOLD).strength(5.0F).sound(SoundType.ANVIL)));
	public static final Block LANTERN = register(
		"lantern", new Lantern(Block.Properties.of(Material.METAL).strength(3.5F).sound(SoundType.LANTERN).lightLevel(15).noOcclusion())
	);
	public static final Block CAMPFIRE = register(
		"campfire",
		new CampfireBlock(Block.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F).sound(SoundType.WOOD).lightLevel(15).randomTicks().noOcclusion())
	);
	public static final Block SWEET_BERRY_BUSH = register(
		"sweet_berry_bush", new SweetBerryBushBlock(Block.Properties.of(Material.PLANT).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH))
	);
	public static final Block STRUCTURE_BLOCK = register(
		"structure_block", new StructureBlock(Block.Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3600000.0F).noDrops())
	);
	public static final Block JIGSAW = register(
		"jigsaw", new JigsawBlock(Block.Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3600000.0F).noDrops())
	);
	public static final Block COMPOSTER = register("composter", new ComposterBlock(Block.Properties.of(Material.WOOD).strength(0.6F).sound(SoundType.WOOD)));
	public static final Block BEE_NEST = register("bee_nest", new BeehiveBlock(Block.Properties.of(Material.WOOD).strength(0.3F).sound(SoundType.WOOD)));
	public static final Block BEEHIVE = register("beehive", new BeehiveBlock(Block.Properties.of(Material.WOOD).strength(0.6F).sound(SoundType.WOOD)));
	public static final Block HONEY_BLOCK = register(
		"honey_block",
		new HoneyBlock(Block.Properties.of(Material.CLAY, MaterialColor.COLOR_ORANGE).speedFactor(0.4F).jumpFactor(0.5F).noOcclusion().sound(SoundType.HONEY_BLOCK))
	);
	public static final Block HONEYCOMB_BLOCK = register(
		"honeycomb_block", new Block(Block.Properties.of(Material.CLAY, MaterialColor.COLOR_ORANGE).strength(0.6F).sound(SoundType.CORAL_BLOCK))
	);

	private static Block register(String string, Block block) {
		return Registry.register(Registry.BLOCK, string, block);
	}

	static {
		for (Block block : Registry.BLOCK) {
			for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
				blockState.initCache();
				Block.BLOCK_STATE_REGISTRY.add(blockState);
			}

			block.getLootTable();
		}
	}
}
