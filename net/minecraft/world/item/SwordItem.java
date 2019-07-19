/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class SwordItem
extends TieredItem {
    private final float attackDamage;
    private final float attackSpeed;

    public SwordItem(Tier tier, int i, float f, Item.Properties properties) {
        super(tier, properties);
        this.attackSpeed = f;
        this.attackDamage = (float)i + tier.getAttackDamageBonus();
    }

    public float getDamage() {
        return this.attackDamage;
    }

    @Override
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return !player.isCreative();
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        Block block = blockState.getBlock();
        if (block == Blocks.COBWEB) {
            return 15.0f;
        }
        Material material = blockState.getMaterial();
        if (material == Material.PLANT || material == Material.REPLACEABLE_PLANT || material == Material.CORAL || blockState.is(BlockTags.LEAVES) || material == Material.VEGETABLE) {
            return 1.5f;
        }
        return 1.0f;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity2, LivingEntity livingEntity22) {
        itemStack.hurtAndBreak(1, livingEntity22, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity2) {
        if (blockState.getDestroySpeed(level, blockPos) != 0.0f) {
            itemStack.hurtAndBreak(2, livingEntity2, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public boolean canDestroySpecial(BlockState blockState) {
        return blockState.getBlock() == Blocks.COBWEB;
    }

    @Override
    public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getDefaultAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)this.attackSpeed, AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }
}

