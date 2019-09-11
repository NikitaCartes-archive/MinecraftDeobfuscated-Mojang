package net.minecraft.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Item implements ItemLike {
	public static final Map<Block, Item> BY_BLOCK = Maps.<Block, Item>newHashMap();
	private static final ItemPropertyFunction PROPERTY_DAMAGED = (itemStack, level, livingEntity) -> itemStack.isDamaged() ? 1.0F : 0.0F;
	private static final ItemPropertyFunction PROPERTY_DAMAGE = (itemStack, level, livingEntity) -> Mth.clamp(
			(float)itemStack.getDamageValue() / (float)itemStack.getMaxDamage(), 0.0F, 1.0F
		);
	private static final ItemPropertyFunction PROPERTY_LEFTHANDED = (itemStack, level, livingEntity) -> livingEntity != null
				&& livingEntity.getMainArm() != HumanoidArm.RIGHT
			? 1.0F
			: 0.0F;
	private static final ItemPropertyFunction PROPERTY_COOLDOWN = (itemStack, level, livingEntity) -> livingEntity instanceof Player
			? ((Player)livingEntity).getCooldowns().getCooldownPercent(itemStack.getItem(), 0.0F)
			: 0.0F;
	private static final ItemPropertyFunction PROPERTY_CUSTOM_MODEL_DATA = (itemStack, level, livingEntity) -> itemStack.hasTag()
			? (float)itemStack.getTag().getInt("CustomModelData")
			: 0.0F;
	protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
	protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
	protected static final Random random = new Random();
	private final Map<ResourceLocation, ItemPropertyFunction> properties = Maps.<ResourceLocation, ItemPropertyFunction>newHashMap();
	protected final CreativeModeTab category;
	private final Rarity rarity;
	private final int maxStackSize;
	private final int maxDamage;
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
		this.addProperty(new ResourceLocation("lefthanded"), PROPERTY_LEFTHANDED);
		this.addProperty(new ResourceLocation("cooldown"), PROPERTY_COOLDOWN);
		this.addProperty(new ResourceLocation("custom_model_data"), PROPERTY_CUSTOM_MODEL_DATA);
		this.category = properties.category;
		this.rarity = properties.rarity;
		this.craftingRemainingItem = properties.craftingRemainingItem;
		this.maxDamage = properties.maxDamage;
		this.maxStackSize = properties.maxStackSize;
		this.foodProperties = properties.foodProperties;
		if (this.maxDamage > 0) {
			this.addProperty(new ResourceLocation("damaged"), PROPERTY_DAMAGED);
			this.addProperty(new ResourceLocation("damage"), PROPERTY_DAMAGE);
		}
	}

	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public ItemPropertyFunction getProperty(ResourceLocation resourceLocation) {
		return (ItemPropertyFunction)this.properties.get(resourceLocation);
	}

	@Environment(EnvType.CLIENT)
	public boolean hasProperties() {
		return !this.properties.isEmpty();
	}

	public boolean verifyTagAfterLoad(CompoundTag compoundTag) {
		return false;
	}

	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return true;
	}

	@Override
	public Item asItem() {
		return this;
	}

	public final void addProperty(ResourceLocation resourceLocation, ItemPropertyFunction itemPropertyFunction) {
		this.properties.put(resourceLocation, itemPropertyFunction);
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
				return InteractionResultHolder.success(itemStack);
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

	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		return false;
	}

	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		return false;
	}

	public boolean canDestroySpecial(BlockState blockState) {
		return false;
	}

	public boolean interactEnemy(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public Component getDescription() {
		return new TranslatableComponent(this.getDescriptionId());
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

	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
	}

	public Component getName(ItemStack itemStack) {
		return new TranslatableComponent(this.getDescriptionId(itemStack));
	}

	@Environment(EnvType.CLIENT)
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

	protected static HitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
		float f = player.xRot;
		float g = player.yRot;
		Vec3 vec3 = player.getEyePosition(1.0F);
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
		if (this.allowdedIn(creativeModeTab)) {
			nonNullList.add(new ItemStack(this));
		}
	}

	protected boolean allowdedIn(CreativeModeTab creativeModeTab) {
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

	public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		return HashMultimap.create();
	}

	public boolean useOnRelease(ItemStack itemStack) {
		return itemStack.getItem() == Items.CROSSBOW;
	}

	@Environment(EnvType.CLIENT)
	public ItemStack getDefaultInstance() {
		return new ItemStack(this);
	}

	public boolean is(Tag<Item> tag) {
		return tag.contains(this);
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

	public static class Properties {
		private int maxStackSize = 64;
		private int maxDamage;
		private Item craftingRemainingItem;
		private CreativeModeTab category;
		private Rarity rarity = Rarity.COMMON;
		private FoodProperties foodProperties;

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
	}
}
