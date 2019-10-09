package net.minecraft.world.entity.projectile;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class Arrow extends AbstractArrow {
	private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
	private Potion potion = Potions.EMPTY;
	private final Set<MobEffectInstance> effects = Sets.<MobEffectInstance>newHashSet();
	private boolean fixedColor;

	public Arrow(EntityType<? extends Arrow> entityType, Level level) {
		super(entityType, level);
	}

	public Arrow(Level level, double d, double e, double f) {
		super(EntityType.ARROW, d, e, f, level);
	}

	public Arrow(Level level, LivingEntity livingEntity) {
		super(EntityType.ARROW, livingEntity, level);
	}

	public void setEffectsFromItem(ItemStack itemStack) {
		if (itemStack.getItem() == Items.TIPPED_ARROW) {
			this.potion = PotionUtils.getPotion(itemStack);
			Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(itemStack);
			if (!collection.isEmpty()) {
				for (MobEffectInstance mobEffectInstance : collection) {
					this.effects.add(new MobEffectInstance(mobEffectInstance));
				}
			}

			int i = getCustomColor(itemStack);
			if (i == -1) {
				this.updateColor();
			} else {
				this.setFixedColor(i);
			}
		} else if (itemStack.getItem() == Items.ARROW) {
			this.potion = Potions.EMPTY;
			this.effects.clear();
			this.entityData.set(ID_EFFECT_COLOR, -1);
		}
	}

	public static int getCustomColor(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null && compoundTag.contains("CustomPotionColor", 99) ? compoundTag.getInt("CustomPotionColor") : -1;
	}

	private void updateColor() {
		this.fixedColor = false;
		if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
			this.entityData.set(ID_EFFECT_COLOR, -1);
		} else {
			this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
		}
	}

	public void addEffect(MobEffectInstance mobEffectInstance) {
		this.effects.add(mobEffectInstance);
		this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ID_EFFECT_COLOR, -1);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level.isClientSide) {
			if (this.inGround) {
				if (this.inGroundTime % 5 == 0) {
					this.makeParticle(1);
				}
			} else {
				this.makeParticle(2);
			}
		} else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
			this.level.broadcastEntityEvent(this, (byte)0);
			this.potion = Potions.EMPTY;
			this.effects.clear();
			this.entityData.set(ID_EFFECT_COLOR, -1);
		}
	}

	private void makeParticle(int i) {
		int j = this.getColor();
		if (j != -1 && i > 0) {
			double d = (double)(j >> 16 & 0xFF) / 255.0;
			double e = (double)(j >> 8 & 0xFF) / 255.0;
			double f = (double)(j >> 0 & 0xFF) / 255.0;

			for (int k = 0; k < i; k++) {
				this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), d, e, f);
			}
		}
	}

	public int getColor() {
		return this.entityData.get(ID_EFFECT_COLOR);
	}

	private void setFixedColor(int i) {
		this.fixedColor = true;
		this.entityData.set(ID_EFFECT_COLOR, i);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.potion != Potions.EMPTY && this.potion != null) {
			compoundTag.putString("Potion", Registry.POTION.getKey(this.potion).toString());
		}

		if (this.fixedColor) {
			compoundTag.putInt("Color", this.getColor());
		}

		if (!this.effects.isEmpty()) {
			ListTag listTag = new ListTag();

			for (MobEffectInstance mobEffectInstance : this.effects) {
				listTag.add(mobEffectInstance.save(new CompoundTag()));
			}

			compoundTag.put("CustomPotionEffects", listTag);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("Potion", 8)) {
			this.potion = PotionUtils.getPotion(compoundTag);
		}

		for (MobEffectInstance mobEffectInstance : PotionUtils.getCustomEffects(compoundTag)) {
			this.addEffect(mobEffectInstance);
		}

		if (compoundTag.contains("Color", 99)) {
			this.setFixedColor(compoundTag.getInt("Color"));
		} else {
			this.updateColor();
		}
	}

	@Override
	protected void doPostHurtEffects(LivingEntity livingEntity) {
		super.doPostHurtEffects(livingEntity);

		for (MobEffectInstance mobEffectInstance : this.potion.getEffects()) {
			livingEntity.addEffect(
				new MobEffectInstance(
					mobEffectInstance.getEffect(),
					Math.max(mobEffectInstance.getDuration() / 8, 1),
					mobEffectInstance.getAmplifier(),
					mobEffectInstance.isAmbient(),
					mobEffectInstance.isVisible()
				)
			);
		}

		if (!this.effects.isEmpty()) {
			for (MobEffectInstance mobEffectInstance : this.effects) {
				livingEntity.addEffect(mobEffectInstance);
			}
		}
	}

	@Override
	protected ItemStack getPickupItem() {
		if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
			return new ItemStack(Items.ARROW);
		} else {
			ItemStack itemStack = new ItemStack(Items.TIPPED_ARROW);
			PotionUtils.setPotion(itemStack, this.potion);
			PotionUtils.setCustomEffects(itemStack, this.effects);
			if (this.fixedColor) {
				itemStack.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
			}

			return itemStack;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 0) {
			int i = this.getColor();
			if (i != -1) {
				double d = (double)(i >> 16 & 0xFF) / 255.0;
				double e = (double)(i >> 8 & 0xFF) / 255.0;
				double f = (double)(i >> 0 & 0xFF) / 255.0;

				for (int j = 0; j < 20; j++) {
					this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), d, e, f);
				}
			}
		} else {
			super.handleEntityEvent(b);
		}
	}
}
