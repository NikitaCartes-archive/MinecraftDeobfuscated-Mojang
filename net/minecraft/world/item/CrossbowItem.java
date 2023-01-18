/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CrossbowItem
extends ProjectileWeaponItem
implements Vanishable {
    private static final String TAG_CHARGED = "Charged";
    private static final String TAG_CHARGED_PROJECTILES = "ChargedProjectiles";
    private static final int MAX_CHARGE_DURATION = 25;
    public static final int DEFAULT_RANGE = 8;
    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;
    private static final float START_SOUND_PERCENT = 0.2f;
    private static final float MID_SOUND_PERCENT = 0.5f;
    private static final float ARROW_POWER = 3.15f;
    private static final float FIREWORK_POWER = 1.6f;

    public CrossbowItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return ARROW_OR_FIREWORK;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (CrossbowItem.isCharged(itemStack)) {
            CrossbowItem.performShooting(level, player, interactionHand, itemStack, CrossbowItem.getShootingPower(itemStack), 1.0f);
            CrossbowItem.setCharged(itemStack, false);
            return InteractionResultHolder.consume(itemStack);
        }
        if (!player.getProjectile(itemStack).isEmpty()) {
            if (!CrossbowItem.isCharged(itemStack)) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
                player.startUsingItem(interactionHand);
            }
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    private static float getShootingPower(ItemStack itemStack) {
        if (CrossbowItem.containsChargedProjectile(itemStack, Items.FIREWORK_ROCKET)) {
            return 1.6f;
        }
        return 3.15f;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        int j = this.getUseDuration(itemStack) - i;
        float f = CrossbowItem.getPowerForTime(j, itemStack);
        if (f >= 1.0f && !CrossbowItem.isCharged(itemStack) && CrossbowItem.tryLoadProjectiles(livingEntity, itemStack)) {
            CrossbowItem.setCharged(itemStack, true);
            SoundSource soundSource = livingEntity instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundSource, 1.0f, 1.0f / (level.getRandom().nextFloat() * 0.5f + 1.0f) + 0.2f);
        }
    }

    private static boolean tryLoadProjectiles(LivingEntity livingEntity, ItemStack itemStack) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, itemStack);
        int j = i == 0 ? 1 : 3;
        boolean bl = livingEntity instanceof Player && ((Player)livingEntity).getAbilities().instabuild;
        ItemStack itemStack2 = livingEntity.getProjectile(itemStack);
        ItemStack itemStack3 = itemStack2.copy();
        for (int k = 0; k < j; ++k) {
            if (k > 0) {
                itemStack2 = itemStack3.copy();
            }
            if (itemStack2.isEmpty() && bl) {
                itemStack2 = new ItemStack(Items.ARROW);
                itemStack3 = itemStack2.copy();
            }
            if (CrossbowItem.loadProjectile(livingEntity, itemStack, itemStack2, k > 0, bl)) continue;
            return false;
        }
        return true;
    }

    private static boolean loadProjectile(LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl, boolean bl2) {
        ItemStack itemStack3;
        boolean bl3;
        if (itemStack2.isEmpty()) {
            return false;
        }
        boolean bl4 = bl3 = bl2 && itemStack2.getItem() instanceof ArrowItem;
        if (!(bl3 || bl2 || bl)) {
            itemStack3 = itemStack2.split(1);
            if (itemStack2.isEmpty() && livingEntity instanceof Player) {
                ((Player)livingEntity).getInventory().removeItem(itemStack2);
            }
        } else {
            itemStack3 = itemStack2.copy();
        }
        CrossbowItem.addChargedProjectile(itemStack, itemStack3);
        return true;
    }

    public static boolean isCharged(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && compoundTag.getBoolean(TAG_CHARGED);
    }

    public static void setCharged(ItemStack itemStack, boolean bl) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putBoolean(TAG_CHARGED, bl);
    }

    private static void addChargedProjectile(ItemStack itemStack, ItemStack itemStack2) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = compoundTag.contains(TAG_CHARGED_PROJECTILES, 9) ? compoundTag.getList(TAG_CHARGED_PROJECTILES, 10) : new ListTag();
        CompoundTag compoundTag2 = new CompoundTag();
        itemStack2.save(compoundTag2);
        listTag.add(compoundTag2);
        compoundTag.put(TAG_CHARGED_PROJECTILES, listTag);
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack itemStack) {
        ListTag listTag;
        ArrayList<ItemStack> list = Lists.newArrayList();
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && compoundTag.contains(TAG_CHARGED_PROJECTILES, 9) && (listTag = compoundTag.getList(TAG_CHARGED_PROJECTILES, 10)) != null) {
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag2 = listTag.getCompound(i);
                list.add(ItemStack.of(compoundTag2));
            }
        }
        return list;
    }

    private static void clearChargedProjectiles(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null) {
            ListTag listTag = compoundTag.getList(TAG_CHARGED_PROJECTILES, 9);
            listTag.clear();
            compoundTag.put(TAG_CHARGED_PROJECTILES, listTag);
        }
    }

    public static boolean containsChargedProjectile(ItemStack itemStack2, Item item) {
        return CrossbowItem.getChargedProjectiles(itemStack2).stream().anyMatch(itemStack -> itemStack.is(item));
    }

    private static void shootProjectile(Level level, LivingEntity livingEntity2, InteractionHand interactionHand, ItemStack itemStack, ItemStack itemStack2, float f, boolean bl, float g, float h, float i) {
        Projectile projectile;
        if (level.isClientSide) {
            return;
        }
        boolean bl2 = itemStack2.is(Items.FIREWORK_ROCKET);
        if (bl2) {
            projectile = new FireworkRocketEntity(level, itemStack2, livingEntity2, livingEntity2.getX(), livingEntity2.getEyeY() - (double)0.15f, livingEntity2.getZ(), true);
        } else {
            projectile = CrossbowItem.getArrow(level, livingEntity2, itemStack, itemStack2);
            if (bl || i != 0.0f) {
                ((AbstractArrow)projectile).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
        }
        if (livingEntity2 instanceof CrossbowAttackMob) {
            CrossbowAttackMob crossbowAttackMob = (CrossbowAttackMob)((Object)livingEntity2);
            crossbowAttackMob.shootCrossbowProjectile(crossbowAttackMob.getTarget(), itemStack, projectile, i);
        } else {
            Vec3 vec3 = livingEntity2.getUpVector(1.0f);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis((double)(i * ((float)Math.PI / 180)), vec3.x, vec3.y, vec3.z);
            Vec3 vec32 = livingEntity2.getViewVector(1.0f);
            Vector3f vector3f = vec32.toVector3f().rotate(quaternionf);
            projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), g, h);
        }
        itemStack.hurtAndBreak(bl2 ? 3 : 1, livingEntity2, livingEntity -> livingEntity.broadcastBreakEvent(interactionHand));
        level.addFreshEntity(projectile);
        level.playSound(null, livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0f, f);
    }

    private static AbstractArrow getArrow(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2) {
        ArrowItem arrowItem = (ArrowItem)(itemStack2.getItem() instanceof ArrowItem ? itemStack2.getItem() : Items.ARROW);
        AbstractArrow abstractArrow = arrowItem.createArrow(level, itemStack2, livingEntity);
        if (livingEntity instanceof Player) {
            abstractArrow.setCritArrow(true);
        }
        abstractArrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        abstractArrow.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, itemStack);
        if (i > 0) {
            abstractArrow.setPierceLevel((byte)i);
        }
        return abstractArrow;
    }

    public static void performShooting(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, float f, float g) {
        List<ItemStack> list = CrossbowItem.getChargedProjectiles(itemStack);
        float[] fs = CrossbowItem.getShotPitches(livingEntity.getRandom());
        for (int i = 0; i < list.size(); ++i) {
            boolean bl;
            ItemStack itemStack2 = list.get(i);
            boolean bl2 = bl = livingEntity instanceof Player && ((Player)livingEntity).getAbilities().instabuild;
            if (itemStack2.isEmpty()) continue;
            if (i == 0) {
                CrossbowItem.shootProjectile(level, livingEntity, interactionHand, itemStack, itemStack2, fs[i], bl, f, g, 0.0f);
                continue;
            }
            if (i == 1) {
                CrossbowItem.shootProjectile(level, livingEntity, interactionHand, itemStack, itemStack2, fs[i], bl, f, g, -10.0f);
                continue;
            }
            if (i != 2) continue;
            CrossbowItem.shootProjectile(level, livingEntity, interactionHand, itemStack, itemStack2, fs[i], bl, f, g, 10.0f);
        }
        CrossbowItem.onCrossbowShot(level, livingEntity, itemStack);
    }

    private static float[] getShotPitches(RandomSource randomSource) {
        boolean bl = randomSource.nextBoolean();
        return new float[]{1.0f, CrossbowItem.getRandomShotPitch(bl, randomSource), CrossbowItem.getRandomShotPitch(!bl, randomSource)};
    }

    private static float getRandomShotPitch(boolean bl, RandomSource randomSource) {
        float f = bl ? 0.63f : 0.43f;
        return 1.0f / (randomSource.nextFloat() * 0.5f + 1.8f) + f;
    }

    private static void onCrossbowShot(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            if (!level.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, itemStack);
            }
            serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        }
        CrossbowItem.clearChargedProjectiles(itemStack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        if (!level.isClientSide) {
            int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack);
            SoundEvent soundEvent = this.getStartSound(j);
            SoundEvent soundEvent2 = j == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE : null;
            float f = (float)(itemStack.getUseDuration() - i) / (float)CrossbowItem.getChargeDuration(itemStack);
            if (f < 0.2f) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }
            if (f >= 0.2f && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), soundEvent, SoundSource.PLAYERS, 0.5f, 1.0f);
            }
            if (f >= 0.5f && soundEvent2 != null && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), soundEvent2, SoundSource.PLAYERS, 0.5f, 1.0f);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return CrossbowItem.getChargeDuration(itemStack) + 3;
    }

    public static int getChargeDuration(ItemStack itemStack) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack);
        return i == 0 ? 25 : 25 - 5 * i;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.CROSSBOW;
    }

    private SoundEvent getStartSound(int i) {
        switch (i) {
            case 1: {
                return SoundEvents.CROSSBOW_QUICK_CHARGE_1;
            }
            case 2: {
                return SoundEvents.CROSSBOW_QUICK_CHARGE_2;
            }
            case 3: {
                return SoundEvents.CROSSBOW_QUICK_CHARGE_3;
            }
        }
        return SoundEvents.CROSSBOW_LOADING_START;
    }

    private static float getPowerForTime(int i, ItemStack itemStack) {
        float f = (float)i / (float)CrossbowItem.getChargeDuration(itemStack);
        if (f > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        List<ItemStack> list2 = CrossbowItem.getChargedProjectiles(itemStack);
        if (!CrossbowItem.isCharged(itemStack) || list2.isEmpty()) {
            return;
        }
        ItemStack itemStack2 = list2.get(0);
        list.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(itemStack2.getDisplayName()));
        if (tooltipFlag.isAdvanced() && itemStack2.is(Items.FIREWORK_ROCKET)) {
            ArrayList<Component> list3 = Lists.newArrayList();
            Items.FIREWORK_ROCKET.appendHoverText(itemStack2, level, list3, tooltipFlag);
            if (!list3.isEmpty()) {
                for (int i = 0; i < list3.size(); ++i) {
                    list3.set(i, Component.literal("  ").append((Component)list3.get(i)).withStyle(ChatFormatting.GRAY));
                }
                list.addAll(list3);
            }
        }
    }

    @Override
    public boolean useOnRelease(ItemStack itemStack) {
        return itemStack.is(this);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 8;
    }
}

