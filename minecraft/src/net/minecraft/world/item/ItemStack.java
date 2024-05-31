package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.NullOps;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public final class ItemStack implements DataComponentHolder {
	public static final Codec<Holder<Item>> ITEM_NON_AIR_CODEC = BuiltInRegistries.ITEM
		.holderByNameCodec()
		.validate(holder -> holder.is(Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> "Item must not be minecraft:air") : DataResult.success(holder));
	public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(
		() -> RecordCodecBuilder.create(
				instance -> instance.group(
							ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
							ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
							DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(itemStack -> itemStack.components.asPatch())
						)
						.apply(instance, ItemStack::new)
			)
	);
	public static final Codec<ItemStack> SINGLE_ITEM_CODEC = Codec.lazyInitialized(
		() -> RecordCodecBuilder.create(
				instance -> instance.group(
							ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
							DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(itemStack -> itemStack.components.asPatch())
						)
						.apply(instance, (holder, dataComponentPatch) -> new ItemStack(holder, 1, dataComponentPatch))
			)
	);
	public static final Codec<ItemStack> STRICT_CODEC = CODEC.validate(ItemStack::validateStrict);
	public static final Codec<ItemStack> STRICT_SINGLE_ITEM_CODEC = SINGLE_ITEM_CODEC.validate(ItemStack::validateStrict);
	public static final Codec<ItemStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
		.xmap(optional -> (ItemStack)optional.orElse(ItemStack.EMPTY), itemStack -> itemStack.isEmpty() ? Optional.empty() : Optional.of(itemStack));
	public static final Codec<ItemStack> SIMPLE_ITEM_CODEC = ITEM_NON_AIR_CODEC.xmap(ItemStack::new, ItemStack::getItemHolder);
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
		private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);

		public ItemStack decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			int i = registryFriendlyByteBuf.readVarInt();
			if (i <= 0) {
				return ItemStack.EMPTY;
			} else {
				Holder<Item> holder = ITEM_STREAM_CODEC.decode(registryFriendlyByteBuf);
				DataComponentPatch dataComponentPatch = DataComponentPatch.STREAM_CODEC.decode(registryFriendlyByteBuf);
				return new ItemStack(holder, i, dataComponentPatch);
			}
		}

		public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack) {
			if (itemStack.isEmpty()) {
				registryFriendlyByteBuf.writeVarInt(0);
			} else {
				registryFriendlyByteBuf.writeVarInt(itemStack.getCount());
				ITEM_STREAM_CODEC.encode(registryFriendlyByteBuf, itemStack.getItemHolder());
				DataComponentPatch.STREAM_CODEC.encode(registryFriendlyByteBuf, itemStack.components.asPatch());
			}
		}
	};
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
		public ItemStack decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			ItemStack itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
			if (itemStack.isEmpty()) {
				throw new DecoderException("Empty ItemStack not allowed");
			} else {
				return itemStack;
			}
		}

		public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack) {
			if (itemStack.isEmpty()) {
				throw new EncoderException("Empty ItemStack not allowed");
			} else {
				ItemStack.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, itemStack);
			}
		}
	};
	public static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> OPTIONAL_LIST_STREAM_CODEC = OPTIONAL_STREAM_CODEC.apply(
		ByteBufCodecs.collection(NonNullList::createWithCapacity)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> LIST_STREAM_CODEC = STREAM_CODEC.apply(
		ByteBufCodecs.collection(NonNullList::createWithCapacity)
	);
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final ItemStack EMPTY = new ItemStack((Void)null);
	private static final Component DISABLED_ITEM_TOOLTIP = Component.translatable("item.disabled").withStyle(ChatFormatting.RED);
	private int count;
	private int popTime;
	@Deprecated
	@Nullable
	private final Item item;
	final PatchedDataComponentMap components;
	@Nullable
	private Entity entityRepresentation;

	private static DataResult<ItemStack> validateStrict(ItemStack itemStack) {
		DataResult<Unit> dataResult = validateComponents(itemStack.getComponents());
		if (dataResult.isError()) {
			return dataResult.map(unit -> itemStack);
		} else {
			return itemStack.getCount() > itemStack.getMaxStackSize()
				? DataResult.error(() -> "Item stack with stack size of " + itemStack.getCount() + " was larger than maximum: " + itemStack.getMaxStackSize())
				: DataResult.success(itemStack);
		}
	}

	public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> validatedStreamCodec(StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec) {
		return new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
			public ItemStack decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
				ItemStack itemStack = streamCodec.decode(registryFriendlyByteBuf);
				if (!itemStack.isEmpty()) {
					RegistryOps<Unit> registryOps = registryFriendlyByteBuf.registryAccess().createSerializationContext(NullOps.INSTANCE);
					ItemStack.CODEC.encodeStart(registryOps, itemStack).getOrThrow(DecoderException::new);
				}

				return itemStack;
			}

			public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack) {
				streamCodec.encode(registryFriendlyByteBuf, itemStack);
			}
		};
	}

	public Optional<TooltipComponent> getTooltipImage() {
		return this.getItem().getTooltipImage(this);
	}

	@Override
	public DataComponentMap getComponents() {
		return (DataComponentMap)(!this.isEmpty() ? this.components : DataComponentMap.EMPTY);
	}

	public DataComponentMap getPrototype() {
		return !this.isEmpty() ? this.getItem().components() : DataComponentMap.EMPTY;
	}

	public DataComponentPatch getComponentsPatch() {
		return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
	}

	public ItemStack(ItemLike itemLike) {
		this(itemLike, 1);
	}

	public ItemStack(Holder<Item> holder) {
		this(holder.value(), 1);
	}

	public ItemStack(Holder<Item> holder, int i, DataComponentPatch dataComponentPatch) {
		this(holder.value(), i, PatchedDataComponentMap.fromPatch(holder.value().components(), dataComponentPatch));
	}

	public ItemStack(Holder<Item> holder, int i) {
		this(holder.value(), i);
	}

	public ItemStack(ItemLike itemLike, int i) {
		this(itemLike, i, new PatchedDataComponentMap(itemLike.asItem().components()));
	}

	private ItemStack(ItemLike itemLike, int i, PatchedDataComponentMap patchedDataComponentMap) {
		this.item = itemLike.asItem();
		this.count = i;
		this.components = patchedDataComponentMap;
		this.getItem().verifyComponentsAfterLoad(this);
	}

	private ItemStack(@Nullable Void void_) {
		this.item = null;
		this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
	}

	public static DataResult<Unit> validateComponents(DataComponentMap dataComponentMap) {
		if (dataComponentMap.has(DataComponents.MAX_DAMAGE) && dataComponentMap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
			return DataResult.error(() -> "Item cannot be both damageable and stackable");
		} else {
			ItemContainerContents itemContainerContents = dataComponentMap.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

			for (ItemStack itemStack : itemContainerContents.nonEmptyItems()) {
				int i = itemStack.getCount();
				int j = itemStack.getMaxStackSize();
				if (i > j) {
					return DataResult.error(() -> "Item stack with count of " + i + " was larger than maximum: " + j);
				}
			}

			return DataResult.success(Unit.INSTANCE);
		}
	}

	public static Optional<ItemStack> parse(HolderLookup.Provider provider, Tag tag) {
		return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
			.resultOrPartial(string -> LOGGER.error("Tried to load invalid item: '{}'", string));
	}

	public static ItemStack parseOptional(HolderLookup.Provider provider, CompoundTag compoundTag) {
		return compoundTag.isEmpty() ? EMPTY : (ItemStack)parse(provider, compoundTag).orElse(EMPTY);
	}

	public boolean isEmpty() {
		return this == EMPTY || this.item == Items.AIR || this.count <= 0;
	}

	public boolean isItemEnabled(FeatureFlagSet featureFlagSet) {
		return this.isEmpty() || this.getItem().isEnabled(featureFlagSet);
	}

	public ItemStack split(int i) {
		int j = Math.min(i, this.getCount());
		ItemStack itemStack = this.copyWithCount(j);
		this.shrink(j);
		return itemStack;
	}

	public ItemStack copyAndClear() {
		if (this.isEmpty()) {
			return EMPTY;
		} else {
			ItemStack itemStack = this.copy();
			this.setCount(0);
			return itemStack;
		}
	}

	public Item getItem() {
		return this.isEmpty() ? Items.AIR : this.item;
	}

	public Holder<Item> getItemHolder() {
		return this.getItem().builtInRegistryHolder();
	}

	public boolean is(TagKey<Item> tagKey) {
		return this.getItem().builtInRegistryHolder().is(tagKey);
	}

	public boolean is(Item item) {
		return this.getItem() == item;
	}

	public boolean is(Predicate<Holder<Item>> predicate) {
		return predicate.test(this.getItem().builtInRegistryHolder());
	}

	public boolean is(Holder<Item> holder) {
		return this.getItem().builtInRegistryHolder() == holder;
	}

	public boolean is(HolderSet<Item> holderSet) {
		return holderSet.contains(this.getItemHolder());
	}

	public Stream<TagKey<Item>> getTags() {
		return this.getItem().builtInRegistryHolder().tags();
	}

	public InteractionResult useOn(UseOnContext useOnContext) {
		Player player = useOnContext.getPlayer();
		BlockPos blockPos = useOnContext.getClickedPos();
		if (player != null && !player.getAbilities().mayBuild && !this.canPlaceOnBlockInAdventureMode(new BlockInWorld(useOnContext.getLevel(), blockPos, false))) {
			return InteractionResult.PASS;
		} else {
			Item item = this.getItem();
			InteractionResult interactionResult = item.useOn(useOnContext);
			if (player != null && interactionResult.indicateItemUse()) {
				player.awardStat(Stats.ITEM_USED.get(item));
			}

			return interactionResult;
		}
	}

	public float getDestroySpeed(BlockState blockState) {
		return this.getItem().getDestroySpeed(this, blockState);
	}

	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		return this.getItem().use(level, player, interactionHand);
	}

	public ItemStack finishUsingItem(Level level, LivingEntity livingEntity) {
		return this.getItem().finishUsingItem(this, level, livingEntity);
	}

	public Tag save(HolderLookup.Provider provider, Tag tag) {
		if (this.isEmpty()) {
			throw new IllegalStateException("Cannot encode empty ItemStack");
		} else {
			return CODEC.encode(this, provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
		}
	}

	public Tag save(HolderLookup.Provider provider) {
		if (this.isEmpty()) {
			throw new IllegalStateException("Cannot encode empty ItemStack");
		} else {
			return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
		}
	}

	public Tag saveOptional(HolderLookup.Provider provider) {
		return (Tag)(this.isEmpty() ? new CompoundTag() : this.save(provider, new CompoundTag()));
	}

	public int getMaxStackSize() {
		return this.getOrDefault(DataComponents.MAX_STACK_SIZE, Integer.valueOf(1));
	}

	public boolean isStackable() {
		return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
	}

	public boolean isDamageableItem() {
		return this.has(DataComponents.MAX_DAMAGE) && !this.has(DataComponents.UNBREAKABLE) && this.has(DataComponents.DAMAGE);
	}

	public boolean isDamaged() {
		return this.isDamageableItem() && this.getDamageValue() > 0;
	}

	public int getDamageValue() {
		return Mth.clamp(this.getOrDefault(DataComponents.DAMAGE, Integer.valueOf(0)), 0, this.getMaxDamage());
	}

	public void setDamageValue(int i) {
		this.set(DataComponents.DAMAGE, Mth.clamp(i, 0, this.getMaxDamage()));
	}

	public int getMaxDamage() {
		return this.getOrDefault(DataComponents.MAX_DAMAGE, Integer.valueOf(0));
	}

	public void hurtAndBreak(int i, ServerLevel serverLevel, @Nullable ServerPlayer serverPlayer, Consumer<Item> consumer) {
		if (this.isDamageableItem()) {
			if (serverPlayer == null || !serverPlayer.hasInfiniteMaterials()) {
				if (i > 0) {
					i = EnchantmentHelper.processDurabilityChange(serverLevel, this, i);
					if (i <= 0) {
						return;
					}
				}

				if (serverPlayer != null && i != 0) {
					CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, this, this.getDamageValue() + i);
				}

				int j = this.getDamageValue() + i;
				this.setDamageValue(j);
				if (j >= this.getMaxDamage()) {
					Item item = this.getItem();
					this.shrink(1);
					consumer.accept(item);
				}
			}
		}
	}

	public void hurtAndBreak(int i, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
		if (livingEntity.level() instanceof ServerLevel serverLevel) {
			this.hurtAndBreak(
				i, serverLevel, livingEntity instanceof ServerPlayer serverPlayer ? serverPlayer : null, item -> livingEntity.onEquippedItemBroken(item, equipmentSlot)
			);
		}
	}

	public ItemStack hurtAndConvertOnBreak(int i, ItemLike itemLike, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
		this.hurtAndBreak(i, livingEntity, equipmentSlot);
		if (this.isEmpty()) {
			ItemStack itemStack = this.transmuteCopyIgnoreEmpty(itemLike, 1);
			if (itemStack.isDamageableItem()) {
				itemStack.setDamageValue(0);
			}

			return itemStack;
		} else {
			return this;
		}
	}

	public boolean isBarVisible() {
		return this.getItem().isBarVisible(this);
	}

	public int getBarWidth() {
		return this.getItem().getBarWidth(this);
	}

	public int getBarColor() {
		return this.getItem().getBarColor(this);
	}

	public boolean overrideStackedOnOther(Slot slot, ClickAction clickAction, Player player) {
		return this.getItem().overrideStackedOnOther(this, slot, clickAction, player);
	}

	public boolean overrideOtherStackedOnMe(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		return this.getItem().overrideOtherStackedOnMe(this, itemStack, slot, clickAction, player, slotAccess);
	}

	public boolean hurtEnemy(LivingEntity livingEntity, Player player) {
		Item item = this.getItem();
		if (item.hurtEnemy(this, livingEntity, player)) {
			player.awardStat(Stats.ITEM_USED.get(item));
			return true;
		} else {
			return false;
		}
	}

	public void postHurtEnemy(LivingEntity livingEntity, Player player) {
		this.getItem().postHurtEnemy(this, livingEntity, player);
	}

	public void mineBlock(Level level, BlockState blockState, BlockPos blockPos, Player player) {
		Item item = this.getItem();
		if (item.mineBlock(this, level, blockState, blockPos, player)) {
			player.awardStat(Stats.ITEM_USED.get(item));
		}
	}

	public boolean isCorrectToolForDrops(BlockState blockState) {
		return this.getItem().isCorrectToolForDrops(this, blockState);
	}

	public InteractionResult interactLivingEntity(Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		return this.getItem().interactLivingEntity(this, player, livingEntity, interactionHand);
	}

	public ItemStack copy() {
		if (this.isEmpty()) {
			return EMPTY;
		} else {
			ItemStack itemStack = new ItemStack(this.getItem(), this.count, this.components.copy());
			itemStack.setPopTime(this.getPopTime());
			return itemStack;
		}
	}

	public ItemStack copyWithCount(int i) {
		if (this.isEmpty()) {
			return EMPTY;
		} else {
			ItemStack itemStack = this.copy();
			itemStack.setCount(i);
			return itemStack;
		}
	}

	public ItemStack transmuteCopy(ItemLike itemLike) {
		return this.transmuteCopy(itemLike, this.getCount());
	}

	public ItemStack transmuteCopy(ItemLike itemLike, int i) {
		return this.isEmpty() ? EMPTY : this.transmuteCopyIgnoreEmpty(itemLike, i);
	}

	private ItemStack transmuteCopyIgnoreEmpty(ItemLike itemLike, int i) {
		return new ItemStack(itemLike.asItem().builtInRegistryHolder(), i, this.components.asPatch());
	}

	public static boolean matches(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack == itemStack2) {
			return true;
		} else {
			return itemStack.getCount() != itemStack2.getCount() ? false : isSameItemSameComponents(itemStack, itemStack2);
		}
	}

	@Deprecated
	public static boolean listMatches(List<ItemStack> list, List<ItemStack> list2) {
		if (list.size() != list2.size()) {
			return false;
		} else {
			for (int i = 0; i < list.size(); i++) {
				if (!matches((ItemStack)list.get(i), (ItemStack)list2.get(i))) {
					return false;
				}
			}

			return true;
		}
	}

	public static boolean isSameItem(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack.is(itemStack2.getItem());
	}

	public static boolean isSameItemSameComponents(ItemStack itemStack, ItemStack itemStack2) {
		if (!itemStack.is(itemStack2.getItem())) {
			return false;
		} else {
			return itemStack.isEmpty() && itemStack2.isEmpty() ? true : Objects.equals(itemStack.components, itemStack2.components);
		}
	}

	public static MapCodec<ItemStack> lenientOptionalFieldOf(String string) {
		return CODEC.lenientOptionalFieldOf(string)
			.xmap(optional -> (ItemStack)optional.orElse(EMPTY), itemStack -> itemStack.isEmpty() ? Optional.empty() : Optional.of(itemStack));
	}

	public static int hashItemAndComponents(@Nullable ItemStack itemStack) {
		if (itemStack != null) {
			int i = 31 + itemStack.getItem().hashCode();
			return 31 * i + itemStack.getComponents().hashCode();
		} else {
			return 0;
		}
	}

	@Deprecated
	public static int hashStackList(List<ItemStack> list) {
		int i = 0;

		for (ItemStack itemStack : list) {
			i = i * 31 + hashItemAndComponents(itemStack);
		}

		return i;
	}

	public String getDescriptionId() {
		return this.getItem().getDescriptionId(this);
	}

	public String toString() {
		return this.getCount() + " " + this.getItem();
	}

	public void inventoryTick(Level level, Entity entity, int i, boolean bl) {
		if (this.popTime > 0) {
			this.popTime--;
		}

		if (this.getItem() != null) {
			this.getItem().inventoryTick(this, level, entity, i, bl);
		}
	}

	public void onCraftedBy(Level level, Player player, int i) {
		player.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), i);
		this.getItem().onCraftedBy(this, level, player);
	}

	public void onCraftedBySystem(Level level) {
		this.getItem().onCraftedPostProcess(this, level);
	}

	public int getUseDuration(LivingEntity livingEntity) {
		return this.getItem().getUseDuration(this, livingEntity);
	}

	public UseAnim getUseAnimation() {
		return this.getItem().getUseAnimation(this);
	}

	public void releaseUsing(Level level, LivingEntity livingEntity, int i) {
		this.getItem().releaseUsing(this, level, livingEntity, i);
	}

	public boolean useOnRelease() {
		return this.getItem().useOnRelease(this);
	}

	@Nullable
	public <T> T set(DataComponentType<? super T> dataComponentType, @Nullable T object) {
		return this.components.set(dataComponentType, object);
	}

	@Nullable
	public <T, U> T update(DataComponentType<T> dataComponentType, T object, U object2, BiFunction<T, U, T> biFunction) {
		return this.set(dataComponentType, (T)biFunction.apply(this.getOrDefault(dataComponentType, object), object2));
	}

	@Nullable
	public <T> T update(DataComponentType<T> dataComponentType, T object, UnaryOperator<T> unaryOperator) {
		T object2 = this.getOrDefault(dataComponentType, object);
		return this.set(dataComponentType, (T)unaryOperator.apply(object2));
	}

	@Nullable
	public <T> T remove(DataComponentType<? extends T> dataComponentType) {
		return this.components.remove(dataComponentType);
	}

	public void applyComponentsAndValidate(DataComponentPatch dataComponentPatch) {
		DataComponentPatch dataComponentPatch2 = this.components.asPatch();
		this.components.applyPatch(dataComponentPatch);
		Optional<Error<ItemStack>> optional = validateStrict(this).error();
		if (optional.isPresent()) {
			LOGGER.error("Failed to apply component patch '{}' to item: '{}'", dataComponentPatch, ((Error)optional.get()).message());
			this.components.restorePatch(dataComponentPatch2);
		} else {
			this.getItem().verifyComponentsAfterLoad(this);
		}
	}

	public void applyComponents(DataComponentPatch dataComponentPatch) {
		this.components.applyPatch(dataComponentPatch);
		this.getItem().verifyComponentsAfterLoad(this);
	}

	public void applyComponents(DataComponentMap dataComponentMap) {
		this.components.setAll(dataComponentMap);
		this.getItem().verifyComponentsAfterLoad(this);
	}

	public Component getHoverName() {
		Component component = this.get(DataComponents.CUSTOM_NAME);
		if (component != null) {
			return component;
		} else {
			Component component2 = this.get(DataComponents.ITEM_NAME);
			return component2 != null ? component2 : this.getItem().getName(this);
		}
	}

	private <T extends TooltipProvider> void addToTooltip(
		DataComponentType<T> dataComponentType, Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag
	) {
		T tooltipProvider = (T)this.get(dataComponentType);
		if (tooltipProvider != null) {
			tooltipProvider.addToTooltip(tooltipContext, consumer, tooltipFlag);
		}
	}

	public List<Component> getTooltipLines(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		if (!tooltipFlag.isCreative() && this.has(DataComponents.HIDE_TOOLTIP)) {
			return List.of();
		} else {
			List<Component> list = Lists.<Component>newArrayList();
			MutableComponent mutableComponent = Component.empty().append(this.getHoverName()).withStyle(this.getRarity().color());
			if (this.has(DataComponents.CUSTOM_NAME)) {
				mutableComponent.withStyle(ChatFormatting.ITALIC);
			}

			list.add(mutableComponent);
			if (!tooltipFlag.isAdvanced() && !this.has(DataComponents.CUSTOM_NAME) && this.is(Items.FILLED_MAP)) {
				MapId mapId = this.get(DataComponents.MAP_ID);
				if (mapId != null) {
					list.add(MapItem.getTooltipForId(mapId));
				}
			}

			Consumer<Component> consumer = list::add;
			if (!this.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
				this.getItem().appendHoverText(this, tooltipContext, list, tooltipFlag);
			}

			this.addToTooltip(DataComponents.JUKEBOX_PLAYABLE, tooltipContext, consumer, tooltipFlag);
			this.addToTooltip(DataComponents.TRIM, tooltipContext, consumer, tooltipFlag);
			this.addToTooltip(DataComponents.STORED_ENCHANTMENTS, tooltipContext, consumer, tooltipFlag);
			this.addToTooltip(DataComponents.ENCHANTMENTS, tooltipContext, consumer, tooltipFlag);
			this.addToTooltip(DataComponents.DYED_COLOR, tooltipContext, consumer, tooltipFlag);
			this.addToTooltip(DataComponents.LORE, tooltipContext, consumer, tooltipFlag);
			this.addAttributeTooltips(consumer, player);
			this.addToTooltip(DataComponents.UNBREAKABLE, tooltipContext, consumer, tooltipFlag);
			AdventureModePredicate adventureModePredicate = this.get(DataComponents.CAN_BREAK);
			if (adventureModePredicate != null && adventureModePredicate.showInTooltip()) {
				consumer.accept(CommonComponents.EMPTY);
				consumer.accept(AdventureModePredicate.CAN_BREAK_HEADER);
				adventureModePredicate.addToTooltip(consumer);
			}

			AdventureModePredicate adventureModePredicate2 = this.get(DataComponents.CAN_PLACE_ON);
			if (adventureModePredicate2 != null && adventureModePredicate2.showInTooltip()) {
				consumer.accept(CommonComponents.EMPTY);
				consumer.accept(AdventureModePredicate.CAN_PLACE_HEADER);
				adventureModePredicate2.addToTooltip(consumer);
			}

			if (tooltipFlag.isAdvanced()) {
				if (this.isDamaged()) {
					list.add(Component.translatable("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
				}

				list.add(Component.literal(BuiltInRegistries.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
				int i = this.components.size();
				if (i > 0) {
					list.add(Component.translatable("item.components", i).withStyle(ChatFormatting.DARK_GRAY));
				}
			}

			if (player != null && !this.getItem().isEnabled(player.level().enabledFeatures())) {
				list.add(DISABLED_ITEM_TOOLTIP);
			}

			return list;
		}
	}

	private void addAttributeTooltips(Consumer<Component> consumer, @Nullable Player player) {
		ItemAttributeModifiers itemAttributeModifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		if (itemAttributeModifiers.showInTooltip()) {
			for (EquipmentSlotGroup equipmentSlotGroup : EquipmentSlotGroup.values()) {
				MutableBoolean mutableBoolean = new MutableBoolean(true);
				this.forEachModifier(equipmentSlotGroup, (holder, attributeModifier) -> {
					if (mutableBoolean.isTrue()) {
						consumer.accept(CommonComponents.EMPTY);
						consumer.accept(Component.translatable("item.modifiers." + equipmentSlotGroup.getSerializedName()).withStyle(ChatFormatting.GRAY));
						mutableBoolean.setFalse();
					}

					this.addModifierTooltip(consumer, player, holder, attributeModifier);
				});
			}
		}
	}

	private void addModifierTooltip(Consumer<Component> consumer, @Nullable Player player, Holder<Attribute> holder, AttributeModifier attributeModifier) {
		double d = attributeModifier.amount();
		boolean bl = false;
		if (player != null) {
			if (attributeModifier.is(Item.BASE_ATTACK_DAMAGE_ID)) {
				d += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
				bl = true;
			} else if (attributeModifier.is(Item.BASE_ATTACK_SPEED_ID)) {
				d += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
				bl = true;
			}
		}

		double e;
		if (attributeModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
			|| attributeModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
			e = d * 100.0;
		} else if (holder.is(Attributes.KNOCKBACK_RESISTANCE)) {
			e = d * 10.0;
		} else {
			e = d;
		}

		if (bl) {
			consumer.accept(
				CommonComponents.space()
					.append(
						Component.translatable(
							"attribute.modifier.equals." + attributeModifier.operation().id(),
							ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e),
							Component.translatable(holder.value().getDescriptionId())
						)
					)
					.withStyle(ChatFormatting.DARK_GREEN)
			);
		} else if (d > 0.0) {
			consumer.accept(
				Component.translatable(
						"attribute.modifier.plus." + attributeModifier.operation().id(),
						ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e),
						Component.translatable(holder.value().getDescriptionId())
					)
					.withStyle(holder.value().getStyle(true))
			);
		} else if (d < 0.0) {
			consumer.accept(
				Component.translatable(
						"attribute.modifier.take." + attributeModifier.operation().id(),
						ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-e),
						Component.translatable(holder.value().getDescriptionId())
					)
					.withStyle(holder.value().getStyle(false))
			);
		}
	}

	public boolean hasFoil() {
		Boolean boolean_ = this.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
		return boolean_ != null ? boolean_ : this.getItem().isFoil(this);
	}

	public Rarity getRarity() {
		Rarity rarity = this.getOrDefault(DataComponents.RARITY, Rarity.COMMON);
		if (!this.isEnchanted()) {
			return rarity;
		} else {
			return switch (rarity) {
				case COMMON, UNCOMMON -> Rarity.RARE;
				case RARE -> Rarity.EPIC;
				default -> rarity;
			};
		}
	}

	public boolean isEnchantable() {
		if (!this.getItem().isEnchantable(this)) {
			return false;
		} else {
			ItemEnchantments itemEnchantments = this.get(DataComponents.ENCHANTMENTS);
			return itemEnchantments != null && itemEnchantments.isEmpty();
		}
	}

	public void enchant(Holder<Enchantment> holder, int i) {
		EnchantmentHelper.updateEnchantments(this, mutable -> mutable.upgrade(holder, i));
	}

	public boolean isEnchanted() {
		return !this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
	}

	public ItemEnchantments getEnchantments() {
		return this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
	}

	public boolean isFramed() {
		return this.entityRepresentation instanceof ItemFrame;
	}

	public void setEntityRepresentation(@Nullable Entity entity) {
		if (!this.isEmpty()) {
			this.entityRepresentation = entity;
		}
	}

	@Nullable
	public ItemFrame getFrame() {
		return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
	}

	@Nullable
	public Entity getEntityRepresentation() {
		return !this.isEmpty() ? this.entityRepresentation : null;
	}

	public void forEachModifier(EquipmentSlotGroup equipmentSlotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
		ItemAttributeModifiers itemAttributeModifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		if (!itemAttributeModifiers.modifiers().isEmpty()) {
			itemAttributeModifiers.forEach(equipmentSlotGroup, biConsumer);
		} else {
			this.getItem().getDefaultAttributeModifiers().forEach(equipmentSlotGroup, biConsumer);
		}

		EnchantmentHelper.forEachModifier(this, equipmentSlotGroup, biConsumer);
	}

	public void forEachModifier(EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
		ItemAttributeModifiers itemAttributeModifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		if (!itemAttributeModifiers.modifiers().isEmpty()) {
			itemAttributeModifiers.forEach(equipmentSlot, biConsumer);
		} else {
			this.getItem().getDefaultAttributeModifiers().forEach(equipmentSlot, biConsumer);
		}

		EnchantmentHelper.forEachModifier(this, equipmentSlot, biConsumer);
	}

	public Component getDisplayName() {
		MutableComponent mutableComponent = Component.empty().append(this.getHoverName());
		if (this.has(DataComponents.CUSTOM_NAME)) {
			mutableComponent.withStyle(ChatFormatting.ITALIC);
		}

		MutableComponent mutableComponent2 = ComponentUtils.wrapInSquareBrackets(mutableComponent);
		if (!this.isEmpty()) {
			mutableComponent2.withStyle(this.getRarity().color())
				.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this))));
		}

		return mutableComponent2;
	}

	public boolean canPlaceOnBlockInAdventureMode(BlockInWorld blockInWorld) {
		AdventureModePredicate adventureModePredicate = this.get(DataComponents.CAN_PLACE_ON);
		return adventureModePredicate != null && adventureModePredicate.test(blockInWorld);
	}

	public boolean canBreakBlockInAdventureMode(BlockInWorld blockInWorld) {
		AdventureModePredicate adventureModePredicate = this.get(DataComponents.CAN_BREAK);
		return adventureModePredicate != null && adventureModePredicate.test(blockInWorld);
	}

	public int getPopTime() {
		return this.popTime;
	}

	public void setPopTime(int i) {
		this.popTime = i;
	}

	public int getCount() {
		return this.isEmpty() ? 0 : this.count;
	}

	public void setCount(int i) {
		this.count = i;
	}

	public void limitSize(int i) {
		if (!this.isEmpty() && this.getCount() > i) {
			this.setCount(i);
		}
	}

	public void grow(int i) {
		this.setCount(this.getCount() + i);
	}

	public void shrink(int i) {
		this.grow(-i);
	}

	public void consume(int i, @Nullable LivingEntity livingEntity) {
		if (livingEntity == null || !livingEntity.hasInfiniteMaterials()) {
			this.shrink(i);
		}
	}

	public ItemStack consumeAndReturn(int i, @Nullable LivingEntity livingEntity) {
		ItemStack itemStack = this.copyWithCount(i);
		this.consume(i, livingEntity);
		return itemStack;
	}

	public void onUseTick(Level level, LivingEntity livingEntity, int i) {
		this.getItem().onUseTick(level, livingEntity, this, i);
	}

	public void onDestroyed(ItemEntity itemEntity) {
		this.getItem().onDestroyed(itemEntity);
	}

	public SoundEvent getDrinkingSound() {
		return this.getItem().getDrinkingSound();
	}

	public SoundEvent getEatingSound() {
		return this.getItem().getEatingSound();
	}

	public SoundEvent getBreakingSound() {
		return this.getItem().getBreakingSound();
	}

	public boolean canBeHurtBy(DamageSource damageSource) {
		return !this.has(DataComponents.FIRE_RESISTANT) || !damageSource.is(DamageTypeTags.IS_FIRE);
	}
}
