/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;

public class EndGatewayFeature
extends Feature<EndGatewayConfiguration> {
    public EndGatewayFeature(Function<Dynamic<?>, ? extends EndGatewayConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, EndGatewayConfiguration endGatewayConfiguration) {
        for (BlockPos blockPos22 : BlockPos.betweenClosed(blockPos.offset(-1, -2, -1), blockPos.offset(1, 2, 1))) {
            boolean bl4;
            boolean bl = blockPos22.getX() == blockPos.getX();
            boolean bl2 = blockPos22.getY() == blockPos.getY();
            boolean bl3 = blockPos22.getZ() == blockPos.getZ();
            boolean bl5 = bl4 = Math.abs(blockPos22.getY() - blockPos.getY()) == 2;
            if (bl && bl2 && bl3) {
                BlockPos blockPos3 = blockPos22.immutable();
                this.setBlock(levelAccessor, blockPos3, Blocks.END_GATEWAY.defaultBlockState());
                endGatewayConfiguration.getExit().ifPresent(blockPos2 -> {
                    BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos3);
                    if (blockEntity instanceof TheEndGatewayBlockEntity) {
                        TheEndGatewayBlockEntity theEndGatewayBlockEntity = (TheEndGatewayBlockEntity)blockEntity;
                        theEndGatewayBlockEntity.setExitPosition((BlockPos)blockPos2, endGatewayConfiguration.isExitExact());
                        blockEntity.setChanged();
                    }
                });
                continue;
            }
            if (bl2) {
                this.setBlock(levelAccessor, blockPos22, Blocks.AIR.defaultBlockState());
                continue;
            }
            if (bl4 && bl && bl3) {
                this.setBlock(levelAccessor, blockPos22, Blocks.BEDROCK.defaultBlockState());
                continue;
            }
            if (!bl && !bl3 || bl4) {
                this.setBlock(levelAccessor, blockPos22, Blocks.AIR.defaultBlockState());
                continue;
            }
            this.setBlock(levelAccessor, blockPos22, Blocks.BEDROCK.defaultBlockState());
        }
        return true;
    }
}

