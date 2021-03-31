/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class FeaturePoolElement
extends StructurePoolElement {
    public static final Codec<FeaturePoolElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConfiguredFeature.CODEC.fieldOf("feature")).forGetter(featurePoolElement -> featurePoolElement.feature), FeaturePoolElement.projectionCodec()).apply((Applicative<FeaturePoolElement, ?>)instance, FeaturePoolElement::new));
    private final Supplier<ConfiguredFeature<?, ?>> feature;
    private final CompoundTag defaultJigsawNBT;

    protected FeaturePoolElement(Supplier<ConfiguredFeature<?, ?>> supplier, StructureTemplatePool.Projection projection) {
        super(projection);
        this.feature = supplier;
        this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
    }

    private CompoundTag fillDefaultJigsawNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("name", "minecraft:bottom");
        compoundTag.putString("final_state", "minecraft:air");
        compoundTag.putString("pool", "minecraft:empty");
        compoundTag.putString("target", "minecraft:empty");
        compoundTag.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
        return compoundTag;
    }

    @Override
    public Vec3i getSize(StructureManager structureManager, Rotation rotation) {
        return Vec3i.ZERO;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random) {
        ArrayList<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
        list.add(new StructureTemplate.StructureBlockInfo(blockPos, (BlockState)Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)), this.defaultJigsawNBT));
        return list;
    }

    @Override
    public BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation) {
        Vec3i vec3i = this.getSize(structureManager, rotation);
        return new BoundingBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + vec3i.getX(), blockPos.getY() + vec3i.getY(), blockPos.getZ() + vec3i.getZ());
    }

    @Override
    public boolean place(StructureManager structureManager, WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, Rotation rotation, BoundingBox boundingBox, Random random, boolean bl) {
        return this.feature.get().place(worldGenLevel, chunkGenerator, random, blockPos);
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.FEATURE;
    }

    public String toString() {
        return "Feature[" + Registry.FEATURE.getKey((Feature<?>)this.feature.get().feature()) + "]";
    }
}

