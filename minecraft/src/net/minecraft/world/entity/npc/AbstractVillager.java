package net.minecraft.world.entity.npc;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public abstract class AbstractVillager extends AgableMob implements Npc, Merchant {
	private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(AbstractVillager.class, EntityDataSerializers.INT);
	@Nullable
	private Player tradingPlayer;
	@Nullable
	protected MerchantOffers offers;
	private final SimpleContainer inventory = new SimpleContainer(8);

	public AbstractVillager(EntityType<? extends AbstractVillager> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		LevelAccessor levelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		if (spawnGroupData == null) {
			spawnGroupData = new AgableMob.AgableMobGroupData();
			((AgableMob.AgableMobGroupData)spawnGroupData).setShouldSpawnBaby(false);
		}

		return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public int getUnhappyCounter() {
		return this.entityData.get(DATA_UNHAPPY_COUNTER);
	}

	public void setUnhappyCounter(int i) {
		this.entityData.set(DATA_UNHAPPY_COUNTER, i);
	}

	@Override
	public int getVillagerXp() {
		return 0;
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return this.isBaby() ? 0.81F : 1.62F;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_UNHAPPY_COUNTER, 0);
	}

	@Override
	public void setTradingPlayer(@Nullable Player player) {
		this.tradingPlayer = player;
	}

	@Nullable
	@Override
	public Player getTradingPlayer() {
		return this.tradingPlayer;
	}

	public boolean isTrading() {
		return this.tradingPlayer != null;
	}

	@Override
	public MerchantOffers getOffers() {
		if (this.offers == null) {
			this.offers = new MerchantOffers();
			this.updateTrades();
		}

		return this.offers;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void overrideOffers(@Nullable MerchantOffers merchantOffers) {
	}

	@Override
	public void overrideXp(int i) {
	}

	@Override
	public void notifyTrade(MerchantOffer merchantOffer) {
		merchantOffer.increaseUses();
		this.ambientSoundTime = -this.getAmbientSoundInterval();
		this.rewardTradeXp(merchantOffer);
		if (this.tradingPlayer instanceof ServerPlayer) {
			CriteriaTriggers.TRADE.trigger((ServerPlayer)this.tradingPlayer, this, merchantOffer.getResult());
		}
	}

	protected abstract void rewardTradeXp(MerchantOffer merchantOffer);

	@Override
	public boolean showProgressBar() {
		return true;
	}

	@Override
	public void notifyTradeUpdated(ItemStack itemStack) {
		if (!this.level.isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
			this.ambientSoundTime = -this.getAmbientSoundInterval();
			this.playSound(this.getTradeUpdatedSound(!itemStack.isEmpty()), this.getSoundVolume(), this.getVoicePitch());
		}
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return SoundEvents.VILLAGER_YES;
	}

	protected SoundEvent getTradeUpdatedSound(boolean bl) {
		return bl ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
	}

	public void playCelebrateSound() {
		this.playSound(SoundEvents.VILLAGER_CELEBRATE, this.getSoundVolume(), this.getVoicePitch());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		MerchantOffers merchantOffers = this.getOffers();
		if (!merchantOffers.isEmpty()) {
			compoundTag.put("Offers", merchantOffers.createTag());
		}

		ListTag listTag = new ListTag();

		for (int i = 0; i < this.inventory.getContainerSize(); i++) {
			ItemStack itemStack = this.inventory.getItem(i);
			if (!itemStack.isEmpty()) {
				listTag.add(itemStack.save(new CompoundTag()));
			}
		}

		compoundTag.put("Inventory", listTag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("Offers", 10)) {
			this.offers = new MerchantOffers(compoundTag.getCompound("Offers"));
		}

		ListTag listTag = compoundTag.getList("Inventory", 10);

		for (int i = 0; i < listTag.size(); i++) {
			ItemStack itemStack = ItemStack.of(listTag.getCompound(i));
			if (!itemStack.isEmpty()) {
				this.inventory.addItem(itemStack);
			}
		}
	}

	@Nullable
	@Override
	public Entity changeDimension(DimensionType dimensionType) {
		this.stopTrading();
		return super.changeDimension(dimensionType);
	}

	protected void stopTrading() {
		this.setTradingPlayer(null);
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
		this.stopTrading();
	}

	@Environment(EnvType.CLIENT)
	protected void addParticlesAroundSelf(ParticleOptions particleOptions) {
		for (int i = 0; i < 5; i++) {
			double d = this.random.nextGaussian() * 0.02;
			double e = this.random.nextGaussian() * 0.02;
			double f = this.random.nextGaussian() * 0.02;
			this.level.addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), d, e, f);
		}
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return false;
	}

	public SimpleContainer getInventory() {
		return this.inventory;
	}

	@Override
	public boolean setSlot(int i, ItemStack itemStack) {
		if (super.setSlot(i, itemStack)) {
			return true;
		} else {
			int j = i - 300;
			if (j >= 0 && j < this.inventory.getContainerSize()) {
				this.inventory.setItem(j, itemStack);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public Level getLevel() {
		return this.level;
	}

	protected abstract void updateTrades();

	protected void addOffersFromItemListings(MerchantOffers merchantOffers, VillagerTrades.ItemListing[] itemListings, int i) {
		Set<Integer> set = Sets.<Integer>newHashSet();
		if (itemListings.length > i) {
			while (set.size() < i) {
				set.add(this.random.nextInt(itemListings.length));
			}
		} else {
			for (int j = 0; j < itemListings.length; j++) {
				set.add(j);
			}
		}

		for (Integer integer : set) {
			VillagerTrades.ItemListing itemListing = itemListings[integer];
			MerchantOffer merchantOffer = itemListing.getOffer(this, this.random);
			if (merchantOffer != null) {
				merchantOffers.add(merchantOffer);
			}
		}
	}
}
