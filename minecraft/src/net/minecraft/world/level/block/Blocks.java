package net.minecraft.world.level.block;

import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.references.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
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
	public static final Block AIR = register("air", new AirBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().air()));
	public static final Block STONE = register(
		"stone",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F))
	);
	public static final Block GRANITE = register(
		"granite",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F))
	);
	public static final Block POLISHED_GRANITE = register(
		"polished_granite",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F))
	);
	public static final Block DIORITE = register(
		"diorite",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block POLISHED_DIORITE = register(
		"polished_diorite",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block ANDESITE = register(
		"andesite",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F))
	);
	public static final Block POLISHED_ANDESITE = register(
		"polished_andesite",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F))
	);
	public static final Block GRASS_BLOCK = register(
		"grass_block", new GrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS))
	);
	public static final Block DIRT = register("dirt", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));
	public static final Block COARSE_DIRT = register(
		"coarse_dirt", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL))
	);
	public static final Block PODZOL = register(
		"podzol", new SnowyDirtBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).strength(0.5F).sound(SoundType.GRAVEL))
	);
	public static final Block COBBLESTONE = register(
		"cobblestone",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F))
	);
	public static final Block OAK_PLANKS = register(
		"oak_planks",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block SPRUCE_PLANKS = register(
		"spruce_planks",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block BIRCH_PLANKS = register(
		"birch_planks",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block JUNGLE_PLANKS = register(
		"jungle_planks",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block ACACIA_PLANKS = register(
		"acacia_planks",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block CHERRY_PLANKS = register(
		"cherry_planks",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_WHITE)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.CHERRY_WOOD)
				.ignitedByLava()
		)
	);
	public static final Block DARK_OAK_PLANKS = register(
		"dark_oak_planks",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BROWN)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block MANGROVE_PLANKS = register(
		"mangrove_planks",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block BAMBOO_PLANKS = register(
		"bamboo_planks",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.BAMBOO_WOOD)
				.ignitedByLava()
		)
	);
	public static final Block BAMBOO_MOSAIC = register(
		"bamboo_mosaic",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.BAMBOO_WOOD)
				.ignitedByLava()
		)
	);
	public static final Block OAK_SAPLING = register(
		"oak_sapling",
		new SaplingBlock(
			TreeGrower.OAK,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SPRUCE_SAPLING = register(
		"spruce_sapling",
		new SaplingBlock(
			TreeGrower.SPRUCE,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BIRCH_SAPLING = register(
		"birch_sapling",
		new SaplingBlock(
			TreeGrower.BIRCH,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block JUNGLE_SAPLING = register(
		"jungle_sapling",
		new SaplingBlock(
			TreeGrower.JUNGLE,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ACACIA_SAPLING = register(
		"acacia_sapling",
		new SaplingBlock(
			TreeGrower.ACACIA,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CHERRY_SAPLING = register(
		"cherry_sapling",
		new SaplingBlock(
			TreeGrower.CHERRY,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_PINK)
				.noCollission()
				.randomTicks()
				.instabreak()
				.sound(SoundType.CHERRY_SAPLING)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block DARK_OAK_SAPLING = register(
		"dark_oak_sapling",
		new SaplingBlock(
			TreeGrower.DARK_OAK,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block MANGROVE_PROPAGULE = register(
		"mangrove_propagule",
		new MangrovePropaguleBlock(
			TreeGrower.MANGROVE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.randomTicks()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BEDROCK = register(
		"bedrock",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.strength(-1.0F, 3600000.0F)
				.noLootTable()
				.isValidSpawn(Blocks::never)
		)
	);
	public static final Block WATER = register(
		"water",
		new LiquidBlock(
			Fluids.WATER,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WATER)
				.replaceable()
				.noCollission()
				.strength(100.0F)
				.pushReaction(PushReaction.DESTROY)
				.noLootTable()
				.liquid()
				.sound(SoundType.EMPTY)
		)
	);
	public static final Block LAVA = register(
		"lava",
		new LiquidBlock(
			Fluids.LAVA,
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
		)
	);
	public static final Block SAND = register(
		"sand",
		new ColoredFallingBlock(
			new ColorRGBA(14406560), BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block SUSPICIOUS_SAND = register(
		"suspicious_sand",
		new BrushableBlock(
			SAND,
			SoundEvents.BRUSH_SAND,
			SoundEvents.BRUSH_SAND_COMPLETED,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.SAND)
				.instrument(NoteBlockInstrument.SNARE)
				.strength(0.25F)
				.sound(SoundType.SUSPICIOUS_SAND)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block RED_SAND = register(
		"red_sand",
		new ColoredFallingBlock(
			new ColorRGBA(11098145),
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block GRAVEL = register(
		"gravel",
		new ColoredFallingBlock(
			new ColorRGBA(-8356741),
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.SNARE).strength(0.6F).sound(SoundType.GRAVEL)
		)
	);
	public static final Block SUSPICIOUS_GRAVEL = register(
		"suspicious_gravel",
		new BrushableBlock(
			GRAVEL,
			SoundEvents.BRUSH_GRAVEL,
			SoundEvents.BRUSH_GRAVEL_COMPLETED,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.SNARE)
				.strength(0.25F)
				.sound(SoundType.SUSPICIOUS_GRAVEL)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block GOLD_ORE = register(
		"gold_ore",
		new DropExperienceBlock(
			ConstantInt.of(0),
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
		)
	);
	public static final Block DEEPSLATE_GOLD_ORE = register(
		"deepslate_gold_ore",
		new DropExperienceBlock(
			ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(GOLD_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
		)
	);
	public static final Block IRON_ORE = register(
		"iron_ore",
		new DropExperienceBlock(
			ConstantInt.of(0),
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
		)
	);
	public static final Block DEEPSLATE_IRON_ORE = register(
		"deepslate_iron_ore",
		new DropExperienceBlock(
			ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(IRON_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
		)
	);
	public static final Block COAL_ORE = register(
		"coal_ore",
		new DropExperienceBlock(
			UniformInt.of(0, 2),
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
		)
	);
	public static final Block DEEPSLATE_COAL_ORE = register(
		"deepslate_coal_ore",
		new DropExperienceBlock(
			UniformInt.of(0, 2), BlockBehaviour.Properties.ofLegacyCopy(COAL_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
		)
	);
	public static final Block NETHER_GOLD_ORE = register(
		"nether_gold_ore",
		new DropExperienceBlock(
			UniformInt.of(0, 1),
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(3.0F, 3.0F)
				.sound(SoundType.NETHER_GOLD_ORE)
		)
	);
	public static final Block OAK_LOG = register("oak_log", log(MapColor.WOOD, MapColor.PODZOL));
	public static final Block SPRUCE_LOG = register("spruce_log", log(MapColor.PODZOL, MapColor.COLOR_BROWN));
	public static final Block BIRCH_LOG = register("birch_log", log(MapColor.SAND, MapColor.QUARTZ));
	public static final Block JUNGLE_LOG = register("jungle_log", log(MapColor.DIRT, MapColor.PODZOL));
	public static final Block ACACIA_LOG = register("acacia_log", log(MapColor.COLOR_ORANGE, MapColor.STONE));
	public static final Block CHERRY_LOG = register("cherry_log", log(MapColor.TERRACOTTA_WHITE, MapColor.TERRACOTTA_GRAY, SoundType.CHERRY_WOOD));
	public static final Block DARK_OAK_LOG = register("dark_oak_log", log(MapColor.COLOR_BROWN, MapColor.COLOR_BROWN));
	public static final Block MANGROVE_LOG = register("mangrove_log", log(MapColor.COLOR_RED, MapColor.PODZOL));
	public static final Block MANGROVE_ROOTS = register(
		"mangrove_roots",
		new MangroveRootsBlock(
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
		)
	);
	public static final Block MUDDY_MANGROVE_ROOTS = register(
		"muddy_mangrove_roots", new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).strength(0.7F).sound(SoundType.MUDDY_MANGROVE_ROOTS))
	);
	public static final Block BAMBOO_BLOCK = register("bamboo_block", log(MapColor.COLOR_YELLOW, MapColor.PLANT, SoundType.BAMBOO_WOOD));
	public static final Block STRIPPED_SPRUCE_LOG = register("stripped_spruce_log", log(MapColor.PODZOL, MapColor.PODZOL));
	public static final Block STRIPPED_BIRCH_LOG = register("stripped_birch_log", log(MapColor.SAND, MapColor.SAND));
	public static final Block STRIPPED_JUNGLE_LOG = register("stripped_jungle_log", log(MapColor.DIRT, MapColor.DIRT));
	public static final Block STRIPPED_ACACIA_LOG = register("stripped_acacia_log", log(MapColor.COLOR_ORANGE, MapColor.COLOR_ORANGE));
	public static final Block STRIPPED_CHERRY_LOG = register(
		"stripped_cherry_log", log(MapColor.TERRACOTTA_WHITE, MapColor.TERRACOTTA_PINK, SoundType.CHERRY_WOOD)
	);
	public static final Block STRIPPED_DARK_OAK_LOG = register("stripped_dark_oak_log", log(MapColor.COLOR_BROWN, MapColor.COLOR_BROWN));
	public static final Block STRIPPED_OAK_LOG = register("stripped_oak_log", log(MapColor.WOOD, MapColor.WOOD));
	public static final Block STRIPPED_MANGROVE_LOG = register("stripped_mangrove_log", log(MapColor.COLOR_RED, MapColor.COLOR_RED));
	public static final Block STRIPPED_BAMBOO_BLOCK = register("stripped_bamboo_block", log(MapColor.COLOR_YELLOW, MapColor.COLOR_YELLOW, SoundType.BAMBOO_WOOD));
	public static final Block OAK_WOOD = register(
		"oak_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block SPRUCE_WOOD = register(
		"spruce_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block BIRCH_WOOD = register(
		"birch_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block JUNGLE_WOOD = register(
		"jungle_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block ACACIA_WOOD = register(
		"acacia_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block CHERRY_WOOD = register(
		"cherry_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_GRAY)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F)
				.sound(SoundType.CHERRY_WOOD)
				.ignitedByLava()
		)
	);
	public static final Block DARK_OAK_WOOD = register(
		"dark_oak_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block MANGROVE_WOOD = register(
		"mangrove_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block STRIPPED_OAK_WOOD = register(
		"stripped_oak_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block STRIPPED_SPRUCE_WOOD = register(
		"stripped_spruce_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block STRIPPED_BIRCH_WOOD = register(
		"stripped_birch_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block STRIPPED_JUNGLE_WOOD = register(
		"stripped_jungle_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block STRIPPED_ACACIA_WOOD = register(
		"stripped_acacia_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block STRIPPED_CHERRY_WOOD = register(
		"stripped_cherry_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_PINK)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F)
				.sound(SoundType.CHERRY_WOOD)
				.ignitedByLava()
		)
	);
	public static final Block STRIPPED_DARK_OAK_WOOD = register(
		"stripped_dark_oak_wood",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block STRIPPED_MANGROVE_WOOD = register("stripped_mangrove_wood", log(MapColor.COLOR_RED, MapColor.COLOR_RED));
	public static final Block OAK_LEAVES = register("oak_leaves", leaves(SoundType.GRASS));
	public static final Block SPRUCE_LEAVES = register("spruce_leaves", leaves(SoundType.GRASS));
	public static final Block BIRCH_LEAVES = register("birch_leaves", leaves(SoundType.GRASS));
	public static final Block JUNGLE_LEAVES = register("jungle_leaves", leaves(SoundType.GRASS));
	public static final Block ACACIA_LEAVES = register("acacia_leaves", leaves(SoundType.GRASS));
	public static final Block CHERRY_LEAVES = register(
		"cherry_leaves",
		new CherryLeavesBlock(
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
		)
	);
	public static final Block DARK_OAK_LEAVES = register("dark_oak_leaves", leaves(SoundType.GRASS));
	public static final Block MANGROVE_LEAVES = register(
		"mangrove_leaves",
		new MangroveLeavesBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.strength(0.2F)
				.randomTicks()
				.sound(SoundType.GRASS)
				.noOcclusion()
				.isValidSpawn(Blocks::ocelotOrParrot)
				.isSuffocating(Blocks::never)
				.isViewBlocking(Blocks::never)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
				.isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block AZALEA_LEAVES = register("azalea_leaves", leaves(SoundType.AZALEA_LEAVES));
	public static final Block FLOWERING_AZALEA_LEAVES = register("flowering_azalea_leaves", leaves(SoundType.AZALEA_LEAVES));
	public static final Block SPONGE = register(
		"sponge", new SpongeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.6F).sound(SoundType.SPONGE))
	);
	public static final Block WET_SPONGE = register(
		"wet_sponge", new WetSpongeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.6F).sound(SoundType.WET_SPONGE))
	);
	public static final Block GLASS = register(
		"glass",
		new TransparentBlock(
			BlockBehaviour.Properties.of()
				.instrument(NoteBlockInstrument.HAT)
				.strength(0.3F)
				.sound(SoundType.GLASS)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.isRedstoneConductor(Blocks::never)
				.isSuffocating(Blocks::never)
				.isViewBlocking(Blocks::never)
		)
	);
	public static final Block LAPIS_ORE = register(
		"lapis_ore",
		new DropExperienceBlock(
			UniformInt.of(2, 5),
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
		)
	);
	public static final Block DEEPSLATE_LAPIS_ORE = register(
		"deepslate_lapis_ore",
		new DropExperienceBlock(
			UniformInt.of(2, 5), BlockBehaviour.Properties.ofLegacyCopy(LAPIS_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
		)
	);
	public static final Block LAPIS_BLOCK = register(
		"lapis_block", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.LAPIS).requiresCorrectToolForDrops().strength(3.0F, 3.0F))
	);
	public static final Block DISPENSER = register(
		"dispenser",
		new DispenserBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)
		)
	);
	public static final Block SANDSTONE = register(
		"sandstone",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F))
	);
	public static final Block CHISELED_SANDSTONE = register(
		"chiseled_sandstone",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F))
	);
	public static final Block CUT_SANDSTONE = register(
		"cut_sandstone",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F))
	);
	public static final Block NOTE_BLOCK = register(
		"note_block",
		new NoteBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).strength(0.8F).ignitedByLava()
		)
	);
	public static final Block WHITE_BED = register("white_bed", bed(DyeColor.WHITE));
	public static final Block ORANGE_BED = register("orange_bed", bed(DyeColor.ORANGE));
	public static final Block MAGENTA_BED = register("magenta_bed", bed(DyeColor.MAGENTA));
	public static final Block LIGHT_BLUE_BED = register("light_blue_bed", bed(DyeColor.LIGHT_BLUE));
	public static final Block YELLOW_BED = register("yellow_bed", bed(DyeColor.YELLOW));
	public static final Block LIME_BED = register("lime_bed", bed(DyeColor.LIME));
	public static final Block PINK_BED = register("pink_bed", bed(DyeColor.PINK));
	public static final Block GRAY_BED = register("gray_bed", bed(DyeColor.GRAY));
	public static final Block LIGHT_GRAY_BED = register("light_gray_bed", bed(DyeColor.LIGHT_GRAY));
	public static final Block CYAN_BED = register("cyan_bed", bed(DyeColor.CYAN));
	public static final Block PURPLE_BED = register("purple_bed", bed(DyeColor.PURPLE));
	public static final Block BLUE_BED = register("blue_bed", bed(DyeColor.BLUE));
	public static final Block BROWN_BED = register("brown_bed", bed(DyeColor.BROWN));
	public static final Block GREEN_BED = register("green_bed", bed(DyeColor.GREEN));
	public static final Block RED_BED = register("red_bed", bed(DyeColor.RED));
	public static final Block BLACK_BED = register("black_bed", bed(DyeColor.BLACK));
	public static final Block POWERED_RAIL = register(
		"powered_rail", new PoweredRailBlock(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL))
	);
	public static final Block DETECTOR_RAIL = register(
		"detector_rail", new DetectorRailBlock(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL))
	);
	public static final Block STICKY_PISTON = register("sticky_piston", pistonBase(true));
	public static final Block COBWEB = register(
		"cobweb",
		new WebBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOL)
				.forceSolidOn()
				.noCollission()
				.requiresCorrectToolForDrops()
				.strength(4.0F)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SHORT_GRASS = register(
		"short_grass",
		new TallGrassBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XYZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block FERN = register(
		"fern",
		new TallGrassBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XYZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block DEAD_BUSH = register(
		"dead_bush",
		new DeadBushBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SEAGRASS = register(
		"seagrass",
		new SeagrassBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WATER)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.WET_GRASS)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block TALL_SEAGRASS = register(
		"tall_seagrass",
		new TallSeagrassBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WATER)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.WET_GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block PISTON = register("piston", pistonBase(false));
	public static final Block PISTON_HEAD = register(
		"piston_head", new PistonHeadBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F).noLootTable().pushReaction(PushReaction.BLOCK))
	);
	public static final Block WHITE_WOOL = register(
		"white_wool",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block ORANGE_WOOL = register(
		"orange_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block MAGENTA_WOOL = register(
		"magenta_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block LIGHT_BLUE_WOOL = register(
		"light_blue_wool",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.instrument(NoteBlockInstrument.GUITAR)
				.strength(0.8F)
				.sound(SoundType.WOOL)
				.ignitedByLava()
		)
	);
	public static final Block YELLOW_WOOL = register(
		"yellow_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block LIME_WOOL = register(
		"lime_wool",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_LIGHT_GREEN)
				.instrument(NoteBlockInstrument.GUITAR)
				.strength(0.8F)
				.sound(SoundType.WOOL)
				.ignitedByLava()
		)
	);
	public static final Block PINK_WOOL = register(
		"pink_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block GRAY_WOOL = register(
		"gray_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block LIGHT_GRAY_WOOL = register(
		"light_gray_wool",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_LIGHT_GRAY)
				.instrument(NoteBlockInstrument.GUITAR)
				.strength(0.8F)
				.sound(SoundType.WOOL)
				.ignitedByLava()
		)
	);
	public static final Block CYAN_WOOL = register(
		"cyan_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block PURPLE_WOOL = register(
		"purple_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block BLUE_WOOL = register(
		"blue_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block BROWN_WOOL = register(
		"brown_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block GREEN_WOOL = register(
		"green_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block RED_WOOL = register(
		"red_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block BLACK_WOOL = register(
		"black_wool",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block MOVING_PISTON = register(
		"moving_piston",
		new MovingPistonBlock(
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
		)
	);
	public static final Block DANDELION = register(
		"dandelion",
		new FlowerBlock(
			MobEffects.SATURATION,
			0.35F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block TORCHFLOWER = register(
		"torchflower",
		new FlowerBlock(
			MobEffects.NIGHT_VISION,
			5.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block POPPY = register(
		"poppy",
		new FlowerBlock(
			MobEffects.NIGHT_VISION,
			5.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BLUE_ORCHID = register(
		"blue_orchid",
		new FlowerBlock(
			MobEffects.SATURATION,
			0.35F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ALLIUM = register(
		"allium",
		new FlowerBlock(
			MobEffects.FIRE_RESISTANCE,
			4.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block AZURE_BLUET = register(
		"azure_bluet",
		new FlowerBlock(
			MobEffects.BLINDNESS,
			8.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block RED_TULIP = register(
		"red_tulip",
		new FlowerBlock(
			MobEffects.WEAKNESS,
			9.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ORANGE_TULIP = register(
		"orange_tulip",
		new FlowerBlock(
			MobEffects.WEAKNESS,
			9.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block WHITE_TULIP = register(
		"white_tulip",
		new FlowerBlock(
			MobEffects.WEAKNESS,
			9.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block PINK_TULIP = register(
		"pink_tulip",
		new FlowerBlock(
			MobEffects.WEAKNESS,
			9.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block OXEYE_DAISY = register(
		"oxeye_daisy",
		new FlowerBlock(
			MobEffects.REGENERATION,
			8.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CORNFLOWER = register(
		"cornflower",
		new FlowerBlock(
			MobEffects.JUMP,
			6.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block WITHER_ROSE = register(
		"wither_rose",
		new WitherRoseBlock(
			MobEffects.WITHER,
			8.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block LILY_OF_THE_VALLEY = register(
		"lily_of_the_valley",
		new FlowerBlock(
			MobEffects.POISON,
			12.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BROWN_MUSHROOM = register(
		"brown_mushroom",
		new MushroomBlock(
			TreeFeatures.HUGE_BROWN_MUSHROOM,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BROWN)
				.noCollission()
				.randomTicks()
				.instabreak()
				.sound(SoundType.GRASS)
				.lightLevel(blockStatex -> 1)
				.hasPostProcess(Blocks::always)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block RED_MUSHROOM = register(
		"red_mushroom",
		new MushroomBlock(
			TreeFeatures.HUGE_RED_MUSHROOM,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.noCollission()
				.randomTicks()
				.instabreak()
				.sound(SoundType.GRASS)
				.hasPostProcess(Blocks::always)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block GOLD_BLOCK = register(
		"gold_block",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.GOLD)
				.instrument(NoteBlockInstrument.BELL)
				.requiresCorrectToolForDrops()
				.strength(3.0F, 6.0F)
				.sound(SoundType.METAL)
		)
	);
	public static final Block IRON_BLOCK = register(
		"iron_block",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.instrument(NoteBlockInstrument.IRON_XYLOPHONE)
				.requiresCorrectToolForDrops()
				.strength(5.0F, 6.0F)
				.sound(SoundType.METAL)
		)
	);
	public static final Block BRICKS = register(
		"bricks",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block TNT = register(
		"tnt",
		new TntBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instabreak().sound(SoundType.GRASS).ignitedByLava().isRedstoneConductor(Blocks::never))
	);
	public static final Block BOOKSHELF = register(
		"bookshelf",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.5F).sound(SoundType.WOOD).ignitedByLava())
	);
	public static final Block CHISELED_BOOKSHELF = register(
		"chiseled_bookshelf",
		new ChiseledBookShelfBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.instrument(NoteBlockInstrument.BASS)
				.strength(1.5F)
				.sound(SoundType.CHISELED_BOOKSHELF)
				.ignitedByLava()
		)
	);
	public static final Block MOSSY_COBBLESTONE = register(
		"mossy_cobblestone",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F))
	);
	public static final Block OBSIDIAN = register(
		"obsidian",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(50.0F, 1200.0F)
		)
	);
	public static final Block TORCH = register(
		"torch",
		new TorchBlock(
			ParticleTypes.FLAME,
			BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(blockStatex -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block WALL_TORCH = register(
		"wall_torch",
		new WallTorchBlock(
			ParticleTypes.FLAME,
			BlockBehaviour.Properties.of()
				.noCollission()
				.instabreak()
				.lightLevel(blockStatex -> 14)
				.sound(SoundType.WOOD)
				.dropsLike(TORCH)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block FIRE = register(
		"fire",
		new FireBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.FIRE)
				.replaceable()
				.noCollission()
				.instabreak()
				.lightLevel(blockStatex -> 15)
				.sound(SoundType.WOOL)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SOUL_FIRE = register(
		"soul_fire",
		new SoulFireBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.replaceable()
				.noCollission()
				.instabreak()
				.lightLevel(blockStatex -> 10)
				.sound(SoundType.WOOL)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SPAWNER = register(
		"spawner",
		new SpawnerBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(5.0F)
				.sound(SoundType.METAL)
				.noOcclusion()
		)
	);
	public static final Block OAK_STAIRS = register("oak_stairs", legacyStair(OAK_PLANKS));
	public static final Block CHEST = register(
		"chest",
		new ChestBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava(),
			() -> BlockEntityType.CHEST
		)
	);
	public static final Block REDSTONE_WIRE = register(
		"redstone_wire", new RedStoneWireBlock(BlockBehaviour.Properties.of().noCollission().instabreak().pushReaction(PushReaction.DESTROY))
	);
	public static final Block DIAMOND_ORE = register(
		"diamond_ore",
		new DropExperienceBlock(
			UniformInt.of(3, 7),
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
		)
	);
	public static final Block DEEPSLATE_DIAMOND_ORE = register(
		"deepslate_diamond_ore",
		new DropExperienceBlock(
			UniformInt.of(3, 7), BlockBehaviour.Properties.ofLegacyCopy(DIAMOND_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
		)
	);
	public static final Block DIAMOND_BLOCK = register(
		"diamond_block",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL))
	);
	public static final Block CRAFTING_TABLE = register(
		"crafting_table",
		new CraftingTableBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block WHEAT = register(
		"wheat",
		new CropBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block FARMLAND = register(
		"farmland",
		new FarmBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.DIRT)
				.randomTicks()
				.strength(0.6F)
				.sound(SoundType.GRAVEL)
				.isViewBlocking(Blocks::always)
				.isSuffocating(Blocks::always)
		)
	);
	public static final Block FURNACE = register(
		"furnace",
		new FurnaceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(3.5F)
				.lightLevel(litBlockEmission(13))
		)
	);
	public static final Block OAK_SIGN = register(
		"oak_sign",
		new StandingSignBlock(
			WoodType.OAK,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()
		)
	);
	public static final Block SPRUCE_SIGN = register(
		"spruce_sign",
		new StandingSignBlock(
			WoodType.SPRUCE,
			BlockBehaviour.Properties.of()
				.mapColor(SPRUCE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block BIRCH_SIGN = register(
		"birch_sign",
		new StandingSignBlock(
			WoodType.BIRCH,
			BlockBehaviour.Properties.of().mapColor(MapColor.SAND).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()
		)
	);
	public static final Block ACACIA_SIGN = register(
		"acacia_sign",
		new StandingSignBlock(
			WoodType.ACACIA,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block CHERRY_SIGN = register(
		"cherry_sign",
		new StandingSignBlock(
			WoodType.CHERRY,
			BlockBehaviour.Properties.of()
				.mapColor(CHERRY_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block JUNGLE_SIGN = register(
		"jungle_sign",
		new StandingSignBlock(
			WoodType.JUNGLE,
			BlockBehaviour.Properties.of()
				.mapColor(JUNGLE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block DARK_OAK_SIGN = register(
		"dark_oak_sign",
		new StandingSignBlock(
			WoodType.DARK_OAK,
			BlockBehaviour.Properties.of()
				.mapColor(DARK_OAK_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block MANGROVE_SIGN = register(
		"mangrove_sign",
		new StandingSignBlock(
			WoodType.MANGROVE,
			BlockBehaviour.Properties.of()
				.mapColor(MANGROVE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block BAMBOO_SIGN = register(
		"bamboo_sign",
		new StandingSignBlock(
			WoodType.BAMBOO,
			BlockBehaviour.Properties.of()
				.mapColor(BAMBOO_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block OAK_DOOR = register(
		"oak_door",
		new DoorBlock(
			BlockSetType.OAK,
			BlockBehaviour.Properties.of()
				.mapColor(OAK_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block LADDER = register(
		"ladder",
		new LadderBlock(BlockBehaviour.Properties.of().forceSolidOff().strength(0.4F).sound(SoundType.LADDER).noOcclusion().pushReaction(PushReaction.DESTROY))
	);
	public static final Block RAIL = register("rail", new RailBlock(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL)));
	public static final Block COBBLESTONE_STAIRS = register("cobblestone_stairs", legacyStair(COBBLESTONE));
	public static final Block OAK_WALL_SIGN = register(
		"oak_wall_sign",
		new WallSignBlock(
			WoodType.OAK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(OAK_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block SPRUCE_WALL_SIGN = register(
		"spruce_wall_sign",
		new WallSignBlock(
			WoodType.SPRUCE,
			BlockBehaviour.Properties.of()
				.mapColor(SPRUCE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(SPRUCE_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block BIRCH_WALL_SIGN = register(
		"birch_wall_sign",
		new WallSignBlock(
			WoodType.BIRCH,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.SAND)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(BIRCH_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block ACACIA_WALL_SIGN = register(
		"acacia_wall_sign",
		new WallSignBlock(
			WoodType.ACACIA,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(ACACIA_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block CHERRY_WALL_SIGN = register(
		"cherry_wall_sign",
		new WallSignBlock(
			WoodType.CHERRY,
			BlockBehaviour.Properties.of()
				.mapColor(CHERRY_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(CHERRY_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block JUNGLE_WALL_SIGN = register(
		"jungle_wall_sign",
		new WallSignBlock(
			WoodType.JUNGLE,
			BlockBehaviour.Properties.of()
				.mapColor(JUNGLE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(JUNGLE_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block DARK_OAK_WALL_SIGN = register(
		"dark_oak_wall_sign",
		new WallSignBlock(
			WoodType.DARK_OAK,
			BlockBehaviour.Properties.of()
				.mapColor(DARK_OAK_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(DARK_OAK_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block MANGROVE_WALL_SIGN = register(
		"mangrove_wall_sign",
		new WallSignBlock(
			WoodType.MANGROVE,
			BlockBehaviour.Properties.of()
				.mapColor(MANGROVE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(MANGROVE_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block BAMBOO_WALL_SIGN = register(
		"bamboo_wall_sign",
		new WallSignBlock(
			WoodType.BAMBOO,
			BlockBehaviour.Properties.of()
				.mapColor(BAMBOO_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
				.dropsLike(BAMBOO_SIGN)
		)
	);
	public static final Block OAK_HANGING_SIGN = register(
		"oak_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.OAK,
			BlockBehaviour.Properties.of()
				.mapColor(OAK_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block SPRUCE_HANGING_SIGN = register(
		"spruce_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.SPRUCE,
			BlockBehaviour.Properties.of()
				.mapColor(SPRUCE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block BIRCH_HANGING_SIGN = register(
		"birch_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.BIRCH,
			BlockBehaviour.Properties.of().mapColor(MapColor.SAND).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()
		)
	);
	public static final Block ACACIA_HANGING_SIGN = register(
		"acacia_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.ACACIA,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block CHERRY_HANGING_SIGN = register(
		"cherry_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.CHERRY,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_PINK)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block JUNGLE_HANGING_SIGN = register(
		"jungle_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.JUNGLE,
			BlockBehaviour.Properties.of()
				.mapColor(JUNGLE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block DARK_OAK_HANGING_SIGN = register(
		"dark_oak_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.DARK_OAK,
			BlockBehaviour.Properties.of()
				.mapColor(DARK_OAK_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block CRIMSON_HANGING_SIGN = register(
		"crimson_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.CRIMSON,
			BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_STEM).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F)
		)
	);
	public static final Block WARPED_HANGING_SIGN = register(
		"warped_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.WARPED,
			BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_STEM).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F)
		)
	);
	public static final Block MANGROVE_HANGING_SIGN = register(
		"mangrove_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.MANGROVE,
			BlockBehaviour.Properties.of()
				.mapColor(MANGROVE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block BAMBOO_HANGING_SIGN = register(
		"bamboo_hanging_sign",
		new CeilingHangingSignBlock(
			WoodType.BAMBOO,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
		)
	);
	public static final Block OAK_WALL_HANGING_SIGN = register(
		"oak_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.OAK,
			BlockBehaviour.Properties.of()
				.mapColor(OAK_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
				.dropsLike(OAK_HANGING_SIGN)
		)
	);
	public static final Block SPRUCE_WALL_HANGING_SIGN = register(
		"spruce_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.SPRUCE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(SPRUCE_HANGING_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block BIRCH_WALL_HANGING_SIGN = register(
		"birch_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.BIRCH,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.SAND)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(BIRCH_HANGING_SIGN)
				.ignitedByLava()
		)
	);
	public static final Block ACACIA_WALL_HANGING_SIGN = register(
		"acacia_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.ACACIA,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
				.dropsLike(ACACIA_HANGING_SIGN)
		)
	);
	public static final Block CHERRY_WALL_HANGING_SIGN = register(
		"cherry_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.CHERRY,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_PINK)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
				.dropsLike(CHERRY_HANGING_SIGN)
		)
	);
	public static final Block JUNGLE_WALL_HANGING_SIGN = register(
		"jungle_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.JUNGLE,
			BlockBehaviour.Properties.of()
				.mapColor(JUNGLE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
				.dropsLike(JUNGLE_HANGING_SIGN)
		)
	);
	public static final Block DARK_OAK_WALL_HANGING_SIGN = register(
		"dark_oak_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.DARK_OAK,
			BlockBehaviour.Properties.of()
				.mapColor(DARK_OAK_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
				.dropsLike(DARK_OAK_HANGING_SIGN)
		)
	);
	public static final Block MANGROVE_WALL_HANGING_SIGN = register(
		"mangrove_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.MANGROVE,
			BlockBehaviour.Properties.of()
				.mapColor(MANGROVE_LOG.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
				.dropsLike(MANGROVE_HANGING_SIGN)
		)
	);
	public static final Block CRIMSON_WALL_HANGING_SIGN = register(
		"crimson_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.CRIMSON,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.CRIMSON_STEM)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(CRIMSON_HANGING_SIGN)
		)
	);
	public static final Block WARPED_WALL_HANGING_SIGN = register(
		"warped_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.WARPED,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WARPED_STEM)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.dropsLike(WARPED_HANGING_SIGN)
		)
	);
	public static final Block BAMBOO_WALL_HANGING_SIGN = register(
		"bamboo_wall_hanging_sign",
		new WallHangingSignBlock(
			WoodType.BAMBOO,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.ignitedByLava()
				.dropsLike(BAMBOO_HANGING_SIGN)
		)
	);
	public static final Block LEVER = register(
		"lever", new LeverBlock(BlockBehaviour.Properties.of().noCollission().strength(0.5F).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY))
	);
	public static final Block STONE_PRESSURE_PLATE = register(
		"stone_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.STONE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.strength(0.5F)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block IRON_DOOR = register(
		"iron_door",
		new DoorBlock(
			BlockSetType.IRON,
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F).noOcclusion().pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block OAK_PRESSURE_PLATE = register(
		"oak_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.OAK,
			BlockBehaviour.Properties.of()
				.mapColor(OAK_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SPRUCE_PRESSURE_PLATE = register(
		"spruce_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.SPRUCE,
			BlockBehaviour.Properties.of()
				.mapColor(SPRUCE_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BIRCH_PRESSURE_PLATE = register(
		"birch_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.BIRCH,
			BlockBehaviour.Properties.of()
				.mapColor(BIRCH_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block JUNGLE_PRESSURE_PLATE = register(
		"jungle_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.JUNGLE,
			BlockBehaviour.Properties.of()
				.mapColor(JUNGLE_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ACACIA_PRESSURE_PLATE = register(
		"acacia_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.ACACIA,
			BlockBehaviour.Properties.of()
				.mapColor(ACACIA_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CHERRY_PRESSURE_PLATE = register(
		"cherry_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.CHERRY,
			BlockBehaviour.Properties.of()
				.mapColor(CHERRY_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block DARK_OAK_PRESSURE_PLATE = register(
		"dark_oak_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.DARK_OAK,
			BlockBehaviour.Properties.of()
				.mapColor(DARK_OAK_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block MANGROVE_PRESSURE_PLATE = register(
		"mangrove_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.MANGROVE,
			BlockBehaviour.Properties.of()
				.mapColor(MANGROVE_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BAMBOO_PRESSURE_PLATE = register(
		"bamboo_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.BAMBOO,
			BlockBehaviour.Properties.of()
				.mapColor(BAMBOO_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block REDSTONE_ORE = register(
		"redstone_ore",
		new RedStoneOreBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.randomTicks()
				.lightLevel(litBlockEmission(9))
				.strength(3.0F, 3.0F)
		)
	);
	public static final Block DEEPSLATE_REDSTONE_ORE = register(
		"deepslate_redstone_ore",
		new RedStoneOreBlock(BlockBehaviour.Properties.ofLegacyCopy(REDSTONE_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE))
	);
	public static final Block REDSTONE_TORCH = register(
		"redstone_torch",
		new RedstoneTorchBlock(
			BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block REDSTONE_WALL_TORCH = register(
		"redstone_wall_torch",
		new RedstoneWallTorchBlock(
			BlockBehaviour.Properties.of()
				.noCollission()
				.instabreak()
				.lightLevel(litBlockEmission(7))
				.sound(SoundType.WOOD)
				.dropsLike(REDSTONE_TORCH)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block STONE_BUTTON = register("stone_button", stoneButton());
	public static final Block SNOW = register(
		"snow",
		new SnowLayerBlock(
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
		)
	);
	public static final Block ICE = register(
		"ice",
		new IceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.ICE)
				.friction(0.98F)
				.randomTicks()
				.strength(0.5F)
				.sound(SoundType.GLASS)
				.noOcclusion()
				.isValidSpawn((blockStatex, blockGetter, blockPos, entityType) -> entityType == EntityType.POLAR_BEAR)
				.isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block SNOW_BLOCK = register(
		"snow_block", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).requiresCorrectToolForDrops().strength(0.2F).sound(SoundType.SNOW))
	);
	public static final Block CACTUS = register(
		"cactus",
		new CactusBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().strength(0.4F).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY))
	);
	public static final Block CLAY = register(
		"clay", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).instrument(NoteBlockInstrument.FLUTE).strength(0.6F).sound(SoundType.GRAVEL))
	);
	public static final Block SUGAR_CANE = register(
		"sugar_cane",
		new SugarCaneBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block JUKEBOX = register(
		"jukebox",
		new JukeboxBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F, 6.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block OAK_FENCE = register(
		"oak_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(OAK_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block NETHERRACK = register(
		"netherrack",
		new NetherrackBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(0.4F)
				.sound(SoundType.NETHERRACK)
		)
	);
	public static final Block SOUL_SAND = register(
		"soul_sand",
		new SoulSandBlock(
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
		)
	);
	public static final Block SOUL_SOIL = register(
		"soul_soil", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.5F).sound(SoundType.SOUL_SOIL))
	);
	public static final Block BASALT = register(
		"basalt",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
				.sound(SoundType.BASALT)
		)
	);
	public static final Block POLISHED_BASALT = register(
		"polished_basalt",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
				.sound(SoundType.BASALT)
		)
	);
	public static final Block SOUL_TORCH = register(
		"soul_torch",
		new TorchBlock(
			ParticleTypes.SOUL_FIRE_FLAME,
			BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(blockStatex -> 10).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SOUL_WALL_TORCH = register(
		"soul_wall_torch",
		new WallTorchBlock(
			ParticleTypes.SOUL_FIRE_FLAME,
			BlockBehaviour.Properties.of()
				.noCollission()
				.instabreak()
				.lightLevel(blockStatex -> 10)
				.sound(SoundType.WOOD)
				.dropsLike(SOUL_TORCH)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block GLOWSTONE = register(
		"glowstone",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.SAND)
				.instrument(NoteBlockInstrument.PLING)
				.strength(0.3F)
				.sound(SoundType.GLASS)
				.lightLevel(blockStatex -> 15)
				.isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block NETHER_PORTAL = register(
		"nether_portal",
		new NetherPortalBlock(
			BlockBehaviour.Properties.of()
				.noCollission()
				.randomTicks()
				.strength(-1.0F)
				.sound(SoundType.GLASS)
				.lightLevel(blockStatex -> 11)
				.pushReaction(PushReaction.BLOCK)
		)
	);
	public static final Block CARVED_PUMPKIN = register(
		"carved_pumpkin",
		new EquipableCarvedPumpkinBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.isValidSpawn(Blocks::always)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block JACK_O_LANTERN = register(
		"jack_o_lantern",
		new CarvedPumpkinBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.lightLevel(blockStatex -> 15)
				.isValidSpawn(Blocks::always)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CAKE = register(
		"cake", new CakeBlock(BlockBehaviour.Properties.of().forceSolidOn().strength(0.5F).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY))
	);
	public static final Block REPEATER = register(
		"repeater", new RepeaterBlock(BlockBehaviour.Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY))
	);
	public static final Block WHITE_STAINED_GLASS = register("white_stained_glass", stainedGlass(DyeColor.WHITE));
	public static final Block ORANGE_STAINED_GLASS = register("orange_stained_glass", stainedGlass(DyeColor.ORANGE));
	public static final Block MAGENTA_STAINED_GLASS = register("magenta_stained_glass", stainedGlass(DyeColor.MAGENTA));
	public static final Block LIGHT_BLUE_STAINED_GLASS = register("light_blue_stained_glass", stainedGlass(DyeColor.LIGHT_BLUE));
	public static final Block YELLOW_STAINED_GLASS = register("yellow_stained_glass", stainedGlass(DyeColor.YELLOW));
	public static final Block LIME_STAINED_GLASS = register("lime_stained_glass", stainedGlass(DyeColor.LIME));
	public static final Block PINK_STAINED_GLASS = register("pink_stained_glass", stainedGlass(DyeColor.PINK));
	public static final Block GRAY_STAINED_GLASS = register("gray_stained_glass", stainedGlass(DyeColor.GRAY));
	public static final Block LIGHT_GRAY_STAINED_GLASS = register("light_gray_stained_glass", stainedGlass(DyeColor.LIGHT_GRAY));
	public static final Block CYAN_STAINED_GLASS = register("cyan_stained_glass", stainedGlass(DyeColor.CYAN));
	public static final Block PURPLE_STAINED_GLASS = register("purple_stained_glass", stainedGlass(DyeColor.PURPLE));
	public static final Block BLUE_STAINED_GLASS = register("blue_stained_glass", stainedGlass(DyeColor.BLUE));
	public static final Block BROWN_STAINED_GLASS = register("brown_stained_glass", stainedGlass(DyeColor.BROWN));
	public static final Block GREEN_STAINED_GLASS = register("green_stained_glass", stainedGlass(DyeColor.GREEN));
	public static final Block RED_STAINED_GLASS = register("red_stained_glass", stainedGlass(DyeColor.RED));
	public static final Block BLACK_STAINED_GLASS = register("black_stained_glass", stainedGlass(DyeColor.BLACK));
	public static final Block OAK_TRAPDOOR = register(
		"oak_trapdoor",
		new TrapDoorBlock(
			BlockSetType.OAK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.ignitedByLava()
		)
	);
	public static final Block SPRUCE_TRAPDOOR = register(
		"spruce_trapdoor",
		new TrapDoorBlock(
			BlockSetType.SPRUCE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PODZOL)
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.ignitedByLava()
		)
	);
	public static final Block BIRCH_TRAPDOOR = register(
		"birch_trapdoor",
		new TrapDoorBlock(
			BlockSetType.BIRCH,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.SAND)
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.ignitedByLava()
		)
	);
	public static final Block JUNGLE_TRAPDOOR = register(
		"jungle_trapdoor",
		new TrapDoorBlock(
			BlockSetType.JUNGLE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.DIRT)
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.ignitedByLava()
		)
	);
	public static final Block ACACIA_TRAPDOOR = register(
		"acacia_trapdoor",
		new TrapDoorBlock(
			BlockSetType.ACACIA,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.ignitedByLava()
		)
	);
	public static final Block CHERRY_TRAPDOOR = register(
		"cherry_trapdoor",
		new TrapDoorBlock(
			BlockSetType.CHERRY,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_WHITE)
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.ignitedByLava()
		)
	);
	public static final Block DARK_OAK_TRAPDOOR = register(
		"dark_oak_trapdoor",
		new TrapDoorBlock(
			BlockSetType.DARK_OAK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BROWN)
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.ignitedByLava()
		)
	);
	public static final Block MANGROVE_TRAPDOOR = register(
		"mangrove_trapdoor",
		new TrapDoorBlock(
			BlockSetType.MANGROVE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.ignitedByLava()
		)
	);
	public static final Block BAMBOO_TRAPDOOR = register(
		"bamboo_trapdoor",
		new TrapDoorBlock(
			BlockSetType.BAMBOO,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.ignitedByLava()
		)
	);
	public static final Block STONE_BRICKS = register(
		"stone_bricks",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F))
	);
	public static final Block MOSSY_STONE_BRICKS = register(
		"mossy_stone_bricks",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F))
	);
	public static final Block CRACKED_STONE_BRICKS = register(
		"cracked_stone_bricks",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F))
	);
	public static final Block CHISELED_STONE_BRICKS = register(
		"chiseled_stone_bricks",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F))
	);
	public static final Block PACKED_MUD = register(
		"packed_mud", new Block(BlockBehaviour.Properties.ofLegacyCopy(DIRT).strength(1.0F, 3.0F).sound(SoundType.PACKED_MUD))
	);
	public static final Block MUD_BRICKS = register(
		"mud_bricks",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 3.0F)
				.sound(SoundType.MUD_BRICKS)
		)
	);
	public static final Block INFESTED_STONE = register("infested_stone", new InfestedBlock(STONE, BlockBehaviour.Properties.of().mapColor(MapColor.CLAY)));
	public static final Block INFESTED_COBBLESTONE = register(
		"infested_cobblestone", new InfestedBlock(COBBLESTONE, BlockBehaviour.Properties.of().mapColor(MapColor.CLAY))
	);
	public static final Block INFESTED_STONE_BRICKS = register(
		"infested_stone_bricks", new InfestedBlock(STONE_BRICKS, BlockBehaviour.Properties.of().mapColor(MapColor.CLAY))
	);
	public static final Block INFESTED_MOSSY_STONE_BRICKS = register(
		"infested_mossy_stone_bricks", new InfestedBlock(MOSSY_STONE_BRICKS, BlockBehaviour.Properties.of().mapColor(MapColor.CLAY))
	);
	public static final Block INFESTED_CRACKED_STONE_BRICKS = register(
		"infested_cracked_stone_bricks", new InfestedBlock(CRACKED_STONE_BRICKS, BlockBehaviour.Properties.of().mapColor(MapColor.CLAY))
	);
	public static final Block INFESTED_CHISELED_STONE_BRICKS = register(
		"infested_chiseled_stone_bricks", new InfestedBlock(CHISELED_STONE_BRICKS, BlockBehaviour.Properties.of().mapColor(MapColor.CLAY))
	);
	public static final Block BROWN_MUSHROOM_BLOCK = register(
		"brown_mushroom_block",
		new HugeMushroomBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block RED_MUSHROOM_BLOCK = register(
		"red_mushroom_block",
		new HugeMushroomBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block MUSHROOM_STEM = register(
		"mushroom_stem",
		new HugeMushroomBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block IRON_BARS = register(
		"iron_bars", new IronBarsBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL).noOcclusion())
	);
	public static final Block CHAIN = register(
		"chain",
		new ChainBlock(BlockBehaviour.Properties.of().forceSolidOn().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.CHAIN).noOcclusion())
	);
	public static final Block GLASS_PANE = register(
		"glass_pane", new IronBarsBlock(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion())
	);
	public static final Block PUMPKIN = register(
		net.minecraft.references.Blocks.PUMPKIN,
		new PumpkinBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.instrument(NoteBlockInstrument.DIDGERIDOO)
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block MELON = register(
		net.minecraft.references.Blocks.MELON,
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY))
	);
	public static final Block ATTACHED_PUMPKIN_STEM = register(
		net.minecraft.references.Blocks.ATTACHED_PUMPKIN_STEM,
		new AttachedStemBlock(
			net.minecraft.references.Blocks.PUMPKIN_STEM,
			net.minecraft.references.Blocks.PUMPKIN,
			Items.PUMPKIN_SEEDS,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ATTACHED_MELON_STEM = register(
		net.minecraft.references.Blocks.ATTACHED_MELON_STEM,
		new AttachedStemBlock(
			net.minecraft.references.Blocks.MELON_STEM,
			net.minecraft.references.Blocks.MELON,
			Items.MELON_SEEDS,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block PUMPKIN_STEM = register(
		net.minecraft.references.Blocks.PUMPKIN_STEM,
		new StemBlock(
			net.minecraft.references.Blocks.PUMPKIN,
			net.minecraft.references.Blocks.ATTACHED_PUMPKIN_STEM,
			Items.PUMPKIN_SEEDS,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.randomTicks()
				.instabreak()
				.sound(SoundType.HARD_CROP)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block MELON_STEM = register(
		net.minecraft.references.Blocks.MELON_STEM,
		new StemBlock(
			net.minecraft.references.Blocks.MELON,
			net.minecraft.references.Blocks.ATTACHED_MELON_STEM,
			Items.MELON_SEEDS,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.randomTicks()
				.instabreak()
				.sound(SoundType.HARD_CROP)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block VINE = register(
		"vine",
		new VineBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.replaceable()
				.noCollission()
				.randomTicks()
				.strength(0.2F)
				.sound(SoundType.VINE)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block GLOW_LICHEN = register(
		"glow_lichen",
		new GlowLichenBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.GLOW_LICHEN)
				.replaceable()
				.noCollission()
				.strength(0.2F)
				.sound(SoundType.GLOW_LICHEN)
				.lightLevel(GlowLichenBlock.emission(7))
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block OAK_FENCE_GATE = register(
		"oak_fence_gate",
		new FenceGateBlock(
			WoodType.OAK,
			BlockBehaviour.Properties.of()
				.mapColor(OAK_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
		)
	);
	public static final Block BRICK_STAIRS = register("brick_stairs", legacyStair(BRICKS));
	public static final Block STONE_BRICK_STAIRS = register("stone_brick_stairs", legacyStair(STONE_BRICKS));
	public static final Block MUD_BRICK_STAIRS = register("mud_brick_stairs", legacyStair(MUD_BRICKS));
	public static final Block MYCELIUM = register(
		"mycelium", new MyceliumBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).randomTicks().strength(0.6F).sound(SoundType.GRASS))
	);
	public static final Block LILY_PAD = register(
		"lily_pad",
		new WaterlilyBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.LILY_PAD).noOcclusion().pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block NETHER_BRICKS = register(
		"nether_bricks",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(2.0F, 6.0F)
				.sound(SoundType.NETHER_BRICKS)
		)
	);
	public static final Block NETHER_BRICK_FENCE = register(
		"nether_brick_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(2.0F, 6.0F)
				.sound(SoundType.NETHER_BRICKS)
		)
	);
	public static final Block NETHER_BRICK_STAIRS = register("nether_brick_stairs", legacyStair(NETHER_BRICKS));
	public static final Block NETHER_WART = register(
		"nether_wart",
		new NetherWartBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noCollission().randomTicks().sound(SoundType.NETHER_WART).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ENCHANTING_TABLE = register(
		"enchanting_table",
		new EnchantmentTableBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.lightLevel(blockStatex -> 7)
				.strength(5.0F, 1200.0F)
		)
	);
	public static final Block BREWING_STAND = register(
		"brewing_stand",
		new BrewingStandBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(0.5F).lightLevel(blockStatex -> 1).noOcclusion()
		)
	);
	public static final Block CAULDRON = register(
		"cauldron", new CauldronBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.0F).noOcclusion())
	);
	public static final Block WATER_CAULDRON = register(
		"water_cauldron", new LayeredCauldronBlock(Biome.Precipitation.RAIN, CauldronInteraction.WATER, BlockBehaviour.Properties.ofLegacyCopy(CAULDRON))
	);
	public static final Block LAVA_CAULDRON = register(
		"lava_cauldron", new LavaCauldronBlock(BlockBehaviour.Properties.ofLegacyCopy(CAULDRON).lightLevel(blockStatex -> 15))
	);
	public static final Block POWDER_SNOW_CAULDRON = register(
		"powder_snow_cauldron", new LayeredCauldronBlock(Biome.Precipitation.SNOW, CauldronInteraction.POWDER_SNOW, BlockBehaviour.Properties.ofLegacyCopy(CAULDRON))
	);
	public static final Block END_PORTAL = register(
		"end_portal",
		new EndPortalBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.noCollission()
				.lightLevel(blockStatex -> 15)
				.strength(-1.0F, 3600000.0F)
				.noLootTable()
				.pushReaction(PushReaction.BLOCK)
		)
	);
	public static final Block END_PORTAL_FRAME = register(
		"end_portal_frame",
		new EndPortalFrameBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GREEN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.sound(SoundType.GLASS)
				.lightLevel(blockStatex -> 1)
				.strength(-1.0F, 3600000.0F)
				.noLootTable()
		)
	);
	public static final Block END_STONE = register(
		"end_stone",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F))
	);
	public static final Block DRAGON_EGG = register(
		"dragon_egg",
		new DragonEggBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.strength(3.0F, 9.0F)
				.lightLevel(blockStatex -> 1)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block REDSTONE_LAMP = register(
		"redstone_lamp",
		new RedstoneLampBlock(BlockBehaviour.Properties.of().lightLevel(litBlockEmission(15)).strength(0.3F).sound(SoundType.GLASS).isValidSpawn(Blocks::always))
	);
	public static final Block COCOA = register(
		"cocoa",
		new CocoaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.randomTicks()
				.strength(0.2F, 3.0F)
				.sound(SoundType.WOOD)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SANDSTONE_STAIRS = register("sandstone_stairs", legacyStair(SANDSTONE));
	public static final Block EMERALD_ORE = register(
		"emerald_ore",
		new DropExperienceBlock(
			UniformInt.of(3, 7),
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)
		)
	);
	public static final Block DEEPSLATE_EMERALD_ORE = register(
		"deepslate_emerald_ore",
		new DropExperienceBlock(
			UniformInt.of(3, 7), BlockBehaviour.Properties.ofLegacyCopy(EMERALD_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
		)
	);
	public static final Block ENDER_CHEST = register(
		"ender_chest",
		new EnderChestBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(22.5F, 600.0F)
				.lightLevel(blockStatex -> 7)
		)
	);
	public static final Block TRIPWIRE_HOOK = register(
		"tripwire_hook", new TripWireHookBlock(BlockBehaviour.Properties.of().noCollission().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY))
	);
	public static final Block TRIPWIRE = register(
		"tripwire", new TripWireBlock(TRIPWIRE_HOOK, BlockBehaviour.Properties.of().noCollission().pushReaction(PushReaction.DESTROY))
	);
	public static final Block EMERALD_BLOCK = register(
		"emerald_block",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.EMERALD)
				.instrument(NoteBlockInstrument.BIT)
				.requiresCorrectToolForDrops()
				.strength(5.0F, 6.0F)
				.sound(SoundType.METAL)
		)
	);
	public static final Block SPRUCE_STAIRS = register("spruce_stairs", legacyStair(SPRUCE_PLANKS));
	public static final Block BIRCH_STAIRS = register("birch_stairs", legacyStair(BIRCH_PLANKS));
	public static final Block JUNGLE_STAIRS = register("jungle_stairs", legacyStair(JUNGLE_PLANKS));
	public static final Block COMMAND_BLOCK = register(
		"command_block",
		new CommandBlock(false, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable())
	);
	public static final Block BEACON = register(
		"beacon",
		new BeaconBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.DIAMOND)
				.instrument(NoteBlockInstrument.HAT)
				.strength(3.0F)
				.lightLevel(blockStatex -> 15)
				.noOcclusion()
				.isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block COBBLESTONE_WALL = register("cobblestone_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(COBBLESTONE).forceSolidOn()));
	public static final Block MOSSY_COBBLESTONE_WALL = register(
		"mossy_cobblestone_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(COBBLESTONE).forceSolidOn())
	);
	public static final Block FLOWER_POT = register("flower_pot", flowerPot(AIR));
	public static final Block POTTED_TORCHFLOWER = register("potted_torchflower", flowerPot(TORCHFLOWER));
	public static final Block POTTED_OAK_SAPLING = register("potted_oak_sapling", flowerPot(OAK_SAPLING));
	public static final Block POTTED_SPRUCE_SAPLING = register("potted_spruce_sapling", flowerPot(SPRUCE_SAPLING));
	public static final Block POTTED_BIRCH_SAPLING = register("potted_birch_sapling", flowerPot(BIRCH_SAPLING));
	public static final Block POTTED_JUNGLE_SAPLING = register("potted_jungle_sapling", flowerPot(JUNGLE_SAPLING));
	public static final Block POTTED_ACACIA_SAPLING = register("potted_acacia_sapling", flowerPot(ACACIA_SAPLING));
	public static final Block POTTED_CHERRY_SAPLING = register("potted_cherry_sapling", flowerPot(CHERRY_SAPLING));
	public static final Block POTTED_DARK_OAK_SAPLING = register("potted_dark_oak_sapling", flowerPot(DARK_OAK_SAPLING));
	public static final Block POTTED_MANGROVE_PROPAGULE = register("potted_mangrove_propagule", flowerPot(MANGROVE_PROPAGULE));
	public static final Block POTTED_FERN = register("potted_fern", flowerPot(FERN));
	public static final Block POTTED_DANDELION = register("potted_dandelion", flowerPot(DANDELION));
	public static final Block POTTED_POPPY = register("potted_poppy", flowerPot(POPPY));
	public static final Block POTTED_BLUE_ORCHID = register("potted_blue_orchid", flowerPot(BLUE_ORCHID));
	public static final Block POTTED_ALLIUM = register("potted_allium", flowerPot(ALLIUM));
	public static final Block POTTED_AZURE_BLUET = register("potted_azure_bluet", flowerPot(AZURE_BLUET));
	public static final Block POTTED_RED_TULIP = register("potted_red_tulip", flowerPot(RED_TULIP));
	public static final Block POTTED_ORANGE_TULIP = register("potted_orange_tulip", flowerPot(ORANGE_TULIP));
	public static final Block POTTED_WHITE_TULIP = register("potted_white_tulip", flowerPot(WHITE_TULIP));
	public static final Block POTTED_PINK_TULIP = register("potted_pink_tulip", flowerPot(PINK_TULIP));
	public static final Block POTTED_OXEYE_DAISY = register("potted_oxeye_daisy", flowerPot(OXEYE_DAISY));
	public static final Block POTTED_CORNFLOWER = register("potted_cornflower", flowerPot(CORNFLOWER));
	public static final Block POTTED_LILY_OF_THE_VALLEY = register("potted_lily_of_the_valley", flowerPot(LILY_OF_THE_VALLEY));
	public static final Block POTTED_WITHER_ROSE = register("potted_wither_rose", flowerPot(WITHER_ROSE));
	public static final Block POTTED_RED_MUSHROOM = register("potted_red_mushroom", flowerPot(RED_MUSHROOM));
	public static final Block POTTED_BROWN_MUSHROOM = register("potted_brown_mushroom", flowerPot(BROWN_MUSHROOM));
	public static final Block POTTED_DEAD_BUSH = register("potted_dead_bush", flowerPot(DEAD_BUSH));
	public static final Block POTTED_CACTUS = register("potted_cactus", flowerPot(CACTUS));
	public static final Block CARROTS = register(
		"carrots",
		new CarrotBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block POTATOES = register(
		"potatoes",
		new PotatoBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block OAK_BUTTON = register("oak_button", woodenButton(BlockSetType.OAK));
	public static final Block SPRUCE_BUTTON = register("spruce_button", woodenButton(BlockSetType.SPRUCE));
	public static final Block BIRCH_BUTTON = register("birch_button", woodenButton(BlockSetType.BIRCH));
	public static final Block JUNGLE_BUTTON = register("jungle_button", woodenButton(BlockSetType.JUNGLE));
	public static final Block ACACIA_BUTTON = register("acacia_button", woodenButton(BlockSetType.ACACIA));
	public static final Block CHERRY_BUTTON = register("cherry_button", woodenButton(BlockSetType.CHERRY));
	public static final Block DARK_OAK_BUTTON = register("dark_oak_button", woodenButton(BlockSetType.DARK_OAK));
	public static final Block MANGROVE_BUTTON = register("mangrove_button", woodenButton(BlockSetType.MANGROVE));
	public static final Block BAMBOO_BUTTON = register("bamboo_button", woodenButton(BlockSetType.BAMBOO));
	public static final Block SKELETON_SKULL = register(
		"skeleton_skull",
		new SkullBlock(
			SkullBlock.Types.SKELETON, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.SKELETON).strength(1.0F).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SKELETON_WALL_SKULL = register(
		"skeleton_wall_skull",
		new WallSkullBlock(SkullBlock.Types.SKELETON, BlockBehaviour.Properties.of().strength(1.0F).dropsLike(SKELETON_SKULL).pushReaction(PushReaction.DESTROY))
	);
	public static final Block WITHER_SKELETON_SKULL = register(
		"wither_skeleton_skull",
		new WitherSkullBlock(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.WITHER_SKELETON).strength(1.0F).pushReaction(PushReaction.DESTROY))
	);
	public static final Block WITHER_SKELETON_WALL_SKULL = register(
		"wither_skeleton_wall_skull",
		new WitherWallSkullBlock(BlockBehaviour.Properties.of().strength(1.0F).dropsLike(WITHER_SKELETON_SKULL).pushReaction(PushReaction.DESTROY))
	);
	public static final Block ZOMBIE_HEAD = register(
		"zombie_head",
		new SkullBlock(
			SkullBlock.Types.ZOMBIE, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.ZOMBIE).strength(1.0F).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ZOMBIE_WALL_HEAD = register(
		"zombie_wall_head",
		new WallSkullBlock(SkullBlock.Types.ZOMBIE, BlockBehaviour.Properties.of().strength(1.0F).dropsLike(ZOMBIE_HEAD).pushReaction(PushReaction.DESTROY))
	);
	public static final Block PLAYER_HEAD = register(
		"player_head",
		new PlayerHeadBlock(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.CUSTOM_HEAD).strength(1.0F).pushReaction(PushReaction.DESTROY))
	);
	public static final Block PLAYER_WALL_HEAD = register(
		"player_wall_head", new PlayerWallHeadBlock(BlockBehaviour.Properties.of().strength(1.0F).dropsLike(PLAYER_HEAD).pushReaction(PushReaction.DESTROY))
	);
	public static final Block CREEPER_HEAD = register(
		"creeper_head",
		new SkullBlock(
			SkullBlock.Types.CREEPER, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.CREEPER).strength(1.0F).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CREEPER_WALL_HEAD = register(
		"creeper_wall_head",
		new WallSkullBlock(SkullBlock.Types.CREEPER, BlockBehaviour.Properties.of().strength(1.0F).dropsLike(CREEPER_HEAD).pushReaction(PushReaction.DESTROY))
	);
	public static final Block DRAGON_HEAD = register(
		"dragon_head",
		new SkullBlock(
			SkullBlock.Types.DRAGON, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.DRAGON).strength(1.0F).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block DRAGON_WALL_HEAD = register(
		"dragon_wall_head",
		new WallSkullBlock(SkullBlock.Types.DRAGON, BlockBehaviour.Properties.of().strength(1.0F).dropsLike(DRAGON_HEAD).pushReaction(PushReaction.DESTROY))
	);
	public static final Block PIGLIN_HEAD = register(
		"piglin_head",
		new SkullBlock(
			SkullBlock.Types.PIGLIN, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.PIGLIN).strength(1.0F).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block PIGLIN_WALL_HEAD = register(
		"piglin_wall_head", new PiglinWallSkullBlock(BlockBehaviour.Properties.of().strength(1.0F).dropsLike(PIGLIN_HEAD).pushReaction(PushReaction.DESTROY))
	);
	public static final Block ANVIL = register(
		"anvil",
		new AnvilBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.requiresCorrectToolForDrops()
				.strength(5.0F, 1200.0F)
				.sound(SoundType.ANVIL)
				.pushReaction(PushReaction.BLOCK)
		)
	);
	public static final Block CHIPPED_ANVIL = register(
		"chipped_anvil",
		new AnvilBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.requiresCorrectToolForDrops()
				.strength(5.0F, 1200.0F)
				.sound(SoundType.ANVIL)
				.pushReaction(PushReaction.BLOCK)
		)
	);
	public static final Block DAMAGED_ANVIL = register(
		"damaged_anvil",
		new AnvilBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.requiresCorrectToolForDrops()
				.strength(5.0F, 1200.0F)
				.sound(SoundType.ANVIL)
				.pushReaction(PushReaction.BLOCK)
		)
	);
	public static final Block TRAPPED_CHEST = register(
		"trapped_chest",
		new TrappedChestBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block LIGHT_WEIGHTED_PRESSURE_PLATE = register(
		"light_weighted_pressure_plate",
		new WeightedPressurePlateBlock(
			15,
			BlockSetType.GOLD,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.GOLD)
				.forceSolidOn()
				.requiresCorrectToolForDrops()
				.noCollission()
				.strength(0.5F)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block HEAVY_WEIGHTED_PRESSURE_PLATE = register(
		"heavy_weighted_pressure_plate",
		new WeightedPressurePlateBlock(
			150,
			BlockSetType.IRON,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.forceSolidOn()
				.requiresCorrectToolForDrops()
				.noCollission()
				.strength(0.5F)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block COMPARATOR = register(
		"comparator", new ComparatorBlock(BlockBehaviour.Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY))
	);
	public static final Block DAYLIGHT_DETECTOR = register(
		"daylight_detector",
		new DaylightDetectorBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(0.2F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block REDSTONE_BLOCK = register(
		"redstone_block",
		new PoweredBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.FIRE)
				.requiresCorrectToolForDrops()
				.strength(5.0F, 6.0F)
				.sound(SoundType.METAL)
				.isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block NETHER_QUARTZ_ORE = register(
		"nether_quartz_ore",
		new DropExperienceBlock(
			UniformInt.of(2, 5),
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(3.0F, 3.0F)
				.sound(SoundType.NETHER_ORE)
		)
	);
	public static final Block HOPPER = register(
		"hopper",
		new HopperBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 4.8F).sound(SoundType.METAL).noOcclusion()
		)
	);
	public static final Block QUARTZ_BLOCK = register(
		"quartz_block",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F))
	);
	public static final Block CHISELED_QUARTZ_BLOCK = register(
		"chiseled_quartz_block",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F))
	);
	public static final Block QUARTZ_PILLAR = register(
		"quartz_pillar",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
		)
	);
	public static final Block QUARTZ_STAIRS = register("quartz_stairs", legacyStair(QUARTZ_BLOCK));
	public static final Block ACTIVATOR_RAIL = register(
		"activator_rail", new PoweredRailBlock(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL))
	);
	public static final Block DROPPER = register(
		"dropper",
		new DropperBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)
		)
	);
	public static final Block WHITE_TERRACOTTA = register(
		"white_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_WHITE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block ORANGE_TERRACOTTA = register(
		"orange_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_ORANGE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block MAGENTA_TERRACOTTA = register(
		"magenta_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_MAGENTA)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block LIGHT_BLUE_TERRACOTTA = register(
		"light_blue_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block YELLOW_TERRACOTTA = register(
		"yellow_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_YELLOW)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block LIME_TERRACOTTA = register(
		"lime_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_LIGHT_GREEN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block PINK_TERRACOTTA = register(
		"pink_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_PINK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block GRAY_TERRACOTTA = register(
		"gray_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_GRAY)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block LIGHT_GRAY_TERRACOTTA = register(
		"light_gray_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block CYAN_TERRACOTTA = register(
		"cyan_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_CYAN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block PURPLE_TERRACOTTA = register(
		"purple_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_PURPLE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block BLUE_TERRACOTTA = register(
		"blue_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_BLUE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block BROWN_TERRACOTTA = register(
		"brown_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_BROWN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block GREEN_TERRACOTTA = register(
		"green_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_GREEN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block RED_TERRACOTTA = register(
		"red_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_RED)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block BLACK_TERRACOTTA = register(
		"black_terracotta",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_BLACK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.25F, 4.2F)
		)
	);
	public static final Block WHITE_STAINED_GLASS_PANE = register(
		"white_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.WHITE, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block ORANGE_STAINED_GLASS_PANE = register(
		"orange_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.ORANGE, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block MAGENTA_STAINED_GLASS_PANE = register(
		"magenta_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.MAGENTA, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block LIGHT_BLUE_STAINED_GLASS_PANE = register(
		"light_blue_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block YELLOW_STAINED_GLASS_PANE = register(
		"yellow_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.YELLOW, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block LIME_STAINED_GLASS_PANE = register(
		"lime_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.LIME, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block PINK_STAINED_GLASS_PANE = register(
		"pink_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.PINK, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block GRAY_STAINED_GLASS_PANE = register(
		"gray_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.GRAY, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block LIGHT_GRAY_STAINED_GLASS_PANE = register(
		"light_gray_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.LIGHT_GRAY, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block CYAN_STAINED_GLASS_PANE = register(
		"cyan_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.CYAN, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block PURPLE_STAINED_GLASS_PANE = register(
		"purple_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.PURPLE, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block BLUE_STAINED_GLASS_PANE = register(
		"blue_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.BLUE, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block BROWN_STAINED_GLASS_PANE = register(
		"brown_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.BROWN, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block GREEN_STAINED_GLASS_PANE = register(
		"green_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.GREEN, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block RED_STAINED_GLASS_PANE = register(
		"red_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.RED, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block BLACK_STAINED_GLASS_PANE = register(
		"black_stained_glass_pane",
		new StainedGlassPaneBlock(
			DyeColor.BLACK, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
		)
	);
	public static final Block ACACIA_STAIRS = register("acacia_stairs", legacyStair(ACACIA_PLANKS));
	public static final Block CHERRY_STAIRS = register("cherry_stairs", legacyStair(CHERRY_PLANKS));
	public static final Block DARK_OAK_STAIRS = register("dark_oak_stairs", legacyStair(DARK_OAK_PLANKS));
	public static final Block MANGROVE_STAIRS = register("mangrove_stairs", legacyStair(MANGROVE_PLANKS));
	public static final Block BAMBOO_STAIRS = register("bamboo_stairs", legacyStair(BAMBOO_PLANKS));
	public static final Block BAMBOO_MOSAIC_STAIRS = register("bamboo_mosaic_stairs", legacyStair(BAMBOO_MOSAIC));
	public static final Block SLIME_BLOCK = register(
		"slime_block", new SlimeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).friction(0.8F).sound(SoundType.SLIME_BLOCK).noOcclusion())
	);
	public static final Block BARRIER = register(
		"barrier",
		new BarrierBlock(
			BlockBehaviour.Properties.of()
				.strength(-1.0F, 3600000.8F)
				.noLootTable()
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.noTerrainParticles()
				.pushReaction(PushReaction.BLOCK)
		)
	);
	public static final Block LIGHT = register(
		"light",
		new LightBlock(BlockBehaviour.Properties.of().replaceable().strength(-1.0F, 3600000.8F).noLootTable().noOcclusion().lightLevel(LightBlock.LIGHT_EMISSION))
	);
	public static final Block IRON_TRAPDOOR = register(
		"iron_trapdoor",
		new TrapDoorBlock(
			BlockSetType.IRON,
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F).noOcclusion().isValidSpawn(Blocks::never)
		)
	);
	public static final Block PRISMARINE = register(
		"prismarine",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block PRISMARINE_BRICKS = register(
		"prismarine_bricks",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block DARK_PRISMARINE = register(
		"dark_prismarine",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block PRISMARINE_STAIRS = register("prismarine_stairs", legacyStair(PRISMARINE));
	public static final Block PRISMARINE_BRICK_STAIRS = register("prismarine_brick_stairs", legacyStair(PRISMARINE_BRICKS));
	public static final Block DARK_PRISMARINE_STAIRS = register("dark_prismarine_stairs", legacyStair(DARK_PRISMARINE));
	public static final Block PRISMARINE_SLAB = register(
		"prismarine_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block PRISMARINE_BRICK_SLAB = register(
		"prismarine_brick_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block DARK_PRISMARINE_SLAB = register(
		"dark_prismarine_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block SEA_LANTERN = register(
		"sea_lantern",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.QUARTZ)
				.instrument(NoteBlockInstrument.HAT)
				.strength(0.3F)
				.sound(SoundType.GLASS)
				.lightLevel(blockStatex -> 15)
				.isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block HAY_BLOCK = register(
		"hay_block",
		new HayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).instrument(NoteBlockInstrument.BANJO).strength(0.5F).sound(SoundType.GRASS))
	);
	public static final Block WHITE_CARPET = register(
		"white_carpet",
		new WoolCarpetBlock(DyeColor.WHITE, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block ORANGE_CARPET = register(
		"orange_carpet",
		new WoolCarpetBlock(DyeColor.ORANGE, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block MAGENTA_CARPET = register(
		"magenta_carpet",
		new WoolCarpetBlock(DyeColor.MAGENTA, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block LIGHT_BLUE_CARPET = register(
		"light_blue_carpet",
		new WoolCarpetBlock(
			DyeColor.LIGHT_BLUE, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block YELLOW_CARPET = register(
		"yellow_carpet",
		new WoolCarpetBlock(DyeColor.YELLOW, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block LIME_CARPET = register(
		"lime_carpet",
		new WoolCarpetBlock(DyeColor.LIME, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block PINK_CARPET = register(
		"pink_carpet",
		new WoolCarpetBlock(DyeColor.PINK, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block GRAY_CARPET = register(
		"gray_carpet",
		new WoolCarpetBlock(DyeColor.GRAY, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block LIGHT_GRAY_CARPET = register(
		"light_gray_carpet",
		new WoolCarpetBlock(
			DyeColor.LIGHT_GRAY, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()
		)
	);
	public static final Block CYAN_CARPET = register(
		"cyan_carpet",
		new WoolCarpetBlock(DyeColor.CYAN, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block PURPLE_CARPET = register(
		"purple_carpet",
		new WoolCarpetBlock(DyeColor.PURPLE, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block BLUE_CARPET = register(
		"blue_carpet",
		new WoolCarpetBlock(DyeColor.BLUE, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block BROWN_CARPET = register(
		"brown_carpet",
		new WoolCarpetBlock(DyeColor.BROWN, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block GREEN_CARPET = register(
		"green_carpet",
		new WoolCarpetBlock(DyeColor.GREEN, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block RED_CARPET = register(
		"red_carpet",
		new WoolCarpetBlock(DyeColor.RED, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block BLACK_CARPET = register(
		"black_carpet",
		new WoolCarpetBlock(DyeColor.BLACK, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.1F).sound(SoundType.WOOL).ignitedByLava())
	);
	public static final Block TERRACOTTA = register(
		"terracotta",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)
		)
	);
	public static final Block COAL_BLOCK = register(
		"coal_block",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)
		)
	);
	public static final Block PACKED_ICE = register(
		"packed_ice",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.ICE).instrument(NoteBlockInstrument.CHIME).friction(0.98F).strength(0.5F).sound(SoundType.GLASS))
	);
	public static final Block SUNFLOWER = register(
		"sunflower",
		new TallFlowerBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block LILAC = register(
		"lilac",
		new TallFlowerBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ROSE_BUSH = register(
		"rose_bush",
		new TallFlowerBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block PEONY = register(
		"peony",
		new TallFlowerBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block TALL_GRASS = register(
		"tall_grass",
		new DoublePlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block LARGE_FERN = register(
		"large_fern",
		new DoublePlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.GRASS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block WHITE_BANNER = register(
		"white_banner",
		new BannerBlock(
			DyeColor.WHITE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block ORANGE_BANNER = register(
		"orange_banner",
		new BannerBlock(
			DyeColor.ORANGE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block MAGENTA_BANNER = register(
		"magenta_banner",
		new BannerBlock(
			DyeColor.MAGENTA,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block LIGHT_BLUE_BANNER = register(
		"light_blue_banner",
		new BannerBlock(
			DyeColor.LIGHT_BLUE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block YELLOW_BANNER = register(
		"yellow_banner",
		new BannerBlock(
			DyeColor.YELLOW,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block LIME_BANNER = register(
		"lime_banner",
		new BannerBlock(
			DyeColor.LIME,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block PINK_BANNER = register(
		"pink_banner",
		new BannerBlock(
			DyeColor.PINK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block GRAY_BANNER = register(
		"gray_banner",
		new BannerBlock(
			DyeColor.GRAY,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block LIGHT_GRAY_BANNER = register(
		"light_gray_banner",
		new BannerBlock(
			DyeColor.LIGHT_GRAY,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block CYAN_BANNER = register(
		"cyan_banner",
		new BannerBlock(
			DyeColor.CYAN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block PURPLE_BANNER = register(
		"purple_banner",
		new BannerBlock(
			DyeColor.PURPLE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block BLUE_BANNER = register(
		"blue_banner",
		new BannerBlock(
			DyeColor.BLUE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block BROWN_BANNER = register(
		"brown_banner",
		new BannerBlock(
			DyeColor.BROWN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block GREEN_BANNER = register(
		"green_banner",
		new BannerBlock(
			DyeColor.GREEN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block RED_BANNER = register(
		"red_banner",
		new BannerBlock(
			DyeColor.RED,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block BLACK_BANNER = register(
		"black_banner",
		new BannerBlock(
			DyeColor.BLACK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block WHITE_WALL_BANNER = register(
		"white_wall_banner",
		new WallBannerBlock(
			DyeColor.WHITE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(WHITE_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block ORANGE_WALL_BANNER = register(
		"orange_wall_banner",
		new WallBannerBlock(
			DyeColor.ORANGE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(ORANGE_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block MAGENTA_WALL_BANNER = register(
		"magenta_wall_banner",
		new WallBannerBlock(
			DyeColor.MAGENTA,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(MAGENTA_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block LIGHT_BLUE_WALL_BANNER = register(
		"light_blue_wall_banner",
		new WallBannerBlock(
			DyeColor.LIGHT_BLUE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(LIGHT_BLUE_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block YELLOW_WALL_BANNER = register(
		"yellow_wall_banner",
		new WallBannerBlock(
			DyeColor.YELLOW,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(YELLOW_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block LIME_WALL_BANNER = register(
		"lime_wall_banner",
		new WallBannerBlock(
			DyeColor.LIME,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(LIME_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block PINK_WALL_BANNER = register(
		"pink_wall_banner",
		new WallBannerBlock(
			DyeColor.PINK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(PINK_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block GRAY_WALL_BANNER = register(
		"gray_wall_banner",
		new WallBannerBlock(
			DyeColor.GRAY,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(GRAY_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block LIGHT_GRAY_WALL_BANNER = register(
		"light_gray_wall_banner",
		new WallBannerBlock(
			DyeColor.LIGHT_GRAY,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(LIGHT_GRAY_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block CYAN_WALL_BANNER = register(
		"cyan_wall_banner",
		new WallBannerBlock(
			DyeColor.CYAN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(CYAN_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block PURPLE_WALL_BANNER = register(
		"purple_wall_banner",
		new WallBannerBlock(
			DyeColor.PURPLE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(PURPLE_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block BLUE_WALL_BANNER = register(
		"blue_wall_banner",
		new WallBannerBlock(
			DyeColor.BLUE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(BLUE_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block BROWN_WALL_BANNER = register(
		"brown_wall_banner",
		new WallBannerBlock(
			DyeColor.BROWN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(BROWN_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block GREEN_WALL_BANNER = register(
		"green_wall_banner",
		new WallBannerBlock(
			DyeColor.GREEN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(GREEN_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block RED_WALL_BANNER = register(
		"red_wall_banner",
		new WallBannerBlock(
			DyeColor.RED,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(RED_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block BLACK_WALL_BANNER = register(
		"black_wall_banner",
		new WallBannerBlock(
			DyeColor.BLACK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(1.0F)
				.sound(SoundType.WOOD)
				.dropsLike(BLACK_BANNER)
				.ignitedByLava()
		)
	);
	public static final Block RED_SANDSTONE = register(
		"red_sandstone",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
		)
	);
	public static final Block CHISELED_RED_SANDSTONE = register(
		"chiseled_red_sandstone",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
		)
	);
	public static final Block CUT_RED_SANDSTONE = register(
		"cut_red_sandstone",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)
		)
	);
	public static final Block RED_SANDSTONE_STAIRS = register("red_sandstone_stairs", legacyStair(RED_SANDSTONE));
	public static final Block OAK_SLAB = register(
		"oak_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block SPRUCE_SLAB = register(
		"spruce_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block BIRCH_SLAB = register(
		"birch_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block JUNGLE_SLAB = register(
		"jungle_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block ACACIA_SLAB = register(
		"acacia_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block CHERRY_SLAB = register(
		"cherry_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_WHITE)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.CHERRY_WOOD)
				.ignitedByLava()
		)
	);
	public static final Block DARK_OAK_SLAB = register(
		"dark_oak_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BROWN)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		)
	);
	public static final Block MANGROVE_SLAB = register(
		"mangrove_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block BAMBOO_SLAB = register(
		"bamboo_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.BAMBOO_WOOD)
				.ignitedByLava()
		)
	);
	public static final Block BAMBOO_MOSAIC_SLAB = register(
		"bamboo_mosaic_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.BAMBOO_WOOD)
				.ignitedByLava()
		)
	);
	public static final Block STONE_SLAB = register(
		"stone_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block SMOOTH_STONE_SLAB = register(
		"smooth_stone_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block SANDSTONE_SLAB = register(
		"sandstone_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block CUT_SANDSTONE_SLAB = register(
		"cut_sandstone_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block PETRIFIED_OAK_SLAB = register(
		"petrified_oak_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block COBBLESTONE_SLAB = register(
		"cobblestone_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block BRICK_SLAB = register(
		"brick_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block STONE_BRICK_SLAB = register(
		"stone_brick_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block MUD_BRICK_SLAB = register(
		"mud_brick_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 3.0F)
				.sound(SoundType.MUD_BRICKS)
		)
	);
	public static final Block NETHER_BRICK_SLAB = register(
		"nether_brick_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(2.0F, 6.0F)
				.sound(SoundType.NETHER_BRICKS)
		)
	);
	public static final Block QUARTZ_SLAB = register(
		"quartz_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block RED_SANDSTONE_SLAB = register(
		"red_sandstone_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block CUT_RED_SANDSTONE_SLAB = register(
		"cut_red_sandstone_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block PURPUR_SLAB = register(
		"purpur_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block SMOOTH_STONE = register(
		"smooth_stone",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F))
	);
	public static final Block SMOOTH_SANDSTONE = register(
		"smooth_sandstone",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F))
	);
	public static final Block SMOOTH_QUARTZ = register(
		"smooth_quartz",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block SMOOTH_RED_SANDSTONE = register(
		"smooth_red_sandstone",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)
		)
	);
	public static final Block SPRUCE_FENCE_GATE = register(
		"spruce_fence_gate",
		new FenceGateBlock(
			WoodType.SPRUCE,
			BlockBehaviour.Properties.of()
				.mapColor(SPRUCE_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
		)
	);
	public static final Block BIRCH_FENCE_GATE = register(
		"birch_fence_gate",
		new FenceGateBlock(
			WoodType.BIRCH,
			BlockBehaviour.Properties.of()
				.mapColor(BIRCH_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
		)
	);
	public static final Block JUNGLE_FENCE_GATE = register(
		"jungle_fence_gate",
		new FenceGateBlock(
			WoodType.JUNGLE,
			BlockBehaviour.Properties.of()
				.mapColor(JUNGLE_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
		)
	);
	public static final Block ACACIA_FENCE_GATE = register(
		"acacia_fence_gate",
		new FenceGateBlock(
			WoodType.ACACIA,
			BlockBehaviour.Properties.of()
				.mapColor(ACACIA_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
		)
	);
	public static final Block CHERRY_FENCE_GATE = register(
		"cherry_fence_gate",
		new FenceGateBlock(
			WoodType.CHERRY,
			BlockBehaviour.Properties.of()
				.mapColor(CHERRY_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
		)
	);
	public static final Block DARK_OAK_FENCE_GATE = register(
		"dark_oak_fence_gate",
		new FenceGateBlock(
			WoodType.DARK_OAK,
			BlockBehaviour.Properties.of()
				.mapColor(DARK_OAK_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
		)
	);
	public static final Block MANGROVE_FENCE_GATE = register(
		"mangrove_fence_gate",
		new FenceGateBlock(
			WoodType.MANGROVE,
			BlockBehaviour.Properties.of()
				.mapColor(MANGROVE_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
		)
	);
	public static final Block BAMBOO_FENCE_GATE = register(
		"bamboo_fence_gate",
		new FenceGateBlock(
			WoodType.BAMBOO,
			BlockBehaviour.Properties.of()
				.mapColor(BAMBOO_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
		)
	);
	public static final Block SPRUCE_FENCE = register(
		"spruce_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(SPRUCE_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
				.sound(SoundType.WOOD)
		)
	);
	public static final Block BIRCH_FENCE = register(
		"birch_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(BIRCH_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
				.sound(SoundType.WOOD)
		)
	);
	public static final Block JUNGLE_FENCE = register(
		"jungle_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(JUNGLE_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
				.sound(SoundType.WOOD)
		)
	);
	public static final Block ACACIA_FENCE = register(
		"acacia_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(ACACIA_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
				.sound(SoundType.WOOD)
		)
	);
	public static final Block CHERRY_FENCE = register(
		"cherry_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(CHERRY_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
				.sound(SoundType.CHERRY_WOOD)
		)
	);
	public static final Block DARK_OAK_FENCE = register(
		"dark_oak_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DARK_OAK_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
				.sound(SoundType.WOOD)
		)
	);
	public static final Block MANGROVE_FENCE = register(
		"mangrove_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MANGROVE_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.ignitedByLava()
				.sound(SoundType.WOOD)
		)
	);
	public static final Block BAMBOO_FENCE = register(
		"bamboo_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(BAMBOO_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.BAMBOO_WOOD)
				.ignitedByLava()
		)
	);
	public static final Block SPRUCE_DOOR = register(
		"spruce_door",
		new DoorBlock(
			BlockSetType.SPRUCE,
			BlockBehaviour.Properties.of()
				.mapColor(SPRUCE_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BIRCH_DOOR = register(
		"birch_door",
		new DoorBlock(
			BlockSetType.BIRCH,
			BlockBehaviour.Properties.of()
				.mapColor(BIRCH_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block JUNGLE_DOOR = register(
		"jungle_door",
		new DoorBlock(
			BlockSetType.JUNGLE,
			BlockBehaviour.Properties.of()
				.mapColor(JUNGLE_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ACACIA_DOOR = register(
		"acacia_door",
		new DoorBlock(
			BlockSetType.ACACIA,
			BlockBehaviour.Properties.of()
				.mapColor(ACACIA_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CHERRY_DOOR = register(
		"cherry_door",
		new DoorBlock(
			BlockSetType.CHERRY,
			BlockBehaviour.Properties.of()
				.mapColor(CHERRY_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block DARK_OAK_DOOR = register(
		"dark_oak_door",
		new DoorBlock(
			BlockSetType.DARK_OAK,
			BlockBehaviour.Properties.of()
				.mapColor(DARK_OAK_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block MANGROVE_DOOR = register(
		"mangrove_door",
		new DoorBlock(
			BlockSetType.MANGROVE,
			BlockBehaviour.Properties.of()
				.mapColor(MANGROVE_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BAMBOO_DOOR = register(
		"bamboo_door",
		new DoorBlock(
			BlockSetType.BAMBOO,
			BlockBehaviour.Properties.of()
				.mapColor(BAMBOO_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block END_ROD = register(
		"end_rod", new EndRodBlock(BlockBehaviour.Properties.of().forceSolidOff().instabreak().lightLevel(blockStatex -> 14).sound(SoundType.WOOD).noOcclusion())
	);
	public static final Block CHORUS_PLANT = register(
		"chorus_plant",
		new ChorusPlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_PURPLE)
				.forceSolidOff()
				.strength(0.4F)
				.sound(SoundType.WOOD)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CHORUS_FLOWER = register(
		"chorus_flower",
		new ChorusFlowerBlock(
			CHORUS_PLANT,
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
		)
	);
	public static final Block PURPUR_BLOCK = register(
		"purpur_block",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block PURPUR_PILLAR = register(
		"purpur_pillar",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block PURPUR_STAIRS = register("purpur_stairs", legacyStair(PURPUR_BLOCK));
	public static final Block END_STONE_BRICKS = register(
		"end_stone_bricks",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F))
	);
	public static final Block TORCHFLOWER_CROP = register(
		"torchflower_crop",
		new TorchflowerCropBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block PITCHER_CROP = register(
		"pitcher_crop",
		new PitcherCropBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block PITCHER_PLANT = register(
		"pitcher_plant",
		new DoublePlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.CROP)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BEETROOTS = register(
		"beetroots",
		new BeetrootBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block DIRT_PATH = register(
		"dirt_path",
		new DirtPathBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS).isViewBlocking(Blocks::always).isSuffocating(Blocks::always)
		)
	);
	public static final Block END_GATEWAY = register(
		"end_gateway",
		new EndGatewayBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.noCollission()
				.lightLevel(blockStatex -> 15)
				.strength(-1.0F, 3600000.0F)
				.noLootTable()
				.pushReaction(PushReaction.BLOCK)
		)
	);
	public static final Block REPEATING_COMMAND_BLOCK = register(
		"repeating_command_block",
		new CommandBlock(
			false, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()
		)
	);
	public static final Block CHAIN_COMMAND_BLOCK = register(
		"chain_command_block",
		new CommandBlock(true, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable())
	);
	public static final Block FROSTED_ICE = register(
		"frosted_ice",
		new FrostedIceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.ICE)
				.friction(0.98F)
				.randomTicks()
				.strength(0.5F)
				.sound(SoundType.GLASS)
				.noOcclusion()
				.isValidSpawn((blockStatex, blockGetter, blockPos, entityType) -> entityType == EntityType.POLAR_BEAR)
				.isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block MAGMA_BLOCK = register(
		"magma_block",
		new MagmaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.lightLevel(blockStatex -> 3)
				.strength(0.5F)
				.isValidSpawn((blockStatex, blockGetter, blockPos, entityType) -> entityType.fireImmune())
				.hasPostProcess(Blocks::always)
				.emissiveRendering(Blocks::always)
		)
	);
	public static final Block NETHER_WART_BLOCK = register(
		"nether_wart_block", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.WART_BLOCK))
	);
	public static final Block RED_NETHER_BRICKS = register(
		"red_nether_bricks",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(2.0F, 6.0F)
				.sound(SoundType.NETHER_BRICKS)
		)
	);
	public static final Block BONE_BLOCK = register(
		"bone_block",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.SAND)
				.instrument(NoteBlockInstrument.XYLOPHONE)
				.requiresCorrectToolForDrops()
				.strength(2.0F)
				.sound(SoundType.BONE_BLOCK)
		)
	);
	public static final Block STRUCTURE_VOID = register(
		"structure_void",
		new StructureVoidBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().noTerrainParticles().pushReaction(PushReaction.DESTROY))
	);
	public static final Block OBSERVER = register(
		"observer",
		new ObserverBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.strength(3.0F)
				.requiresCorrectToolForDrops()
				.isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block SHULKER_BOX = register("shulker_box", shulkerBox(null, MapColor.COLOR_PURPLE));
	public static final Block WHITE_SHULKER_BOX = register("white_shulker_box", shulkerBox(DyeColor.WHITE, MapColor.SNOW));
	public static final Block ORANGE_SHULKER_BOX = register("orange_shulker_box", shulkerBox(DyeColor.ORANGE, MapColor.COLOR_ORANGE));
	public static final Block MAGENTA_SHULKER_BOX = register("magenta_shulker_box", shulkerBox(DyeColor.MAGENTA, MapColor.COLOR_MAGENTA));
	public static final Block LIGHT_BLUE_SHULKER_BOX = register("light_blue_shulker_box", shulkerBox(DyeColor.LIGHT_BLUE, MapColor.COLOR_LIGHT_BLUE));
	public static final Block YELLOW_SHULKER_BOX = register("yellow_shulker_box", shulkerBox(DyeColor.YELLOW, MapColor.COLOR_YELLOW));
	public static final Block LIME_SHULKER_BOX = register("lime_shulker_box", shulkerBox(DyeColor.LIME, MapColor.COLOR_LIGHT_GREEN));
	public static final Block PINK_SHULKER_BOX = register("pink_shulker_box", shulkerBox(DyeColor.PINK, MapColor.COLOR_PINK));
	public static final Block GRAY_SHULKER_BOX = register("gray_shulker_box", shulkerBox(DyeColor.GRAY, MapColor.COLOR_GRAY));
	public static final Block LIGHT_GRAY_SHULKER_BOX = register("light_gray_shulker_box", shulkerBox(DyeColor.LIGHT_GRAY, MapColor.COLOR_LIGHT_GRAY));
	public static final Block CYAN_SHULKER_BOX = register("cyan_shulker_box", shulkerBox(DyeColor.CYAN, MapColor.COLOR_CYAN));
	public static final Block PURPLE_SHULKER_BOX = register("purple_shulker_box", shulkerBox(DyeColor.PURPLE, MapColor.TERRACOTTA_PURPLE));
	public static final Block BLUE_SHULKER_BOX = register("blue_shulker_box", shulkerBox(DyeColor.BLUE, MapColor.COLOR_BLUE));
	public static final Block BROWN_SHULKER_BOX = register("brown_shulker_box", shulkerBox(DyeColor.BROWN, MapColor.COLOR_BROWN));
	public static final Block GREEN_SHULKER_BOX = register("green_shulker_box", shulkerBox(DyeColor.GREEN, MapColor.COLOR_GREEN));
	public static final Block RED_SHULKER_BOX = register("red_shulker_box", shulkerBox(DyeColor.RED, MapColor.COLOR_RED));
	public static final Block BLACK_SHULKER_BOX = register("black_shulker_box", shulkerBox(DyeColor.BLACK, MapColor.COLOR_BLACK));
	public static final Block WHITE_GLAZED_TERRACOTTA = register(
		"white_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.WHITE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block ORANGE_GLAZED_TERRACOTTA = register(
		"orange_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.ORANGE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block MAGENTA_GLAZED_TERRACOTTA = register(
		"magenta_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.MAGENTA)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block LIGHT_BLUE_GLAZED_TERRACOTTA = register(
		"light_blue_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.LIGHT_BLUE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block YELLOW_GLAZED_TERRACOTTA = register(
		"yellow_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.YELLOW)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block LIME_GLAZED_TERRACOTTA = register(
		"lime_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.LIME)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block PINK_GLAZED_TERRACOTTA = register(
		"pink_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.PINK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block GRAY_GLAZED_TERRACOTTA = register(
		"gray_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.GRAY)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block LIGHT_GRAY_GLAZED_TERRACOTTA = register(
		"light_gray_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.LIGHT_GRAY)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block CYAN_GLAZED_TERRACOTTA = register(
		"cyan_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.CYAN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block PURPLE_GLAZED_TERRACOTTA = register(
		"purple_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.PURPLE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block BLUE_GLAZED_TERRACOTTA = register(
		"blue_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.BLUE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block BROWN_GLAZED_TERRACOTTA = register(
		"brown_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.BROWN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block GREEN_GLAZED_TERRACOTTA = register(
		"green_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.GREEN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block RED_GLAZED_TERRACOTTA = register(
		"red_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.RED)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block BLACK_GLAZED_TERRACOTTA = register(
		"black_glazed_terracotta",
		new GlazedTerracottaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(DyeColor.BLACK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.4F)
				.pushReaction(PushReaction.PUSH_ONLY)
		)
	);
	public static final Block WHITE_CONCRETE = register(
		"white_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.WHITE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block ORANGE_CONCRETE = register(
		"orange_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block MAGENTA_CONCRETE = register(
		"magenta_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block LIGHT_BLUE_CONCRETE = register(
		"light_blue_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_BLUE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block YELLOW_CONCRETE = register(
		"yellow_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.YELLOW).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block LIME_CONCRETE = register(
		"lime_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.LIME).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block PINK_CONCRETE = register(
		"pink_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.PINK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block GRAY_CONCRETE = register(
		"gray_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block LIGHT_GRAY_CONCRETE = register(
		"light_gray_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block CYAN_CONCRETE = register(
		"cyan_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.CYAN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block PURPLE_CONCRETE = register(
		"purple_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.PURPLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block BLUE_CONCRETE = register(
		"blue_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.BLUE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block BROWN_CONCRETE = register(
		"brown_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.BROWN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block GREEN_CONCRETE = register(
		"green_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.GREEN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block RED_CONCRETE = register(
		"red_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block BLACK_CONCRETE = register(
		"black_concrete",
		new Block(BlockBehaviour.Properties.of().mapColor(DyeColor.BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F))
	);
	public static final Block WHITE_CONCRETE_POWDER = register(
		"white_concrete_powder",
		new ConcretePowderBlock(
			WHITE_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.WHITE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block ORANGE_CONCRETE_POWDER = register(
		"orange_concrete_powder",
		new ConcretePowderBlock(
			ORANGE_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.ORANGE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block MAGENTA_CONCRETE_POWDER = register(
		"magenta_concrete_powder",
		new ConcretePowderBlock(
			MAGENTA_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.MAGENTA).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block LIGHT_BLUE_CONCRETE_POWDER = register(
		"light_blue_concrete_powder",
		new ConcretePowderBlock(
			LIGHT_BLUE_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_BLUE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block YELLOW_CONCRETE_POWDER = register(
		"yellow_concrete_powder",
		new ConcretePowderBlock(
			YELLOW_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.YELLOW).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block LIME_CONCRETE_POWDER = register(
		"lime_concrete_powder",
		new ConcretePowderBlock(
			LIME_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.LIME).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block PINK_CONCRETE_POWDER = register(
		"pink_concrete_powder",
		new ConcretePowderBlock(
			PINK_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.PINK).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block GRAY_CONCRETE_POWDER = register(
		"gray_concrete_powder",
		new ConcretePowderBlock(
			GRAY_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.GRAY).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block LIGHT_GRAY_CONCRETE_POWDER = register(
		"light_gray_concrete_powder",
		new ConcretePowderBlock(
			LIGHT_GRAY_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_GRAY).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block CYAN_CONCRETE_POWDER = register(
		"cyan_concrete_powder",
		new ConcretePowderBlock(
			CYAN_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.CYAN).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block PURPLE_CONCRETE_POWDER = register(
		"purple_concrete_powder",
		new ConcretePowderBlock(
			PURPLE_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.PURPLE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block BLUE_CONCRETE_POWDER = register(
		"blue_concrete_powder",
		new ConcretePowderBlock(
			BLUE_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.BLUE).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block BROWN_CONCRETE_POWDER = register(
		"brown_concrete_powder",
		new ConcretePowderBlock(
			BROWN_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.BROWN).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block GREEN_CONCRETE_POWDER = register(
		"green_concrete_powder",
		new ConcretePowderBlock(
			GREEN_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.GREEN).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block RED_CONCRETE_POWDER = register(
		"red_concrete_powder",
		new ConcretePowderBlock(
			RED_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.RED).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block BLACK_CONCRETE_POWDER = register(
		"black_concrete_powder",
		new ConcretePowderBlock(
			BLACK_CONCRETE, BlockBehaviour.Properties.of().mapColor(DyeColor.BLACK).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)
		)
	);
	public static final Block KELP = register(
		"kelp",
		new KelpBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WATER)
				.noCollission()
				.randomTicks()
				.instabreak()
				.sound(SoundType.WET_GRASS)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block KELP_PLANT = register(
		"kelp_plant",
		new KelpPlantBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WATER).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block DRIED_KELP_BLOCK = register(
		"dried_kelp_block", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.5F, 2.5F).sound(SoundType.GRASS))
	);
	public static final Block TURTLE_EGG = register(
		"turtle_egg",
		new TurtleEggBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.SAND)
				.forceSolidOn()
				.strength(0.5F)
				.sound(SoundType.METAL)
				.randomTicks()
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SNIFFER_EGG = register(
		"sniffer_egg", new SnifferEggBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.5F).sound(SoundType.METAL).noOcclusion())
	);
	public static final Block DEAD_TUBE_CORAL_BLOCK = register(
		"dead_tube_coral_block",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
		)
	);
	public static final Block DEAD_BRAIN_CORAL_BLOCK = register(
		"dead_brain_coral_block",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
		)
	);
	public static final Block DEAD_BUBBLE_CORAL_BLOCK = register(
		"dead_bubble_coral_block",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
		)
	);
	public static final Block DEAD_FIRE_CORAL_BLOCK = register(
		"dead_fire_coral_block",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
		)
	);
	public static final Block DEAD_HORN_CORAL_BLOCK = register(
		"dead_horn_coral_block",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
		)
	);
	public static final Block TUBE_CORAL_BLOCK = register(
		"tube_coral_block",
		new CoralBlock(
			DEAD_TUBE_CORAL_BLOCK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLUE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
				.sound(SoundType.CORAL_BLOCK)
		)
	);
	public static final Block BRAIN_CORAL_BLOCK = register(
		"brain_coral_block",
		new CoralBlock(
			DEAD_BRAIN_CORAL_BLOCK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_PINK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
				.sound(SoundType.CORAL_BLOCK)
		)
	);
	public static final Block BUBBLE_CORAL_BLOCK = register(
		"bubble_coral_block",
		new CoralBlock(
			DEAD_BUBBLE_CORAL_BLOCK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_PURPLE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
				.sound(SoundType.CORAL_BLOCK)
		)
	);
	public static final Block FIRE_CORAL_BLOCK = register(
		"fire_coral_block",
		new CoralBlock(
			DEAD_FIRE_CORAL_BLOCK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
				.sound(SoundType.CORAL_BLOCK)
		)
	);
	public static final Block HORN_CORAL_BLOCK = register(
		"horn_coral_block",
		new CoralBlock(
			DEAD_HORN_CORAL_BLOCK,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
				.sound(SoundType.CORAL_BLOCK)
		)
	);
	public static final Block DEAD_TUBE_CORAL = register(
		"dead_tube_coral",
		new BaseCoralPlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block DEAD_BRAIN_CORAL = register(
		"dead_brain_coral",
		new BaseCoralPlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block DEAD_BUBBLE_CORAL = register(
		"dead_bubble_coral",
		new BaseCoralPlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block DEAD_FIRE_CORAL = register(
		"dead_fire_coral",
		new BaseCoralPlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block DEAD_HORN_CORAL = register(
		"dead_horn_coral",
		new BaseCoralPlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block TUBE_CORAL = register(
		"tube_coral",
		new CoralPlantBlock(
			DEAD_TUBE_CORAL,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BRAIN_CORAL = register(
		"brain_coral",
		new CoralPlantBlock(
			DEAD_BRAIN_CORAL,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BUBBLE_CORAL = register(
		"bubble_coral",
		new CoralPlantBlock(
			DEAD_BUBBLE_CORAL,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block FIRE_CORAL = register(
		"fire_coral",
		new CoralPlantBlock(
			DEAD_FIRE_CORAL,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block HORN_CORAL = register(
		"horn_coral",
		new CoralPlantBlock(
			DEAD_HORN_CORAL,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block DEAD_TUBE_CORAL_FAN = register(
		"dead_tube_coral_fan",
		new BaseCoralFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block DEAD_BRAIN_CORAL_FAN = register(
		"dead_brain_coral_fan",
		new BaseCoralFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block DEAD_BUBBLE_CORAL_FAN = register(
		"dead_bubble_coral_fan",
		new BaseCoralFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block DEAD_FIRE_CORAL_FAN = register(
		"dead_fire_coral_fan",
		new BaseCoralFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block DEAD_HORN_CORAL_FAN = register(
		"dead_horn_coral_fan",
		new BaseCoralFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
		)
	);
	public static final Block TUBE_CORAL_FAN = register(
		"tube_coral_fan",
		new CoralFanBlock(
			DEAD_TUBE_CORAL_FAN,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BRAIN_CORAL_FAN = register(
		"brain_coral_fan",
		new CoralFanBlock(
			DEAD_BRAIN_CORAL_FAN,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BUBBLE_CORAL_FAN = register(
		"bubble_coral_fan",
		new CoralFanBlock(
			DEAD_BUBBLE_CORAL_FAN,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block FIRE_CORAL_FAN = register(
		"fire_coral_fan",
		new CoralFanBlock(
			DEAD_FIRE_CORAL_FAN,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block HORN_CORAL_FAN = register(
		"horn_coral_fan",
		new CoralFanBlock(
			DEAD_HORN_CORAL_FAN,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block DEAD_TUBE_CORAL_WALL_FAN = register(
		"dead_tube_coral_wall_fan",
		new BaseCoralWallFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
				.dropsLike(DEAD_TUBE_CORAL_FAN)
		)
	);
	public static final Block DEAD_BRAIN_CORAL_WALL_FAN = register(
		"dead_brain_coral_wall_fan",
		new BaseCoralWallFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
				.dropsLike(DEAD_BRAIN_CORAL_FAN)
		)
	);
	public static final Block DEAD_BUBBLE_CORAL_WALL_FAN = register(
		"dead_bubble_coral_wall_fan",
		new BaseCoralWallFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
				.dropsLike(DEAD_BUBBLE_CORAL_FAN)
		)
	);
	public static final Block DEAD_FIRE_CORAL_WALL_FAN = register(
		"dead_fire_coral_wall_fan",
		new BaseCoralWallFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
				.dropsLike(DEAD_FIRE_CORAL_FAN)
		)
	);
	public static final Block DEAD_HORN_CORAL_WALL_FAN = register(
		"dead_horn_coral_wall_fan",
		new BaseCoralWallFanBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GRAY)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.instabreak()
				.dropsLike(DEAD_HORN_CORAL_FAN)
		)
	);
	public static final Block TUBE_CORAL_WALL_FAN = register(
		"tube_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_TUBE_CORAL_WALL_FAN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLUE)
				.noCollission()
				.instabreak()
				.sound(SoundType.WET_GRASS)
				.dropsLike(TUBE_CORAL_FAN)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BRAIN_CORAL_WALL_FAN = register(
		"brain_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_BRAIN_CORAL_WALL_FAN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_PINK)
				.noCollission()
				.instabreak()
				.sound(SoundType.WET_GRASS)
				.dropsLike(BRAIN_CORAL_FAN)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BUBBLE_CORAL_WALL_FAN = register(
		"bubble_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_BUBBLE_CORAL_WALL_FAN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_PURPLE)
				.noCollission()
				.instabreak()
				.sound(SoundType.WET_GRASS)
				.dropsLike(BUBBLE_CORAL_FAN)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block FIRE_CORAL_WALL_FAN = register(
		"fire_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_FIRE_CORAL_WALL_FAN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.noCollission()
				.instabreak()
				.sound(SoundType.WET_GRASS)
				.dropsLike(FIRE_CORAL_FAN)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block HORN_CORAL_WALL_FAN = register(
		"horn_coral_wall_fan",
		new CoralWallFanBlock(
			DEAD_HORN_CORAL_WALL_FAN,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.noCollission()
				.instabreak()
				.sound(SoundType.WET_GRASS)
				.dropsLike(HORN_CORAL_FAN)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SEA_PICKLE = register(
		"sea_pickle",
		new SeaPickleBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GREEN)
				.lightLevel(blockStatex -> SeaPickleBlock.isDead(blockStatex) ? 0 : 3 + 3 * (Integer)blockStatex.getValue(SeaPickleBlock.PICKLES))
				.sound(SoundType.SLIME_BLOCK)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BLUE_ICE = register(
		"blue_ice", new HalfTransparentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(2.8F).friction(0.989F).sound(SoundType.GLASS))
	);
	public static final Block CONDUIT = register(
		"conduit",
		new ConduitBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.DIAMOND)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.HAT)
				.strength(3.0F)
				.lightLevel(blockStatex -> 15)
				.noOcclusion()
		)
	);
	public static final Block BAMBOO_SAPLING = register(
		"bamboo_sapling",
		new BambooSaplingBlock(
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
		)
	);
	public static final Block BAMBOO = register(
		"bamboo",
		new BambooStalkBlock(
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
		)
	);
	public static final Block POTTED_BAMBOO = register("potted_bamboo", flowerPot(BAMBOO));
	public static final Block VOID_AIR = register("void_air", new AirBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().air()));
	public static final Block CAVE_AIR = register("cave_air", new AirBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().air()));
	public static final Block BUBBLE_COLUMN = register(
		"bubble_column",
		new BubbleColumnBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WATER)
				.replaceable()
				.noCollission()
				.noLootTable()
				.pushReaction(PushReaction.DESTROY)
				.liquid()
				.sound(SoundType.EMPTY)
		)
	);
	public static final Block POLISHED_GRANITE_STAIRS = register("polished_granite_stairs", legacyStair(POLISHED_GRANITE));
	public static final Block SMOOTH_RED_SANDSTONE_STAIRS = register("smooth_red_sandstone_stairs", legacyStair(SMOOTH_RED_SANDSTONE));
	public static final Block MOSSY_STONE_BRICK_STAIRS = register("mossy_stone_brick_stairs", legacyStair(MOSSY_STONE_BRICKS));
	public static final Block POLISHED_DIORITE_STAIRS = register("polished_diorite_stairs", legacyStair(POLISHED_DIORITE));
	public static final Block MOSSY_COBBLESTONE_STAIRS = register("mossy_cobblestone_stairs", legacyStair(MOSSY_COBBLESTONE));
	public static final Block END_STONE_BRICK_STAIRS = register("end_stone_brick_stairs", legacyStair(END_STONE_BRICKS));
	public static final Block STONE_STAIRS = register("stone_stairs", legacyStair(STONE));
	public static final Block SMOOTH_SANDSTONE_STAIRS = register("smooth_sandstone_stairs", legacyStair(SMOOTH_SANDSTONE));
	public static final Block SMOOTH_QUARTZ_STAIRS = register("smooth_quartz_stairs", legacyStair(SMOOTH_QUARTZ));
	public static final Block GRANITE_STAIRS = register("granite_stairs", legacyStair(GRANITE));
	public static final Block ANDESITE_STAIRS = register("andesite_stairs", legacyStair(ANDESITE));
	public static final Block RED_NETHER_BRICK_STAIRS = register("red_nether_brick_stairs", legacyStair(RED_NETHER_BRICKS));
	public static final Block POLISHED_ANDESITE_STAIRS = register("polished_andesite_stairs", legacyStair(POLISHED_ANDESITE));
	public static final Block DIORITE_STAIRS = register("diorite_stairs", legacyStair(DIORITE));
	public static final Block POLISHED_GRANITE_SLAB = register("polished_granite_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_GRANITE)));
	public static final Block SMOOTH_RED_SANDSTONE_SLAB = register(
		"smooth_red_sandstone_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(SMOOTH_RED_SANDSTONE))
	);
	public static final Block MOSSY_STONE_BRICK_SLAB = register(
		"mossy_stone_brick_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(MOSSY_STONE_BRICKS))
	);
	public static final Block POLISHED_DIORITE_SLAB = register("polished_diorite_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_DIORITE)));
	public static final Block MOSSY_COBBLESTONE_SLAB = register("mossy_cobblestone_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(MOSSY_COBBLESTONE)));
	public static final Block END_STONE_BRICK_SLAB = register("end_stone_brick_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(END_STONE_BRICKS)));
	public static final Block SMOOTH_SANDSTONE_SLAB = register("smooth_sandstone_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(SMOOTH_SANDSTONE)));
	public static final Block SMOOTH_QUARTZ_SLAB = register("smooth_quartz_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(SMOOTH_QUARTZ)));
	public static final Block GRANITE_SLAB = register("granite_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(GRANITE)));
	public static final Block ANDESITE_SLAB = register("andesite_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(ANDESITE)));
	public static final Block RED_NETHER_BRICK_SLAB = register("red_nether_brick_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(RED_NETHER_BRICKS)));
	public static final Block POLISHED_ANDESITE_SLAB = register("polished_andesite_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_ANDESITE)));
	public static final Block DIORITE_SLAB = register("diorite_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(DIORITE)));
	public static final Block BRICK_WALL = register("brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(BRICKS).forceSolidOn()));
	public static final Block PRISMARINE_WALL = register("prismarine_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(PRISMARINE).forceSolidOn()));
	public static final Block RED_SANDSTONE_WALL = register(
		"red_sandstone_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(RED_SANDSTONE).forceSolidOn())
	);
	public static final Block MOSSY_STONE_BRICK_WALL = register(
		"mossy_stone_brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(MOSSY_STONE_BRICKS).forceSolidOn())
	);
	public static final Block GRANITE_WALL = register("granite_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(GRANITE).forceSolidOn()));
	public static final Block STONE_BRICK_WALL = register("stone_brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(STONE_BRICKS).forceSolidOn()));
	public static final Block MUD_BRICK_WALL = register("mud_brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(MUD_BRICKS).forceSolidOn()));
	public static final Block NETHER_BRICK_WALL = register(
		"nether_brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(NETHER_BRICKS).forceSolidOn())
	);
	public static final Block ANDESITE_WALL = register("andesite_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(ANDESITE).forceSolidOn()));
	public static final Block RED_NETHER_BRICK_WALL = register(
		"red_nether_brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(RED_NETHER_BRICKS).forceSolidOn())
	);
	public static final Block SANDSTONE_WALL = register("sandstone_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(SANDSTONE).forceSolidOn()));
	public static final Block END_STONE_BRICK_WALL = register(
		"end_stone_brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(END_STONE_BRICKS).forceSolidOn())
	);
	public static final Block DIORITE_WALL = register("diorite_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(DIORITE).forceSolidOn()));
	public static final Block SCAFFOLDING = register(
		"scaffolding",
		new ScaffoldingBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.SAND)
				.noCollission()
				.sound(SoundType.SCAFFOLDING)
				.dynamicShape()
				.isValidSpawn(Blocks::never)
				.pushReaction(PushReaction.DESTROY)
				.isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block LOOM = register(
		"loom",
		new LoomBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block BARREL = register(
		"barrel",
		new BarrelBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block SMOKER = register(
		"smoker",
		new SmokerBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(3.5F)
				.lightLevel(litBlockEmission(13))
		)
	);
	public static final Block BLAST_FURNACE = register(
		"blast_furnace",
		new BlastFurnaceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(3.5F)
				.lightLevel(litBlockEmission(13))
		)
	);
	public static final Block CARTOGRAPHY_TABLE = register(
		"cartography_table",
		new CartographyTableBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block FLETCHING_TABLE = register(
		"fletching_table",
		new FletchingTableBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block GRINDSTONE = register(
		"grindstone",
		new GrindstoneBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.requiresCorrectToolForDrops()
				.strength(2.0F, 6.0F)
				.sound(SoundType.STONE)
				.pushReaction(PushReaction.BLOCK)
		)
	);
	public static final Block LECTERN = register(
		"lectern",
		new LecternBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block SMITHING_TABLE = register(
		"smithing_table",
		new SmithingTableBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block STONECUTTER = register(
		"stonecutter",
		new StonecutterBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)
		)
	);
	public static final Block BELL = register(
		"bell",
		new BellBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.GOLD)
				.forceSolidOn()
				.requiresCorrectToolForDrops()
				.strength(5.0F)
				.sound(SoundType.ANVIL)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block LANTERN = register(
		"lantern",
		new LanternBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.forceSolidOn()
				.requiresCorrectToolForDrops()
				.strength(3.5F)
				.sound(SoundType.LANTERN)
				.lightLevel(blockStatex -> 15)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SOUL_LANTERN = register(
		"soul_lantern",
		new LanternBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.forceSolidOn()
				.requiresCorrectToolForDrops()
				.strength(3.5F)
				.sound(SoundType.LANTERN)
				.lightLevel(blockStatex -> 10)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CAMPFIRE = register(
		"campfire",
		new CampfireBlock(
			true,
			1,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PODZOL)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F)
				.sound(SoundType.WOOD)
				.lightLevel(litBlockEmission(15))
				.noOcclusion()
				.ignitedByLava()
		)
	);
	public static final Block SOUL_CAMPFIRE = register(
		"soul_campfire",
		new CampfireBlock(
			false,
			2,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PODZOL)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F)
				.sound(SoundType.WOOD)
				.lightLevel(litBlockEmission(10))
				.noOcclusion()
				.ignitedByLava()
		)
	);
	public static final Block SWEET_BERRY_BUSH = register(
		"sweet_berry_bush",
		new SweetBerryBushBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block WARPED_STEM = register("warped_stem", netherStem(MapColor.WARPED_STEM));
	public static final Block STRIPPED_WARPED_STEM = register("stripped_warped_stem", netherStem(MapColor.WARPED_STEM));
	public static final Block WARPED_HYPHAE = register(
		"warped_hyphae",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_HYPHAE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM)
		)
	);
	public static final Block STRIPPED_WARPED_HYPHAE = register(
		"stripped_warped_hyphae",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_HYPHAE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM)
		)
	);
	public static final Block WARPED_NYLIUM = register(
		"warped_nylium",
		new NyliumBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WARPED_NYLIUM)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(0.4F)
				.sound(SoundType.NYLIUM)
				.randomTicks()
		)
	);
	public static final Block WARPED_FUNGUS = register(
		"warped_fungus",
		new FungusBlock(
			TreeFeatures.WARPED_FUNGUS_PLANTED,
			WARPED_NYLIUM,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).instabreak().noCollission().sound(SoundType.FUNGUS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block WARPED_WART_BLOCK = register(
		"warped_wart_block", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_WART_BLOCK).strength(1.0F).sound(SoundType.WART_BLOCK))
	);
	public static final Block WARPED_ROOTS = register(
		"warped_roots",
		new RootsBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_CYAN)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.ROOTS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block NETHER_SPROUTS = register(
		"nether_sprouts",
		new NetherSproutsBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_CYAN)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.NETHER_SPROUTS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CRIMSON_STEM = register("crimson_stem", netherStem(MapColor.CRIMSON_STEM));
	public static final Block STRIPPED_CRIMSON_STEM = register("stripped_crimson_stem", netherStem(MapColor.CRIMSON_STEM));
	public static final Block CRIMSON_HYPHAE = register(
		"crimson_hyphae",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_HYPHAE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM)
		)
	);
	public static final Block STRIPPED_CRIMSON_HYPHAE = register(
		"stripped_crimson_hyphae",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_HYPHAE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM)
		)
	);
	public static final Block CRIMSON_NYLIUM = register(
		"crimson_nylium",
		new NyliumBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.CRIMSON_NYLIUM)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(0.4F)
				.sound(SoundType.NYLIUM)
				.randomTicks()
		)
	);
	public static final Block CRIMSON_FUNGUS = register(
		"crimson_fungus",
		new FungusBlock(
			TreeFeatures.CRIMSON_FUNGUS_PLANTED,
			CRIMSON_NYLIUM,
			BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instabreak().noCollission().sound(SoundType.FUNGUS).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SHROOMLIGHT = register(
		"shroomlight",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.SHROOMLIGHT).lightLevel(blockStatex -> 15))
	);
	public static final Block WEEPING_VINES = register(
		"weeping_vines",
		new WeepingVinesBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.randomTicks()
				.noCollission()
				.instabreak()
				.sound(SoundType.WEEPING_VINES)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block WEEPING_VINES_PLANT = register(
		"weeping_vines_plant",
		new WeepingVinesPlantBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).noCollission().instabreak().sound(SoundType.WEEPING_VINES).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block TWISTING_VINES = register(
		"twisting_vines",
		new TwistingVinesBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_CYAN)
				.randomTicks()
				.noCollission()
				.instabreak()
				.sound(SoundType.WEEPING_VINES)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block TWISTING_VINES_PLANT = register(
		"twisting_vines_plant",
		new TwistingVinesPlantBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).noCollission().instabreak().sound(SoundType.WEEPING_VINES).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CRIMSON_ROOTS = register(
		"crimson_roots",
		new RootsBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.ROOTS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CRIMSON_PLANKS = register(
		"crimson_planks",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_STEM).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD)
		)
	);
	public static final Block WARPED_PLANKS = register(
		"warped_planks",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.WARPED_STEM).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD)
		)
	);
	public static final Block CRIMSON_SLAB = register(
		"crimson_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of()
				.mapColor(CRIMSON_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.NETHER_WOOD)
		)
	);
	public static final Block WARPED_SLAB = register(
		"warped_slab",
		new SlabBlock(
			BlockBehaviour.Properties.of()
				.mapColor(WARPED_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.NETHER_WOOD)
		)
	);
	public static final Block CRIMSON_PRESSURE_PLATE = register(
		"crimson_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.CRIMSON,
			BlockBehaviour.Properties.of()
				.mapColor(CRIMSON_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block WARPED_PRESSURE_PLATE = register(
		"warped_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.WARPED,
			BlockBehaviour.Properties.of()
				.mapColor(WARPED_PLANKS.defaultMapColor())
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASS)
				.noCollission()
				.strength(0.5F)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CRIMSON_FENCE = register(
		"crimson_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(CRIMSON_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.NETHER_WOOD)
		)
	);
	public static final Block WARPED_FENCE = register(
		"warped_fence",
		new FenceBlock(
			BlockBehaviour.Properties.of()
				.mapColor(WARPED_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F)
				.sound(SoundType.NETHER_WOOD)
		)
	);
	public static final Block CRIMSON_TRAPDOOR = register(
		"crimson_trapdoor",
		new TrapDoorBlock(
			BlockSetType.CRIMSON,
			BlockBehaviour.Properties.of()
				.mapColor(CRIMSON_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
		)
	);
	public static final Block WARPED_TRAPDOOR = register(
		"warped_trapdoor",
		new TrapDoorBlock(
			BlockSetType.WARPED,
			BlockBehaviour.Properties.of()
				.mapColor(WARPED_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
		)
	);
	public static final Block CRIMSON_FENCE_GATE = register(
		"crimson_fence_gate",
		new FenceGateBlock(
			WoodType.CRIMSON,
			BlockBehaviour.Properties.of().mapColor(CRIMSON_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F)
		)
	);
	public static final Block WARPED_FENCE_GATE = register(
		"warped_fence_gate",
		new FenceGateBlock(
			WoodType.WARPED,
			BlockBehaviour.Properties.of().mapColor(WARPED_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F)
		)
	);
	public static final Block CRIMSON_STAIRS = register("crimson_stairs", legacyStair(CRIMSON_PLANKS));
	public static final Block WARPED_STAIRS = register("warped_stairs", legacyStair(WARPED_PLANKS));
	public static final Block CRIMSON_BUTTON = register("crimson_button", woodenButton(BlockSetType.CRIMSON));
	public static final Block WARPED_BUTTON = register("warped_button", woodenButton(BlockSetType.WARPED));
	public static final Block CRIMSON_DOOR = register(
		"crimson_door",
		new DoorBlock(
			BlockSetType.CRIMSON,
			BlockBehaviour.Properties.of()
				.mapColor(CRIMSON_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block WARPED_DOOR = register(
		"warped_door",
		new DoorBlock(
			BlockSetType.WARPED,
			BlockBehaviour.Properties.of()
				.mapColor(WARPED_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.strength(3.0F)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CRIMSON_SIGN = register(
		"crimson_sign",
		new StandingSignBlock(
			WoodType.CRIMSON,
			BlockBehaviour.Properties.of().mapColor(CRIMSON_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)
		)
	);
	public static final Block WARPED_SIGN = register(
		"warped_sign",
		new StandingSignBlock(
			WoodType.WARPED,
			BlockBehaviour.Properties.of().mapColor(WARPED_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)
		)
	);
	public static final Block CRIMSON_WALL_SIGN = register(
		"crimson_wall_sign",
		new WallSignBlock(
			WoodType.CRIMSON,
			BlockBehaviour.Properties.of()
				.mapColor(CRIMSON_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.forceSolidOn()
				.noCollission()
				.strength(1.0F)
				.dropsLike(CRIMSON_SIGN)
		)
	);
	public static final Block WARPED_WALL_SIGN = register(
		"warped_wall_sign",
		new WallSignBlock(
			WoodType.WARPED,
			BlockBehaviour.Properties.of()
				.mapColor(WARPED_PLANKS.defaultMapColor())
				.instrument(NoteBlockInstrument.BASS)
				.forceSolidOn()
				.noCollission()
				.strength(1.0F)
				.dropsLike(WARPED_SIGN)
		)
	);
	public static final Block STRUCTURE_BLOCK = register(
		"structure_block",
		new StructureBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable())
	);
	public static final Block JIGSAW = register(
		"jigsaw",
		new JigsawBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable())
	);
	public static final Block COMPOSTER = register(
		"composter",
		new ComposterBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(0.6F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block TARGET = register(
		"target", new TargetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(0.5F).sound(SoundType.GRASS))
	);
	public static final Block BEE_NEST = register(
		"bee_nest",
		new BeehiveBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).instrument(NoteBlockInstrument.BASS).strength(0.3F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block BEEHIVE = register(
		"beehive",
		new BeehiveBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(0.6F).sound(SoundType.WOOD).ignitedByLava()
		)
	);
	public static final Block HONEY_BLOCK = register(
		"honey_block",
		new HoneyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).speedFactor(0.4F).jumpFactor(0.5F).noOcclusion().sound(SoundType.HONEY_BLOCK))
	);
	public static final Block HONEYCOMB_BLOCK = register(
		"honeycomb_block", new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.6F).sound(SoundType.CORAL_BLOCK))
	);
	public static final Block NETHERITE_BLOCK = register(
		"netherite_block",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK)
		)
	);
	public static final Block ANCIENT_DEBRIS = register(
		"ancient_debris",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(30.0F, 1200.0F).sound(SoundType.ANCIENT_DEBRIS)
		)
	);
	public static final Block CRYING_OBSIDIAN = register(
		"crying_obsidian",
		new CryingObsidianBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(50.0F, 1200.0F)
				.lightLevel(blockStatex -> 10)
		)
	);
	public static final Block RESPAWN_ANCHOR = register(
		"respawn_anchor",
		new RespawnAnchorBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(50.0F, 1200.0F)
				.lightLevel(blockStatex -> RespawnAnchorBlock.getScaledChargeLevel(blockStatex, 15))
		)
	);
	public static final Block POTTED_CRIMSON_FUNGUS = register("potted_crimson_fungus", flowerPot(CRIMSON_FUNGUS));
	public static final Block POTTED_WARPED_FUNGUS = register("potted_warped_fungus", flowerPot(WARPED_FUNGUS));
	public static final Block POTTED_CRIMSON_ROOTS = register("potted_crimson_roots", flowerPot(CRIMSON_ROOTS));
	public static final Block POTTED_WARPED_ROOTS = register("potted_warped_roots", flowerPot(WARPED_ROOTS));
	public static final Block LODESTONE = register(
		"lodestone",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.requiresCorrectToolForDrops()
				.strength(3.5F)
				.sound(SoundType.LODESTONE)
				.pushReaction(PushReaction.BLOCK)
		)
	);
	public static final Block BLACKSTONE = register(
		"blackstone",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)
		)
	);
	public static final Block BLACKSTONE_STAIRS = register("blackstone_stairs", legacyStair(BLACKSTONE));
	public static final Block BLACKSTONE_WALL = register("blackstone_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(BLACKSTONE).forceSolidOn()));
	public static final Block BLACKSTONE_SLAB = register("blackstone_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(BLACKSTONE).strength(2.0F, 6.0F)));
	public static final Block POLISHED_BLACKSTONE = register(
		"polished_blackstone", new Block(BlockBehaviour.Properties.ofLegacyCopy(BLACKSTONE).strength(2.0F, 6.0F))
	);
	public static final Block POLISHED_BLACKSTONE_BRICKS = register(
		"polished_blackstone_bricks", new Block(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE).strength(1.5F, 6.0F))
	);
	public static final Block CRACKED_POLISHED_BLACKSTONE_BRICKS = register(
		"cracked_polished_blackstone_bricks", new Block(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE_BRICKS))
	);
	public static final Block CHISELED_POLISHED_BLACKSTONE = register(
		"chiseled_polished_blackstone", new Block(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE).strength(1.5F, 6.0F))
	);
	public static final Block POLISHED_BLACKSTONE_BRICK_SLAB = register(
		"polished_blackstone_brick_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE_BRICKS).strength(2.0F, 6.0F))
	);
	public static final Block POLISHED_BLACKSTONE_BRICK_STAIRS = register("polished_blackstone_brick_stairs", legacyStair(POLISHED_BLACKSTONE_BRICKS));
	public static final Block POLISHED_BLACKSTONE_BRICK_WALL = register(
		"polished_blackstone_brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE_BRICKS).forceSolidOn())
	);
	public static final Block GILDED_BLACKSTONE = register(
		"gilded_blackstone", new Block(BlockBehaviour.Properties.ofLegacyCopy(BLACKSTONE).sound(SoundType.GILDED_BLACKSTONE))
	);
	public static final Block POLISHED_BLACKSTONE_STAIRS = register("polished_blackstone_stairs", legacyStair(POLISHED_BLACKSTONE));
	public static final Block POLISHED_BLACKSTONE_SLAB = register(
		"polished_blackstone_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE))
	);
	public static final Block POLISHED_BLACKSTONE_PRESSURE_PLATE = register(
		"polished_blackstone_pressure_plate",
		new PressurePlateBlock(
			BlockSetType.POLISHED_BLACKSTONE,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.forceSolidOn()
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.noCollission()
				.strength(0.5F)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block POLISHED_BLACKSTONE_BUTTON = register("polished_blackstone_button", stoneButton());
	public static final Block POLISHED_BLACKSTONE_WALL = register(
		"polished_blackstone_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_BLACKSTONE).forceSolidOn())
	);
	public static final Block CHISELED_NETHER_BRICKS = register(
		"chiseled_nether_bricks",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(2.0F, 6.0F)
				.sound(SoundType.NETHER_BRICKS)
		)
	);
	public static final Block CRACKED_NETHER_BRICKS = register(
		"cracked_nether_bricks",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.NETHER)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(2.0F, 6.0F)
				.sound(SoundType.NETHER_BRICKS)
		)
	);
	public static final Block QUARTZ_BRICKS = register("quartz_bricks", new Block(BlockBehaviour.Properties.ofLegacyCopy(QUARTZ_BLOCK)));
	public static final Block CANDLE = register("candle", candle(MapColor.SAND));
	public static final Block WHITE_CANDLE = register("white_candle", candle(MapColor.WOOL));
	public static final Block ORANGE_CANDLE = register("orange_candle", candle(MapColor.COLOR_ORANGE));
	public static final Block MAGENTA_CANDLE = register("magenta_candle", candle(MapColor.COLOR_MAGENTA));
	public static final Block LIGHT_BLUE_CANDLE = register("light_blue_candle", candle(MapColor.COLOR_LIGHT_BLUE));
	public static final Block YELLOW_CANDLE = register("yellow_candle", candle(MapColor.COLOR_YELLOW));
	public static final Block LIME_CANDLE = register("lime_candle", candle(MapColor.COLOR_LIGHT_GREEN));
	public static final Block PINK_CANDLE = register("pink_candle", candle(MapColor.COLOR_PINK));
	public static final Block GRAY_CANDLE = register("gray_candle", candle(MapColor.COLOR_GRAY));
	public static final Block LIGHT_GRAY_CANDLE = register("light_gray_candle", candle(MapColor.COLOR_LIGHT_GRAY));
	public static final Block CYAN_CANDLE = register("cyan_candle", candle(MapColor.COLOR_CYAN));
	public static final Block PURPLE_CANDLE = register("purple_candle", candle(MapColor.COLOR_PURPLE));
	public static final Block BLUE_CANDLE = register("blue_candle", candle(MapColor.COLOR_BLUE));
	public static final Block BROWN_CANDLE = register("brown_candle", candle(MapColor.COLOR_BROWN));
	public static final Block GREEN_CANDLE = register("green_candle", candle(MapColor.COLOR_GREEN));
	public static final Block RED_CANDLE = register("red_candle", candle(MapColor.COLOR_RED));
	public static final Block BLACK_CANDLE = register("black_candle", candle(MapColor.COLOR_BLACK));
	public static final Block CANDLE_CAKE = register(
		"candle_cake", new CandleCakeBlock(CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CAKE).lightLevel(litBlockEmission(3)))
	);
	public static final Block WHITE_CANDLE_CAKE = register(
		"white_candle_cake", new CandleCakeBlock(WHITE_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block ORANGE_CANDLE_CAKE = register(
		"orange_candle_cake", new CandleCakeBlock(ORANGE_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block MAGENTA_CANDLE_CAKE = register(
		"magenta_candle_cake", new CandleCakeBlock(MAGENTA_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block LIGHT_BLUE_CANDLE_CAKE = register(
		"light_blue_candle_cake", new CandleCakeBlock(LIGHT_BLUE_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block YELLOW_CANDLE_CAKE = register(
		"yellow_candle_cake", new CandleCakeBlock(YELLOW_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block LIME_CANDLE_CAKE = register(
		"lime_candle_cake", new CandleCakeBlock(LIME_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block PINK_CANDLE_CAKE = register(
		"pink_candle_cake", new CandleCakeBlock(PINK_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block GRAY_CANDLE_CAKE = register(
		"gray_candle_cake", new CandleCakeBlock(GRAY_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block LIGHT_GRAY_CANDLE_CAKE = register(
		"light_gray_candle_cake", new CandleCakeBlock(LIGHT_GRAY_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block CYAN_CANDLE_CAKE = register(
		"cyan_candle_cake", new CandleCakeBlock(CYAN_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block PURPLE_CANDLE_CAKE = register(
		"purple_candle_cake", new CandleCakeBlock(PURPLE_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block BLUE_CANDLE_CAKE = register(
		"blue_candle_cake", new CandleCakeBlock(BLUE_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block BROWN_CANDLE_CAKE = register(
		"brown_candle_cake", new CandleCakeBlock(BROWN_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block GREEN_CANDLE_CAKE = register(
		"green_candle_cake", new CandleCakeBlock(GREEN_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block RED_CANDLE_CAKE = register("red_candle_cake", new CandleCakeBlock(RED_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE)));
	public static final Block BLACK_CANDLE_CAKE = register(
		"black_candle_cake", new CandleCakeBlock(BLACK_CANDLE, BlockBehaviour.Properties.ofLegacyCopy(CANDLE_CAKE))
	);
	public static final Block AMETHYST_BLOCK = register(
		"amethyst_block",
		new AmethystBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.5F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops())
	);
	public static final Block BUDDING_AMETHYST = register(
		"budding_amethyst",
		new BuddingAmethystBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_PURPLE)
				.randomTicks()
				.strength(1.5F)
				.sound(SoundType.AMETHYST)
				.requiresCorrectToolForDrops()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block AMETHYST_CLUSTER = register(
		"amethyst_cluster",
		new AmethystClusterBlock(
			7.0F,
			3.0F,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_PURPLE)
				.forceSolidOn()
				.noOcclusion()
				.sound(SoundType.AMETHYST_CLUSTER)
				.strength(1.5F)
				.lightLevel(blockStatex -> 5)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block LARGE_AMETHYST_BUD = register(
		"large_amethyst_bud",
		new AmethystClusterBlock(
			5.0F, 3.0F, BlockBehaviour.Properties.ofLegacyCopy(AMETHYST_CLUSTER).sound(SoundType.MEDIUM_AMETHYST_BUD).lightLevel(blockStatex -> 4)
		)
	);
	public static final Block MEDIUM_AMETHYST_BUD = register(
		"medium_amethyst_bud",
		new AmethystClusterBlock(
			4.0F, 3.0F, BlockBehaviour.Properties.ofLegacyCopy(AMETHYST_CLUSTER).sound(SoundType.LARGE_AMETHYST_BUD).lightLevel(blockStatex -> 2)
		)
	);
	public static final Block SMALL_AMETHYST_BUD = register(
		"small_amethyst_bud",
		new AmethystClusterBlock(
			3.0F, 4.0F, BlockBehaviour.Properties.ofLegacyCopy(AMETHYST_CLUSTER).sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(blockStatex -> 1)
		)
	);
	public static final Block TUFF = register(
		"tuff",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_GRAY)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.sound(SoundType.TUFF)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F)
		)
	);
	public static final Block TUFF_SLAB = register(
		"tuff_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(TUFF).requiredFeatures(FeatureFlags.UPDATE_1_21))
	);
	public static final Block TUFF_STAIRS = register(
		"tuff_stairs", new StairBlock(TUFF.defaultBlockState(), BlockBehaviour.Properties.ofLegacyCopy(TUFF).requiredFeatures(FeatureFlags.UPDATE_1_21))
	);
	public static final Block TUFF_WALL = register(
		"tuff_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(TUFF).forceSolidOn().requiredFeatures(FeatureFlags.UPDATE_1_21))
	);
	public static final Block POLISHED_TUFF = register(
		"polished_tuff", new Block(BlockBehaviour.Properties.ofLegacyCopy(TUFF).sound(SoundType.POLISHED_TUFF).requiredFeatures(FeatureFlags.UPDATE_1_21))
	);
	public static final Block POLISHED_TUFF_SLAB = register("polished_tuff_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_TUFF)));
	public static final Block POLISHED_TUFF_STAIRS = register(
		"polished_tuff_stairs", new StairBlock(POLISHED_TUFF.defaultBlockState(), BlockBehaviour.Properties.ofLegacyCopy(POLISHED_TUFF))
	);
	public static final Block POLISHED_TUFF_WALL = register(
		"polished_tuff_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_TUFF).forceSolidOn())
	);
	public static final Block CHISELED_TUFF = register(
		"chiseled_tuff", new Block(BlockBehaviour.Properties.ofLegacyCopy(TUFF).requiredFeatures(FeatureFlags.UPDATE_1_21))
	);
	public static final Block TUFF_BRICKS = register(
		"tuff_bricks", new Block(BlockBehaviour.Properties.ofLegacyCopy(TUFF).sound(SoundType.TUFF_BRICKS).requiredFeatures(FeatureFlags.UPDATE_1_21))
	);
	public static final Block TUFF_BRICK_SLAB = register("tuff_brick_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(TUFF_BRICKS)));
	public static final Block TUFF_BRICK_STAIRS = register(
		"tuff_brick_stairs", new StairBlock(TUFF_BRICKS.defaultBlockState(), BlockBehaviour.Properties.ofLegacyCopy(TUFF_BRICKS))
	);
	public static final Block TUFF_BRICK_WALL = register("tuff_brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(TUFF_BRICKS).forceSolidOn()));
	public static final Block CHISELED_TUFF_BRICKS = register("chiseled_tuff_bricks", new Block(BlockBehaviour.Properties.ofLegacyCopy(TUFF_BRICKS)));
	public static final Block CALCITE = register(
		"calcite",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_WHITE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.sound(SoundType.CALCITE)
				.requiresCorrectToolForDrops()
				.strength(0.75F)
		)
	);
	public static final Block TINTED_GLASS = register(
		"tinted_glass",
		new TintedGlassBlock(
			BlockBehaviour.Properties.ofLegacyCopy(GLASS)
				.mapColor(MapColor.COLOR_GRAY)
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.isRedstoneConductor(Blocks::never)
				.isSuffocating(Blocks::never)
				.isViewBlocking(Blocks::never)
		)
	);
	public static final Block POWDER_SNOW = register(
		"powder_snow",
		new PowderSnowBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.25F).sound(SoundType.POWDER_SNOW).dynamicShape().isRedstoneConductor(Blocks::never)
		)
	);
	public static final Block SCULK_SENSOR = register(
		"sculk_sensor",
		new SculkSensorBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_CYAN)
				.strength(1.5F)
				.sound(SoundType.SCULK_SENSOR)
				.lightLevel(blockStatex -> 1)
				.emissiveRendering((blockStatex, blockGetter, blockPos) -> SculkSensorBlock.getPhase(blockStatex) == SculkSensorPhase.ACTIVE)
		)
	);
	public static final Block CALIBRATED_SCULK_SENSOR = register(
		"calibrated_sculk_sensor", new CalibratedSculkSensorBlock(BlockBehaviour.Properties.ofLegacyCopy(SCULK_SENSOR))
	);
	public static final Block SCULK = register(
		"sculk", new SculkBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.2F).sound(SoundType.SCULK))
	);
	public static final Block SCULK_VEIN = register(
		"sculk_vein",
		new SculkVeinBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_BLACK)
				.forceSolidOn()
				.noCollission()
				.strength(0.2F)
				.sound(SoundType.SCULK_VEIN)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SCULK_CATALYST = register(
		"sculk_catalyst",
		new SculkCatalystBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0F, 3.0F).sound(SoundType.SCULK_CATALYST).lightLevel(blockStatex -> 6)
		)
	);
	public static final Block SCULK_SHRIEKER = register(
		"sculk_shrieker", new SculkShriekerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0F, 3.0F).sound(SoundType.SCULK_SHRIEKER))
	);
	public static final Block COPPER_BLOCK = register(
		"copper_block",
		new WeatheringCopperFullBlock(
			WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.COPPER)
		)
	);
	public static final Block EXPOSED_COPPER = register(
		"exposed_copper",
		new WeatheringCopperFullBlock(
			WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
		)
	);
	public static final Block WEATHERED_COPPER = register(
		"weathered_copper",
		new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK).mapColor(MapColor.WARPED_STEM))
	);
	public static final Block OXIDIZED_COPPER = register(
		"oxidized_copper",
		new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK).mapColor(MapColor.WARPED_NYLIUM))
	);
	public static final Block COPPER_ORE = register("copper_ore", new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(IRON_ORE)));
	public static final Block DEEPSLATE_COPPER_ORE = register(
		"deepslate_copper_ore",
		new DropExperienceBlock(
			ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(COPPER_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)
		)
	);
	public static final Block OXIDIZED_CUT_COPPER = register(
		"oxidized_cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER))
	);
	public static final Block WEATHERED_CUT_COPPER = register(
		"weathered_cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER))
	);
	public static final Block EXPOSED_CUT_COPPER = register(
		"exposed_cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER))
	);
	public static final Block CUT_COPPER = register(
		"cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK))
	);
	public static final Block OXIDIZED_CHISELED_COPPER = register(
		"oxidized_chiseled_copper",
		new WeatheringCopperFullBlock(
			WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER).requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block WEATHERED_CHISELED_COPPER = register(
		"weathered_chiseled_copper",
		new WeatheringCopperFullBlock(
			WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER).requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block EXPOSED_CHISELED_COPPER = register(
		"exposed_chiseled_copper",
		new WeatheringCopperFullBlock(
			WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER).requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block CHISELED_COPPER = register(
		"chiseled_copper",
		new WeatheringCopperFullBlock(
			WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK).requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block WAXED_OXIDIZED_CHISELED_COPPER = register(
		"waxed_oxidized_chiseled_copper", new Block(BlockBehaviour.Properties.ofFullCopy(OXIDIZED_CHISELED_COPPER))
	);
	public static final Block WAXED_WEATHERED_CHISELED_COPPER = register(
		"waxed_weathered_chiseled_copper", new Block(BlockBehaviour.Properties.ofFullCopy(WEATHERED_CHISELED_COPPER))
	);
	public static final Block WAXED_EXPOSED_CHISELED_COPPER = register(
		"waxed_exposed_chiseled_copper", new Block(BlockBehaviour.Properties.ofFullCopy(EXPOSED_CHISELED_COPPER))
	);
	public static final Block WAXED_CHISELED_COPPER = register("waxed_chiseled_copper", new Block(BlockBehaviour.Properties.ofFullCopy(CHISELED_COPPER)));
	public static final Block OXIDIZED_CUT_COPPER_STAIRS = register(
		"oxidized_cut_copper_stairs",
		new WeatheringCopperStairBlock(
			WeatheringCopper.WeatherState.OXIDIZED, OXIDIZED_CUT_COPPER.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(OXIDIZED_CUT_COPPER)
		)
	);
	public static final Block WEATHERED_CUT_COPPER_STAIRS = register(
		"weathered_cut_copper_stairs",
		new WeatheringCopperStairBlock(
			WeatheringCopper.WeatherState.WEATHERED, WEATHERED_CUT_COPPER.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER)
		)
	);
	public static final Block EXPOSED_CUT_COPPER_STAIRS = register(
		"exposed_cut_copper_stairs",
		new WeatheringCopperStairBlock(
			WeatheringCopper.WeatherState.EXPOSED, EXPOSED_CUT_COPPER.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER)
		)
	);
	public static final Block CUT_COPPER_STAIRS = register(
		"cut_copper_stairs",
		new WeatheringCopperStairBlock(WeatheringCopper.WeatherState.UNAFFECTED, CUT_COPPER.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK))
	);
	public static final Block OXIDIZED_CUT_COPPER_SLAB = register(
		"oxidized_cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.ofFullCopy(OXIDIZED_CUT_COPPER))
	);
	public static final Block WEATHERED_CUT_COPPER_SLAB = register(
		"weathered_cut_copper_slab",
		new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.ofFullCopy(WEATHERED_CUT_COPPER))
	);
	public static final Block EXPOSED_CUT_COPPER_SLAB = register(
		"exposed_cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.ofFullCopy(EXPOSED_CUT_COPPER))
	);
	public static final Block CUT_COPPER_SLAB = register(
		"cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.ofFullCopy(CUT_COPPER))
	);
	public static final Block WAXED_COPPER_BLOCK = register("waxed_copper_block", new Block(BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK)));
	public static final Block WAXED_WEATHERED_COPPER = register("waxed_weathered_copper", new Block(BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER)));
	public static final Block WAXED_EXPOSED_COPPER = register("waxed_exposed_copper", new Block(BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER)));
	public static final Block WAXED_OXIDIZED_COPPER = register("waxed_oxidized_copper", new Block(BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER)));
	public static final Block WAXED_OXIDIZED_CUT_COPPER = register("waxed_oxidized_cut_copper", new Block(BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER)));
	public static final Block WAXED_WEATHERED_CUT_COPPER = register(
		"waxed_weathered_cut_copper", new Block(BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER))
	);
	public static final Block WAXED_EXPOSED_CUT_COPPER = register("waxed_exposed_cut_copper", new Block(BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER)));
	public static final Block WAXED_CUT_COPPER = register("waxed_cut_copper", new Block(BlockBehaviour.Properties.ofFullCopy(COPPER_BLOCK)));
	public static final Block WAXED_OXIDIZED_CUT_COPPER_STAIRS = register("waxed_oxidized_cut_copper_stairs", stair(WAXED_OXIDIZED_CUT_COPPER));
	public static final Block WAXED_WEATHERED_CUT_COPPER_STAIRS = register("waxed_weathered_cut_copper_stairs", stair(WAXED_WEATHERED_CUT_COPPER));
	public static final Block WAXED_EXPOSED_CUT_COPPER_STAIRS = register("waxed_exposed_cut_copper_stairs", stair(WAXED_EXPOSED_CUT_COPPER));
	public static final Block WAXED_CUT_COPPER_STAIRS = register("waxed_cut_copper_stairs", stair(WAXED_CUT_COPPER));
	public static final Block WAXED_OXIDIZED_CUT_COPPER_SLAB = register(
		"waxed_oxidized_cut_copper_slab", new SlabBlock(BlockBehaviour.Properties.ofFullCopy(WAXED_OXIDIZED_CUT_COPPER).requiresCorrectToolForDrops())
	);
	public static final Block WAXED_WEATHERED_CUT_COPPER_SLAB = register(
		"waxed_weathered_cut_copper_slab", new SlabBlock(BlockBehaviour.Properties.ofFullCopy(WAXED_WEATHERED_CUT_COPPER).requiresCorrectToolForDrops())
	);
	public static final Block WAXED_EXPOSED_CUT_COPPER_SLAB = register(
		"waxed_exposed_cut_copper_slab", new SlabBlock(BlockBehaviour.Properties.ofFullCopy(WAXED_EXPOSED_CUT_COPPER).requiresCorrectToolForDrops())
	);
	public static final Block WAXED_CUT_COPPER_SLAB = register(
		"waxed_cut_copper_slab", new SlabBlock(BlockBehaviour.Properties.ofFullCopy(WAXED_CUT_COPPER).requiresCorrectToolForDrops())
	);
	public static final Block COPPER_DOOR = register(
		"copper_door",
		new WeatheringCopperDoorBlock(
			BlockSetType.COPPER,
			WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.mapColor(COPPER_BLOCK.defaultMapColor())
				.strength(3.0F, 6.0F)
				.noOcclusion()
				.requiresCorrectToolForDrops()
				.pushReaction(PushReaction.DESTROY)
				.requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block EXPOSED_COPPER_DOOR = register(
		"exposed_copper_door",
		new WeatheringCopperDoorBlock(
			BlockSetType.COPPER, WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.ofFullCopy(COPPER_DOOR).mapColor(EXPOSED_COPPER.defaultMapColor())
		)
	);
	public static final Block OXIDIZED_COPPER_DOOR = register(
		"oxidized_copper_door",
		new WeatheringCopperDoorBlock(
			BlockSetType.COPPER, WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.ofFullCopy(COPPER_DOOR).mapColor(OXIDIZED_COPPER.defaultMapColor())
		)
	);
	public static final Block WEATHERED_COPPER_DOOR = register(
		"weathered_copper_door",
		new WeatheringCopperDoorBlock(
			BlockSetType.COPPER, WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.ofFullCopy(COPPER_DOOR).mapColor(WEATHERED_COPPER.defaultMapColor())
		)
	);
	public static final Block WAXED_COPPER_DOOR = register(
		"waxed_copper_door", new DoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.ofFullCopy(COPPER_DOOR))
	);
	public static final Block WAXED_EXPOSED_COPPER_DOOR = register(
		"waxed_exposed_copper_door", new DoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_DOOR))
	);
	public static final Block WAXED_OXIDIZED_COPPER_DOOR = register(
		"waxed_oxidized_copper_door", new DoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_DOOR))
	);
	public static final Block WAXED_WEATHERED_COPPER_DOOR = register(
		"waxed_weathered_copper_door", new DoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_DOOR))
	);
	public static final Block COPPER_TRAPDOOR = register(
		"copper_trapdoor",
		new WeatheringCopperTrapDoorBlock(
			BlockSetType.COPPER,
			WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.mapColor(COPPER_BLOCK.defaultMapColor())
				.strength(3.0F, 6.0F)
				.requiresCorrectToolForDrops()
				.noOcclusion()
				.isValidSpawn(Blocks::never)
				.requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block EXPOSED_COPPER_TRAPDOOR = register(
		"exposed_copper_trapdoor",
		new WeatheringCopperTrapDoorBlock(
			BlockSetType.COPPER, WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.ofFullCopy(COPPER_TRAPDOOR).mapColor(EXPOSED_COPPER.defaultMapColor())
		)
	);
	public static final Block OXIDIZED_COPPER_TRAPDOOR = register(
		"oxidized_copper_trapdoor",
		new WeatheringCopperTrapDoorBlock(
			BlockSetType.COPPER,
			WeatheringCopper.WeatherState.OXIDIZED,
			BlockBehaviour.Properties.ofFullCopy(COPPER_TRAPDOOR).mapColor(OXIDIZED_COPPER.defaultMapColor())
		)
	);
	public static final Block WEATHERED_COPPER_TRAPDOOR = register(
		"weathered_copper_trapdoor",
		new WeatheringCopperTrapDoorBlock(
			BlockSetType.COPPER,
			WeatheringCopper.WeatherState.WEATHERED,
			BlockBehaviour.Properties.ofFullCopy(COPPER_TRAPDOOR).mapColor(WEATHERED_COPPER.defaultMapColor())
		)
	);
	public static final Block WAXED_COPPER_TRAPDOOR = register(
		"waxed_copper_trapdoor", new TrapDoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.ofFullCopy(COPPER_TRAPDOOR))
	);
	public static final Block WAXED_EXPOSED_COPPER_TRAPDOOR = register(
		"waxed_exposed_copper_trapdoor", new TrapDoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_TRAPDOOR))
	);
	public static final Block WAXED_OXIDIZED_COPPER_TRAPDOOR = register(
		"waxed_oxidized_copper_trapdoor", new TrapDoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_TRAPDOOR))
	);
	public static final Block WAXED_WEATHERED_COPPER_TRAPDOOR = register(
		"waxed_weathered_copper_trapdoor", new TrapDoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_TRAPDOOR))
	);
	public static final Block COPPER_GRATE = register(
		"copper_grate",
		new WeatheringCopperGrateBlock(
			WeatheringCopper.WeatherState.UNAFFECTED,
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
				.requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block EXPOSED_COPPER_GRATE = register(
		"exposed_copper_grate",
		new WeatheringCopperGrateBlock(
			WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.ofFullCopy(COPPER_GRATE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
		)
	);
	public static final Block WEATHERED_COPPER_GRATE = register(
		"weathered_copper_grate",
		new WeatheringCopperGrateBlock(WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.ofFullCopy(COPPER_GRATE).mapColor(MapColor.WARPED_STEM))
	);
	public static final Block OXIDIZED_COPPER_GRATE = register(
		"oxidized_copper_grate",
		new WeatheringCopperGrateBlock(WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.ofFullCopy(COPPER_GRATE).mapColor(MapColor.WARPED_NYLIUM))
	);
	public static final Block WAXED_COPPER_GRATE = register(
		"waxed_copper_grate", new WaterloggedTransparentBlock(BlockBehaviour.Properties.ofFullCopy(COPPER_GRATE))
	);
	public static final Block WAXED_EXPOSED_COPPER_GRATE = register(
		"waxed_exposed_copper_grate", new WaterloggedTransparentBlock(BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_GRATE))
	);
	public static final Block WAXED_WEATHERED_COPPER_GRATE = register(
		"waxed_weathered_copper_grate", new WaterloggedTransparentBlock(BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_GRATE))
	);
	public static final Block WAXED_OXIDIZED_COPPER_GRATE = register(
		"waxed_oxidized_copper_grate", new WaterloggedTransparentBlock(BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_GRATE))
	);
	public static final Block COPPER_BULB = register(
		"copper_bulb",
		new WeatheringCopperBulbBlock(
			WeatheringCopper.WeatherState.UNAFFECTED,
			BlockBehaviour.Properties.of()
				.mapColor(COPPER_BLOCK.defaultMapColor())
				.strength(3.0F, 6.0F)
				.sound(SoundType.COPPER_BULB)
				.requiresCorrectToolForDrops()
				.isRedstoneConductor(Blocks::never)
				.lightLevel(litBlockEmission(15))
				.requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block EXPOSED_COPPER_BULB = register(
		"exposed_copper_bulb",
		new WeatheringCopperBulbBlock(
			WeatheringCopper.WeatherState.EXPOSED,
			BlockBehaviour.Properties.ofFullCopy(COPPER_BULB).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).lightLevel(litBlockEmission(12))
		)
	);
	public static final Block WEATHERED_COPPER_BULB = register(
		"weathered_copper_bulb",
		new WeatheringCopperBulbBlock(
			WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.ofFullCopy(COPPER_BULB).mapColor(MapColor.WARPED_STEM).lightLevel(litBlockEmission(8))
		)
	);
	public static final Block OXIDIZED_COPPER_BULB = register(
		"oxidized_copper_bulb",
		new WeatheringCopperBulbBlock(
			WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.ofFullCopy(COPPER_BULB).mapColor(MapColor.WARPED_NYLIUM).lightLevel(litBlockEmission(4))
		)
	);
	public static final Block WAXED_COPPER_BULB = register("waxed_copper_bulb", new CopperBulbBlock(BlockBehaviour.Properties.ofFullCopy(COPPER_BULB)));
	public static final Block WAXED_EXPOSED_COPPER_BULB = register(
		"waxed_exposed_copper_bulb", new CopperBulbBlock(BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_BULB))
	);
	public static final Block WAXED_WEATHERED_COPPER_BULB = register(
		"waxed_weathered_copper_bulb", new CopperBulbBlock(BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_BULB))
	);
	public static final Block WAXED_OXIDIZED_COPPER_BULB = register(
		"waxed_oxidized_copper_bulb", new CopperBulbBlock(BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_BULB))
	);
	public static final Block LIGHTNING_ROD = register(
		"lightning_rod",
		new LightningRodBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_ORANGE)
				.forceSolidOn()
				.requiresCorrectToolForDrops()
				.strength(3.0F, 6.0F)
				.sound(SoundType.COPPER)
				.noOcclusion()
		)
	);
	public static final Block POINTED_DRIPSTONE = register(
		"pointed_dripstone",
		new PointedDripstoneBlock(
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
		)
	);
	public static final Block DRIPSTONE_BLOCK = register(
		"dripstone_block",
		new Block(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_BROWN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.sound(SoundType.DRIPSTONE_BLOCK)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 1.0F)
		)
	);
	public static final Block CAVE_VINES = register(
		"cave_vines",
		new CaveVinesBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.randomTicks()
				.noCollission()
				.lightLevel(CaveVines.emission(14))
				.instabreak()
				.sound(SoundType.CAVE_VINES)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block CAVE_VINES_PLANT = register(
		"cave_vines_plant",
		new CaveVinesPlantBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.lightLevel(CaveVines.emission(14))
				.instabreak()
				.sound(SoundType.CAVE_VINES)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SPORE_BLOSSOM = register(
		"spore_blossom",
		new SporeBlossomBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().noCollission().sound(SoundType.SPORE_BLOSSOM).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block AZALEA = register(
		"azalea",
		new AzaleaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.forceSolidOff()
				.instabreak()
				.sound(SoundType.AZALEA)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block FLOWERING_AZALEA = register(
		"flowering_azalea",
		new AzaleaBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.forceSolidOff()
				.instabreak()
				.sound(SoundType.FLOWERING_AZALEA)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block MOSS_CARPET = register(
		"moss_carpet",
		new CarpetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.1F).sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY))
	);
	public static final Block PINK_PETALS = register(
		"pink_petals",
		new PinkPetalsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY))
	);
	public static final Block MOSS_BLOCK = register(
		"moss_block",
		new MossBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.1F).sound(SoundType.MOSS).pushReaction(PushReaction.DESTROY))
	);
	public static final Block BIG_DRIPLEAF = register(
		"big_dripleaf",
		new BigDripleafBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).forceSolidOff().strength(0.1F).sound(SoundType.BIG_DRIPLEAF).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block BIG_DRIPLEAF_STEM = register(
		"big_dripleaf_stem",
		new BigDripleafStemBlock(
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().strength(0.1F).sound(SoundType.BIG_DRIPLEAF).pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block SMALL_DRIPLEAF = register(
		"small_dripleaf",
		new SmallDripleafBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT)
				.noCollission()
				.instabreak()
				.sound(SoundType.SMALL_DRIPLEAF)
				.offsetType(BlockBehaviour.OffsetType.XYZ)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block HANGING_ROOTS = register(
		"hanging_roots",
		new HangingRootsBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.DIRT)
				.replaceable()
				.noCollission()
				.instabreak()
				.sound(SoundType.HANGING_ROOTS)
				.offsetType(BlockBehaviour.OffsetType.XZ)
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block ROOTED_DIRT = register(
		"rooted_dirt", new RootedDirtBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.ROOTED_DIRT))
	);
	public static final Block MUD = register(
		"mud",
		new MudBlock(
			BlockBehaviour.Properties.ofLegacyCopy(DIRT)
				.mapColor(MapColor.TERRACOTTA_CYAN)
				.isValidSpawn(Blocks::always)
				.isRedstoneConductor(Blocks::always)
				.isViewBlocking(Blocks::always)
				.isSuffocating(Blocks::always)
				.sound(SoundType.MUD)
		)
	);
	public static final Block DEEPSLATE = register(
		"deepslate",
		new RotatedPillarBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.DEEPSLATE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(3.0F, 6.0F)
				.sound(SoundType.DEEPSLATE)
		)
	);
	public static final Block COBBLED_DEEPSLATE = register("cobbled_deepslate", new Block(BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE).strength(3.5F, 6.0F)));
	public static final Block COBBLED_DEEPSLATE_STAIRS = register("cobbled_deepslate_stairs", legacyStair(COBBLED_DEEPSLATE));
	public static final Block COBBLED_DEEPSLATE_SLAB = register("cobbled_deepslate_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE)));
	public static final Block COBBLED_DEEPSLATE_WALL = register(
		"cobbled_deepslate_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).forceSolidOn())
	);
	public static final Block POLISHED_DEEPSLATE = register(
		"polished_deepslate", new Block(BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).sound(SoundType.POLISHED_DEEPSLATE))
	);
	public static final Block POLISHED_DEEPSLATE_STAIRS = register("polished_deepslate_stairs", legacyStair(POLISHED_DEEPSLATE));
	public static final Block POLISHED_DEEPSLATE_SLAB = register(
		"polished_deepslate_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_DEEPSLATE))
	);
	public static final Block POLISHED_DEEPSLATE_WALL = register(
		"polished_deepslate_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(POLISHED_DEEPSLATE).forceSolidOn())
	);
	public static final Block DEEPSLATE_TILES = register(
		"deepslate_tiles", new Block(BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).sound(SoundType.DEEPSLATE_TILES))
	);
	public static final Block DEEPSLATE_TILE_STAIRS = register("deepslate_tile_stairs", legacyStair(DEEPSLATE_TILES));
	public static final Block DEEPSLATE_TILE_SLAB = register("deepslate_tile_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_TILES)));
	public static final Block DEEPSLATE_TILE_WALL = register(
		"deepslate_tile_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_TILES).forceSolidOn())
	);
	public static final Block DEEPSLATE_BRICKS = register(
		"deepslate_bricks", new Block(BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).sound(SoundType.DEEPSLATE_BRICKS))
	);
	public static final Block DEEPSLATE_BRICK_STAIRS = register("deepslate_brick_stairs", legacyStair(DEEPSLATE_BRICKS));
	public static final Block DEEPSLATE_BRICK_SLAB = register("deepslate_brick_slab", new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_BRICKS)));
	public static final Block DEEPSLATE_BRICK_WALL = register(
		"deepslate_brick_wall", new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_BRICKS).forceSolidOn())
	);
	public static final Block CHISELED_DEEPSLATE = register(
		"chiseled_deepslate", new Block(BlockBehaviour.Properties.ofLegacyCopy(COBBLED_DEEPSLATE).sound(SoundType.DEEPSLATE_BRICKS))
	);
	public static final Block CRACKED_DEEPSLATE_BRICKS = register("cracked_deepslate_bricks", new Block(BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_BRICKS)));
	public static final Block CRACKED_DEEPSLATE_TILES = register("cracked_deepslate_tiles", new Block(BlockBehaviour.Properties.ofLegacyCopy(DEEPSLATE_TILES)));
	public static final Block INFESTED_DEEPSLATE = register(
		"infested_deepslate", new InfestedRotatedPillarBlock(DEEPSLATE, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.DEEPSLATE))
	);
	public static final Block SMOOTH_BASALT = register("smooth_basalt", new Block(BlockBehaviour.Properties.ofLegacyCopy(BASALT)));
	public static final Block RAW_IRON_BLOCK = register(
		"raw_iron_block",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.RAW_IRON).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)
		)
	);
	public static final Block RAW_COPPER_BLOCK = register(
		"raw_copper_block",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)
		)
	);
	public static final Block RAW_GOLD_BLOCK = register(
		"raw_gold_block",
		new Block(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F))
	);
	public static final Block POTTED_AZALEA = register("potted_azalea_bush", flowerPot(AZALEA));
	public static final Block POTTED_FLOWERING_AZALEA = register("potted_flowering_azalea_bush", flowerPot(FLOWERING_AZALEA));
	public static final Block OCHRE_FROGLIGHT = register(
		"ochre_froglight",
		new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.3F).lightLevel(blockStatex -> 15).sound(SoundType.FROGLIGHT))
	);
	public static final Block VERDANT_FROGLIGHT = register(
		"verdant_froglight",
		new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GLOW_LICHEN).strength(0.3F).lightLevel(blockStatex -> 15).sound(SoundType.FROGLIGHT))
	);
	public static final Block PEARLESCENT_FROGLIGHT = register(
		"pearlescent_froglight",
		new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.3F).lightLevel(blockStatex -> 15).sound(SoundType.FROGLIGHT))
	);
	public static final Block FROGSPAWN = register(
		"frogspawn",
		new FrogspawnBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.WATER)
				.instabreak()
				.noOcclusion()
				.noCollission()
				.sound(SoundType.FROGSPAWN)
				.pushReaction(PushReaction.DESTROY)
		)
	);
	public static final Block REINFORCED_DEEPSLATE = register(
		"reinforced_deepslate",
		new Block(
			BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.DEEPSLATE).strength(55.0F, 1200.0F)
		)
	);
	public static final Block DECORATED_POT = register(
		"decorated_pot",
		new DecoratedPotBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).strength(0.0F, 0.0F).pushReaction(PushReaction.DESTROY).noOcclusion())
	);
	public static final Block CRAFTER = register(
		"crafter", new CrafterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3.5F).requiredFeatures(FeatureFlags.UPDATE_1_21))
	);
	public static final Block TRIAL_SPAWNER = register(
		"trial_spawner",
		new TrialSpawnerBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.lightLevel(blockStatex -> ((TrialSpawnerState)blockStatex.getValue(TrialSpawnerBlock.STATE)).lightLevel())
				.strength(50.0F)
				.sound(SoundType.TRIAL_SPAWNER)
				.isViewBlocking(Blocks::never)
				.noOcclusion()
				.requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block VAULT = register(
		"vault",
		new VaultBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.noOcclusion()
				.requiresCorrectToolForDrops()
				.sound(SoundType.VAULT)
				.lightLevel(blockStatex -> ((VaultState)blockStatex.getValue(VaultBlock.STATE)).lightLevel())
				.strength(50.0F)
				.isViewBlocking(Blocks::never)
				.requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);
	public static final Block HEAVY_CORE = register(
		"heavy_core",
		new HeavyCoreBlock(
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.METAL)
				.instrument(NoteBlockInstrument.SNARE)
				.sound(SoundType.HEAVY_CORE)
				.strength(10.0F)
				.pushReaction(PushReaction.NORMAL)
				.explosionResistance(1200.0F)
				.requiredFeatures(FeatureFlags.UPDATE_1_21)
		)
	);

	private static ToIntFunction<BlockState> litBlockEmission(int i) {
		return blockState -> blockState.getValue(BlockStateProperties.LIT) ? i : 0;
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

	private static Block bed(DyeColor dyeColor) {
		return new BedBlock(
			dyeColor,
			BlockBehaviour.Properties.of()
				.mapColor(blockState -> blockState.getValue(BedBlock.PART) == BedPart.FOOT ? dyeColor.getMapColor() : MapColor.WOOL)
				.sound(SoundType.WOOD)
				.strength(0.2F)
				.noOcclusion()
				.ignitedByLava()
				.pushReaction(PushReaction.DESTROY)
		);
	}

	private static Block log(MapColor mapColor, MapColor mapColor2) {
		return new RotatedPillarBlock(
			BlockBehaviour.Properties.of()
				.mapColor(blockState -> blockState.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? mapColor : mapColor2)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F)
				.sound(SoundType.WOOD)
				.ignitedByLava()
		);
	}

	private static Block log(MapColor mapColor, MapColor mapColor2, SoundType soundType) {
		return new RotatedPillarBlock(
			BlockBehaviour.Properties.of()
				.mapColor(blockState -> blockState.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? mapColor : mapColor2)
				.instrument(NoteBlockInstrument.BASS)
				.strength(2.0F)
				.sound(soundType)
				.ignitedByLava()
		);
	}

	private static Block netherStem(MapColor mapColor) {
		return new RotatedPillarBlock(
			BlockBehaviour.Properties.of().mapColor(blockState -> mapColor).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.STEM)
		);
	}

	private static boolean always(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}

	private static boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	private static Block stainedGlass(DyeColor dyeColor) {
		return new StainedGlassBlock(
			dyeColor,
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

	private static Block leaves(SoundType soundType) {
		return new LeavesBlock(
			BlockBehaviour.Properties.of()
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
				.isRedstoneConductor(Blocks::never)
		);
	}

	private static Block shulkerBox(@Nullable DyeColor dyeColor, MapColor mapColor) {
		return new ShulkerBoxBlock(
			dyeColor,
			BlockBehaviour.Properties.of()
				.mapColor(mapColor)
				.forceSolidOn()
				.strength(2.0F)
				.dynamicShape()
				.noOcclusion()
				.isSuffocating(NOT_CLOSED_SHULKER)
				.isViewBlocking(NOT_CLOSED_SHULKER)
				.pushReaction(PushReaction.DESTROY)
		);
	}

	private static Block pistonBase(boolean bl) {
		BlockBehaviour.StatePredicate statePredicate = (blockState, blockGetter, blockPos) -> !(Boolean)blockState.getValue(PistonBaseBlock.EXTENDED);
		return new PistonBaseBlock(
			bl,
			BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.strength(1.5F)
				.isRedstoneConductor(Blocks::never)
				.isSuffocating(statePredicate)
				.isViewBlocking(statePredicate)
				.pushReaction(PushReaction.BLOCK)
		);
	}

	private static Block woodenButton(BlockSetType blockSetType) {
		return new ButtonBlock(blockSetType, 30, BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY));
	}

	private static Block stoneButton() {
		return new ButtonBlock(BlockSetType.STONE, 20, BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY));
	}

	private static Block flowerPot(Block block) {
		return new FlowerPotBlock(block, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY));
	}

	private static Block candle(MapColor mapColor) {
		return new CandleBlock(
			BlockBehaviour.Properties.of()
				.mapColor(mapColor)
				.noOcclusion()
				.strength(0.1F)
				.sound(SoundType.CANDLE)
				.lightLevel(CandleBlock.LIGHT_EMISSION)
				.pushReaction(PushReaction.DESTROY)
		);
	}

	@Deprecated
	private static Block legacyStair(Block block) {
		return new StairBlock(block.defaultBlockState(), BlockBehaviour.Properties.ofLegacyCopy(block));
	}

	private static Block stair(Block block) {
		return new StairBlock(block.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block));
	}

	public static Block register(String string, Block block) {
		return Registry.register(BuiltInRegistries.BLOCK, string, block);
	}

	public static Block register(ResourceKey<Block> resourceKey, Block block) {
		return Registry.register(BuiltInRegistries.BLOCK, resourceKey, block);
	}

	public static void rebuildCache() {
		Block.BLOCK_STATE_REGISTRY.forEach(BlockBehaviour.BlockStateBase::initCache);
	}

	static {
		for (Block block : BuiltInRegistries.BLOCK) {
			for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
				Block.BLOCK_STATE_REGISTRY.add(blockState);
				blockState.initCache();
			}

			block.getLootTable();
		}
	}
}
