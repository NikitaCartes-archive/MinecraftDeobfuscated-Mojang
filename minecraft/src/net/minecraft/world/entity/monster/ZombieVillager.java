package net.minecraft.world.entity.monster;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ZombieVillager extends Zombie implements VillagerDataHolder {
	private static final EntityDataAccessor<Boolean> DATA_CONVERTING_ID = SynchedEntityData.defineId(ZombieVillager.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(
		ZombieVillager.class, EntityDataSerializers.VILLAGER_DATA
	);
	private int villagerConversionTime;
	private UUID conversionStarter;
	private Tag gossips;
	private CompoundTag tradeOffers;
	private int villagerXp;

	public ZombieVillager(EntityType<? extends ZombieVillager> entityType, Level level) {
		super(entityType, level);
		this.setVillagerData(this.getVillagerData().setProfession(Registry.VILLAGER_PROFESSION.getRandom(this.random)));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_CONVERTING_ID, false);
		this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData()).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("VillagerData", tag));
		if (this.tradeOffers != null) {
			compoundTag.put("Offers", this.tradeOffers);
		}

		if (this.gossips != null) {
			compoundTag.put("Gossips", this.gossips);
		}

		compoundTag.putInt("ConversionTime", this.isConverting() ? this.villagerConversionTime : -1);
		if (this.conversionStarter != null) {
			compoundTag.putUUID("ConversionPlayer", this.conversionStarter);
		}

		compoundTag.putInt("Xp", this.villagerXp);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("VillagerData", 10)) {
			DataResult<VillagerData> dataResult = VillagerData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.get("VillagerData")));
			dataResult.resultOrPartial(LOGGER::error).ifPresent(this::setVillagerData);
		}

		if (compoundTag.contains("Offers", 10)) {
			this.tradeOffers = compoundTag.getCompound("Offers");
		}

		if (compoundTag.contains("Gossips", 10)) {
			this.gossips = compoundTag.getList("Gossips", 10);
		}

		if (compoundTag.contains("ConversionTime", 99) && compoundTag.getInt("ConversionTime") > -1) {
			this.startConverting(compoundTag.hasUUID("ConversionPlayer") ? compoundTag.getUUID("ConversionPlayer") : null, compoundTag.getInt("ConversionTime"));
		}

		if (compoundTag.contains("Xp", 3)) {
			this.villagerXp = compoundTag.getInt("Xp");
		}
	}

	@Override
	public void tick() {
		if (!this.level.isClientSide && this.isAlive() && this.isConverting()) {
			int i = this.getConversionProgress();
			this.villagerConversionTime -= i;
			if (this.villagerConversionTime <= 0) {
				this.finishConversion((ServerLevel)this.level);
			}
		}

		super.tick();
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.getItem() == Items.GOLDEN_APPLE) {
			if (this.hasEffect(MobEffects.WEAKNESS)) {
				if (!player.abilities.instabuild) {
					itemStack.shrink(1);
				}

				if (!this.level.isClientSide) {
					this.startConverting(player.getUUID(), this.random.nextInt(2401) + 3600);
				}

				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.CONSUME;
			}
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Override
	protected boolean convertsInWater() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.isConverting() && this.villagerXp == 0;
	}

	public boolean isConverting() {
		return this.getEntityData().get(DATA_CONVERTING_ID);
	}

	private void startConverting(@Nullable UUID uUID, int i) {
		this.conversionStarter = uUID;
		this.villagerConversionTime = i;
		this.getEntityData().set(DATA_CONVERTING_ID, true);
		this.removeEffect(MobEffects.WEAKNESS);
		this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, i, Math.min(this.level.getDifficulty().getId() - 1, 0)));
		this.level.broadcastEntityEvent(this, (byte)16);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 16) {
			if (!this.isSilent()) {
				this.level
					.playLocalSound(
						this.getX(),
						this.getEyeY(),
						this.getZ(),
						SoundEvents.ZOMBIE_VILLAGER_CURE,
						this.getSoundSource(),
						1.0F + this.random.nextFloat(),
						this.random.nextFloat() * 0.7F + 0.3F,
						false
					);
			}
		} else {
			super.handleEntityEvent(b);
		}
	}

	private void finishConversion(ServerLevel serverLevel) {
		Villager villager = EntityType.VILLAGER.create(serverLevel);

		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			ItemStack itemStack = this.getItemBySlot(equipmentSlot);
			if (!itemStack.isEmpty()) {
				if (EnchantmentHelper.hasBindingCurse(itemStack)) {
					villager.setSlot(equipmentSlot.getIndex() + 300, itemStack);
				} else {
					double d = (double)this.getEquipmentDropChance(equipmentSlot);
					if (d > 1.0) {
						this.spawnAtLocation(itemStack);
					}
				}
			}
		}

		villager.copyPosition(this);
		villager.setVillagerData(this.getVillagerData());
		if (this.gossips != null) {
			villager.setGossips(this.gossips);
		}

		if (this.tradeOffers != null) {
			villager.setOffers(new MerchantOffers(this.tradeOffers));
		}

		villager.setVillagerXp(this.villagerXp);
		villager.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(villager.blockPosition()), MobSpawnType.CONVERSION, null, null);
		if (this.isBaby()) {
			villager.setAge(-24000);
		}

		this.remove();
		villager.setNoAi(this.isNoAi());
		if (this.hasCustomName()) {
			villager.setCustomName(this.getCustomName());
			villager.setCustomNameVisible(this.isCustomNameVisible());
		}

		if (this.isPersistenceRequired()) {
			villager.setPersistenceRequired();
		}

		villager.setInvulnerable(this.isInvulnerable());
		serverLevel.addFreshEntityWithPassengers(villager);
		if (this.conversionStarter != null) {
			Player player = serverLevel.getPlayerByUUID(this.conversionStarter);
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayer)player, this, villager);
				serverLevel.onReputationEvent(ReputationEventType.ZOMBIE_VILLAGER_CURED, player, villager);
			}
		}

		villager.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
		if (!this.isSilent()) {
			serverLevel.levelEvent(null, 1027, this.blockPosition(), 0);
		}
	}

	private int getConversionProgress() {
		int i = 1;
		if (this.random.nextFloat() < 0.01F) {
			int j = 0;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int k = (int)this.getX() - 4; k < (int)this.getX() + 4 && j < 14; k++) {
				for (int l = (int)this.getY() - 4; l < (int)this.getY() + 4 && j < 14; l++) {
					for (int m = (int)this.getZ() - 4; m < (int)this.getZ() + 4 && j < 14; m++) {
						Block block = this.level.getBlockState(mutableBlockPos.set(k, l, m)).getBlock();
						if (block == Blocks.IRON_BARS || block instanceof BedBlock) {
							if (this.random.nextFloat() < 0.3F) {
								i++;
							}

							j++;
						}
					}
				}
			}
		}

		return i;
	}

	@Override
	protected float getVoicePitch() {
		return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 2.0F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
	}

	@Override
	public SoundEvent getAmbientSound() {
		return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
	}

	@Override
	public SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ZOMBIE_VILLAGER_HURT;
	}

	@Override
	public SoundEvent getDeathSound() {
		return SoundEvents.ZOMBIE_VILLAGER_DEATH;
	}

	@Override
	public SoundEvent getStepSound() {
		return SoundEvents.ZOMBIE_VILLAGER_STEP;
	}

	@Override
	protected ItemStack getSkull() {
		return ItemStack.EMPTY;
	}

	public void setTradeOffers(CompoundTag compoundTag) {
		this.tradeOffers = compoundTag;
	}

	public void setGossips(Tag tag) {
		this.gossips = tag;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		this.setVillagerData(this.getVillagerData().setType(VillagerType.byBiome(serverLevelAccessor.getBiome(this.blockPosition()))));
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public void setVillagerData(VillagerData villagerData) {
		VillagerData villagerData2 = this.getVillagerData();
		if (villagerData2.getProfession() != villagerData.getProfession()) {
			this.tradeOffers = null;
		}

		this.entityData.set(DATA_VILLAGER_DATA, villagerData);
	}

	@Override
	public VillagerData getVillagerData() {
		return this.entityData.get(DATA_VILLAGER_DATA);
	}

	public void setVillagerXp(int i) {
		this.villagerXp = i;
	}
}
