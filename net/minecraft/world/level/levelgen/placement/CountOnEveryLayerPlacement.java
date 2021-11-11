/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

@Deprecated
public class CountOnEveryLayerPlacement
extends PlacementModifier {
    public static final Codec<CountOnEveryLayerPlacement> CODEC = ((MapCodec)IntProvider.codec(0, 256).fieldOf("count")).xmap(CountOnEveryLayerPlacement::new, countOnEveryLayerPlacement -> countOnEveryLayerPlacement.count).codec();
    private final IntProvider count;

    private CountOnEveryLayerPlacement(IntProvider intProvider) {
        this.count = intProvider;
    }

    public static CountOnEveryLayerPlacement of(IntProvider intProvider) {
        return new CountOnEveryLayerPlacement(intProvider);
    }

    public static CountOnEveryLayerPlacement of(int i) {
        return CountOnEveryLayerPlacement.of(ConstantInt.of(i));
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
        boolean bl;
        Stream.Builder<BlockPos> builder = Stream.builder();
        int i = 0;
        do {
            bl = false;
            for (int j = 0; j < this.count.sample(random); ++j) {
                int l;
                int m;
                int k = random.nextInt(16) + blockPos.getX();
                int n = CountOnEveryLayerPlacement.findOnGroundYPosition(placementContext, k, m = placementContext.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l = random.nextInt(16) + blockPos.getZ()), l, i);
                if (n == Integer.MAX_VALUE) continue;
                builder.add(new BlockPos(k, n, l));
                bl = true;
            }
            ++i;
        } while (bl);
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.COUNT_ON_EVERY_LAYER;
    }

    private static int findOnGroundYPosition(PlacementContext placementContext, int i, int j, int k, int l) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, j, k);
        int m = 0;
        BlockState blockState = placementContext.getBlockState(mutableBlockPos);
        for (int n = j; n >= placementContext.getMinBuildHeight() + 1; --n) {
            mutableBlockPos.setY(n - 1);
            BlockState blockState2 = placementContext.getBlockState(mutableBlockPos);
            if (!CountOnEveryLayerPlacement.isEmpty(blockState2) && CountOnEveryLayerPlacement.isEmpty(blockState) && !blockState2.is(Blocks.BEDROCK)) {
                if (m == l) {
                    return mutableBlockPos.getY() + 1;
                }
                ++m;
            }
            blockState = blockState2;
        }
        return Integer.MAX_VALUE;
    }

    private static boolean isEmpty(BlockState blockState) {
        return blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.LAVA);
    }
}

