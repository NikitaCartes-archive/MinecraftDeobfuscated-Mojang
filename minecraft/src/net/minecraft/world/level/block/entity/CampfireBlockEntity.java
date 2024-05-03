package net.minecraft.world.level.block.entity;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class CampfireBlockEntity extends BlockEntity implements Clearable {
	private static final int BURN_COOL_SPEED = 2;
	private static final int NUM_SLOTS = 4;
	private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
	private final int[] cookingProgress = new int[4];
	private final int[] cookingTime = new int[4];
	private final RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);

	public CampfireBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.CAMPFIRE, blockPos, blockState);
	}

	public static void cookTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity) {
		boolean bl = false;

		for (int i = 0; i < campfireBlockEntity.items.size(); i++) {
			ItemStack itemStack = campfireBlockEntity.items.get(i);
			if (!itemStack.isEmpty()) {
				bl = true;
				campfireBlockEntity.cookingProgress[i]++;
				if (campfireBlockEntity.cookingProgress[i] >= campfireBlockEntity.cookingTime[i]) {
					SingleRecipeInput singleRecipeInput = new SingleRecipeInput(itemStack);
					ItemStack itemStack2 = (ItemStack)campfireBlockEntity.quickCheck
						.getRecipeFor(singleRecipeInput, level)
						.map(recipeHolder -> ((CampfireCookingRecipe)recipeHolder.value()).assemble(singleRecipeInput, level.registryAccess()))
						.orElse(itemStack);
					if (itemStack2.isItemEnabled(level.enabledFeatures())) {
						Containers.dropItemStack(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack2);
						campfireBlockEntity.items.set(i, ItemStack.EMPTY);
						level.sendBlockUpdated(blockPos, blockState, blockState, 3);
						level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState));
					}
				}
			}
		}

		if (bl) {
			setChanged(level, blockPos, blockState);
		}
	}

	public static void cooldownTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity) {
		boolean bl = false;

		for (int i = 0; i < campfireBlockEntity.items.size(); i++) {
			if (campfireBlockEntity.cookingProgress[i] > 0) {
				bl = true;
				campfireBlockEntity.cookingProgress[i] = Mth.clamp(campfireBlockEntity.cookingProgress[i] - 2, 0, campfireBlockEntity.cookingTime[i]);
			}
		}

		if (bl) {
			setChanged(level, blockPos, blockState);
		}
	}

	public static void particleTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity) {
		RandomSource randomSource = level.random;
		if (randomSource.nextFloat() < 0.11F) {
			for (int i = 0; i < randomSource.nextInt(2) + 2; i++) {
				CampfireBlock.makeParticles(level, blockPos, (Boolean)blockState.getValue(CampfireBlock.SIGNAL_FIRE), false);
			}
		}

		int i = ((Direction)blockState.getValue(CampfireBlock.FACING)).get2DDataValue();

		for (int j = 0; j < campfireBlockEntity.items.size(); j++) {
			if (!campfireBlockEntity.items.get(j).isEmpty() && randomSource.nextFloat() < 0.2F) {
				Direction direction = Direction.from2DDataValue(Math.floorMod(j + i, 4));
				float f = 0.3125F;
				double d = (double)blockPos.getX() + 0.5 - (double)((float)direction.getStepX() * 0.3125F) + (double)((float)direction.getClockWise().getStepX() * 0.3125F);
				double e = (double)blockPos.getY() + 0.5;
				double g = (double)blockPos.getZ() + 0.5 - (double)((float)direction.getStepZ() * 0.3125F) + (double)((float)direction.getClockWise().getStepZ() * 0.3125F);

				for (int k = 0; k < 4; k++) {
					level.addParticle(ParticleTypes.SMOKE, d, e, g, 0.0, 5.0E-4, 0.0);
				}
			}
		}
	}

	public NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		this.items.clear();
		ContainerHelper.loadAllItems(compoundTag, this.items, provider);
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
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		ContainerHelper.saveAllItems(compoundTag, this.items, true, provider);
		compoundTag.putIntArray("CookingTimes", this.cookingProgress);
		compoundTag.putIntArray("CookingTotalTimes", this.cookingTime);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag compoundTag = new CompoundTag();
		ContainerHelper.saveAllItems(compoundTag, this.items, true, provider);
		return compoundTag;
	}

	public Optional<RecipeHolder<CampfireCookingRecipe>> getCookableRecipe(ItemStack itemStack) {
		return this.items.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.quickCheck.getRecipeFor(new SingleRecipeInput(itemStack), this.level);
	}

	public boolean placeFood(@Nullable Entity entity, ItemStack itemStack, int i) {
		for (int j = 0; j < this.items.size(); j++) {
			ItemStack itemStack2 = this.items.get(j);
			if (itemStack2.isEmpty()) {
				this.cookingTime[j] = i;
				this.cookingProgress[j] = 0;
				this.items.set(j, itemStack.split(1));
				this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
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
		if (this.level != null) {
			this.markUpdated();
		}
	}

	@Override
	protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
		super.applyImplicitComponents(dataComponentInput);
		dataComponentInput.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.getItems());
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		compoundTag.remove("Items");
	}
}
