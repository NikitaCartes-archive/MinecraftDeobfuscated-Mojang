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
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {
	private static final int[] SLOTS_FOR_UP = new int[]{0};
	private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
	private static final int[] SLOTS_FOR_SIDES = new int[]{1};
	protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
	private int litTime;
	private int litDuration;
	private int cookingProgress;
	private int cookingTotalTime;
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
	private final RecipeType<? extends AbstractCookingRecipe> recipeType;

	protected AbstractFurnaceBlockEntity(
		BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType
	) {
		super(blockEntityType, blockPos, blockState);
		this.recipeType = recipeType;
	}

	public static Map<Item, Integer> getFuel() {
		Map<Item, Integer> map = Maps.<Item, Integer>newLinkedHashMap();
		add(map, Items.LAVA_BUCKET, 20000);
		add(map, Blocks.COAL_BLOCK, 16000);
		add(map, Items.BLAZE_ROD, 2400);
		add(map, Items.COAL, 1600);
		add(map, Items.CHARCOAL, 1600);
		add(map, ItemTags.LOGS, 300);
		add(map, ItemTags.PLANKS, 300);
		add(map, ItemTags.WOODEN_STAIRS, 300);
		add(map, ItemTags.WOODEN_SLABS, 150);
		add(map, ItemTags.WOODEN_TRAPDOORS, 300);
		add(map, ItemTags.WOODEN_PRESSURE_PLATES, 300);
		add(map, Blocks.OAK_FENCE, 300);
		add(map, Blocks.BIRCH_FENCE, 300);
		add(map, Blocks.SPRUCE_FENCE, 300);
		add(map, Blocks.JUNGLE_FENCE, 300);
		add(map, Blocks.DARK_OAK_FENCE, 300);
		add(map, Blocks.ACACIA_FENCE, 300);
		add(map, Blocks.OAK_FENCE_GATE, 300);
		add(map, Blocks.BIRCH_FENCE_GATE, 300);
		add(map, Blocks.SPRUCE_FENCE_GATE, 300);
		add(map, Blocks.JUNGLE_FENCE_GATE, 300);
		add(map, Blocks.DARK_OAK_FENCE_GATE, 300);
		add(map, Blocks.ACACIA_FENCE_GATE, 300);
		add(map, Blocks.NOTE_BLOCK, 300);
		add(map, Blocks.BOOKSHELF, 300);
		add(map, Blocks.LECTERN, 300);
		add(map, Blocks.JUKEBOX, 300);
		add(map, Blocks.CHEST, 300);
		add(map, Blocks.TRAPPED_CHEST, 300);
		add(map, Blocks.CRAFTING_TABLE, 300);
		add(map, Blocks.DAYLIGHT_DETECTOR, 300);
		add(map, ItemTags.BANNERS, 300);
		add(map, Items.BOW, 300);
		add(map, Items.FISHING_ROD, 300);
		add(map, Blocks.LADDER, 300);
		add(map, ItemTags.SIGNS, 200);
		add(map, Items.WOODEN_SHOVEL, 200);
		add(map, Items.WOODEN_SWORD, 200);
		add(map, Items.WOODEN_HOE, 200);
		add(map, Items.WOODEN_AXE, 200);
		add(map, Items.WOODEN_PICKAXE, 200);
		add(map, ItemTags.WOODEN_DOORS, 200);
		add(map, ItemTags.BOATS, 1200);
		add(map, ItemTags.WOOL, 100);
		add(map, ItemTags.WOODEN_BUTTONS, 100);
		add(map, Items.STICK, 100);
		add(map, ItemTags.SAPLINGS, 100);
		add(map, Items.BOWL, 100);
		add(map, ItemTags.CARPETS, 67);
		add(map, Blocks.DRIED_KELP_BLOCK, 4001);
		add(map, Items.CROSSBOW, 300);
		add(map, Blocks.BAMBOO, 50);
		add(map, Blocks.DEAD_BUSH, 100);
		add(map, Blocks.SCAFFOLDING, 400);
		add(map, Blocks.LOOM, 300);
		add(map, Blocks.BARREL, 300);
		add(map, Blocks.CARTOGRAPHY_TABLE, 300);
		add(map, Blocks.FLETCHING_TABLE, 300);
		add(map, Blocks.SMITHING_TABLE, 300);
		add(map, Blocks.COMPOSTER, 300);
		add(map, Blocks.AZALEA, 100);
		add(map, Blocks.FLOWERING_AZALEA, 100);
		return map;
	}

	private static boolean isNeverAFurnaceFuel(Item item) {
		return ItemTags.NON_FLAMMABLE_WOOD.contains(item);
	}

	private static void add(Map<Item, Integer> map, Tag<Item> tag, int i) {
		for (Item item : tag.getValues()) {
			if (!isNeverAFurnaceFuel(item)) {
				map.put(item, i);
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
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compoundTag, this.items);
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
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		compoundTag.putShort("BurnTime", (short)this.litTime);
		compoundTag.putShort("CookTime", (short)this.cookingProgress);
		compoundTag.putShort("CookTimeTotal", (short)this.cookingTotalTime);
		ContainerHelper.saveAllItems(compoundTag, this.items);
		CompoundTag compoundTag2 = new CompoundTag();
		this.recipesUsed.forEach((resourceLocation, integer) -> compoundTag2.putInt(resourceLocation.toString(), integer));
		compoundTag.put("RecipesUsed", compoundTag2);
		return compoundTag;
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
		boolean bl = abstractFurnaceBlockEntity.isLit();
		boolean bl2 = false;
		if (abstractFurnaceBlockEntity.isLit()) {
			abstractFurnaceBlockEntity.litTime--;
		}

		ItemStack itemStack = abstractFurnaceBlockEntity.items.get(1);
		if (abstractFurnaceBlockEntity.isLit() || !itemStack.isEmpty() && !abstractFurnaceBlockEntity.items.get(0).isEmpty()) {
			Recipe<?> recipe = (Recipe<?>)level.getRecipeManager().getRecipeFor(abstractFurnaceBlockEntity.recipeType, abstractFurnaceBlockEntity, level).orElse(null);
			int i = abstractFurnaceBlockEntity.getMaxStackSize();
			if (!abstractFurnaceBlockEntity.isLit() && canBurn(recipe, abstractFurnaceBlockEntity.items, i)) {
				abstractFurnaceBlockEntity.litTime = abstractFurnaceBlockEntity.getBurnDuration(itemStack);
				abstractFurnaceBlockEntity.litDuration = abstractFurnaceBlockEntity.litTime;
				if (abstractFurnaceBlockEntity.isLit()) {
					bl2 = true;
					if (!itemStack.isEmpty()) {
						Item item = itemStack.getItem();
						itemStack.shrink(1);
						if (itemStack.isEmpty()) {
							Item item2 = item.getCraftingRemainingItem();
							abstractFurnaceBlockEntity.items.set(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
						}
					}
				}
			}

			if (abstractFurnaceBlockEntity.isLit() && canBurn(recipe, abstractFurnaceBlockEntity.items, i)) {
				abstractFurnaceBlockEntity.cookingProgress++;
				if (abstractFurnaceBlockEntity.cookingProgress == abstractFurnaceBlockEntity.cookingTotalTime) {
					abstractFurnaceBlockEntity.cookingProgress = 0;
					abstractFurnaceBlockEntity.cookingTotalTime = getTotalCookTime(level, abstractFurnaceBlockEntity.recipeType, abstractFurnaceBlockEntity);
					if (burn(recipe, abstractFurnaceBlockEntity.items, i)) {
						abstractFurnaceBlockEntity.setRecipeUsed(recipe);
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

	private static boolean canBurn(@Nullable Recipe<?> recipe, NonNullList<ItemStack> nonNullList, int i) {
		if (!nonNullList.get(0).isEmpty() && recipe != null) {
			ItemStack itemStack = recipe.getResultItem();
			if (itemStack.isEmpty()) {
				return false;
			} else {
				ItemStack itemStack2 = nonNullList.get(2);
				if (itemStack2.isEmpty()) {
					return true;
				} else if (!itemStack2.sameItem(itemStack)) {
					return false;
				} else {
					return itemStack2.getCount() < i && itemStack2.getCount() < itemStack2.getMaxStackSize() ? true : itemStack2.getCount() < itemStack.getMaxStackSize();
				}
			}
		} else {
			return false;
		}
	}

	private static boolean burn(@Nullable Recipe<?> recipe, NonNullList<ItemStack> nonNullList, int i) {
		if (recipe != null && canBurn(recipe, nonNullList, i)) {
			ItemStack itemStack = nonNullList.get(0);
			ItemStack itemStack2 = recipe.getResultItem();
			ItemStack itemStack3 = nonNullList.get(2);
			if (itemStack3.isEmpty()) {
				nonNullList.set(2, itemStack2.copy());
			} else if (itemStack3.is(itemStack2.getItem())) {
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

	private static int getTotalCookTime(Level level, RecipeType<? extends AbstractCookingRecipe> recipeType, Container container) {
		return (Integer)level.getRecipeManager().getRecipeFor(recipeType, container, level).map(AbstractCookingRecipe::getCookingTime).orElse(200);
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
	public boolean isEmpty() {
		for (ItemStack itemStack : this.items) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getItem(int i) {
		return this.items.get(i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		return ContainerHelper.removeItem(this.items, i, j);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return ContainerHelper.takeItem(this.items, i);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		ItemStack itemStack2 = this.items.get(i);
		boolean bl = !itemStack.isEmpty() && itemStack.sameItem(itemStack2) && ItemStack.tagMatches(itemStack, itemStack2);
		this.items.set(i, itemStack);
		if (itemStack.getCount() > this.getMaxStackSize()) {
			itemStack.setCount(this.getMaxStackSize());
		}

		if (i == 0 && !bl) {
			this.cookingTotalTime = getTotalCookTime(this.level, this.recipeType, this);
			this.cookingProgress = 0;
			this.setChanged();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.level.getBlockEntity(this.worldPosition) != this
			? false
			: player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) <= 64.0;
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
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public void setRecipeUsed(@Nullable Recipe<?> recipe) {
		if (recipe != null) {
			ResourceLocation resourceLocation = recipe.getId();
			this.recipesUsed.addTo(resourceLocation, 1);
		}
	}

	@Nullable
	@Override
	public Recipe<?> getRecipeUsed() {
		return null;
	}

	@Override
	public void awardUsedRecipes(Player player) {
	}

	public void awardUsedRecipesAndPopExperience(ServerPlayer serverPlayer) {
		List<Recipe<?>> list = this.getRecipesToAwardAndPopExperience(serverPlayer.getLevel(), serverPlayer.position());
		serverPlayer.awardRecipes(list);
		this.recipesUsed.clear();
	}

	public List<Recipe<?>> getRecipesToAwardAndPopExperience(ServerLevel serverLevel, Vec3 vec3) {
		List<Recipe<?>> list = Lists.<Recipe<?>>newArrayList();

		for (Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet()) {
			serverLevel.getRecipeManager().byKey((ResourceLocation)entry.getKey()).ifPresent(recipe -> {
				list.add(recipe);
				createExperience(serverLevel, vec3, entry.getIntValue(), ((AbstractCookingRecipe)recipe).getExperience());
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
