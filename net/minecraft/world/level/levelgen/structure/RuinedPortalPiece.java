/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuinedPortalPiece
extends TemplateStructurePiece {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation templateLocation;
    private final Rotation rotation;
    private final Mirror mirror;
    private final VerticalPlacement verticalPlacement;
    private final Properties properties;

    public RuinedPortalPiece(BlockPos blockPos, VerticalPlacement verticalPlacement, Properties properties, ResourceLocation resourceLocation, StructureTemplate structureTemplate, Rotation rotation, Mirror mirror, BlockPos blockPos2) {
        super(StructurePieceType.RUINED_PORTAL, 0);
        this.templatePosition = blockPos;
        this.templateLocation = resourceLocation;
        this.rotation = rotation;
        this.mirror = mirror;
        this.verticalPlacement = verticalPlacement;
        this.properties = properties;
        this.loadTemplate(structureTemplate, blockPos2);
    }

    public RuinedPortalPiece(ServerLevel serverLevel, CompoundTag compoundTag) {
        super(StructurePieceType.RUINED_PORTAL, compoundTag);
        this.templateLocation = new ResourceLocation(compoundTag.getString("Template"));
        this.rotation = Rotation.valueOf(compoundTag.getString("Rotation"));
        this.mirror = Mirror.valueOf(compoundTag.getString("Mirror"));
        this.verticalPlacement = VerticalPlacement.byName(compoundTag.getString("VerticalPlacement"));
        this.properties = (Properties)Properties.CODEC.parse(new Dynamic<Tag>(NbtOps.INSTANCE, compoundTag.get("Properties"))).getOrThrow(true, LOGGER::error);
        StructureTemplate structureTemplate = serverLevel.getStructureManager().getOrCreate(this.templateLocation);
        this.loadTemplate(structureTemplate, new BlockPos(structureTemplate.getSize().getX() / 2, 0, structureTemplate.getSize().getZ() / 2));
    }

    @Override
    protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
        super.addAdditionalSaveData(serverLevel, compoundTag);
        compoundTag.putString("Template", this.templateLocation.toString());
        compoundTag.putString("Rotation", this.rotation.name());
        compoundTag.putString("Mirror", this.mirror.name());
        compoundTag.putString("VerticalPlacement", this.verticalPlacement.getName());
        Properties.CODEC.encodeStart(NbtOps.INSTANCE, this.properties).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("Properties", (Tag)tag));
    }

    private void loadTemplate(StructureTemplate structureTemplate, BlockPos blockPos) {
        BlockIgnoreProcessor blockIgnoreProcessor = this.properties.airPocket ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
        ArrayList<ProcessorRule> list = Lists.newArrayList();
        list.add(RuinedPortalPiece.getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3f, Blocks.AIR));
        list.add(this.getLavaProcessorRule());
        if (!this.properties.cold) {
            list.add(RuinedPortalPiece.getBlockReplaceRule(Blocks.NETHERRACK, 0.07f, Blocks.MAGMA_BLOCK));
        }
        StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setRotation(this.rotation).setMirror(this.mirror).setRotationPivot(blockPos).addProcessor(blockIgnoreProcessor).addProcessor(new RuleProcessor(list)).addProcessor(new BlockAgeProcessor(this.properties.mossiness)).addProcessor(new LavaSubmergedBlockProcessor());
        if (this.properties.replaceWithBlackstone) {
            structurePlaceSettings.addProcessor(BlackstoneReplaceProcessor.INSTANCE);
        }
        this.setup(structureTemplate, this.templatePosition, structurePlaceSettings);
    }

    private ProcessorRule getLavaProcessorRule() {
        if (this.verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR) {
            return RuinedPortalPiece.getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
        }
        if (this.properties.cold) {
            return RuinedPortalPiece.getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK);
        }
        return RuinedPortalPiece.getBlockReplaceRule(Blocks.LAVA, 0.2f, Blocks.MAGMA_BLOCK);
    }

    @Override
    public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos2) {
        if (!boundingBox.isInside(this.templatePosition)) {
            return true;
        }
        boundingBox.expand(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
        boolean bl = super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos2);
        this.spreadNetherrack(random, worldGenLevel);
        this.addNetherrackDripColumnsBelowPortal(random, worldGenLevel);
        if (this.properties.vines || this.properties.overgrown) {
            BlockPos.betweenClosedStream(this.getBoundingBox()).forEach(blockPos -> {
                if (this.properties.vines) {
                    this.maybeAddVines(random, worldGenLevel, (BlockPos)blockPos);
                }
                if (this.properties.overgrown) {
                    this.maybeAddLeavesAbove(random, worldGenLevel, (BlockPos)blockPos);
                }
            });
        }
        return bl;
    }

    @Override
    protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
    }

    private void maybeAddVines(Random random, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (blockState.isAir() || blockState.is(Blocks.VINE)) {
            return;
        }
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
        if (!blockState2.isAir()) {
            return;
        }
        if (!Block.isFaceFull(blockState.getCollisionShape(levelAccessor, blockPos), direction)) {
            return;
        }
        BooleanProperty booleanProperty = VineBlock.getPropertyForFace(direction.getOpposite());
        levelAccessor.setBlock(blockPos2, (BlockState)Blocks.VINE.defaultBlockState().setValue(booleanProperty, true), 3);
    }

    private void maybeAddLeavesAbove(Random random, LevelAccessor levelAccessor, BlockPos blockPos) {
        if (random.nextFloat() < 0.5f && levelAccessor.getBlockState(blockPos).is(Blocks.NETHERRACK) && levelAccessor.getBlockState(blockPos.above()).isAir()) {
            levelAccessor.setBlock(blockPos.above(), (BlockState)Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 3);
        }
    }

    private void addNetherrackDripColumnsBelowPortal(Random random, LevelAccessor levelAccessor) {
        for (int i = this.boundingBox.x0 + 1; i < this.boundingBox.x1; ++i) {
            for (int j = this.boundingBox.z0 + 1; j < this.boundingBox.z1; ++j) {
                BlockPos blockPos = new BlockPos(i, this.boundingBox.y0, j);
                if (!levelAccessor.getBlockState(blockPos).is(Blocks.NETHERRACK)) continue;
                this.addNetherrackDripColumn(random, levelAccessor, blockPos.below());
            }
        }
    }

    private void addNetherrackDripColumn(Random random, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        this.placeNetherrackOrMagma(random, levelAccessor, mutableBlockPos);
        for (int i = 8; i > 0 && random.nextFloat() < 0.5f; --i) {
            mutableBlockPos.move(Direction.DOWN);
            this.placeNetherrackOrMagma(random, levelAccessor, mutableBlockPos);
        }
    }

    private void spreadNetherrack(Random random, LevelAccessor levelAccessor) {
        boolean bl = this.verticalPlacement == VerticalPlacement.ON_LAND_SURFACE || this.verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR;
        BlockPos blockPos = this.boundingBox.getCenter();
        int i = blockPos.getX();
        int j = blockPos.getZ();
        float[] fs = new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f, 0.9f, 0.8f, 0.7f, 0.6f, 0.4f, 0.2f};
        int k = fs.length;
        int l = (this.boundingBox.getXSpan() + this.boundingBox.getZSpan()) / 2;
        int m = random.nextInt(Math.max(1, 8 - l / 2));
        int n = 3;
        BlockPos.MutableBlockPos mutableBlockPos = BlockPos.ZERO.mutable();
        for (int o = i - k; o <= i + k; ++o) {
            for (int p = j - k; p <= j + k; ++p) {
                int q = Math.abs(o - i) + Math.abs(p - j);
                int r = Math.max(0, q + m);
                if (r >= k) continue;
                float f = fs[r];
                if (!(random.nextDouble() < (double)f)) continue;
                int s = RuinedPortalPiece.getSurfaceY(levelAccessor, o, p, this.verticalPlacement);
                int t = bl ? s : Math.min(this.boundingBox.y0, s);
                mutableBlockPos.set(o, t, p);
                if (Math.abs(t - this.boundingBox.y0) > 3 || !this.canBlockBeReplacedByNetherrackOrMagma(levelAccessor, mutableBlockPos)) continue;
                this.placeNetherrackOrMagma(random, levelAccessor, mutableBlockPos);
                if (this.properties.overgrown) {
                    this.maybeAddLeavesAbove(random, levelAccessor, mutableBlockPos);
                }
                this.addNetherrackDripColumn(random, levelAccessor, (BlockPos)mutableBlockPos.below());
            }
        }
    }

    private boolean canBlockBeReplacedByNetherrackOrMagma(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        return !blockState.is(Blocks.AIR) && !blockState.is(Blocks.OBSIDIAN) && !blockState.is(Blocks.CHEST) && (this.verticalPlacement == VerticalPlacement.IN_NETHER || !blockState.is(Blocks.LAVA));
    }

    private void placeNetherrackOrMagma(Random random, LevelAccessor levelAccessor, BlockPos blockPos) {
        if (!this.properties.cold && random.nextFloat() < 0.07f) {
            levelAccessor.setBlock(blockPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        } else {
            levelAccessor.setBlock(blockPos, Blocks.NETHERRACK.defaultBlockState(), 3);
        }
    }

    private static int getSurfaceY(LevelAccessor levelAccessor, int i, int j, VerticalPlacement verticalPlacement) {
        return levelAccessor.getHeight(RuinedPortalPiece.getHeightMapType(verticalPlacement), i, j) - 1;
    }

    public static Heightmap.Types getHeightMapType(VerticalPlacement verticalPlacement) {
        return verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
    }

    private static ProcessorRule getBlockReplaceRule(Block block, float f, Block block2) {
        return new ProcessorRule(new RandomBlockMatchTest(block, f), AlwaysTrueTest.INSTANCE, block2.defaultBlockState());
    }

    private static ProcessorRule getBlockReplaceRule(Block block, Block block2) {
        return new ProcessorRule(new BlockMatchTest(block), AlwaysTrueTest.INSTANCE, block2.defaultBlockState());
    }

    public static enum VerticalPlacement {
        ON_LAND_SURFACE("on_land_surface"),
        PARTLY_BURIED("partly_buried"),
        ON_OCEAN_FLOOR("on_ocean_floor"),
        IN_MOUNTAIN("in_mountain"),
        UNDERGROUND("underground"),
        IN_NETHER("in_nether");

        private static final Map<String, VerticalPlacement> BY_NAME;
        private final String name;

        private VerticalPlacement(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        public static VerticalPlacement byName(String string) {
            return BY_NAME.get(string);
        }

        static {
            BY_NAME = Arrays.stream(VerticalPlacement.values()).collect(Collectors.toMap(VerticalPlacement::getName, verticalPlacement -> verticalPlacement));
        }
    }

    public static class Properties {
        public static final Codec<Properties> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("cold")).forGetter(properties -> properties.cold), ((MapCodec)Codec.FLOAT.fieldOf("mossiness")).forGetter(properties -> Float.valueOf(properties.mossiness)), ((MapCodec)Codec.BOOL.fieldOf("air_pocket")).forGetter(properties -> properties.airPocket), ((MapCodec)Codec.BOOL.fieldOf("overgrown")).forGetter(properties -> properties.overgrown), ((MapCodec)Codec.BOOL.fieldOf("vines")).forGetter(properties -> properties.vines), ((MapCodec)Codec.BOOL.fieldOf("replace_with_blackstone")).forGetter(properties -> properties.replaceWithBlackstone)).apply((Applicative<Properties, ?>)instance, Properties::new));
        public boolean cold;
        public float mossiness = 0.2f;
        public boolean airPocket;
        public boolean overgrown;
        public boolean vines;
        public boolean replaceWithBlackstone;

        public Properties() {
        }

        public <T> Properties(boolean bl, float f, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
            this.cold = bl;
            this.mossiness = f;
            this.airPocket = bl2;
            this.overgrown = bl3;
            this.vines = bl4;
            this.replaceWithBlackstone = bl5;
        }
    }
}

