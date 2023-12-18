package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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

public class NoteBlock extends Block {
	public static final MapCodec<NoteBlock> CODEC = simpleCodec(NoteBlock::new);
	public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final IntegerProperty NOTE = BlockStateProperties.NOTE;
	public static final int NOTE_VOLUME = 3;

	@Override
	public MapCodec<NoteBlock> codec() {
		return CODEC;
	}

	public NoteBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, Integer.valueOf(0)).setValue(POWERED, Boolean.valueOf(false))
		);
	}

	private BlockState setInstrument(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		NoteBlockInstrument noteBlockInstrument = levelAccessor.getBlockState(blockPos.above()).instrument();
		if (noteBlockInstrument.worksAboveNoteBlock()) {
			return blockState.setValue(INSTRUMENT, noteBlockInstrument);
		} else {
			NoteBlockInstrument noteBlockInstrument2 = levelAccessor.getBlockState(blockPos.below()).instrument();
			NoteBlockInstrument noteBlockInstrument3 = noteBlockInstrument2.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : noteBlockInstrument2;
			return blockState.setValue(INSTRUMENT, noteBlockInstrument3);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.setInstrument(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), this.defaultBlockState());
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		boolean bl = direction.getAxis() == Direction.Axis.Y;
		return bl
			? this.setInstrument(levelAccessor, blockPos, blockState)
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		boolean bl2 = level.hasNeighborSignal(blockPos);
		if (bl2 != (Boolean)blockState.getValue(POWERED)) {
			if (bl2) {
				this.playNote(null, blockState, level, blockPos);
			}

			level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(bl2)), 3);
		}
	}

	private void playNote(@Nullable Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
		if (((NoteBlockInstrument)blockState.getValue(INSTRUMENT)).worksAboveNoteBlock() || level.getBlockState(blockPos.above()).isAir()) {
			level.blockEvent(blockPos, this, 0, 0);
			level.gameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, blockPos);
		}
	}

	@Override
	public ItemInteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		return itemStack.is(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS) && blockHitResult.getDirection() == Direction.UP
			? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
			: super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			blockState = blockState.cycle(NOTE);
			level.setBlock(blockPos, blockState, 3);
			this.playNote(player, blockState, level, blockPos);
			player.awardStat(Stats.TUNE_NOTEBLOCK);
			return InteractionResult.CONSUME;
		}
	}

	@Override
	public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		if (!level.isClientSide) {
			this.playNote(player, blockState, level, blockPos);
			player.awardStat(Stats.PLAY_NOTEBLOCK);
		}
	}

	public static float getPitchFromNote(int i) {
		return (float)Math.pow(2.0, (double)(i - 12) / 12.0);
	}

	@Override
	public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
		NoteBlockInstrument noteBlockInstrument = blockState.getValue(INSTRUMENT);
		float f;
		if (noteBlockInstrument.isTunable()) {
			int k = (Integer)blockState.getValue(NOTE);
			f = getPitchFromNote(k);
			level.addParticle(
				ParticleTypes.NOTE, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + 0.5, (double)k / 24.0, 0.0, 0.0
			);
		} else {
			f = 1.0F;
		}

		Holder<SoundEvent> holder;
		if (noteBlockInstrument.hasCustomSound()) {
			ResourceLocation resourceLocation = this.getCustomSoundId(level, blockPos);
			if (resourceLocation == null) {
				return false;
			}

			holder = Holder.direct(SoundEvent.createVariableRangeEvent(resourceLocation));
		} else {
			holder = noteBlockInstrument.getSoundEvent();
		}

		level.playSeededSound(
			null,
			(double)blockPos.getX() + 0.5,
			(double)blockPos.getY() + 0.5,
			(double)blockPos.getZ() + 0.5,
			holder,
			SoundSource.RECORDS,
			3.0F,
			f,
			level.random.nextLong()
		);
		return true;
	}

	@Nullable
	private ResourceLocation getCustomSoundId(Level level, BlockPos blockPos) {
		return level.getBlockEntity(blockPos.above()) instanceof SkullBlockEntity skullBlockEntity ? skullBlockEntity.getNoteBlockSound() : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(INSTRUMENT, POWERED, NOTE);
	}
}
