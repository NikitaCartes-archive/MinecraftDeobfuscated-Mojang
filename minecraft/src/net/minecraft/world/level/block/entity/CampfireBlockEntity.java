package net.minecraft.world.level.block.entity;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CampfireBlockEntity extends BlockEntity implements Clearable, TickableBlockEntity {
	private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
	private final int[] cookingProgress = new int[4];
	private final int[] cookingTime = new int[4];

	public CampfireBlockEntity() {
		super(BlockEntityType.CAMPFIRE);
	}

	@Override
	public void tick() {
		boolean bl = (Boolean)this.getBlockState().getValue(CampfireBlock.LIT);
		boolean bl2 = this.level.isClientSide;
		if (bl2) {
			if (bl) {
				this.makeParticles();
			}
		} else {
			if (bl) {
				this.cook();
			} else {
				for (int i = 0; i < this.items.size(); i++) {
					if (this.cookingProgress[i] > 0) {
						this.cookingProgress[i] = Mth.clamp(this.cookingProgress[i] - 2, 0, this.cookingTime[i]);
					}
				}
			}
		}
	}

	private void cook() {
		for (int i = 0; i < this.items.size(); i++) {
			ItemStack itemStack = this.items.get(i);
			if (!itemStack.isEmpty()) {
				this.cookingProgress[i]++;
				if (this.cookingProgress[i] >= this.cookingTime[i]) {
					Container container = new SimpleContainer(itemStack);
					ItemStack itemStack2 = (ItemStack)this.level
						.getRecipeManager()
						.getRecipeFor(RecipeType.CAMPFIRE_COOKING, container, this.level)
						.map(campfireCookingRecipe -> campfireCookingRecipe.assemble(container))
						.orElse(itemStack);
					BlockPos blockPos = this.getBlockPos();
					Containers.dropItemStack(this.level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack2);
					this.items.set(i, ItemStack.EMPTY);
					this.markUpdated();
				}
			}
		}
	}

	private void makeParticles() {
		Level level = this.getLevel();
		if (level != null) {
			BlockPos blockPos = this.getBlockPos();
			Random random = level.random;
			if (random.nextFloat() < 0.11F) {
				for (int i = 0; i < random.nextInt(2) + 2; i++) {
					CampfireBlock.makeParticles(level, blockPos, (Boolean)this.getBlockState().getValue(CampfireBlock.SIGNAL_FIRE), false);
				}
			}

			int i = ((Direction)this.getBlockState().getValue(CampfireBlock.FACING)).get2DDataValue();

			for (int j = 0; j < this.items.size(); j++) {
				if (!this.items.get(j).isEmpty() && random.nextFloat() < 0.2F) {
					Direction direction = Direction.from2DDataValue(Math.floorMod(j + i, 4));
					float f = 0.3125F;
					double d = (double)blockPos.getX()
						+ 0.5
						- (double)((float)direction.getStepX() * 0.3125F)
						+ (double)((float)direction.getClockWise().getStepX() * 0.3125F);
					double e = (double)blockPos.getY() + 0.5;
					double g = (double)blockPos.getZ()
						+ 0.5
						- (double)((float)direction.getStepZ() * 0.3125F)
						+ (double)((float)direction.getClockWise().getStepZ() * 0.3125F);

					for (int k = 0; k < 4; k++) {
						level.addParticle(ParticleTypes.SMOKE, d, e, g, 0.0, 5.0E-4, 0.0);
					}
				}
			}
		}
	}

	public NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	public void load(BlockState blockState, CompoundTag compoundTag) {
		super.load(blockState, compoundTag);
		this.items.clear();
		ContainerHelper.loadAllItems(compoundTag, this.items);
		if (compoundTag.contains("CookingTimes", 11)) {
			int[] is = compoundTag.getIntArray("CookingTimes");
			System.arraycopy(is, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, is.length));
		}

		if (compoundTag.contains("CookingTotalTimes", 11)) {
			int[] is = compoundTag.getIntArray("CookingTotalTimes");
			System.arraycopy(is, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, is.length));
		}
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		this.saveMetadataAndItems(compoundTag);
		compoundTag.putIntArray("CookingTimes", this.cookingProgress);
		compoundTag.putIntArray("CookingTotalTimes", this.cookingTime);
		return compoundTag;
	}

	private CompoundTag saveMetadataAndItems(CompoundTag compoundTag) {
		super.save(compoundTag);
		ContainerHelper.saveAllItems(compoundTag, this.items, true);
		return compoundTag;
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 13, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveMetadataAndItems(new CompoundTag());
	}

	public Optional<CampfireCookingRecipe> getCookableRecipe(ItemStack itemStack) {
		return this.items.stream().noneMatch(ItemStack::isEmpty)
			? Optional.empty()
			: this.level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SimpleContainer(itemStack), this.level);
	}

	public boolean placeFood(ItemStack itemStack, int i) {
		for (int j = 0; j < this.items.size(); j++) {
			ItemStack itemStack2 = this.items.get(j);
			if (itemStack2.isEmpty()) {
				this.cookingTime[j] = i;
				this.cookingProgress[j] = 0;
				this.items.set(j, itemStack.split(1));
				this.markUpdated();
				return true;
			}
		}

		return false;
	}

	private void markUpdated() {
		this.setChanged();
		this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	public void dowse() {
		if (!this.getLevel().isClientSide) {
			Containers.dropContents(this.getLevel(), this.getBlockPos(), this.getItems());
		}

		this.markUpdated();
	}
}
