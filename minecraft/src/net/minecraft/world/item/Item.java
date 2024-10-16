package net.minecraft.world.item;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Item implements FeatureElement, ItemLike {
	public static final Codec<Holder<Item>> CODEC = BuiltInRegistries.ITEM
		.holderByNameCodec()
		.validate(holder -> holder.is(Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> "Item must not be minecraft:air") : DataResult.success(holder));
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Map<Block, Item> BY_BLOCK = Maps.<Block, Item>newHashMap();
	public static final ResourceLocation BASE_ATTACK_DAMAGE_ID = ResourceLocation.withDefaultNamespace("base_attack_damage");
	public static final ResourceLocation BASE_ATTACK_SPEED_ID = ResourceLocation.withDefaultNamespace("base_attack_speed");
	public static final int DEFAULT_MAX_STACK_SIZE = 64;
	public static final int ABSOLUTE_MAX_STACK_SIZE = 99;
	public static final int MAX_BAR_WIDTH = 13;
	private final Holder.Reference<Item> builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
	private final DataComponentMap components;
	@Nullable
	private final Item craftingRemainingItem;
	protected final String descriptionId;
	private final FeatureFlagSet requiredFeatures;

	public static int getId(Item item) {
		return item == null ? 0 : BuiltInRegistries.ITEM.getId(item);
	}

	public static Item byId(int i) {
		return BuiltInRegistries.ITEM.byId(i);
	}

	@Deprecated
	public static Item byBlock(Block block) {
		return (Item)BY_BLOCK.getOrDefault(block, Items.AIR);
	}

	public Item(Item.Properties properties) {
		this.descriptionId = properties.effectiveDescriptionId();
		this.components = properties.buildAndValidateComponents(Component.translatable(this.descriptionId), properties.effectiveModel());
		this.craftingRemainingItem = properties.craftingRemainingItem;
		this.requiredFeatures = properties.requiredFeatures;
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

	public DataComponentMap components() {
		return this.components;
	}

	public int getDefaultMaxStackSize() {
		return this.components.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
	}

	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
	}

	public void onDestroyed(ItemEntity itemEntity) {
	}

	public void verifyComponentsAfterLoad(ItemStack itemStack) {
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
		Tool tool = itemStack.get(DataComponents.TOOL);
		return tool != null ? tool.getMiningSpeed(blockState) : 1.0F;
	}

	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
		if (consumable != null) {
			return consumable.startConsuming(player, itemStack, interactionHand);
		} else {
			Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
			return (InteractionResult)(equippable != null && equippable.swappable() ? equippable.swapWithEquipmentSlot(itemStack, player) : InteractionResult.PASS);
		}
	}

	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
		return consumable != null ? consumable.onConsume(level, livingEntity, itemStack) : itemStack;
	}

	public boolean isBarVisible(ItemStack itemStack) {
		return itemStack.isDamaged();
	}

	public int getBarWidth(ItemStack itemStack) {
		return Mth.clamp(Math.round(13.0F - (float)itemStack.getDamageValue() * 13.0F / (float)itemStack.getMaxDamage()), 0, 13);
	}

	public int getBarColor(ItemStack itemStack) {
		int i = itemStack.getMaxDamage();
		float f = Math.max(0.0F, ((float)i - (float)itemStack.getDamageValue()) / (float)i);
		return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
	}

	public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
		return false;
	}

	public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		return false;
	}

	public float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
		return 0.0F;
	}

	@Nullable
	public DamageSource getDamageSource(LivingEntity livingEntity) {
		return null;
	}

	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		return false;
	}

	public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
	}

	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		Tool tool = itemStack.get(DataComponents.TOOL);
		if (tool == null) {
			return false;
		} else {
			if (!level.isClientSide && blockState.getDestroySpeed(level, blockPos) != 0.0F && tool.damagePerBlock() > 0) {
				itemStack.hurtAndBreak(tool.damagePerBlock(), livingEntity, EquipmentSlot.MAINHAND);
			}

			return true;
		}
	}

	public boolean isCorrectToolForDrops(ItemStack itemStack, BlockState blockState) {
		Tool tool = itemStack.get(DataComponents.TOOL);
		return tool != null && tool.isCorrectForDrops(blockState);
	}

	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		return InteractionResult.PASS;
	}

	public String toString() {
		return BuiltInRegistries.ITEM.wrapAsHolder(this).getRegisteredName();
	}

	public final ItemStack getCraftingRemainder() {
		return this.craftingRemainingItem == null ? ItemStack.EMPTY : new ItemStack(this.craftingRemainingItem);
	}

	public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
	}

	public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
		this.onCraftedPostProcess(itemStack, level);
	}

	public void onCraftedPostProcess(ItemStack itemStack, Level level) {
	}

	public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
		Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
		return consumable != null ? consumable.animation() : ItemUseAnimation.NONE;
	}

	public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
		Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
		return consumable != null ? consumable.consumeTicks() : 0;
	}

	public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
		return false;
	}

	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
	}

	public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
		return Optional.empty();
	}

	@VisibleForTesting
	public final String getDescriptionId() {
		return this.descriptionId;
	}

	public final Component getName() {
		return this.components.getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
	}

	public Component getName(ItemStack itemStack) {
		return itemStack.getComponents().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
	}

	public boolean isFoil(ItemStack itemStack) {
		return itemStack.isEnchanted();
	}

	protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
		Vec3 vec3 = player.getEyePosition();
		Vec3 vec32 = vec3.add(player.calculateViewVector(player.getXRot(), player.getYRot()).scale(player.blockInteractionRange()));
		return level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluid, player));
	}

	public boolean useOnRelease(ItemStack itemStack) {
		return false;
	}

	public ItemStack getDefaultInstance() {
		return new ItemStack(this);
	}

	public SoundEvent getBreakingSound() {
		return SoundEvents.ITEM_BREAK;
	}

	public boolean canFitInsideContainerItems() {
		return true;
	}

	@Override
	public FeatureFlagSet requiredFeatures() {
		return this.requiredFeatures;
	}

	public static class Properties {
		private static final DependantName<Item, String> BLOCK_DESCRIPTION_ID = resourceKey -> Util.makeDescriptionId("block", resourceKey.location());
		private static final DependantName<Item, String> ITEM_DESCRIPTION_ID = resourceKey -> Util.makeDescriptionId("item", resourceKey.location());
		private final DataComponentMap.Builder components = DataComponentMap.builder().addAll(DataComponents.COMMON_ITEM_COMPONENTS);
		@Nullable
		Item craftingRemainingItem;
		FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
		@Nullable
		private ResourceKey<Item> id;
		private DependantName<Item, String> descriptionId = ITEM_DESCRIPTION_ID;
		private DependantName<Item, ResourceLocation> model = ResourceKey::location;

		public Item.Properties food(FoodProperties foodProperties) {
			return this.food(foodProperties, Consumables.DEFAULT_FOOD);
		}

		public Item.Properties food(FoodProperties foodProperties, Consumable consumable) {
			return this.component(DataComponents.FOOD, foodProperties).component(DataComponents.CONSUMABLE, consumable);
		}

		public Item.Properties usingConvertsTo(Item item) {
			return this.component(DataComponents.USE_REMAINDER, new UseRemainder(new ItemStack(item)));
		}

		public Item.Properties useCooldown(float f) {
			return this.component(DataComponents.USE_COOLDOWN, new UseCooldown(f));
		}

		public Item.Properties stacksTo(int i) {
			return this.component(DataComponents.MAX_STACK_SIZE, i);
		}

		public Item.Properties durability(int i) {
			this.component(DataComponents.MAX_DAMAGE, i);
			this.component(DataComponents.MAX_STACK_SIZE, 1);
			this.component(DataComponents.DAMAGE, 0);
			return this;
		}

		public Item.Properties craftRemainder(Item item) {
			this.craftingRemainingItem = item;
			return this;
		}

		public Item.Properties rarity(Rarity rarity) {
			return this.component(DataComponents.RARITY, rarity);
		}

		public Item.Properties fireResistant() {
			return this.component(DataComponents.DAMAGE_RESISTANT, new DamageResistant(DamageTypeTags.IS_FIRE));
		}

		public Item.Properties jukeboxPlayable(ResourceKey<JukeboxSong> resourceKey) {
			return this.component(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(new EitherHolder<>(resourceKey), true));
		}

		public Item.Properties enchantable(int i) {
			return this.component(DataComponents.ENCHANTABLE, new Enchantable(i));
		}

		public Item.Properties repairable(Item item) {
			return this.component(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(item.builtInRegistryHolder())));
		}

		public Item.Properties repairable(TagKey<Item> tagKey) {
			HolderGetter<Item> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM);
			return this.component(DataComponents.REPAIRABLE, new Repairable(holderGetter.getOrThrow(tagKey)));
		}

		public Item.Properties equippable(EquipmentSlot equipmentSlot) {
			return this.component(DataComponents.EQUIPPABLE, Equippable.builder(equipmentSlot).build());
		}

		public Item.Properties equippableUnswappable(EquipmentSlot equipmentSlot) {
			return this.component(DataComponents.EQUIPPABLE, Equippable.builder(equipmentSlot).setSwappable(false).build());
		}

		public Item.Properties requiredFeatures(FeatureFlag... featureFlags) {
			this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
			return this;
		}

		public Item.Properties setId(ResourceKey<Item> resourceKey) {
			this.id = resourceKey;
			return this;
		}

		public Item.Properties overrideDescription(String string) {
			this.descriptionId = DependantName.fixed(string);
			return this;
		}

		public Item.Properties useBlockDescriptionPrefix() {
			this.descriptionId = BLOCK_DESCRIPTION_ID;
			return this;
		}

		public Item.Properties useItemDescriptionPrefix() {
			this.descriptionId = ITEM_DESCRIPTION_ID;
			return this;
		}

		protected String effectiveDescriptionId() {
			return this.descriptionId.get((ResourceKey<Item>)Objects.requireNonNull(this.id, "Item id not set"));
		}

		public Item.Properties overrideModel(ResourceLocation resourceLocation) {
			this.model = DependantName.fixed(resourceLocation);
			return this;
		}

		public ResourceLocation effectiveModel() {
			return this.model.get((ResourceKey<Item>)Objects.requireNonNull(this.id, "Item id not set"));
		}

		public <T> Item.Properties component(DataComponentType<T> dataComponentType, T object) {
			this.components.set(dataComponentType, object);
			return this;
		}

		public Item.Properties attributes(ItemAttributeModifiers itemAttributeModifiers) {
			return this.component(DataComponents.ATTRIBUTE_MODIFIERS, itemAttributeModifiers);
		}

		DataComponentMap buildAndValidateComponents(Component component, ResourceLocation resourceLocation) {
			DataComponentMap dataComponentMap = this.components.set(DataComponents.ITEM_NAME, component).set(DataComponents.ITEM_MODEL, resourceLocation).build();
			if (dataComponentMap.has(DataComponents.DAMAGE) && dataComponentMap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
				throw new IllegalStateException("Item cannot have both durability and be stackable");
			} else {
				return dataComponentMap;
			}
		}
	}

	public interface TooltipContext {
		Item.TooltipContext EMPTY = new Item.TooltipContext() {
			@Nullable
			@Override
			public HolderLookup.Provider registries() {
				return null;
			}

			@Override
			public float tickRate() {
				return 20.0F;
			}

			@Nullable
			@Override
			public MapItemSavedData mapData(MapId mapId) {
				return null;
			}
		};

		@Nullable
		HolderLookup.Provider registries();

		float tickRate();

		@Nullable
		MapItemSavedData mapData(MapId mapId);

		static Item.TooltipContext of(@Nullable Level level) {
			return level == null ? EMPTY : new Item.TooltipContext() {
				@Override
				public HolderLookup.Provider registries() {
					return level.registryAccess();
				}

				@Override
				public float tickRate() {
					return level.tickRateManager().tickrate();
				}

				@Override
				public MapItemSavedData mapData(MapId mapId) {
					return level.getMapData(mapId);
				}
			};
		}

		static Item.TooltipContext of(HolderLookup.Provider provider) {
			return new Item.TooltipContext() {
				@Override
				public HolderLookup.Provider registries() {
					return provider;
				}

				@Override
				public float tickRate() {
					return 20.0F;
				}

				@Nullable
				@Override
				public MapItemSavedData mapData(MapId mapId) {
					return null;
				}
			};
		}
	}
}
