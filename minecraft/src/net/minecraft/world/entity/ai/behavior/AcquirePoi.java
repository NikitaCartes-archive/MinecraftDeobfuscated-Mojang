package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;

public class AcquirePoi extends Behavior<PathfinderMob> {
	private final PoiType poiType;
	private final MemoryModuleType<GlobalPos> memoryType;
	private final boolean onlyIfAdult;
	private long lastUpdate;
	private final Long2LongMap batchCache = new Long2LongOpenHashMap();
	private int triedCount;

	public AcquirePoi(PoiType poiType, MemoryModuleType<GlobalPos> memoryModuleType, boolean bl) {
		super(ImmutableMap.of(memoryModuleType, MemoryStatus.VALUE_ABSENT));
		this.poiType = poiType;
		this.memoryType = memoryModuleType;
		this.onlyIfAdult = bl;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		return this.onlyIfAdult && pathfinderMob.isBaby() ? false : serverLevel.getGameTime() - this.lastUpdate >= 20L;
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		this.triedCount = 0;
		this.lastUpdate = serverLevel.getGameTime() + (long)serverLevel.getRandom().nextInt(20);
		PoiManager poiManager = serverLevel.getPoiManager();
		Predicate<BlockPos> predicate = blockPosx -> {
			long lx = blockPosx.asLong();
			if (this.batchCache.containsKey(lx)) {
				return false;
			} else if (++this.triedCount >= 5) {
				return false;
			} else {
				this.batchCache.put(lx, this.lastUpdate + 40L);
				return true;
			}
		};
		Stream<BlockPos> stream = poiManager.findAll(this.poiType.getPredicate(), predicate, new BlockPos(pathfinderMob), 48, PoiManager.Occupancy.HAS_SPACE);
		Path path = pathfinderMob.getNavigation().createPath(stream, this.poiType.getValidRange());
		if (path != null && path.canReach()) {
			BlockPos blockPos = path.getTarget();
			poiManager.getType(blockPos).ifPresent(poiType -> {
				poiManager.take(this.poiType.getPredicate(), blockPos2 -> blockPos2.equals(blockPos), blockPos, 1);
				pathfinderMob.getBrain().setMemory(this.memoryType, GlobalPos.of(serverLevel.getDimension().getType(), blockPos));
				DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
			});
		} else if (this.triedCount < 5) {
			this.batchCache.long2LongEntrySet().removeIf(entry -> entry.getLongValue() < this.lastUpdate);
		}
	}
}
