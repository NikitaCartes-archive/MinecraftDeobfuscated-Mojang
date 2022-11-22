/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class NoteBlock
extends Block {
    public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty NOTE = BlockStateProperties.NOTE;

    public NoteBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(INSTRUMENT, NoteBlockInstrument.HARP)).setValue(NOTE, 0)).setValue(POWERED, false));
    }

    private static boolean isFeatureFlagEnabled(LevelAccessor levelAccessor) {
        return levelAccessor.enabledFeatures().contains(FeatureFlags.UPDATE_1_20);
    }

    private BlockState setInstrument(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        if (NoteBlock.isFeatureFlagEnabled(levelAccessor)) {
            BlockState blockState2 = levelAccessor.getBlockState(blockPos.above());
            return (BlockState)blockState.setValue(INSTRUMENT, NoteBlockInstrument.byStateAbove(blockState2).orElseGet(() -> NoteBlockInstrument.byStateBelow(levelAccessor.getBlockState(blockPos.below()))));
        }
        return (BlockState)blockState.setValue(INSTRUMENT, NoteBlockInstrument.byStateBelow(levelAccessor.getBlockState(blockPos.below())));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.setInstrument(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), this.defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        boolean bl;
        boolean bl2 = NoteBlock.isFeatureFlagEnabled(levelAccessor) ? direction.getAxis() == Direction.Axis.Y : (bl = direction == Direction.DOWN);
        if (bl) {
            return this.setInstrument(levelAccessor, blockPos, blockState);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        boolean bl2 = level.hasNeighborSignal(blockPos);
        if (bl2 != blockState.getValue(POWERED)) {
            if (bl2) {
                this.playNote(null, blockState, level, blockPos);
            }
            level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, bl2), 3);
        }
    }

    private void playNote(@Nullable Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
        if (!blockState.getValue(INSTRUMENT).requiresAirAbove() || level.getBlockState(blockPos.above()).isAir()) {
            level.blockEvent(blockPos, this, 0, 0);
            level.gameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, blockPos);
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        blockState = (BlockState)blockState.cycle(NOTE);
        level.setBlock(blockPos, blockState, 3);
        this.playNote(player, blockState, level, blockPos);
        player.awardStat(Stats.TUNE_NOTEBLOCK);
        return InteractionResult.CONSUME;
    }

    @Override
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        if (level.isClientSide) {
            return;
        }
        this.playNote(player, blockState, level, blockPos);
        player.awardStat(Stats.PLAY_NOTEBLOCK);
    }

    @Override
    public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
        SoundEvent soundEvent;
        float f;
        NoteBlockInstrument noteBlockInstrument = blockState.getValue(INSTRUMENT);
        if (noteBlockInstrument.isTunable()) {
            int k = blockState.getValue(NOTE);
            f = (float)Math.pow(2.0, (double)(k - 12) / 12.0);
            level.addParticle(ParticleTypes.NOTE, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + 0.5, (double)k / 24.0, 0.0, 0.0);
        } else {
            f = 1.0f;
        }
        if (noteBlockInstrument.hasCustomSound()) {
            soundEvent = this.getCustomSoundEvent(noteBlockInstrument, level, blockPos);
            if (soundEvent == null) {
                return false;
            }
        } else {
            soundEvent = noteBlockInstrument.getSoundEvent();
        }
        level.playSound(null, blockPos, soundEvent, SoundSource.RECORDS, 3.0f, f);
        return true;
    }

    @Nullable
    private SoundEvent getCustomSoundEvent(NoteBlockInstrument noteBlockInstrument, Level level, BlockPos blockPos) {
        SkullBlockEntity skullBlockEntity;
        ResourceLocation resourceLocation;
        BlockEntity blockEntity = level.getBlockEntity(blockPos.above());
        if (blockEntity instanceof SkullBlockEntity && (resourceLocation = (skullBlockEntity = (SkullBlockEntity)blockEntity).getNoteBlockSound()) != null) {
            return new SoundEvent(resourceLocation);
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INSTRUMENT, POWERED, NOTE);
    }
}

