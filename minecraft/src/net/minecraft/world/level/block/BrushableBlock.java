package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class BrushableBlock extends BaseEntityBlock implements Fallable {
	public static final MapCodec<BrushableBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					BuiltInRegistries.BLOCK.byNameCodec().fieldOf("turns_into").forGetter(BrushableBlock::getTurnsInto),
					BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_sound").forGetter(BrushableBlock::getBrushSound),
					BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("brush_completed_sound").forGetter(BrushableBlock::getBrushCompletedSound),
					propertiesCodec()
				)
				.apply(instance, BrushableBlock::new)
	);
	private static final IntegerProperty DUSTED = BlockStateProperties.DUSTED;
	public static final int TICK_DELAY = 2;
	private final Block turnsInto;
	private final SoundEvent brushSound;
	private final SoundEvent brushCompletedSound;

	@Override
	public MapCodec<BrushableBlock> codec() {
		return CODEC;
	}

	public BrushableBlock(Block block, SoundEvent soundEvent, SoundEvent soundEvent2, BlockBehaviour.Properties properties) {
		super(properties);
		this.turnsInto = block;
		this.brushSound = soundEvent;
		this.brushCompletedSound = soundEvent2;
		this.registerDefaultState(this.stateDefinition.any().setValue(DUSTED, Integer.valueOf(0)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DUSTED);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		level.scheduleTick(blockPos, this, 2);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		scheduledTickAccess.scheduleTick(blockPos, this, 2);
		return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (serverLevel.getBlockEntity(blockPos) instanceof BrushableBlockEntity brushableBlockEntity) {
			brushableBlockEntity.checkReset(serverLevel);
		}

		if (FallingBlock.isFree(serverLevel.getBlockState(blockPos.below())) && blockPos.getY() >= serverLevel.getMinY()) {
			FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(serverLevel, blockPos, blockState);
			fallingBlockEntity.disableDrop();
		}
	}

	@Override
	public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
		Vec3 vec3 = fallingBlockEntity.getBoundingBox().getCenter();
		level.levelEvent(2001, BlockPos.containing(vec3), Block.getId(fallingBlockEntity.getBlockState()));
		level.gameEvent(fallingBlockEntity, GameEvent.BLOCK_DESTROY, vec3);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextInt(16) == 0) {
			BlockPos blockPos2 = blockPos.below();
			if (FallingBlock.isFree(level.getBlockState(blockPos2))) {
				double d = (double)blockPos.getX() + randomSource.nextDouble();
				double e = (double)blockPos.getY() - 0.05;
				double f = (double)blockPos.getZ() + randomSource.nextDouble();
				level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, blockState), d, e, f, 0.0, 0.0, 0.0);
			}
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BrushableBlockEntity(blockPos, blockState);
	}

	public Block getTurnsInto() {
		return this.turnsInto;
	}

	public SoundEvent getBrushSound() {
		return this.brushSound;
	}

	public SoundEvent getBrushCompletedSound() {
		return this.brushCompletedSound;
	}
}
