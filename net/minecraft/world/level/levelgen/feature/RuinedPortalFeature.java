/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalFeature
extends StructureFeature<RuinedPortalConfiguration> {
    private static final String[] STRUCTURE_LOCATION_PORTALS = new String[]{"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
    private static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = new String[]{"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};
    private static final float PROBABILITY_OF_GIANT_PORTAL = 0.05f;
    private static final float PROBABILITY_OF_AIR_POCKET = 0.5f;
    private static final float PROBABILITY_OF_UNDERGROUND = 0.5f;
    private static final float UNDERWATER_MOSSINESS = 0.8f;
    private static final float JUNGLE_MOSSINESS = 0.8f;
    private static final float SWAMP_MOSSINESS = 0.5f;
    private static final int MIN_Y_INDEX = 15;

    public RuinedPortalFeature(Codec<RuinedPortalConfiguration> codec) {
        super(codec, RuinedPortalFeature::generatePieces);
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, RuinedPortalConfiguration ruinedPortalConfiguration, PieceGenerator.Context context) {
        RuinedPortalPiece.VerticalPlacement verticalPlacement;
        RuinedPortalPiece.Properties properties = new RuinedPortalPiece.Properties();
        if (ruinedPortalConfiguration.portalType == Type.DESERT) {
            verticalPlacement = RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED;
            properties.airPocket = false;
            properties.mossiness = 0.0f;
        } else if (ruinedPortalConfiguration.portalType == Type.JUNGLE) {
            verticalPlacement = RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
            properties.airPocket = context.random().nextFloat() < 0.5f;
            properties.mossiness = 0.8f;
            properties.overgrown = true;
            properties.vines = true;
        } else if (ruinedPortalConfiguration.portalType == Type.SWAMP) {
            verticalPlacement = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
            properties.airPocket = false;
            properties.mossiness = 0.5f;
            properties.vines = true;
        } else if (ruinedPortalConfiguration.portalType == Type.MOUNTAIN) {
            bl = context.random().nextFloat() < 0.5f;
            verticalPlacement = bl ? RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
            properties.airPocket = bl || context.random().nextFloat() < 0.5f;
        } else if (ruinedPortalConfiguration.portalType == Type.OCEAN) {
            verticalPlacement = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
            properties.airPocket = false;
            properties.mossiness = 0.8f;
        } else if (ruinedPortalConfiguration.portalType == Type.NETHER) {
            verticalPlacement = RuinedPortalPiece.VerticalPlacement.IN_NETHER;
            properties.airPocket = context.random().nextFloat() < 0.5f;
            properties.mossiness = 0.0f;
            properties.replaceWithBlackstone = true;
        } else {
            bl = context.random().nextFloat() < 0.5f;
            verticalPlacement = bl ? RuinedPortalPiece.VerticalPlacement.UNDERGROUND : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
            properties.airPocket = bl || context.random().nextFloat() < 0.5f;
        }
        ResourceLocation resourceLocation = context.random().nextFloat() < 0.05f ? new ResourceLocation(STRUCTURE_LOCATION_GIANT_PORTALS[context.random().nextInt(STRUCTURE_LOCATION_GIANT_PORTALS.length)]) : new ResourceLocation(STRUCTURE_LOCATION_PORTALS[context.random().nextInt(STRUCTURE_LOCATION_PORTALS.length)]);
        StructureTemplate structureTemplate = context.structureManager().getOrCreate(resourceLocation);
        Rotation rotation = Util.getRandom(Rotation.values(), (Random)context.random());
        Mirror mirror = context.random().nextFloat() < 0.5f ? Mirror.NONE : Mirror.FRONT_BACK;
        BlockPos blockPos = new BlockPos(structureTemplate.getSize().getX() / 2, 0, structureTemplate.getSize().getZ() / 2);
        BlockPos blockPos2 = context.chunkPos().getWorldPosition();
        BoundingBox boundingBox = structureTemplate.getBoundingBox(blockPos2, rotation, blockPos, mirror);
        BlockPos blockPos3 = boundingBox.getCenter();
        int i = blockPos3.getX();
        int j = blockPos3.getZ();
        int k = context.chunkGenerator().getBaseHeight(i, j, RuinedPortalPiece.getHeightMapType(verticalPlacement), context.heightAccessor()) - 1;
        int l = RuinedPortalFeature.findSuitableY(context.random(), context.chunkGenerator(), verticalPlacement, properties.airPocket, k, boundingBox.getYSpan(), boundingBox, context.heightAccessor());
        BlockPos blockPos4 = new BlockPos(blockPos2.getX(), l, blockPos2.getZ());
        if (!context.validBiome().test(context.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(blockPos4.getX()), QuartPos.fromBlock(blockPos4.getY()), QuartPos.fromBlock(blockPos4.getZ())))) {
            return;
        }
        if (ruinedPortalConfiguration.portalType == Type.MOUNTAIN || ruinedPortalConfiguration.portalType == Type.OCEAN || ruinedPortalConfiguration.portalType == Type.STANDARD) {
            properties.cold = RuinedPortalFeature.isCold(blockPos4, context.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(blockPos4.getX()), QuartPos.fromBlock(blockPos4.getY()), QuartPos.fromBlock(blockPos4.getZ())));
        }
        structurePiecesBuilder.addPiece(new RuinedPortalPiece(context.structureManager(), blockPos4, verticalPlacement, properties, resourceLocation, structureTemplate, rotation, mirror, blockPos));
    }

    private static boolean isCold(BlockPos blockPos, Biome biome) {
        return biome.getTemperature(blockPos) < 0.15f;
    }

    private static int findSuitableY(Random random, ChunkGenerator chunkGenerator, RuinedPortalPiece.VerticalPlacement verticalPlacement, boolean bl, int i, int j, BoundingBox boundingBox, LevelHeightAccessor levelHeightAccessor) {
        int n;
        int k = levelHeightAccessor.getMinBuildHeight() + 15;
        if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
            l = bl ? Mth.randomBetweenInclusive(random, 32, 100) : (random.nextFloat() < 0.5f ? Mth.randomBetweenInclusive(random, 27, 29) : Mth.randomBetweenInclusive(random, 29, 100));
        } else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
            m = i - j;
            l = RuinedPortalFeature.getRandomWithinInterval(random, 70, m);
        } else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
            m = i - j;
            l = RuinedPortalFeature.getRandomWithinInterval(random, k, m);
        } else {
            l = verticalPlacement == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED ? i - j + Mth.randomBetweenInclusive(random, 2, 8) : i;
        }
        ImmutableList<BlockPos> list = ImmutableList.of(new BlockPos(boundingBox.minX(), 0, boundingBox.minZ()), new BlockPos(boundingBox.maxX(), 0, boundingBox.minZ()), new BlockPos(boundingBox.minX(), 0, boundingBox.maxZ()), new BlockPos(boundingBox.maxX(), 0, boundingBox.maxZ()));
        List list2 = list.stream().map(blockPos -> chunkGenerator.getBaseColumn(blockPos.getX(), blockPos.getZ(), levelHeightAccessor)).collect(Collectors.toList());
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

    public static enum Type implements StringRepresentable
    {
        STANDARD("standard"),
        DESERT("desert"),
        JUNGLE("jungle"),
        SWAMP("swamp"),
        MOUNTAIN("mountain"),
        OCEAN("ocean"),
        NETHER("nether");

        public static final Codec<Type> CODEC;
        private static final Map<String, Type> BY_NAME;
        private final String name;

        private Type(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        public static Type byName(String string) {
            return BY_NAME.get(string);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Type::values, Type::byName);
            BY_NAME = Arrays.stream(Type.values()).collect(Collectors.toMap(Type::getName, type -> type));
        }
    }
}

