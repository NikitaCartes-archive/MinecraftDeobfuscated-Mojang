/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HoeItem
extends TieredItem {
    private final float attackSpeed;
    protected static final Map<Block, BlockState> TILLABLES = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.FARMLAND.defaultBlockState(), Blocks.GRASS_PATH, Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT, Blocks.FARMLAND.defaultBlockState(), Blocks.COARSE_DIRT, Blocks.DIRT.defaultBlockState()));

    public HoeItem(Tier tier, float f, Item.Properties properties) {
        super(tier, properties);
        this.attackSpeed = f;
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        if (blockState.is(BlockTags.WART_BLOCKS)) {
            return 15.0f;
        }
        return 1.0f;
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity2) {
        if (!level.isClientSide) {
            itemStack.hurtAndBreak(1, livingEntity2, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        if (blockState.is(BlockTags.WART_BLOCKS)) {
            return true;
        }
        return super.mineBlock(itemStack, level, blockState, blockPos, livingEntity2);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockState blockState;
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        if (useOnContext.getClickedFace() != Direction.DOWN && level.getBlockState(blockPos.above()).isAir() && (blockState = TILLABLES.get(level.getBlockState(blockPos).getBlock())) != null) {
            Player player2 = useOnContext.getPlayer();
            level.playSound(player2, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!level.isClientSide) {
                level.setBlock(blockPos, blockState, 11);
                if (player2 != null) {
                    useOnContext.getItemInHand().hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(useOnContext.getHand()));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity2, LivingEntity livingEntity22) {
        itemStack.hurtAndBreak(1, livingEntity22, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getDefaultAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 0.0, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)this.attackSpeed, AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }
}

