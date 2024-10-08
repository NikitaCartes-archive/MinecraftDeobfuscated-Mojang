package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap.Entry;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible {
	protected static final int SLOT_INPUT = 0;
	protected static final int SLOT_FUEL = 1;
	protected static final int SLOT_RESULT = 2;
	public static final int DATA_LIT_TIME = 0;
	private static final int[] SLOTS_FOR_UP = new int[]{0};
	private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
	private static final int[] SLOTS_FOR_SIDES = new int[]{1};
	public static final int DATA_LIT_DURATION = 1;
	public static final int DATA_COOKING_PROGRESS = 2;
	public static final int DATA_COOKING_TOTAL_TIME = 3;
	public static final int NUM_DATA_VALUES = 4;
	public static final int BURN_TIME_STANDARD = 200;
	public static final int BURN_COOL_SPEED = 2;
	public static final int UNKNOWN_LIT_DURATION = 0;
	protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
	int litTime;
	int litDuration = 0;
	int cookingProgress;
	int cookingTotalTime;
	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int i) {
			switch (i) {
				case 0:
					return AbstractFurnaceBlockEntity.this.litTime;
				case 1:
					return AbstractFurnaceBlockEntity.this.litDuration;
				case 2:
					return AbstractFurnaceBlockEntity.this.cookingProgress;
				case 3:
					return AbstractFurnaceBlockEntity.this.cookingTotalTime;
				default:
					return 0;
			}
		}

		@Override
		public void set(int i, int j) {
			switch (i) {
				case 0:
					AbstractFurnaceBlockEntity.this.litTime = j;
					break;
				case 1:
					AbstractFurnaceBlockEntity.this.litDuration = j;
					break;
				case 2:
					AbstractFurnaceBlockEntity.this.cookingProgress = j;
					break;
				case 3:
					AbstractFurnaceBlockEntity.this.cookingTotalTime = j;
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	};
	private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap<>();
	private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;

	protected AbstractFurnaceBlockEntity(
		BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType
	) {
		super(blockEntityType, blockPos, blockState);
		this.quickCheck = RecipeManager.createCheck(recipeType);
	}

	private boolean isLit() {
		return this.litTime > 0;
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compoundTag, this.items, provider);
		this.litTime = compoundTag.getShort("BurnTime");
		this.cookingProgress = compoundTag.getShort("CookTime");
		this.cookingTotalTime = compoundTag.getShort("CookTimeTotal");
		this.litDuration = 0;
		CompoundTag compoundTag2 = compoundTag.getCompound("RecipesUsed");

		for (String string : compoundTag2.getAllKeys()) {
			this.recipesUsed.put(ResourceKey.create(Registries.RECIPE, ResourceLocation.parse(string)), compoundTag2.getInt(string));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		compoundTag.putShort("BurnTime", (short)this.litTime);
		compoundTag.putShort("CookTime", (short)this.cookingProgress);
		compoundTag.putShort("CookTimeTotal", (short)this.cookingTotalTime);
		ContainerHelper.saveAllItems(compoundTag, this.items, provider);
		CompoundTag compoundTag2 = new CompoundTag();
		this.recipesUsed.forEach((resourceKey, integer) -> compoundTag2.putInt(resourceKey.location().toString(), integer));
		compoundTag.put("RecipesUsed", compoundTag2);
	}

	public static void serverTick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
		boolean bl = abstractFurnaceBlockEntity.isLit();
		boolean bl2 = false;
		if (abstractFurnaceBlockEntity.isLit()) {
			abstractFurnaceBlockEntity.litTime--;
		}

		ItemStack itemStack = abstractFurnaceBlockEntity.items.get(1);
		ItemStack itemStack2 = abstractFurnaceBlockEntity.items.get(0);
		boolean bl3 = !itemStack2.isEmpty();
		boolean bl4 = !itemStack.isEmpty();
		if (abstractFurnaceBlockEntity.litDuration == 0) {
			abstractFurnaceBlockEntity.litDuration = abstractFurnaceBlockEntity.getBurnDuration(serverLevel.fuelValues(), itemStack);
		}

		if (abstractFurnaceBlockEntity.isLit() || bl4 && bl3) {
			SingleRecipeInput singleRecipeInput = new SingleRecipeInput(itemStack2);
			RecipeHolder<? extends AbstractCookingRecipe> recipeHolder;
			if (bl3) {
				recipeHolder = (RecipeHolder<? extends AbstractCookingRecipe>)abstractFurnaceBlockEntity.quickCheck
					.getRecipeFor(singleRecipeInput, serverLevel)
					.orElse(null);
			} else {
				recipeHolder = null;
			}

			int i = abstractFurnaceBlockEntity.getMaxStackSize();
			if (!abstractFurnaceBlockEntity.isLit() && canBurn(serverLevel.registryAccess(), recipeHolder, singleRecipeInput, abstractFurnaceBlockEntity.items, i)) {
				abstractFurnaceBlockEntity.litTime = abstractFurnaceBlockEntity.getBurnDuration(serverLevel.fuelValues(), itemStack);
				abstractFurnaceBlockEntity.litDuration = abstractFurnaceBlockEntity.litTime;
				if (abstractFurnaceBlockEntity.isLit()) {
					bl2 = true;
					if (bl4) {
						Item item = itemStack.getItem();
						itemStack.shrink(1);
						if (itemStack.isEmpty()) {
							abstractFurnaceBlockEntity.items.set(1, item.getCraftingRemainder());
						}
					}
				}
			}

			if (abstractFurnaceBlockEntity.isLit() && canBurn(serverLevel.registryAccess(), recipeHolder, singleRecipeInput, abstractFurnaceBlockEntity.items, i)) {
				abstractFurnaceBlockEntity.cookingProgress++;
				if (abstractFurnaceBlockEntity.cookingProgress == abstractFurnaceBlockEntity.cookingTotalTime) {
					abstractFurnaceBlockEntity.cookingProgress = 0;
					abstractFurnaceBlockEntity.cookingTotalTime = getTotalCookTime(serverLevel, abstractFurnaceBlockEntity);
					if (burn(serverLevel.registryAccess(), recipeHolder, singleRecipeInput, abstractFurnaceBlockEntity.items, i)) {
						abstractFurnaceBlockEntity.setRecipeUsed(recipeHolder);
					}

					bl2 = true;
				}
			} else {
				abstractFurnaceBlockEntity.cookingProgress = 0;
			}
		} else if (!abstractFurnaceBlockEntity.isLit() && abstractFurnaceBlockEntity.cookingProgress > 0) {
			abstractFurnaceBlockEntity.cookingProgress = Mth.clamp(abstractFurnaceBlockEntity.cookingProgress - 2, 0, abstractFurnaceBlockEntity.cookingTotalTime);
		}

		if (bl != abstractFurnaceBlockEntity.isLit()) {
			bl2 = true;
			blockState = blockState.setValue(AbstractFurnaceBlock.LIT, Boolean.valueOf(abstractFurnaceBlockEntity.isLit()));
			serverLevel.setBlock(blockPos, blockState, 3);
		}

		if (bl2) {
			setChanged(serverLevel, blockPos, blockState);
		}
	}

	private static boolean canBurn(
		RegistryAccess registryAccess,
		@Nullable RecipeHolder<? extends AbstractCookingRecipe> recipeHolder,
		SingleRecipeInput singleRecipeInput,
		NonNullList<ItemStack> nonNullList,
		int i
	) {
		if (!nonNullList.get(0).isEmpty() && recipeHolder != null) {
			ItemStack itemStack = recipeHolder.value().assemble(singleRecipeInput, registryAccess);
			if (itemStack.isEmpty()) {
				return false;
			} else {
				ItemStack itemStack2 = nonNullList.get(2);
				if (itemStack2.isEmpty()) {
					return true;
				} else if (!ItemStack.isSameItemSameComponents(itemStack2, itemStack)) {
					return false;
				} else {
					return itemStack2.getCount() < i && itemStack2.getCount() < itemStack2.getMaxStackSize() ? true : itemStack2.getCount() < itemStack.getMaxStackSize();
				}
			}
		} else {
			return false;
		}
	}

	private static boolean burn(
		RegistryAccess registryAccess,
		@Nullable RecipeHolder<? extends AbstractCookingRecipe> recipeHolder,
		SingleRecipeInput singleRecipeInput,
		NonNullList<ItemStack> nonNullList,
		int i
	) {
		if (recipeHolder != null && canBurn(registryAccess, recipeHolder, singleRecipeInput, nonNullList, i)) {
			ItemStack itemStack = nonNullList.get(0);
			ItemStack itemStack2 = recipeHolder.value().assemble(singleRecipeInput, registryAccess);
			ItemStack itemStack3 = nonNullList.get(2);
			if (itemStack3.isEmpty()) {
				nonNullList.set(2, itemStack2.copy());
			} else if (ItemStack.isSameItemSameComponents(itemStack3, itemStack2)) {
				itemStack3.grow(1);
			}

			if (itemStack.is(Blocks.WET_SPONGE.asItem()) && !nonNullList.get(1).isEmpty() && nonNullList.get(1).is(Items.BUCKET)) {
				nonNullList.set(1, new ItemStack(Items.WATER_BUCKET));
			}

			itemStack.shrink(1);
			return true;
		} else {
			return false;
		}
	}

	protected int getBurnDuration(FuelValues fuelValues, ItemStack itemStack) {
		return fuelValues.burnDuration(itemStack);
	}

	private static int getTotalCookTime(ServerLevel serverLevel, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
		SingleRecipeInput singleRecipeInput = new SingleRecipeInput(abstractFurnaceBlockEntity.getItem(0));
		return (Integer)abstractFurnaceBlockEntity.quickCheck
			.getRecipeFor(singleRecipeInput, serverLevel)
			.map(recipeHolder -> ((AbstractCookingRecipe)recipeHolder.value()).cookingTime())
			.orElse(200);
	}

	@Override
	public int[] getSlotsForFace(Direction direction) {
		if (direction == Direction.DOWN) {
			return SLOTS_FOR_DOWN;
		} else {
			return direction == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
		}
	}

	@Override
	public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
		return this.canPlaceItem(i, itemStack);
	}

	@Override
	public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
		return direction == Direction.DOWN && i == 1 ? itemStack.is(Items.WATER_BUCKET) || itemStack.is(Items.BUCKET) : true;
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> nonNullList) {
		this.items = nonNullList;
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		ItemStack itemStack2 = this.items.get(i);
		boolean bl = !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack2, itemStack);
		this.items.set(i, itemStack);
		itemStack.limitSize(this.getMaxStackSize(itemStack));
		if (i == 0 && !bl && this.level instanceof ServerLevel serverLevel) {
			this.cookingTotalTime = getTotalCookTime(serverLevel, this);
			this.cookingProgress = 0;
			this.setChanged();
		}
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemStack) {
		if (i == 2) {
			return false;
		} else if (i != 1) {
			return true;
		} else {
			ItemStack itemStack2 = this.items.get(1);
			return this.level.fuelValues().isFuel(itemStack) || itemStack.is(Items.BUCKET) && !itemStack2.is(Items.BUCKET);
		}
	}

	@Override
	public void setRecipeUsed(@Nullable RecipeHolder<?> recipeHolder) {
		if (recipeHolder != null) {
			ResourceKey<Recipe<?>> resourceKey = recipeHolder.id();
			this.recipesUsed.addTo(resourceKey, 1);
		}
	}

	@Nullable
	@Override
	public RecipeHolder<?> getRecipeUsed() {
		return null;
	}

	@Override
	public void awardUsedRecipes(Player player, List<ItemStack> list) {
	}

	public void awardUsedRecipesAndPopExperience(ServerPlayer serverPlayer) {
		List<RecipeHolder<?>> list = this.getRecipesToAwardAndPopExperience(serverPlayer.serverLevel(), serverPlayer.position());
		serverPlayer.awardRecipes(list);

		for (RecipeHolder<?> recipeHolder : list) {
			if (recipeHolder != null) {
				serverPlayer.triggerRecipeCrafted(recipeHolder, this.items);
			}
		}

		this.recipesUsed.clear();
	}

	public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel serverLevel, Vec3 vec3) {
		List<RecipeHolder<?>> list = Lists.<RecipeHolder<?>>newArrayList();

		for (Entry<ResourceKey<Recipe<?>>> entry : this.recipesUsed.reference2IntEntrySet()) {
			serverLevel.recipeAccess().byKey((ResourceKey<Recipe<?>>)entry.getKey()).ifPresent(recipeHolder -> {
				list.add(recipeHolder);
				createExperience(serverLevel, vec3, entry.getIntValue(), ((AbstractCookingRecipe)recipeHolder.value()).experience());
			});
		}

		return list;
	}

	private static void createExperience(ServerLevel serverLevel, Vec3 vec3, int i, float f) {
		int j = Mth.floor((float)i * f);
		float g = Mth.frac((float)i * f);
		if (g != 0.0F && Math.random() < (double)g) {
			j++;
		}

		ExperienceOrb.award(serverLevel, vec3, j);
	}

	@Override
	public void fillStackedContents(StackedItemContents stackedItemContents) {
		for (ItemStack itemStack : this.items) {
			stackedItemContents.accountStack(itemStack);
		}
	}
}
