package net.minecraft.world.level.block.entity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.PotatoRefineryMenu;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.PotatoRefinementRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PotatoRefineryBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PotatoRefineryBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible {
	protected static final int SLOT_INPUT = 0;
	protected static final int SLOT_BOTTLE_INPUT = 2;
	protected static final int SLOT_FUEL = 1;
	protected static final int SLOT_RESULT = 3;
	public static final int DATA_LIT_TIME = 0;
	private static final int[] SLOTS_FOR_UP = new int[]{0};
	private static final int[] SLOTS_FOR_DOWN = new int[]{3, 1};
	private static final int[] SLOTS_FOR_SIDES = new int[]{1, 2};
	public static final int DATA_LIT_DURATION = 1;
	public static final int DATA_COOKING_PROGRESS = 2;
	public static final int DATA_COOKING_TOTAL_TIME = 3;
	public static final int NUM_DATA_VALUES = 4;
	public static final int BURN_TIME_STANDARD = 200;
	public static final int BURN_COOL_SPEED = 2;
	protected NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
	int litTime;
	int litDuration;
	int cookingProgress;
	int cookingTotalTime;
	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int i) {
			switch (i) {
				case 0:
					return PotatoRefineryBlockEntity.this.litTime;
				case 1:
					return PotatoRefineryBlockEntity.this.litDuration;
				case 2:
					return PotatoRefineryBlockEntity.this.cookingProgress;
				case 3:
					return PotatoRefineryBlockEntity.this.cookingTotalTime;
				default:
					return 0;
			}
		}

		@Override
		public void set(int i, int j) {
			switch (i) {
				case 0:
					PotatoRefineryBlockEntity.this.litTime = j;
					break;
				case 1:
					PotatoRefineryBlockEntity.this.litDuration = j;
					break;
				case 2:
					PotatoRefineryBlockEntity.this.cookingProgress = j;
					break;
				case 3:
					PotatoRefineryBlockEntity.this.cookingTotalTime = j;
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	};
	private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
	private float storedExperience;
	private final SingleKeyCache<PotatoRefineryBlockEntity.RecipeKey, PotatoRefineryBlockEntity.SomeKindOfRecipe> newQuickCheck;

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.potato_refinery");
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new PotatoRefineryMenu(i, inventory, this, this.dataAccess);
	}

	public PotatoRefineryBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.POTATO_REFINERY, blockPos, blockState);
		RecipeManager.CachedCheck<Container, PotatoRefinementRecipe> cachedCheck = RecipeManager.createCheck(RecipeType.POTATO_REFINEMENT);
		this.newQuickCheck = Util.singleKeyCache(recipeKey -> {
			boolean bl = !recipeKey.inputStack.isEmpty() && !recipeKey.bottleInputStack.isEmpty();
			if (bl) {
				ItemStack itemStack = recipeKey.bottleInputStack;
				if (recipeKey.inputStack.is(Items.POTATO_OIL) && !itemStack.isEmpty()) {
					ItemStack itemStack2 = itemStack.copyWithCount(1);
					LubricationComponent.lubricate(itemStack2);
					return new PotatoRefineryBlockEntity.LubricationRecipe(itemStack.copyWithCount(1), itemStack2);
				}

				RecipeHolder<PotatoRefinementRecipe> recipeHolder = (RecipeHolder<PotatoRefinementRecipe>)cachedCheck.getRecipeFor(this, recipeKey.level).orElse(null);
				if (recipeHolder != null) {
					return new PotatoRefineryBlockEntity.NormalRecipe(recipeHolder);
				}
			}

			return null;
		});
	}

	private PotatoRefineryBlockEntity.SomeKindOfRecipe quickCheckRecipe(Level level) {
		return this.newQuickCheck.getValue(new PotatoRefineryBlockEntity.RecipeKey(level, this.items.get(0), this.items.get(2)));
	}

	public static Map<Item, Integer> getFuel() {
		return FurnaceBlockEntity.getFuel();
	}

	public float harvestExperience() {
		float f = this.storedExperience;
		this.storedExperience = 0.0F;
		return f;
	}

	private boolean isLit() {
		return this.litTime > 0;
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.load(compoundTag, provider);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compoundTag, this.items, provider);
		this.litTime = compoundTag.getShort("BurnTime");
		this.cookingProgress = compoundTag.getShort("CookTime");
		this.cookingTotalTime = compoundTag.getShort("CookTimeTotal");
		this.litDuration = this.getBurnDuration(this.items.get(1));
		CompoundTag compoundTag2 = compoundTag.getCompound("RecipesUsed");

		for (String string : compoundTag2.getAllKeys()) {
			this.recipesUsed.put(new ResourceLocation(string), compoundTag2.getInt(string));
		}

		this.storedExperience = compoundTag.contains("StoredExperience") ? compoundTag.getFloat("StoredExperience") : 0.0F;
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		compoundTag.putShort("BurnTime", (short)this.litTime);
		compoundTag.putShort("CookTime", (short)this.cookingProgress);
		compoundTag.putShort("CookTimeTotal", (short)this.cookingTotalTime);
		ContainerHelper.saveAllItems(compoundTag, this.items, provider);
		CompoundTag compoundTag2 = new CompoundTag();
		this.recipesUsed.forEach((resourceLocation, integer) -> compoundTag2.putInt(resourceLocation.toString(), integer));
		compoundTag.put("RecipesUsed", compoundTag2);
		compoundTag.putFloat("StoredExperience", this.storedExperience);
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, PotatoRefineryBlockEntity potatoRefineryBlockEntity) {
		boolean bl = potatoRefineryBlockEntity.isLit();
		boolean bl2 = false;
		if (potatoRefineryBlockEntity.isLit()) {
			potatoRefineryBlockEntity.litTime--;
		}

		ItemStack itemStack = potatoRefineryBlockEntity.items.get(1);
		boolean bl3 = potatoRefineryBlockEntity.hasIngredients();
		boolean bl4 = !itemStack.isEmpty();
		if (potatoRefineryBlockEntity.isLit() || bl4 && bl3) {
			PotatoRefineryBlockEntity.SomeKindOfRecipe someKindOfRecipe = potatoRefineryBlockEntity.quickCheckRecipe(level);
			int i = potatoRefineryBlockEntity.getMaxStackSize();
			if (!potatoRefineryBlockEntity.isLit() && canRefine(level.registryAccess(), someKindOfRecipe, potatoRefineryBlockEntity.items, i)) {
				potatoRefineryBlockEntity.litTime = potatoRefineryBlockEntity.getBurnDuration(itemStack);
				potatoRefineryBlockEntity.litDuration = potatoRefineryBlockEntity.litTime;
				if (potatoRefineryBlockEntity.isLit()) {
					bl2 = true;
					if (bl4) {
						Item item = itemStack.getItem();
						itemStack.shrink(1);
						if (itemStack.isEmpty()) {
							Item item2 = item.getCraftingRemainingItem();
							potatoRefineryBlockEntity.items.set(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
						}
					}
				}
			}

			if (potatoRefineryBlockEntity.isLit() && canRefine(level.registryAccess(), someKindOfRecipe, potatoRefineryBlockEntity.items, i)) {
				potatoRefineryBlockEntity.cookingProgress++;
				if (potatoRefineryBlockEntity.cookingProgress == potatoRefineryBlockEntity.cookingTotalTime) {
					potatoRefineryBlockEntity.cookingProgress = 0;
					potatoRefineryBlockEntity.cookingTotalTime = getTotalRefinementTime(level, potatoRefineryBlockEntity);
					if (refine(level.registryAccess(), someKindOfRecipe, potatoRefineryBlockEntity.items, i)
						&& someKindOfRecipe instanceof PotatoRefineryBlockEntity.NormalRecipe normalRecipe) {
						potatoRefineryBlockEntity.setRecipeUsed(normalRecipe.recipeHolder);
					}

					bl2 = true;
				}
			} else {
				potatoRefineryBlockEntity.cookingProgress = 0;
			}
		} else if (!potatoRefineryBlockEntity.isLit() && potatoRefineryBlockEntity.cookingProgress > 0) {
			potatoRefineryBlockEntity.cookingProgress = Mth.clamp(potatoRefineryBlockEntity.cookingProgress - 2, 0, potatoRefineryBlockEntity.cookingTotalTime);
		}

		if (bl != potatoRefineryBlockEntity.isLit()) {
			bl2 = true;
			blockState = blockState.setValue(PotatoRefineryBlock.LIT, Boolean.valueOf(potatoRefineryBlockEntity.isLit()));
			level.setBlock(blockPos, blockState, 3);
		}

		if (bl2) {
			setChanged(level, blockPos, blockState);
		}
	}

	private boolean hasIngredients() {
		return !this.items.get(0).isEmpty() && !this.items.get(2).isEmpty();
	}

	private static boolean canRefine(
		RegistryAccess registryAccess, @Nullable PotatoRefineryBlockEntity.SomeKindOfRecipe someKindOfRecipe, NonNullList<ItemStack> nonNullList, int i
	) {
		if (!nonNullList.get(0).isEmpty() && !nonNullList.get(2).isEmpty() && someKindOfRecipe != null) {
			ItemStack itemStack = someKindOfRecipe.getResultItem(registryAccess);
			if (itemStack.isEmpty()) {
				return false;
			} else {
				ItemStack itemStack2 = nonNullList.get(3);
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

	private static boolean refine(
		RegistryAccess registryAccess, @Nullable PotatoRefineryBlockEntity.SomeKindOfRecipe someKindOfRecipe, NonNullList<ItemStack> nonNullList, int i
	) {
		if (someKindOfRecipe != null && canRefine(registryAccess, someKindOfRecipe, nonNullList, i)) {
			ItemStack itemStack = nonNullList.get(0);
			ItemStack itemStack2 = nonNullList.get(2);
			ItemStack itemStack3 = someKindOfRecipe.getResultItem(registryAccess);
			ItemStack itemStack4 = nonNullList.get(3);
			if (itemStack4.isEmpty()) {
				nonNullList.set(3, itemStack3.copy());
			} else if (ItemStack.isSameItemSameComponents(itemStack4, itemStack3)) {
				itemStack4.grow(1);
			}

			itemStack.shrink(1);
			itemStack2.shrink(1);
			return true;
		} else {
			return false;
		}
	}

	protected int getBurnDuration(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return 0;
		} else {
			Item item = itemStack.getItem();
			return (Integer)getFuel().getOrDefault(item, 0);
		}
	}

	private static int getTotalRefinementTime(Level level, PotatoRefineryBlockEntity potatoRefineryBlockEntity) {
		PotatoRefineryBlockEntity.SomeKindOfRecipe someKindOfRecipe = potatoRefineryBlockEntity.quickCheckRecipe(level);
		return someKindOfRecipe != null ? someKindOfRecipe.getTotalRefinementTime() : 20;
	}

	public static boolean isFuel(ItemStack itemStack) {
		return getFuel().containsKey(itemStack.getItem());
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
		boolean bl = itemStack.isEmpty() || !ItemStack.isSameItemSameComponents(itemStack2, itemStack);
		this.items.set(i, itemStack);
		if (itemStack.getCount() > this.getMaxStackSize()) {
			itemStack.setCount(this.getMaxStackSize());
		}

		if ((i == 0 || i == 2) && bl) {
			this.cookingTotalTime = getTotalRefinementTime(this.level, this);
			this.cookingProgress = 0;
			this.setChanged();
		}
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemStack) {
		if (i == 3) {
			return false;
		} else if (i != 1) {
			return true;
		} else {
			ItemStack itemStack2 = this.items.get(1);
			return isFuel(itemStack) || itemStack.is(Items.BUCKET) && !itemStack2.is(Items.BUCKET);
		}
	}

	@Override
	public void setRecipeUsed(@Nullable RecipeHolder<?> recipeHolder) {
		if (recipeHolder != null) {
			ResourceLocation resourceLocation = recipeHolder.id();
			this.recipesUsed.addTo(resourceLocation, 1);
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

	@Override
	public void fillStackedContents(StackedContents stackedContents) {
		for (ItemStack itemStack : this.items) {
			stackedContents.accountStack(itemStack);
		}
	}

	static record LubricationRecipe(ItemStack itemStack, ItemStack result) implements PotatoRefineryBlockEntity.SomeKindOfRecipe {
		@Override
		public ItemStack getResultItem(RegistryAccess registryAccess) {
			return this.result;
		}

		@Override
		public int getTotalRefinementTime() {
			return 20;
		}
	}

	static record NormalRecipe(RecipeHolder<PotatoRefinementRecipe> recipeHolder) implements PotatoRefineryBlockEntity.SomeKindOfRecipe {

		@Override
		public ItemStack getResultItem(RegistryAccess registryAccess) {
			return this.recipeHolder.value().getResultItem(registryAccess);
		}

		@Override
		public int getTotalRefinementTime() {
			return this.recipeHolder.value().getRefinementTime();
		}
	}

	static record RecipeKey(Level level, ItemStack inputStack, ItemStack bottleInputStack) {
	}

	interface SomeKindOfRecipe {
		ItemStack getResultItem(RegistryAccess registryAccess);

		int getTotalRefinementTime();
	}
}
