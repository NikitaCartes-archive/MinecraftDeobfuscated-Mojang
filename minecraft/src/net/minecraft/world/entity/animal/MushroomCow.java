package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class MushroomCow extends Cow implements Shearable, VariantHolder<MushroomCow.Variant> {
	private static final EntityDataAccessor<String> DATA_TYPE = SynchedEntityData.defineId(MushroomCow.class, EntityDataSerializers.STRING);
	private static final int MUTATE_CHANCE = 1024;
	private static final String TAG_STEW_EFFECTS = "stew_effects";
	@Nullable
	private SuspiciousStewEffects stewEffects;
	@Nullable
	private UUID lastLightningBoltUUID;

	public MushroomCow(EntityType<? extends MushroomCow> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return levelReader.getBlockState(blockPos.below()).is(Blocks.MYCELIUM) ? 10.0F : levelReader.getPathfindingCostFromLightLevels(blockPos);
	}

	public static boolean checkMushroomSpawnRules(
		EntityType<MushroomCow> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource
	) {
		return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && isBrightEnoughToSpawn(levelAccessor, blockPos);
	}

	@Override
	public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
		UUID uUID = lightningBolt.getUUID();
		if (!uUID.equals(this.lastLightningBoltUUID)) {
			this.setVariant(this.getVariant() == MushroomCow.Variant.RED ? MushroomCow.Variant.BROWN : MushroomCow.Variant.RED);
			this.lastLightningBoltUUID = uUID;
			this.playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0F, 1.0F);
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_TYPE, MushroomCow.Variant.RED.type);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.is(Items.BOWL) && !this.isBaby()) {
			boolean bl = false;
			ItemStack itemStack2;
			if (this.stewEffects != null) {
				bl = true;
				itemStack2 = new ItemStack(Items.SUSPICIOUS_STEW);
				itemStack2.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, this.stewEffects);
				this.stewEffects = null;
			} else {
				itemStack2 = new ItemStack(Items.MUSHROOM_STEW);
			}

			ItemStack itemStack3 = ItemUtils.createFilledResult(itemStack, player, itemStack2, false);
			player.setItemInHand(interactionHand, itemStack3);
			SoundEvent soundEvent;
			if (bl) {
				soundEvent = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
			} else {
				soundEvent = SoundEvents.MOOSHROOM_MILK;
			}

			this.playSound(soundEvent, 1.0F, 1.0F);
			return InteractionResult.SUCCESS;
		} else if (itemStack.is(Items.SHEARS) && this.readyForShearing()) {
			this.shear(SoundSource.PLAYERS);
			this.gameEvent(GameEvent.SHEAR, player);
			if (!this.level().isClientSide) {
				itemStack.hurtAndBreak(1, player, getSlotForHand(interactionHand));
			}

			return InteractionResult.SUCCESS;
		} else if (this.getVariant() == MushroomCow.Variant.BROWN && itemStack.is(ItemTags.SMALL_FLOWERS)) {
			if (this.stewEffects != null) {
				for (int i = 0; i < 2; i++) {
					this.level()
						.addParticle(
							ParticleTypes.SMOKE,
							this.getX() + this.random.nextDouble() / 2.0,
							this.getY(0.5),
							this.getZ() + this.random.nextDouble() / 2.0,
							0.0,
							this.random.nextDouble() / 5.0,
							0.0
						);
				}
			} else {
				Optional<SuspiciousStewEffects> optional = this.getEffectsFromItemStack(itemStack);
				if (optional.isEmpty()) {
					return InteractionResult.PASS;
				}

				itemStack.consume(1, player);

				for (int j = 0; j < 4; j++) {
					this.level()
						.addParticle(
							ParticleTypes.EFFECT,
							this.getX() + this.random.nextDouble() / 2.0,
							this.getY(0.5),
							this.getZ() + this.random.nextDouble() / 2.0,
							0.0,
							this.random.nextDouble() / 5.0,
							0.0
						);
				}

				this.stewEffects = (SuspiciousStewEffects)optional.get();
				this.playSound(SoundEvents.MOOSHROOM_EAT, 2.0F, 1.0F);
			}

			return InteractionResult.SUCCESS;
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Override
	public void shear(SoundSource soundSource) {
		this.level().playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, soundSource, 1.0F, 1.0F);
		if (!this.level().isClientSide()) {
			Cow cow = EntityType.COW.create(this.level(), EntitySpawnReason.CONVERSION);
			if (cow != null) {
				((ServerLevel)this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
				this.discard();
				cow.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
				cow.setHealth(this.getHealth());
				cow.yBodyRot = this.yBodyRot;
				if (this.hasCustomName()) {
					cow.setCustomName(this.getCustomName());
					cow.setCustomNameVisible(this.isCustomNameVisible());
				}

				if (this.isPersistenceRequired()) {
					cow.setPersistenceRequired();
				}

				cow.setInvulnerable(this.isInvulnerable());
				this.level().addFreshEntity(cow);
				this.dropFromShearingLootTable(this.getVariant().lootTable(), itemStack -> {
					for (int i = 0; i < itemStack.getCount(); i++) {
						this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(1.0), this.getZ(), itemStack.copyWithCount(1)));
					}
				});
			}
		}
	}

	@Override
	public boolean readyForShearing() {
		return this.isAlive() && !this.isBaby();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putString("Type", this.getVariant().getSerializedName());
		if (this.stewEffects != null) {
			SuspiciousStewEffects.CODEC.encodeStart(NbtOps.INSTANCE, this.stewEffects).ifSuccess(tag -> compoundTag.put("stew_effects", tag));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setVariant(MushroomCow.Variant.byName(compoundTag.getString("Type")));
		if (compoundTag.contains("stew_effects", 9)) {
			SuspiciousStewEffects.CODEC
				.parse(NbtOps.INSTANCE, compoundTag.get("stew_effects"))
				.ifSuccess(suspiciousStewEffects -> this.stewEffects = suspiciousStewEffects);
		}
	}

	private Optional<SuspiciousStewEffects> getEffectsFromItemStack(ItemStack itemStack) {
		SuspiciousEffectHolder suspiciousEffectHolder = SuspiciousEffectHolder.tryGet(itemStack.getItem());
		return suspiciousEffectHolder != null ? Optional.of(suspiciousEffectHolder.getSuspiciousEffects()) : Optional.empty();
	}

	public void setVariant(MushroomCow.Variant variant) {
		this.entityData.set(DATA_TYPE, variant.type);
	}

	public MushroomCow.Variant getVariant() {
		return MushroomCow.Variant.byName(this.entityData.get(DATA_TYPE));
	}

	@Nullable
	public MushroomCow getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		MushroomCow mushroomCow = EntityType.MOOSHROOM.create(serverLevel, EntitySpawnReason.BREEDING);
		if (mushroomCow != null) {
			mushroomCow.setVariant(this.getOffspringVariant((MushroomCow)ageableMob));
		}

		return mushroomCow;
	}

	private MushroomCow.Variant getOffspringVariant(MushroomCow mushroomCow) {
		MushroomCow.Variant variant = this.getVariant();
		MushroomCow.Variant variant2 = mushroomCow.getVariant();
		MushroomCow.Variant variant3;
		if (variant == variant2 && this.random.nextInt(1024) == 0) {
			variant3 = variant == MushroomCow.Variant.BROWN ? MushroomCow.Variant.RED : MushroomCow.Variant.BROWN;
		} else {
			variant3 = this.random.nextBoolean() ? variant : variant2;
		}

		return variant3;
	}

	public static enum Variant implements StringRepresentable {
		RED("red", Blocks.RED_MUSHROOM.defaultBlockState(), BuiltInLootTables.SHEAR_RED_MOOSHROOM),
		BROWN("brown", Blocks.BROWN_MUSHROOM.defaultBlockState(), BuiltInLootTables.SHEAR_BROWN_MOOSHROOM);

		public static final StringRepresentable.EnumCodec<MushroomCow.Variant> CODEC = StringRepresentable.fromEnum(MushroomCow.Variant::values);
		final String type;
		private final BlockState blockState;
		private final ResourceKey<LootTable> lootTable;

		private Variant(final String string2, final BlockState blockState, final ResourceKey<LootTable> resourceKey) {
			this.type = string2;
			this.blockState = blockState;
			this.lootTable = resourceKey;
		}

		public BlockState getBlockState() {
			return this.blockState;
		}

		@Override
		public String getSerializedName() {
			return this.type;
		}

		public ResourceKey<LootTable> lootTable() {
			return this.lootTable;
		}

		static MushroomCow.Variant byName(String string) {
			return (MushroomCow.Variant)CODEC.byName(string, RED);
		}
	}
}
