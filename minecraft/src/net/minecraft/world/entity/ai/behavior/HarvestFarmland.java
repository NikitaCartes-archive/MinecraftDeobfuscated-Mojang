package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HarvestFarmland extends Behavior<Villager> {
	private static final int HARVEST_DURATION = 200;
	public static final float SPEED_MODIFIER = 0.5F;
	@Nullable
	private BlockPos aboveFarmlandPos;
	private long nextOkStartTime;
	private int timeWorkedSoFar;
	private final List<BlockPos> validFarmlandAroundVillager = Lists.<BlockPos>newArrayList();

	public HarvestFarmland() {
		super(
			ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.SECONDARY_JOB_SITE,
				MemoryStatus.VALUE_PRESENT
			)
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
			return false;
		} else if (villager.getVillagerData().getProfession() != VillagerProfession.FARMER) {
			return false;
		} else {
			BlockPos.MutableBlockPos mutableBlockPos = villager.blockPosition().mutable();
			this.validFarmlandAroundVillager.clear();

			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					for (int k = -1; k <= 1; k++) {
						mutableBlockPos.set(villager.getX() + (double)i, villager.getY() + (double)j, villager.getZ() + (double)k);
						if (this.validPos(mutableBlockPos, serverLevel)) {
							this.validFarmlandAroundVillager.add(new BlockPos(mutableBlockPos));
						}
					}
				}
			}

			this.aboveFarmlandPos = this.getValidFarmland(serverLevel);
			return this.aboveFarmlandPos != null;
		}
	}

	@Nullable
	private BlockPos getValidFarmland(ServerLevel serverLevel) {
		return this.validFarmlandAroundVillager.isEmpty()
			? null
			: (BlockPos)this.validFarmlandAroundVillager.get(serverLevel.getRandom().nextInt(this.validFarmlandAroundVillager.size()));
	}

	private boolean validPos(BlockPos blockPos, ServerLevel serverLevel) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		Block block = blockState.getBlock();
		Block block2 = serverLevel.getBlockState(blockPos.below()).getBlock();
		return block instanceof CropBlock && ((CropBlock)block).isMaxAge(blockState) || blockState.isAir() && block2 instanceof FarmBlock;
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		if (l > this.nextOkStartTime && this.aboveFarmlandPos != null) {
			villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
			villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
		}
	}

	protected void stop(ServerLevel serverLevel, Villager villager, long l) {
		villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
		villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		this.timeWorkedSoFar = 0;
		this.nextOkStartTime = l + 40L;
	}

	protected void tick(ServerLevel serverLevel, Villager villager, long l) {
		if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerToCenterThan(villager.position(), 1.0)) {
			if (this.aboveFarmlandPos != null && l > this.nextOkStartTime) {
				BlockState blockState = serverLevel.getBlockState(this.aboveFarmlandPos);
				Block block = blockState.getBlock();
				Block block2 = serverLevel.getBlockState(this.aboveFarmlandPos.below()).getBlock();
				if (block instanceof CropBlock && ((CropBlock)block).isMaxAge(blockState)) {
					serverLevel.destroyBlock(this.aboveFarmlandPos, true, villager);
				}

				if (blockState.isAir() && block2 instanceof FarmBlock && villager.hasFarmSeeds()) {
					SimpleContainer simpleContainer = villager.getInventory();

					for (int i = 0; i < simpleContainer.getContainerSize(); i++) {
						ItemStack itemStack = simpleContainer.getItem(i);
						boolean bl = false;
						if (!itemStack.isEmpty()) {
							if (itemStack.is(Items.WHEAT_SEEDS)) {
								BlockState blockState2 = Blocks.WHEAT.defaultBlockState();
								serverLevel.setBlockAndUpdate(this.aboveFarmlandPos, blockState2);
								serverLevel.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, GameEvent.Context.of(villager, blockState2));
								bl = true;
							} else if (itemStack.is(Items.POTATO)) {
								BlockState blockState2 = Blocks.POTATOES.defaultBlockState();
								serverLevel.setBlockAndUpdate(this.aboveFarmlandPos, blockState2);
								serverLevel.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, GameEvent.Context.of(villager, blockState2));
								bl = true;
							} else if (itemStack.is(Items.CARROT)) {
								BlockState blockState2 = Blocks.CARROTS.defaultBlockState();
								serverLevel.setBlockAndUpdate(this.aboveFarmlandPos, blockState2);
								serverLevel.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, GameEvent.Context.of(villager, blockState2));
								bl = true;
							} else if (itemStack.is(Items.BEETROOT_SEEDS)) {
								BlockState blockState2 = Blocks.BEETROOTS.defaultBlockState();
								serverLevel.setBlockAndUpdate(this.aboveFarmlandPos, blockState2);
								serverLevel.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, GameEvent.Context.of(villager, blockState2));
								bl = true;
							}
						}

						if (bl) {
							serverLevel.playSound(
								null,
								(double)this.aboveFarmlandPos.getX(),
								(double)this.aboveFarmlandPos.getY(),
								(double)this.aboveFarmlandPos.getZ(),
								SoundEvents.CROP_PLANTED,
								SoundSource.BLOCKS,
								1.0F,
								1.0F
							);
							itemStack.shrink(1);
							if (itemStack.isEmpty()) {
								simpleContainer.setItem(i, ItemStack.EMPTY);
							}
							break;
						}
					}
				}

				if (block instanceof CropBlock && !((CropBlock)block).isMaxAge(blockState)) {
					this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
					this.aboveFarmlandPos = this.getValidFarmland(serverLevel);
					if (this.aboveFarmlandPos != null) {
						this.nextOkStartTime = l + 20L;
						villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
						villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
					}
				}
			}

			this.timeWorkedSoFar++;
		}
	}

	protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
		return this.timeWorkedSoFar < 200;
	}
}
