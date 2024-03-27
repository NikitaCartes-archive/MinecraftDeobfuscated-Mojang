package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;

public class JukeboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem.BlockContainerSingleItem {
	private static final int SONG_END_PADDING = 20;
	private ItemStack item = ItemStack.EMPTY;
	private int ticksSinceLastEvent;
	private long tickCount;
	private long recordStartedTick;
	private boolean isPlaying;

	public JukeboxBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.JUKEBOX, blockPos, blockState);
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		if (compoundTag.contains("RecordItem", 10)) {
			this.item = (ItemStack)ItemStack.parse(provider, compoundTag.getCompound("RecordItem")).orElse(ItemStack.EMPTY);
		} else {
			this.item = ItemStack.EMPTY;
		}

		this.isPlaying = compoundTag.getBoolean("IsPlaying");
		this.recordStartedTick = compoundTag.getLong("RecordStartTick");
		this.tickCount = compoundTag.getLong("TickCount");
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		if (!this.getTheItem().isEmpty()) {
			compoundTag.put("RecordItem", this.getTheItem().save(provider));
		}

		compoundTag.putBoolean("IsPlaying", this.isPlaying);
		compoundTag.putLong("RecordStartTick", this.recordStartedTick);
		compoundTag.putLong("TickCount", this.tickCount);
	}

	public boolean isRecordPlaying() {
		return !this.getTheItem().isEmpty() && this.isPlaying;
	}

	private void setHasRecordBlockState(@Nullable Entity entity, boolean bl) {
		if (this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
			this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(bl)), 2);
			this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
		}
	}

	@VisibleForTesting
	public void startPlaying() {
		this.recordStartedTick = this.tickCount;
		this.isPlaying = true;
		this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
		this.level.levelEvent(null, 1010, this.getBlockPos(), Item.getId(this.getTheItem().getItem()));
		this.setChanged();
	}

	private void stopPlaying() {
		this.isPlaying = false;
		this.level.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
		this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
		this.level.levelEvent(1011, this.getBlockPos(), 0);
		this.setChanged();
	}

	private void tick(Level level, BlockPos blockPos, BlockState blockState) {
		this.ticksSinceLastEvent++;
		if (this.isRecordPlaying() && this.getTheItem().getItem() instanceof RecordItem recordItem) {
			if (this.shouldRecordStopPlaying(recordItem)) {
				this.stopPlaying();
			} else if (this.shouldSendJukeboxPlayingEvent()) {
				this.ticksSinceLastEvent = 0;
				level.gameEvent(GameEvent.JUKEBOX_PLAY, blockPos, GameEvent.Context.of(blockState));
				this.spawnMusicParticles(level, blockPos);
			}
		}

		this.tickCount++;
	}

	private boolean shouldRecordStopPlaying(RecordItem recordItem) {
		return this.tickCount >= this.recordStartedTick + (long)recordItem.getLengthInTicks() + 20L;
	}

	private boolean shouldSendJukeboxPlayingEvent() {
		return this.ticksSinceLastEvent >= 20;
	}

	@Override
	public ItemStack getTheItem() {
		return this.item;
	}

	@Override
	public ItemStack splitTheItem(int i) {
		ItemStack itemStack = this.item;
		this.item = ItemStack.EMPTY;
		if (!itemStack.isEmpty()) {
			this.setHasRecordBlockState(null, false);
			this.stopPlaying();
		}

		return itemStack;
	}

	@Override
	public void setTheItem(ItemStack itemStack) {
		if (itemStack.is(ItemTags.MUSIC_DISCS) && this.level != null) {
			this.item = itemStack;
			this.setHasRecordBlockState(null, true);
			this.startPlaying();
		} else if (itemStack.isEmpty()) {
			this.splitTheItem(1);
		}
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public BlockEntity getContainerBlockEntity() {
		return this;
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemStack) {
		return itemStack.is(ItemTags.MUSIC_DISCS) && this.getItem(i).isEmpty();
	}

	@Override
	public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
		return container.hasAnyMatching(ItemStack::isEmpty);
	}

	private void spawnMusicParticles(Level level, BlockPos blockPos) {
		if (level instanceof ServerLevel serverLevel) {
			Vec3 vec3 = Vec3.atBottomCenterOf(blockPos).add(0.0, 1.2F, 0.0);
			float f = (float)level.getRandom().nextInt(4) / 24.0F;
			serverLevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, (double)f, 0.0, 0.0, 1.0);
		}
	}

	public void popOutRecord() {
		if (this.level != null && !this.level.isClientSide) {
			BlockPos blockPos = this.getBlockPos();
			ItemStack itemStack = this.getTheItem();
			if (!itemStack.isEmpty()) {
				this.removeTheItem();
				Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockPos, 0.5, 1.01, 0.5).offsetRandom(this.level.random, 0.7F);
				ItemStack itemStack2 = itemStack.copy();
				ItemEntity itemEntity = new ItemEntity(this.level, vec3.x(), vec3.y(), vec3.z(), itemStack2);
				itemEntity.setDefaultPickUpDelay();
				this.level.addFreshEntity(itemEntity);
			}
		}
	}

	public static void playRecordTick(Level level, BlockPos blockPos, BlockState blockState, JukeboxBlockEntity jukeboxBlockEntity) {
		jukeboxBlockEntity.tick(level, blockPos, blockState);
	}

	@VisibleForTesting
	public void setRecordWithoutPlaying(ItemStack itemStack) {
		this.item = itemStack;
		this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
		this.setChanged();
	}
}
