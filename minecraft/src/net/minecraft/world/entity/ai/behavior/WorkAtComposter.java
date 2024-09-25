package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WorkAtComposter extends WorkAtPoi {
	private static final List<Item> COMPOSTABLE_ITEMS = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);

	@Override
	protected void useWorkstation(ServerLevel serverLevel, Villager villager) {
		Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
		if (!optional.isEmpty()) {
			GlobalPos globalPos = (GlobalPos)optional.get();
			BlockState blockState = serverLevel.getBlockState(globalPos.pos());
			if (blockState.is(Blocks.COMPOSTER)) {
				this.makeBread(serverLevel, villager);
				this.compostItems(serverLevel, villager, globalPos, blockState);
			}
		}
	}

	private void compostItems(ServerLevel serverLevel, Villager villager, GlobalPos globalPos, BlockState blockState) {
		BlockPos blockPos = globalPos.pos();
		if ((Integer)blockState.getValue(ComposterBlock.LEVEL) == 8) {
			blockState = ComposterBlock.extractProduce(villager, blockState, serverLevel, blockPos);
		}

		int i = 20;
		int j = 10;
		int[] is = new int[COMPOSTABLE_ITEMS.size()];
		SimpleContainer simpleContainer = villager.getInventory();
		int k = simpleContainer.getContainerSize();
		BlockState blockState2 = blockState;

		for (int l = k - 1; l >= 0 && i > 0; l--) {
			ItemStack itemStack = simpleContainer.getItem(l);
			int m = COMPOSTABLE_ITEMS.indexOf(itemStack.getItem());
			if (m != -1) {
				int n = itemStack.getCount();
				int o = is[m] + n;
				is[m] = o;
				int p = Math.min(Math.min(o - 10, i), n);
				if (p > 0) {
					i -= p;

					for (int q = 0; q < p; q++) {
						blockState2 = ComposterBlock.insertItem(villager, blockState2, serverLevel, itemStack, blockPos);
						if ((Integer)blockState2.getValue(ComposterBlock.LEVEL) == 7) {
							this.spawnComposterFillEffects(serverLevel, blockState, blockPos, blockState2);
							return;
						}
					}
				}
			}
		}

		this.spawnComposterFillEffects(serverLevel, blockState, blockPos, blockState2);
	}

	private void spawnComposterFillEffects(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos, BlockState blockState2) {
		serverLevel.levelEvent(1500, blockPos, blockState2 != blockState ? 1 : 0);
	}

	private void makeBread(ServerLevel serverLevel, Villager villager) {
		SimpleContainer simpleContainer = villager.getInventory();
		if (simpleContainer.countItem(Items.BREAD) <= 36) {
			int i = simpleContainer.countItem(Items.WHEAT);
			int j = 3;
			int k = 3;
			int l = Math.min(3, i / 3);
			if (l != 0) {
				int m = l * 3;
				simpleContainer.removeItemType(Items.WHEAT, m);
				ItemStack itemStack = simpleContainer.addItem(new ItemStack(Items.BREAD, l));
				if (!itemStack.isEmpty()) {
					villager.spawnAtLocation(serverLevel, itemStack, 0.5F);
				}
			}
		}
	}
}
