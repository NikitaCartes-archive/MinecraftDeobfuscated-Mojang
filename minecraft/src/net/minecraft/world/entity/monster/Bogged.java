package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class Bogged extends AbstractSkeleton implements Shearable {
	private static final int HARD_ATTACK_INTERVAL = 50;
	private static final int NORMAL_ATTACK_INTERVAL = 70;
	private static final EntityDataAccessor<Boolean> DATA_SHEARED = SynchedEntityData.defineId(Bogged.class, EntityDataSerializers.BOOLEAN);
	public static final String SHEARED_TAG_NAME = "sheared";

	public static AttributeSupplier.Builder createAttributes() {
		return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 16.0);
	}

	public Bogged(EntityType<? extends Bogged> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_SHEARED, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("sheared", this.isSheared());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setSheared(compoundTag.getBoolean("sheared"));
	}

	public boolean isSheared() {
		return this.entityData.get(DATA_SHEARED);
	}

	public void setSheared(boolean bl) {
		this.entityData.set(DATA_SHEARED, bl);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.is(Items.SHEARS) && this.readyForShearing()) {
			this.shear(SoundSource.PLAYERS);
			this.gameEvent(GameEvent.SHEAR, player);
			if (!this.level().isClientSide) {
				itemStack.hurtAndBreak(1, player, getSlotForHand(interactionHand));
			}

			return InteractionResult.sidedSuccess(this.level().isClientSide);
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.BOGGED_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.BOGGED_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BOGGED_DEATH;
	}

	@Override
	protected SoundEvent getStepSound() {
		return SoundEvents.BOGGED_STEP;
	}

	@Override
	protected AbstractArrow getArrow(ItemStack itemStack, float f, @Nullable ItemStack itemStack2) {
		AbstractArrow abstractArrow = super.getArrow(itemStack, f, itemStack2);
		if (abstractArrow instanceof Arrow arrow) {
			arrow.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
		}

		return abstractArrow;
	}

	@Override
	protected int getHardAttackInterval() {
		return 50;
	}

	@Override
	protected int getAttackInterval() {
		return 70;
	}

	@Override
	public void shear(SoundSource soundSource) {
		this.level().playSound(null, this, SoundEvents.BOGGED_SHEAR, soundSource, 1.0F, 1.0F);
		this.spawnShearedMushrooms();
		this.setSheared(true);
	}

	private void spawnShearedMushrooms() {
		if (this.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
			LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.BOGGED_SHEAR);
			LootParams lootParams = new LootParams.Builder(serverLevel)
				.withParameter(LootContextParams.ORIGIN, this.position())
				.withParameter(LootContextParams.THIS_ENTITY, this)
				.create(LootContextParamSets.SHEARING);

			for (ItemStack itemStack : lootTable.getRandomItems(lootParams)) {
				this.spawnAtLocation(itemStack);
			}
		}
	}

	@Override
	public boolean readyForShearing() {
		return !this.isSheared() && this.isAlive();
	}
}
