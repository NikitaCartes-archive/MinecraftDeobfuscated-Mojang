/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DiggerItem
extends TieredItem
implements Vanishable {
    private final TagKey<Block> blocks;
    protected final float speed;
    private final float attackDamageBaseline;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    protected DiggerItem(float f, float g, Tier tier, TagKey<Block> tagKey, Item.Properties properties) {
        super(tier, properties);
        this.blocks = tagKey;
        this.speed = tier.getSpeed();
        this.attackDamageBaseline = f + tier.getAttackDamageBonus();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamageBaseline, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)g, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        return blockState.is(this.blocks) ? this.speed : 1.0f;
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

    @Override
    public boolean isCorrectToolForDrops(BlockState blockState) {
        int i = this.getTier().getLevel();
        if (i < 3 && blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        }
        if (i < 2 && blockState.is(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        }
        if (i < 1 && blockState.is(BlockTags.NEEDS_STONE_TOOL)) {
            return false;
        }
        return blockState.is(this.blocks);
    }
}

