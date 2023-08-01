package net.minecraft.world.entity.monster.piglin;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.joml.Vector3f;

public abstract class AbstractPiglin extends Monster {
	protected static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(
		AbstractPiglin.class, EntityDataSerializers.BOOLEAN
	);
	protected static final int CONVERSION_TIME = 300;
	protected static final float PIGLIN_EYE_HEIGHT = 1.79F;
	protected int timeInOverworld;

	public AbstractPiglin(EntityType<? extends AbstractPiglin> entityType, Level level) {
		super(entityType, level);
		this.setCanPickUpLoot(true);
		this.applyOpenDoorsAbility();
		this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
		this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
	}

	private void applyOpenDoorsAbility() {
		if (GoalUtils.hasGroundPathNavigation(this)) {
			((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
		}
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 1.79F;
	}

	@Override
	protected float ridingOffset(Entity entity) {
		return -0.7F;
	}

	@Override
	protected Vector3f getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
		return new Vector3f(0.0F, entityDimensions.height + 0.0625F * f, 0.0F);
	}

	protected abstract boolean canHunt();

	public void setImmuneToZombification(boolean bl) {
		this.getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, bl);
	}

	protected boolean isImmuneToZombification() {
		return this.getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.isImmuneToZombification()) {
			compoundTag.putBoolean("IsImmuneToZombification", true);
		}

		compoundTag.putInt("TimeInOverworld", this.timeInOverworld);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setImmuneToZombification(compoundTag.getBoolean("IsImmuneToZombification"));
		this.timeInOverworld = compoundTag.getInt("TimeInOverworld");
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (this.isConverting()) {
			this.timeInOverworld++;
		} else {
			this.timeInOverworld = 0;
		}

		if (this.timeInOverworld > 300) {
			this.playConvertedSound();
			this.finishConversion((ServerLevel)this.level());
		}
	}

	public boolean isConverting() {
		return !this.level().dimensionType().piglinSafe() && !this.isImmuneToZombification() && !this.isNoAi();
	}

	protected void finishConversion(ServerLevel serverLevel) {
		ZombifiedPiglin zombifiedPiglin = this.convertTo(EntityType.ZOMBIFIED_PIGLIN, true);
		if (zombifiedPiglin != null) {
			zombifiedPiglin.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
		}
	}

	public boolean isAdult() {
		return !this.isBaby();
	}

	public abstract PiglinArmPose getArmPose();

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return (LivingEntity)this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
	}

	protected boolean isHoldingMeleeWeapon() {
		return this.getMainHandItem().getItem() instanceof TieredItem;
	}

	@Override
	public void playAmbientSound() {
		if (PiglinAi.isIdle(this)) {
			super.playAmbientSound();
		}
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	protected abstract void playConvertedSound();
}
