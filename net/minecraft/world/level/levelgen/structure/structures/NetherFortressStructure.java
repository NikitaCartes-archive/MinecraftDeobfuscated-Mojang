/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;

public class NetherFortressStructure
extends Structure {
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3), new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4), new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5), new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5), new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)});
    public static final Codec<NetherFortressStructure> CODEC = NetherFortressStructure.simpleCodec(NetherFortressStructure::new);

    public NetherFortressStructure(Structure.StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        ChunkPos chunkPos = generationContext.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 64, chunkPos.getMinBlockZ());
        return Optional.of(new Structure.GenerationStub(blockPos, structurePiecesBuilder -> NetherFortressStructure.generatePieces(structurePiecesBuilder, generationContext)));
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        NetherFortressPieces.StartPiece startPiece = new NetherFortressPieces.StartPiece(generationContext.random(), generationContext.chunkPos().getBlockX(2), generationContext.chunkPos().getBlockZ(2));
        structurePiecesBuilder.addPiece(startPiece);
        startPiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
        List<StructurePiece> list = startPiece.pendingChildren;
        while (!list.isEmpty()) {
            int i = generationContext.random().nextInt(list.size());
            StructurePiece structurePiece = list.remove(i);
            structurePiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
        }
        structurePiecesBuilder.moveInsideHeights(generationContext.random(), 48, 70);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.FORTRESS;
    }
}

