package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

public class CelebrateVillagersSurvivedRaid extends Behavior<Villager> {
	@Nullable
	private Raid currentRaid;

	public CelebrateVillagersSurvivedRaid(int i, int j) {
		super(ImmutableMap.of(), i, j);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		BlockPos blockPos = villager.blockPosition();
		this.currentRaid = serverLevel.getRaidAt(blockPos);
		return this.currentRaid != null && this.currentRaid.isVictory() && MoveToSkySeeingSpot.hasNoBlocksAbove(serverLevel, villager, blockPos);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
		return this.currentRaid != null && !this.currentRaid.isStopped();
	}

	protected void stop(ServerLevel serverLevel, Villager villager, long l) {
		this.currentRaid = null;
		villager.getBrain().updateActivityFromSchedule(serverLevel.getDayTime(), serverLevel.getGameTime());
	}

	protected void tick(ServerLevel serverLevel, Villager villager, long l) {
		RandomSource randomSource = villager.getRandom();
		if (randomSource.nextInt(100) == 0) {
			villager.playCelebrateSound();
		}

		if (randomSource.nextInt(200) == 0 && MoveToSkySeeingSpot.hasNoBlocksAbove(serverLevel, villager, villager.blockPosition())) {
			DyeColor dyeColor = Util.getRandom(DyeColor.values(), randomSource);
			int i = randomSource.nextInt(3);
			ItemStack itemStack = this.getFirework(dyeColor, i);
			Projectile.spawnProjectile(
				new FireworkRocketEntity(villager.level(), villager, villager.getX(), villager.getEyeY(), villager.getZ(), itemStack), serverLevel, itemStack
			);
		}
	}

	private ItemStack getFirework(DyeColor dyeColor, int i) {
		ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
		itemStack.set(
			DataComponents.FIREWORKS,
			new Fireworks((byte)i, List.of(new FireworkExplosion(FireworkExplosion.Shape.BURST, IntList.of(dyeColor.getFireworkColor()), IntList.of(), false, false)))
		);
		return itemStack;
	}
}
