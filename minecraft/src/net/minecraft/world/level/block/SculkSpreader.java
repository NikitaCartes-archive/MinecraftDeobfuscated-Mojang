package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class SculkSpreader {
	public static final int XP_PER_GROWTH_SPAWN = 10;
	public static final int NO_GROWTH_RADIUS = 4;
	public static final int MAX_GROWTH_RATE_RADIUS = 24;
	public static final int MAX_CHARGE = 1000;
	public static final int CHARGE_DECAY_RATE_HIGH = 10;
	public static final int ADDITIONAL_SLOW_DECAY_RATE = 5;
	public static final float MAX_DECAY_FACTOR = 0.5F;
	private static final int MAX_CURSORS = 32;
	private List<SculkSpreader.ChargeCursor> cursors = new ArrayList();
	private static final Logger LOGGER = LogUtils.getLogger();

	@VisibleForTesting
	public List<SculkSpreader.ChargeCursor> getCursors() {
		return this.cursors;
	}

	public void load(CompoundTag compoundTag) {
		if (compoundTag.contains("cursors", 9)) {
			this.cursors.clear();
			List<SculkSpreader.ChargeCursor> list = (List<SculkSpreader.ChargeCursor>)SculkSpreader.ChargeCursor.CODEC
				.listOf()
				.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.getList("cursors", 10)))
				.resultOrPartial(LOGGER::error)
				.orElseGet(ArrayList::new);
			int i = Math.min(list.size(), 32);

			for (int j = 0; j < i; j++) {
				this.addCursor((SculkSpreader.ChargeCursor)list.get(j));
			}
		}
	}

	public void save(CompoundTag compoundTag) {
		SculkSpreader.ChargeCursor.CODEC
			.listOf()
			.encodeStart(NbtOps.INSTANCE, this.cursors)
			.resultOrPartial(LOGGER::error)
			.ifPresent(tag -> compoundTag.put("cursors", tag));
	}

	public void addCursors(BlockPos blockPos, int i) {
		while (i > 0) {
			int j = Math.min(i, 1000);
			this.addCursor(new SculkSpreader.ChargeCursor(blockPos, j));
			i -= j;
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
			Map<BlockPos, SculkSpreader.ChargeCursor> map = new HashMap();
			Object2IntMap<BlockPos> object2IntMap = new Object2IntOpenHashMap<>();

			for (SculkSpreader.ChargeCursor chargeCursor : this.cursors) {
				chargeCursor.update(level, blockPos, random);
				if (chargeCursor.charge <= 0) {
					level.levelEvent(3006, chargeCursor.getPos(), 0);
				} else {
					BlockPos blockPos2 = chargeCursor.getPos();
					object2IntMap.computeInt(blockPos2, (blockPosx, integer) -> (integer == null ? 0 : integer) + chargeCursor.charge);
					SculkSpreader.ChargeCursor chargeCursor2 = (SculkSpreader.ChargeCursor)map.get(blockPos2);
					if (chargeCursor2 == null) {
						map.put(blockPos2, chargeCursor);
						list.add(chargeCursor);
					} else if (chargeCursor.charge + chargeCursor2.charge <= 1000) {
						chargeCursor2.mergeWith(chargeCursor);
					} else {
						list.add(chargeCursor);
						if (chargeCursor.charge < chargeCursor2.charge) {
							map.put(blockPos2, chargeCursor);
						}
					}
				}
			}

			for (Entry<BlockPos> entry : object2IntMap.object2IntEntrySet()) {
				BlockPos blockPos2 = (BlockPos)entry.getKey();
				int i = entry.getIntValue();
				SculkSpreader.ChargeCursor chargeCursor3 = (SculkSpreader.ChargeCursor)map.get(blockPos2);
				Collection<Direction> collection = chargeCursor3 == null ? null : chargeCursor3.getFacingData();
				if (i > 0 && collection != null) {
					int j = (int)(Math.log1p((double)i) / 2.3F) + 1;
					int k = (j << 6) + MultifaceBlock.pack(collection);
					level.levelEvent(3006, blockPos2, k);
				}
			}

			this.cursors = list;
		}
	}

	public static class ChargeCursor {
		private static final List<Vec3i> NON_CORNER_NEIGHBOURS = Util.make(
			new ArrayList(18),
			arrayList -> BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
					.filter(blockPos -> (blockPos.getX() == 0 || blockPos.getY() == 0 || blockPos.getZ() == 0) && !blockPos.equals(BlockPos.ZERO))
					.map(BlockPos::immutable)
					.forEach(arrayList::add)
		);
		public static final int MAX_CURSOR_DECAY_DELAY = 1;
		private BlockPos pos;
		int charge;
		private int updateDelay;
		private int decayDelay;
		@Nullable
		private Set<Direction> facings;
		private static final Codec<Set<Direction>> DIRECTION_SET = Direction.CODEC.listOf().xmap(list -> Sets.newEnumSet(list, Direction.class), Lists::newArrayList);
		public static final Codec<SculkSpreader.ChargeCursor> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BlockPos.CODEC.fieldOf("pos").forGetter(SculkSpreader.ChargeCursor::getPos),
						Codec.intRange(0, 1000).fieldOf("charge").orElse(0).forGetter(SculkSpreader.ChargeCursor::getCharge),
						Codec.intRange(0, 1).fieldOf("decay_delay").orElse(1).forGetter(SculkSpreader.ChargeCursor::getDecayDelay),
						Codec.intRange(0, Integer.MAX_VALUE).fieldOf("update_delay").orElse(0).forGetter(chargeCursor -> chargeCursor.updateDelay),
						DIRECTION_SET.optionalFieldOf("facings").forGetter(chargeCursor -> Optional.ofNullable(chargeCursor.getFacingData()))
					)
					.apply(instance, SculkSpreader.ChargeCursor::new)
		);

		private ChargeCursor(BlockPos blockPos, int i, int j, int k, Optional<Set<Direction>> optional) {
			this.pos = blockPos;
			this.charge = i;
			this.decayDelay = j;
			this.updateDelay = k;
			this.facings = (Set<Direction>)optional.orElse(null);
		}

		public ChargeCursor(BlockPos blockPos, int i) {
			this(blockPos, i, 1, 0, Optional.empty());
		}

		public BlockPos getPos() {
			return this.pos;
		}

		public int getCharge() {
			return this.charge;
		}

		public int getDecayDelay() {
			return this.decayDelay;
		}

		@Nullable
		public Set<Direction> getFacingData() {
			return this.facings;
		}

		public void update(Level level, BlockPos blockPos, Random random) {
			if (level.shouldTickBlocksAt(this.pos) && this.charge > 0) {
				if (this.updateDelay > 0) {
					this.updateDelay--;
				} else {
					BlockState blockState = level.getBlockState(this.pos);
					SculkBehaviour sculkBehaviour = getBlockBehaviour(blockState);
					if (sculkBehaviour.attemptSpreadVein(level, this.pos, blockState, this.facings)) {
						if (sculkBehaviour.canChangeBlockStateOnSpread()) {
							blockState = level.getBlockState(this.pos);
							sculkBehaviour = getBlockBehaviour(blockState);
						}

						level.playSound(null, this.pos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
					}

					this.charge = sculkBehaviour.attemptUseCharge(this, level, blockPos, random);
					if (this.charge <= 0) {
						sculkBehaviour.onDischarged(level, blockState, this.pos, random);
					} else {
						BlockPos blockPos2 = getValidMovementPos(level, this.pos, random);
						if (blockPos2 != null) {
							sculkBehaviour.onDischarged(level, blockState, this.pos, random);
							this.pos = blockPos2.immutable();
							blockState = level.getBlockState(blockPos2);
						}

						if (blockState.getBlock() instanceof SculkBehaviour) {
							this.facings = MultifaceBlock.availableFaces(blockState);
						}

						this.decayDelay = sculkBehaviour.updateDecayDelay(this.decayDelay);
						this.updateDelay = sculkBehaviour.getSculkSpreadDelay();
					}
				}
			}
		}

		void mergeWith(SculkSpreader.ChargeCursor chargeCursor) {
			this.charge = this.charge + chargeCursor.charge;
			chargeCursor.charge = 0;
			this.updateDelay = Math.min(this.updateDelay, chargeCursor.updateDelay);
		}

		private static SculkBehaviour getBlockBehaviour(BlockState blockState) {
			return blockState.getBlock() instanceof SculkBehaviour sculkBehaviour ? sculkBehaviour : SculkBehaviour.DEFAULT;
		}

		private static List<Vec3i> getRandomizedNonCornerNeighbourOffsets(Random random) {
			List<Vec3i> list = new ArrayList(NON_CORNER_NEIGHBOURS);
			Collections.shuffle(list, random);
			return list;
		}

		@Nullable
		private static BlockPos getValidMovementPos(Level level, BlockPos blockPos, Random random) {
			BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
			BlockPos.MutableBlockPos mutableBlockPos2 = blockPos.mutable();

			for (Vec3i vec3i : getRandomizedNonCornerNeighbourOffsets(random)) {
				mutableBlockPos2.setWithOffset(blockPos, vec3i);
				BlockState blockState = level.getBlockState(mutableBlockPos2);
				if (blockState.getBlock() instanceof SculkBehaviour && isMovementUnobstructed(level, blockPos, mutableBlockPos2)) {
					mutableBlockPos.set(mutableBlockPos2);
					if (SculkVeinBlock.hasSubstrateAccess(level, blockState, mutableBlockPos2)) {
						break;
					}
				}
			}

			return mutableBlockPos.equals(blockPos) ? null : mutableBlockPos;
		}

		private static boolean isMovementUnobstructed(Level level, BlockPos blockPos, BlockPos blockPos2) {
			if (blockPos.distManhattan(blockPos2) == 1) {
				return true;
			} else {
				BlockPos blockPos3 = blockPos2.subtract(blockPos);
				Direction direction = Direction.fromAxisAndDirection(
					Direction.Axis.X, blockPos3.getX() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE
				);
				Direction direction2 = Direction.fromAxisAndDirection(
					Direction.Axis.Y, blockPos3.getY() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE
				);
				Direction direction3 = Direction.fromAxisAndDirection(
					Direction.Axis.Z, blockPos3.getZ() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE
				);
				if (blockPos3.getX() == 0) {
					return isUnobstructed(level, blockPos, direction2) || isUnobstructed(level, blockPos, direction3);
				} else {
					return blockPos3.getY() == 0
						? isUnobstructed(level, blockPos, direction) || isUnobstructed(level, blockPos, direction3)
						: isUnobstructed(level, blockPos, direction) || isUnobstructed(level, blockPos, direction2);
				}
			}
		}

		private static boolean isUnobstructed(Level level, BlockPos blockPos, Direction direction) {
			BlockPos blockPos2 = blockPos.relative(direction);
			return !level.getBlockState(blockPos2).isFaceSturdy(level, blockPos2, direction.getOpposite());
		}
	}
}
