package net.minecraft.world.level.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;

@EnvironmentInterfaces({@EnvironmentInterface(
		value = EnvType.CLIENT,
		itf = LidBlockEntity.class
	)})
public class ChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity, TickableBlockEntity {
	private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
	protected float openness;
	protected float oOpenness;
	protected int openCount;
	private int tickInterval;

	protected ChestBlockEntity(BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
	}

	public ChestBlockEntity() {
		this(BlockEntityType.CHEST);
	}

	@Override
	public int getContainerSize() {
		return 27;
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("container.chest");
	}

	@Override
	public void load(BlockState blockState, CompoundTag compoundTag) {
		super.load(blockState, compoundTag);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(compoundTag)) {
			ContainerHelper.loadAllItems(compoundTag, this.items);
		}
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		if (!this.trySaveLootTable(compoundTag)) {
			ContainerHelper.saveAllItems(compoundTag, this.items);
		}

		return compoundTag;
	}

	@Override
	public void tick() {
		int i = this.worldPosition.getX();
		int j = this.worldPosition.getY();
		int k = this.worldPosition.getZ();
		this.tickInterval++;
		this.openCount = getOpenCount(this.level, this, this.tickInterval, i, j, k, this.openCount);
		this.oOpenness = this.openness;
		float f = 0.1F;
		if (this.openCount > 0 && this.openness == 0.0F) {
			this.playSound(SoundEvents.CHEST_OPEN);
		}

		if (this.openCount == 0 && this.openness > 0.0F || this.openCount > 0 && this.openness < 1.0F) {
			float g = this.openness;
			if (this.openCount > 0) {
				this.openness += 0.1F;
			} else {
				this.openness -= 0.1F;
			}

			if (this.openness > 1.0F) {
				this.openness = 1.0F;
			}

			float h = 0.5F;
			if (this.openness < 0.5F && g >= 0.5F) {
				this.playSound(SoundEvents.CHEST_CLOSE);
			}

			if (this.openness < 0.0F) {
				this.openness = 0.0F;
			}
		}
	}

	public static int getOpenCount(Level level, BaseContainerBlockEntity baseContainerBlockEntity, int i, int j, int k, int l, int m) {
		if (!level.isClientSide && m != 0 && (i + j + k + l) % 200 == 0) {
			m = getOpenCount(level, baseContainerBlockEntity, j, k, l);
		}

		return m;
	}

	public static int getOpenCount(Level level, BaseContainerBlockEntity baseContainerBlockEntity, int i, int j, int k) {
		int l = 0;
		float f = 5.0F;

		for (Player player : level.getEntitiesOfClass(
			Player.class,
			new AABB(
				(double)((float)i - 5.0F),
				(double)((float)j - 5.0F),
				(double)((float)k - 5.0F),
				(double)((float)(i + 1) + 5.0F),
				(double)((float)(j + 1) + 5.0F),
				(double)((float)(k + 1) + 5.0F)
			)
		)) {
			if (player.containerMenu instanceof ChestMenu) {
				Container container = ((ChestMenu)player.containerMenu).getContainer();
				if (container == baseContainerBlockEntity || container instanceof CompoundContainer && ((CompoundContainer)container).contains(baseContainerBlockEntity)) {
					l++;
				}
			}
		}

		return l;
	}

	private void playSound(SoundEvent soundEvent) {
		ChestType chestType = this.getBlockState().getValue(ChestBlock.TYPE);
		if (chestType != ChestType.LEFT) {
			double d = (double)this.worldPosition.getX() + 0.5;
			double e = (double)this.worldPosition.getY() + 0.5;
			double f = (double)this.worldPosition.getZ() + 0.5;
			if (chestType == ChestType.RIGHT) {
				Direction direction = ChestBlock.getConnectedDirection(this.getBlockState());
				d += (double)direction.getStepX() * 0.5;
				f += (double)direction.getStepZ() * 0.5;
			}

			this.level.playSound(null, d, e, f, soundEvent, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
		}
	}

	@Override
	public boolean triggerEvent(int i, int j) {
		if (i == 1) {
			this.openCount = j;
			return true;
		} else {
			return super.triggerEvent(i, j);
		}
	}

	@Override
	public void startOpen(Player player) {
		if (!player.isSpectator()) {
			if (this.openCount < 0) {
				this.openCount = 0;
			}

			this.openCount++;
			this.signalOpenCount();
		}
	}

	@Override
	public void stopOpen(Player player) {
		if (!player.isSpectator()) {
			this.openCount--;
			this.signalOpenCount();
		}
	}

	protected void signalOpenCount() {
		Block block = this.getBlockState().getBlock();
		if (block instanceof ChestBlock) {
			this.level.blockEvent(this.worldPosition, block, 1, this.openCount);
			this.level.updateNeighborsAt(this.worldPosition, block);
		}
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> nonNullList) {
		this.items = nonNullList;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public float getOpenNess(float f) {
		return Mth.lerp(f, this.oOpenness, this.openness);
	}

	public static int getOpenCount(BlockGetter blockGetter, BlockPos blockPos) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		if (blockState.getBlock().isEntityBlock()) {
			BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
			if (blockEntity instanceof ChestBlockEntity) {
				return ((ChestBlockEntity)blockEntity).openCount;
			}
		}

		return 0;
	}

	public static void swapContents(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2) {
		NonNullList<ItemStack> nonNullList = chestBlockEntity.getItems();
		chestBlockEntity.setItems(chestBlockEntity2.getItems());
		chestBlockEntity2.setItems(nonNullList);
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return ChestMenu.threeRows(i, inventory, this);
	}
}
