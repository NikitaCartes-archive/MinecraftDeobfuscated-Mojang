package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SerializableLong;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

public class Villager extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder {
	private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.VILLAGER_DATA);
	public static final Map<Item, Integer> FOOD_POINTS = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
	private static final Set<Item> WANTED_ITEMS = ImmutableSet.of(
		Items.BREAD, Items.POTATO, Items.CARROT, Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT, Items.BEETROOT_SEEDS
	);
	private int updateMerchantTimer;
	private boolean increaseProfessionLevelOnUpdate;
	@Nullable
	private Player lastTradedPlayer;
	private byte foodLevel;
	private final GossipContainer gossips = new GossipContainer();
	private long lastGossipTime;
	private long lastGossipDecayTime;
	private int villagerXp;
	private long lastRestockGameTime;
	private int numberOfRestocksToday;
	private long lastRestockCheckDayTime;
	private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.HOME,
		MemoryModuleType.JOB_SITE,
		MemoryModuleType.MEETING_POINT,
		MemoryModuleType.LIVING_ENTITIES,
		MemoryModuleType.VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.VISIBLE_VILLAGER_BABIES,
		MemoryModuleType.NEAREST_PLAYERS,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.INTERACTION_TARGET,
		MemoryModuleType.BREED_TARGET,
		MemoryModuleType.PATH,
		MemoryModuleType.INTERACTABLE_DOORS,
		MemoryModuleType.OPENED_DOORS,
		MemoryModuleType.NEAREST_BED,
		MemoryModuleType.HURT_BY,
		MemoryModuleType.HURT_BY_ENTITY,
		MemoryModuleType.NEAREST_HOSTILE,
		MemoryModuleType.SECONDARY_JOB_SITE,
		MemoryModuleType.HIDING_PLACE,
		MemoryModuleType.HEARD_BELL_TIME,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.LAST_SLEPT,
		MemoryModuleType.LAST_WOKEN,
		MemoryModuleType.LAST_WORKED_AT_POI,
		MemoryModuleType.GOLEM_LAST_SEEN_TIME
	);
	private static final ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES,
		SensorType.NEAREST_PLAYERS,
		SensorType.INTERACTABLE_DOORS,
		SensorType.NEAREST_BED,
		SensorType.HURT_BY,
		SensorType.VILLAGER_HOSTILES,
		SensorType.VILLAGER_BABIES,
		SensorType.SECONDARY_POIS,
		SensorType.GOLEM_LAST_SEEN
	);
	public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<Villager, PoiType>> POI_MEMORIES = ImmutableMap.of(
		MemoryModuleType.HOME,
		(villager, poiType) -> poiType == PoiType.HOME,
		MemoryModuleType.JOB_SITE,
		(villager, poiType) -> villager.getVillagerData().getProfession().getJobPoiType() == poiType,
		MemoryModuleType.MEETING_POINT,
		(villager, poiType) -> poiType == PoiType.MEETING
	);

	public Villager(EntityType<? extends Villager> entityType, Level level) {
		this(entityType, level, VillagerType.PLAINS);
	}

	public Villager(EntityType<? extends Villager> entityType, Level level, VillagerType villagerType) {
		super(entityType, level);
		((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
		this.getNavigation().setCanFloat(true);
		this.setCanPickUpLoot(true);
		this.setVillagerData(this.getVillagerData().setType(villagerType).setProfession(VillagerProfession.NONE));
		this.brain = this.makeBrain(new Dynamic<>(NbtOps.INSTANCE, new CompoundTag()));
	}

	@Override
	public Brain<Villager> getBrain() {
		return (Brain<Villager>)super.getBrain();
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		Brain<Villager> brain = new Brain<>(MEMORY_TYPES, SENSOR_TYPES, dynamic);
		this.registerBrainGoals(brain);
		return brain;
	}

	public void refreshBrain(ServerLevel serverLevel) {
		Brain<Villager> brain = this.getBrain();
		brain.stopAll(serverLevel, this);
		this.brain = brain.copyWithoutGoals();
		this.registerBrainGoals(this.getBrain());
	}

	private void registerBrainGoals(Brain<Villager> brain) {
		VillagerProfession villagerProfession = this.getVillagerData().getProfession();
		float f = (float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
		if (this.isBaby()) {
			brain.setSchedule(Schedule.VILLAGER_BABY);
			brain.addActivity(Activity.PLAY, VillagerGoalPackages.getPlayPackage(f));
		} else {
			brain.setSchedule(Schedule.VILLAGER_DEFAULT);
			brain.addActivity(
				Activity.WORK, VillagerGoalPackages.getWorkPackage(villagerProfession, f), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT))
			);
		}

		brain.addActivity(Activity.CORE, VillagerGoalPackages.getCorePackage(villagerProfession, f));
		brain.addActivity(
			Activity.MEET,
			VillagerGoalPackages.getMeetPackage(villagerProfession, f),
			ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT))
		);
		brain.addActivity(Activity.REST, VillagerGoalPackages.getRestPackage(villagerProfession, f));
		brain.addActivity(Activity.IDLE, VillagerGoalPackages.getIdlePackage(villagerProfession, f));
		brain.addActivity(Activity.PANIC, VillagerGoalPackages.getPanicPackage(villagerProfession, f));
		brain.addActivity(Activity.PRE_RAID, VillagerGoalPackages.getPreRaidPackage(villagerProfession, f));
		brain.addActivity(Activity.RAID, VillagerGoalPackages.getRaidPackage(villagerProfession, f));
		brain.addActivity(Activity.HIDE, VillagerGoalPackages.getHidePackage(villagerProfession, f));
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.setActivity(Activity.IDLE);
		brain.updateActivity(this.level.getDayTime(), this.level.getGameTime());
	}

	@Override
	protected void ageBoundaryReached() {
		super.ageBoundaryReached();
		if (this.level instanceof ServerLevel) {
			this.refreshBrain((ServerLevel)this.level);
		}
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0);
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("brain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		if (!this.isTrading() && this.updateMerchantTimer > 0) {
			this.updateMerchantTimer--;
			if (this.updateMerchantTimer <= 0) {
				if (this.increaseProfessionLevelOnUpdate) {
					this.increaseMerchantCareer();
					this.increaseProfessionLevelOnUpdate = false;
				}

				this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
			}
		}

		if (this.lastTradedPlayer != null && this.level instanceof ServerLevel) {
			((ServerLevel)this.level).onReputationEvent(ReputationEventType.TRADE, this.lastTradedPlayer, this);
			this.level.broadcastEntityEvent(this, (byte)14);
			this.lastTradedPlayer = null;
		}

		if (!this.isNoAi() && this.random.nextInt(100) == 0) {
			Raid raid = ((ServerLevel)this.level).getRaidAt(new BlockPos(this));
			if (raid != null && raid.isActive() && !raid.isOver()) {
				this.level.broadcastEntityEvent(this, (byte)42);
			}
		}

		if (this.getVillagerData().getProfession() == VillagerProfession.NONE && this.isTrading()) {
			this.stopTrading();
		}

		super.customServerAiStep();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getUnhappyCounter() > 0) {
			this.setUnhappyCounter(this.getUnhappyCounter() - 1);
		}

		this.maybeDecayGossip();
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		boolean bl = itemStack.getItem() == Items.NAME_TAG;
		if (bl) {
			itemStack.interactEnemy(player, this, interactionHand);
			return true;
		} else if (itemStack.getItem() == Items.VILLAGER_SPAWN_EGG || !this.isAlive() || this.isTrading() || this.isSleeping()) {
			return super.mobInteract(player, interactionHand);
		} else if (this.isBaby()) {
			this.setUnhappy();
			return super.mobInteract(player, interactionHand);
		} else {
			boolean bl2 = this.getOffers().isEmpty();
			if (interactionHand == InteractionHand.MAIN_HAND) {
				if (bl2 && !this.level.isClientSide) {
					this.setUnhappy();
				}

				player.awardStat(Stats.TALKED_TO_VILLAGER);
			}

			if (bl2) {
				return super.mobInteract(player, interactionHand);
			} else {
				if (!this.level.isClientSide && !this.offers.isEmpty()) {
					this.startTrading(player);
				}

				return true;
			}
		}
	}

	private void setUnhappy() {
		this.setUnhappyCounter(40);
		if (!this.level.isClientSide()) {
			this.playSound(SoundEvents.VILLAGER_NO, this.getSoundVolume(), this.getVoicePitch());
		}
	}

	private void startTrading(Player player) {
		this.updateSpecialPrices(player);
		this.setTradingPlayer(player);
		this.openTradingScreen(player, this.getDisplayName(), this.getVillagerData().getLevel());
	}

	@Override
	public void setTradingPlayer(@Nullable Player player) {
		boolean bl = this.getTradingPlayer() != null && player == null;
		super.setTradingPlayer(player);
		if (bl) {
			this.stopTrading();
		}
	}

	@Override
	protected void stopTrading() {
		super.stopTrading();
		this.resetSpecialPrices();
	}

	private void resetSpecialPrices() {
		for (MerchantOffer merchantOffer : this.getOffers()) {
			merchantOffer.resetSpecialPriceDiff();
		}
	}

	@Override
	public boolean canRestock() {
		return true;
	}

	public void restock() {
		this.updateDemand();

		for (MerchantOffer merchantOffer : this.getOffers()) {
			merchantOffer.resetUses();
		}

		if (this.getVillagerData().getProfession() == VillagerProfession.FARMER) {
			this.makeBread();
		}

		this.lastRestockGameTime = this.level.getGameTime();
		this.numberOfRestocksToday++;
	}

	private boolean needsToRestock() {
		for (MerchantOffer merchantOffer : this.getOffers()) {
			if (merchantOffer.needsRestock()) {
				return true;
			}
		}

		return false;
	}

	private boolean allowedToRestock() {
		return this.numberOfRestocksToday == 0 || this.numberOfRestocksToday < 2 && this.level.getGameTime() > this.lastRestockGameTime + 2400L;
	}

	public boolean shouldRestock() {
		long l = this.lastRestockGameTime + 12000L;
		long m = this.level.getGameTime();
		boolean bl = m > l;
		long n = this.level.getDayTime();
		if (this.lastRestockCheckDayTime > 0L) {
			long o = this.lastRestockCheckDayTime / 24000L;
			long p = n / 24000L;
			bl |= p > o;
		}

		this.lastRestockCheckDayTime = n;
		if (bl) {
			this.lastRestockGameTime = m;
			this.resetNumberOfRestocks();
		}

		return this.allowedToRestock() && this.needsToRestock();
	}

	private void catchUpDemand() {
		int i = 2 - this.numberOfRestocksToday;
		if (i > 0) {
			for (MerchantOffer merchantOffer : this.getOffers()) {
				merchantOffer.resetUses();
			}
		}

		for (int j = 0; j < i; j++) {
			this.updateDemand();
		}
	}

	private void updateDemand() {
		for (MerchantOffer merchantOffer : this.getOffers()) {
			merchantOffer.updateDemand();
		}
	}

	private void updateSpecialPrices(Player player) {
		int i = this.getPlayerReputation(player);
		if (i != 0) {
			for (MerchantOffer merchantOffer : this.getOffers()) {
				merchantOffer.addToSpecialPriceDiff(-Mth.floor((float)i * merchantOffer.getPriceMultiplier()));
			}
		}

		if (player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
			MobEffectInstance mobEffectInstance = player.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
			int j = mobEffectInstance.getAmplifier();

			for (MerchantOffer merchantOffer2 : this.getOffers()) {
				double d = 0.3 + 0.0625 * (double)j;
				int k = (int)Math.floor(d * (double)merchantOffer2.getBaseCostA().getCount());
				merchantOffer2.addToSpecialPriceDiff(-Math.max(k, 1));
			}
		}
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.put("VillagerData", this.getVillagerData().serialize(NbtOps.INSTANCE));
		compoundTag.putByte("FoodLevel", this.foodLevel);
		compoundTag.put("Gossips", this.gossips.store(NbtOps.INSTANCE).getValue());
		compoundTag.putInt("Xp", this.villagerXp);
		compoundTag.putLong("LastRestock", this.lastRestockGameTime);
		compoundTag.putLong("LastGossipDecay", this.lastGossipDecayTime);
		compoundTag.putInt("RestocksToday", this.numberOfRestocksToday);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("VillagerData", 10)) {
			this.setVillagerData(new VillagerData(new Dynamic<>(NbtOps.INSTANCE, compoundTag.get("VillagerData"))));
		}

		if (compoundTag.contains("Offers", 10)) {
			this.offers = new MerchantOffers(compoundTag.getCompound("Offers"));
		}

		if (compoundTag.contains("FoodLevel", 1)) {
			this.foodLevel = compoundTag.getByte("FoodLevel");
		}

		ListTag listTag = compoundTag.getList("Gossips", 10);
		this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, listTag));
		if (compoundTag.contains("Xp", 3)) {
			this.villagerXp = compoundTag.getInt("Xp");
		}

		this.lastRestockGameTime = compoundTag.getLong("LastRestock");
		this.lastGossipDecayTime = compoundTag.getLong("LastGossipDecay");
		this.setCanPickUpLoot(true);
		this.refreshBrain((ServerLevel)this.level);
		this.numberOfRestocksToday = compoundTag.getInt("RestocksToday");
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return false;
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		if (this.isSleeping()) {
			return null;
		} else {
			return this.isTrading() ? SoundEvents.VILLAGER_TRADE : SoundEvents.VILLAGER_AMBIENT;
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.VILLAGER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.VILLAGER_DEATH;
	}

	public void playWorkSound() {
		SoundEvent soundEvent = this.getVillagerData().getProfession().getWorkSound();
		if (soundEvent != null) {
			this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
		}
	}

	public void setVillagerData(VillagerData villagerData) {
		VillagerData villagerData2 = this.getVillagerData();
		if (villagerData2.getProfession() != villagerData.getProfession()) {
			this.offers = null;
		}

		this.entityData.set(DATA_VILLAGER_DATA, villagerData);
	}

	@Override
	public VillagerData getVillagerData() {
		return this.entityData.get(DATA_VILLAGER_DATA);
	}

	@Override
	protected void rewardTradeXp(MerchantOffer merchantOffer) {
		int i = 3 + this.random.nextInt(4);
		this.villagerXp = this.villagerXp + merchantOffer.getXp();
		this.lastTradedPlayer = this.getTradingPlayer();
		if (this.shouldIncreaseLevel()) {
			this.updateMerchantTimer = 40;
			this.increaseProfessionLevelOnUpdate = true;
			i += 5;
		}

		if (merchantOffer.shouldRewardExp()) {
			this.level.addFreshEntity(new ExperienceOrb(this.level, this.getX(), this.getY() + 0.5, this.getZ(), i));
		}
	}

	@Override
	public void setLastHurtByMob(@Nullable LivingEntity livingEntity) {
		if (livingEntity != null && this.level instanceof ServerLevel) {
			((ServerLevel)this.level).onReputationEvent(ReputationEventType.VILLAGER_HURT, livingEntity, this);
			if (this.isAlive() && livingEntity instanceof Player) {
				this.level.broadcastEntityEvent(this, (byte)13);
			}
		}

		super.setLastHurtByMob(livingEntity);
	}

	@Override
	public void die(DamageSource damageSource) {
		LOGGER.info("Villager {} died, message: '{}'", this, damageSource.getLocalizedDeathMessage(this).getString());
		Entity entity = damageSource.getEntity();
		if (entity != null) {
			this.tellWitnessesThatIWasMurdered(entity);
		}

		this.releasePoi(MemoryModuleType.HOME);
		this.releasePoi(MemoryModuleType.JOB_SITE);
		this.releasePoi(MemoryModuleType.MEETING_POINT);
		super.die(damageSource);
	}

	private void tellWitnessesThatIWasMurdered(Entity entity) {
		if (this.level instanceof ServerLevel) {
			Optional<List<LivingEntity>> optional = this.brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
			if (optional.isPresent()) {
				ServerLevel serverLevel = (ServerLevel)this.level;
				((List)optional.get())
					.stream()
					.filter(livingEntity -> livingEntity instanceof ReputationEventHandler)
					.forEach(livingEntity -> serverLevel.onReputationEvent(ReputationEventType.VILLAGER_KILLED, entity, (ReputationEventHandler)livingEntity));
			}
		}
	}

	public void releasePoi(MemoryModuleType<GlobalPos> memoryModuleType) {
		if (this.level instanceof ServerLevel) {
			MinecraftServer minecraftServer = ((ServerLevel)this.level).getServer();
			this.brain.getMemory(memoryModuleType).ifPresent(globalPos -> {
				ServerLevel serverLevel = minecraftServer.getLevel(globalPos.dimension());
				PoiManager poiManager = serverLevel.getPoiManager();
				Optional<PoiType> optional = poiManager.getType(globalPos.pos());
				BiPredicate<Villager, PoiType> biPredicate = (BiPredicate<Villager, PoiType>)POI_MEMORIES.get(memoryModuleType);
				if (optional.isPresent() && biPredicate.test(this, optional.get())) {
					poiManager.release(globalPos.pos());
					DebugPackets.sendPoiTicketCountPacket(serverLevel, globalPos.pos());
				}
			});
		}
	}

	public boolean canBreed() {
		return this.foodLevel + this.countFoodPointsInInventory() >= 12 && this.getAge() == 0;
	}

	private boolean hungry() {
		return this.foodLevel < 12;
	}

	private void eatUntilFull() {
		if (this.hungry() && this.countFoodPointsInInventory() != 0) {
			for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
				ItemStack itemStack = this.getInventory().getItem(i);
				if (!itemStack.isEmpty()) {
					Integer integer = (Integer)FOOD_POINTS.get(itemStack.getItem());
					if (integer != null) {
						int j = itemStack.getCount();

						for (int k = j; k > 0; k--) {
							this.foodLevel = (byte)(this.foodLevel + integer);
							this.getInventory().removeItem(i, 1);
							if (!this.hungry()) {
								return;
							}
						}
					}
				}
			}
		}
	}

	public int getPlayerReputation(Player player) {
		return this.gossips.getReputation(player.getUUID(), gossipType -> true);
	}

	private void digestFood(int i) {
		this.foodLevel = (byte)(this.foodLevel - i);
	}

	public void eatAndDigestFood() {
		this.eatUntilFull();
		this.digestFood(12);
	}

	public void setOffers(MerchantOffers merchantOffers) {
		this.offers = merchantOffers;
	}

	private boolean shouldIncreaseLevel() {
		int i = this.getVillagerData().getLevel();
		return VillagerData.canLevelUp(i) && this.villagerXp >= VillagerData.getMaxXpPerLevel(i);
	}

	private void increaseMerchantCareer() {
		this.setVillagerData(this.getVillagerData().setLevel(this.getVillagerData().getLevel() + 1));
		this.updateTrades();
	}

	@Override
	protected Component getTypeName() {
		return new TranslatableComponent(
			this.getType().getDescriptionId() + '.' + Registry.VILLAGER_PROFESSION.getKey(this.getVillagerData().getProfession()).getPath()
		);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 12) {
			this.addParticlesAroundSelf(ParticleTypes.HEART);
		} else if (b == 13) {
			this.addParticlesAroundSelf(ParticleTypes.ANGRY_VILLAGER);
		} else if (b == 14) {
			this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
		} else if (b == 42) {
			this.addParticlesAroundSelf(ParticleTypes.SPLASH);
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		LevelAccessor levelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		if (mobSpawnType == MobSpawnType.BREEDING) {
			this.setVillagerData(this.getVillagerData().setProfession(VillagerProfession.NONE));
		}

		if (mobSpawnType == MobSpawnType.COMMAND
			|| mobSpawnType == MobSpawnType.SPAWN_EGG
			|| mobSpawnType == MobSpawnType.SPAWNER
			|| mobSpawnType == MobSpawnType.DISPENSER) {
			this.setVillagerData(this.getVillagerData().setType(VillagerType.byBiome(levelAccessor.getBiome(new BlockPos(this)))));
		}

		return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public Villager getBreedOffspring(AgableMob agableMob) {
		double d = this.random.nextDouble();
		VillagerType villagerType;
		if (d < 0.5) {
			villagerType = VillagerType.byBiome(this.level.getBiome(new BlockPos(this)));
		} else if (d < 0.75) {
			villagerType = this.getVillagerData().getType();
		} else {
			villagerType = ((Villager)agableMob).getVillagerData().getType();
		}

		Villager villager = new Villager(EntityType.VILLAGER, this.level, villagerType);
		villager.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(new BlockPos(villager)), MobSpawnType.BREEDING, null, null);
		return villager;
	}

	@Override
	public void thunderHit(LightningBolt lightningBolt) {
		Witch witch = EntityType.WITCH.create(this.level);
		witch.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
		witch.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(new BlockPos(witch)), MobSpawnType.CONVERSION, null, null);
		witch.setNoAi(this.isNoAi());
		if (this.hasCustomName()) {
			witch.setCustomName(this.getCustomName());
			witch.setCustomNameVisible(this.isCustomNameVisible());
		}

		this.level.addFreshEntity(witch);
		this.remove();
	}

	@Override
	protected void pickUpItem(ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		Item item = itemStack.getItem();
		if (this.wantToPickUp(item)) {
			SimpleContainer simpleContainer = this.getInventory();
			boolean bl = false;

			for (int i = 0; i < simpleContainer.getContainerSize(); i++) {
				ItemStack itemStack2 = simpleContainer.getItem(i);
				if (itemStack2.isEmpty() || itemStack2.getItem() == item && itemStack2.getCount() < itemStack2.getMaxStackSize()) {
					bl = true;
					break;
				}
			}

			if (!bl) {
				return;
			}

			int ix = simpleContainer.countItem(item);
			if (ix == 256) {
				return;
			}

			if (ix > 256) {
				simpleContainer.removeItemType(item, ix - 256);
				return;
			}

			this.take(itemEntity, itemStack.getCount());
			ItemStack itemStack2 = simpleContainer.addItem(itemStack);
			if (itemStack2.isEmpty()) {
				itemEntity.remove();
			} else {
				itemStack.setCount(itemStack2.getCount());
			}
		}
	}

	public boolean wantToPickUp(Item item) {
		return WANTED_ITEMS.contains(item) || this.getVillagerData().getProfession().getRequestedItems().contains(item);
	}

	public boolean hasExcessFood() {
		return this.countFoodPointsInInventory() >= 24;
	}

	public boolean wantsMoreFood() {
		return this.countFoodPointsInInventory() < 12;
	}

	private int countFoodPointsInInventory() {
		SimpleContainer simpleContainer = this.getInventory();
		return FOOD_POINTS.entrySet().stream().mapToInt(entry -> simpleContainer.countItem((Item)entry.getKey()) * (Integer)entry.getValue()).sum();
	}

	private void makeBread() {
		SimpleContainer simpleContainer = this.getInventory();
		int i = simpleContainer.countItem(Items.WHEAT);
		int j = i / 3;
		if (j != 0) {
			int k = j * 3;
			simpleContainer.removeItemType(Items.WHEAT, k);
			ItemStack itemStack = simpleContainer.addItem(new ItemStack(Items.BREAD, j));
			if (!itemStack.isEmpty()) {
				this.spawnAtLocation(itemStack, 0.5F);
			}
		}
	}

	public boolean hasFarmSeeds() {
		SimpleContainer simpleContainer = this.getInventory();
		return simpleContainer.hasAnyOf(ImmutableSet.of(Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS));
	}

	@Override
	protected void updateTrades() {
		VillagerData villagerData = this.getVillagerData();
		Int2ObjectMap<VillagerTrades.ItemListing[]> int2ObjectMap = (Int2ObjectMap<VillagerTrades.ItemListing[]>)VillagerTrades.TRADES
			.get(villagerData.getProfession());
		if (int2ObjectMap != null && !int2ObjectMap.isEmpty()) {
			VillagerTrades.ItemListing[] itemListings = int2ObjectMap.get(villagerData.getLevel());
			if (itemListings != null) {
				MerchantOffers merchantOffers = this.getOffers();
				this.addOffersFromItemListings(merchantOffers, itemListings, 2);
			}
		}
	}

	public void gossip(Villager villager, long l) {
		if ((l < this.lastGossipTime || l >= this.lastGossipTime + 1200L) && (l < villager.lastGossipTime || l >= villager.lastGossipTime + 1200L)) {
			this.gossips.transferFrom(villager.gossips, this.random, 10);
			this.lastGossipTime = l;
			villager.lastGossipTime = l;
			this.spawnGolemIfNeeded(l, 5);
		}
	}

	private void maybeDecayGossip() {
		long l = this.level.getGameTime();
		if (this.lastGossipDecayTime == 0L) {
			this.lastGossipDecayTime = l;
		} else if (l >= this.lastGossipDecayTime + 24000L) {
			this.gossips.decay();
			this.lastGossipDecayTime = l;
		}
	}

	public void spawnGolemIfNeeded(long l, int i) {
		if (this.wantsToSpawnGolem(l)) {
			AABB aABB = this.getBoundingBox().inflate(10.0, 10.0, 10.0);
			List<Villager> list = this.level.getEntitiesOfClass(Villager.class, aABB);
			List<Villager> list2 = (List<Villager>)list.stream().filter(villager -> villager.wantsToSpawnGolem(l)).limit(5L).collect(Collectors.toList());
			if (list2.size() >= i) {
				IronGolem ironGolem = this.trySpawnGolem();
				if (ironGolem != null) {
					list.forEach(villager -> villager.sawGolem(l));
				}
			}
		}
	}

	private void sawGolem(long l) {
		this.brain.setMemory(MemoryModuleType.GOLEM_LAST_SEEN_TIME, l);
	}

	private boolean hasSeenGolemRecently(long l) {
		Optional<Long> optional = this.brain.getMemory(MemoryModuleType.GOLEM_LAST_SEEN_TIME);
		if (!optional.isPresent()) {
			return false;
		} else {
			Long long_ = (Long)optional.get();
			return l - long_ <= 600L;
		}
	}

	public boolean wantsToSpawnGolem(long l) {
		VillagerData villagerData = this.getVillagerData();
		if (villagerData.getProfession() == VillagerProfession.NONE || villagerData.getProfession() == VillagerProfession.NITWIT) {
			return false;
		} else {
			return !this.golemSpawnConditionsMet(this.level.getGameTime()) ? false : !this.hasSeenGolemRecently(l);
		}
	}

	@Nullable
	private IronGolem trySpawnGolem() {
		BlockPos blockPos = new BlockPos(this);

		for (int i = 0; i < 10; i++) {
			double d = (double)(this.level.random.nextInt(16) - 8);
			double e = (double)(this.level.random.nextInt(16) - 8);
			double f = 6.0;

			for (int j = 0; j >= -12; j--) {
				BlockPos blockPos2 = blockPos.offset(d, f + (double)j, e);
				if ((this.level.getBlockState(blockPos2).isAir() || this.level.getBlockState(blockPos2).getMaterial().isLiquid())
					&& this.level.getBlockState(blockPos2.below()).getMaterial().isSolidBlocking()) {
					f += (double)j;
					break;
				}
			}

			BlockPos blockPos3 = blockPos.offset(d, f, e);
			IronGolem ironGolem = EntityType.IRON_GOLEM.create(this.level, null, null, null, blockPos3, MobSpawnType.MOB_SUMMONED, false, false);
			if (ironGolem != null) {
				if (ironGolem.checkSpawnRules(this.level, MobSpawnType.MOB_SUMMONED) && ironGolem.checkSpawnObstruction(this.level)) {
					this.level.addFreshEntity(ironGolem);
					return ironGolem;
				}

				ironGolem.remove();
			}
		}

		return null;
	}

	@Override
	public void onReputationEventFrom(ReputationEventType reputationEventType, Entity entity) {
		if (reputationEventType == ReputationEventType.ZOMBIE_VILLAGER_CURED) {
			this.gossips.add(entity.getUUID(), GossipType.MAJOR_POSITIVE, 20);
			this.gossips.add(entity.getUUID(), GossipType.MINOR_POSITIVE, 25);
		} else if (reputationEventType == ReputationEventType.TRADE) {
			this.gossips.add(entity.getUUID(), GossipType.TRADING, 2);
		} else if (reputationEventType == ReputationEventType.VILLAGER_HURT) {
			this.gossips.add(entity.getUUID(), GossipType.MINOR_NEGATIVE, 25);
		} else if (reputationEventType == ReputationEventType.VILLAGER_KILLED) {
			this.gossips.add(entity.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
		}
	}

	@Override
	public int getVillagerXp() {
		return this.villagerXp;
	}

	public void setVillagerXp(int i) {
		this.villagerXp = i;
	}

	private void resetNumberOfRestocks() {
		this.catchUpDemand();
		this.numberOfRestocksToday = 0;
	}

	public GossipContainer getGossips() {
		return this.gossips;
	}

	public void setGossips(Tag tag) {
		this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, tag));
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public void startSleeping(BlockPos blockPos) {
		super.startSleeping(blockPos);
		this.brain.setMemory(MemoryModuleType.LAST_SLEPT, SerializableLong.of(this.level.getGameTime()));
	}

	@Override
	public void stopSleeping() {
		super.stopSleeping();
		this.brain.setMemory(MemoryModuleType.LAST_WOKEN, SerializableLong.of(this.level.getGameTime()));
	}

	private boolean golemSpawnConditionsMet(long l) {
		Optional<SerializableLong> optional = this.brain.getMemory(MemoryModuleType.LAST_SLEPT);
		Optional<SerializableLong> optional2 = this.brain.getMemory(MemoryModuleType.LAST_WORKED_AT_POI);
		return optional.isPresent() && optional2.isPresent()
			? l - ((SerializableLong)optional.get()).value() < 24000L && l - ((SerializableLong)optional2.get()).value() < 36000L
			: false;
	}
}
