package net.minecraft.world.entity.projectile;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

public class Arrow extends AbstractArrow {
	private static final int EXPOSED_POTION_DECAY_TIME = 600;
	private static final int NO_EFFECT_COLOR = -1;
	private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
	private static final byte EVENT_POTION_PUFF = 0;

	public Arrow(EntityType<? extends Arrow> entityType, Level level) {
		super(entityType, level);
	}

	public Arrow(Level level, double d, double e, double f, ItemStack itemStack) {
		super(EntityType.ARROW, d, e, f, level, itemStack);
		this.updateColor();
	}

	public Arrow(Level level, LivingEntity livingEntity, ItemStack itemStack) {
		super(EntityType.ARROW, livingEntity, level, itemStack);
		this.updateColor();
	}

	private PotionContents getPotionContents() {
		return this.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
	}

	private void setPotionContents(PotionContents potionContents) {
		this.getPickupItemStackOrigin().set(DataComponents.POTION_CONTENTS, potionContents);
		this.updateColor();
	}

	@Override
	protected void setPickupItemStack(ItemStack itemStack) {
		super.setPickupItemStack(itemStack);
		this.updateColor();
	}

	private void updateColor() {
		PotionContents potionContents = this.getPotionContents();
		this.entityData.set(ID_EFFECT_COLOR, potionContents.equals(PotionContents.EMPTY) ? -1 : potionContents.getColorForArrow());
	}

	public void addEffect(MobEffectInstance mobEffectInstance) {
		this.setPotionContents(this.getPotionContents().withEffectAdded(mobEffectInstance));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ID_EFFECT_COLOR, -1);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) {
			if (this.inGround) {
				if (this.inGroundTime % 5 == 0) {
					this.makeParticle(1);
				}
			} else {
				this.makeParticle(2);
			}
		} else if (this.inGround && this.inGroundTime != 0 && !this.getPotionContents().equals(PotionContents.EMPTY) && this.inGroundTime >= 600) {
			this.level().broadcastEntityEvent(this, (byte)0);
			this.setPickupItemStack(new ItemStack(Items.ARROW));
		}
	}

	private void makeParticle(int i) {
		int j = this.getColor();
		if (j != -1 && i > 0) {
			double d = (double)(j >> 16 & 0xFF) / 255.0;
			double e = (double)(j >> 8 & 0xFF) / 255.0;
			double f = (double)(j >> 0 & 0xFF) / 255.0;

			for (int k = 0; k < i; k++) {
				this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), d, e, f);
			}
		}
	}

	public int getColor() {
		return this.entityData.get(ID_EFFECT_COLOR);
	}

	@Override
	protected void doPostHurtEffects(LivingEntity livingEntity) {
		super.doPostHurtEffects(livingEntity);
		Entity entity = this.getEffectSource();
		PotionContents potionContents = this.getPotionContents();
		if (potionContents.potion().isPresent()) {
			for (MobEffectInstance mobEffectInstance : ((Potion)((Holder)potionContents.potion().get()).value()).getEffects()) {
				livingEntity.addEffect(
					new MobEffectInstance(
						mobEffectInstance.getEffect(),
						Math.max(mobEffectInstance.mapDuration(i -> i / 8), 1),
						mobEffectInstance.getAmplifier(),
						mobEffectInstance.isAmbient(),
						mobEffectInstance.isVisible()
					),
					entity
				);
			}
		}

		for (MobEffectInstance mobEffectInstance : potionContents.customEffects()) {
			livingEntity.addEffect(mobEffectInstance, entity);
		}
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 0) {
			int i = this.getColor();
			if (i != -1) {
				double d = (double)(i >> 16 & 0xFF) / 255.0;
				double e = (double)(i >> 8 & 0xFF) / 255.0;
				double f = (double)(i >> 0 & 0xFF) / 255.0;

				for (int j = 0; j < 20; j++) {
					this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), d, e, f);
				}
			}
		} else {
			super.handleEntityEvent(b);
		}
	}
}
