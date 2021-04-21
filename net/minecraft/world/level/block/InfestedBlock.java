/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class InfestedBlock
extends Block {
    private final Block hostBlock;
    private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> HOST_TO_INFESTED_STATES = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> INFESTED_TO_HOST_STATES = Maps.newIdentityHashMap();

    public InfestedBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.hostBlock = block;
        BLOCK_BY_HOST_BLOCK.put(block, this);
    }

    public Block getHostBlock() {
        return this.hostBlock;
    }

    public static boolean isCompatibleHostBlock(BlockState blockState) {
        return BLOCK_BY_HOST_BLOCK.containsKey(blockState.getBlock());
    }

    private void spawnInfestation(ServerLevel serverLevel, BlockPos blockPos) {
        Silverfish silverfish = EntityType.SILVERFISH.create(serverLevel);
        silverfish.moveTo((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, 0.0f, 0.0f);
        serverLevel.addFreshEntity(silverfish);
        silverfish.spawnAnim();
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack);
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
            this.spawnInfestation(serverLevel, blockPos);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        if (level instanceof ServerLevel) {
            this.spawnInfestation((ServerLevel)level, blockPos);
        }
    }

    public static BlockState infestedStateByHost(BlockState blockState) {
        return InfestedBlock.getNewStateWithProperties(HOST_TO_INFESTED_STATES, blockState, () -> BLOCK_BY_HOST_BLOCK.get(blockState.getBlock()).defaultBlockState());
    }

    public BlockState hostStateByInfested(BlockState blockState) {
        return InfestedBlock.getNewStateWithProperties(INFESTED_TO_HOST_STATES, blockState, () -> this.getHostBlock().defaultBlockState());
    }

    private static BlockState getNewStateWithProperties(Map<BlockState, BlockState> map, BlockState blockState2, Supplier<BlockState> supplier) {
        return map.computeIfAbsent(blockState2, blockState -> {
            BlockState blockState2 = (BlockState)supplier.get();
            for (Property<?> property : blockState.getProperties()) {
                blockState2 = blockState2.hasProperty(property) ? (BlockState)blockState2.setValue(property, blockState.getValue(property)) : blockState2;
            }
            return blockState2;
        });
    }
}

