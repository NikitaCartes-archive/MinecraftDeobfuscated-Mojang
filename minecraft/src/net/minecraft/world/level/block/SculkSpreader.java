package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SculkSpreader {
	public static final int XP_PER_GROWTH_SPAWN = 10;
	public static final int NO_GROWTH_RADIUS = 4;
	public static final int MAX_GROWTH_RATE_RADIUS = 24;
	public static final short MAX_CHARGE = 1000;
	public static final int CHARGE_DECAY_RATE_HIGH = 10;
	public static final int ADDITIONAL_SLOW_DECAY_RATE = 5;
	public static final float MAX_DECAY_FACTOR = 0.5F;
	private static final int MAX_CURSORS = 32;
	private List<SculkSpreader.ChargeCursor> cursors = new ArrayList();

	@VisibleForTesting
	public List<SculkSpreader.ChargeCursor> getCursors() {
		return this.cursors;
	}

	public void load(CompoundTag compoundTag) {
		if (!this.cursors.isEmpty()) {
			throw new IllegalStateException("Some cursors have already been loaded. This is never expected to happen!");
		} else {
			for (Tag tag : compoundTag.getList("cursors", 9)) {
				if (tag instanceof CompoundTag compoundTag2) {
					this.addCursor(new SculkSpreader.ChargeCursor(compoundTag2));
				}
			}
		}
	}

	public void save(CompoundTag compoundTag) {
		ListTag listTag = new ListTag();

		for (SculkSpreader.ChargeCursor chargeCursor : this.cursors) {
			CompoundTag compoundTag2 = new CompoundTag();
			chargeCursor.save(compoundTag2);
			listTag.add(compoundTag2);
		}

		compoundTag.put("cursors", listTag);
	}

	public void addCursors(BlockPos blockPos, int i) {
		while (i > 0) {
			short s = (short)Math.min(i, 1000);
			this.addCursor(new SculkSpreader.ChargeCursor(blockPos, s));
			i -= s;
		}
	}

	private void addCursor(SculkSpreader.ChargeCursor chargeCursor) {
		if (this.cursors.size() < 32) {
			this.cursors.add(chargeCursor);
		}
	}

	public void updateCursors(Level level, BlockPos blockPos, Random random) {
		if (!this.cursors.isEmpty()) {
			List<SculkSpreader.ChargeCursor> list = new ArrayList();
			HashMap<BlockPos, SculkSpreader.ChargeCursor> hashMap = new HashMap();
			HashMap<BlockPos, Integer> hashMap2 = new HashMap();

			for (int i = 0; i < this.cursors.size(); i++) {
				SculkSpreader.ChargeCursor chargeCursor = (SculkSpreader.ChargeCursor)this.cursors.get(i);
				chargeCursor.update(level, blockPos, random);
				if (chargeCursor.charge <= 0) {
					level.levelEvent(3006, chargeCursor.getPos(), 0);
				} else {
					BlockPos blockPos2 = chargeCursor.getPos();
					hashMap2.put(blockPos2, (Integer)hashMap2.getOrDefault(blockPos2, 0) + chargeCursor.charge);
					SculkSpreader.ChargeCursor chargeCursor2 = (SculkSpreader.ChargeCursor)hashMap.get(blockPos2);
					if (chargeCursor2 == null) {
						hashMap.put(blockPos2, chargeCursor);
						list.add(chargeCursor);
					} else if (chargeCursor.charge + chargeCursor2.charge <= 1000) {
						chargeCursor2.addFrom(chargeCursor);
					} else {
						list.add(chargeCursor);
						if (chargeCursor.charge < chargeCursor2.charge) {
							hashMap.put(blockPos2, chargeCursor);
						}
					}
				}
			}

			for (BlockPos blockPos3 : hashMap2.keySet()) {
				int j = (Integer)hashMap2.get(blockPos3);
				SculkSpreader.ChargeCursor chargeCursor2 = (SculkSpreader.ChargeCursor)hashMap.get(blockPos3);
				byte b = chargeCursor2 == null ? 0 : chargeCursor2.getFacingData();
				if (b != -1 && j > 0) {
					int k = (int)(Math.log1p((double)j) / 2.3F) + 1;
					int l = (k << 6) + b;
					level.levelEvent(3006, blockPos3, l);
				}
			}

			this.cursors = list;
		}
	}

	public static class ChargeCursor {
		public static final byte UNKNOWN_FACING = -1;
		public static final byte EMPTY_FACING = 0;
		public static final byte MAX_CURSOR_DECAY_DELAY = 2;
		private BlockPos pos;
		short charge;
		private byte updateDelay;
		private byte facingData;
		private byte decayDelay;
		private static final Vec3i[] DIRECT_NEIGHBOUR_OFFSETS = new Vec3i[18];

		public ChargeCursor(BlockPos blockPos, short s) {
			this.pos = blockPos;
			this.charge = s;
			this.updateDelay = 0;
			this.decayDelay = 2;
			this.facingData = -1;
		}

		ChargeCursor(CompoundTag compoundTag) {
			this.pos = new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
			this.charge = compoundTag.getShort("charge");
			this.updateDelay = compoundTag.getByte("update");
			this.decayDelay = compoundTag.getByte("decay");
			this.facingData = compoundTag.getByte("faces");
		}

		public void save(CompoundTag compoundTag) {
			compoundTag.putInt("x", this.pos.getX());
			compoundTag.putInt("y", this.pos.getY());
			compoundTag.putInt("z", this.pos.getZ());
			compoundTag.putInt("charge", this.charge);
			compoundTag.putByte("update", this.updateDelay);
			compoundTag.putByte("decay", this.decayDelay);
			compoundTag.putByte("faces", this.facingData);
		}

		public BlockPos getPos() {
			return this.pos;
		}

		public short getCharge() {
			return this.charge;
		}

		public int getDecayDelay() {
			return this.decayDelay;
		}

		public byte getFacingData() {
			return this.facingData;
		}

		public void update(Level level, BlockPos blockPos, Random random) {
			if (level.shouldTickBlocksAt(this.pos)) {
				if (--this.updateDelay <= 0) {
					BlockState blockState = level.getBlockState(this.pos);
					SculkBehaviour sculkBehaviour = this.getBlockBehaviour(blockState);
					this.decayDelay = sculkBehaviour.updateDecayDelay(this.decayDelay);
					if (this.charge <= 0) {
						sculkBehaviour.onDischarged(level, blockState, this.pos, random);
					} else {
						if (sculkBehaviour.attemptSpreadVein(level, this.pos, blockState, this.facingData)) {
							if (sculkBehaviour.canChangeBlockStateOnSpread()) {
								blockState = level.getBlockState(this.pos);
								sculkBehaviour = this.getBlockBehaviour(blockState);
							}

							level.playSound(null, this.pos, SoundEvents.SCULK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
						}

						this.charge = sculkBehaviour.attemptUseCharge(this, level, blockPos, random);
						if (this.charge <= 0) {
							sculkBehaviour.onDischarged(level, blockState, this.pos, random);
						} else {
							BlockPos blockPos2 = this.getMoveToPos(level, this.pos, random);
							if (blockPos2 != null) {
								sculkBehaviour.onDischarged(level, blockState, this.pos, random);
								this.pos = blockPos2;
								blockState = level.getBlockState(blockPos2);
							}

							if (blockState.getBlock() instanceof SculkBehaviour) {
								this.facingData = Direction.pack(MultifaceBlock.availableFaces(blockState));
							}

							this.updateDelay = sculkBehaviour.getSculkSpreadDelay();
						}
					}
				}
			}
		}

		public void addFrom(SculkSpreader.ChargeCursor chargeCursor) {
			this.charge = (short)(this.charge + chargeCursor.charge);
			chargeCursor.charge = 0;
			this.updateDelay = (byte)Math.min(this.updateDelay, chargeCursor.updateDelay);
		}

		private SculkBehaviour getBlockBehaviour(BlockState blockState) {
			return blockState.getBlock() instanceof SculkBehaviour sculkBehaviour ? sculkBehaviour : SculkBehaviour.DEFAULT;
		}

		private Vec3i[] getDirectNeighbourOffsets(Random random) {
			int i = DIRECT_NEIGHBOUR_OFFSETS.length;
			Vec3i[] vec3is = new Vec3i[i];
			System.arraycopy(DIRECT_NEIGHBOUR_OFFSETS, 0, vec3is, 0, i);

			for (int j = i - 1; j > 0; j--) {
				int k = random.nextInt(j + 1);
				Vec3i vec3i = vec3is[j];
				vec3is[j] = vec3is[k];
				vec3is[k] = vec3i;
			}

			return vec3is;
		}

		@Nullable
		private BlockPos getMoveToPos(Level level, BlockPos blockPos, Random random) {
			BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
			BlockPos.MutableBlockPos mutableBlockPos2 = blockPos.mutable();

			for (Vec3i vec3i : this.getDirectNeighbourOffsets(random)) {
				mutableBlockPos2.setWithOffset(blockPos, vec3i);
				BlockState blockState = level.getBlockState(mutableBlockPos2);
				if (blockState.getBlock() instanceof SculkBehaviour && this.transferUnobstructed(level, blockPos, mutableBlockPos2)) {
					mutableBlockPos.set(mutableBlockPos2);
					if (SculkVeinBlock.hasSubstrateAccess(level, blockState, mutableBlockPos2)) {
						break;
					}
				}
			}

			return mutableBlockPos.equals(blockPos) ? null : mutableBlockPos.immutable();
		}

		private boolean transferUnobstructed(Level level, BlockPos blockPos, BlockPos blockPos2) {
			double d = blockPos.distSqr(blockPos2, false);
			if (d < 1.5) {
				return true;
			} else if (d > 2.5) {
				return false;
			} else {
				Direction direction;
				Direction direction2;
				if (blockPos.getX() == blockPos2.getX()) {
					direction = blockPos.getY() < blockPos2.getY() ? Direction.UP : Direction.DOWN;
					direction2 = blockPos.getZ() < blockPos2.getZ() ? Direction.SOUTH : Direction.NORTH;
				} else if (blockPos.getY() == blockPos2.getY()) {
					direction = blockPos.getX() < blockPos2.getX() ? Direction.EAST : Direction.WEST;
					direction2 = blockPos.getZ() < blockPos2.getZ() ? Direction.SOUTH : Direction.NORTH;
				} else {
					direction = blockPos.getX() < blockPos2.getX() ? Direction.EAST : Direction.WEST;
					direction2 = blockPos.getY() < blockPos2.getY() ? Direction.UP : Direction.DOWN;
				}

				BlockPos blockPos3 = blockPos.relative(direction);
				BlockPos blockPos4 = blockPos.relative(direction2);
				return !level.getBlockState(blockPos3).isFaceSturdy(level, blockPos3, direction.getOpposite())
					|| !level.getBlockState(blockPos4).isFaceSturdy(level, blockPos4, direction2.getOpposite());
			}
		}

		static {
			int i = 0;

			for (int j = -1; j < 2; j++) {
				for (int k = -1; k < 2; k++) {
					for (int l = -1; l < 2; l++) {
						if ((k != 0 || j != 0 || l != 0) && (k == 0 || j == 0 || l == 0)) {
							DIRECT_NEIGHBOUR_OFFSETS[i++] = new Vec3i(k, j, l);
						}
					}
				}
			}
		}
	}
}
