/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class BowItem
extends ProjectileWeaponItem
implements Vanishable {
    public static final int MAX_DRAW_DURATION = 20;
    public static final int DEFAULT_RANGE = 15;

    public BowItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        boolean bl2;
        int j;
        float f;
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        boolean bl = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, itemStack) > 0;
        ItemStack itemStack2 = player.getProjectile(itemStack);
        if (itemStack2.isEmpty() && !bl) {
            return;
        }
        if (itemStack2.isEmpty()) {
            itemStack2 = new ItemStack(Items.ARROW);
        }
        if ((double)(f = BowItem.getPowerForTime(j = this.getUseDuration(itemStack) - i)) < 0.1) {
            return;
        }
        boolean bl3 = bl2 = bl && itemStack2.is(Items.ARROW);
        if (!level.isClientSide) {
            int l;
            int k;
            ArrowItem arrowItem = (ArrowItem)(itemStack2.getItem() instanceof ArrowItem ? itemStack2.getItem() : Items.ARROW);
            AbstractArrow abstractArrow = arrowItem.createArrow(level, itemStack2, player);
            abstractArrow.shootFromRotation(player, player.xRot, player.yRot, 0.0f, f * 3.0f, 1.0f);
            if (f == 1.0f) {
                abstractArrow.setCritArrow(true);
            }
            if ((k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, itemStack)) > 0) {
                abstractArrow.setBaseDamage(abstractArrow.getBaseDamage() + (double)k * 0.5 + 0.5);
            }
            if ((l = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, itemStack)) > 0) {
                abstractArrow.setKnockback(l);
            }
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, itemStack) > 0) {
                abstractArrow.setSecondsOnFire(100);
            }
            itemStack.hurtAndBreak(1, player, player2 -> player2.broadcastBreakEvent(player.getUsedItemHand()));
            if (bl2 || player.getAbilities().instabuild && (itemStack2.is(Items.SPECTRAL_ARROW) || itemStack2.is(Items.TIPPED_ARROW))) {
                abstractArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(abstractArrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + f * 0.5f);
        if (!bl2 && !player.getAbilities().instabuild) {
            itemStack2.shrink(1);
            if (itemStack2.isEmpty()) {
                player.getInventory().removeItem(itemStack2);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    public static float getPowerForTime(int i) {
        float f = (float)i / 20.0f;
        if ((f = (f * f + f * 2.0f) / 3.0f) > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        boolean bl;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        boolean bl2 = bl = !player.getProjectile(itemStack).isEmpty();
        if (player.getAbilities().instabuild || bl) {
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }
}

