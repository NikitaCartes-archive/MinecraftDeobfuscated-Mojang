/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WorkAtComposter
extends WorkAtPoi {
    private static final List<Item> COMPOSTABLE_ITEMS = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);

    @Override
    protected void useWorkstation(ServerLevel serverLevel, Villager villager) {
        Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (!optional.isPresent()) {
            return;
        }
        GlobalPos globalPos = optional.get();
        BlockState blockState = serverLevel.getBlockState(globalPos.pos());
        Block block = blockState.getBlock();
        if (block == Blocks.COMPOSTER) {
            this.makeBread(villager);
            this.compostItems(serverLevel, villager, globalPos, blockState);
        }
    }

    private void compostItems(ServerLevel serverLevel, Villager villager, GlobalPos globalPos, BlockState blockState) {
        if (blockState.getValue(ComposterBlock.LEVEL) == 8) {
            blockState = ComposterBlock.extractProduce(blockState, serverLevel, globalPos.pos());
        }
        int i = 20;
        int j = 10;
        int[] is = new int[COMPOSTABLE_ITEMS.size()];
        SimpleContainer simpleContainer = villager.getInventory();
        int k = simpleContainer.getContainerSize();
        for (int l = k - 1; l >= 0 && i > 0; --l) {
            int o;
            ItemStack itemStack = simpleContainer.getItem(l);
            int m = COMPOSTABLE_ITEMS.indexOf(itemStack.getItem());
            if (m == -1) continue;
            int n = itemStack.getCount();
            is[m] = o = is[m] + n;
            int p = Math.min(Math.min(o - 10, i), n);
            if (p <= 0) continue;
            i -= p;
            for (int q = 0; q < p; ++q) {
                if ((blockState = ComposterBlock.insertItem(blockState, serverLevel, itemStack, globalPos.pos())).getValue(ComposterBlock.LEVEL) != 7) continue;
                return;
            }
        }
    }

    private void makeBread(Villager villager) {
        SimpleContainer simpleContainer = villager.getInventory();
        if (simpleContainer.countItem(Items.BREAD) > 36) {
            return;
        }
        int i = simpleContainer.countItem(Items.WHEAT);
        int j = 3;
        int k = 3;
        int l = Math.min(3, i / 3);
        if (l == 0) {
            return;
        }
        int m = l * 3;
        simpleContainer.removeItemType(Items.WHEAT, m);
        ItemStack itemStack = simpleContainer.addItem(new ItemStack(Items.BREAD, l));
        if (!itemStack.isEmpty()) {
            villager.spawnAtLocation(itemStack, 0.5f);
        }
    }
}

