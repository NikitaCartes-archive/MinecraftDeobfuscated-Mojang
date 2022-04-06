/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MultifaceSpreader {
    public static final SpreadType[] DEFAULT_SPREAD_ORDER = new SpreadType[]{SpreadType.SAME_POSITION, SpreadType.SAME_PLANE, SpreadType.WRAP_AROUND};
    private final SpreadConfig config;

    public MultifaceSpreader(MultifaceBlock multifaceBlock) {
        this(new DefaultSpreaderConfig(multifaceBlock));
    }

    public MultifaceSpreader(SpreadConfig spreadConfig) {
        this.config = spreadConfig;
    }

    public boolean canSpreadInAnyDirection(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return Direction.stream().anyMatch(direction2 -> this.getSpreadFromFaceTowardDirection(blockState, blockGetter, blockPos, direction, (Direction)direction2, this.config::canSpreadInto).isPresent());
    }

    public Optional<SpreadPos> spreadFromRandomFaceTowardRandomDirection(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource) {
        return Direction.allShuffled(randomSource).stream().filter(direction -> this.config.canSpreadFrom(blockState, (Direction)direction)).map(direction -> this.spreadFromFaceTowardRandomDirection(blockState, levelAccessor, blockPos, (Direction)direction, randomSource, false)).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
    }

    public long spreadAll(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, boolean bl) {
        return Direction.stream().filter(direction -> this.config.canSpreadFrom(blockState, (Direction)direction)).map(direction -> this.spreadFromFaceTowardAllDirections(blockState, levelAccessor, blockPos, (Direction)direction, bl)).reduce(0L, Long::sum);
    }

    public Optional<SpreadPos> spreadFromFaceTowardRandomDirection(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, RandomSource randomSource, boolean bl) {
        return Direction.allShuffled(randomSource).stream().map(direction2 -> this.spreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, (Direction)direction2, bl)).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
    }

    private long spreadFromFaceTowardAllDirections(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, boolean bl) {
        return Direction.stream().map(direction2 -> this.spreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, (Direction)direction2, bl)).filter(Optional::isPresent).count();
    }

    @VisibleForTesting
    public Optional<SpreadPos> spreadFromFaceTowardDirection(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, Direction direction2, boolean bl) {
        return this.getSpreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, direction2, this.config::canSpreadInto).flatMap(spreadPos -> this.spreadToFace(levelAccessor, (SpreadPos)spreadPos, bl));
    }

    public Optional<SpreadPos> getSpreadFromFaceTowardDirection(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction, Direction direction2, SpreadPredicate spreadPredicate) {
        if (direction2.getAxis() == direction.getAxis()) {
            return Optional.empty();
        }
        if (!(this.config.isOtherBlockValidAsSource(blockState) || this.config.hasFace(blockState, direction) && !this.config.hasFace(blockState, direction2))) {
            return Optional.empty();
        }
        for (SpreadType spreadType : this.config.getSpreadTypes()) {
            SpreadPos spreadPos = spreadType.getSpreadPos(blockPos, direction2, direction);
            if (!spreadPredicate.test(blockGetter, blockPos, spreadPos)) continue;
            return Optional.of(spreadPos);
        }
        return Optional.empty();
    }

    public Optional<SpreadPos> spreadToFace(LevelAccessor levelAccessor, SpreadPos spreadPos, boolean bl) {
        BlockState blockState = levelAccessor.getBlockState(spreadPos.pos());
        if (this.config.placeBlock(levelAccessor, spreadPos, blockState, bl)) {
            return Optional.of(spreadPos);
        }
        return Optional.empty();
    }

    public static class DefaultSpreaderConfig
    implements SpreadConfig {
        protected MultifaceBlock block;

        public DefaultSpreaderConfig(MultifaceBlock multifaceBlock) {
            this.block = multifaceBlock;
        }

        @Override
        @Nullable
        public BlockState getStateForPlacement(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return this.block.getStateForPlacement(blockState, blockGetter, blockPos, direction);
        }

        protected boolean stateCanBeReplaced(BlockGetter blockGetter, BlockPos blockPos, BlockPos blockPos2, Direction direction, BlockState blockState) {
            return blockState.isAir() || blockState.is(this.block) || blockState.is(Blocks.WATER) && blockState.getFluidState().isSource();
        }

        @Override
        public boolean canSpreadInto(BlockGetter blockGetter, BlockPos blockPos, SpreadPos spreadPos) {
            BlockState blockState = blockGetter.getBlockState(spreadPos.pos());
            return this.stateCanBeReplaced(blockGetter, blockPos, spreadPos.pos(), spreadPos.face(), blockState) && this.block.isValidStateForPlacement(blockGetter, blockState, spreadPos.pos(), spreadPos.face());
        }
    }

    public static interface SpreadConfig {
        @Nullable
        public BlockState getStateForPlacement(BlockState var1, BlockGetter var2, BlockPos var3, Direction var4);

        public boolean canSpreadInto(BlockGetter var1, BlockPos var2, SpreadPos var3);

        default public SpreadType[] getSpreadTypes() {
            return DEFAULT_SPREAD_ORDER;
        }

        default public boolean hasFace(BlockState blockState, Direction direction) {
            return MultifaceBlock.hasFace(blockState, direction);
        }

        default public boolean isOtherBlockValidAsSource(BlockState blockState) {
            return false;
        }

        default public boolean canSpreadFrom(BlockState blockState, Direction direction) {
            return this.isOtherBlockValidAsSource(blockState) || this.hasFace(blockState, direction);
        }

        default public boolean placeBlock(LevelAccessor levelAccessor, SpreadPos spreadPos, BlockState blockState, boolean bl) {
            BlockState blockState2 = this.getStateForPlacement(blockState, levelAccessor, spreadPos.pos(), spreadPos.face());
            if (blockState2 != null) {
                if (bl) {
                    levelAccessor.getChunk(spreadPos.pos()).markPosForPostprocessing(spreadPos.pos());
                }
                return levelAccessor.setBlock(spreadPos.pos(), blockState2, 2);
            }
            return false;
        }
    }

    @FunctionalInterface
    public static interface SpreadPredicate {
        public boolean test(BlockGetter var1, BlockPos var2, SpreadPos var3);
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    public static enum SpreadType {
        SAME_POSITION{

            @Override
            public SpreadPos getSpreadPos(BlockPos blockPos, Direction direction, Direction direction2) {
                return new SpreadPos(blockPos, direction);
            }
        }
        ,
        SAME_PLANE{

            @Override
            public SpreadPos getSpreadPos(BlockPos blockPos, Direction direction, Direction direction2) {
                return new SpreadPos(blockPos.relative(direction), direction2);
            }
        }
        ,
        WRAP_AROUND{

            @Override
            public SpreadPos getSpreadPos(BlockPos blockPos, Direction direction, Direction direction2) {
                return new SpreadPos(blockPos.relative(direction).relative(direction2), direction.getOpposite());
            }
        };


        public abstract SpreadPos getSpreadPos(BlockPos var1, Direction var2, Direction var3);
    }

    public record SpreadPos(BlockPos pos, Direction face) {
    }
}

