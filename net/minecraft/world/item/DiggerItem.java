/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
    private final float attackDamageBaseline;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    protected DiggerItem(float f, float g, Tier tier, Set<Block> set, Item.Properties properties) {
        super(tier, properties);
        this.blocks = set;
        this.speed = tier.getSpeed();
        this.attackDamageBaseline = f + tier.getAttackDamageBonus();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamageBaseline, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)g, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
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
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            return this.defaultModifiers;
        }
        return super.getDefaultAttributeModifiers(equipmentSlot);
    }

    public float getAttackDamage() {
        return this.attackDamageBaseline;
    }
}

