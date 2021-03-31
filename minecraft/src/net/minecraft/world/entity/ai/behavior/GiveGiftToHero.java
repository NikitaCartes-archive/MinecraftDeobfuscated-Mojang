package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class GiveGiftToHero extends Behavior<Villager> {
	private static final int THROW_GIFT_AT_DISTANCE = 5;
	private static final int MIN_TIME_BETWEEN_GIFTS = 600;
	private static final int MAX_TIME_BETWEEN_GIFTS = 6600;
	private static final int TIME_TO_DELAY_FOR_HEAD_TO_FINISH_TURNING = 20;
	private static final Map<VillagerProfession, ResourceLocation> GIFTS = Util.make(Maps.<VillagerProfession, ResourceLocation>newHashMap(), hashMap -> {
		hashMap.put(VillagerProfession.ARMORER, BuiltInLootTables.ARMORER_GIFT);
		hashMap.put(VillagerProfession.BUTCHER, BuiltInLootTables.BUTCHER_GIFT);
		hashMap.put(VillagerProfession.CARTOGRAPHER, BuiltInLootTables.CARTOGRAPHER_GIFT);
		hashMap.put(VillagerProfession.CLERIC, BuiltInLootTables.CLERIC_GIFT);
		hashMap.put(VillagerProfession.FARMER, BuiltInLootTables.FARMER_GIFT);
		hashMap.put(VillagerProfession.FISHERMAN, BuiltInLootTables.FISHERMAN_GIFT);
		hashMap.put(VillagerProfession.FLETCHER, BuiltInLootTables.FLETCHER_GIFT);
		hashMap.put(VillagerProfession.LEATHERWORKER, BuiltInLootTables.LEATHERWORKER_GIFT);
		hashMap.put(VillagerProfession.LIBRARIAN, BuiltInLootTables.LIBRARIAN_GIFT);
		hashMap.put(VillagerProfession.MASON, BuiltInLootTables.MASON_GIFT);
		hashMap.put(VillagerProfession.SHEPHERD, BuiltInLootTables.SHEPHERD_GIFT);
		hashMap.put(VillagerProfession.TOOLSMITH, BuiltInLootTables.TOOLSMITH_GIFT);
		hashMap.put(VillagerProfession.WEAPONSMITH, BuiltInLootTables.WEAPONSMITH_GIFT);
	});
	private static final float SPEED_MODIFIER = 0.5F;
	private int timeUntilNextGift = 600;
	private boolean giftGivenDuringThisRun;
	private long timeSinceStart;

	public GiveGiftToHero(int i) {
		super(
			ImmutableMap.of(
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.INTERACTION_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.NEAREST_VISIBLE_PLAYER,
				MemoryStatus.VALUE_PRESENT
			),
			i
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		if (!this.isHeroVisible(villager)) {
			return false;
		} else if (this.timeUntilNextGift > 0) {
			this.timeUntilNextGift--;
			return false;
		} else {
			return true;
		}
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		this.giftGivenDuringThisRun = false;
		this.timeSinceStart = l;
		Player player = (Player)this.getNearestTargetableHero(villager).get();
		villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, player);
		BehaviorUtils.lookAtEntity(villager, player);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
		return this.isHeroVisible(villager) && !this.giftGivenDuringThisRun;
	}

	protected void tick(ServerLevel serverLevel, Villager villager, long l) {
		Player player = (Player)this.getNearestTargetableHero(villager).get();
		BehaviorUtils.lookAtEntity(villager, player);
		if (this.isWithinThrowingDistance(villager, player)) {
			if (l - this.timeSinceStart > 20L) {
				this.throwGift(villager, player);
				this.giftGivenDuringThisRun = true;
			}
		} else {
			BehaviorUtils.setWalkAndLookTargetMemories(villager, player, 0.5F, 5);
		}
	}

	protected void stop(ServerLevel serverLevel, Villager villager, long l) {
		this.timeUntilNextGift = calculateTimeUntilNextGift(serverLevel);
		villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
		villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
	}

	private void throwGift(Villager villager, LivingEntity livingEntity) {
		for (ItemStack itemStack : this.getItemToThrow(villager)) {
			BehaviorUtils.throwItem(villager, itemStack, livingEntity.position());
		}
	}

	private List<ItemStack> getItemToThrow(Villager villager) {
		if (villager.isBaby()) {
			return ImmutableList.of(new ItemStack(Items.POPPY));
		} else {
			VillagerProfession villagerProfession = villager.getVillagerData().getProfession();
			if (GIFTS.containsKey(villagerProfession)) {
				LootTable lootTable = villager.level.getServer().getLootTables().get((ResourceLocation)GIFTS.get(villagerProfession));
				LootContext.Builder builder = new LootContext.Builder((ServerLevel)villager.level)
					.withParameter(LootContextParams.ORIGIN, villager.position())
					.withParameter(LootContextParams.THIS_ENTITY, villager)
					.withRandom(villager.getRandom());
				return lootTable.getRandomItems(builder.create(LootContextParamSets.GIFT));
			} else {
				return ImmutableList.of(new ItemStack(Items.WHEAT_SEEDS));
			}
		}
	}

	private boolean isHeroVisible(Villager villager) {
		return this.getNearestTargetableHero(villager).isPresent();
	}

	private Optional<Player> getNearestTargetableHero(Villager villager) {
		return villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(this::isHero);
	}

	private boolean isHero(Player player) {
		return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
	}

	private boolean isWithinThrowingDistance(Villager villager, Player player) {
		BlockPos blockPos = player.blockPosition();
		BlockPos blockPos2 = villager.blockPosition();
		return blockPos2.closerThan(blockPos, 5.0);
	}

	private static int calculateTimeUntilNextGift(ServerLevel serverLevel) {
		return 600 + serverLevel.random.nextInt(6001);
	}
}
