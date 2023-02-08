package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class JukeboxBlockEntity extends BlockEntity implements Clearable {
	private ItemStack record = ItemStack.EMPTY;
	private int ticksSinceLastEvent;
	private long tickCount;
	private long recordStartedTick;
	private boolean isPlaying;

	public JukeboxBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.JUKEBOX, blockPos, blockState);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("RecordItem", 10)) {
			this.setRecord(ItemStack.of(compoundTag.getCompound("RecordItem")));
		}

		this.isPlaying = compoundTag.getBoolean("IsPlaying");
		this.recordStartedTick = compoundTag.getLong("RecordStartTick");
		this.tickCount = compoundTag.getLong("TickCount");
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		if (!this.getRecord().isEmpty()) {
			compoundTag.put("RecordItem", this.getRecord().save(new CompoundTag()));
		}

		compoundTag.putBoolean("IsPlaying", this.isPlaying);
		compoundTag.putLong("RecordStartTick", this.recordStartedTick);
		compoundTag.putLong("TickCount", this.tickCount);
	}

	public ItemStack getRecord() {
		return this.record;
	}

	public void setRecord(ItemStack itemStack) {
		this.record = itemStack;
		this.setChanged();
	}

	public void playRecord() {
		this.recordStartedTick = this.tickCount;
		this.isPlaying = true;
	}

	@Override
	public void clearContent() {
		this.setRecord(ItemStack.EMPTY);
		this.isPlaying = false;
	}

	public static void playRecordTick(Level level, BlockPos blockPos, BlockState blockState, JukeboxBlockEntity jukeboxBlockEntity) {
		jukeboxBlockEntity.ticksSinceLastEvent++;
		if (recordIsPlaying(blockState, jukeboxBlockEntity) && jukeboxBlockEntity.getRecord().getItem() instanceof RecordItem recordItem) {
			if (recordShouldStopPlaying(jukeboxBlockEntity, recordItem)) {
				level.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, blockPos, GameEvent.Context.of(blockState));
				jukeboxBlockEntity.isPlaying = false;
			} else if (shouldSendJukeboxPlayingEvent(jukeboxBlockEntity)) {
				jukeboxBlockEntity.ticksSinceLastEvent = 0;
				level.gameEvent(GameEvent.JUKEBOX_PLAY, blockPos, GameEvent.Context.of(blockState));
				spawnMusicParticles(level, blockPos);
			}
		}

		jukeboxBlockEntity.tickCount++;
	}

	private static void spawnMusicParticles(Level level, BlockPos blockPos) {
		if (level instanceof ServerLevel serverLevel) {
			Vec3 vec3 = Vec3.atBottomCenterOf(blockPos).add(0.0, 1.2F, 0.0);
			float f = (float)level.getRandom().nextInt(4) / 24.0F;
			serverLevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, (double)f, 0.0, 0.0, 1.0);
		}
	}

	private static boolean recordIsPlaying(BlockState blockState, JukeboxBlockEntity jukeboxBlockEntity) {
		return (Boolean)blockState.getValue(JukeboxBlock.HAS_RECORD) && jukeboxBlockEntity.isPlaying;
	}

	private static boolean recordShouldStopPlaying(JukeboxBlockEntity jukeboxBlockEntity, RecordItem recordItem) {
		return jukeboxBlockEntity.tickCount >= jukeboxBlockEntity.recordStartedTick + (long)recordItem.getLengthInTicks();
	}

	private static boolean shouldSendJukeboxPlayingEvent(JukeboxBlockEntity jukeboxBlockEntity) {
		return jukeboxBlockEntity.ticksSinceLastEvent >= 20;
	}
}
