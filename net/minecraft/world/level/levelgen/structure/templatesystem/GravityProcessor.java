/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class GravityProcessor
extends StructureProcessor {
    public static final Codec<GravityProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Heightmap.Types.CODEC.fieldOf("heightmap")).withDefault(Heightmap.Types.WORLD_SURFACE_WG).forGetter(gravityProcessor -> gravityProcessor.heightmap), ((MapCodec)Codec.INT.fieldOf("offset")).withDefault(0).forGetter(gravityProcessor -> gravityProcessor.offset)).apply((Applicative<GravityProcessor, ?>)instance, GravityProcessor::new));
    private final Heightmap.Types heightmap;
    private final int offset;

    public GravityProcessor(Heightmap.Types types, int i) {
        this.heightmap = types;
        this.offset = i;
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Heightmap.Types types = levelReader instanceof ServerLevel ? (this.heightmap == Heightmap.Types.WORLD_SURFACE_WG ? Heightmap.Types.WORLD_SURFACE : (this.heightmap == Heightmap.Types.OCEAN_FLOOR_WG ? Heightmap.Types.OCEAN_FLOOR : this.heightmap)) : this.heightmap;
        int i = levelReader.getHeight(types, structureBlockInfo2.pos.getX(), structureBlockInfo2.pos.getZ()) + this.offset;
        int j = structureBlockInfo.pos.getY();
        return new StructureTemplate.StructureBlockInfo(new BlockPos(structureBlockInfo2.pos.getX(), i + j, structureBlockInfo2.pos.getZ()), structureBlockInfo2.state, structureBlockInfo2.nbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.GRAVITY;
    }
}

