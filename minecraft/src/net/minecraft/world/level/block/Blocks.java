package net.minecraft.world.level.block;

import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.references.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class Blocks {
	private static final BlockBehaviour.StatePredicate NOT_CLOSED_SHULKER = (blockStatex, blockGetter, blockPos) -> blockGetter.getBlockEntity(blockPos) instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity
			? shulkerBoxBlockEntity.isClosed()
			: true;
	private static final BlockBehaviour.StatePredicate NOT_EXTENDED_PISTON = (blockStatex, blockGetter, blockPos) -> !(Boolean)blockStatex.getValue(
			PistonBaseBlock.EXTENDED
		);
	public static final Block AIR = register("air", AirBlock::new, BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().air());
	public static final Block STONE = register(
		"stone", BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block GRANITE = register(
		"granite", BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block POLISHED_GRANITE = register(
		"polished_granite",
		BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block DIORITE = register(
		"diorite",
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block POLISHED_DIORITE = register(
		"polished_diorite",
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block ANDESITE = register(
		"andesite",
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block POLISHED_ANDESITE = register(
		"polished_andesite",
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block GRASS_BLOCK = register(
		"grass_block", GrassBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS)
	);
	public static final Block DIRT = register("dirt", BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL));
	public static final Block COARSE_DIRT = register("coarse_dirt", BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL));
	public static final Block PODZOL = register(
		"podzol", SnowyDirtBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).strength(0.5F).sound(SoundType.GRAVEL)
	);
	public static final Block COBBLESTONE = register(
		"cobblestone",
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block OAK_PLANKS = register(
		"oak_planks",
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block SPRUCE_PLANKS = register(
		"spruce_planks",
		BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block BIRCH_PLANKS = register(
		"birch_planks",
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block JUNGLE_PLANKS = register(
		"jungle_planks",
		BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block ACACIA_PLANKS = register(
		"acacia_planks",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_ORANGE)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block CHERRY_PLANKS = register(
		"cherry_planks",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_WHITE)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.CHERRY_WOOD)
			.ignitedByLava()
	);
	public static final Block DARK_OAK_PLANKS = register(
		"dark_oak_planks",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block MANGROVE_PLANKS = register(
		"mangrove_planks",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block BAMBOO_PLANKS = register(
		"bamboo_planks",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_YELLOW)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.BAMBOO_WOOD)
			.ignitedByLava()
	);
	public static final Block BAMBOO_MOSAIC = register(
		"bamboo_mosaic",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_YELLOW)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.BAMBOO_WOOD)
			.ignitedByLava()
	);
	public static final Block OAK_SAPLING = register(
		"oak_sapling",
		properties -> new SaplingBlock(TreeGrower.OAK, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block SPRUCE_SAPLING = register(
		"spruce_sapling",
		properties -> new SaplingBlock(TreeGrower.SPRUCE, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block BIRCH_SAPLING = register(
		"birch_sapling",
		properties -> new SaplingBlock(TreeGrower.BIRCH, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block JUNGLE_SAPLING = register(
		"jungle_sapling",
		properties -> new SaplingBlock(TreeGrower.JUNGLE, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block ACACIA_SAPLING = register(
		"acacia_sapling",
		properties -> new SaplingBlock(TreeGrower.ACACIA, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block CHERRY_SAPLING = register(
		"cherry_sapling",
		properties -> new SaplingBlock(TreeGrower.CHERRY, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PINK)
			.noCollission()
			.randomTicks()
			.instabreak()
			.sound(SoundType.CHERRY_SAPLING)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block DARK_OAK_SAPLING = register(
		"dark_oak_sapling",
		properties -> new SaplingBlock(TreeGrower.DARK_OAK, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block MANGROVE_PROPAGULE = register(
		"mangrove_propagule",
		properties -> new MangrovePropaguleBlock(TreeGrower.MANGROVE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.randomTicks()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BEDROCK = register(
		"bedrock",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.strength(-1.0F, 3600000.0F)
			.noLootTable()
			.isValidSpawn(Blocks::never)
	);
	public static final Block WATER = register(
		"water",
		properties -> new LiquidBlock(Fluids.WATER, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WATER)
			.replaceable()
			.noCollission()
			.strength(100.0F)
			.pushReaction(PushReaction.DESTROY)
			.noLootTable()
			.liquid()
			.sound(SoundType.EMPTY)
	);
	public static final Block LAVA = register(
		"lava",
		properties -> new LiquidBlock(Fluids.LAVA, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.FIRE)
			.replaceable()
			.noCollission()
			.randomTicks()
			.strength(100.0F)
			.lightLevel(blockStatex -> 15)
			.pushReaction(PushReaction.DESTROY)
			.noLootTable()
			.liquid()
			.sound(SoundType.EMPTY)
	);
	public static final Block SAND = register(
		"sand",
		properties -> new ColoredFallingBlock(new ColorRGBA(14406560), properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block SUSPICIOUS_SAND = register(
		"suspicious_sand",
		properties -> new BrushableBlock(SAND, SoundEvents.BRUSH_SAND, SoundEvents.BRUSH_SAND, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.SAND)
			.instrument(NoteBlockInstrument.SNARE)
			.strength(0.25F)
			.sound(SoundType.SUSPICIOUS_SAND)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block RED_SAND = register(
		"red_sand",
		properties -> new ColoredFallingBlock(new ColorRGBA(11098145), properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block GRAVEL = register(
		"gravel",
		properties -> new ColoredFallingBlock(new ColorRGBA(-8356741), properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.SNARE).strength(0.6F).sound(SoundType.GRAVEL)
	);
	public static final Block SUSPICIOUS_GRAVEL = register(
		"suspicious_gravel",
		properties -> new BrushableBlock(GRAVEL, SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.SNARE)
			.strength(0.25F)
			.sound(SoundType.SUSPICIOUS_GRAVEL)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block GOLD_ORE = register(
		"gold_ore",
		properties -> new DropExperienceBlock(ConstantInt.of(0), properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
	);
	public static final Block DEEPSLATE_GOLD_ORE = register(
		"deepslate_gold_ore",
		properties -> new DropExperienceBlock(ConstantInt.of(0), properties),
		BlockBehaviour.Properties.ofLegacyCopy(GOLD_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
	);
	public static final Block IRON_ORE = register(
		"iron_ore",
		properties -> new DropExperienceBlock(ConstantInt.of(0), properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
	);
	public static final Block DEEPSLATE_IRON_ORE = register(
		"deepslate_iron_ore",
		properties -> new DropExperienceBlock(ConstantInt.of(0), properties),
		BlockBehaviour.Properties.ofLegacyCopy(IRON_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
	);
	public static final Block COAL_ORE = register(
		"coal_ore",
		properties -> new DropExperienceBlock(UniformInt.of(0, 2), properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
	);
	public static final Block DEEPSLATE_COAL_ORE = register(
		"deepslate_coal_ore",
		properties -> new DropExperienceBlock(UniformInt.of(0, 2), properties),
		BlockBehaviour.Properties.ofLegacyCopy(COAL_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
	);
	public static final Block NETHER_GOLD_ORE = register(
		"nether_gold_ore",
		properties -> new DropExperienceBlock(UniformInt.of(0, 1), properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(3.0F, 3.0F)
			.sound(SoundType.NETHER_GOLD_ORE)
	);
	public static final Block OAK_LOG = register("oak_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.PODZOL, SoundType.WOOD));
	public static final Block SPRUCE_LOG = register("spruce_log", RotatedPillarBlock::new, logProperties(MapColor.PODZOL, MapColor.COLOR_BROWN, SoundType.WOOD));
	public static final Block BIRCH_LOG = register("birch_log", RotatedPillarBlock::new, logProperties(MapColor.SAND, MapColor.QUARTZ, SoundType.WOOD));
	public static final Block JUNGLE_LOG = register("jungle_log", RotatedPillarBlock::new, logProperties(MapColor.DIRT, MapColor.PODZOL, SoundType.WOOD));
	public static final Block ACACIA_LOG = register("acacia_log", RotatedPillarBlock::new, logProperties(MapColor.COLOR_ORANGE, MapColor.STONE, SoundType.WOOD));
	public static final Block CHERRY_LOG = register(
		"cherry_log", RotatedPillarBlock::new, logProperties(MapColor.TERRACOTTA_WHITE, MapColor.TERRACOTTA_GRAY, SoundType.CHERRY_WOOD)
	);
	public static final Block DARK_OAK_LOG = register(
		"dark_oak_log", RotatedPillarBlock::new, logProperties(MapColor.COLOR_BROWN, MapColor.COLOR_BROWN, SoundType.WOOD)
	);
	public static final Block MANGROVE_LOG = register("mangrove_log", RotatedPillarBlock::new, logProperties(MapColor.COLOR_RED, MapColor.PODZOL, SoundType.WOOD));
	public static final Block MANGROVE_ROOTS = register(
		"mangrove_roots",
		MangroveRootsBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PODZOL)
			.instrument(NoteBlockInstrument.BASS)
			.strength(0.7F)
			.sound(SoundType.MANGROVE_ROOTS)
			.noOcclusion()
			.isSuffocating(Blocks::never)
			.isViewBlocking(Blocks::never)
			.noOcclusion()
			.ignitedByLava()
	);
	public static final Block MUDDY_MANGROVE_ROOTS = register(
		"muddy_mangrove_roots",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).strength(0.7F).sound(SoundType.MUDDY_MANGROVE_ROOTS)
	);
	public static final Block BAMBOO_BLOCK = register(
		"bamboo_block", RotatedPillarBlock::new, logProperties(MapColor.COLOR_YELLOW, MapColor.PLANT, SoundType.BAMBOO_WOOD)
	);
	public static final Block STRIPPED_SPRUCE_LOG = register(
		"stripped_spruce_log", RotatedPillarBlock::new, logProperties(MapColor.PODZOL, MapColor.PODZOL, SoundType.WOOD)
	);
	public static final Block STRIPPED_BIRCH_LOG = register(
		"stripped_birch_log", RotatedPillarBlock::new, logProperties(MapColor.SAND, MapColor.SAND, SoundType.WOOD)
	);
	public static final Block STRIPPED_JUNGLE_LOG = register(
		"stripped_jungle_log", RotatedPillarBlock::new, logProperties(MapColor.DIRT, MapColor.DIRT, SoundType.WOOD)
	);
	public static final Block STRIPPED_ACACIA_LOG = register(
		"stripped_acacia_log", RotatedPillarBlock::new, logProperties(MapColor.COLOR_ORANGE, MapColor.COLOR_ORANGE, SoundType.WOOD)
	);
	public static final Block STRIPPED_CHERRY_LOG = register(
		"stripped_cherry_log", RotatedPillarBlock::new, logProperties(MapColor.TERRACOTTA_WHITE, MapColor.TERRACOTTA_PINK, SoundType.CHERRY_WOOD)
	);
	public static final Block STRIPPED_DARK_OAK_LOG = register(
		"stripped_dark_oak_log", RotatedPillarBlock::new, logProperties(MapColor.COLOR_BROWN, MapColor.COLOR_BROWN, SoundType.WOOD)
	);
	public static final Block STRIPPED_OAK_LOG = register("stripped_oak_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
	public static final Block STRIPPED_MANGROVE_LOG = register(
		"stripped_mangrove_log", RotatedPillarBlock::new, logProperties(MapColor.COLOR_RED, MapColor.COLOR_RED, SoundType.WOOD)
	);
	public static final Block STRIPPED_BAMBOO_BLOCK = register(
		"stripped_bamboo_block", RotatedPillarBlock::new, logProperties(MapColor.COLOR_YELLOW, MapColor.COLOR_YELLOW, SoundType.BAMBOO_WOOD)
	);
	public static final Block OAK_WOOD = register(
		"oak_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block SPRUCE_WOOD = register(
		"spruce_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block BIRCH_WOOD = register(
		"birch_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block JUNGLE_WOOD = register(
		"jungle_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block ACACIA_WOOD = register(
		"acacia_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block CHERRY_WOOD = register(
		"cherry_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_GRAY)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F)
			.sound(SoundType.CHERRY_WOOD)
			.ignitedByLava()
	);
	public static final Block DARK_OAK_WOOD = register(
		"dark_oak_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block MANGROVE_WOOD = register(
		"mangrove_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block STRIPPED_OAK_WOOD = register(
		"stripped_oak_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block STRIPPED_SPRUCE_WOOD = register(
		"stripped_spruce_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block STRIPPED_BIRCH_WOOD = register(
		"stripped_birch_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block STRIPPED_JUNGLE_WOOD = register(
		"stripped_jungle_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block STRIPPED_ACACIA_WOOD = register(
		"stripped_acacia_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block STRIPPED_CHERRY_WOOD = register(
		"stripped_cherry_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_PINK)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F)
			.sound(SoundType.CHERRY_WOOD)
			.ignitedByLava()
	);
	public static final Block STRIPPED_DARK_OAK_WOOD = register(
		"stripped_dark_oak_wood",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block STRIPPED_MANGROVE_WOOD = register(
		"stripped_mangrove_wood", RotatedPillarBlock::new, logProperties(MapColor.COLOR_RED, MapColor.COLOR_RED, SoundType.WOOD)
	);
	public static final Block OAK_LEAVES = register("oak_leaves", LeavesBlock::new, leavesProperties(SoundType.GRASS));
	public static final Block SPRUCE_LEAVES = register("spruce_leaves", LeavesBlock::new, leavesProperties(SoundType.GRASS));
	public static final Block BIRCH_LEAVES = register("birch_leaves", LeavesBlock::new, leavesProperties(SoundType.GRASS));
	public static final Block JUNGLE_LEAVES = register("jungle_leaves", LeavesBlock::new, leavesProperties(SoundType.GRASS));
	public static final Block ACACIA_LEAVES = register("acacia_leaves", LeavesBlock::new, leavesProperties(SoundType.GRASS));
	public static final Block CHERRY_LEAVES = register(
		"cherry_leaves",
		CherryLeavesBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PINK)
			.strength(0.2F)
			.randomTicks()
			.sound(SoundType.CHERRY_LEAVES)
			.noOcclusion()
			.isValidSpawn(Blocks::ocelotOrParrot)
			.isSuffocating(Blocks::never)
			.isViewBlocking(Blocks::never)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block DARK_OAK_LEAVES = register("dark_oak_leaves", LeavesBlock::new, leavesProperties(SoundType.GRASS));
	public static final Block MANGROVE_LEAVES = register("mangrove_leaves", MangroveLeavesBlock::new, leavesProperties(SoundType.GRASS));
	public static final Block AZALEA_LEAVES = register("azalea_leaves", LeavesBlock::new, leavesProperties(SoundType.AZALEA_LEAVES));
	public static final Block FLOWERING_AZALEA_LEAVES = register("flowering_azalea_leaves", LeavesBlock::new, leavesProperties(SoundType.AZALEA_LEAVES));
	public static final Block SPONGE = register(
		"sponge", SpongeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.6F).sound(SoundType.SPONGE)
	);
	public static final Block WET_SPONGE = register(
		"wet_sponge", WetSpongeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.6F).sound(SoundType.WET_SPONGE)
	);
	public static final Block GLASS = register(
		"glass",
		TransparentBlock::new,
		BlockBehaviour.Properties.of()
			.instrument(NoteBlockInstrument.HAT)
			.strength(0.3F)
			.sound(SoundType.GLASS)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.isRedstoneConductor(Blocks::never)
			.isSuffocating(Blocks::never)
			.isViewBlocking(Blocks::never)
	);
	public static final Block LAPIS_ORE = register(
		"lapis_ore",
		properties -> new DropExperienceBlock(UniformInt.of(2, 5), properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
	);
	public static final Block DEEPSLATE_LAPIS_ORE = register(
		"deepslate_lapis_ore",
		properties -> new DropExperienceBlock(UniformInt.of(2, 5), properties),
		BlockBehaviour.Properties.ofLegacyCopy(LAPIS_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
	);
	public static final Block LAPIS_BLOCK = register(
		"lapis_block", BlockBehaviour.Properties.of().mapColor(MapColor.LAPIS).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
	);
	public static final Block DISPENSER = register(
		"dispenser",
		DispenserBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)
	);
	public static final Block SANDSTONE = register(
		"sandstone", BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
	);
	public static final Block CHISELED_SANDSTONE = register(
		"chiseled_sandstone",
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
	);
	public static final Block CUT_SANDSTONE = register(
		"cut_sandstone", BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
	);
	public static final Block NOTE_BLOCK = register(
		"note_block",
		NoteBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).strength(0.8F).ignitedByLava()
	);
	public static final Block WHITE_BED = registerBed("white_bed", DyeColor.WHITE);
	public static final Block ORANGE_BED = registerBed("orange_bed", DyeColor.ORANGE);
	public static final Block MAGENTA_BED = registerBed("magenta_bed", DyeColor.MAGENTA);
	public static final Block LIGHT_BLUE_BED = registerBed("light_blue_bed", DyeColor.LIGHT_BLUE);
	public static final Block YELLOW_BED = registerBed("yellow_bed", DyeColor.YELLOW);
	public static final Block LIME_BED = registerBed("lime_bed", DyeColor.LIME);
	public static final Block PINK_BED = registerBed("pink_bed", DyeColor.PINK);
	public static final Block GRAY_BED = registerBed("gray_bed", DyeColor.GRAY);
	public static final Block LIGHT_GRAY_BED = registerBed("light_gray_bed", DyeColor.LIGHT_GRAY);
	public static final Block CYAN_BED = registerBed("cyan_bed", DyeColor.CYAN);
	public static final Block PURPLE_BED = registerBed("purple_bed", DyeColor.PURPLE);
	public static final Block BLUE_BED = registerBed("blue_bed", DyeColor.BLUE);
	public static final Block BROWN_BED = registerBed("brown_bed", DyeColor.BROWN);
	public static final Block GREEN_BED = registerBed("green_bed", DyeColor.GREEN);
	public static final Block RED_BED = registerBed("red_bed", DyeColor.RED);
	public static final Block BLACK_BED = registerBed("black_bed", DyeColor.BLACK);
	public static final Block POWERED_RAIL = register(
		"powered_rail", PoweredRailBlock::new, BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL)
	);
	public static final Block DETECTOR_RAIL = register(
		"detector_rail", DetectorRailBlock::new, BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL)
	);
	public static final Block STICKY_PISTON = register("sticky_piston", properties -> new PistonBaseBlock(true, properties), pistonProperties());
	public static final Block COBWEB = register(
		"cobweb",
		WebBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOL)
			.sound(SoundType.COBWEB)
			.forceSolidOn()
			.noCollission()
			.requiresCorrectToolForDrops()
			.strength(4.0F)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SHORT_GRASS = register(
		"short_grass",
		TallGrassBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XYZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block FERN = register(
		"fern",
		TallGrassBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XYZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block DEAD_BUSH = register(
		"dead_bush",
		DeadBushBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SEAGRASS = register(
		"seagrass",
		SeagrassBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WATER)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.WET_GRASS)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block TALL_SEAGRASS = register(
		"tall_seagrass",
		TallSeagrassBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WATER)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.WET_GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block PISTON = register("piston", properties -> new PistonBaseBlock(false, properties), pistonProperties());
	public static final Block PISTON_HEAD = register(
		"piston_head", PistonHeadBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F).noLootTable().pushReaction(PushReaction.BLOCK)
	);
	public static final Block WHITE_WOOL = register(
		"white_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block ORANGE_WOOL = register(
		"orange_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block MAGENTA_WOOL = register(
		"magenta_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block LIGHT_BLUE_WOOL = register(
		"light_blue_wool",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_BLUE)
			.instrument(NoteBlockInstrument.GUITAR)
			.strength(0.8F)
			.sound(SoundType.WOOL)
			.ignitedByLava()
	);
	public static final Block YELLOW_WOOL = register(
		"yellow_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block LIME_WOOL = register(
		"lime_wool",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_GREEN)
			.instrument(NoteBlockInstrument.GUITAR)
			.strength(0.8F)
			.sound(SoundType.WOOL)
			.ignitedByLava()
	);
	public static final Block PINK_WOOL = register(
		"pink_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block GRAY_WOOL = register(
		"gray_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block LIGHT_GRAY_WOOL = register(
		"light_gray_wool",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_GRAY)
			.instrument(NoteBlockInstrument.GUITAR)
			.strength(0.8F)
			.sound(SoundType.WOOL)
			.ignitedByLava()
	);
	public static final Block CYAN_WOOL = register(
		"cyan_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block PURPLE_WOOL = register(
		"purple_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block BLUE_WOOL = register(
		"blue_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block BROWN_WOOL = register(
		"brown_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block GREEN_WOOL = register(
		"green_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block RED_WOOL = register(
		"red_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block BLACK_WOOL = register(
		"black_wool",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block MOVING_PISTON = register(
		"moving_piston",
		MovingPistonBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.forceSolidOn()
			.strength(-1.0F)
			.dynamicShape()
			.noLootTable()
			.noOcclusion()
			.isRedstoneConductor(Blocks::never)
			.isSuffocating(Blocks::never)
			.isViewBlocking(Blocks::never)
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block DANDELION = register(
		"dandelion",
		properties -> new FlowerBlock(MobEffects.SATURATION, 0.35F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block TORCHFLOWER = register(
		"torchflower",
		properties -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block POPPY = register(
		"poppy",
		properties -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BLUE_ORCHID = register(
		"blue_orchid",
		properties -> new FlowerBlock(MobEffects.SATURATION, 0.35F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block ALLIUM = register(
		"allium",
		properties -> new FlowerBlock(MobEffects.FIRE_RESISTANCE, 4.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block AZURE_BLUET = register(
		"azure_bluet",
		properties -> new FlowerBlock(MobEffects.BLINDNESS, 8.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block RED_TULIP = register(
		"red_tulip",
		properties -> new FlowerBlock(MobEffects.WEAKNESS, 9.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block ORANGE_TULIP = register(
		"orange_tulip",
		properties -> new FlowerBlock(MobEffects.WEAKNESS, 9.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block WHITE_TULIP = register(
		"white_tulip",
		properties -> new FlowerBlock(MobEffects.WEAKNESS, 9.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block PINK_TULIP = register(
		"pink_tulip",
		properties -> new FlowerBlock(MobEffects.WEAKNESS, 9.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block OXEYE_DAISY = register(
		"oxeye_daisy",
		properties -> new FlowerBlock(MobEffects.REGENERATION, 8.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CORNFLOWER = register(
		"cornflower",
		properties -> new FlowerBlock(MobEffects.JUMP, 6.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block WITHER_ROSE = register(
		"wither_rose",
		properties -> new WitherRoseBlock(MobEffects.WITHER, 8.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block LILY_OF_THE_VALLEY = register(
		"lily_of_the_valley",
		properties -> new FlowerBlock(MobEffects.POISON, 12.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BROWN_MUSHROOM = register(
		"brown_mushroom",
		properties -> new MushroomBlock(TreeFeatures.HUGE_BROWN_MUSHROOM, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BROWN)
			.noCollission()
			.randomTicks()
			.instabreak()
			.sound(SoundType.GRASS)
			.lightLevel(blockStatex -> 1)
			.hasPostProcess(Blocks::always)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block RED_MUSHROOM = register(
		"red_mushroom",
		properties -> new MushroomBlock(TreeFeatures.HUGE_RED_MUSHROOM, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_RED)
			.noCollission()
			.randomTicks()
			.instabreak()
			.sound(SoundType.GRASS)
			.hasPostProcess(Blocks::always)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block GOLD_BLOCK = register(
		"gold_block",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.GOLD)
			.instrument(NoteBlockInstrument.BELL)
			.requiresCorrectToolForDrops()
			.strength(3.0F, 6.0F)
			.sound(SoundType.METAL)
	);
	public static final Block IRON_BLOCK = register(
		"iron_block",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.instrument(NoteBlockInstrument.IRON_XYLOPHONE)
			.requiresCorrectToolForDrops()
			.strength(5.0F, 6.0F)
			.sound(SoundType.METAL)
	);
	public static final Block BRICKS = register(
		"bricks",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block TNT = register(
		"tnt",
		TntBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instabreak().sound(SoundType.GRASS).ignitedByLava().isRedstoneConductor(Blocks::never)
	);
	public static final Block BOOKSHELF = register(
		"bookshelf", BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block CHISELED_BOOKSHELF = register(
		"chiseled_bookshelf",
		ChiseledBookShelfBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.instrument(NoteBlockInstrument.BASS)
			.strength(1.5F)
			.sound(SoundType.CHISELED_BOOKSHELF)
			.ignitedByLava()
	);
	public static final Block MOSSY_COBBLESTONE = register(
		"mossy_cobblestone",
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block OBSIDIAN = register(
		"obsidian",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F)
	);
	public static final Block TORCH = register(
		"torch",
		properties -> new TorchBlock(ParticleTypes.FLAME, properties),
		BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(blockStatex -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block WALL_TORCH = register(
		"wall_torch",
		properties -> new WallTorchBlock(ParticleTypes.FLAME, properties),
		wallVariant(TORCH, true).noCollission().instabreak().lightLevel(blockStatex -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block FIRE = register(
		"fire",
		FireBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.FIRE)
			.replaceable()
			.noCollission()
			.instabreak()
			.lightLevel(blockStatex -> 15)
			.sound(SoundType.WOOL)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SOUL_FIRE = register(
		"soul_fire",
		SoulFireBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_BLUE)
			.replaceable()
			.noCollission()
			.instabreak()
			.lightLevel(blockStatex -> 10)
			.sound(SoundType.WOOL)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SPAWNER = register(
		"spawner",
		SpawnerBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(5.0F)
			.sound(SoundType.SPAWNER)
			.noOcclusion()
	);
	public static final Block OAK_STAIRS = registerLegacyStair("oak_stairs", OAK_PLANKS);
	public static final Block CHEST = register(
		"chest",
		properties -> new ChestBlock(() -> BlockEntityType.CHEST, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block REDSTONE_WIRE = register(
		"redstone_wire", RedStoneWireBlock::new, BlockBehaviour.Properties.of().noCollission().instabreak().pushReaction(PushReaction.DESTROY)
	);
	public static final Block DIAMOND_ORE = register(
		"diamond_ore",
		properties -> new DropExperienceBlock(UniformInt.of(3, 7), properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
	);
	public static final Block DEEPSLATE_DIAMOND_ORE = register(
		"deepslate_diamond_ore",
		properties -> new DropExperienceBlock(UniformInt.of(3, 7), properties),
		BlockBehaviour.Properties.ofLegacyCopy(DIAMOND_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
	);
	public static final Block DIAMOND_BLOCK = register(
		"diamond_block", BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)
	);
	public static final Block CRAFTING_TABLE = register(
		"crafting_table",
		CraftingTableBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block WHEAT = register(
		"wheat",
		CropBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(blockStatex -> blockStatex.getValue(CropBlock.AGE) >= 6 ? MapColor.COLOR_YELLOW : MapColor.PLANT)
			.noCollission()
			.randomTicks()
			.instabreak()
			.sound(SoundType.CROP)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block FARMLAND = register(
		"farmland",
		FarmBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.DIRT)
			.randomTicks()
			.strength(0.6F)
			.sound(SoundType.GRAVEL)
			.isViewBlocking(Blocks::always)
			.isSuffocating(Blocks::always)
	);
	public static final Block FURNACE = register(
		"furnace",
		FurnaceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(3.5F)
			.lightLevel(litBlockEmission(13))
	);
	public static final Block OAK_SIGN = register(
		"oak_sign",
		properties -> new StandingSignBlock(WoodType.OAK, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()
	);
	public static final Block SPRUCE_SIGN = register(
		"spruce_sign",
		properties -> new StandingSignBlock(WoodType.SPRUCE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(SPRUCE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block BIRCH_SIGN = register(
		"birch_sign",
		properties -> new StandingSignBlock(WoodType.BIRCH, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()
	);
	public static final Block ACACIA_SIGN = register(
		"acacia_sign",
		properties -> new StandingSignBlock(WoodType.ACACIA, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_ORANGE)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block CHERRY_SIGN = register(
		"cherry_sign",
		properties -> new StandingSignBlock(WoodType.CHERRY, properties),
		BlockBehaviour.Properties.of()
			.mapColor(CHERRY_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block JUNGLE_SIGN = register(
		"jungle_sign",
		properties -> new StandingSignBlock(WoodType.JUNGLE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(JUNGLE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block DARK_OAK_SIGN = register(
		"dark_oak_sign",
		properties -> new StandingSignBlock(WoodType.DARK_OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(DARK_OAK_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block MANGROVE_SIGN = register(
		"mangrove_sign",
		properties -> new StandingSignBlock(WoodType.MANGROVE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MANGROVE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block BAMBOO_SIGN = register(
		"bamboo_sign",
		properties -> new StandingSignBlock(WoodType.BAMBOO, properties),
		BlockBehaviour.Properties.of()
			.mapColor(BAMBOO_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block OAK_DOOR = register(
		"oak_door",
		properties -> new DoorBlock(BlockSetType.OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(OAK_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block LADDER = register(
		"ladder",
		LadderBlock::new,
		BlockBehaviour.Properties.of().forceSolidOff().strength(0.4F).sound(SoundType.LADDER).noOcclusion().pushReaction(PushReaction.DESTROY)
	);
	public static final Block RAIL = register("rail", RailBlock::new, BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL));
	public static final Block COBBLESTONE_STAIRS = registerLegacyStair("cobblestone_stairs", COBBLESTONE);
	public static final Block OAK_WALL_SIGN = register(
		"oak_wall_sign",
		properties -> new WallSignBlock(WoodType.OAK, properties),
		wallVariant(OAK_SIGN, true).mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()
	);
	public static final Block SPRUCE_WALL_SIGN = register(
		"spruce_wall_sign",
		properties -> new WallSignBlock(WoodType.SPRUCE, properties),
		wallVariant(SPRUCE_SIGN, true)
			.mapColor(SPRUCE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block BIRCH_WALL_SIGN = register(
		"birch_wall_sign",
		properties -> new WallSignBlock(WoodType.BIRCH, properties),
		wallVariant(BIRCH_SIGN, true).mapColor(MapColor.SAND).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()
	);
	public static final Block ACACIA_WALL_SIGN = register(
		"acacia_wall_sign",
		properties -> new WallSignBlock(WoodType.ACACIA, properties),
		wallVariant(ACACIA_SIGN, true)
			.mapColor(MapColor.COLOR_ORANGE)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block CHERRY_WALL_SIGN = register(
		"cherry_wall_sign",
		properties -> new WallSignBlock(WoodType.CHERRY, properties),
		wallVariant(CHERRY_SIGN, true)
			.mapColor(CHERRY_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block JUNGLE_WALL_SIGN = register(
		"jungle_wall_sign",
		properties -> new WallSignBlock(WoodType.JUNGLE, properties),
		wallVariant(JUNGLE_SIGN, true)
			.mapColor(JUNGLE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block DARK_OAK_WALL_SIGN = register(
		"dark_oak_wall_sign",
		properties -> new WallSignBlock(WoodType.DARK_OAK, properties),
		wallVariant(DARK_OAK_SIGN, true)
			.mapColor(DARK_OAK_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block MANGROVE_WALL_SIGN = register(
		"mangrove_wall_sign",
		properties -> new WallSignBlock(WoodType.MANGROVE, properties),
		wallVariant(MANGROVE_SIGN, true)
			.mapColor(MANGROVE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block BAMBOO_WALL_SIGN = register(
		"bamboo_wall_sign",
		properties -> new WallSignBlock(WoodType.BAMBOO, properties),
		wallVariant(BAMBOO_SIGN, true)
			.mapColor(BAMBOO_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block OAK_HANGING_SIGN = register(
		"oak_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(OAK_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block SPRUCE_HANGING_SIGN = register(
		"spruce_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.SPRUCE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(SPRUCE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block BIRCH_HANGING_SIGN = register(
		"birch_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.BIRCH, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()
	);
	public static final Block ACACIA_HANGING_SIGN = register(
		"acacia_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.ACACIA, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_ORANGE)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block CHERRY_HANGING_SIGN = register(
		"cherry_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.CHERRY, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_PINK)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block JUNGLE_HANGING_SIGN = register(
		"jungle_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.JUNGLE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(JUNGLE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block DARK_OAK_HANGING_SIGN = register(
		"dark_oak_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.DARK_OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(DARK_OAK_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block CRIMSON_HANGING_SIGN = register(
		"crimson_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.CRIMSON, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_STEM).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F)
	);
	public static final Block WARPED_HANGING_SIGN = register(
		"warped_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.WARPED, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_STEM).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F)
	);
	public static final Block MANGROVE_HANGING_SIGN = register(
		"mangrove_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.MANGROVE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MANGROVE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block BAMBOO_HANGING_SIGN = register(
		"bamboo_hanging_sign",
		properties -> new CeilingHangingSignBlock(WoodType.BAMBOO, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_YELLOW)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block OAK_WALL_HANGING_SIGN = register(
		"oak_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.OAK, properties),
		wallVariant(OAK_HANGING_SIGN, true)
			.mapColor(OAK_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block SPRUCE_WALL_HANGING_SIGN = register(
		"spruce_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.SPRUCE, properties),
		wallVariant(SPRUCE_HANGING_SIGN, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block BIRCH_WALL_HANGING_SIGN = register(
		"birch_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.BIRCH, properties),
		wallVariant(BIRCH_HANGING_SIGN, true)
			.mapColor(MapColor.SAND)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block ACACIA_WALL_HANGING_SIGN = register(
		"acacia_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.ACACIA, properties),
		wallVariant(ACACIA_HANGING_SIGN, true)
			.mapColor(MapColor.COLOR_ORANGE)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block CHERRY_WALL_HANGING_SIGN = register(
		"cherry_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.CHERRY, properties),
		wallVariant(CHERRY_HANGING_SIGN, true)
			.mapColor(MapColor.TERRACOTTA_PINK)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block JUNGLE_WALL_HANGING_SIGN = register(
		"jungle_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.JUNGLE, properties),
		wallVariant(JUNGLE_HANGING_SIGN, true)
			.mapColor(JUNGLE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block DARK_OAK_WALL_HANGING_SIGN = register(
		"dark_oak_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.DARK_OAK, properties),
		wallVariant(DARK_OAK_HANGING_SIGN, true)
			.mapColor(DARK_OAK_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block MANGROVE_WALL_HANGING_SIGN = register(
		"mangrove_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.MANGROVE, properties),
		wallVariant(MANGROVE_HANGING_SIGN, true)
			.mapColor(MANGROVE_LOG.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block CRIMSON_WALL_HANGING_SIGN = register(
		"crimson_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.CRIMSON, properties),
		wallVariant(CRIMSON_HANGING_SIGN, true).mapColor(MapColor.CRIMSON_STEM).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F)
	);
	public static final Block WARPED_WALL_HANGING_SIGN = register(
		"warped_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.WARPED, properties),
		wallVariant(WARPED_HANGING_SIGN, true).mapColor(MapColor.WARPED_STEM).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F)
	);
	public static final Block BAMBOO_WALL_HANGING_SIGN = register(
		"bamboo_wall_hanging_sign",
		properties -> new WallHangingSignBlock(WoodType.BAMBOO, properties),
		wallVariant(BAMBOO_HANGING_SIGN, true)
			.mapColor(MapColor.COLOR_YELLOW)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.ignitedByLava()
	);
	public static final Block LEVER = register(
		"lever", LeverBlock::new, BlockBehaviour.Properties.of().noCollission().strength(0.5F).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)
	);
	public static final Block STONE_PRESSURE_PLATE = register(
		"stone_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.STONE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.strength(0.5F)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block IRON_DOOR = register(
		"iron_door",
		properties -> new DoorBlock(BlockSetType.IRON, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F).noOcclusion().pushReaction(PushReaction.DESTROY)
	);
	public static final Block OAK_PRESSURE_PLATE = register(
		"oak_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(OAK_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SPRUCE_PRESSURE_PLATE = register(
		"spruce_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.SPRUCE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(SPRUCE_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BIRCH_PRESSURE_PLATE = register(
		"birch_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.BIRCH, properties),
		BlockBehaviour.Properties.of()
			.mapColor(BIRCH_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block JUNGLE_PRESSURE_PLATE = register(
		"jungle_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.JUNGLE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(JUNGLE_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block ACACIA_PRESSURE_PLATE = register(
		"acacia_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.ACACIA, properties),
		BlockBehaviour.Properties.of()
			.mapColor(ACACIA_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CHERRY_PRESSURE_PLATE = register(
		"cherry_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.CHERRY, properties),
		BlockBehaviour.Properties.of()
			.mapColor(CHERRY_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block DARK_OAK_PRESSURE_PLATE = register(
		"dark_oak_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.DARK_OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(DARK_OAK_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block MANGROVE_PRESSURE_PLATE = register(
		"mangrove_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.MANGROVE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MANGROVE_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BAMBOO_PRESSURE_PLATE = register(
		"bamboo_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.BAMBOO, properties),
		BlockBehaviour.Properties.of()
			.mapColor(BAMBOO_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block REDSTONE_ORE = register(
		"redstone_ore",
		RedStoneOreBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.randomTicks()
			.lightLevel(litBlockEmission(9))
			.strength(3.0F, 3.0F)
	);
	public static final Block DEEPSLATE_REDSTONE_ORE = register(
		"deepslate_redstone_ore",
		RedStoneOreBlock::new,
		BlockBehaviour.Properties.ofLegacyCopy(REDSTONE_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
	);
	public static final Block REDSTONE_TORCH = register(
		"redstone_torch",
		RedstoneTorchBlock::new,
		BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block REDSTONE_WALL_TORCH = register(
		"redstone_wall_torch",
		RedstoneWallTorchBlock::new,
		wallVariant(REDSTONE_TORCH, true).noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block STONE_BUTTON = register("stone_button", properties -> new ButtonBlock(BlockSetType.STONE, 20, properties), buttonProperties());
	public static final Block SNOW = register(
		"snow",
		SnowLayerBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.SNOW)
			.replaceable()
			.forceSolidOff()
			.randomTicks()
			.strength(0.1F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.SNOW)
			.isViewBlocking((blockStatex, blockGetter, blockPos) -> (Integer)blockStatex.getValue(SnowLayerBlock.LAYERS) >= 8)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block ICE = register(
		"ice",
		IceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.ICE)
			.friction(0.98F)
			.randomTicks()
			.strength(0.5F)
			.sound(SoundType.GLASS)
			.noOcclusion()
			.isValidSpawn((blockStatex, blockGetter, blockPos, entityType) -> entityType == EntityType.POLAR_BEAR)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block SNOW_BLOCK = register(
		"snow_block", BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).requiresCorrectToolForDrops().strength(0.2F).sound(SoundType.SNOW)
	);
	public static final Block CACTUS = register(
		"cactus",
		CactusBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().strength(0.4F).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)
	);
	public static final Block CLAY = register(
		"clay", BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).instrument(NoteBlockInstrument.FLUTE).strength(0.6F).sound(SoundType.GRAVEL)
	);
	public static final Block SUGAR_CANE = register(
		"sugar_cane",
		SugarCaneBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block JUKEBOX = register(
		"jukebox",
		JukeboxBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F, 6.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block OAK_FENCE = register(
		"oak_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(OAK_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block NETHERRACK = register(
		"netherrack",
		NetherrackBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(0.4F)
			.sound(SoundType.NETHERRACK)
	);
	public static final Block SOUL_SAND = register(
		"soul_sand",
		SoulSandBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BROWN)
			.instrument(NoteBlockInstrument.COW_BELL)
			.strength(0.5F)
			.speedFactor(0.4F)
			.sound(SoundType.SOUL_SAND)
			.isValidSpawn(Blocks::always)
			.isRedstoneConductor(Blocks::always)
			.isViewBlocking(Blocks::always)
			.isSuffocating(Blocks::always)
	);
	public static final Block SOUL_SOIL = register(
		"soul_soil", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.5F).sound(SoundType.SOUL_SOIL)
	);
	public static final Block BASALT = register(
		"basalt",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
			.sound(SoundType.BASALT)
	);
	public static final Block POLISHED_BASALT = register(
		"polished_basalt",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
			.sound(SoundType.BASALT)
	);
	public static final Block SOUL_TORCH = register(
		"soul_torch",
		properties -> new TorchBlock(ParticleTypes.SOUL_FIRE_FLAME, properties),
		BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(blockStatex -> 10).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block SOUL_WALL_TORCH = register(
		"soul_wall_torch",
		properties -> new WallTorchBlock(ParticleTypes.SOUL_FIRE_FLAME, properties),
		wallVariant(SOUL_TORCH, true).noCollission().instabreak().lightLevel(blockStatex -> 10).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block GLOWSTONE = register(
		"glowstone",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.SAND)
			.instrument(NoteBlockInstrument.PLING)
			.strength(0.3F)
			.sound(SoundType.GLASS)
			.lightLevel(blockStatex -> 15)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block NETHER_PORTAL = register(
		"nether_portal",
		NetherPortalBlock::new,
		BlockBehaviour.Properties.of()
			.noCollission()
			.randomTicks()
			.strength(-1.0F)
			.sound(SoundType.GLASS)
			.lightLevel(blockStatex -> 11)
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block CARVED_PUMPKIN = register(
		"carved_pumpkin",
		CarvedPumpkinBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_ORANGE)
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.isValidSpawn(Blocks::always)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block JACK_O_LANTERN = register(
		"jack_o_lantern",
		CarvedPumpkinBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_ORANGE)
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.lightLevel(blockStatex -> 15)
			.isValidSpawn(Blocks::always)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CAKE = register(
		"cake", CakeBlock::new, BlockBehaviour.Properties.of().forceSolidOn().strength(0.5F).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)
	);
	public static final Block REPEATER = register(
		"repeater", RepeaterBlock::new, BlockBehaviour.Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)
	);
	public static final Block WHITE_STAINED_GLASS = registerStainedGlass("white_stained_glass", DyeColor.WHITE);
	public static final Block ORANGE_STAINED_GLASS = registerStainedGlass("orange_stained_glass", DyeColor.ORANGE);
	public static final Block MAGENTA_STAINED_GLASS = registerStainedGlass("magenta_stained_glass", DyeColor.MAGENTA);
	public static final Block LIGHT_BLUE_STAINED_GLASS = registerStainedGlass("light_blue_stained_glass", DyeColor.LIGHT_BLUE);
	public static final Block YELLOW_STAINED_GLASS = registerStainedGlass("yellow_stained_glass", DyeColor.YELLOW);
	public static final Block LIME_STAINED_GLASS = registerStainedGlass("lime_stained_glass", DyeColor.LIME);
	public static final Block PINK_STAINED_GLASS = registerStainedGlass("pink_stained_glass", DyeColor.PINK);
	public static final Block GRAY_STAINED_GLASS = registerStainedGlass("gray_stained_glass", DyeColor.GRAY);
	public static final Block LIGHT_GRAY_STAINED_GLASS = registerStainedGlass("light_gray_stained_glass", DyeColor.LIGHT_GRAY);
	public static final Block CYAN_STAINED_GLASS = registerStainedGlass("cyan_stained_glass", DyeColor.CYAN);
	public static final Block PURPLE_STAINED_GLASS = registerStainedGlass("purple_stained_glass", DyeColor.PURPLE);
	public static final Block BLUE_STAINED_GLASS = registerStainedGlass("blue_stained_glass", DyeColor.BLUE);
	public static final Block BROWN_STAINED_GLASS = registerStainedGlass("brown_stained_glass", DyeColor.BROWN);
	public static final Block GREEN_STAINED_GLASS = registerStainedGlass("green_stained_glass", DyeColor.GREEN);
	public static final Block RED_STAINED_GLASS = registerStainedGlass("red_stained_glass", DyeColor.RED);
	public static final Block BLACK_STAINED_GLASS = registerStainedGlass("black_stained_glass", DyeColor.BLACK);
	public static final Block OAK_TRAPDOOR = register(
		"oak_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block SPRUCE_TRAPDOOR = register(
		"spruce_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.SPRUCE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PODZOL)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block BIRCH_TRAPDOOR = register(
		"birch_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.BIRCH, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.SAND)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block JUNGLE_TRAPDOOR = register(
		"jungle_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.JUNGLE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.DIRT)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block ACACIA_TRAPDOOR = register(
		"acacia_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.ACACIA, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_ORANGE)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block CHERRY_TRAPDOOR = register(
		"cherry_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.CHERRY, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_WHITE)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block DARK_OAK_TRAPDOOR = register(
		"dark_oak_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.DARK_OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BROWN)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block MANGROVE_TRAPDOOR = register(
		"mangrove_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.MANGROVE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_RED)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block BAMBOO_TRAPDOOR = register(
		"bamboo_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.BAMBOO, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_YELLOW)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block STONE_BRICKS = register(
		"stone_bricks",
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block MOSSY_STONE_BRICKS = register(
		"mossy_stone_bricks",
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block CRACKED_STONE_BRICKS = register(
		"cracked_stone_bricks",
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block CHISELED_STONE_BRICKS = register(
		"chiseled_stone_bricks",
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block PACKED_MUD = register("packed_mud", BlockBehaviour.Properties.ofLegacyCopy(DIRT).strength(1.0F, 3.0F).sound(SoundType.PACKED_MUD));
	public static final Block MUD_BRICKS = register(
		"mud_bricks",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 3.0F)
			.sound(SoundType.MUD_BRICKS)
	);
	public static final Block INFESTED_STONE = register(
		"infested_stone", properties -> new InfestedBlock(STONE, properties), BlockBehaviour.Properties.of().mapColor(MapColor.CLAY)
	);
	public static final Block INFESTED_COBBLESTONE = register(
		"infested_cobblestone", properties -> new InfestedBlock(COBBLESTONE, properties), BlockBehaviour.Properties.of().mapColor(MapColor.CLAY)
	);
	public static final Block INFESTED_STONE_BRICKS = register(
		"infested_stone_bricks", properties -> new InfestedBlock(STONE_BRICKS, properties), BlockBehaviour.Properties.of().mapColor(MapColor.CLAY)
	);
	public static final Block INFESTED_MOSSY_STONE_BRICKS = register(
		"infested_mossy_stone_bricks", properties -> new InfestedBlock(MOSSY_STONE_BRICKS, properties), BlockBehaviour.Properties.of().mapColor(MapColor.CLAY)
	);
	public static final Block INFESTED_CRACKED_STONE_BRICKS = register(
		"infested_cracked_stone_bricks", properties -> new InfestedBlock(CRACKED_STONE_BRICKS, properties), BlockBehaviour.Properties.of().mapColor(MapColor.CLAY)
	);
	public static final Block INFESTED_CHISELED_STONE_BRICKS = register(
		"infested_chiseled_stone_bricks", properties -> new InfestedBlock(CHISELED_STONE_BRICKS, properties), BlockBehaviour.Properties.of().mapColor(MapColor.CLAY)
	);
	public static final Block BROWN_MUSHROOM_BLOCK = register(
		"brown_mushroom_block",
		HugeMushroomBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block RED_MUSHROOM_BLOCK = register(
		"red_mushroom_block",
		HugeMushroomBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block MUSHROOM_STEM = register(
		"mushroom_stem",
		HugeMushroomBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block IRON_BARS = register(
		"iron_bars", IronBarsBlock::new, BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL).noOcclusion()
	);
	public static final Block CHAIN = register(
		"chain",
		ChainBlock::new,
		BlockBehaviour.Properties.of().forceSolidOn().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.CHAIN).noOcclusion()
	);
	public static final Block GLASS_PANE = register(
		"glass_pane", IronBarsBlock::new, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block PUMPKIN = register(
		net.minecraft.references.Blocks.PUMPKIN,
		PumpkinBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_ORANGE)
			.instrument(NoteBlockInstrument.DIDGERIDOO)
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block MELON = register(
		net.minecraft.references.Blocks.MELON,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block ATTACHED_PUMPKIN_STEM = register(
		net.minecraft.references.Blocks.ATTACHED_PUMPKIN_STEM,
		properties -> new AttachedStemBlock(net.minecraft.references.Blocks.PUMPKIN_STEM, net.minecraft.references.Blocks.PUMPKIN, Items.PUMPKIN_SEEDS, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block ATTACHED_MELON_STEM = register(
		net.minecraft.references.Blocks.ATTACHED_MELON_STEM,
		properties -> new AttachedStemBlock(net.minecraft.references.Blocks.MELON_STEM, net.minecraft.references.Blocks.MELON, Items.MELON_SEEDS, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block PUMPKIN_STEM = register(
		net.minecraft.references.Blocks.PUMPKIN_STEM,
		properties -> new StemBlock(net.minecraft.references.Blocks.PUMPKIN, net.minecraft.references.Blocks.ATTACHED_PUMPKIN_STEM, Items.PUMPKIN_SEEDS, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.randomTicks()
			.instabreak()
			.sound(SoundType.HARD_CROP)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block MELON_STEM = register(
		net.minecraft.references.Blocks.MELON_STEM,
		properties -> new StemBlock(net.minecraft.references.Blocks.MELON, net.minecraft.references.Blocks.ATTACHED_MELON_STEM, Items.MELON_SEEDS, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.randomTicks()
			.instabreak()
			.sound(SoundType.HARD_CROP)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block VINE = register(
		"vine",
		VineBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.replaceable()
			.noCollission()
			.randomTicks()
			.strength(0.2F)
			.sound(SoundType.VINE)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block GLOW_LICHEN = register(
		"glow_lichen",
		GlowLichenBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.GLOW_LICHEN)
			.replaceable()
			.noCollission()
			.strength(0.2F)
			.sound(SoundType.GLOW_LICHEN)
			.lightLevel(GlowLichenBlock.emission(7))
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block OAK_FENCE_GATE = register(
		"oak_fence_gate",
		properties -> new FenceGateBlock(WoodType.OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(OAK_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
	);
	public static final Block BRICK_STAIRS = registerLegacyStair("brick_stairs", BRICKS);
	public static final Block STONE_BRICK_STAIRS = registerLegacyStair("stone_brick_stairs", STONE_BRICKS);
	public static final Block MUD_BRICK_STAIRS = registerLegacyStair("mud_brick_stairs", MUD_BRICKS);
	public static final Block MYCELIUM = register(
		"mycelium", MyceliumBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).randomTicks().strength(0.6F).sound(SoundType.GRASS)
	);
	public static final Block LILY_PAD = register(
		"lily_pad",
		WaterlilyBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.LILY_PAD).noOcclusion().pushReaction(PushReaction.DESTROY)
	);
	public static final Block NETHER_BRICKS = register(
		"nether_bricks",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(2.0F, 6.0F)
			.sound(SoundType.NETHER_BRICKS)
	);
	public static final Block NETHER_BRICK_FENCE = register(
		"nether_brick_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(2.0F, 6.0F)
			.sound(SoundType.NETHER_BRICKS)
	);
	public static final Block NETHER_BRICK_STAIRS = registerLegacyStair("nether_brick_stairs", NETHER_BRICKS);
	public static final Block NETHER_WART = register(
		"nether_wart",
		NetherWartBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noCollission().randomTicks().sound(SoundType.NETHER_WART).pushReaction(PushReaction.DESTROY)
	);
	public static final Block ENCHANTING_TABLE = register(
		"enchanting_table",
		EnchantingTableBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_RED)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.lightLevel(blockStatex -> 7)
			.strength(5.0F, 1200.0F)
	);
	public static final Block BREWING_STAND = register(
		"brewing_stand",
		BrewingStandBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(0.5F).lightLevel(blockStatex -> 1).noOcclusion()
	);
	public static final Block CAULDRON = register(
		"cauldron", CauldronBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.0F).noOcclusion()
	);
	public static final Block WATER_CAULDRON = register(
		"water_cauldron",
		properties -> new LayeredCauldronBlock(Biome.Precipitation.RAIN, CauldronInteraction.WATER, properties),
		BlockBehaviour.Properties.ofLegacyCopy(CAULDRON)
	);
	public static final Block LAVA_CAULDRON = register(
		"lava_cauldron", LavaCauldronBlock::new, BlockBehaviour.Properties.ofLegacyCopy(CAULDRON).lightLevel(blockStatex -> 15)
	);
	public static final Block POWDER_SNOW_CAULDRON = register(
		"powder_snow_cauldron",
		properties -> new LayeredCauldronBlock(Biome.Precipitation.SNOW, CauldronInteraction.POWDER_SNOW, properties),
		BlockBehaviour.Properties.ofLegacyCopy(CAULDRON)
	);
	public static final Block END_PORTAL = register(
		"end_portal",
		EndPortalBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.noCollission()
			.lightLevel(blockStatex -> 15)
			.strength(-1.0F, 3600000.0F)
			.noLootTable()
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block END_PORTAL_FRAME = register(
		"end_portal_frame",
		EndPortalFrameBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GREEN)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.sound(SoundType.GLASS)
			.lightLevel(blockStatex -> 1)
			.strength(-1.0F, 3600000.0F)
			.noLootTable()
	);
	public static final Block END_STONE = register(
		"end_stone",
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F)
	);
	public static final Block DRAGON_EGG = register(
		"dragon_egg",
		DragonEggBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.strength(3.0F, 9.0F)
			.lightLevel(blockStatex -> 1)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block REDSTONE_LAMP = register(
		"redstone_lamp",
		RedstoneLampBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_ORANGE)
			.lightLevel(litBlockEmission(15))
			.strength(0.3F)
			.sound(SoundType.GLASS)
			.isValidSpawn(Blocks::always)
	);
	public static final Block COCOA = register(
		"cocoa",
		CocoaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.randomTicks()
			.strength(0.2F, 3.0F)
			.sound(SoundType.WOOD)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SANDSTONE_STAIRS = registerLegacyStair("sandstone_stairs", SANDSTONE);
	public static final Block EMERALD_ORE = register(
		"emerald_ore",
		properties -> new DropExperienceBlock(UniformInt.of(3, 7), properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
	);
	public static final Block DEEPSLATE_EMERALD_ORE = register(
		"deepslate_emerald_ore",
		properties -> new DropExperienceBlock(UniformInt.of(3, 7), properties),
		BlockBehaviour.Properties.ofLegacyCopy(EMERALD_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
	);
	public static final Block ENDER_CHEST = register(
		"ender_chest",
		EnderChestBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(22.5F, 600.0F)
			.lightLevel(blockStatex -> 7)
	);
	public static final Block TRIPWIRE_HOOK = register(
		"tripwire_hook", TripWireHookBlock::new, BlockBehaviour.Properties.of().noCollission().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
	);
	public static final Block TRIPWIRE = register(
		"tripwire", properties -> new TripWireBlock(TRIPWIRE_HOOK, properties), BlockBehaviour.Properties.of().noCollission().pushReaction(PushReaction.DESTROY)
	);
	public static final Block EMERALD_BLOCK = register(
		"emerald_block",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.EMERALD)
			.instrument(NoteBlockInstrument.BIT)
			.requiresCorrectToolForDrops()
			.strength(5.0F, 6.0F)
			.sound(SoundType.METAL)
	);
	public static final Block SPRUCE_STAIRS = registerLegacyStair("spruce_stairs", SPRUCE_PLANKS);
	public static final Block BIRCH_STAIRS = registerLegacyStair("birch_stairs", BIRCH_PLANKS);
	public static final Block JUNGLE_STAIRS = registerLegacyStair("jungle_stairs", JUNGLE_PLANKS);
	public static final Block COMMAND_BLOCK = register(
		"command_block",
		properties -> new CommandBlock(false, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()
	);
	public static final Block BEACON = register(
		"beacon",
		BeaconBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.DIAMOND)
			.instrument(NoteBlockInstrument.HAT)
			.strength(3.0F)
			.lightLevel(blockStatex -> 15)
			.noOcclusion()
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block COBBLESTONE_WALL = register("cobblestone_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(COBBLESTONE).forceSolidOn());
	public static final Block MOSSY_COBBLESTONE_WALL = register(
		"mossy_cobblestone_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(COBBLESTONE).forceSolidOn()
	);
	public static final Block FLOWER_POT = register("flower_pot", properties -> new FlowerPotBlock(AIR, properties), flowerPotProperties());
	public static final Block POTTED_TORCHFLOWER = register("potted_torchflower", properties -> new FlowerPotBlock(TORCHFLOWER, properties), flowerPotProperties());
	public static final Block POTTED_OAK_SAPLING = register("potted_oak_sapling", properties -> new FlowerPotBlock(OAK_SAPLING, properties), flowerPotProperties());
	public static final Block POTTED_SPRUCE_SAPLING = register(
		"potted_spruce_sapling", properties -> new FlowerPotBlock(SPRUCE_SAPLING, properties), flowerPotProperties()
	);
	public static final Block POTTED_BIRCH_SAPLING = register(
		"potted_birch_sapling", properties -> new FlowerPotBlock(BIRCH_SAPLING, properties), flowerPotProperties()
	);
	public static final Block POTTED_JUNGLE_SAPLING = register(
		"potted_jungle_sapling", properties -> new FlowerPotBlock(JUNGLE_SAPLING, properties), flowerPotProperties()
	);
	public static final Block POTTED_ACACIA_SAPLING = register(
		"potted_acacia_sapling", properties -> new FlowerPotBlock(ACACIA_SAPLING, properties), flowerPotProperties()
	);
	public static final Block POTTED_CHERRY_SAPLING = register(
		"potted_cherry_sapling", properties -> new FlowerPotBlock(CHERRY_SAPLING, properties), flowerPotProperties()
	);
	public static final Block POTTED_DARK_OAK_SAPLING = register(
		"potted_dark_oak_sapling", properties -> new FlowerPotBlock(DARK_OAK_SAPLING, properties), flowerPotProperties()
	);
	public static final Block POTTED_MANGROVE_PROPAGULE = register(
		"potted_mangrove_propagule", properties -> new FlowerPotBlock(MANGROVE_PROPAGULE, properties), flowerPotProperties()
	);
	public static final Block POTTED_FERN = register("potted_fern", properties -> new FlowerPotBlock(FERN, properties), flowerPotProperties());
	public static final Block POTTED_DANDELION = register("potted_dandelion", properties -> new FlowerPotBlock(DANDELION, properties), flowerPotProperties());
	public static final Block POTTED_POPPY = register("potted_poppy", properties -> new FlowerPotBlock(POPPY, properties), flowerPotProperties());
	public static final Block POTTED_BLUE_ORCHID = register("potted_blue_orchid", properties -> new FlowerPotBlock(BLUE_ORCHID, properties), flowerPotProperties());
	public static final Block POTTED_ALLIUM = register("potted_allium", properties -> new FlowerPotBlock(ALLIUM, properties), flowerPotProperties());
	public static final Block POTTED_AZURE_BLUET = register("potted_azure_bluet", properties -> new FlowerPotBlock(AZURE_BLUET, properties), flowerPotProperties());
	public static final Block POTTED_RED_TULIP = register("potted_red_tulip", properties -> new FlowerPotBlock(RED_TULIP, properties), flowerPotProperties());
	public static final Block POTTED_ORANGE_TULIP = register(
		"potted_orange_tulip", properties -> new FlowerPotBlock(ORANGE_TULIP, properties), flowerPotProperties()
	);
	public static final Block POTTED_WHITE_TULIP = register("potted_white_tulip", properties -> new FlowerPotBlock(WHITE_TULIP, properties), flowerPotProperties());
	public static final Block POTTED_PINK_TULIP = register("potted_pink_tulip", properties -> new FlowerPotBlock(PINK_TULIP, properties), flowerPotProperties());
	public static final Block POTTED_OXEYE_DAISY = register("potted_oxeye_daisy", properties -> new FlowerPotBlock(OXEYE_DAISY, properties), flowerPotProperties());
	public static final Block POTTED_CORNFLOWER = register("potted_cornflower", properties -> new FlowerPotBlock(CORNFLOWER, properties), flowerPotProperties());
	public static final Block POTTED_LILY_OF_THE_VALLEY = register(
		"potted_lily_of_the_valley", properties -> new FlowerPotBlock(LILY_OF_THE_VALLEY, properties), flowerPotProperties()
	);
	public static final Block POTTED_WITHER_ROSE = register("potted_wither_rose", properties -> new FlowerPotBlock(WITHER_ROSE, properties), flowerPotProperties());
	public static final Block POTTED_RED_MUSHROOM = register(
		"potted_red_mushroom", properties -> new FlowerPotBlock(RED_MUSHROOM, properties), flowerPotProperties()
	);
	public static final Block POTTED_BROWN_MUSHROOM = register(
		"potted_brown_mushroom", properties -> new FlowerPotBlock(BROWN_MUSHROOM, properties), flowerPotProperties()
	);
	public static final Block POTTED_DEAD_BUSH = register("potted_dead_bush", properties -> new FlowerPotBlock(DEAD_BUSH, properties), flowerPotProperties());
	public static final Block POTTED_CACTUS = register("potted_cactus", properties -> new FlowerPotBlock(CACTUS, properties), flowerPotProperties());
	public static final Block CARROTS = register(
		"carrots",
		CarrotBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
	);
	public static final Block POTATOES = register(
		"potatoes",
		PotatoBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
	);
	public static final Block OAK_BUTTON = register("oak_button", properties -> new ButtonBlock(BlockSetType.OAK, 30, properties), buttonProperties());
	public static final Block SPRUCE_BUTTON = register("spruce_button", properties -> new ButtonBlock(BlockSetType.SPRUCE, 30, properties), buttonProperties());
	public static final Block BIRCH_BUTTON = register("birch_button", properties -> new ButtonBlock(BlockSetType.BIRCH, 30, properties), buttonProperties());
	public static final Block JUNGLE_BUTTON = register("jungle_button", properties -> new ButtonBlock(BlockSetType.JUNGLE, 30, properties), buttonProperties());
	public static final Block ACACIA_BUTTON = register("acacia_button", properties -> new ButtonBlock(BlockSetType.ACACIA, 30, properties), buttonProperties());
	public static final Block CHERRY_BUTTON = register("cherry_button", properties -> new ButtonBlock(BlockSetType.CHERRY, 30, properties), buttonProperties());
	public static final Block DARK_OAK_BUTTON = register(
		"dark_oak_button", properties -> new ButtonBlock(BlockSetType.DARK_OAK, 30, properties), buttonProperties()
	);
	public static final Block MANGROVE_BUTTON = register(
		"mangrove_button", properties -> new ButtonBlock(BlockSetType.MANGROVE, 30, properties), buttonProperties()
	);
	public static final Block BAMBOO_BUTTON = register("bamboo_button", properties -> new ButtonBlock(BlockSetType.BAMBOO, 30, properties), buttonProperties());
	public static final Block SKELETON_SKULL = register(
		"skeleton_skull",
		properties -> new SkullBlock(SkullBlock.Types.SKELETON, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.SKELETON).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block SKELETON_WALL_SKULL = register(
		"skeleton_wall_skull",
		properties -> new WallSkullBlock(SkullBlock.Types.SKELETON, properties),
		wallVariant(SKELETON_SKULL, true).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block WITHER_SKELETON_SKULL = register(
		"wither_skeleton_skull",
		WitherSkullBlock::new,
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.WITHER_SKELETON).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block WITHER_SKELETON_WALL_SKULL = register(
		"wither_skeleton_wall_skull", WitherWallSkullBlock::new, wallVariant(WITHER_SKELETON_SKULL, true).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block ZOMBIE_HEAD = register(
		"zombie_head",
		properties -> new SkullBlock(SkullBlock.Types.ZOMBIE, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.ZOMBIE).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block ZOMBIE_WALL_HEAD = register(
		"zombie_wall_head",
		properties -> new WallSkullBlock(SkullBlock.Types.ZOMBIE, properties),
		wallVariant(ZOMBIE_HEAD, true).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block PLAYER_HEAD = register(
		"player_head",
		PlayerHeadBlock::new,
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.CUSTOM_HEAD).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block PLAYER_WALL_HEAD = register(
		"player_wall_head", PlayerWallHeadBlock::new, wallVariant(PLAYER_HEAD, true).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block CREEPER_HEAD = register(
		"creeper_head",
		properties -> new SkullBlock(SkullBlock.Types.CREEPER, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.CREEPER).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block CREEPER_WALL_HEAD = register(
		"creeper_wall_head",
		properties -> new WallSkullBlock(SkullBlock.Types.CREEPER, properties),
		wallVariant(CREEPER_HEAD, true).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block DRAGON_HEAD = register(
		"dragon_head",
		properties -> new SkullBlock(SkullBlock.Types.DRAGON, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.DRAGON).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block DRAGON_WALL_HEAD = register(
		"dragon_wall_head",
		properties -> new WallSkullBlock(SkullBlock.Types.DRAGON, properties),
		wallVariant(DRAGON_HEAD, true).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block PIGLIN_HEAD = register(
		"piglin_head",
		properties -> new SkullBlock(SkullBlock.Types.PIGLIN, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.PIGLIN).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block PIGLIN_WALL_HEAD = register(
		"piglin_wall_head", PiglinWallSkullBlock::new, wallVariant(PIGLIN_HEAD, true).strength(1.0F).pushReaction(PushReaction.DESTROY)
	);
	public static final Block ANVIL = register(
		"anvil",
		AnvilBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(5.0F, 1200.0F)
			.sound(SoundType.ANVIL)
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block CHIPPED_ANVIL = register(
		"chipped_anvil",
		AnvilBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(5.0F, 1200.0F)
			.sound(SoundType.ANVIL)
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block DAMAGED_ANVIL = register(
		"damaged_anvil",
		AnvilBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(5.0F, 1200.0F)
			.sound(SoundType.ANVIL)
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block TRAPPED_CHEST = register(
		"trapped_chest",
		TrappedChestBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block LIGHT_WEIGHTED_PRESSURE_PLATE = register(
		"light_weighted_pressure_plate",
		properties -> new WeightedPressurePlateBlock(15, BlockSetType.GOLD, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.GOLD)
			.forceSolidOn()
			.requiresCorrectToolForDrops()
			.noCollission()
			.strength(0.5F)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block HEAVY_WEIGHTED_PRESSURE_PLATE = register(
		"heavy_weighted_pressure_plate",
		properties -> new WeightedPressurePlateBlock(150, BlockSetType.IRON, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.forceSolidOn()
			.requiresCorrectToolForDrops()
			.noCollission()
			.strength(0.5F)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block COMPARATOR = register(
		"comparator", ComparatorBlock::new, BlockBehaviour.Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)
	);
	public static final Block DAYLIGHT_DETECTOR = register(
		"daylight_detector",
		DaylightDetectorBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block REDSTONE_BLOCK = register(
		"redstone_block",
		PoweredBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.FIRE)
			.requiresCorrectToolForDrops()
			.strength(5.0F, 6.0F)
			.sound(SoundType.METAL)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block NETHER_QUARTZ_ORE = register(
		"nether_quartz_ore",
		properties -> new DropExperienceBlock(UniformInt.of(2, 5), properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(3.0F, 3.0F)
			.sound(SoundType.NETHER_ORE)
	);
	public static final Block HOPPER = register(
		"hopper",
		HopperBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 4.8F).sound(SoundType.METAL).noOcclusion()
	);
	public static final Block QUARTZ_BLOCK = register(
		"quartz_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
	);
	public static final Block CHISELED_QUARTZ_BLOCK = register(
		"chiseled_quartz_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
	);
	public static final Block QUARTZ_PILLAR = register(
		"quartz_pillar",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
	);
	public static final Block QUARTZ_STAIRS = registerLegacyStair("quartz_stairs", QUARTZ_BLOCK);
	public static final Block ACTIVATOR_RAIL = register(
		"activator_rail", PoweredRailBlock::new, BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL)
	);
	public static final Block DROPPER = register(
		"dropper",
		DropperBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)
	);
	public static final Block WHITE_TERRACOTTA = register(
		"white_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_WHITE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block ORANGE_TERRACOTTA = register(
		"orange_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_ORANGE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block MAGENTA_TERRACOTTA = register(
		"magenta_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_MAGENTA)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block LIGHT_BLUE_TERRACOTTA = register(
		"light_blue_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block YELLOW_TERRACOTTA = register(
		"yellow_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_YELLOW)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block LIME_TERRACOTTA = register(
		"lime_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_LIGHT_GREEN)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block PINK_TERRACOTTA = register(
		"pink_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_PINK)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block GRAY_TERRACOTTA = register(
		"gray_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_GRAY)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block LIGHT_GRAY_TERRACOTTA = register(
		"light_gray_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block CYAN_TERRACOTTA = register(
		"cyan_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_CYAN)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block PURPLE_TERRACOTTA = register(
		"purple_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_PURPLE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block BLUE_TERRACOTTA = register(
		"blue_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_BLUE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block BROWN_TERRACOTTA = register(
		"brown_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_BROWN)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block GREEN_TERRACOTTA = register(
		"green_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_GREEN)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block RED_TERRACOTTA = register(
		"red_terracotta",
		BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)
	);
	public static final Block BLACK_TERRACOTTA = register(
		"black_terracotta",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_BLACK)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.25F, 4.2F)
	);
	public static final Block WHITE_STAINED_GLASS_PANE = register(
		"white_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.WHITE, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block ORANGE_STAINED_GLASS_PANE = register(
		"orange_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.ORANGE, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block MAGENTA_STAINED_GLASS_PANE = register(
		"magenta_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.MAGENTA, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block LIGHT_BLUE_STAINED_GLASS_PANE = register(
		"light_blue_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.LIGHT_BLUE, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block YELLOW_STAINED_GLASS_PANE = register(
		"yellow_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.YELLOW, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block LIME_STAINED_GLASS_PANE = register(
		"lime_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.LIME, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block PINK_STAINED_GLASS_PANE = register(
		"pink_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.PINK, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block GRAY_STAINED_GLASS_PANE = register(
		"gray_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.GRAY, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block LIGHT_GRAY_STAINED_GLASS_PANE = register(
		"light_gray_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.LIGHT_GRAY, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block CYAN_STAINED_GLASS_PANE = register(
		"cyan_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.CYAN, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block PURPLE_STAINED_GLASS_PANE = register(
		"purple_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.PURPLE, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block BLUE_STAINED_GLASS_PANE = register(
		"blue_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.BLUE, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block BROWN_STAINED_GLASS_PANE = register(
		"brown_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.BROWN, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block GREEN_STAINED_GLASS_PANE = register(
		"green_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.GREEN, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block RED_STAINED_GLASS_PANE = register(
		"red_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.RED, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block BLACK_STAINED_GLASS_PANE = register(
		"black_stained_glass_pane",
		properties -> new StainedGlassPaneBlock(DyeColor.BLACK, properties),
		BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
	);
	public static final Block ACACIA_STAIRS = registerLegacyStair("acacia_stairs", ACACIA_PLANKS);
	public static final Block CHERRY_STAIRS = registerLegacyStair("cherry_stairs", CHERRY_PLANKS);
	public static final Block DARK_OAK_STAIRS = registerLegacyStair("dark_oak_stairs", DARK_OAK_PLANKS);
	public static final Block MANGROVE_STAIRS = registerLegacyStair("mangrove_stairs", MANGROVE_PLANKS);
	public static final Block BAMBOO_STAIRS = registerLegacyStair("bamboo_stairs", BAMBOO_PLANKS);
	public static final Block BAMBOO_MOSAIC_STAIRS = registerLegacyStair("bamboo_mosaic_stairs", BAMBOO_MOSAIC);
	public static final Block SLIME_BLOCK = register(
		"slime_block", SlimeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).friction(0.8F).sound(SoundType.SLIME_BLOCK).noOcclusion()
	);
	public static final Block BARRIER = register(
		"barrier",
		BarrierBlock::new,
		BlockBehaviour.Properties.of()
			.strength(-1.0F, 3600000.8F)
			.mapColor(waterloggedMapColor(MapColor.NONE))
			.noLootTable()
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.noTerrainParticles()
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block LIGHT = register(
		"light",
		LightBlock::new,
		BlockBehaviour.Properties.of()
			.replaceable()
			.strength(-1.0F, 3600000.8F)
			.mapColor(waterloggedMapColor(MapColor.NONE))
			.noLootTable()
			.noOcclusion()
			.lightLevel(LightBlock.LIGHT_EMISSION)
	);
	public static final Block IRON_TRAPDOOR = register(
		"iron_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.IRON, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F).noOcclusion().isValidSpawn(Blocks::never)
	);
	public static final Block PRISMARINE = register(
		"prismarine",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block PRISMARINE_BRICKS = register(
		"prismarine_bricks",
		BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block DARK_PRISMARINE = register(
		"dark_prismarine",
		BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block PRISMARINE_STAIRS = registerLegacyStair("prismarine_stairs", PRISMARINE);
	public static final Block PRISMARINE_BRICK_STAIRS = registerLegacyStair("prismarine_brick_stairs", PRISMARINE_BRICKS);
	public static final Block DARK_PRISMARINE_STAIRS = registerLegacyStair("dark_prismarine_stairs", DARK_PRISMARINE);
	public static final Block PRISMARINE_SLAB = register(
		"prismarine_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block PRISMARINE_BRICK_SLAB = register(
		"prismarine_brick_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block DARK_PRISMARINE_SLAB = register(
		"dark_prismarine_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block SEA_LANTERN = register(
		"sea_lantern",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.QUARTZ)
			.instrument(NoteBlockInstrument.HAT)
			.strength(0.3F)
			.sound(SoundType.GLASS)
			.lightLevel(blockStatex -> 15)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block HAY_BLOCK = register(
		"hay_block",
		HayBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).instrument(NoteBlockInstrument.BANJO).strength(0.5F).sound(SoundType.GRASS)
	);
	public static final Block WHITE_CARPET = register(
		"white_carpet",
		properties -> new WoolCarpetBlock(DyeColor.WHITE, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block ORANGE_CARPET = register(
		"orange_carpet",
		properties -> new WoolCarpetBlock(DyeColor.ORANGE, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block MAGENTA_CARPET = register(
		"magenta_carpet",
		properties -> new WoolCarpetBlock(DyeColor.MAGENTA, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block LIGHT_BLUE_CARPET = register(
		"light_blue_carpet",
		properties -> new WoolCarpetBlock(DyeColor.LIGHT_BLUE, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block YELLOW_CARPET = register(
		"yellow_carpet",
		properties -> new WoolCarpetBlock(DyeColor.YELLOW, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block LIME_CARPET = register(
		"lime_carpet",
		properties -> new WoolCarpetBlock(DyeColor.LIME, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block PINK_CARPET = register(
		"pink_carpet",
		properties -> new WoolCarpetBlock(DyeColor.PINK, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block GRAY_CARPET = register(
		"gray_carpet",
		properties -> new WoolCarpetBlock(DyeColor.GRAY, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block LIGHT_GRAY_CARPET = register(
		"light_gray_carpet",
		properties -> new WoolCarpetBlock(DyeColor.LIGHT_GRAY, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block CYAN_CARPET = register(
		"cyan_carpet",
		properties -> new WoolCarpetBlock(DyeColor.CYAN, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block PURPLE_CARPET = register(
		"purple_carpet",
		properties -> new WoolCarpetBlock(DyeColor.PURPLE, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block BLUE_CARPET = register(
		"blue_carpet",
		properties -> new WoolCarpetBlock(DyeColor.BLUE, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block BROWN_CARPET = register(
		"brown_carpet",
		properties -> new WoolCarpetBlock(DyeColor.BROWN, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block GREEN_CARPET = register(
		"green_carpet",
		properties -> new WoolCarpetBlock(DyeColor.GREEN, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block RED_CARPET = register(
		"red_carpet",
		properties -> new WoolCarpetBlock(DyeColor.RED, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block BLACK_CARPET = register(
		"black_carpet",
		properties -> new WoolCarpetBlock(DyeColor.BLACK, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
	);
	public static final Block TERRACOTTA = register(
		"terracotta",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)
	);
	public static final Block COAL_BLOCK = register(
		"coal_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)
	);
	public static final Block PACKED_ICE = register(
		"packed_ice",
		BlockBehaviour.Properties.of().mapColor(MapColor.ICE).instrument(NoteBlockInstrument.CHIME).friction(0.98F).strength(0.5F).sound(SoundType.GLASS)
	);
	public static final Block SUNFLOWER = register(
		"sunflower",
		TallFlowerBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block LILAC = register(
		"lilac",
		TallFlowerBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block ROSE_BUSH = register(
		"rose_bush",
		TallFlowerBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block PEONY = register(
		"peony",
		TallFlowerBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block TALL_GRASS = register(
		"tall_grass",
		DoublePlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block LARGE_FERN = register(
		"large_fern",
		DoublePlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block WHITE_BANNER = register(
		"white_banner",
		properties -> new BannerBlock(DyeColor.WHITE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block ORANGE_BANNER = register(
		"orange_banner",
		properties -> new BannerBlock(DyeColor.ORANGE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block MAGENTA_BANNER = register(
		"magenta_banner",
		properties -> new BannerBlock(DyeColor.MAGENTA, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block LIGHT_BLUE_BANNER = register(
		"light_blue_banner",
		properties -> new BannerBlock(DyeColor.LIGHT_BLUE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block YELLOW_BANNER = register(
		"yellow_banner",
		properties -> new BannerBlock(DyeColor.YELLOW, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block LIME_BANNER = register(
		"lime_banner",
		properties -> new BannerBlock(DyeColor.LIME, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block PINK_BANNER = register(
		"pink_banner",
		properties -> new BannerBlock(DyeColor.PINK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block GRAY_BANNER = register(
		"gray_banner",
		properties -> new BannerBlock(DyeColor.GRAY, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block LIGHT_GRAY_BANNER = register(
		"light_gray_banner",
		properties -> new BannerBlock(DyeColor.LIGHT_GRAY, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block CYAN_BANNER = register(
		"cyan_banner",
		properties -> new BannerBlock(DyeColor.CYAN, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block PURPLE_BANNER = register(
		"purple_banner",
		properties -> new BannerBlock(DyeColor.PURPLE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block BLUE_BANNER = register(
		"blue_banner",
		properties -> new BannerBlock(DyeColor.BLUE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block BROWN_BANNER = register(
		"brown_banner",
		properties -> new BannerBlock(DyeColor.BROWN, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block GREEN_BANNER = register(
		"green_banner",
		properties -> new BannerBlock(DyeColor.GREEN, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block RED_BANNER = register(
		"red_banner",
		properties -> new BannerBlock(DyeColor.RED, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block BLACK_BANNER = register(
		"black_banner",
		properties -> new BannerBlock(DyeColor.BLACK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block WHITE_WALL_BANNER = register(
		"white_wall_banner",
		properties -> new WallBannerBlock(DyeColor.WHITE, properties),
		wallVariant(WHITE_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block ORANGE_WALL_BANNER = register(
		"orange_wall_banner",
		properties -> new WallBannerBlock(DyeColor.ORANGE, properties),
		wallVariant(ORANGE_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block MAGENTA_WALL_BANNER = register(
		"magenta_wall_banner",
		properties -> new WallBannerBlock(DyeColor.MAGENTA, properties),
		wallVariant(MAGENTA_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block LIGHT_BLUE_WALL_BANNER = register(
		"light_blue_wall_banner",
		properties -> new WallBannerBlock(DyeColor.LIGHT_BLUE, properties),
		wallVariant(LIGHT_BLUE_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block YELLOW_WALL_BANNER = register(
		"yellow_wall_banner",
		properties -> new WallBannerBlock(DyeColor.YELLOW, properties),
		wallVariant(YELLOW_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block LIME_WALL_BANNER = register(
		"lime_wall_banner",
		properties -> new WallBannerBlock(DyeColor.LIME, properties),
		wallVariant(LIME_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block PINK_WALL_BANNER = register(
		"pink_wall_banner",
		properties -> new WallBannerBlock(DyeColor.PINK, properties),
		wallVariant(PINK_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block GRAY_WALL_BANNER = register(
		"gray_wall_banner",
		properties -> new WallBannerBlock(DyeColor.GRAY, properties),
		wallVariant(GRAY_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block LIGHT_GRAY_WALL_BANNER = register(
		"light_gray_wall_banner",
		properties -> new WallBannerBlock(DyeColor.LIGHT_GRAY, properties),
		wallVariant(LIGHT_GRAY_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block CYAN_WALL_BANNER = register(
		"cyan_wall_banner",
		properties -> new WallBannerBlock(DyeColor.CYAN, properties),
		wallVariant(CYAN_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block PURPLE_WALL_BANNER = register(
		"purple_wall_banner",
		properties -> new WallBannerBlock(DyeColor.PURPLE, properties),
		wallVariant(PURPLE_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block BLUE_WALL_BANNER = register(
		"blue_wall_banner",
		properties -> new WallBannerBlock(DyeColor.BLUE, properties),
		wallVariant(BLUE_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block BROWN_WALL_BANNER = register(
		"brown_wall_banner",
		properties -> new WallBannerBlock(DyeColor.BROWN, properties),
		wallVariant(BROWN_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block GREEN_WALL_BANNER = register(
		"green_wall_banner",
		properties -> new WallBannerBlock(DyeColor.GREEN, properties),
		wallVariant(GREEN_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block RED_WALL_BANNER = register(
		"red_wall_banner",
		properties -> new WallBannerBlock(DyeColor.RED, properties),
		wallVariant(RED_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block BLACK_WALL_BANNER = register(
		"black_wall_banner",
		properties -> new WallBannerBlock(DyeColor.BLACK, properties),
		wallVariant(BLACK_BANNER, true)
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block RED_SANDSTONE = register(
		"red_sandstone",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
	);
	public static final Block CHISELED_RED_SANDSTONE = register(
		"chiseled_red_sandstone",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
	);
	public static final Block CUT_RED_SANDSTONE = register(
		"cut_red_sandstone",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
	);
	public static final Block RED_SANDSTONE_STAIRS = registerLegacyStair("red_sandstone_stairs", RED_SANDSTONE);
	public static final Block OAK_SLAB = register(
		"oak_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block SPRUCE_SLAB = register(
		"spruce_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block BIRCH_SLAB = register(
		"birch_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block JUNGLE_SLAB = register(
		"jungle_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block ACACIA_SLAB = register(
		"acacia_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_ORANGE)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);
	public static final Block CHERRY_SLAB = register(
		"cherry_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_WHITE)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.CHERRY_WOOD)
			.ignitedByLava()
	);
	public static final Block DARK_OAK_SLAB = register(
		"dark_oak_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block MANGROVE_SLAB = register(
		"mangrove_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block BAMBOO_SLAB = register(
		"bamboo_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_YELLOW)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.BAMBOO_WOOD)
			.ignitedByLava()
	);
	public static final Block BAMBOO_MOSAIC_SLAB = register(
		"bamboo_mosaic_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_YELLOW)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.BAMBOO_WOOD)
			.ignitedByLava()
	);
	public static final Block STONE_SLAB = register(
		"stone_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block SMOOTH_STONE_SLAB = register(
		"smooth_stone_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block SANDSTONE_SLAB = register(
		"sandstone_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block CUT_SANDSTONE_SLAB = register(
		"cut_sandstone_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block PETRIFIED_OAK_SLAB = register(
		"petrified_oak_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block COBBLESTONE_SLAB = register(
		"cobblestone_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block BRICK_SLAB = register(
		"brick_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block STONE_BRICK_SLAB = register(
		"stone_brick_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block MUD_BRICK_SLAB = register(
		"mud_brick_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 3.0F)
			.sound(SoundType.MUD_BRICKS)
	);
	public static final Block NETHER_BRICK_SLAB = register(
		"nether_brick_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(2.0F, 6.0F)
			.sound(SoundType.NETHER_BRICKS)
	);
	public static final Block QUARTZ_SLAB = register(
		"quartz_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block RED_SANDSTONE_SLAB = register(
		"red_sandstone_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block CUT_RED_SANDSTONE_SLAB = register(
		"cut_red_sandstone_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block PURPUR_SLAB = register(
		"purpur_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block SMOOTH_STONE = register(
		"smooth_stone",
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block SMOOTH_SANDSTONE = register(
		"smooth_sandstone",
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block SMOOTH_QUARTZ = register(
		"smooth_quartz",
		BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block SMOOTH_RED_SANDSTONE = register(
		"smooth_red_sandstone",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
	);
	public static final Block SPRUCE_FENCE_GATE = register(
		"spruce_fence_gate",
		properties -> new FenceGateBlock(WoodType.SPRUCE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(SPRUCE_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
	);
	public static final Block BIRCH_FENCE_GATE = register(
		"birch_fence_gate",
		properties -> new FenceGateBlock(WoodType.BIRCH, properties),
		BlockBehaviour.Properties.of()
			.mapColor(BIRCH_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
	);
	public static final Block JUNGLE_FENCE_GATE = register(
		"jungle_fence_gate",
		properties -> new FenceGateBlock(WoodType.JUNGLE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(JUNGLE_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
	);
	public static final Block ACACIA_FENCE_GATE = register(
		"acacia_fence_gate",
		properties -> new FenceGateBlock(WoodType.ACACIA, properties),
		BlockBehaviour.Properties.of()
			.mapColor(ACACIA_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
	);
	public static final Block CHERRY_FENCE_GATE = register(
		"cherry_fence_gate",
		properties -> new FenceGateBlock(WoodType.CHERRY, properties),
		BlockBehaviour.Properties.of()
			.mapColor(CHERRY_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
	);
	public static final Block DARK_OAK_FENCE_GATE = register(
		"dark_oak_fence_gate",
		properties -> new FenceGateBlock(WoodType.DARK_OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(DARK_OAK_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
	);
	public static final Block MANGROVE_FENCE_GATE = register(
		"mangrove_fence_gate",
		properties -> new FenceGateBlock(WoodType.MANGROVE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MANGROVE_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
	);
	public static final Block BAMBOO_FENCE_GATE = register(
		"bamboo_fence_gate",
		properties -> new FenceGateBlock(WoodType.BAMBOO, properties),
		BlockBehaviour.Properties.of()
			.mapColor(BAMBOO_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
	);
	public static final Block SPRUCE_FENCE = register(
		"spruce_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(SPRUCE_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
			.sound(SoundType.WOOD)
	);
	public static final Block BIRCH_FENCE = register(
		"birch_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(BIRCH_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
			.sound(SoundType.WOOD)
	);
	public static final Block JUNGLE_FENCE = register(
		"jungle_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(JUNGLE_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
			.sound(SoundType.WOOD)
	);
	public static final Block ACACIA_FENCE = register(
		"acacia_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(ACACIA_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
			.sound(SoundType.WOOD)
	);
	public static final Block CHERRY_FENCE = register(
		"cherry_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(CHERRY_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
			.sound(SoundType.CHERRY_WOOD)
	);
	public static final Block DARK_OAK_FENCE = register(
		"dark_oak_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DARK_OAK_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
			.sound(SoundType.WOOD)
	);
	public static final Block MANGROVE_FENCE = register(
		"mangrove_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MANGROVE_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.ignitedByLava()
			.sound(SoundType.WOOD)
	);
	public static final Block BAMBOO_FENCE = register(
		"bamboo_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(BAMBOO_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.BAMBOO_WOOD)
			.ignitedByLava()
	);
	public static final Block SPRUCE_DOOR = register(
		"spruce_door",
		properties -> new DoorBlock(BlockSetType.SPRUCE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(SPRUCE_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BIRCH_DOOR = register(
		"birch_door",
		properties -> new DoorBlock(BlockSetType.BIRCH, properties),
		BlockBehaviour.Properties.of()
			.mapColor(BIRCH_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block JUNGLE_DOOR = register(
		"jungle_door",
		properties -> new DoorBlock(BlockSetType.JUNGLE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(JUNGLE_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block ACACIA_DOOR = register(
		"acacia_door",
		properties -> new DoorBlock(BlockSetType.ACACIA, properties),
		BlockBehaviour.Properties.of()
			.mapColor(ACACIA_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CHERRY_DOOR = register(
		"cherry_door",
		properties -> new DoorBlock(BlockSetType.CHERRY, properties),
		BlockBehaviour.Properties.of()
			.mapColor(CHERRY_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block DARK_OAK_DOOR = register(
		"dark_oak_door",
		properties -> new DoorBlock(BlockSetType.DARK_OAK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(DARK_OAK_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block MANGROVE_DOOR = register(
		"mangrove_door",
		properties -> new DoorBlock(BlockSetType.MANGROVE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MANGROVE_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BAMBOO_DOOR = register(
		"bamboo_door",
		properties -> new DoorBlock(BlockSetType.BAMBOO, properties),
		BlockBehaviour.Properties.of()
			.mapColor(BAMBOO_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block END_ROD = register(
		"end_rod", EndRodBlock::new, BlockBehaviour.Properties.of().forceSolidOff().instabreak().lightLevel(blockStatex -> 14).sound(SoundType.WOOD).noOcclusion()
	);
	public static final Block CHORUS_PLANT = register(
		"chorus_plant",
		ChorusPlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.forceSolidOff()
			.strength(0.4F)
			.sound(SoundType.WOOD)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CHORUS_FLOWER = register(
		"chorus_flower",
		properties -> new ChorusFlowerBlock(CHORUS_PLANT, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.forceSolidOff()
			.randomTicks()
			.strength(0.4F)
			.sound(SoundType.WOOD)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.pushReaction(PushReaction.DESTROY)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block PURPUR_BLOCK = register(
		"purpur_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block PURPUR_PILLAR = register(
		"purpur_pillar",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block PURPUR_STAIRS = registerLegacyStair("purpur_stairs", PURPUR_BLOCK);
	public static final Block END_STONE_BRICKS = register(
		"end_stone_bricks",
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F)
	);
	public static final Block TORCHFLOWER_CROP = register(
		"torchflower_crop",
		TorchflowerCropBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
	);
	public static final Block PITCHER_CROP = register(
		"pitcher_crop",
		PitcherCropBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
	);
	public static final Block PITCHER_PLANT = register(
		"pitcher_plant",
		DoublePlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.CROP)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BEETROOTS = register(
		"beetroots",
		BeetrootBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
	);
	public static final Block DIRT_PATH = register(
		"dirt_path",
		DirtPathBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS).isViewBlocking(Blocks::always).isSuffocating(Blocks::always)
	);
	public static final Block END_GATEWAY = register(
		"end_gateway",
		EndGatewayBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.noCollission()
			.lightLevel(blockStatex -> 15)
			.strength(-1.0F, 3600000.0F)
			.noLootTable()
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block REPEATING_COMMAND_BLOCK = register(
		"repeating_command_block",
		properties -> new CommandBlock(false, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()
	);
	public static final Block CHAIN_COMMAND_BLOCK = register(
		"chain_command_block",
		properties -> new CommandBlock(true, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()
	);
	public static final Block FROSTED_ICE = register(
		"frosted_ice",
		FrostedIceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.ICE)
			.friction(0.98F)
			.strength(0.5F)
			.sound(SoundType.GLASS)
			.noOcclusion()
			.isValidSpawn((blockStatex, blockGetter, blockPos, entityType) -> entityType == EntityType.POLAR_BEAR)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block MAGMA_BLOCK = register(
		"magma_block",
		MagmaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.lightLevel(blockStatex -> 3)
			.strength(0.5F)
			.isValidSpawn((blockStatex, blockGetter, blockPos, entityType) -> entityType.fireImmune())
			.hasPostProcess(Blocks::always)
			.emissiveRendering(Blocks::always)
	);
	public static final Block NETHER_WART_BLOCK = register(
		"nether_wart_block", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.WART_BLOCK)
	);
	public static final Block RED_NETHER_BRICKS = register(
		"red_nether_bricks",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(2.0F, 6.0F)
			.sound(SoundType.NETHER_BRICKS)
	);
	public static final Block BONE_BLOCK = register(
		"bone_block",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.SAND)
			.instrument(NoteBlockInstrument.XYLOPHONE)
			.requiresCorrectToolForDrops()
			.strength(2.0F)
			.sound(SoundType.BONE_BLOCK)
	);
	public static final Block STRUCTURE_VOID = register(
		"structure_void",
		StructureVoidBlock::new,
		BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().noTerrainParticles().pushReaction(PushReaction.DESTROY)
	);
	public static final Block OBSERVER = register(
		"observer",
		ObserverBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.strength(3.0F)
			.requiresCorrectToolForDrops()
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block SHULKER_BOX = register(
		"shulker_box", properties -> new ShulkerBoxBlock(null, properties), shulkerBoxProperties(MapColor.COLOR_PURPLE)
	);
	public static final Block WHITE_SHULKER_BOX = register(
		"white_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.WHITE, properties), shulkerBoxProperties(MapColor.SNOW)
	);
	public static final Block ORANGE_SHULKER_BOX = register(
		"orange_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.ORANGE, properties), shulkerBoxProperties(MapColor.COLOR_ORANGE)
	);
	public static final Block MAGENTA_SHULKER_BOX = register(
		"magenta_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.MAGENTA, properties), shulkerBoxProperties(MapColor.COLOR_MAGENTA)
	);
	public static final Block LIGHT_BLUE_SHULKER_BOX = register(
		"light_blue_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.LIGHT_BLUE, properties), shulkerBoxProperties(MapColor.COLOR_LIGHT_BLUE)
	);
	public static final Block YELLOW_SHULKER_BOX = register(
		"yellow_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.YELLOW, properties), shulkerBoxProperties(MapColor.COLOR_YELLOW)
	);
	public static final Block LIME_SHULKER_BOX = register(
		"lime_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.LIME, properties), shulkerBoxProperties(MapColor.COLOR_LIGHT_GREEN)
	);
	public static final Block PINK_SHULKER_BOX = register(
		"pink_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.PINK, properties), shulkerBoxProperties(MapColor.COLOR_PINK)
	);
	public static final Block GRAY_SHULKER_BOX = register(
		"gray_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.GRAY, properties), shulkerBoxProperties(MapColor.COLOR_GRAY)
	);
	public static final Block LIGHT_GRAY_SHULKER_BOX = register(
		"light_gray_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.LIGHT_GRAY, properties), shulkerBoxProperties(MapColor.COLOR_LIGHT_GRAY)
	);
	public static final Block CYAN_SHULKER_BOX = register(
		"cyan_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.CYAN, properties), shulkerBoxProperties(MapColor.COLOR_CYAN)
	);
	public static final Block PURPLE_SHULKER_BOX = register(
		"purple_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.PURPLE, properties), shulkerBoxProperties(MapColor.TERRACOTTA_PURPLE)
	);
	public static final Block BLUE_SHULKER_BOX = register(
		"blue_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.BLUE, properties), shulkerBoxProperties(MapColor.COLOR_BLUE)
	);
	public static final Block BROWN_SHULKER_BOX = register(
		"brown_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.BROWN, properties), shulkerBoxProperties(MapColor.COLOR_BROWN)
	);
	public static final Block GREEN_SHULKER_BOX = register(
		"green_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.GREEN, properties), shulkerBoxProperties(MapColor.COLOR_GREEN)
	);
	public static final Block RED_SHULKER_BOX = register(
		"red_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.RED, properties), shulkerBoxProperties(MapColor.COLOR_RED)
	);
	public static final Block BLACK_SHULKER_BOX = register(
		"black_shulker_box", properties -> new ShulkerBoxBlock(DyeColor.BLACK, properties), shulkerBoxProperties(MapColor.COLOR_BLACK)
	);
	public static final Block WHITE_GLAZED_TERRACOTTA = register(
		"white_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.WHITE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block ORANGE_GLAZED_TERRACOTTA = register(
		"orange_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.ORANGE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block MAGENTA_GLAZED_TERRACOTTA = register(
		"magenta_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.MAGENTA)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block LIGHT_BLUE_GLAZED_TERRACOTTA = register(
		"light_blue_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.LIGHT_BLUE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block YELLOW_GLAZED_TERRACOTTA = register(
		"yellow_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.YELLOW)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block LIME_GLAZED_TERRACOTTA = register(
		"lime_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.LIME)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block PINK_GLAZED_TERRACOTTA = register(
		"pink_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.PINK)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block GRAY_GLAZED_TERRACOTTA = register(
		"gray_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.GRAY)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block LIGHT_GRAY_GLAZED_TERRACOTTA = register(
		"light_gray_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.LIGHT_GRAY)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block CYAN_GLAZED_TERRACOTTA = register(
		"cyan_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.CYAN)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block PURPLE_GLAZED_TERRACOTTA = register(
		"purple_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.PURPLE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block BLUE_GLAZED_TERRACOTTA = register(
		"blue_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.BLUE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block BROWN_GLAZED_TERRACOTTA = register(
		"brown_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.BROWN)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block GREEN_GLAZED_TERRACOTTA = register(
		"green_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.GREEN)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block RED_GLAZED_TERRACOTTA = register(
		"red_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.RED)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block BLACK_GLAZED_TERRACOTTA = register(
		"black_glazed_terracotta",
		GlazedTerracottaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(DyeColor.BLACK)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.4F)
			.pushReaction(PushReaction.PUSH_ONLY)
	);
	public static final Block WHITE_CONCRETE = register(
		"white_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.WHITE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block ORANGE_CONCRETE = register(
		"orange_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block MAGENTA_CONCRETE = register(
		"magenta_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block LIGHT_BLUE_CONCRETE = register(
		"light_blue_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_BLUE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block YELLOW_CONCRETE = register(
		"yellow_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.YELLOW).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block LIME_CONCRETE = register(
		"lime_concrete", BlockBehaviour.Properties.of().mapColor(DyeColor.LIME).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block PINK_CONCRETE = register(
		"pink_concrete", BlockBehaviour.Properties.of().mapColor(DyeColor.PINK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block GRAY_CONCRETE = register(
		"gray_concrete", BlockBehaviour.Properties.of().mapColor(DyeColor.GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block LIGHT_GRAY_CONCRETE = register(
		"light_gray_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block CYAN_CONCRETE = register(
		"cyan_concrete", BlockBehaviour.Properties.of().mapColor(DyeColor.CYAN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block PURPLE_CONCRETE = register(
		"purple_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.PURPLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block BLUE_CONCRETE = register(
		"blue_concrete", BlockBehaviour.Properties.of().mapColor(DyeColor.BLUE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block BROWN_CONCRETE = register(
		"brown_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.BROWN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block GREEN_CONCRETE = register(
		"green_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.GREEN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block RED_CONCRETE = register(
		"red_concrete", BlockBehaviour.Properties.of().mapColor(DyeColor.RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block BLACK_CONCRETE = register(
		"black_concrete",
		BlockBehaviour.Properties.of().mapColor(DyeColor.BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)
	);
	public static final Block WHITE_CONCRETE_POWDER = register(
		"white_concrete_powder",
		properties -> new ConcretePowderBlock(WHITE_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.WHITE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block ORANGE_CONCRETE_POWDER = register(
		"orange_concrete_powder",
		properties -> new ConcretePowderBlock(ORANGE_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.ORANGE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block MAGENTA_CONCRETE_POWDER = register(
		"magenta_concrete_powder",
		properties -> new ConcretePowderBlock(MAGENTA_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block LIGHT_BLUE_CONCRETE_POWDER = register(
		"light_blue_concrete_powder",
		properties -> new ConcretePowderBlock(LIGHT_BLUE_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_BLUE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block YELLOW_CONCRETE_POWDER = register(
		"yellow_concrete_powder",
		properties -> new ConcretePowderBlock(YELLOW_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.YELLOW).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block LIME_CONCRETE_POWDER = register(
		"lime_concrete_powder",
		properties -> new ConcretePowderBlock(LIME_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.LIME).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block PINK_CONCRETE_POWDER = register(
		"pink_concrete_powder",
		properties -> new ConcretePowderBlock(PINK_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.PINK).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block GRAY_CONCRETE_POWDER = register(
		"gray_concrete_powder",
		properties -> new ConcretePowderBlock(GRAY_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.GRAY).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block LIGHT_GRAY_CONCRETE_POWDER = register(
		"light_gray_concrete_powder",
		properties -> new ConcretePowderBlock(LIGHT_GRAY_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_GRAY).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block CYAN_CONCRETE_POWDER = register(
		"cyan_concrete_powder",
		properties -> new ConcretePowderBlock(CYAN_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.CYAN).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block PURPLE_CONCRETE_POWDER = register(
		"purple_concrete_powder",
		properties -> new ConcretePowderBlock(PURPLE_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.PURPLE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block BLUE_CONCRETE_POWDER = register(
		"blue_concrete_powder",
		properties -> new ConcretePowderBlock(BLUE_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.BLUE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block BROWN_CONCRETE_POWDER = register(
		"brown_concrete_powder",
		properties -> new ConcretePowderBlock(BROWN_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.BROWN).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block GREEN_CONCRETE_POWDER = register(
		"green_concrete_powder",
		properties -> new ConcretePowderBlock(GREEN_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.GREEN).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block RED_CONCRETE_POWDER = register(
		"red_concrete_powder",
		properties -> new ConcretePowderBlock(RED_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.RED).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block BLACK_CONCRETE_POWDER = register(
		"black_concrete_powder",
		properties -> new ConcretePowderBlock(BLACK_CONCRETE, properties),
		BlockBehaviour.Properties.of().mapColor(DyeColor.BLACK).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
	);
	public static final Block KELP = register(
		"kelp",
		KelpBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WATER)
			.noCollission()
			.randomTicks()
			.instabreak()
			.sound(SoundType.WET_GRASS)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block KELP_PLANT = register(
		"kelp_plant",
		KelpPlantBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WATER).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block DRIED_KELP_BLOCK = register(
		"dried_kelp_block", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.5F, 2.5F).sound(SoundType.GRASS)
	);
	public static final Block TURTLE_EGG = register(
		"turtle_egg",
		TurtleEggBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.SAND)
			.forceSolidOn()
			.strength(0.5F)
			.sound(SoundType.METAL)
			.randomTicks()
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SNIFFER_EGG = register(
		"sniffer_egg", SnifferEggBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.5F).sound(SoundType.METAL).noOcclusion()
	);
	public static final Block DEAD_TUBE_CORAL_BLOCK = register(
		"dead_tube_coral_block",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
	);
	public static final Block DEAD_BRAIN_CORAL_BLOCK = register(
		"dead_brain_coral_block",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
	);
	public static final Block DEAD_BUBBLE_CORAL_BLOCK = register(
		"dead_bubble_coral_block",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
	);
	public static final Block DEAD_FIRE_CORAL_BLOCK = register(
		"dead_fire_coral_block",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
	);
	public static final Block DEAD_HORN_CORAL_BLOCK = register(
		"dead_horn_coral_block",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
	);
	public static final Block TUBE_CORAL_BLOCK = register(
		"tube_coral_block",
		properties -> new CoralBlock(DEAD_TUBE_CORAL_BLOCK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLUE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
			.sound(SoundType.CORAL_BLOCK)
	);
	public static final Block BRAIN_CORAL_BLOCK = register(
		"brain_coral_block",
		properties -> new CoralBlock(DEAD_BRAIN_CORAL_BLOCK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PINK)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
			.sound(SoundType.CORAL_BLOCK)
	);
	public static final Block BUBBLE_CORAL_BLOCK = register(
		"bubble_coral_block",
		properties -> new CoralBlock(DEAD_BUBBLE_CORAL_BLOCK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
			.sound(SoundType.CORAL_BLOCK)
	);
	public static final Block FIRE_CORAL_BLOCK = register(
		"fire_coral_block",
		properties -> new CoralBlock(DEAD_FIRE_CORAL_BLOCK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_RED)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
			.sound(SoundType.CORAL_BLOCK)
	);
	public static final Block HORN_CORAL_BLOCK = register(
		"horn_coral_block",
		properties -> new CoralBlock(DEAD_HORN_CORAL_BLOCK, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_YELLOW)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
			.sound(SoundType.CORAL_BLOCK)
	);
	public static final Block DEAD_TUBE_CORAL = register(
		"dead_tube_coral",
		BaseCoralPlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_BRAIN_CORAL = register(
		"dead_brain_coral",
		BaseCoralPlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_BUBBLE_CORAL = register(
		"dead_bubble_coral",
		BaseCoralPlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_FIRE_CORAL = register(
		"dead_fire_coral",
		BaseCoralPlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_HORN_CORAL = register(
		"dead_horn_coral",
		BaseCoralPlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block TUBE_CORAL = register(
		"tube_coral",
		properties -> new CoralPlantBlock(DEAD_TUBE_CORAL, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block BRAIN_CORAL = register(
		"brain_coral",
		properties -> new CoralPlantBlock(DEAD_BRAIN_CORAL, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block BUBBLE_CORAL = register(
		"bubble_coral",
		properties -> new CoralPlantBlock(DEAD_BUBBLE_CORAL, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block FIRE_CORAL = register(
		"fire_coral",
		properties -> new CoralPlantBlock(DEAD_FIRE_CORAL, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block HORN_CORAL = register(
		"horn_coral",
		properties -> new CoralPlantBlock(DEAD_HORN_CORAL, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block DEAD_TUBE_CORAL_FAN = register(
		"dead_tube_coral_fan",
		BaseCoralFanBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_BRAIN_CORAL_FAN = register(
		"dead_brain_coral_fan",
		BaseCoralFanBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_BUBBLE_CORAL_FAN = register(
		"dead_bubble_coral_fan",
		BaseCoralFanBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_FIRE_CORAL_FAN = register(
		"dead_fire_coral_fan",
		BaseCoralFanBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_HORN_CORAL_FAN = register(
		"dead_horn_coral_fan",
		BaseCoralFanBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block TUBE_CORAL_FAN = register(
		"tube_coral_fan",
		properties -> new CoralFanBlock(DEAD_TUBE_CORAL_FAN, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block BRAIN_CORAL_FAN = register(
		"brain_coral_fan",
		properties -> new CoralFanBlock(DEAD_BRAIN_CORAL_FAN, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block BUBBLE_CORAL_FAN = register(
		"bubble_coral_fan",
		properties -> new CoralFanBlock(DEAD_BUBBLE_CORAL_FAN, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block FIRE_CORAL_FAN = register(
		"fire_coral_fan",
		properties -> new CoralFanBlock(DEAD_FIRE_CORAL_FAN, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block HORN_CORAL_FAN = register(
		"horn_coral_fan",
		properties -> new CoralFanBlock(DEAD_HORN_CORAL_FAN, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block DEAD_TUBE_CORAL_WALL_FAN = register(
		"dead_tube_coral_wall_fan",
		BaseCoralWallFanBlock::new,
		wallVariant(DEAD_TUBE_CORAL_FAN, false)
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_BRAIN_CORAL_WALL_FAN = register(
		"dead_brain_coral_wall_fan",
		BaseCoralWallFanBlock::new,
		wallVariant(DEAD_BRAIN_CORAL_FAN, false)
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_BUBBLE_CORAL_WALL_FAN = register(
		"dead_bubble_coral_wall_fan",
		BaseCoralWallFanBlock::new,
		wallVariant(DEAD_BUBBLE_CORAL_FAN, false)
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_FIRE_CORAL_WALL_FAN = register(
		"dead_fire_coral_wall_fan",
		BaseCoralWallFanBlock::new,
		wallVariant(DEAD_FIRE_CORAL_FAN, false)
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block DEAD_HORN_CORAL_WALL_FAN = register(
		"dead_horn_coral_wall_fan",
		BaseCoralWallFanBlock::new,
		wallVariant(DEAD_HORN_CORAL_FAN, false)
			.mapColor(MapColor.COLOR_GRAY)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.instabreak()
	);
	public static final Block TUBE_CORAL_WALL_FAN = register(
		"tube_coral_wall_fan",
		properties -> new CoralWallFanBlock(DEAD_TUBE_CORAL_WALL_FAN, properties),
		wallVariant(TUBE_CORAL_FAN, false).mapColor(MapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block BRAIN_CORAL_WALL_FAN = register(
		"brain_coral_wall_fan",
		properties -> new CoralWallFanBlock(DEAD_BRAIN_CORAL_WALL_FAN, properties),
		wallVariant(BRAIN_CORAL_FAN, false).mapColor(MapColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block BUBBLE_CORAL_WALL_FAN = register(
		"bubble_coral_wall_fan",
		properties -> new CoralWallFanBlock(DEAD_BUBBLE_CORAL_WALL_FAN, properties),
		wallVariant(BUBBLE_CORAL_FAN, false)
			.mapColor(MapColor.COLOR_PURPLE)
			.noCollission()
			.instabreak()
			.sound(SoundType.WET_GRASS)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block FIRE_CORAL_WALL_FAN = register(
		"fire_coral_wall_fan",
		properties -> new CoralWallFanBlock(DEAD_FIRE_CORAL_WALL_FAN, properties),
		wallVariant(FIRE_CORAL_FAN, false).mapColor(MapColor.COLOR_RED).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block HORN_CORAL_WALL_FAN = register(
		"horn_coral_wall_fan",
		properties -> new CoralWallFanBlock(DEAD_HORN_CORAL_WALL_FAN, properties),
		wallVariant(HORN_CORAL_FAN, false).mapColor(MapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block SEA_PICKLE = register(
		"sea_pickle",
		SeaPickleBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GREEN)
			.lightLevel(blockStatex -> SeaPickleBlock.isDead(blockStatex) ? 0 : 3 + 3 * (Integer)blockStatex.getValue(SeaPickleBlock.PICKLES))
			.sound(SoundType.SLIME_BLOCK)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BLUE_ICE = register(
		"blue_ice", HalfTransparentBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(2.8F).friction(0.989F).sound(SoundType.GLASS)
	);
	public static final Block CONDUIT = register(
		"conduit",
		ConduitBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.DIAMOND)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.HAT)
			.strength(3.0F)
			.lightLevel(blockStatex -> 15)
			.noOcclusion()
	);
	public static final Block BAMBOO_SAPLING = register(
		"bamboo_sapling",
		BambooSaplingBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.forceSolidOn()
			.randomTicks()
			.instabreak()
			.noCollission()
			.strength(1.0F)
			.sound(SoundType.BAMBOO_SAPLING)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block BAMBOO = register(
		"bamboo",
		BambooStalkBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.forceSolidOn()
			.randomTicks()
			.instabreak()
			.strength(1.0F)
			.sound(SoundType.BAMBOO)
			.noOcclusion()
			.dynamicShape()
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block POTTED_BAMBOO = register("potted_bamboo", properties -> new FlowerPotBlock(BAMBOO, properties), flowerPotProperties());
	public static final Block VOID_AIR = register("void_air", AirBlock::new, BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().air());
	public static final Block CAVE_AIR = register("cave_air", AirBlock::new, BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().air());
	public static final Block BUBBLE_COLUMN = register(
		"bubble_column",
		BubbleColumnBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WATER)
			.replaceable()
			.noCollission()
			.noLootTable()
			.pushReaction(PushReaction.DESTROY)
			.liquid()
			.sound(SoundType.EMPTY)
	);
	public static final Block POLISHED_GRANITE_STAIRS = registerLegacyStair("polished_granite_stairs", POLISHED_GRANITE);
	public static final Block SMOOTH_RED_SANDSTONE_STAIRS = registerLegacyStair("smooth_red_sandstone_stairs", SMOOTH_RED_SANDSTONE);
	public static final Block MOSSY_STONE_BRICK_STAIRS = registerLegacyStair("mossy_stone_brick_stairs", MOSSY_STONE_BRICKS);
	public static final Block POLISHED_DIORITE_STAIRS = registerLegacyStair("polished_diorite_stairs", POLISHED_DIORITE);
	public static final Block MOSSY_COBBLESTONE_STAIRS = registerLegacyStair("mossy_cobblestone_stairs", MOSSY_COBBLESTONE);
	public static final Block END_STONE_BRICK_STAIRS = registerLegacyStair("end_stone_brick_stairs", END_STONE_BRICKS);
	public static final Block STONE_STAIRS = registerLegacyStair("stone_stairs", STONE);
	public static final Block SMOOTH_SANDSTONE_STAIRS = registerLegacyStair("smooth_sandstone_stairs", SMOOTH_SANDSTONE);
	public static final Block SMOOTH_QUARTZ_STAIRS = registerLegacyStair("smooth_quartz_stairs", SMOOTH_QUARTZ);
	public static final Block GRANITE_STAIRS = registerLegacyStair("granite_stairs", GRANITE);
	public static final Block ANDESITE_STAIRS = registerLegacyStair("andesite_stairs", ANDESITE);
	public static final Block RED_NETHER_BRICK_STAIRS = registerLegacyStair("red_nether_brick_stairs", RED_NETHER_BRICKS);
	public static final Block POLISHED_ANDESITE_STAIRS = registerLegacyStair("polished_andesite_stairs", POLISHED_ANDESITE);
	public static final Block DIORITE_STAIRS = registerLegacyStair("diorite_stairs", DIORITE);
	public static final Block POLISHED_GRANITE_SLAB = register("polished_granite_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_GRANITE));
	public static final Block SMOOTH_RED_SANDSTONE_SLAB = register(
		"smooth_red_sandstone_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(SMOOTH_RED_SANDSTONE)
	);
	public static final Block MOSSY_STONE_BRICK_SLAB = register(
		"mossy_stone_brick_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(MOSSY_STONE_BRICKS)
	);
	public static final Block POLISHED_DIORITE_SLAB = register("polished_diorite_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_DIORITE));
	public static final Block MOSSY_COBBLESTONE_SLAB = register(
		"mossy_cobblestone_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(MOSSY_COBBLESTONE)
	);
	public static final Block END_STONE_BRICK_SLAB = register("end_stone_brick_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(END_STONE_BRICKS));
	public static final Block SMOOTH_SANDSTONE_SLAB = register("smooth_sandstone_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(SMOOTH_SANDSTONE));
	public static final Block SMOOTH_QUARTZ_SLAB = register("smooth_quartz_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(SMOOTH_QUARTZ));
	public static final Block GRANITE_SLAB = register("granite_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(GRANITE));
	public static final Block ANDESITE_SLAB = register("andesite_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(ANDESITE));
	public static final Block RED_NETHER_BRICK_SLAB = register("red_nether_brick_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(RED_NETHER_BRICKS));
	public static final Block POLISHED_ANDESITE_SLAB = register(
		"polished_andesite_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_ANDESITE)
	);
	public static final Block DIORITE_SLAB = register("diorite_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(DIORITE));
	public static final Block BRICK_WALL = register("brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(BRICKS).forceSolidOn());
	public static final Block PRISMARINE_WALL = register("prismarine_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(PRISMARINE).forceSolidOn());
	public static final Block RED_SANDSTONE_WALL = register(
		"red_sandstone_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(RED_SANDSTONE).forceSolidOn()
	);
	public static final Block MOSSY_STONE_BRICK_WALL = register(
		"mossy_stone_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(MOSSY_STONE_BRICKS).forceSolidOn()
	);
	public static final Block GRANITE_WALL = register("granite_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(GRANITE).forceSolidOn());
	public static final Block STONE_BRICK_WALL = register("stone_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(STONE_BRICKS).forceSolidOn());
	public static final Block MUD_BRICK_WALL = register("mud_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(MUD_BRICKS).forceSolidOn());
	public static final Block NETHER_BRICK_WALL = register(
		"nether_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(NETHER_BRICKS).forceSolidOn()
	);
	public static final Block ANDESITE_WALL = register("andesite_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(ANDESITE).forceSolidOn());
	public static final Block RED_NETHER_BRICK_WALL = register(
		"red_nether_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(RED_NETHER_BRICKS).forceSolidOn()
	);
	public static final Block SANDSTONE_WALL = register("sandstone_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(SANDSTONE).forceSolidOn());
	public static final Block END_STONE_BRICK_WALL = register(
		"end_stone_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(END_STONE_BRICKS).forceSolidOn()
	);
	public static final Block DIORITE_WALL = register("diorite_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(DIORITE).forceSolidOn());
	public static final Block SCAFFOLDING = register(
		"scaffolding",
		ScaffoldingBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.SAND)
			.noCollission()
			.sound(SoundType.SCAFFOLDING)
			.dynamicShape()
			.isValidSpawn(Blocks::never)
			.pushReaction(PushReaction.DESTROY)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block LOOM = register(
		"loom",
		LoomBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block BARREL = register(
		"barrel",
		BarrelBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block SMOKER = register(
		"smoker",
		SmokerBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(3.5F)
			.lightLevel(litBlockEmission(13))
	);
	public static final Block BLAST_FURNACE = register(
		"blast_furnace",
		BlastFurnaceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(3.5F)
			.lightLevel(litBlockEmission(13))
	);
	public static final Block CARTOGRAPHY_TABLE = register(
		"cartography_table",
		CartographyTableBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block FLETCHING_TABLE = register(
		"fletching_table",
		FletchingTableBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block GRINDSTONE = register(
		"grindstone",
		GrindstoneBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(2.0F, 6.0F)
			.sound(SoundType.STONE)
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block LECTERN = register(
		"lectern",
		LecternBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block SMITHING_TABLE = register(
		"smithing_table",
		SmithingTableBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block STONECUTTER = register(
		"stonecutter",
		StonecutterBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)
	);
	public static final Block BELL = register(
		"bell",
		BellBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.GOLD)
			.forceSolidOn()
			.requiresCorrectToolForDrops()
			.strength(5.0F)
			.sound(SoundType.ANVIL)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block LANTERN = register(
		"lantern",
		LanternBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.forceSolidOn()
			.requiresCorrectToolForDrops()
			.strength(3.5F)
			.sound(SoundType.LANTERN)
			.lightLevel(blockStatex -> 15)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SOUL_LANTERN = register(
		"soul_lantern",
		LanternBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.forceSolidOn()
			.requiresCorrectToolForDrops()
			.strength(3.5F)
			.sound(SoundType.LANTERN)
			.lightLevel(blockStatex -> 10)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CAMPFIRE = register(
		"campfire",
		properties -> new CampfireBlock(true, 1, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PODZOL)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F)
			.sound(SoundType.WOOD)
			.lightLevel(litBlockEmission(15))
			.noOcclusion()
			.ignitedByLava()
	);
	public static final Block SOUL_CAMPFIRE = register(
		"soul_campfire",
		properties -> new CampfireBlock(false, 2, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PODZOL)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F)
			.sound(SoundType.WOOD)
			.lightLevel(litBlockEmission(10))
			.noOcclusion()
			.ignitedByLava()
	);
	public static final Block SWEET_BERRY_BUSH = register(
		"sweet_berry_bush",
		SweetBerryBushBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)
	);
	public static final Block WARPED_STEM = register("warped_stem", RotatedPillarBlock::new, netherStemProperties(MapColor.WARPED_STEM));
	public static final Block STRIPPED_WARPED_STEM = register("stripped_warped_stem", RotatedPillarBlock::new, netherStemProperties(MapColor.WARPED_STEM));
	public static final Block WARPED_HYPHAE = register(
		"warped_hyphae",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_HYPHAE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM)
	);
	public static final Block STRIPPED_WARPED_HYPHAE = register(
		"stripped_warped_hyphae",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_HYPHAE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM)
	);
	public static final Block WARPED_NYLIUM = register(
		"warped_nylium",
		NyliumBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WARPED_NYLIUM)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(0.4F)
			.sound(SoundType.NYLIUM)
			.randomTicks()
	);
	public static final Block WARPED_FUNGUS = register(
		"warped_fungus",
		properties -> new FungusBlock(TreeFeatures.WARPED_FUNGUS_PLANTED, WARPED_NYLIUM, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).instabreak().noCollission().sound(SoundType.FUNGUS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block WARPED_WART_BLOCK = register(
		"warped_wart_block", BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_WART_BLOCK).strength(1.0F).sound(SoundType.WART_BLOCK)
	);
	public static final Block WARPED_ROOTS = register(
		"warped_roots",
		RootsBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_CYAN)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.ROOTS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block NETHER_SPROUTS = register(
		"nether_sprouts",
		NetherSproutsBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_CYAN)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.NETHER_SPROUTS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CRIMSON_STEM = register("crimson_stem", RotatedPillarBlock::new, netherStemProperties(MapColor.CRIMSON_STEM));
	public static final Block STRIPPED_CRIMSON_STEM = register("stripped_crimson_stem", RotatedPillarBlock::new, netherStemProperties(MapColor.CRIMSON_STEM));
	public static final Block CRIMSON_HYPHAE = register(
		"crimson_hyphae",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_HYPHAE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM)
	);
	public static final Block STRIPPED_CRIMSON_HYPHAE = register(
		"stripped_crimson_hyphae",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_HYPHAE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM)
	);
	public static final Block CRIMSON_NYLIUM = register(
		"crimson_nylium",
		NyliumBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.CRIMSON_NYLIUM)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(0.4F)
			.sound(SoundType.NYLIUM)
			.randomTicks()
	);
	public static final Block CRIMSON_FUNGUS = register(
		"crimson_fungus",
		properties -> new FungusBlock(TreeFeatures.CRIMSON_FUNGUS_PLANTED, CRIMSON_NYLIUM, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instabreak().noCollission().sound(SoundType.FUNGUS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block SHROOMLIGHT = register(
		"shroomlight", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.SHROOMLIGHT).lightLevel(blockStatex -> 15)
	);
	public static final Block WEEPING_VINES = register(
		"weeping_vines",
		WeepingVinesBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.randomTicks()
			.noCollission()
			.instabreak()
			.sound(SoundType.WEEPING_VINES)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block WEEPING_VINES_PLANT = register(
		"weeping_vines_plant",
		WeepingVinesPlantBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).noCollission().instabreak().sound(SoundType.WEEPING_VINES).pushReaction(PushReaction.DESTROY)
	);
	public static final Block TWISTING_VINES = register(
		"twisting_vines",
		TwistingVinesBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_CYAN)
			.randomTicks()
			.noCollission()
			.instabreak()
			.sound(SoundType.WEEPING_VINES)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block TWISTING_VINES_PLANT = register(
		"twisting_vines_plant",
		TwistingVinesPlantBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).noCollission().instabreak().sound(SoundType.WEEPING_VINES).pushReaction(PushReaction.DESTROY)
	);
	public static final Block CRIMSON_ROOTS = register(
		"crimson_roots",
		RootsBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.ROOTS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CRIMSON_PLANKS = register(
		"crimson_planks",
		BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_STEM).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD)
	);
	public static final Block WARPED_PLANKS = register(
		"warped_planks",
		BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_STEM).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD)
	);
	public static final Block CRIMSON_SLAB = register(
		"crimson_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(CRIMSON_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.NETHER_WOOD)
	);
	public static final Block WARPED_SLAB = register(
		"warped_slab",
		SlabBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(WARPED_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.NETHER_WOOD)
	);
	public static final Block CRIMSON_PRESSURE_PLATE = register(
		"crimson_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.CRIMSON, properties),
		BlockBehaviour.Properties.of()
			.mapColor(CRIMSON_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block WARPED_PRESSURE_PLATE = register(
		"warped_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.WARPED, properties),
		BlockBehaviour.Properties.of()
			.mapColor(WARPED_PLANKS.defaultMapColor())
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollission()
			.strength(0.5F)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CRIMSON_FENCE = register(
		"crimson_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(CRIMSON_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.NETHER_WOOD)
	);
	public static final Block WARPED_FENCE = register(
		"warped_fence",
		FenceBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(WARPED_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.NETHER_WOOD)
	);
	public static final Block CRIMSON_TRAPDOOR = register(
		"crimson_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.CRIMSON, properties),
		BlockBehaviour.Properties.of()
			.mapColor(CRIMSON_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
	);
	public static final Block WARPED_TRAPDOOR = register(
		"warped_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.WARPED, properties),
		BlockBehaviour.Properties.of()
			.mapColor(WARPED_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
	);
	public static final Block CRIMSON_FENCE_GATE = register(
		"crimson_fence_gate",
		properties -> new FenceGateBlock(WoodType.CRIMSON, properties),
		BlockBehaviour.Properties.of().mapColor(CRIMSON_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F)
	);
	public static final Block WARPED_FENCE_GATE = register(
		"warped_fence_gate",
		properties -> new FenceGateBlock(WoodType.WARPED, properties),
		BlockBehaviour.Properties.of().mapColor(WARPED_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F)
	);
	public static final Block CRIMSON_STAIRS = registerLegacyStair("crimson_stairs", CRIMSON_PLANKS);
	public static final Block WARPED_STAIRS = registerLegacyStair("warped_stairs", WARPED_PLANKS);
	public static final Block CRIMSON_BUTTON = register("crimson_button", properties -> new ButtonBlock(BlockSetType.CRIMSON, 30, properties), buttonProperties());
	public static final Block WARPED_BUTTON = register("warped_button", properties -> new ButtonBlock(BlockSetType.WARPED, 30, properties), buttonProperties());
	public static final Block CRIMSON_DOOR = register(
		"crimson_door",
		properties -> new DoorBlock(BlockSetType.CRIMSON, properties),
		BlockBehaviour.Properties.of()
			.mapColor(CRIMSON_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block WARPED_DOOR = register(
		"warped_door",
		properties -> new DoorBlock(BlockSetType.WARPED, properties),
		BlockBehaviour.Properties.of()
			.mapColor(WARPED_PLANKS.defaultMapColor())
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CRIMSON_SIGN = register(
		"crimson_sign",
		properties -> new StandingSignBlock(WoodType.CRIMSON, properties),
		BlockBehaviour.Properties.of().mapColor(CRIMSON_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)
	);
	public static final Block WARPED_SIGN = register(
		"warped_sign",
		properties -> new StandingSignBlock(WoodType.WARPED, properties),
		BlockBehaviour.Properties.of().mapColor(WARPED_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)
	);
	public static final Block CRIMSON_WALL_SIGN = register(
		"crimson_wall_sign",
		properties -> new WallSignBlock(WoodType.CRIMSON, properties),
		wallVariant(CRIMSON_SIGN, true).mapColor(CRIMSON_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)
	);
	public static final Block WARPED_WALL_SIGN = register(
		"warped_wall_sign",
		properties -> new WallSignBlock(WoodType.WARPED, properties),
		wallVariant(WARPED_SIGN, true).mapColor(WARPED_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)
	);
	public static final Block STRUCTURE_BLOCK = register(
		"structure_block",
		StructureBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()
	);
	public static final Block JIGSAW = register(
		"jigsaw",
		JigsawBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()
	);
	public static final Block COMPOSTER = register(
		"composter",
		ComposterBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(0.6F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block TARGET = register(
		"target", TargetBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(0.5F).sound(SoundType.GRASS)
	);
	public static final Block BEE_NEST = register(
		"bee_nest",
		BeehiveBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).instrument(NoteBlockInstrument.BASS).strength(0.3F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block BEEHIVE = register(
		"beehive",
		BeehiveBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(0.6F).sound(SoundType.WOOD).ignitedByLava()
	);
	public static final Block HONEY_BLOCK = register(
		"honey_block",
		HoneyBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).speedFactor(0.4F).jumpFactor(0.5F).noOcclusion().sound(SoundType.HONEY_BLOCK)
	);
	public static final Block HONEYCOMB_BLOCK = register(
		"honeycomb_block", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.6F).sound(SoundType.CORAL_BLOCK)
	);
	public static final Block NETHERITE_BLOCK = register(
		"netherite_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK)
	);
	public static final Block ANCIENT_DEBRIS = register(
		"ancient_debris",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(30.0F, 1200.0F).sound(SoundType.ANCIENT_DEBRIS)
	);
	public static final Block CRYING_OBSIDIAN = register(
		"crying_obsidian",
		CryingObsidianBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(50.0F, 1200.0F)
			.lightLevel(blockStatex -> 10)
	);
	public static final Block RESPAWN_ANCHOR = register(
		"respawn_anchor",
		RespawnAnchorBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(50.0F, 1200.0F)
			.lightLevel(blockStatex -> RespawnAnchorBlock.getScaledChargeLevel(blockStatex, 15))
	);
	public static final Block POTTED_CRIMSON_FUNGUS = register(
		"potted_crimson_fungus", properties -> new FlowerPotBlock(CRIMSON_FUNGUS, properties), flowerPotProperties()
	);
	public static final Block POTTED_WARPED_FUNGUS = register(
		"potted_warped_fungus", properties -> new FlowerPotBlock(WARPED_FUNGUS, properties), flowerPotProperties()
	);
	public static final Block POTTED_CRIMSON_ROOTS = register(
		"potted_crimson_roots", properties -> new FlowerPotBlock(CRIMSON_ROOTS, properties), flowerPotProperties()
	);
	public static final Block POTTED_WARPED_ROOTS = register(
		"potted_warped_roots", properties -> new FlowerPotBlock(WARPED_ROOTS, properties), flowerPotProperties()
	);
	public static final Block LODESTONE = register(
		"lodestone",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.requiresCorrectToolForDrops()
			.strength(3.5F)
			.sound(SoundType.LODESTONE)
			.pushReaction(PushReaction.BLOCK)
	);
	public static final Block BLACKSTONE = register(
		"blackstone",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
	);
	public static final Block BLACKSTONE_STAIRS = registerLegacyStair("blackstone_stairs", BLACKSTONE);
	public static final Block BLACKSTONE_WALL = register("blackstone_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(BLACKSTONE).forceSolidOn());
	public static final Block BLACKSTONE_SLAB = register(
		"blackstone_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(BLACKSTONE).strength(2.0F, 6.0F)
	);
	public static final Block POLISHED_BLACKSTONE = register("polished_blackstone", BlockBehaviour.Properties.ofLegacyCopy(BLACKSTONE).strength(2.0F, 6.0F));
	public static final Block POLISHED_BLACKSTONE_BRICKS = register(
		"polished_blackstone_bricks", BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE).strength(1.5F, 6.0F)
	);
	public static final Block CRACKED_POLISHED_BLACKSTONE_BRICKS = register(
		"cracked_polished_blackstone_bricks", BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE_BRICKS)
	);
	public static final Block CHISELED_POLISHED_BLACKSTONE = register(
		"chiseled_polished_blackstone", BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE).strength(1.5F, 6.0F)
	);
	public static final Block POLISHED_BLACKSTONE_BRICK_SLAB = register(
		"polished_blackstone_brick_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE_BRICKS).strength(2.0F, 6.0F)
	);
	public static final Block POLISHED_BLACKSTONE_BRICK_STAIRS = registerLegacyStair("polished_blackstone_brick_stairs", POLISHED_BLACKSTONE_BRICKS);
	public static final Block POLISHED_BLACKSTONE_BRICK_WALL = register(
		"polished_blackstone_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE_BRICKS).forceSolidOn()
	);
	public static final Block GILDED_BLACKSTONE = register(
		"gilded_blackstone", BlockBehaviour.Properties.ofLegacyCopy(BLACKSTONE).sound(SoundType.GILDED_BLACKSTONE)
	);
	public static final Block POLISHED_BLACKSTONE_STAIRS = registerLegacyStair("polished_blackstone_stairs", POLISHED_BLACKSTONE);
	public static final Block POLISHED_BLACKSTONE_SLAB = register(
		"polished_blackstone_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE)
	);
	public static final Block POLISHED_BLACKSTONE_PRESSURE_PLATE = register(
		"polished_blackstone_pressure_plate",
		properties -> new PressurePlateBlock(BlockSetType.POLISHED_BLACKSTONE, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.noCollission()
			.strength(0.5F)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block POLISHED_BLACKSTONE_BUTTON = register(
		"polished_blackstone_button", properties -> new ButtonBlock(BlockSetType.STONE, 20, properties), buttonProperties()
	);
	public static final Block POLISHED_BLACKSTONE_WALL = register(
		"polished_blackstone_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE).forceSolidOn()
	);
	public static final Block CHISELED_NETHER_BRICKS = register(
		"chiseled_nether_bricks",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(2.0F, 6.0F)
			.sound(SoundType.NETHER_BRICKS)
	);
	public static final Block CRACKED_NETHER_BRICKS = register(
		"cracked_nether_bricks",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.NETHER)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(2.0F, 6.0F)
			.sound(SoundType.NETHER_BRICKS)
	);
	public static final Block QUARTZ_BRICKS = register("quartz_bricks", BlockBehaviour.Properties.ofLegacyCopy(QUARTZ_BLOCK));
	public static final Block CANDLE = register("candle", CandleBlock::new, candleProperties(MapColor.SAND));
	public static final Block WHITE_CANDLE = register("white_candle", CandleBlock::new, candleProperties(MapColor.WOOL));
	public static final Block ORANGE_CANDLE = register("orange_candle", CandleBlock::new, candleProperties(MapColor.COLOR_ORANGE));
	public static final Block MAGENTA_CANDLE = register("magenta_candle", CandleBlock::new, candleProperties(MapColor.COLOR_MAGENTA));
	public static final Block LIGHT_BLUE_CANDLE = register("light_blue_candle", CandleBlock::new, candleProperties(MapColor.COLOR_LIGHT_BLUE));
	public static final Block YELLOW_CANDLE = register("yellow_candle", CandleBlock::new, candleProperties(MapColor.COLOR_YELLOW));
	public static final Block LIME_CANDLE = register("lime_candle", CandleBlock::new, candleProperties(MapColor.COLOR_LIGHT_GREEN));
	public static final Block PINK_CANDLE = register("pink_candle", CandleBlock::new, candleProperties(MapColor.COLOR_PINK));
	public static final Block GRAY_CANDLE = register("gray_candle", CandleBlock::new, candleProperties(MapColor.COLOR_GRAY));
	public static final Block LIGHT_GRAY_CANDLE = register("light_gray_candle", CandleBlock::new, candleProperties(MapColor.COLOR_LIGHT_GRAY));
	public static final Block CYAN_CANDLE = register("cyan_candle", CandleBlock::new, candleProperties(MapColor.COLOR_CYAN));
	public static final Block PURPLE_CANDLE = register("purple_candle", CandleBlock::new, candleProperties(MapColor.COLOR_PURPLE));
	public static final Block BLUE_CANDLE = register("blue_candle", CandleBlock::new, candleProperties(MapColor.COLOR_BLUE));
	public static final Block BROWN_CANDLE = register("brown_candle", CandleBlock::new, candleProperties(MapColor.COLOR_BROWN));
	public static final Block GREEN_CANDLE = register("green_candle", CandleBlock::new, candleProperties(MapColor.COLOR_GREEN));
	public static final Block RED_CANDLE = register("red_candle", CandleBlock::new, candleProperties(MapColor.COLOR_RED));
	public static final Block BLACK_CANDLE = register("black_candle", CandleBlock::new, candleProperties(MapColor.COLOR_BLACK));
	public static final Block CANDLE_CAKE = register(
		"candle_cake", properties -> new CandleCakeBlock(CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CAKE).lightLevel(litBlockEmission(3))
	);
	public static final Block WHITE_CANDLE_CAKE = register(
		"white_candle_cake", properties -> new CandleCakeBlock(WHITE_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block ORANGE_CANDLE_CAKE = register(
		"orange_candle_cake", properties -> new CandleCakeBlock(ORANGE_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block MAGENTA_CANDLE_CAKE = register(
		"magenta_candle_cake", properties -> new CandleCakeBlock(MAGENTA_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block LIGHT_BLUE_CANDLE_CAKE = register(
		"light_blue_candle_cake", properties -> new CandleCakeBlock(LIGHT_BLUE_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block YELLOW_CANDLE_CAKE = register(
		"yellow_candle_cake", properties -> new CandleCakeBlock(YELLOW_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block LIME_CANDLE_CAKE = register(
		"lime_candle_cake", properties -> new CandleCakeBlock(LIME_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block PINK_CANDLE_CAKE = register(
		"pink_candle_cake", properties -> new CandleCakeBlock(PINK_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block GRAY_CANDLE_CAKE = register(
		"gray_candle_cake", properties -> new CandleCakeBlock(GRAY_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block LIGHT_GRAY_CANDLE_CAKE = register(
		"light_gray_candle_cake", properties -> new CandleCakeBlock(LIGHT_GRAY_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block CYAN_CANDLE_CAKE = register(
		"cyan_candle_cake", properties -> new CandleCakeBlock(CYAN_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block PURPLE_CANDLE_CAKE = register(
		"purple_candle_cake", properties -> new CandleCakeBlock(PURPLE_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block BLUE_CANDLE_CAKE = register(
		"blue_candle_cake", properties -> new CandleCakeBlock(BLUE_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block BROWN_CANDLE_CAKE = register(
		"brown_candle_cake", properties -> new CandleCakeBlock(BROWN_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block GREEN_CANDLE_CAKE = register(
		"green_candle_cake", properties -> new CandleCakeBlock(GREEN_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block RED_CANDLE_CAKE = register(
		"red_candle_cake", properties -> new CandleCakeBlock(RED_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block BLACK_CANDLE_CAKE = register(
		"black_candle_cake", properties -> new CandleCakeBlock(BLACK_CANDLE, properties), BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)
	);
	public static final Block AMETHYST_BLOCK = register(
		"amethyst_block",
		AmethystBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.5F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()
	);
	public static final Block BUDDING_AMETHYST = register(
		"budding_amethyst",
		BuddingAmethystBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.randomTicks()
			.strength(1.5F)
			.sound(SoundType.AMETHYST)
			.requiresCorrectToolForDrops()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block AMETHYST_CLUSTER = register(
		"amethyst_cluster",
		properties -> new AmethystClusterBlock(7.0F, 3.0F, properties),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.forceSolidOn()
			.noOcclusion()
			.sound(SoundType.AMETHYST_CLUSTER)
			.strength(1.5F)
			.lightLevel(blockStatex -> 5)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block LARGE_AMETHYST_BUD = register(
		"large_amethyst_bud",
		properties -> new AmethystClusterBlock(5.0F, 3.0F, properties),
		BlockBehaviour.Properties.ofLegacyCopy(AMETHYST_CLUSTER).sound(SoundType.MEDIUM_AMETHYST_BUD).lightLevel(blockStatex -> 4)
	);
	public static final Block MEDIUM_AMETHYST_BUD = register(
		"medium_amethyst_bud",
		properties -> new AmethystClusterBlock(4.0F, 3.0F, properties),
		BlockBehaviour.Properties.ofLegacyCopy(AMETHYST_CLUSTER).sound(SoundType.LARGE_AMETHYST_BUD).lightLevel(blockStatex -> 2)
	);
	public static final Block SMALL_AMETHYST_BUD = register(
		"small_amethyst_bud",
		properties -> new AmethystClusterBlock(3.0F, 4.0F, properties),
		BlockBehaviour.Properties.ofLegacyCopy(AMETHYST_CLUSTER).sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(blockStatex -> 1)
	);
	public static final Block TUFF = register(
		"tuff",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_GRAY)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.sound(SoundType.TUFF)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 6.0F)
	);
	public static final Block TUFF_SLAB = register("tuff_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(TUFF));
	public static final Block TUFF_STAIRS = register(
		"tuff_stairs", properties -> new StairBlock(TUFF.defaultBlockState(), properties), BlockBehaviour.Properties.ofLegacyCopy(TUFF)
	);
	public static final Block TUFF_WALL = register("tuff_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(TUFF).forceSolidOn());
	public static final Block POLISHED_TUFF = register("polished_tuff", BlockBehaviour.Properties.ofLegacyCopy(TUFF).sound(SoundType.POLISHED_TUFF));
	public static final Block POLISHED_TUFF_SLAB = register("polished_tuff_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_TUFF));
	public static final Block POLISHED_TUFF_STAIRS = register(
		"polished_tuff_stairs", properties -> new StairBlock(POLISHED_TUFF.defaultBlockState(), properties), BlockBehaviour.Properties.ofLegacyCopy(POLISHED_TUFF)
	);
	public static final Block POLISHED_TUFF_WALL = register(
		"polished_tuff_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_TUFF).forceSolidOn()
	);
	public static final Block CHISELED_TUFF = register("chiseled_tuff", BlockBehaviour.Properties.ofLegacyCopy(TUFF));
	public static final Block TUFF_BRICKS = register("tuff_bricks", BlockBehaviour.Properties.ofLegacyCopy(TUFF).sound(SoundType.TUFF_BRICKS));
	public static final Block TUFF_BRICK_SLAB = register("tuff_brick_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(TUFF_BRICKS));
	public static final Block TUFF_BRICK_STAIRS = register(
		"tuff_brick_stairs", properties -> new StairBlock(TUFF_BRICKS.defaultBlockState(), properties), BlockBehaviour.Properties.ofLegacyCopy(TUFF_BRICKS)
	);
	public static final Block TUFF_BRICK_WALL = register("tuff_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(TUFF_BRICKS).forceSolidOn());
	public static final Block CHISELED_TUFF_BRICKS = register("chiseled_tuff_bricks", BlockBehaviour.Properties.ofLegacyCopy(TUFF_BRICKS));
	public static final Block CALCITE = register(
		"calcite",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_WHITE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.sound(SoundType.CALCITE)
			.requiresCorrectToolForDrops()
			.strength(0.75F)
	);
	public static final Block TINTED_GLASS = register(
		"tinted_glass",
		TintedGlassBlock::new,
		BlockBehaviour.Properties.ofLegacyCopy(GLASS)
			.mapColor(MapColor.COLOR_GRAY)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.isRedstoneConductor(Blocks::never)
			.isSuffocating(Blocks::never)
			.isViewBlocking(Blocks::never)
	);
	public static final Block POWDER_SNOW = register(
		"powder_snow",
		PowderSnowBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.SNOW)
			.strength(0.25F)
			.sound(SoundType.POWDER_SNOW)
			.dynamicShape()
			.noOcclusion()
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block SCULK_SENSOR = register(
		"sculk_sensor",
		SculkSensorBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_CYAN)
			.strength(1.5F)
			.sound(SoundType.SCULK_SENSOR)
			.lightLevel(blockStatex -> 1)
			.emissiveRendering((blockStatex, blockGetter, blockPos) -> SculkSensorBlock.getPhase(blockStatex) == SculkSensorPhase.ACTIVE)
	);
	public static final Block CALIBRATED_SCULK_SENSOR = register(
		"calibrated_sculk_sensor", CalibratedSculkSensorBlock::new, BlockBehaviour.Properties.ofLegacyCopy(SCULK_SENSOR)
	);
	public static final Block SCULK = register(
		"sculk", SculkBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.2F).sound(SoundType.SCULK)
	);
	public static final Block SCULK_VEIN = register(
		"sculk_vein",
		SculkVeinBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.forceSolidOn()
			.noCollission()
			.strength(0.2F)
			.sound(SoundType.SCULK_VEIN)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SCULK_CATALYST = register(
		"sculk_catalyst",
		SculkCatalystBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0F, 3.0F).sound(SoundType.SCULK_CATALYST).lightLevel(blockStatex -> 6)
	);
	public static final Block SCULK_SHRIEKER = register(
		"sculk_shrieker", SculkShriekerBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0F, 3.0F).sound(SoundType.SCULK_SHRIEKER)
	);
	public static final Block COPPER_BLOCK = register(
		"copper_block",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.UNAFFECTED, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.COPPER)
	);
	public static final Block EXPOSED_COPPER = register(
		"exposed_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
	);
	public static final Block WEATHERED_COPPER = register(
		"weathered_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.WEATHERED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK).mapColor(MapColor.WARPED_STEM)
	);
	public static final Block OXIDIZED_COPPER = register(
		"oxidized_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.OXIDIZED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK).mapColor(MapColor.WARPED_NYLIUM)
	);
	public static final Block COPPER_ORE = register(
		"copper_ore", properties -> new DropExperienceBlock(ConstantInt.of(0), properties), BlockBehaviour.Properties.ofLegacyCopy(IRON_ORE)
	);
	public static final Block DEEPSLATE_COPPER_ORE = register(
		"deepslate_copper_ore",
		properties -> new DropExperienceBlock(ConstantInt.of(0), properties),
		BlockBehaviour.Properties.ofLegacyCopy(COPPER_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
	);
	public static final Block OXIDIZED_CUT_COPPER = register(
		"oxidized_cut_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.OXIDIZED, properties),
		BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER)
	);
	public static final Block WEATHERED_CUT_COPPER = register(
		"weathered_cut_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.WEATHERED, properties),
		BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER)
	);
	public static final Block EXPOSED_CUT_COPPER = register(
		"exposed_cut_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED, properties),
		BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER)
	);
	public static final Block CUT_COPPER = register(
		"cut_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.UNAFFECTED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK)
	);
	public static final Block OXIDIZED_CHISELED_COPPER = register(
		"oxidized_chiseled_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.OXIDIZED, properties),
		BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER)
	);
	public static final Block WEATHERED_CHISELED_COPPER = register(
		"weathered_chiseled_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.WEATHERED, properties),
		BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER)
	);
	public static final Block EXPOSED_CHISELED_COPPER = register(
		"exposed_chiseled_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED, properties),
		BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER)
	);
	public static final Block CHISELED_COPPER = register(
		"chiseled_copper",
		properties -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.UNAFFECTED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK)
	);
	public static final Block WAXED_OXIDIZED_CHISELED_COPPER = register(
		"waxed_oxidized_chiseled_copper", BlockBehaviour.Properties.ofFullCopy(OXIDIZED_CHISELED_COPPER)
	);
	public static final Block WAXED_WEATHERED_CHISELED_COPPER = register(
		"waxed_weathered_chiseled_copper", BlockBehaviour.Properties.ofFullCopy(WEATHERED_CHISELED_COPPER)
	);
	public static final Block WAXED_EXPOSED_CHISELED_COPPER = register(
		"waxed_exposed_chiseled_copper", BlockBehaviour.Properties.ofFullCopy(EXPOSED_CHISELED_COPPER)
	);
	public static final Block WAXED_CHISELED_COPPER = register("waxed_chiseled_copper", BlockBehaviour.Properties.ofFullCopy(CHISELED_COPPER));
	public static final Block OXIDIZED_CUT_COPPER_STAIRS = register(
		"oxidized_cut_copper_stairs",
		properties -> new WeatheringCopperStairBlock(WeatheringCopper.WeatherState.OXIDIZED, OXIDIZED_CUT_COPPER.defaultBlockState(), properties),
		BlockBehaviour.Properties.ofFullCopy(OXIDIZED_CUT_COPPER)
	);
	public static final Block WEATHERED_CUT_COPPER_STAIRS = register(
		"weathered_cut_copper_stairs",
		properties -> new WeatheringCopperStairBlock(WeatheringCopper.WeatherState.WEATHERED, WEATHERED_CUT_COPPER.defaultBlockState(), properties),
		BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER)
	);
	public static final Block EXPOSED_CUT_COPPER_STAIRS = register(
		"exposed_cut_copper_stairs",
		properties -> new WeatheringCopperStairBlock(WeatheringCopper.WeatherState.EXPOSED, EXPOSED_CUT_COPPER.defaultBlockState(), properties),
		BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER)
	);
	public static final Block CUT_COPPER_STAIRS = register(
		"cut_copper_stairs",
		properties -> new WeatheringCopperStairBlock(WeatheringCopper.WeatherState.UNAFFECTED, CUT_COPPER.defaultBlockState(), properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK)
	);
	public static final Block OXIDIZED_CUT_COPPER_SLAB = register(
		"oxidized_cut_copper_slab",
		properties -> new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.OXIDIZED, properties),
		BlockBehaviour.Properties.ofFullCopy(OXIDIZED_CUT_COPPER)
	);
	public static final Block WEATHERED_CUT_COPPER_SLAB = register(
		"weathered_cut_copper_slab",
		properties -> new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.WEATHERED, properties),
		BlockBehaviour.Properties.ofFullCopy(WEATHERED_CUT_COPPER)
	);
	public static final Block EXPOSED_CUT_COPPER_SLAB = register(
		"exposed_cut_copper_slab",
		properties -> new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.EXPOSED, properties),
		BlockBehaviour.Properties.ofFullCopy(EXPOSED_CUT_COPPER)
	);
	public static final Block CUT_COPPER_SLAB = register(
		"cut_copper_slab",
		properties -> new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.UNAFFECTED, properties),
		BlockBehaviour.Properties.ofFullCopy(CUT_COPPER)
	);
	public static final Block WAXED_COPPER_BLOCK = register("waxed_copper_block", BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK));
	public static final Block WAXED_WEATHERED_COPPER = register("waxed_weathered_copper", BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER));
	public static final Block WAXED_EXPOSED_COPPER = register("waxed_exposed_copper", BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER));
	public static final Block WAXED_OXIDIZED_COPPER = register("waxed_oxidized_copper", BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER));
	public static final Block WAXED_OXIDIZED_CUT_COPPER = register("waxed_oxidized_cut_copper", BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER));
	public static final Block WAXED_WEATHERED_CUT_COPPER = register("waxed_weathered_cut_copper", BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER));
	public static final Block WAXED_EXPOSED_CUT_COPPER = register("waxed_exposed_cut_copper", BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER));
	public static final Block WAXED_CUT_COPPER = register("waxed_cut_copper", BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK));
	public static final Block WAXED_OXIDIZED_CUT_COPPER_STAIRS = registerStair("waxed_oxidized_cut_copper_stairs", WAXED_OXIDIZED_CUT_COPPER);
	public static final Block WAXED_WEATHERED_CUT_COPPER_STAIRS = registerStair("waxed_weathered_cut_copper_stairs", WAXED_WEATHERED_CUT_COPPER);
	public static final Block WAXED_EXPOSED_CUT_COPPER_STAIRS = registerStair("waxed_exposed_cut_copper_stairs", WAXED_EXPOSED_CUT_COPPER);
	public static final Block WAXED_CUT_COPPER_STAIRS = registerStair("waxed_cut_copper_stairs", WAXED_CUT_COPPER);
	public static final Block WAXED_OXIDIZED_CUT_COPPER_SLAB = register(
		"waxed_oxidized_cut_copper_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(WAXED_OXIDIZED_CUT_COPPER).requiresCorrectToolForDrops()
	);
	public static final Block WAXED_WEATHERED_CUT_COPPER_SLAB = register(
		"waxed_weathered_cut_copper_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(WAXED_WEATHERED_CUT_COPPER).requiresCorrectToolForDrops()
	);
	public static final Block WAXED_EXPOSED_CUT_COPPER_SLAB = register(
		"waxed_exposed_cut_copper_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(WAXED_EXPOSED_CUT_COPPER).requiresCorrectToolForDrops()
	);
	public static final Block WAXED_CUT_COPPER_SLAB = register(
		"waxed_cut_copper_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(WAXED_CUT_COPPER).requiresCorrectToolForDrops()
	);
	public static final Block COPPER_DOOR = register(
		"copper_door",
		properties -> new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.WeatherState.UNAFFECTED, properties),
		BlockBehaviour.Properties.of()
			.mapColor(COPPER_BLOCK.defaultMapColor())
			.strength(3.0F, 6.0F)
			.noOcclusion()
			.requiresCorrectToolForDrops()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block EXPOSED_COPPER_DOOR = register(
		"exposed_copper_door",
		properties -> new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.WeatherState.EXPOSED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_DOOR).mapColor(EXPOSED_COPPER.defaultMapColor())
	);
	public static final Block OXIDIZED_COPPER_DOOR = register(
		"oxidized_copper_door",
		properties -> new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.WeatherState.OXIDIZED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_DOOR).mapColor(OXIDIZED_COPPER.defaultMapColor())
	);
	public static final Block WEATHERED_COPPER_DOOR = register(
		"weathered_copper_door",
		properties -> new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.WeatherState.WEATHERED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_DOOR).mapColor(WEATHERED_COPPER.defaultMapColor())
	);
	public static final Block WAXED_COPPER_DOOR = register(
		"waxed_copper_door", properties -> new DoorBlock(BlockSetType.COPPER, properties), BlockBehaviour.Properties.ofFullCopy(COPPER_DOOR)
	);
	public static final Block WAXED_EXPOSED_COPPER_DOOR = register(
		"waxed_exposed_copper_door", properties -> new DoorBlock(BlockSetType.COPPER, properties), BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_DOOR)
	);
	public static final Block WAXED_OXIDIZED_COPPER_DOOR = register(
		"waxed_oxidized_copper_door", properties -> new DoorBlock(BlockSetType.COPPER, properties), BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_DOOR)
	);
	public static final Block WAXED_WEATHERED_COPPER_DOOR = register(
		"waxed_weathered_copper_door", properties -> new DoorBlock(BlockSetType.COPPER, properties), BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_DOOR)
	);
	public static final Block COPPER_TRAPDOOR = register(
		"copper_trapdoor",
		properties -> new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.WeatherState.UNAFFECTED, properties),
		BlockBehaviour.Properties.of()
			.mapColor(COPPER_BLOCK.defaultMapColor())
			.strength(3.0F, 6.0F)
			.requiresCorrectToolForDrops()
			.noOcclusion()
			.isValidSpawn(Blocks::never)
	);
	public static final Block EXPOSED_COPPER_TRAPDOOR = register(
		"exposed_copper_trapdoor",
		properties -> new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.WeatherState.EXPOSED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_TRAPDOOR).mapColor(EXPOSED_COPPER.defaultMapColor())
	);
	public static final Block OXIDIZED_COPPER_TRAPDOOR = register(
		"oxidized_copper_trapdoor",
		properties -> new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.WeatherState.OXIDIZED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_TRAPDOOR).mapColor(OXIDIZED_COPPER.defaultMapColor())
	);
	public static final Block WEATHERED_COPPER_TRAPDOOR = register(
		"weathered_copper_trapdoor",
		properties -> new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.WeatherState.WEATHERED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_TRAPDOOR).mapColor(WEATHERED_COPPER.defaultMapColor())
	);
	public static final Block WAXED_COPPER_TRAPDOOR = register(
		"waxed_copper_trapdoor", properties -> new TrapDoorBlock(BlockSetType.COPPER, properties), BlockBehaviour.Properties.ofFullCopy(COPPER_TRAPDOOR)
	);
	public static final Block WAXED_EXPOSED_COPPER_TRAPDOOR = register(
		"waxed_exposed_copper_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.COPPER, properties),
		BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_TRAPDOOR)
	);
	public static final Block WAXED_OXIDIZED_COPPER_TRAPDOOR = register(
		"waxed_oxidized_copper_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.COPPER, properties),
		BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_TRAPDOOR)
	);
	public static final Block WAXED_WEATHERED_COPPER_TRAPDOOR = register(
		"waxed_weathered_copper_trapdoor",
		properties -> new TrapDoorBlock(BlockSetType.COPPER, properties),
		BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_TRAPDOOR)
	);
	public static final Block COPPER_GRATE = register(
		"copper_grate",
		properties -> new WeatheringCopperGrateBlock(WeatheringCopper.WeatherState.UNAFFECTED, properties),
		BlockBehaviour.Properties.of()
			.strength(3.0F, 6.0F)
			.sound(SoundType.COPPER_GRATE)
			.mapColor(MapColor.COLOR_ORANGE)
			.noOcclusion()
			.requiresCorrectToolForDrops()
			.isValidSpawn(Blocks::never)
			.isRedstoneConductor(Blocks::never)
			.isSuffocating(Blocks::never)
			.isViewBlocking(Blocks::never)
	);
	public static final Block EXPOSED_COPPER_GRATE = register(
		"exposed_copper_grate",
		properties -> new WeatheringCopperGrateBlock(WeatheringCopper.WeatherState.EXPOSED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_GRATE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
	);
	public static final Block WEATHERED_COPPER_GRATE = register(
		"weathered_copper_grate",
		properties -> new WeatheringCopperGrateBlock(WeatheringCopper.WeatherState.WEATHERED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_GRATE).mapColor(MapColor.WARPED_STEM)
	);
	public static final Block OXIDIZED_COPPER_GRATE = register(
		"oxidized_copper_grate",
		properties -> new WeatheringCopperGrateBlock(WeatheringCopper.WeatherState.OXIDIZED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_GRATE).mapColor(MapColor.WARPED_NYLIUM)
	);
	public static final Block WAXED_COPPER_GRATE = register(
		"waxed_copper_grate", WaterloggedTransparentBlock::new, BlockBehaviour.Properties.ofFullCopy(COPPER_GRATE)
	);
	public static final Block WAXED_EXPOSED_COPPER_GRATE = register(
		"waxed_exposed_copper_grate", WaterloggedTransparentBlock::new, BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_GRATE)
	);
	public static final Block WAXED_WEATHERED_COPPER_GRATE = register(
		"waxed_weathered_copper_grate", WaterloggedTransparentBlock::new, BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_GRATE)
	);
	public static final Block WAXED_OXIDIZED_COPPER_GRATE = register(
		"waxed_oxidized_copper_grate", WaterloggedTransparentBlock::new, BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_GRATE)
	);
	public static final Block COPPER_BULB = register(
		"copper_bulb",
		properties -> new WeatheringCopperBulbBlock(WeatheringCopper.WeatherState.UNAFFECTED, properties),
		BlockBehaviour.Properties.of()
			.mapColor(COPPER_BLOCK.defaultMapColor())
			.strength(3.0F, 6.0F)
			.sound(SoundType.COPPER_BULB)
			.requiresCorrectToolForDrops()
			.isRedstoneConductor(Blocks::never)
			.lightLevel(litBlockEmission(15))
	);
	public static final Block EXPOSED_COPPER_BULB = register(
		"exposed_copper_bulb",
		properties -> new WeatheringCopperBulbBlock(WeatheringCopper.WeatherState.EXPOSED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_BULB).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).lightLevel(litBlockEmission(12))
	);
	public static final Block WEATHERED_COPPER_BULB = register(
		"weathered_copper_bulb",
		properties -> new WeatheringCopperBulbBlock(WeatheringCopper.WeatherState.WEATHERED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_BULB).mapColor(MapColor.WARPED_STEM).lightLevel(litBlockEmission(8))
	);
	public static final Block OXIDIZED_COPPER_BULB = register(
		"oxidized_copper_bulb",
		properties -> new WeatheringCopperBulbBlock(WeatheringCopper.WeatherState.OXIDIZED, properties),
		BlockBehaviour.Properties.ofFullCopy(COPPER_BULB).mapColor(MapColor.WARPED_NYLIUM).lightLevel(litBlockEmission(4))
	);
	public static final Block WAXED_COPPER_BULB = register("waxed_copper_bulb", CopperBulbBlock::new, BlockBehaviour.Properties.ofFullCopy(COPPER_BULB));
	public static final Block WAXED_EXPOSED_COPPER_BULB = register(
		"waxed_exposed_copper_bulb", CopperBulbBlock::new, BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_BULB)
	);
	public static final Block WAXED_WEATHERED_COPPER_BULB = register(
		"waxed_weathered_copper_bulb", CopperBulbBlock::new, BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_BULB)
	);
	public static final Block WAXED_OXIDIZED_COPPER_BULB = register(
		"waxed_oxidized_copper_bulb", CopperBulbBlock::new, BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_BULB)
	);
	public static final Block LIGHTNING_ROD = register(
		"lightning_rod",
		LightningRodBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_ORANGE)
			.forceSolidOn()
			.requiresCorrectToolForDrops()
			.strength(3.0F, 6.0F)
			.sound(SoundType.COPPER)
			.noOcclusion()
	);
	public static final Block POINTED_DRIPSTONE = register(
		"pointed_dripstone",
		PointedDripstoneBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_BROWN)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASEDRUM)
			.noOcclusion()
			.sound(SoundType.POINTED_DRIPSTONE)
			.randomTicks()
			.strength(1.5F, 3.0F)
			.dynamicShape()
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
			.isRedstoneConductor(Blocks::never)
	);
	public static final Block DRIPSTONE_BLOCK = register(
		"dripstone_block",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.TERRACOTTA_BROWN)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.sound(SoundType.DRIPSTONE_BLOCK)
			.requiresCorrectToolForDrops()
			.strength(1.5F, 1.0F)
	);
	public static final Block CAVE_VINES = register(
		"cave_vines",
		CaveVinesBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.randomTicks()
			.noCollission()
			.lightLevel(CaveVines.emission(14))
			.instabreak()
			.sound(SoundType.CAVE_VINES)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block CAVE_VINES_PLANT = register(
		"cave_vines_plant",
		CaveVinesPlantBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.lightLevel(CaveVines.emission(14))
			.instabreak()
			.sound(SoundType.CAVE_VINES)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block SPORE_BLOSSOM = register(
		"spore_blossom",
		SporeBlossomBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().noCollission().sound(SoundType.SPORE_BLOSSOM).pushReaction(PushReaction.DESTROY)
	);
	public static final Block AZALEA = register(
		"azalea",
		AzaleaBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).forceSolidOff().instabreak().sound(SoundType.AZALEA).noOcclusion().pushReaction(PushReaction.DESTROY)
	);
	public static final Block FLOWERING_AZALEA = register(
		"flowering_azalea",
		AzaleaBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.forceSolidOff()
			.instabreak()
			.sound(SoundType.FLOWERING_AZALEA)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block MOSS_CARPET = register(
		"moss_carpet",
		CarpetBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.1F).sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY)
	);
	public static final Block PINK_PETALS = register(
		"pink_petals",
		PinkPetalsBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block MOSS_BLOCK = register(
		"moss_block",
		MossBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.1F).sound(SoundType.MOSS).pushReaction(PushReaction.DESTROY)
	);
	public static final Block BIG_DRIPLEAF = register(
		"big_dripleaf",
		BigDripleafBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).forceSolidOff().strength(0.1F).sound(SoundType.BIG_DRIPLEAF).pushReaction(PushReaction.DESTROY)
	);
	public static final Block BIG_DRIPLEAF_STEM = register(
		"big_dripleaf_stem",
		BigDripleafStemBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().strength(0.1F).sound(SoundType.BIG_DRIPLEAF).pushReaction(PushReaction.DESTROY)
	);
	public static final Block SMALL_DRIPLEAF = register(
		"small_dripleaf",
		SmallDripleafBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.noCollission()
			.instabreak()
			.sound(SoundType.SMALL_DRIPLEAF)
			.offsetType(BlockBehaviour.OffsetType.XYZ)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block HANGING_ROOTS = register(
		"hanging_roots",
		HangingRootsBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.DIRT)
			.replaceable()
			.noCollission()
			.instabreak()
			.sound(SoundType.HANGING_ROOTS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block ROOTED_DIRT = register(
		"rooted_dirt", RootedDirtBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.ROOTED_DIRT)
	);
	public static final Block MUD = register(
		"mud",
		MudBlock::new,
		BlockBehaviour.Properties.ofLegacyCopy(DIRT)
			.mapColor(MapColor.TERRACOTTA_CYAN)
			.isValidSpawn(Blocks::always)
			.isRedstoneConductor(Blocks::always)
			.isViewBlocking(Blocks::always)
			.isSuffocating(Blocks::always)
			.sound(SoundType.MUD)
	);
	public static final Block DEEPSLATE = register(
		"deepslate",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.DEEPSLATE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(3.0F, 6.0F)
			.sound(SoundType.DEEPSLATE)
	);
	public static final Block COBBLED_DEEPSLATE = register("cobbled_deepslate", BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE).strength(3.5F, 6.0F));
	public static final Block COBBLED_DEEPSLATE_STAIRS = registerLegacyStair("cobbled_deepslate_stairs", COBBLED_DEEPSLATE);
	public static final Block COBBLED_DEEPSLATE_SLAB = register(
		"cobbled_deepslate_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE)
	);
	public static final Block COBBLED_DEEPSLATE_WALL = register(
		"cobbled_deepslate_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).forceSolidOn()
	);
	public static final Block POLISHED_DEEPSLATE = register(
		"polished_deepslate", BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).sound(SoundType.POLISHED_DEEPSLATE)
	);
	public static final Block POLISHED_DEEPSLATE_STAIRS = registerLegacyStair("polished_deepslate_stairs", POLISHED_DEEPSLATE);
	public static final Block POLISHED_DEEPSLATE_SLAB = register(
		"polished_deepslate_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_DEEPSLATE)
	);
	public static final Block POLISHED_DEEPSLATE_WALL = register(
		"polished_deepslate_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(POLISHED_DEEPSLATE).forceSolidOn()
	);
	public static final Block DEEPSLATE_TILES = register(
		"deepslate_tiles", BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).sound(SoundType.DEEPSLATE_TILES)
	);
	public static final Block DEEPSLATE_TILE_STAIRS = registerLegacyStair("deepslate_tile_stairs", DEEPSLATE_TILES);
	public static final Block DEEPSLATE_TILE_SLAB = register("deepslate_tile_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_TILES));
	public static final Block DEEPSLATE_TILE_WALL = register(
		"deepslate_tile_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_TILES).forceSolidOn()
	);
	public static final Block DEEPSLATE_BRICKS = register(
		"deepslate_bricks", BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).sound(SoundType.DEEPSLATE_BRICKS)
	);
	public static final Block DEEPSLATE_BRICK_STAIRS = registerLegacyStair("deepslate_brick_stairs", DEEPSLATE_BRICKS);
	public static final Block DEEPSLATE_BRICK_SLAB = register("deepslate_brick_slab", SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_BRICKS));
	public static final Block DEEPSLATE_BRICK_WALL = register(
		"deepslate_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_BRICKS).forceSolidOn()
	);
	public static final Block CHISELED_DEEPSLATE = register(
		"chiseled_deepslate", BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).sound(SoundType.DEEPSLATE_BRICKS)
	);
	public static final Block CRACKED_DEEPSLATE_BRICKS = register("cracked_deepslate_bricks", BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_BRICKS));
	public static final Block CRACKED_DEEPSLATE_TILES = register("cracked_deepslate_tiles", BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_TILES));
	public static final Block INFESTED_DEEPSLATE = register(
		"infested_deepslate",
		properties -> new InfestedRotatedPillarBlock(DEEPSLATE, properties),
		BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.DEEPSLATE)
	);
	public static final Block SMOOTH_BASALT = register("smooth_basalt", BlockBehaviour.Properties.ofLegacyCopy(BASALT));
	public static final Block RAW_IRON_BLOCK = register(
		"raw_iron_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.RAW_IRON).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)
	);
	public static final Block RAW_COPPER_BLOCK = register(
		"raw_copper_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)
	);
	public static final Block RAW_GOLD_BLOCK = register(
		"raw_gold_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)
	);
	public static final Block POTTED_AZALEA = register("potted_azalea_bush", properties -> new FlowerPotBlock(AZALEA, properties), flowerPotProperties());
	public static final Block POTTED_FLOWERING_AZALEA = register(
		"potted_flowering_azalea_bush", properties -> new FlowerPotBlock(FLOWERING_AZALEA, properties), flowerPotProperties()
	);
	public static final Block OCHRE_FROGLIGHT = register(
		"ochre_froglight",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.3F).lightLevel(blockStatex -> 15).sound(SoundType.FROGLIGHT)
	);
	public static final Block VERDANT_FROGLIGHT = register(
		"verdant_froglight",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.GLOW_LICHEN).strength(0.3F).lightLevel(blockStatex -> 15).sound(SoundType.FROGLIGHT)
	);
	public static final Block PEARLESCENT_FROGLIGHT = register(
		"pearlescent_froglight",
		RotatedPillarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.3F).lightLevel(blockStatex -> 15).sound(SoundType.FROGLIGHT)
	);
	public static final Block FROGSPAWN = register(
		"frogspawn",
		FrogspawnBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.WATER)
			.instabreak()
			.noOcclusion()
			.noCollission()
			.sound(SoundType.FROGSPAWN)
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block REINFORCED_DEEPSLATE = register(
		"reinforced_deepslate",
		BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.DEEPSLATE).strength(55.0F, 1200.0F)
	);
	public static final Block DECORATED_POT = register(
		"decorated_pot",
		DecoratedPotBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).strength(0.0F, 0.0F).pushReaction(PushReaction.DESTROY).noOcclusion()
	);
	public static final Block CRAFTER = register("crafter", CrafterBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3.5F));
	public static final Block TRIAL_SPAWNER = register(
		"trial_spawner",
		TrialSpawnerBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.lightLevel(blockStatex -> ((TrialSpawnerState)blockStatex.getValue(TrialSpawnerBlock.STATE)).lightLevel())
			.strength(50.0F)
			.sound(SoundType.TRIAL_SPAWNER)
			.isViewBlocking(Blocks::never)
			.noOcclusion()
	);
	public static final Block VAULT = register(
		"vault",
		VaultBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.noOcclusion()
			.sound(SoundType.VAULT)
			.lightLevel(blockStatex -> ((VaultState)blockStatex.getValue(VaultBlock.STATE)).lightLevel())
			.strength(50.0F)
			.isViewBlocking(Blocks::never)
	);
	public static final Block HEAVY_CORE = register(
		"heavy_core",
		HeavyCoreBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.instrument(NoteBlockInstrument.SNARE)
			.sound(SoundType.HEAVY_CORE)
			.strength(10.0F)
			.pushReaction(PushReaction.NORMAL)
			.explosionResistance(1200.0F)
	);

	private static ToIntFunction<BlockState> litBlockEmission(int i) {
		return blockState -> blockState.getValue(BlockStateProperties.LIT) ? i : 0;
	}

	private static Function<BlockState, MapColor> waterloggedMapColor(MapColor mapColor) {
		return blockState -> blockState.getValue(BlockStateProperties.WATERLOGGED) ? MapColor.WATER : mapColor;
	}

	private static Boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
		return false;
	}

	private static Boolean always(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
		return true;
	}

	private static Boolean ocelotOrParrot(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
		return entityType == EntityType.OCELOT || entityType == EntityType.PARROT;
	}

	private static Block registerBed(String string, DyeColor dyeColor) {
		return register(
			string,
			properties -> new BedBlock(dyeColor, properties),
			BlockBehaviour.Properties.of()
				.mapColor(blockState -> blockState.getValue(BedBlock.PART) == BedPart.FOOT ? dyeColor.getMapColor() : MapColor.WOOL)
				.sound(SoundType.WOOD)
				.strength(0.2F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		);
	}

	private static BlockBehaviour.Properties logProperties(MapColor mapColor, MapColor mapColor2, SoundType soundType) {
		return BlockBehaviour.Properties.of()
			.mapColor(blockState -> blockState.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? mapColor : mapColor2)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F)
			.sound(soundType)
			.ignitedByLava();
	}

	private static BlockBehaviour.Properties netherStemProperties(MapColor mapColor) {
		return BlockBehaviour.Properties.of().mapColor(blockState -> mapColor).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM);
	}

	private static boolean always(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}

	private static boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	private static Block registerStainedGlass(String string, DyeColor dyeColor) {
		return register(
			string,
			properties -> new StainedGlassBlock(dyeColor, properties),
			BlockBehaviour.Properties.of()
				.mapColor(dyeColor)
				.instrument(NoteBlockInstrument.HAT)
				.strength(0.3F)
				.sound(SoundType.GLASS)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.isRedstoneConductor(Blocks::never)
				.isSuffocating(Blocks::never)
				.isViewBlocking(Blocks::never)
		);
	}

	private static BlockBehaviour.Properties leavesProperties(SoundType soundType) {
		return BlockBehaviour.Properties.of()
			.mapColor(MapColor.PLANT)
			.strength(0.2F)
			.randomTicks()
			.sound(soundType)
			.noOcclusion()
			.isValidSpawn(Blocks::ocelotOrParrot)
			.isSuffocating(Blocks::never)
			.isViewBlocking(Blocks::never)
			.ignitedByLava()
			.pushReaction(PushReaction.DESTROY)
			.isRedstoneConductor(Blocks::never);
	}

	private static BlockBehaviour.Properties shulkerBoxProperties(MapColor mapColor) {
		return BlockBehaviour.Properties.of()
			.mapColor(mapColor)
			.forceSolidOn()
			.strength(2.0F)
			.dynamicShape()
			.noOcclusion()
			.isSuffocating(NOT_CLOSED_SHULKER)
			.isViewBlocking(NOT_CLOSED_SHULKER)
			.pushReaction(PushReaction.DESTROY);
	}

	private static BlockBehaviour.Properties pistonProperties() {
		return BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.strength(1.5F)
			.isRedstoneConductor(Blocks::never)
			.isSuffocating(NOT_EXTENDED_PISTON)
			.isViewBlocking(NOT_EXTENDED_PISTON)
			.pushReaction(PushReaction.BLOCK);
	}

	private static BlockBehaviour.Properties buttonProperties() {
		return BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY);
	}

	private static BlockBehaviour.Properties flowerPotProperties() {
		return BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY);
	}

	private static BlockBehaviour.Properties candleProperties(MapColor mapColor) {
		return BlockBehaviour.Properties.of()
			.mapColor(mapColor)
			.noOcclusion()
			.strength(0.1F)
			.sound(SoundType.CANDLE)
			.lightLevel(CandleBlock.LIGHT_EMISSION)
			.pushReaction(PushReaction.DESTROY);
	}

	@Deprecated
	private static Block registerLegacyStair(String string, Block block) {
		return register(string, properties -> new StairBlock(block.defaultBlockState(), properties), BlockBehaviour.Properties.ofLegacyCopy(block));
	}

	private static Block registerStair(String string, Block block) {
		return register(string, properties -> new StairBlock(block.defaultBlockState(), properties), BlockBehaviour.Properties.ofFullCopy(block));
	}

	private static BlockBehaviour.Properties wallVariant(Block block, boolean bl) {
		BlockBehaviour.Properties properties = block.properties();
		BlockBehaviour.Properties properties2 = BlockBehaviour.Properties.of().overrideLootTable(block.getLootTable());
		if (bl) {
			properties2 = properties2.overrideDescription(block.getDescriptionId());
		}

		return properties2;
	}

	private static Block register(ResourceKey<Block> resourceKey, Function<BlockBehaviour.Properties, Block> function, BlockBehaviour.Properties properties) {
		Block block = (Block)function.apply(properties.setId(resourceKey));
		return Registry.register(BuiltInRegistries.BLOCK, resourceKey, block);
	}

	private static Block register(ResourceKey<Block> resourceKey, BlockBehaviour.Properties properties) {
		return register(resourceKey, Block::new, properties);
	}

	private static ResourceKey<Block> vanillaBlockId(String string) {
		return ResourceKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace(string));
	}

	private static Block register(String string, Function<BlockBehaviour.Properties, Block> function, BlockBehaviour.Properties properties) {
		return register(vanillaBlockId(string), function, properties);
	}

	private static Block register(String string, BlockBehaviour.Properties properties) {
		return register(string, Block::new, properties);
	}

	static {
		for (Block block : BuiltInRegistries.BLOCK) {
			for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
				Block.BLOCK_STATE_REGISTRY.add(blockState);
				blockState.initCache();
			}
		}
	}
}
