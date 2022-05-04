package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Item implements ItemLike {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Map<Block, Item> BY_BLOCK = Maps.<Block, Item>newHashMap();
	protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
	protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
	public static final int MAX_STACK_SIZE = 64;
	public static final int EAT_DURATION = 32;
	public static final int MAX_BAR_WIDTH = 13;
	private final Holder.Reference<Item> builtInRegistryHolder = Registry.ITEM.createIntrusiveHolder(this);
	@Nullable
	protected final CreativeModeTab category;
	private final Rarity rarity;
	private final int maxStackSize;
	private final int maxDamage;
	private final boolean isFireResistant;
	@Nullable
	private final Item craftingRemainingItem;
	@Nullable
	private String descriptionId;
	@Nullable
	private final FoodProperties foodProperties;

	public static int getId(Item item) {
		return item == null ? 0 : Registry.ITEM.getId(item);
	}

	public static Item byId(int i) {
		return Registry.ITEM.byId(i);
	}

	@Deprecated
	public static Item byBlock(Block block) {
		return (Item)BY_BLOCK.getOrDefault(block, Items.AIR);
	}

	public Item(Item.Properties properties) {
		this.category = properties.category;
		this.rarity = properties.rarity;
		this.craftingRemainingItem = properties.craftingRemainingItem;
		this.maxDamage = properties.maxDamage;
		this.maxStackSize = properties.maxStackSize;
		this.foodProperties = properties.foodProperties;
		this.isFireResistant = properties.isFireResistant;
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			String string = this.getClass().getSimpleName();
			if (!string.endsWith("Item")) {
				LOGGER.error("Item classes should end with Item and {} doesn't.", string);
			}
		}
	}

	@Deprecated
	public Holder.Reference<Item> builtInRegistryHolder() {
		return this.builtInRegistryHolder;
	}

	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
	}

	public void onDestroyed(ItemEntity itemEntity) {
	}

	public void verifyTagAfterLoad(CompoundTag compoundTag) {
	}

	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return true;
	}

	@Override
	public Item asItem() {
		return this;
	}

	public InteractionResult useOn(UseOnContext useOnContext) {
		return InteractionResult.PASS;
	}

	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		return 1.0F;
	}

	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		if (this.isEdible()) {
			ItemStack itemStack = player.getItemInHand(interactionHand);
			if (player.canEat(this.getFoodProperties().canAlwaysEat())) {
				player.startUsingItem(interactionHand);
				return InteractionResultHolder.consume(itemStack);
			} else {
				return InteractionResultHolder.fail(itemStack);
			}
		} else {
			return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
		}
	}

	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		return this.isEdible() ? livingEntity.eat(level, itemStack) : itemStack;
	}

	public final int getMaxStackSize() {
		return this.maxStackSize;
	}

	public final int getMaxDamage() {
		return this.maxDamage;
	}

	public boolean canBeDepleted() {
		return this.maxDamage > 0;
	}

	public boolean isBarVisible(ItemStack itemStack) {
		return itemStack.isDamaged();
	}

	public int getBarWidth(ItemStack itemStack) {
		return Math.round(13.0F - (float)itemStack.getDamageValue() * 13.0F / (float)this.maxDamage);
	}

	public int getBarColor(ItemStack itemStack) {
		float f = Math.max(0.0F, ((float)this.maxDamage - (float)itemStack.getDamageValue()) / (float)this.maxDamage);
		return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
	}

	public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
		return false;
	}

	public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		return false;
	}

	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		return false;
	}

	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		return false;
	}

	public boolean isCorrectToolForDrops(BlockState blockState) {
		return false;
	}

	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		return InteractionResult.PASS;
	}

	public Component getDescription() {
		return Component.translatable(this.getDescriptionId());
	}

	public String toString() {
		return Registry.ITEM.getKey(this).getPath();
	}

	protected String getOrCreateDescriptionId() {
		if (this.descriptionId == null) {
			this.descriptionId = Util.makeDescriptionId("item", Registry.ITEM.getKey(this));
		}

		return this.descriptionId;
	}

	public String getDescriptionId() {
		return this.getOrCreateDescriptionId();
	}

	public String getDescriptionId(ItemStack itemStack) {
		return this.getDescriptionId();
	}

	public boolean shouldOverrideMultiplayerNbt() {
		return true;
	}

	@Nullable
	public final Item getCraftingRemainingItem() {
		return this.craftingRemainingItem;
	}

	public boolean hasCraftingRemainingItem() {
		return this.craftingRemainingItem != null;
	}

	public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
	}

	public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
	}

	public boolean isComplex() {
		return false;
	}

	public UseAnim getUseAnimation(ItemStack itemStack) {
		return itemStack.getItem().isEdible() ? UseAnim.EAT : UseAnim.NONE;
	}

	public int getUseDuration(ItemStack itemStack) {
		if (itemStack.getItem().isEdible()) {
			return this.getFoodProperties().isFastFood() ? 16 : 32;
		} else {
			return 0;
		}
	}

	public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
	}

	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
	}

	public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
		return Optional.empty();
	}

	public Component getName(ItemStack itemStack) {
		return Component.translatable(this.getDescriptionId(itemStack));
	}

	public boolean isFoil(ItemStack itemStack) {
		return itemStack.isEnchanted();
	}

	public Rarity getRarity(ItemStack itemStack) {
		if (!itemStack.isEnchanted()) {
			return this.rarity;
		} else {
			switch (this.rarity) {
				case COMMON:
				case UNCOMMON:
					return Rarity.RARE;
				case RARE:
					return Rarity.EPIC;
				case EPIC:
				default:
					return this.rarity;
			}
		}
	}

	public boolean isEnchantable(ItemStack itemStack) {
		return this.getMaxStackSize() == 1 && this.canBeDepleted();
	}

	protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
		float f = player.getXRot();
		float g = player.getYRot();
		Vec3 vec3 = player.getEyePosition();
		float h = Mth.cos(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
		float i = Mth.sin(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
		float j = -Mth.cos(-f * (float) (Math.PI / 180.0));
		float k = Mth.sin(-f * (float) (Math.PI / 180.0));
		float l = i * j;
		float n = h * j;
		double d = 5.0;
		Vec3 vec32 = vec3.add((double)l * 5.0, (double)k * 5.0, (double)n * 5.0);
		return level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluid, player));
	}

	public int getEnchantmentValue() {
		return 0;
	}

	public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
		if (this.allowedIn(creativeModeTab)) {
			nonNullList.add(new ItemStack(this));
		}
	}

	protected boolean allowedIn(CreativeModeTab creativeModeTab) {
		CreativeModeTab creativeModeTab2 = this.getItemCategory();
		return creativeModeTab2 != null && (creativeModeTab == CreativeModeTab.TAB_SEARCH || creativeModeTab == creativeModeTab2);
	}

	@Nullable
	public final CreativeModeTab getItemCategory() {
		return this.category;
	}

	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return false;
	}

	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		return ImmutableMultimap.of();
	}

	public boolean useOnRelease(ItemStack itemStack) {
		return false;
	}

	public ItemStack getDefaultInstance() {
		return new ItemStack(this);
	}

	public boolean isEdible() {
		return this.foodProperties != null;
	}

	@Nullable
	public FoodProperties getFoodProperties() {
		return this.foodProperties;
	}

	public SoundEvent getDrinkingSound() {
		return SoundEvents.GENERIC_DRINK;
	}

	public SoundEvent getEatingSound() {
		return SoundEvents.GENERIC_EAT;
	}

	public boolean isFireResistant() {
		return this.isFireResistant;
	}

	public boolean canBeHurtBy(DamageSource damageSource) {
		return !this.isFireResistant || !damageSource.isFire();
	}

	@Nullable
	public SoundEvent getEquipSound() {
		return null;
	}

	public boolean canFitInsideContainerItems() {
		return true;
	}

	public static class Properties {
		int maxStackSize = 64;
		int maxDamage;
		@Nullable
		Item craftingRemainingItem;
		@Nullable
		CreativeModeTab category;
		Rarity rarity = Rarity.COMMON;
		@Nullable
		FoodProperties foodProperties;
		boolean isFireResistant;

		public Item.Properties food(FoodProperties foodProperties) {
			this.foodProperties = foodProperties;
			return this;
		}

		public Item.Properties stacksTo(int i) {
			if (this.maxDamage > 0) {
				throw new RuntimeException("Unable to have damage AND stack.");
			} else {
				this.maxStackSize = i;
				return this;
			}
		}

		public Item.Properties defaultDurability(int i) {
			return this.maxDamage == 0 ? this.durability(i) : this;
		}

		public Item.Properties durability(int i) {
			this.maxDamage = i;
			this.maxStackSize = 1;
			return this;
		}

		public Item.Properties craftRemainder(Item item) {
			this.craftingRemainingItem = item;
			return this;
		}

		public Item.Properties tab(CreativeModeTab creativeModeTab) {
			this.category = creativeModeTab;
			return this;
		}

		public Item.Properties rarity(Rarity rarity) {
			this.rarity = rarity;
			return this;
		}

		public Item.Properties fireResistant() {
			this.isFireResistant = true;
			return this;
		}
	}
}
