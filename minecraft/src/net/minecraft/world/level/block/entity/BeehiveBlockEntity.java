package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class BeehiveBlockEntity extends BlockEntity {
	public static final String TAG_FLOWER_POS = "flower_pos";
	public static final String MIN_OCCUPATION_TICKS = "MinOccupationTicks";
	public static final String ENTITY_DATA = "EntityData";
	public static final String TICKS_IN_HIVE = "TicksInHive";
	public static final String HAS_NECTAR = "HasNectar";
	public static final String BEES = "Bees";
	private static final List<String> IGNORED_BEE_TAGS = Arrays.asList(
		"Air",
		"ArmorDropChances",
		"ArmorItems",
		"Brain",
		"CanPickUpLoot",
		"DeathTime",
		"FallDistance",
		"FallFlying",
		"Fire",
		"HandDropChances",
		"HandItems",
		"HurtByTimestamp",
		"HurtTime",
		"LeftHanded",
		"Motion",
		"NoGravity",
		"OnGround",
		"PortalCooldown",
		"Pos",
		"Rotation",
		"CannotEnterHiveTicks",
		"TicksSincePollination",
		"CropsGrownSincePollination",
		"hive_pos",
		"Passengers",
		"leash",
		"UUID"
	);
	public static final int MAX_OCCUPANTS = 3;
	private static final int MIN_TICKS_BEFORE_REENTERING_HIVE = 400;
	private static final int MIN_OCCUPATION_TICKS_NECTAR = 2400;
	public static final int MIN_OCCUPATION_TICKS_NECTARLESS = 600;
	private final List<BeehiveBlockEntity.BeeData> stored = Lists.<BeehiveBlockEntity.BeeData>newArrayList();
	@Nullable
	private BlockPos savedFlowerPos;

	public BeehiveBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.BEEHIVE, blockPos, blockState);
	}

	@Override
	public void setChanged() {
		if (this.isFireNearby()) {
			this.emptyAllLivingFromHive(null, this.level.getBlockState(this.getBlockPos()), BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
		}

		super.setChanged();
	}

	public boolean isFireNearby() {
		if (this.level == null) {
			return false;
		} else {
			for (BlockPos blockPos : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1))) {
				if (this.level.getBlockState(blockPos).getBlock() instanceof FireBlock) {
					return true;
				}
			}

			return false;
		}
	}

	public boolean isEmpty() {
		return this.stored.isEmpty();
	}

	public boolean isFull() {
		return this.stored.size() == 3;
	}

	public void emptyAllLivingFromHive(@Nullable Player player, BlockState blockState, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus) {
		List<Entity> list = this.releaseAllOccupants(blockState, beeReleaseStatus);
		if (player != null) {
			for (Entity entity : list) {
				if (entity instanceof Bee) {
					Bee bee = (Bee)entity;
					if (player.position().distanceToSqr(entity.position()) <= 16.0) {
						if (!this.isSedated()) {
							bee.setTarget(player);
						} else {
							bee.setStayOutOfHiveCountdown(400);
						}
					}
				}
			}
		}
	}

	private List<Entity> releaseAllOccupants(BlockState blockState, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus) {
		List<Entity> list = Lists.<Entity>newArrayList();
		this.stored.removeIf(beeData -> releaseOccupant(this.level, this.worldPosition, blockState, beeData, list, beeReleaseStatus, this.savedFlowerPos));
		if (!list.isEmpty()) {
			super.setChanged();
		}

		return list;
	}

	public void addOccupant(Entity entity, boolean bl) {
		this.addOccupantWithPresetTicks(entity, bl, 0);
	}

	@VisibleForDebug
	public int getOccupantCount() {
		return this.stored.size();
	}

	public static int getHoneyLevel(BlockState blockState) {
		return (Integer)blockState.getValue(BeehiveBlock.HONEY_LEVEL);
	}

	@VisibleForDebug
	public boolean isSedated() {
		return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
	}

	public void addOccupantWithPresetTicks(Entity entity, boolean bl, int i) {
		if (this.stored.size() < 3) {
			entity.stopRiding();
			entity.ejectPassengers();
			CompoundTag compoundTag = new CompoundTag();
			entity.save(compoundTag);
			this.storeBee(compoundTag, i, bl);
			if (this.level != null) {
				if (entity instanceof Bee bee && bee.hasSavedFlowerPos() && (!this.hasSavedFlowerPos() || this.level.random.nextBoolean())) {
					this.savedFlowerPos = bee.getSavedFlowerPos();
				}

				BlockPos blockPos = this.getBlockPos();
				this.level
					.playSound(null, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
				this.level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, this.getBlockState()));
			}

			entity.discard();
			super.setChanged();
		}
	}

	public void storeBee(CompoundTag compoundTag, int i, boolean bl) {
		this.stored.add(new BeehiveBlockEntity.BeeData(compoundTag, i, bl ? 2400 : 600));
	}

	private static boolean releaseOccupant(
		Level level,
		BlockPos blockPos,
		BlockState blockState,
		BeehiveBlockEntity.BeeData beeData,
		@Nullable List<Entity> list,
		BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus,
		@Nullable BlockPos blockPos2
	) {
		if ((level.isNight() || level.isRaining()) && beeReleaseStatus != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
			return false;
		} else {
			CompoundTag compoundTag = beeData.entityData.copy();
			removeIgnoredBeeTags(compoundTag);
			compoundTag.put("hive_pos", NbtUtils.writeBlockPos(blockPos));
			compoundTag.putBoolean("NoGravity", true);
			Direction direction = blockState.getValue(BeehiveBlock.FACING);
			BlockPos blockPos3 = blockPos.relative(direction);
			boolean bl = !level.getBlockState(blockPos3).getCollisionShape(level, blockPos3).isEmpty();
			if (bl && beeReleaseStatus != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
				return false;
			} else {
				Entity entity = EntityType.loadEntityRecursive(compoundTag, level, entityx -> entityx);
				if (entity != null) {
					if (!entity.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
						return false;
					} else {
						if (entity instanceof Bee bee) {
							if (blockPos2 != null && !bee.hasSavedFlowerPos() && level.random.nextFloat() < 0.9F) {
								bee.setSavedFlowerPos(blockPos2);
							}

							if (beeReleaseStatus == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
								bee.dropOffNectar();
								if (blockState.is(BlockTags.BEEHIVES, blockStateBase -> blockStateBase.hasProperty(BeehiveBlock.HONEY_LEVEL))) {
									int i = getHoneyLevel(blockState);
									if (i < 5) {
										int j = level.random.nextInt(100) == 0 ? 2 : 1;
										if (i + j > 5) {
											j--;
										}

										level.setBlockAndUpdate(blockPos, blockState.setValue(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(i + j)));
									}
								}
							}

							setBeeReleaseData(beeData.ticksInHive, bee);
							if (list != null) {
								list.add(bee);
							}

							float f = entity.getBbWidth();
							double d = bl ? 0.0 : 0.55 + (double)(f / 2.0F);
							double e = (double)blockPos.getX() + 0.5 + d * (double)direction.getStepX();
							double g = (double)blockPos.getY() + 0.5 - (double)(entity.getBbHeight() / 2.0F);
							double h = (double)blockPos.getZ() + 0.5 + d * (double)direction.getStepZ();
							entity.moveTo(e, g, h, entity.getYRot(), entity.getXRot());
						}

						level.playSound(null, blockPos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
						level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, level.getBlockState(blockPos)));
						return level.addFreshEntity(entity);
					}
				} else {
					return false;
				}
			}
		}
	}

	static void removeIgnoredBeeTags(CompoundTag compoundTag) {
		for (String string : IGNORED_BEE_TAGS) {
			compoundTag.remove(string);
		}
	}

	private static void setBeeReleaseData(int i, Bee bee) {
		int j = bee.getAge();
		if (j < 0) {
			bee.setAge(Math.min(0, j + i));
		} else if (j > 0) {
			bee.setAge(Math.max(0, j - i));
		}

		bee.setInLoveTime(Math.max(0, bee.getInLoveTime() - i));
	}

	private boolean hasSavedFlowerPos() {
		return this.savedFlowerPos != null;
	}

	private static void tickOccupants(Level level, BlockPos blockPos, BlockState blockState, List<BeehiveBlockEntity.BeeData> list, @Nullable BlockPos blockPos2) {
		boolean bl = false;
		Iterator<BeehiveBlockEntity.BeeData> iterator = list.iterator();

		while (iterator.hasNext()) {
			BeehiveBlockEntity.BeeData beeData = (BeehiveBlockEntity.BeeData)iterator.next();
			if (beeData.ticksInHive > beeData.minOccupationTicks) {
				BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus = beeData.entityData.getBoolean("HasNectar")
					? BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED
					: BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED;
				if (releaseOccupant(level, blockPos, blockState, beeData, null, beeReleaseStatus, blockPos2)) {
					bl = true;
					iterator.remove();
				}
			}

			beeData.ticksInHive++;
		}

		if (bl) {
			setChanged(level, blockPos, blockState);
		}
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BeehiveBlockEntity beehiveBlockEntity) {
		tickOccupants(level, blockPos, blockState, beehiveBlockEntity.stored, beehiveBlockEntity.savedFlowerPos);
		if (!beehiveBlockEntity.stored.isEmpty() && level.getRandom().nextDouble() < 0.005) {
			double d = (double)blockPos.getX() + 0.5;
			double e = (double)blockPos.getY();
			double f = (double)blockPos.getZ() + 0.5;
			level.playSound(null, d, e, f, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		DebugPackets.sendHiveInfo(level, blockPos, blockState, beehiveBlockEntity);
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.load(compoundTag, provider);
		this.stored.clear();
		ListTag listTag = compoundTag.getList("Bees", 10);

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag2 = listTag.getCompound(i);
			BeehiveBlockEntity.BeeData beeData = new BeehiveBlockEntity.BeeData(
				compoundTag2.getCompound("EntityData").copy(), compoundTag2.getInt("TicksInHive"), compoundTag2.getInt("MinOccupationTicks")
			);
			this.stored.add(beeData);
		}

		this.savedFlowerPos = (BlockPos)NbtUtils.readBlockPos(compoundTag, "flower_pos").orElse(null);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		compoundTag.put("Bees", this.writeBees());
		if (this.hasSavedFlowerPos()) {
			compoundTag.put("flower_pos", NbtUtils.writeBlockPos(this.savedFlowerPos));
		}
	}

	public ListTag writeBees() {
		ListTag listTag = new ListTag();

		for (BeehiveBlockEntity.BeeData beeData : this.stored) {
			CompoundTag compoundTag = beeData.entityData.copy();
			compoundTag.remove("UUID");
			CompoundTag compoundTag2 = new CompoundTag();
			compoundTag2.put("EntityData", compoundTag);
			compoundTag2.putInt("TicksInHive", beeData.ticksInHive);
			compoundTag2.putInt("MinOccupationTicks", beeData.minOccupationTicks);
			listTag.add(compoundTag2);
		}

		return listTag;
	}

	static class BeeData {
		final CompoundTag entityData;
		int ticksInHive;
		final int minOccupationTicks;

		BeeData(CompoundTag compoundTag, int i, int j) {
			BeehiveBlockEntity.removeIgnoredBeeTags(compoundTag);
			this.entityData = compoundTag;
			this.ticksInHive = i;
			this.minOccupationTicks = j;
		}
	}

	public static enum BeeReleaseStatus {
		HONEY_DELIVERED,
		BEE_RELEASED,
		EMERGENCY;
	}
}
