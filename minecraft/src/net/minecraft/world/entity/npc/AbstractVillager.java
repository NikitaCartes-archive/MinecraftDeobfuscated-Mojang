package net.minecraft.world.entity.npc;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class AbstractVillager extends AgeableMob implements InventoryCarrier, Npc, Merchant {
	private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(AbstractVillager.class, EntityDataSerializers.INT);
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int VILLAGER_SLOT_OFFSET = 300;
	private static final int VILLAGER_INVENTORY_SIZE = 8;
	@Nullable
	private Player tradingPlayer;
	@Nullable
	protected MerchantOffers offers;
	private final SimpleContainer inventory = new SimpleContainer(8);

	public AbstractVillager(EntityType<? extends AbstractVillager> entityType, Level level) {
		super(entityType, level);
		this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
		this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		if (spawnGroupData == null) {
			spawnGroupData = new AgeableMob.AgeableMobGroupData(false);
		}

		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
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
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_UNHAPPY_COUNTER, 0);
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
		if (this.level().isClientSide) {
			throw new IllegalStateException("Cannot load Villager offers on the client");
		} else {
			if (this.offers == null) {
				this.offers = new MerchantOffers();
				this.updateTrades();
			}

			return this.offers;
		}
	}

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
		if (!this.level().isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
			this.ambientSoundTime = -this.getAmbientSoundInterval();
			this.makeSound(this.getTradeUpdatedSound(!itemStack.isEmpty()));
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
		this.makeSound(SoundEvents.VILLAGER_CELEBRATE);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (!this.level().isClientSide) {
			MerchantOffers merchantOffers = this.getOffers();
			if (!merchantOffers.isEmpty()) {
				compoundTag.put("Offers", MerchantOffers.CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), merchantOffers).getOrThrow());
			}
		}

		this.writeInventoryToTag(compoundTag, this.registryAccess());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("Offers")) {
			MerchantOffers.CODEC
				.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), compoundTag.get("Offers"))
				.resultOrPartial(Util.prefix("Failed to load offers: ", LOGGER::warn))
				.ifPresent(merchantOffers -> this.offers = merchantOffers);
		}

		this.readInventoryFromTag(compoundTag, this.registryAccess());
	}

	@Nullable
	@Override
	public Entity changeDimension(DimensionTransition dimensionTransition) {
		this.stopTrading();
		return super.changeDimension(dimensionTransition);
	}

	protected void stopTrading() {
		this.setTradingPlayer(null);
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
		this.stopTrading();
	}

	protected void addParticlesAroundSelf(ParticleOptions particleOptions) {
		for (int i = 0; i < 5; i++) {
			double d = this.random.nextGaussian() * 0.02;
			double e = this.random.nextGaussian() * 0.02;
			double f = this.random.nextGaussian() * 0.02;
			this.level().addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), d, e, f);
		}
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public SimpleContainer getInventory() {
		return this.inventory;
	}

	@Override
	public SlotAccess getSlot(int i) {
		int j = i - 300;
		return j >= 0 && j < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, j) : super.getSlot(i);
	}

	protected abstract void updateTrades();

	protected void addOffersFromItemListings(MerchantOffers merchantOffers, VillagerTrades.ItemListing[] itemListings, int i) {
		ArrayList<VillagerTrades.ItemListing> arrayList = Lists.newArrayList(itemListings);
		int j = 0;

		while (j < i && !arrayList.isEmpty()) {
			MerchantOffer merchantOffer = ((VillagerTrades.ItemListing)arrayList.remove(this.random.nextInt(arrayList.size()))).getOffer(this, this.random);
			if (merchantOffer != null) {
				merchantOffers.add(merchantOffer);
				j++;
			}
		}
	}

	@Override
	public Vec3 getRopeHoldPosition(float f) {
		float g = Mth.lerp(f, this.yBodyRotO, this.yBodyRot) * (float) (Math.PI / 180.0);
		Vec3 vec3 = new Vec3(0.0, this.getBoundingBox().getYsize() - 1.0, 0.2);
		return this.getPosition(f).add(vec3.yRot(-g));
	}

	@Override
	public boolean isClientSide() {
		return this.level().isClientSide;
	}
}
