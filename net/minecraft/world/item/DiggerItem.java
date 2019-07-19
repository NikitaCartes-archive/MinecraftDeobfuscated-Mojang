/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Multimap;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DiggerItem
extends TieredItem {
    private final Set<Block> blocks;
    protected final float speed;
    protected final float attackDamage;
    protected final float attackSpeed;

    protected DiggerItem(float f, float g, Tier tier, Set<Block> set, Item.Properties properties) {
        super(tier, properties);
        this.blocks = set;
        this.speed = tier.getSpeed();
        this.attackDamage = f + tier.getAttackDamageBonus();
        this.attackSpeed = g;
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        return this.blocks.contains(blockState.getBlock()) ? this.speed : 1.0f;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity2, LivingEntity livingEntity22) {
        itemStack.hurtAndBreak(2, livingEntity22, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity2) {
        if (!level.isClientSide && blockState.getDestroySpeed(level, blockPos) != 0.0f) {
            itemStack.hurtAndBreak(1, livingEntity2, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getDefaultAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)this.attackSpeed, AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }
}

