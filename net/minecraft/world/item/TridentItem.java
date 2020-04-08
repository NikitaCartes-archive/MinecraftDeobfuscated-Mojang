/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TridentItem
extends Item
implements Vanishable {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public TridentItem(Item.Properties properties) {
        super(properties);
        this.addProperty(new ResourceLocation("throwing"), (itemStack, level, livingEntity) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0f : 0.0f);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)-2.9f, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return !player.isCreative();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player2 = (Player)livingEntity;
        int j = this.getUseDuration(itemStack) - i;
        if (j < 10) {
            return;
        }
        int k = EnchantmentHelper.getRiptide(itemStack);
        if (k > 0 && !player2.isInWaterOrRain()) {
            return;
        }
        if (!level.isClientSide) {
            itemStack.hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(livingEntity.getUsedItemHand()));
            if (k == 0) {
                ThrownTrident thrownTrident = new ThrownTrident(level, (LivingEntity)player2, itemStack);
                thrownTrident.shootFromRotation(player2, player2.xRot, player2.yRot, 0.0f, 2.5f + (float)k * 0.5f, 1.0f);
                if (player2.abilities.instabuild) {
                    thrownTrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                level.addFreshEntity(thrownTrident);
                level.playSound(null, thrownTrident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
                if (!player2.abilities.instabuild) {
                    player2.inventory.removeItem(itemStack);
                }
            }
        }
        player2.awardStat(Stats.ITEM_USED.get(this));
        if (k > 0) {
            float f = player2.yRot;
            float g = player2.xRot;
            float h = -Mth.sin(f * ((float)Math.PI / 180)) * Mth.cos(g * ((float)Math.PI / 180));
            float l = -Mth.sin(g * ((float)Math.PI / 180));
            float m = Mth.cos(f * ((float)Math.PI / 180)) * Mth.cos(g * ((float)Math.PI / 180));
            float n = Mth.sqrt(h * h + l * l + m * m);
            float o = 3.0f * ((1.0f + (float)k) / 4.0f);
            player2.push(h *= o / n, l *= o / n, m *= o / n);
            player2.startAutoSpinAttack(20);
            if (player2.isOnGround()) {
                float p = 1.1999999f;
                player2.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
            }
            SoundEvent soundEvent = k >= 3 ? SoundEvents.TRIDENT_RIPTIDE_3 : (k == 2 ? SoundEvents.TRIDENT_RIPTIDE_2 : SoundEvents.TRIDENT_RIPTIDE_1);
            level.playSound(null, player2, soundEvent, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (EnchantmentHelper.getRiptide(itemStack) > 0 && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(itemStack);
        }
        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity2, LivingEntity livingEntity22) {
        itemStack.hurtAndBreak(1, livingEntity22, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity2) {
        if ((double)blockState.getDestroySpeed(level, blockPos) != 0.0) {
            itemStack.hurtAndBreak(2, livingEntity2, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            return this.defaultModifiers;
        }
        return super.getDefaultAttributeModifiers(equipmentSlot);
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}

