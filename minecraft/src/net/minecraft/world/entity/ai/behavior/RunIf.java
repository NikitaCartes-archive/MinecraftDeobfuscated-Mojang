package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class RunIf<E extends LivingEntity> extends Behavior<E> {
	private final Predicate<E> predicate;
	private final Behavior<? super E> wrappedBehavior;
	private final boolean checkWhileRunningAlso;

	public RunIf(Map<MemoryModuleType<?>, MemoryStatus> map, Predicate<E> predicate, Behavior<? super E> behavior, boolean bl) {
		super(mergeMaps(map, behavior.entryCondition));
		this.predicate = predicate;
		this.wrappedBehavior = behavior;
		this.checkWhileRunningAlso = bl;
	}

	private static Map<MemoryModuleType<?>, MemoryStatus> mergeMaps(Map<MemoryModuleType<?>, MemoryStatus> map, Map<MemoryModuleType<?>, MemoryStatus> map2) {
		Map<MemoryModuleType<?>, MemoryStatus> map3 = Maps.<MemoryModuleType<?>, MemoryStatus>newHashMap();
		map3.putAll(map);
		map3.putAll(map2);
		return map3;
	}

	public RunIf(Predicate<E> predicate, Behavior<? super E> behavior) {
		this(ImmutableMap.of(), predicate, behavior, false);
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		return this.predicate.test(livingEntity) && this.wrappedBehavior.checkExtraStartConditions(serverLevel, livingEntity);
	}

	@Override
	protected boolean canStillUse(ServerLevel serverLevel, E livingEntity, long l) {
		return this.checkWhileRunningAlso && this.predicate.test(livingEntity) && this.wrappedBehavior.canStillUse(serverLevel, livingEntity, l);
	}

	@Override
	protected boolean timedOut(long l) {
		return false;
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		this.wrappedBehavior.start(serverLevel, livingEntity, l);
	}

	@Override
	protected void tick(ServerLevel serverLevel, E livingEntity, long l) {
		this.wrappedBehavior.tick(serverLevel, livingEntity, l);
	}

	@Override
	protected void stop(ServerLevel serverLevel, E livingEntity, long l) {
		this.wrappedBehavior.stop(serverLevel, livingEntity, l);
	}

	@Override
	public String toString() {
		return "RunIf: " + this.wrappedBehavior;
	}
}
