package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BeehiveBlockEntity extends BlockEntity implements TickableBlockEntity {
	private final List<BeehiveBlockEntity.BeeData> stored = Lists.<BeehiveBlockEntity.BeeData>newArrayList();
	private BlockPos savedFlowerPos = BlockPos.ZERO;

	public BeehiveBlockEntity() {
		super(BlockEntityType.BEEHIVE);
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
						if (!BeehiveBlock.isCampfireBelow(this.level, this.getBlockPos())) {
							bee.makeAngry(player);
						} else {
							bee.setCannotEnterHiveTicks(400);
						}
					}
				}
			}
		}
	}

	private List<Entity> releaseAllOccupants(BlockState blockState, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus) {
		List<Entity> list = Lists.<Entity>newArrayList();
		this.stored.removeIf(beeData -> this.releaseOccupant(blockState, beeData.entityData, list, beeReleaseStatus));
		return list;
	}

	public void addOccupant(Entity entity, boolean bl) {
		this.addOccupantWithPresetTicks(entity, bl, 0);
	}

	protected void sendDebugPackets() {
		DebugPackets.sendHiveInfo(this);
	}

	public void addOccupantWithPresetTicks(Entity entity, boolean bl, int i) {
		if (this.stored.size() < 3) {
			entity.ejectPassengers();
			CompoundTag compoundTag = new CompoundTag();
			entity.save(compoundTag);
			this.stored.add(new BeehiveBlockEntity.BeeData(compoundTag, i, bl ? 2400 : 600));
			if (this.level != null) {
				if (entity instanceof Bee) {
					Bee bee = (Bee)entity;
					if (!this.hasSavedFlowerPos() || bee.hasSavedFlowerPos() && this.level.random.nextBoolean()) {
						this.savedFlowerPos = bee.getSavedFlowerPos();
					}
				}

				BlockPos blockPos = this.getBlockPos();
				this.level
					.playSound(null, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
			}

			entity.remove();
		}
	}

	private boolean releaseOccupant(
		BlockState blockState, CompoundTag compoundTag, @Nullable List<Entity> list, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus
	) {
		BlockPos blockPos = this.getBlockPos();
		if ((!this.level.isDay() || this.level.isRaining()) && beeReleaseStatus != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
			return false;
		} else {
			compoundTag.remove("Passengers");
			compoundTag.remove("Leash");
			compoundTag.removeUUID("UUID");
			Optional<BlockPos> optional = Optional.empty();
			Direction direction = blockState.getValue(BeehiveBlock.FACING);
			BlockPos blockPos2 = blockPos.relative(direction, 2);
			if (this.level.getBlockState(blockPos2).getCollisionShape(this.level, blockPos2).isEmpty()) {
				optional = Optional.of(blockPos2);
			}

			if (!optional.isPresent()) {
				for (Direction direction2 : Direction.Plane.HORIZONTAL) {
					BlockPos blockPos3 = blockPos.offset(direction2.getStepX() * 2, direction2.getStepY(), direction2.getStepZ() * 2);
					if (this.level.getBlockState(blockPos3).getCollisionShape(this.level, blockPos3).isEmpty()) {
						optional = Optional.of(blockPos3);
						break;
					}
				}
			}

			if (!optional.isPresent()) {
				for (Direction direction2x : Direction.Plane.VERTICAL) {
					BlockPos blockPos3 = blockPos.offset(direction2x.getStepX() * 2, direction2x.getStepY(), direction2x.getStepZ() * 2);
					if (this.level.getBlockState(blockPos3).getCollisionShape(this.level, blockPos3).isEmpty()) {
						optional = Optional.of(blockPos3);
					}
				}
			}

			if (!optional.isPresent()) {
				return false;
			} else {
				BlockPos blockPos4 = (BlockPos)optional.get();
				Entity entity = EntityType.loadEntityRecursive(compoundTag, this.level, entityx -> {
					entityx.moveTo((double)blockPos4.getX(), (double)blockPos4.getY(), (double)blockPos4.getZ(), entityx.yRot, entityx.xRot);
					return entityx;
				});
				if (entity != null) {
					if (!entity.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
						return false;
					} else {
						if (entity instanceof Bee) {
							Bee bee = (Bee)entity;
							if (this.hasSavedFlowerPos() && !bee.hasSavedFlowerPos() && this.level.random.nextFloat() < 0.9F) {
								bee.setSavedFlowerPos(this.savedFlowerPos);
							}

							if (beeReleaseStatus == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
								bee.dropOffNectar();
								if (blockState.getBlock().is(BlockTags.BEEHIVES)) {
									int i = (Integer)blockState.getValue(BeehiveBlock.HONEY_LEVEL);
									if (i < 5) {
										int j = this.level.random.nextInt(100) == 0 ? 2 : 1;
										if (i + j > 5) {
											j--;
										}

										this.level.setBlockAndUpdate(this.getBlockPos(), blockState.setValue(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(i + j)));
									}
								}
							}

							if (list != null) {
								bee.resetTicksSincePollination();
								list.add(bee);
							}
						}

						BlockPos blockPos3 = this.getBlockPos();
						this.level
							.playSound(null, (double)blockPos3.getX(), (double)blockPos3.getY(), (double)blockPos3.getZ(), SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
						return this.level.addFreshEntity(entity);
					}
				} else {
					return false;
				}
			}
		}
	}

	private boolean hasSavedFlowerPos() {
		return this.savedFlowerPos != BlockPos.ZERO;
	}

	private void tickOccupants() {
		Iterator<BeehiveBlockEntity.BeeData> iterator = this.stored.iterator();
		BlockState blockState = this.getBlockState();

		while (iterator.hasNext()) {
			BeehiveBlockEntity.BeeData beeData = (BeehiveBlockEntity.BeeData)iterator.next();
			if (beeData.ticksInHive > beeData.minOccupationTicks) {
				CompoundTag compoundTag = beeData.entityData;
				BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus = compoundTag.getBoolean("HasNectar")
					? BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED
					: BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED;
				if (this.releaseOccupant(blockState, compoundTag, null, beeReleaseStatus)) {
					iterator.remove();
				}
			} else {
				beeData.ticksInHive++;
			}
		}
	}

	@Override
	public void tick() {
		if (!this.level.isClientSide) {
			this.tickOccupants();
			BlockPos blockPos = this.getBlockPos();
			if (this.stored.size() > 0 && this.level.getRandom().nextDouble() < 0.005) {
				double d = (double)blockPos.getX() + 0.5;
				double e = (double)blockPos.getY();
				double f = (double)blockPos.getZ() + 0.5;
				this.level.playSound(null, d, e, f, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0F, 1.0F);
			}

			this.sendDebugPackets();
		}
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.stored.clear();
		ListTag listTag = compoundTag.getList("Bees", 10);

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag2 = listTag.getCompound(i);
			BeehiveBlockEntity.BeeData beeData = new BeehiveBlockEntity.BeeData(
				compoundTag2.getCompound("EntityData"), compoundTag2.getInt("TicksInHive"), compoundTag2.getInt("MinOccupationTicks")
			);
			this.stored.add(beeData);
		}

		this.savedFlowerPos = NbtUtils.readBlockPos(compoundTag.getCompound("FlowerPos"));
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		compoundTag.put("Bees", this.writeBees());
		compoundTag.put("FlowerPos", NbtUtils.writeBlockPos(this.savedFlowerPos));
		return compoundTag;
	}

	public ListTag writeBees() {
		ListTag listTag = new ListTag();

		for (BeehiveBlockEntity.BeeData beeData : this.stored) {
			beeData.entityData.removeUUID("UUID");
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.put("EntityData", beeData.entityData);
			compoundTag.putInt("TicksInHive", beeData.ticksInHive);
			compoundTag.putInt("MinOccupationTicks", beeData.minOccupationTicks);
			listTag.add(compoundTag);
		}

		return listTag;
	}

	static class BeeData {
		private final CompoundTag entityData;
		private int ticksInHive;
		private final int minOccupationTicks;

		private BeeData(CompoundTag compoundTag, int i, int j) {
			compoundTag.removeUUID("UUID");
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
