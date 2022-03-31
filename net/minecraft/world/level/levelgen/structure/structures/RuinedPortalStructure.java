/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalStructure
extends Structure {
    private static final String[] STRUCTURE_LOCATION_PORTALS = new String[]{"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
    private static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = new String[]{"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};
    private static final float PROBABILITY_OF_GIANT_PORTAL = 0.05f;
    private static final int MIN_Y_INDEX = 15;
    private final List<Setup> setups;
    public static final Codec<RuinedPortalStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(RuinedPortalStructure.settingsCodec(instance), ((MapCodec)ExtraCodecs.nonEmptyList(Setup.CODEC.listOf()).fieldOf("setups")).forGetter(ruinedPortalStructure -> ruinedPortalStructure.setups)).apply((Applicative<RuinedPortalStructure, ?>)instance, RuinedPortalStructure::new));

    public RuinedPortalStructure(Structure.StructureSettings structureSettings, List<Setup> list) {
        super(structureSettings);
        this.setups = list;
    }

    public RuinedPortalStructure(Structure.StructureSettings structureSettings, Setup setup) {
        this(structureSettings, List.of(setup));
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        RuinedPortalPiece.Properties properties = new RuinedPortalPiece.Properties();
        WorldgenRandom worldgenRandom = generationContext.random();
        Setup setup = null;
        if (this.setups.size() > 1) {
            float f = 0.0f;
            for (Setup setup2 : this.setups) {
                f += setup2.weight();
            }
            float g = worldgenRandom.nextFloat();
            for (Setup setup3 : this.setups) {
                if (!((g -= setup3.weight() / f) < 0.0f)) continue;
                setup = setup3;
                break;
            }
        } else {
            setup = this.setups.get(0);
        }
        if (setup == null) {
            throw new IllegalStateException();
        }
        Setup setup4 = setup;
        properties.airPocket = RuinedPortalStructure.sample(worldgenRandom, setup4.airPocketProbability());
        properties.mossiness = setup4.mossiness();
        properties.overgrown = setup4.overgrown();
        properties.vines = setup4.vines();
        properties.replaceWithBlackstone = setup4.replaceWithBlackstone();
        ResourceLocation resourceLocation = worldgenRandom.nextFloat() < 0.05f ? new ResourceLocation(STRUCTURE_LOCATION_GIANT_PORTALS[worldgenRandom.nextInt(STRUCTURE_LOCATION_GIANT_PORTALS.length)]) : new ResourceLocation(STRUCTURE_LOCATION_PORTALS[worldgenRandom.nextInt(STRUCTURE_LOCATION_PORTALS.length)]);
        StructureTemplate structureTemplate = generationContext.structureTemplateManager().getOrCreate(resourceLocation);
        Rotation rotation = Util.getRandom(Rotation.values(), (Random)worldgenRandom);
        Mirror mirror = worldgenRandom.nextFloat() < 0.5f ? Mirror.NONE : Mirror.FRONT_BACK;
        BlockPos blockPos = new BlockPos(structureTemplate.getSize().getX() / 2, 0, structureTemplate.getSize().getZ() / 2);
        ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
        LevelHeightAccessor levelHeightAccessor = generationContext.heightAccessor();
        RandomState randomState = generationContext.randomState();
        BlockPos blockPos2 = generationContext.chunkPos().getWorldPosition();
        BoundingBox boundingBox = structureTemplate.getBoundingBox(blockPos2, rotation, blockPos, mirror);
        BlockPos blockPos3 = boundingBox.getCenter();
        int i = chunkGenerator.getBaseHeight(blockPos3.getX(), blockPos3.getZ(), RuinedPortalPiece.getHeightMapType(setup4.placement()), levelHeightAccessor, randomState) - 1;
        int j = RuinedPortalStructure.findSuitableY(worldgenRandom, chunkGenerator, setup4.placement(), properties.airPocket, i, boundingBox.getYSpan(), boundingBox, levelHeightAccessor, randomState);
        BlockPos blockPos4 = new BlockPos(blockPos2.getX(), j, blockPos2.getZ());
        return Optional.of(new Structure.GenerationStub(blockPos4, structurePiecesBuilder -> {
            if (setup4.canBeCold()) {
                properties.cold = RuinedPortalStructure.isCold(blockPos4, generationContext.chunkGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockPos4.getX()), QuartPos.fromBlock(blockPos4.getY()), QuartPos.fromBlock(blockPos4.getZ()), randomState.sampler()));
            }
            structurePiecesBuilder.addPiece(new RuinedPortalPiece(generationContext.structureTemplateManager(), blockPos4, setup4.placement(), properties, resourceLocation, structureTemplate, rotation, mirror, blockPos));
        }));
    }

    private static boolean sample(WorldgenRandom worldgenRandom, float f) {
        if (f == 0.0f) {
            return false;
        }
        if (f == 1.0f) {
            return true;
        }
        return worldgenRandom.nextFloat() < f;
    }

    private static boolean isCold(BlockPos blockPos, Holder<Biome> holder) {
        return holder.value().coldEnoughToSnow(blockPos);
    }

    private static int findSuitableY(Random random, ChunkGenerator chunkGenerator, RuinedPortalPiece.VerticalPlacement verticalPlacement, boolean bl, int i, int j, BoundingBox boundingBox, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        int n;
        int k = levelHeightAccessor.getMinBuildHeight() + 15;
        if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
            l = bl ? Mth.randomBetweenInclusive(random, 32, 100) : (random.nextFloat() < 0.5f ? Mth.randomBetweenInclusive(random, 27, 29) : Mth.randomBetweenInclusive(random, 29, 100));
        } else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
            m = i - j;
            l = RuinedPortalStructure.getRandomWithinInterval(random, 70, m);
        } else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
            m = i - j;
            l = RuinedPortalStructure.getRandomWithinInterval(random, k, m);
        } else {
            l = verticalPlacement == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED ? i - j + Mth.randomBetweenInclusive(random, 2, 8) : i;
        }
        ImmutableList<BlockPos> list = ImmutableList.of(new BlockPos(boundingBox.minX(), 0, boundingBox.minZ()), new BlockPos(boundingBox.maxX(), 0, boundingBox.minZ()), new BlockPos(boundingBox.minX(), 0, boundingBox.maxZ()), new BlockPos(boundingBox.maxX(), 0, boundingBox.maxZ()));
        List list2 = list.stream().map(blockPos -> chunkGenerator.getBaseColumn(blockPos.getX(), blockPos.getZ(), levelHeightAccessor, randomState)).collect(Collectors.toList());
        Heightmap.Types types = verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
        block0: for (n = l; n > k; --n) {
            int o = 0;
            for (NoiseColumn noiseColumn : list2) {
                BlockState blockState = noiseColumn.getBlock(n);
                if (!types.isOpaque().test(blockState) || ++o != 3) continue;
                break block0;
            }
        }
        return n;
    }

    private static int getRandomWithinInterval(Random random, int i, int j) {
        if (i < j) {
            return Mth.randomBetweenInclusive(random, i, j);
        }
        return j;
    }

    @Override
    public StructureType<?> type() {
        return StructureType.RUINED_PORTAL;
    }

    public record Setup(RuinedPortalPiece.VerticalPlacement placement, float airPocketProbability, float mossiness, boolean overgrown, boolean vines, boolean canBeCold, boolean replaceWithBlackstone, float weight) {
        public static final Codec<Setup> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RuinedPortalPiece.VerticalPlacement.CODEC.fieldOf("placement")).forGetter(Setup::placement), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("air_pocket_probability")).forGetter(Setup::airPocketProbability), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("mossiness")).forGetter(Setup::mossiness), ((MapCodec)Codec.BOOL.fieldOf("overgrown")).forGetter(Setup::overgrown), ((MapCodec)Codec.BOOL.fieldOf("vines")).forGetter(Setup::vines), ((MapCodec)Codec.BOOL.fieldOf("can_be_cold")).forGetter(Setup::canBeCold), ((MapCodec)Codec.BOOL.fieldOf("replace_with_blackstone")).forGetter(Setup::replaceWithBlackstone), ((MapCodec)ExtraCodecs.POSITIVE_FLOAT.fieldOf("weight")).forGetter(Setup::weight)).apply((Applicative<Setup, ?>)instance, Setup::new));
    }
}

