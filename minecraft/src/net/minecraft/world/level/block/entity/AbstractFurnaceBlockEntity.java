package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
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
	protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
	int litTime;
	int litDuration;
	int cookingProgress;
	int cookingTotalTime;
	@Nullable
	private static volatile Map<Item, Integer> fuelCache;
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
	private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
	private final RecipeManager.CachedCheck<Container, ? extends AbstractCookingRecipe> quickCheck;

	protected AbstractFurnaceBlockEntity(
		BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType
	) {
		super(blockEntityType, blockPos, blockState);
		this.quickCheck = RecipeManager.createCheck(recipeType);
	}

	public static void invalidateCache() {
		fuelCache = null;
	}

	public static Map<Item, Integer> getFuel() {
		Map<Item, Integer> map = fuelCache;
		if (map != null) {
			return map;
		} else {
			Map<Item, Integer> map2 = Maps.<Item, Integer>newLinkedHashMap();
			add(map2, Items.LAVA_BUCKET, 20000);
			add(map2, Blocks.COAL_BLOCK, 16000);
			add(map2, Items.BLAZE_ROD, 2400);
			add(map2, Items.COAL, 1600);
			add(map2, Items.CHARCOAL, 1600);
			add(map2, ItemTags.LOGS, 300);
			add(map2, ItemTags.BAMBOO_BLOCKS, 300);
			add(map2, ItemTags.PLANKS, 300);
			add(map2, Blocks.BAMBOO_MOSAIC, 300);
			add(map2, ItemTags.WOODEN_STAIRS, 300);
			add(map2, Blocks.BAMBOO_MOSAIC_STAIRS, 300);
			add(map2, ItemTags.WOODEN_SLABS, 150);
			add(map2, Blocks.BAMBOO_MOSAIC_SLAB, 150);
			add(map2, ItemTags.WOODEN_TRAPDOORS, 300);
			add(map2, ItemTags.WOODEN_PRESSURE_PLATES, 300);
			add(map2, ItemTags.WOODEN_FENCES, 300);
			add(map2, ItemTags.FENCE_GATES, 300);
			add(map2, Blocks.NOTE_BLOCK, 300);
			add(map2, Blocks.BOOKSHELF, 300);
			add(map2, Blocks.CHISELED_BOOKSHELF, 300);
			add(map2, Blocks.LECTERN, 300);
			add(map2, Blocks.JUKEBOX, 300);
			add(map2, Blocks.CHEST, 300);
			add(map2, Blocks.TRAPPED_CHEST, 300);
			add(map2, Blocks.CRAFTING_TABLE, 300);
			add(map2, Blocks.DAYLIGHT_DETECTOR, 300);
			add(map2, ItemTags.BANNERS, 300);
			add(map2, Items.BOW, 300);
			add(map2, Items.FISHING_ROD, 300);
			add(map2, Blocks.LADDER, 300);
			add(map2, ItemTags.SIGNS, 200);
			add(map2, ItemTags.HANGING_SIGNS, 800);
			add(map2, Items.WOODEN_SHOVEL, 200);
			add(map2, Items.WOODEN_SWORD, 200);
			add(map2, Items.WOODEN_HOE, 200);
			add(map2, Items.WOODEN_AXE, 200);
			add(map2, Items.WOODEN_PICKAXE, 200);
			add(map2, ItemTags.WOODEN_DOORS, 200);
			add(map2, ItemTags.BOATS, 1200);
			add(map2, ItemTags.WOOL, 100);
			add(map2, ItemTags.WOODEN_BUTTONS, 100);
			add(map2, Items.STICK, 100);
			add(map2, ItemTags.SAPLINGS, 100);
			add(map2, Items.BOWL, 100);
			add(map2, ItemTags.WOOL_CARPETS, 67);
			add(map2, Blocks.DRIED_KELP_BLOCK, 4001);
			add(map2, Items.CROSSBOW, 300);
			add(map2, Blocks.BAMBOO, 50);
			add(map2, Blocks.DEAD_BUSH, 100);
			add(map2, Blocks.SCAFFOLDING, 50);
			add(map2, Blocks.LOOM, 300);
			add(map2, Blocks.BARREL, 300);
			add(map2, Blocks.CARTOGRAPHY_TABLE, 300);
			add(map2, Blocks.FLETCHING_TABLE, 300);
			add(map2, Blocks.SMITHING_TABLE, 300);
			add(map2, Blocks.COMPOSTER, 300);
			add(map2, Blocks.AZALEA, 100);
			add(map2, Blocks.FLOWERING_AZALEA, 100);
			add(map2, Blocks.MANGROVE_ROOTS, 300);
			fuelCache = map2;
			return map2;
		}
	}

	private static boolean isNeverAFurnaceFuel(Item item) {
		return item.builtInRegistryHolder().is(ItemTags.NON_FLAMMABLE_WOOD);
	}

	private static void add(Map<Item, Integer> map, TagKey<Item> tagKey, int i) {
		for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
			if (!isNeverAFurnaceFuel(holder.value())) {
				map.put(holder.value(), i);
			}
		}
	}

	private static void add(Map<Item, Integer> map, ItemLike itemLike, int i) {
		Item item = itemLike.asItem();
		if (isNeverAFurnaceFuel(item)) {
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				throw (IllegalStateException)Util.pauseInIde(
					new IllegalStateException(
						"A developer tried to explicitly make fire resistant item " + item.getName(null).getString() + " a furnace fuel. That will not work!"
					)
				);
			}
		} else {
			map.put(item, i);
		}
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
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
		boolean bl = abstractFurnaceBlockEntity.isLit();
		boolean bl2 = false;
		if (abstractFurnaceBlockEntity.isLit()) {
			abstractFurnaceBlockEntity.litTime--;
		}

		ItemStack itemStack = abstractFurnaceBlockEntity.items.get(1);
		boolean bl3 = !abstractFurnaceBlockEntity.items.get(0).isEmpty();
		boolean bl4 = !itemStack.isEmpty();
		if (abstractFurnaceBlockEntity.isLit() || bl4 && bl3) {
			RecipeHolder<?> recipeHolder;
			if (bl3) {
				recipeHolder = (RecipeHolder<?>)abstractFurnaceBlockEntity.quickCheck.getRecipeFor(abstractFurnaceBlockEntity, level).orElse(null);
			} else {
				recipeHolder = null;
			}

			int i = abstractFurnaceBlockEntity.getMaxStackSize();
			if (!abstractFurnaceBlockEntity.isLit() && canBurn(level.registryAccess(), recipeHolder, abstractFurnaceBlockEntity.items, i)) {
				abstractFurnaceBlockEntity.litTime = abstractFurnaceBlockEntity.getBurnDuration(itemStack);
				abstractFurnaceBlockEntity.litDuration = abstractFurnaceBlockEntity.litTime;
				if (abstractFurnaceBlockEntity.isLit()) {
					bl2 = true;
					if (bl4) {
						Item item = itemStack.getItem();
						itemStack.shrink(1);
						if (itemStack.isEmpty()) {
							Item item2 = item.getCraftingRemainingItem();
							abstractFurnaceBlockEntity.items.set(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
						}
					}
				}
			}

			if (abstractFurnaceBlockEntity.isLit() && canBurn(level.registryAccess(), recipeHolder, abstractFurnaceBlockEntity.items, i)) {
				abstractFurnaceBlockEntity.cookingProgress++;
				if (abstractFurnaceBlockEntity.cookingProgress == abstractFurnaceBlockEntity.cookingTotalTime) {
					abstractFurnaceBlockEntity.cookingProgress = 0;
					abstractFurnaceBlockEntity.cookingTotalTime = getTotalCookTime(level, abstractFurnaceBlockEntity);
					if (burn(level.registryAccess(), recipeHolder, abstractFurnaceBlockEntity.items, i)) {
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
			level.setBlock(blockPos, blockState, 3);
		}

		if (bl2) {
			setChanged(level, blockPos, blockState);
		}
	}

	private static boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipeHolder, NonNullList<ItemStack> nonNullList, int i) {
		if (!nonNullList.get(0).isEmpty() && recipeHolder != null) {
			ItemStack itemStack = recipeHolder.value().getResultItem(registryAccess);
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

	private static boolean burn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipeHolder, NonNullList<ItemStack> nonNullList, int i) {
		if (recipeHolder != null && canBurn(registryAccess, recipeHolder, nonNullList, i)) {
			ItemStack itemStack = nonNullList.get(0);
			ItemStack itemStack2 = recipeHolder.value().getResultItem(registryAccess);
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

	protected int getBurnDuration(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return 0;
		} else {
			Item item = itemStack.getItem();
			return (Integer)getFuel().getOrDefault(item, 0);
		}
	}

	private static int getTotalCookTime(Level level, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
		return (Integer)abstractFurnaceBlockEntity.quickCheck
			.getRecipeFor(abstractFurnaceBlockEntity, level)
			.map(recipeHolder -> ((AbstractCookingRecipe)recipeHolder.value()).getCookingTime())
			.orElse(200);
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
		boolean bl = !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack2, itemStack);
		this.items.set(i, itemStack);
		if (itemStack.getCount() > this.getMaxStackSize()) {
			itemStack.setCount(this.getMaxStackSize());
		}

		if (i == 0 && !bl) {
			this.cookingTotalTime = getTotalCookTime(this.level, this);
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

		for (Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet()) {
			serverLevel.getRecipeManager().byKey((ResourceLocation)entry.getKey()).ifPresent(recipeHolder -> {
				list.add(recipeHolder);
				createExperience(serverLevel, vec3, entry.getIntValue(), ((AbstractCookingRecipe)recipeHolder.value()).getExperience());
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
	public void fillStackedContents(StackedContents stackedContents) {
		for (ItemStack itemStack : this.items) {
			stackedContents.accountStack(itemStack);
		}
	}
}
