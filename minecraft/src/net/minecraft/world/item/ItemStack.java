package net.minecraft.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ItemStack {
	public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Registry.ITEM.fieldOf("id").forGetter(itemStack -> itemStack.item),
					Codec.INT.fieldOf("Count").forGetter(itemStack -> itemStack.count),
					CompoundTag.CODEC.optionalFieldOf("tag").forGetter(itemStack -> Optional.ofNullable(itemStack.tag))
				)
				.apply(instance, ItemStack::new)
	);
	private static final Logger LOGGER = LogManager.getLogger();
	public static final ItemStack EMPTY = new ItemStack((Item)null);
	public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(
		new DecimalFormat("#.##"), decimalFormat -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
	);
	private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);
	private int count;
	private int popTime;
	@Deprecated
	private final Item item;
	private CompoundTag tag;
	private boolean emptyCacheFlag;
	private Entity entityRepresentation;
	private BlockInWorld cachedBreakBlock;
	private boolean cachedBreakBlockResult;
	private BlockInWorld cachedPlaceBlock;
	private boolean cachedPlaceBlockResult;

	public ItemStack(ItemLike itemLike) {
		this(itemLike, 1);
	}

	private ItemStack(ItemLike itemLike, int i, Optional<CompoundTag> optional) {
		this(itemLike, i);
		optional.ifPresent(this::setTag);
	}

	public ItemStack(ItemLike itemLike, int i) {
		this.item = itemLike == null ? null : itemLike.asItem();
		this.count = i;
		if (this.item != null && this.item.canBeDepleted()) {
			this.setDamageValue(this.getDamageValue());
		}

		this.updateEmptyCacheFlag();
	}

	private void updateEmptyCacheFlag() {
		this.emptyCacheFlag = false;
		this.emptyCacheFlag = this.isEmpty();
	}

	private ItemStack(CompoundTag compoundTag) {
		this.item = Registry.ITEM.get(new ResourceLocation(compoundTag.getString("id")));
		this.count = compoundTag.getByte("Count");
		if (compoundTag.contains("tag", 10)) {
			this.tag = compoundTag.getCompound("tag");
			this.getItem().verifyTagAfterLoad(compoundTag);
		}

		if (this.getItem().canBeDepleted()) {
			this.setDamageValue(this.getDamageValue());
		}

		this.updateEmptyCacheFlag();
	}

	public static ItemStack of(CompoundTag compoundTag) {
		try {
			return new ItemStack(compoundTag);
		} catch (RuntimeException var2) {
			LOGGER.debug("Tried to load invalid item: {}", compoundTag, var2);
			return EMPTY;
		}
	}

	public boolean isEmpty() {
		if (this == EMPTY) {
			return true;
		} else {
			return this.getItem() == null || this.getItem() == Items.AIR ? true : this.count <= 0;
		}
	}

	public ItemStack split(int i) {
		int j = Math.min(i, this.count);
		ItemStack itemStack = this.copy();
		itemStack.setCount(j);
		this.shrink(j);
		return itemStack;
	}

	public Item getItem() {
		return this.emptyCacheFlag ? Items.AIR : this.item;
	}

	public InteractionResult useOn(UseOnContext useOnContext) {
		Player player = useOnContext.getPlayer();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockInWorld blockInWorld = new BlockInWorld(useOnContext.getLevel(), blockPos, false);
		if (player != null && !player.abilities.mayBuild && !this.hasAdventureModePlaceTagForBlock(useOnContext.getLevel().getTagManager(), blockInWorld)) {
			return InteractionResult.PASS;
		} else {
			Item item = this.getItem();
			InteractionResult interactionResult = item.useOn(useOnContext);
			if (player != null && interactionResult == InteractionResult.SUCCESS) {
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

	public CompoundTag save(CompoundTag compoundTag) {
		ResourceLocation resourceLocation = Registry.ITEM.getKey(this.getItem());
		compoundTag.putString("id", resourceLocation == null ? "minecraft:air" : resourceLocation.toString());
		compoundTag.putByte("Count", (byte)this.count);
		if (this.tag != null) {
			compoundTag.put("tag", this.tag.copy());
		}

		return compoundTag;
	}

	public int getMaxStackSize() {
		return this.getItem().getMaxStackSize();
	}

	public boolean isStackable() {
		return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
	}

	public boolean isDamageableItem() {
		if (!this.emptyCacheFlag && this.getItem().getMaxDamage() > 0) {
			CompoundTag compoundTag = this.getTag();
			return compoundTag == null || !compoundTag.getBoolean("Unbreakable");
		} else {
			return false;
		}
	}

	public boolean isDamaged() {
		return this.isDamageableItem() && this.getDamageValue() > 0;
	}

	public int getDamageValue() {
		return this.tag == null ? 0 : this.tag.getInt("Damage");
	}

	public void setDamageValue(int i) {
		this.getOrCreateTag().putInt("Damage", Math.max(0, i));
	}

	public int getMaxDamage() {
		return this.getItem().getMaxDamage();
	}

	public boolean hurt(int i, Random random, @Nullable ServerPlayer serverPlayer) {
		if (!this.isDamageableItem()) {
			return false;
		} else {
			if (i > 0) {
				int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, this);
				int k = 0;

				for (int l = 0; j > 0 && l < i; l++) {
					if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(this, j, random)) {
						k++;
					}
				}

				i -= k;
				if (i <= 0) {
					return false;
				}
			}

			if (serverPlayer != null && i != 0) {
				CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, this, this.getDamageValue() + i);
			}

			int j = this.getDamageValue() + i;
			this.setDamageValue(j);
			return j >= this.getMaxDamage();
		}
	}

	public <T extends LivingEntity> void hurtAndBreak(int i, T livingEntity, Consumer<T> consumer) {
		if (!livingEntity.level.isClientSide && (!(livingEntity instanceof Player) || !((Player)livingEntity).abilities.instabuild)) {
			if (this.isDamageableItem()) {
				if (this.hurt(i, livingEntity.getRandom(), livingEntity instanceof ServerPlayer ? (ServerPlayer)livingEntity : null)) {
					consumer.accept(livingEntity);
					Item item = this.getItem();
					this.shrink(1);
					if (livingEntity instanceof Player) {
						((Player)livingEntity).awardStat(Stats.ITEM_BROKEN.get(item));
					}

					this.setDamageValue(0);
				}
			}
		}
	}

	public void hurtEnemy(LivingEntity livingEntity, Player player) {
		Item item = this.getItem();
		if (item.hurtEnemy(this, livingEntity, player)) {
			player.awardStat(Stats.ITEM_USED.get(item));
		}
	}

	public void mineBlock(Level level, BlockState blockState, BlockPos blockPos, Player player) {
		Item item = this.getItem();
		if (item.mineBlock(this, level, blockState, blockPos, player)) {
			player.awardStat(Stats.ITEM_USED.get(item));
		}
	}

	public boolean canDestroySpecial(BlockState blockState) {
		return this.getItem().canDestroySpecial(blockState);
	}

	public boolean interactEnemy(Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
		return this.getItem().interactEnemy(this, player, livingEntity, interactionHand);
	}

	public ItemStack copy() {
		if (this.isEmpty()) {
			return EMPTY;
		} else {
			ItemStack itemStack = new ItemStack(this.getItem(), this.count);
			itemStack.setPopTime(this.getPopTime());
			if (this.tag != null) {
				itemStack.tag = this.tag.copy();
			}

			return itemStack;
		}
	}

	public static boolean tagMatches(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack.isEmpty() && itemStack2.isEmpty()) {
			return true;
		} else if (itemStack.isEmpty() || itemStack2.isEmpty()) {
			return false;
		} else {
			return itemStack.tag == null && itemStack2.tag != null ? false : itemStack.tag == null || itemStack.tag.equals(itemStack2.tag);
		}
	}

	public static boolean matches(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack.isEmpty() && itemStack2.isEmpty()) {
			return true;
		} else {
			return !itemStack.isEmpty() && !itemStack2.isEmpty() ? itemStack.matches(itemStack2) : false;
		}
	}

	private boolean matches(ItemStack itemStack) {
		if (this.count != itemStack.count) {
			return false;
		} else if (this.getItem() != itemStack.getItem()) {
			return false;
		} else {
			return this.tag == null && itemStack.tag != null ? false : this.tag == null || this.tag.equals(itemStack.tag);
		}
	}

	public static boolean isSame(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack == itemStack2) {
			return true;
		} else {
			return !itemStack.isEmpty() && !itemStack2.isEmpty() ? itemStack.sameItem(itemStack2) : false;
		}
	}

	public static boolean isSameIgnoreDurability(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack == itemStack2) {
			return true;
		} else {
			return !itemStack.isEmpty() && !itemStack2.isEmpty() ? itemStack.sameItemStackIgnoreDurability(itemStack2) : false;
		}
	}

	public boolean sameItem(ItemStack itemStack) {
		return !itemStack.isEmpty() && this.getItem() == itemStack.getItem();
	}

	public boolean sameItemStackIgnoreDurability(ItemStack itemStack) {
		return !this.isDamageableItem() ? this.sameItem(itemStack) : !itemStack.isEmpty() && this.getItem() == itemStack.getItem();
	}

	public String getDescriptionId() {
		return this.getItem().getDescriptionId(this);
	}

	public String toString() {
		return this.count + " " + this.getItem();
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

	public int getUseDuration() {
		return this.getItem().getUseDuration(this);
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

	public boolean hasTag() {
		return !this.emptyCacheFlag && this.tag != null && !this.tag.isEmpty();
	}

	@Nullable
	public CompoundTag getTag() {
		return this.tag;
	}

	public CompoundTag getOrCreateTag() {
		if (this.tag == null) {
			this.setTag(new CompoundTag());
		}

		return this.tag;
	}

	public CompoundTag getOrCreateTagElement(String string) {
		if (this.tag != null && this.tag.contains(string, 10)) {
			return this.tag.getCompound(string);
		} else {
			CompoundTag compoundTag = new CompoundTag();
			this.addTagElement(string, compoundTag);
			return compoundTag;
		}
	}

	@Nullable
	public CompoundTag getTagElement(String string) {
		return this.tag != null && this.tag.contains(string, 10) ? this.tag.getCompound(string) : null;
	}

	public void removeTagKey(String string) {
		if (this.tag != null && this.tag.contains(string)) {
			this.tag.remove(string);
			if (this.tag.isEmpty()) {
				this.tag = null;
			}
		}
	}

	public ListTag getEnchantmentTags() {
		return this.tag != null ? this.tag.getList("Enchantments", 10) : new ListTag();
	}

	public void setTag(@Nullable CompoundTag compoundTag) {
		this.tag = compoundTag;
		if (this.getItem().canBeDepleted()) {
			this.setDamageValue(this.getDamageValue());
		}
	}

	public Component getHoverName() {
		CompoundTag compoundTag = this.getTagElement("display");
		if (compoundTag != null && compoundTag.contains("Name", 8)) {
			try {
				Component component = Component.Serializer.fromJson(compoundTag.getString("Name"));
				if (component != null) {
					return component;
				}

				compoundTag.remove("Name");
			} catch (JsonParseException var3) {
				compoundTag.remove("Name");
			}
		}

		return this.getItem().getName(this);
	}

	public ItemStack setHoverName(@Nullable Component component) {
		CompoundTag compoundTag = this.getOrCreateTagElement("display");
		if (component != null) {
			compoundTag.putString("Name", Component.Serializer.toJson(component));
		} else {
			compoundTag.remove("Name");
		}

		return this;
	}

	public void resetHoverName() {
		CompoundTag compoundTag = this.getTagElement("display");
		if (compoundTag != null) {
			compoundTag.remove("Name");
			if (compoundTag.isEmpty()) {
				this.removeTagKey("display");
			}
		}

		if (this.tag != null && this.tag.isEmpty()) {
			this.tag = null;
		}
	}

	public boolean hasCustomHoverName() {
		CompoundTag compoundTag = this.getTagElement("display");
		return compoundTag != null && compoundTag.contains("Name", 8);
	}

	@Environment(EnvType.CLIENT)
	public List<Component> getTooltipLines(@Nullable Player player, TooltipFlag tooltipFlag) {
		List<Component> list = Lists.<Component>newArrayList();
		MutableComponent mutableComponent = new TextComponent("").append(this.getHoverName()).withStyle(this.getRarity().color);
		if (this.hasCustomHoverName()) {
			mutableComponent.withStyle(ChatFormatting.ITALIC);
		}

		list.add(mutableComponent);
		if (!tooltipFlag.isAdvanced() && !this.hasCustomHoverName() && this.getItem() == Items.FILLED_MAP) {
			list.add(new TextComponent("#" + MapItem.getMapId(this)).withStyle(ChatFormatting.GRAY));
		}

		int i = 0;
		if (this.hasTag() && this.tag.contains("HideFlags", 99)) {
			i = this.tag.getInt("HideFlags");
		}

		if ((i & 32) == 0) {
			this.getItem().appendHoverText(this, player == null ? null : player.level, list, tooltipFlag);
		}

		if (this.hasTag()) {
			if ((i & 1) == 0) {
				appendEnchantmentNames(list, this.getEnchantmentTags());
			}

			if (this.tag.contains("display", 10)) {
				CompoundTag compoundTag = this.tag.getCompound("display");
				if (compoundTag.contains("color", 3)) {
					if (tooltipFlag.isAdvanced()) {
						list.add(new TranslatableComponent("item.color", String.format("#%06X", compoundTag.getInt("color"))).withStyle(ChatFormatting.GRAY));
					} else {
						list.add(new TranslatableComponent("item.dyed").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
					}
				}

				if (compoundTag.getTagType("Lore") == 9) {
					ListTag listTag = compoundTag.getList("Lore", 8);

					for (int j = 0; j < listTag.size(); j++) {
						String string = listTag.getString(j);

						try {
							MutableComponent mutableComponent2 = Component.Serializer.fromJson(string);
							if (mutableComponent2 != null) {
								list.add(ComponentUtils.mergeStyles(mutableComponent2, LORE_STYLE));
							}
						} catch (JsonParseException var19) {
							compoundTag.remove("Lore");
						}
					}
				}
			}
		}

		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			Multimap<Attribute, AttributeModifier> multimap = this.getAttributeModifiers(equipmentSlot);
			if (!multimap.isEmpty() && (i & 2) == 0) {
				list.add(TextComponent.EMPTY);
				list.add(new TranslatableComponent("item.modifiers." + equipmentSlot.getName()).withStyle(ChatFormatting.GRAY));

				for (Entry<Attribute, AttributeModifier> entry : multimap.entries()) {
					AttributeModifier attributeModifier = (AttributeModifier)entry.getValue();
					double d = attributeModifier.getAmount();
					boolean bl = false;
					if (player != null) {
						if (attributeModifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
							d += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
							d += (double)EnchantmentHelper.getDamageBonus(this, MobType.UNDEFINED);
							bl = true;
						} else if (attributeModifier.getId() == Item.BASE_ATTACK_SPEED_UUID) {
							d += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
							bl = true;
						}
					}

					double e;
					if (attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE
						|| attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
						e = d * 100.0;
					} else if (((Attribute)entry.getKey()).equals(Attributes.KNOCKBACK_RESISTANCE)) {
						e = d * 10.0;
					} else {
						e = d;
					}

					if (bl) {
						list.add(
							new TextComponent(" ")
								.append(
									new TranslatableComponent(
										"attribute.modifier.equals." + attributeModifier.getOperation().toValue(),
										ATTRIBUTE_MODIFIER_FORMAT.format(e),
										new TranslatableComponent(((Attribute)entry.getKey()).getDescriptionId())
									)
								)
								.withStyle(ChatFormatting.DARK_GREEN)
						);
					} else if (d > 0.0) {
						list.add(
							new TranslatableComponent(
									"attribute.modifier.plus." + attributeModifier.getOperation().toValue(),
									ATTRIBUTE_MODIFIER_FORMAT.format(e),
									new TranslatableComponent(((Attribute)entry.getKey()).getDescriptionId())
								)
								.withStyle(ChatFormatting.BLUE)
						);
					} else if (d < 0.0) {
						e *= -1.0;
						list.add(
							new TranslatableComponent(
									"attribute.modifier.take." + attributeModifier.getOperation().toValue(),
									ATTRIBUTE_MODIFIER_FORMAT.format(e),
									new TranslatableComponent(((Attribute)entry.getKey()).getDescriptionId())
								)
								.withStyle(ChatFormatting.RED)
						);
					}
				}
			}
		}

		if (this.hasTag() && this.getTag().getBoolean("Unbreakable") && (i & 4) == 0) {
			list.add(new TranslatableComponent("item.unbreakable").withStyle(ChatFormatting.BLUE));
		}

		if (this.hasTag() && this.tag.contains("CanDestroy", 9) && (i & 8) == 0) {
			ListTag listTag2 = this.tag.getList("CanDestroy", 8);
			if (!listTag2.isEmpty()) {
				list.add(TextComponent.EMPTY);
				list.add(new TranslatableComponent("item.canBreak").withStyle(ChatFormatting.GRAY));

				for (int k = 0; k < listTag2.size(); k++) {
					list.addAll(expandBlockState(listTag2.getString(k)));
				}
			}
		}

		if (this.hasTag() && this.tag.contains("CanPlaceOn", 9) && (i & 16) == 0) {
			ListTag listTag2 = this.tag.getList("CanPlaceOn", 8);
			if (!listTag2.isEmpty()) {
				list.add(TextComponent.EMPTY);
				list.add(new TranslatableComponent("item.canPlace").withStyle(ChatFormatting.GRAY));

				for (int k = 0; k < listTag2.size(); k++) {
					list.addAll(expandBlockState(listTag2.getString(k)));
				}
			}
		}

		if (tooltipFlag.isAdvanced()) {
			if (this.isDamaged()) {
				list.add(new TranslatableComponent("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
			}

			list.add(new TextComponent(Registry.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
			if (this.hasTag()) {
				list.add(new TranslatableComponent("item.nbt_tags", this.getTag().getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
			}
		}

		return list;
	}

	@Environment(EnvType.CLIENT)
	public static void appendEnchantmentNames(List<Component> list, ListTag listTag) {
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			Registry.ENCHANTMENT
				.getOptional(ResourceLocation.tryParse(compoundTag.getString("id")))
				.ifPresent(enchantment -> list.add(enchantment.getFullname(compoundTag.getInt("lvl"))));
		}
	}

	@Environment(EnvType.CLIENT)
	private static Collection<Component> expandBlockState(String string) {
		try {
			BlockStateParser blockStateParser = new BlockStateParser(new StringReader(string), true).parse(true);
			BlockState blockState = blockStateParser.getState();
			ResourceLocation resourceLocation = blockStateParser.getTag();
			boolean bl = blockState != null;
			boolean bl2 = resourceLocation != null;
			if (bl || bl2) {
				if (bl) {
					return Lists.<Component>newArrayList(blockState.getBlock().getName().withStyle(ChatFormatting.DARK_GRAY));
				}

				Tag<Block> tag = BlockTags.getAllTags().getTag(resourceLocation);
				if (tag != null) {
					Collection<Block> collection = tag.getValues();
					if (!collection.isEmpty()) {
						return (Collection<Component>)collection.stream()
							.map(Block::getName)
							.map(mutableComponent -> mutableComponent.withStyle(ChatFormatting.DARK_GRAY))
							.collect(Collectors.toList());
					}
				}
			}
		} catch (CommandSyntaxException var8) {
		}

		return Lists.<Component>newArrayList(new TextComponent("missingno").withStyle(ChatFormatting.DARK_GRAY));
	}

	public boolean hasFoil() {
		return this.getItem().isFoil(this);
	}

	public Rarity getRarity() {
		return this.getItem().getRarity(this);
	}

	public boolean isEnchantable() {
		return !this.getItem().isEnchantable(this) ? false : !this.isEnchanted();
	}

	public void enchant(Enchantment enchantment, int i) {
		this.getOrCreateTag();
		if (!this.tag.contains("Enchantments", 9)) {
			this.tag.put("Enchantments", new ListTag());
		}

		ListTag listTag = this.tag.getList("Enchantments", 10);
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getKey(enchantment)));
		compoundTag.putShort("lvl", (short)((byte)i));
		listTag.add(compoundTag);
	}

	public boolean isEnchanted() {
		return this.tag != null && this.tag.contains("Enchantments", 9) ? !this.tag.getList("Enchantments", 10).isEmpty() : false;
	}

	public void addTagElement(String string, net.minecraft.nbt.Tag tag) {
		this.getOrCreateTag().put(string, tag);
	}

	public boolean isFramed() {
		return this.entityRepresentation instanceof ItemFrame;
	}

	public void setEntityRepresentation(@Nullable Entity entity) {
		this.entityRepresentation = entity;
	}

	@Nullable
	public ItemFrame getFrame() {
		return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
	}

	@Nullable
	public Entity getEntityRepresentation() {
		return !this.emptyCacheFlag ? this.entityRepresentation : null;
	}

	public int getBaseRepairCost() {
		return this.hasTag() && this.tag.contains("RepairCost", 3) ? this.tag.getInt("RepairCost") : 0;
	}

	public void setRepairCost(int i) {
		this.getOrCreateTag().putInt("RepairCost", i);
	}

	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap;
		if (this.hasTag() && this.tag.contains("AttributeModifiers", 9)) {
			multimap = HashMultimap.create();
			ListTag listTag = this.tag.getList("AttributeModifiers", 10);

			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag = listTag.getCompound(i);
				if (!compoundTag.contains("Slot", 8) || compoundTag.getString("Slot").equals(equipmentSlot.getName())) {
					Optional<Attribute> optional = Registry.ATTRIBUTES.getOptional(ResourceLocation.tryParse(compoundTag.getString("AttributeName")));
					if (optional.isPresent()) {
						AttributeModifier attributeModifier = AttributeModifier.load(compoundTag);
						if (attributeModifier != null && attributeModifier.getId().getLeastSignificantBits() != 0L && attributeModifier.getId().getMostSignificantBits() != 0L) {
							multimap.put((Attribute)optional.get(), attributeModifier);
						}
					}
				}
			}
		} else {
			multimap = this.getItem().getDefaultAttributeModifiers(equipmentSlot);
		}

		return multimap;
	}

	public void addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier, @Nullable EquipmentSlot equipmentSlot) {
		this.getOrCreateTag();
		if (!this.tag.contains("AttributeModifiers", 9)) {
			this.tag.put("AttributeModifiers", new ListTag());
		}

		ListTag listTag = this.tag.getList("AttributeModifiers", 10);
		CompoundTag compoundTag = attributeModifier.save();
		compoundTag.putString("AttributeName", Registry.ATTRIBUTES.getKey(attribute).toString());
		if (equipmentSlot != null) {
			compoundTag.putString("Slot", equipmentSlot.getName());
		}

		listTag.add(compoundTag);
	}

	public Component getDisplayName() {
		MutableComponent mutableComponent = new TextComponent("").append(this.getHoverName());
		if (this.hasCustomHoverName()) {
			mutableComponent.withStyle(ChatFormatting.ITALIC);
		}

		MutableComponent mutableComponent2 = ComponentUtils.wrapInSquareBrackets(mutableComponent);
		if (!this.emptyCacheFlag) {
			mutableComponent2.withStyle(this.getRarity().color)
				.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this))));
		}

		return mutableComponent2;
	}

	private static boolean areSameBlocks(BlockInWorld blockInWorld, @Nullable BlockInWorld blockInWorld2) {
		if (blockInWorld2 == null || blockInWorld.getState() != blockInWorld2.getState()) {
			return false;
		} else if (blockInWorld.getEntity() == null && blockInWorld2.getEntity() == null) {
			return true;
		} else {
			return blockInWorld.getEntity() != null && blockInWorld2.getEntity() != null
				? Objects.equals(blockInWorld.getEntity().save(new CompoundTag()), blockInWorld2.getEntity().save(new CompoundTag()))
				: false;
		}
	}

	public boolean hasAdventureModeBreakTagForBlock(TagManager tagManager, BlockInWorld blockInWorld) {
		if (areSameBlocks(blockInWorld, this.cachedBreakBlock)) {
			return this.cachedBreakBlockResult;
		} else {
			this.cachedBreakBlock = blockInWorld;
			if (this.hasTag() && this.tag.contains("CanDestroy", 9)) {
				ListTag listTag = this.tag.getList("CanDestroy", 8);

				for (int i = 0; i < listTag.size(); i++) {
					String string = listTag.getString(i);

					try {
						Predicate<BlockInWorld> predicate = BlockPredicateArgument.blockPredicate().parse(new StringReader(string)).create(tagManager);
						if (predicate.test(blockInWorld)) {
							this.cachedBreakBlockResult = true;
							return true;
						}
					} catch (CommandSyntaxException var7) {
					}
				}
			}

			this.cachedBreakBlockResult = false;
			return false;
		}
	}

	public boolean hasAdventureModePlaceTagForBlock(TagManager tagManager, BlockInWorld blockInWorld) {
		if (areSameBlocks(blockInWorld, this.cachedPlaceBlock)) {
			return this.cachedPlaceBlockResult;
		} else {
			this.cachedPlaceBlock = blockInWorld;
			if (this.hasTag() && this.tag.contains("CanPlaceOn", 9)) {
				ListTag listTag = this.tag.getList("CanPlaceOn", 8);

				for (int i = 0; i < listTag.size(); i++) {
					String string = listTag.getString(i);

					try {
						Predicate<BlockInWorld> predicate = BlockPredicateArgument.blockPredicate().parse(new StringReader(string)).create(tagManager);
						if (predicate.test(blockInWorld)) {
							this.cachedPlaceBlockResult = true;
							return true;
						}
					} catch (CommandSyntaxException var7) {
					}
				}
			}

			this.cachedPlaceBlockResult = false;
			return false;
		}
	}

	public int getPopTime() {
		return this.popTime;
	}

	public void setPopTime(int i) {
		this.popTime = i;
	}

	public int getCount() {
		return this.emptyCacheFlag ? 0 : this.count;
	}

	public void setCount(int i) {
		this.count = i;
		this.updateEmptyCacheFlag();
	}

	public void grow(int i) {
		this.setCount(this.count + i);
	}

	public void shrink(int i) {
		this.grow(-i);
	}

	public void onUseTick(Level level, LivingEntity livingEntity, int i) {
		this.getItem().onUseTick(level, livingEntity, this, i);
	}

	public boolean isEdible() {
		return this.getItem().isEdible();
	}

	public SoundEvent getDrinkingSound() {
		return this.getItem().getDrinkingSound();
	}

	public SoundEvent getEatingSound() {
		return this.getItem().getEatingSound();
	}
}
