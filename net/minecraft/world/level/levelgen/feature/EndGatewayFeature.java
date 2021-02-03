/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;

public class EndGatewayFeature
extends Feature<EndGatewayConfiguration> {
    public EndGatewayFeature(Codec<EndGatewayConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<EndGatewayConfiguration> featurePlaceContext) {
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        EndGatewayConfiguration endGatewayConfiguration = featurePlaceContext.config();
        for (BlockPos blockPos22 : BlockPos.betweenClosed(blockPos.offset(-1, -2, -1), blockPos.offset(1, 2, 1))) {
            boolean bl4;
            boolean bl = blockPos22.getX() == blockPos.getX();
            boolean bl2 = blockPos22.getY() == blockPos.getY();
            boolean bl3 = blockPos22.getZ() == blockPos.getZ();
            boolean bl5 = bl4 = Math.abs(blockPos22.getY() - blockPos.getY()) == 2;
            if (bl && bl2 && bl3) {
                BlockPos blockPos3 = blockPos22.immutable();
                this.setBlock(worldGenLevel, blockPos3, Blocks.END_GATEWAY.defaultBlockState());
                endGatewayConfiguration.getExit().ifPresent(blockPos2 -> {
                    BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos3);
                    if (blockEntity instanceof TheEndGatewayBlockEntity) {
                        TheEndGatewayBlockEntity theEndGatewayBlockEntity = (TheEndGatewayBlockEntity)blockEntity;
                        theEndGatewayBlockEntity.setExitPosition((BlockPos)blockPos2, endGatewayConfiguration.isExitExact());
                        blockEntity.setChanged();
                    }
                });
                continue;
            }
            if (bl2) {
                this.setBlock(worldGenLevel, blockPos22, Blocks.AIR.defaultBlockState());
                continue;
            }
            if (bl4 && bl && bl3) {
                this.setBlock(worldGenLevel, blockPos22, Blocks.BEDROCK.defaultBlockState());
                continue;
            }
            if (!bl && !bl3 || bl4) {
                this.setBlock(worldGenLevel, blockPos22, Blocks.AIR.defaultBlockState());
                continue;
            }
            this.setBlock(worldGenLevel, blockPos22, Blocks.BEDROCK.defaultBlockState());
        }
        return true;
    }
}

