/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Item
implements FeatureElement,
ItemLike {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final int MAX_STACK_SIZE = 64;
    public static final int EAT_DURATION = 32;
    public static final int MAX_BAR_WIDTH = 13;
    private final Holder.Reference<Item> builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
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
    private final FeatureFlagSet requiredFeatures;

    public static int getId(Item item) {
        return item == null ? 0 : BuiltInRegistries.ITEM.getId(item);
    }

    public static Item byId(int i) {
        return BuiltInRegistries.ITEM.byId(i);
    }

    @Deprecated
    public static Item byBlock(Block block) {
        return BY_BLOCK.getOrDefault(block, Items.AIR);
    }

    public Item(Properties properties) {
        String string;
        this.rarity = properties.rarity;
        this.craftingRemainingItem = properties.craftingRemainingItem;
        this.maxDamage = properties.maxDamage;
        this.maxStackSize = properties.maxStackSize;
        this.foodProperties = properties.foodProperties;
        this.isFireResistant = properties.isFireResistant;
        this.requiredFeatures = properties.requiredFeatures;
        if (SharedConstants.IS_RUNNING_IN_IDE && !(string = this.getClass().getSimpleName()).endsWith("Item")) {
            LOGGER.error("Item classes should end with Item and {} doesn't.", (Object)string);
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
        return 1.0f;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (this.isEdible()) {
            ItemStack itemStack = player.getItemInHand(interactionHand);
            if (player.canEat(this.getFoodProperties().canAlwaysEat())) {
                player.startUsingItem(interactionHand);
                return InteractionResultHolder.consume(itemStack);
            }
            return InteractionResultHolder.fail(itemStack);
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        if (this.isEdible()) {
            return livingEntity.eat(level, itemStack);
        }
        return itemStack;
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
        return Math.round(13.0f - (float)itemStack.getDamageValue() * 13.0f / (float)this.maxDamage);
    }

    public int getBarColor(ItemStack itemStack) {
        float f = Math.max(0.0f, ((float)this.maxDamage - (float)itemStack.getDamageValue()) / (float)this.maxDamage);
        return Mth.hsvToRgb(f / 3.0f, 1.0f, 1.0f);
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
        return BuiltInRegistries.ITEM.getKey(this).getPath();
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
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
        }
        return 0;
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
        }
        switch (this.rarity) {
            case COMMON: 
            case UNCOMMON: {
                return Rarity.RARE;
            }
            case RARE: {
                return Rarity.EPIC;
            }
        }
        return this.rarity;
    }

    public boolean isEnchantable(ItemStack itemStack) {
        return this.getMaxStackSize() == 1 && this.canBeDepleted();
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
        float f = player.getXRot();
        float g = player.getYRot();
        Vec3 vec3 = player.getEyePosition();
        float h = Mth.cos(-g * ((float)Math.PI / 180) - (float)Math.PI);
        float i = Mth.sin(-g * ((float)Math.PI / 180) - (float)Math.PI);
        float j = -Mth.cos(-f * ((float)Math.PI / 180));
        float k = Mth.sin(-f * ((float)Math.PI / 180));
        float l = i * j;
        float m = k;
        float n = h * j;
        double d = 5.0;
        Vec3 vec32 = vec3.add((double)l * 5.0, (double)m * 5.0, (double)n * 5.0);
        return level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluid, player));
    }

    public int getEnchantmentValue() {
        return 0;
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
        return !this.isFireResistant || !damageSource.is(DamageTypeTags.IS_FIRE);
    }

    public boolean canFitInsideContainerItems() {
        return true;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public static class Properties {
        int maxStackSize = 64;
        int maxDamage;
        @Nullable
        Item craftingRemainingItem;
        Rarity rarity = Rarity.COMMON;
        @Nullable
        FoodProperties foodProperties;
        boolean isFireResistant;
        FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

        public Properties food(FoodProperties foodProperties) {
            this.foodProperties = foodProperties;
            return this;
        }

        public Properties stacksTo(int i) {
            if (this.maxDamage > 0) {
                throw new RuntimeException("Unable to have damage AND stack.");
            }
            this.maxStackSize = i;
            return this;
        }

        public Properties defaultDurability(int i) {
            return this.maxDamage == 0 ? this.durability(i) : this;
        }

        public Properties durability(int i) {
            this.maxDamage = i;
            this.maxStackSize = 1;
            return this;
        }

        public Properties craftRemainder(Item item) {
            this.craftingRemainingItem = item;
            return this;
        }

        public Properties rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Properties fireResistant() {
            this.isFireResistant = true;
            return this;
        }

        public Properties requiredFeatures(FeatureFlag ... featureFlags) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
            return this;
        }
    }
}

