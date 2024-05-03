package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CrossbowItem extends ProjectileWeaponItem {
	private static final float MAX_CHARGE_DURATION = 1.25F;
	public static final int DEFAULT_RANGE = 8;
	private boolean startSoundPlayed = false;
	private boolean midLoadSoundPlayed = false;
	private static final float START_SOUND_PERCENT = 0.2F;
	private static final float MID_SOUND_PERCENT = 0.5F;
	private static final float ARROW_POWER = 3.15F;
	private static final float FIREWORK_POWER = 1.6F;
	public static final float MOB_ARROW_POWER = 1.6F;
	private static final CrossbowItem.ChargingSounds DEFAULT_SOUNDS = new CrossbowItem.ChargingSounds(
		Optional.of(SoundEvents.CROSSBOW_LOADING_START), Optional.of(SoundEvents.CROSSBOW_LOADING_MIDDLE), Optional.of(SoundEvents.CROSSBOW_LOADING_END)
	);

	public CrossbowItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public Predicate<ItemStack> getSupportedHeldProjectiles() {
		return ARROW_OR_FIREWORK;
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return ARROW_ONLY;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		ChargedProjectiles chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
		if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
			this.performShooting(level, player, interactionHand, itemStack, getShootingPower(chargedProjectiles), 1.0F, null);
			return InteractionResultHolder.consume(itemStack);
		} else if (!player.getProjectile(itemStack).isEmpty()) {
			this.startSoundPlayed = false;
			this.midLoadSoundPlayed = false;
			player.startUsingItem(interactionHand);
			return InteractionResultHolder.consume(itemStack);
		} else {
			return InteractionResultHolder.fail(itemStack);
		}
	}

	private static float getShootingPower(ChargedProjectiles chargedProjectiles) {
		return chargedProjectiles.contains(Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
	}

	@Override
	public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
		int j = this.getUseDuration(itemStack, livingEntity) - i;
		float f = getPowerForTime(j, itemStack, livingEntity);
		if (f >= 1.0F && !isCharged(itemStack) && tryLoadProjectiles(livingEntity, itemStack)) {
			CrossbowItem.ChargingSounds chargingSounds = this.getChargingSounds(itemStack);
			chargingSounds.end()
				.ifPresent(
					holder -> level.playSound(
							null,
							livingEntity.getX(),
							livingEntity.getY(),
							livingEntity.getZ(),
							(SoundEvent)holder.value(),
							livingEntity.getSoundSource(),
							1.0F,
							1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F
						)
				);
		}
	}

	private static boolean tryLoadProjectiles(LivingEntity livingEntity, ItemStack itemStack) {
		List<ItemStack> list = draw(itemStack, livingEntity.getProjectile(itemStack), livingEntity);
		if (!list.isEmpty()) {
			itemStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(list));
			return true;
		} else {
			return false;
		}
	}

	public static boolean isCharged(ItemStack itemStack) {
		ChargedProjectiles chargedProjectiles = itemStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
		return !chargedProjectiles.isEmpty();
	}

	@Override
	protected void shootProjectile(LivingEntity livingEntity, Projectile projectile, int i, float f, float g, float h, @Nullable LivingEntity livingEntity2) {
		Vector3f vector3f;
		if (livingEntity2 != null) {
			double d = livingEntity2.getX() - livingEntity.getX();
			double e = livingEntity2.getZ() - livingEntity.getZ();
			double j = Math.sqrt(d * d + e * e);
			double k = livingEntity2.getY(0.3333333333333333) - projectile.getY() + j * 0.2F;
			vector3f = getProjectileShotVector(livingEntity, new Vec3(d, k, e), h);
		} else {
			Vec3 vec3 = livingEntity.getUpVector(1.0F);
			Quaternionf quaternionf = new Quaternionf().setAngleAxis((double)(h * (float) (Math.PI / 180.0)), vec3.x, vec3.y, vec3.z);
			Vec3 vec32 = livingEntity.getViewVector(1.0F);
			vector3f = vec32.toVector3f().rotate(quaternionf);
		}

		projectile.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), f, g);
		float l = getShotPitch(livingEntity.getRandom(), i);
		livingEntity.level()
			.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.CROSSBOW_SHOOT, livingEntity.getSoundSource(), 1.0F, l);
	}

	private static Vector3f getProjectileShotVector(LivingEntity livingEntity, Vec3 vec3, float f) {
		Vector3f vector3f = vec3.toVector3f().normalize();
		Vector3f vector3f2 = new Vector3f(vector3f).cross(new Vector3f(0.0F, 1.0F, 0.0F));
		if ((double)vector3f2.lengthSquared() <= 1.0E-7) {
			Vec3 vec32 = livingEntity.getUpVector(1.0F);
			vector3f2 = new Vector3f(vector3f).cross(vec32.toVector3f());
		}

		Vector3f vector3f3 = new Vector3f(vector3f).rotateAxis((float) (Math.PI / 2), vector3f2.x, vector3f2.y, vector3f2.z);
		return new Vector3f(vector3f).rotateAxis(f * (float) (Math.PI / 180.0), vector3f3.x, vector3f3.y, vector3f3.z);
	}

	@Override
	protected Projectile createProjectile(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl) {
		if (itemStack2.is(Items.FIREWORK_ROCKET)) {
			return new FireworkRocketEntity(level, itemStack2, livingEntity, livingEntity.getX(), livingEntity.getEyeY() - 0.15F, livingEntity.getZ(), true);
		} else {
			Projectile projectile = super.createProjectile(level, livingEntity, itemStack, itemStack2, bl);
			if (projectile instanceof AbstractArrow abstractArrow) {
				abstractArrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
			}

			return projectile;
		}
	}

	@Override
	protected int getDurabilityUse(ItemStack itemStack) {
		return itemStack.is(Items.FIREWORK_ROCKET) ? 3 : 1;
	}

	public void performShooting(
		Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, float f, float g, @Nullable LivingEntity livingEntity2
	) {
		if (level instanceof ServerLevel serverLevel) {
			ChargedProjectiles chargedProjectiles = itemStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
			if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
				this.shoot(serverLevel, livingEntity, interactionHand, itemStack, chargedProjectiles.getItems(), f, g, livingEntity instanceof Player, livingEntity2);
				if (livingEntity instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, itemStack);
					serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
				}
			}
		}
	}

	private static float getShotPitch(RandomSource randomSource, int i) {
		return i == 0 ? 1.0F : getRandomShotPitch((i & 1) == 1, randomSource);
	}

	private static float getRandomShotPitch(boolean bl, RandomSource randomSource) {
		float f = bl ? 0.63F : 0.43F;
		return 1.0F / (randomSource.nextFloat() * 0.5F + 1.8F) + f;
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
		if (!level.isClientSide) {
			CrossbowItem.ChargingSounds chargingSounds = this.getChargingSounds(itemStack);
			float f = (float)(itemStack.getUseDuration(livingEntity) - i) / (float)getChargeDuration(itemStack, livingEntity);
			if (f < 0.2F) {
				this.startSoundPlayed = false;
				this.midLoadSoundPlayed = false;
			}

			if (f >= 0.2F && !this.startSoundPlayed) {
				this.startSoundPlayed = true;
				chargingSounds.start()
					.ifPresent(
						holder -> level.playSound(
								null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), (SoundEvent)holder.value(), SoundSource.PLAYERS, 0.5F, 1.0F
							)
					);
			}

			if (f >= 0.5F && !this.midLoadSoundPlayed) {
				this.midLoadSoundPlayed = true;
				chargingSounds.mid()
					.ifPresent(
						holder -> level.playSound(
								null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), (SoundEvent)holder.value(), SoundSource.PLAYERS, 0.5F, 1.0F
							)
					);
			}
		}
	}

	@Override
	public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
		return getChargeDuration(itemStack, livingEntity) + 3;
	}

	public static int getChargeDuration(ItemStack itemStack, LivingEntity livingEntity) {
		float f;
		if (livingEntity.level() instanceof ServerLevel serverLevel) {
			f = EnchantmentHelper.modifyCrossbowChargingTime(serverLevel, itemStack, livingEntity, 1.25F);
		} else {
			f = 1.25F;
		}

		return Mth.floor(f * 20.0F);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.CROSSBOW;
	}

	CrossbowItem.ChargingSounds getChargingSounds(ItemStack itemStack) {
		return (CrossbowItem.ChargingSounds)EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.CROSSBOW_CHARGING_SOUNDS)
			.orElse(DEFAULT_SOUNDS);
	}

	private static float getPowerForTime(int i, ItemStack itemStack, LivingEntity livingEntity) {
		float f = (float)i / (float)getChargeDuration(itemStack, livingEntity);
		if (f > 1.0F) {
			f = 1.0F;
		}

		return f;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		ChargedProjectiles chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
		if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
			ItemStack itemStack2 = (ItemStack)chargedProjectiles.getItems().get(0);
			list.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(itemStack2.getDisplayName()));
			if (tooltipFlag.isAdvanced() && itemStack2.is(Items.FIREWORK_ROCKET)) {
				List<Component> list2 = Lists.<Component>newArrayList();
				Items.FIREWORK_ROCKET.appendHoverText(itemStack2, tooltipContext, list2, tooltipFlag);
				if (!list2.isEmpty()) {
					for (int i = 0; i < list2.size(); i++) {
						list2.set(i, Component.literal("  ").append((Component)list2.get(i)).withStyle(ChatFormatting.GRAY));
					}

					list.addAll(list2);
				}
			}
		}
	}

	@Override
	public boolean useOnRelease(ItemStack itemStack) {
		return itemStack.is(this);
	}

	@Override
	public int getDefaultProjectileRange() {
		return 8;
	}

	public static record ChargingSounds(Optional<Holder<SoundEvent>> start, Optional<Holder<SoundEvent>> mid, Optional<Holder<SoundEvent>> end) {
		public static final Codec<CrossbowItem.ChargingSounds> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						SoundEvent.CODEC.optionalFieldOf("start").forGetter(CrossbowItem.ChargingSounds::start),
						SoundEvent.CODEC.optionalFieldOf("mid").forGetter(CrossbowItem.ChargingSounds::mid),
						SoundEvent.CODEC.optionalFieldOf("end").forGetter(CrossbowItem.ChargingSounds::end)
					)
					.apply(instance, CrossbowItem.ChargingSounds::new)
		);
	}
}
