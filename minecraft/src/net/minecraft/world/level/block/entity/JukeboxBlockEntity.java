package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;

public class JukeboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem.BlockContainerSingleItem {
	public static final String SONG_ITEM_TAG_ID = "RecordItem";
	public static final String TICKS_SINCE_SONG_STARTED_TAG_ID = "ticks_since_song_started";
	private ItemStack item = ItemStack.EMPTY;
	private final JukeboxSongPlayer jukeboxSongPlayer = new JukeboxSongPlayer(this::onSongChanged, this.getBlockPos());

	public JukeboxBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.JUKEBOX, blockPos, blockState);
	}

	public JukeboxSongPlayer getSongPlayer() {
		return this.jukeboxSongPlayer;
	}

	public void onSongChanged() {
		this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
		this.setChanged();
	}

	private void notifyItemChangedInJukebox(boolean bl) {
		if (this.level != null && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
			this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(bl)), 2);
			this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
		}
	}

	public void popOutTheItem() {
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

	public static void tick(Level level, BlockPos blockPos, BlockState blockState, JukeboxBlockEntity jukeboxBlockEntity) {
		jukeboxBlockEntity.jukeboxSongPlayer.tick(level, blockState);
	}

	public int getComparatorOutput() {
		return (Integer)JukeboxSong.fromStack(this.level.registryAccess(), this.item).map(Holder::value).map(JukeboxSong::comparatorOutput).orElse(0);
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		if (compoundTag.contains("RecordItem", 10)) {
			this.item = (ItemStack)ItemStack.parse(provider, compoundTag.getCompound("RecordItem")).orElse(ItemStack.EMPTY);
		} else {
			this.item = ItemStack.EMPTY;
		}

		if (compoundTag.contains("ticks_since_song_started", 4)) {
			JukeboxSong.fromStack(provider, this.item)
				.ifPresent(holder -> this.jukeboxSongPlayer.setSongWithoutPlaying(holder, compoundTag.getLong("ticks_since_song_started")));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		if (!this.getTheItem().isEmpty()) {
			compoundTag.put("RecordItem", this.getTheItem().save(provider));
		}

		if (this.jukeboxSongPlayer.getSong() != null) {
			compoundTag.putLong("ticks_since_song_started", this.jukeboxSongPlayer.getTicksSinceSongStarted());
		}
	}

	@Override
	public ItemStack getTheItem() {
		return this.item;
	}

	@Override
	public ItemStack splitTheItem(int i) {
		ItemStack itemStack = this.item;
		this.setTheItem(ItemStack.EMPTY);
		return itemStack;
	}

	@Override
	public void setTheItem(ItemStack itemStack) {
		this.item = itemStack;
		boolean bl = !this.item.isEmpty();
		Optional<Holder<JukeboxSong>> optional = JukeboxSong.fromStack(this.level.registryAccess(), this.item);
		this.notifyItemChangedInJukebox(bl);
		if (bl && optional.isPresent()) {
			this.jukeboxSongPlayer.play(this.level, (Holder<JukeboxSong>)optional.get());
		} else {
			this.jukeboxSongPlayer.stop(this.level, this.getBlockState());
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
		return itemStack.has(DataComponents.JUKEBOX_PLAYABLE) && this.getItem(i).isEmpty();
	}

	@Override
	public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
		return container.hasAnyMatching(ItemStack::isEmpty);
	}

	@VisibleForTesting
	public void setSongItemWithoutPlaying(ItemStack itemStack) {
		this.item = itemStack;
		JukeboxSong.fromStack(this.level.registryAccess(), itemStack).ifPresent(holder -> this.jukeboxSongPlayer.setSongWithoutPlaying(holder, 0L));
		this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
		this.setChanged();
	}

	@VisibleForTesting
	public void tryForcePlaySong() {
		JukeboxSong.fromStack(this.level.registryAccess(), this.getTheItem()).ifPresent(holder -> this.jukeboxSongPlayer.play(this.level, holder));
	}
}
