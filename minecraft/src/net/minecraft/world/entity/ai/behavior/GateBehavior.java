package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GateBehavior<E extends LivingEntity> implements BehaviorControl<E> {
	private final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
	private final Set<MemoryModuleType<?>> exitErasedMemories;
	private final GateBehavior.OrderPolicy orderPolicy;
	private final GateBehavior.RunningPolicy runningPolicy;
	private final ShufflingList<BehaviorControl<? super E>> behaviors = new ShufflingList<>();
	private Behavior.Status status = Behavior.Status.STOPPED;

	public GateBehavior(
		Map<MemoryModuleType<?>, MemoryStatus> map,
		Set<MemoryModuleType<?>> set,
		GateBehavior.OrderPolicy orderPolicy,
		GateBehavior.RunningPolicy runningPolicy,
		List<Pair<? extends BehaviorControl<? super E>, Integer>> list
	) {
		this.entryCondition = map;
		this.exitErasedMemories = set;
		this.orderPolicy = orderPolicy;
		this.runningPolicy = runningPolicy;
		list.forEach(pair -> this.behaviors.add((BehaviorControl<? super E>)pair.getFirst(), (Integer)pair.getSecond()));
	}

	@Override
	public Behavior.Status getStatus() {
		return this.status;
	}

	private boolean hasRequiredMemories(E livingEntity) {
		for (Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
			MemoryModuleType<?> memoryModuleType = (MemoryModuleType<?>)entry.getKey();
			MemoryStatus memoryStatus = (MemoryStatus)entry.getValue();
			if (!livingEntity.getBrain().checkMemory(memoryModuleType, memoryStatus)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public final boolean tryStart(ServerLevel serverLevel, E livingEntity, long l) {
		if (this.hasRequiredMemories(livingEntity)) {
			this.status = Behavior.Status.RUNNING;
			this.orderPolicy.apply(this.behaviors);
			this.runningPolicy.apply(this.behaviors.stream(), serverLevel, livingEntity, l);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public final void tickOrStop(ServerLevel serverLevel, E livingEntity, long l) {
		this.behaviors
			.stream()
			.filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.RUNNING)
			.forEach(behaviorControl -> behaviorControl.tickOrStop(serverLevel, livingEntity, l));
		if (this.behaviors.stream().noneMatch(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.RUNNING)) {
			this.doStop(serverLevel, livingEntity, l);
		}
	}

	@Override
	public final void doStop(ServerLevel serverLevel, E livingEntity, long l) {
		this.status = Behavior.Status.STOPPED;
		this.behaviors
			.stream()
			.filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.RUNNING)
			.forEach(behaviorControl -> behaviorControl.doStop(serverLevel, livingEntity, l));
		this.exitErasedMemories.forEach(livingEntity.getBrain()::eraseMemory);
	}

	@Override
	public String debugString() {
		return this.getClass().getSimpleName();
	}

	public String toString() {
		Set<? extends BehaviorControl<? super E>> set = (Set<? extends BehaviorControl<? super E>>)this.behaviors
			.stream()
			.filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.RUNNING)
			.collect(Collectors.toSet());
		return "(" + this.getClass().getSimpleName() + "): " + set;
	}

	public static enum OrderPolicy {
		ORDERED(shufflingList -> {
		}),
		SHUFFLED(ShufflingList::shuffle);

		private final Consumer<ShufflingList<?>> consumer;

		private OrderPolicy(Consumer<ShufflingList<?>> consumer) {
			this.consumer = consumer;
		}

		public void apply(ShufflingList<?> shufflingList) {
			this.consumer.accept(shufflingList);
		}
	}

	public static enum RunningPolicy {
		RUN_ONE {
			@Override
			public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> stream, ServerLevel serverLevel, E livingEntity, long l) {
				stream.filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.STOPPED)
					.filter(behaviorControl -> behaviorControl.tryStart(serverLevel, livingEntity, l))
					.findFirst();
			}
		},
		TRY_ALL {
			@Override
			public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> stream, ServerLevel serverLevel, E livingEntity, long l) {
				stream.filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.STOPPED)
					.forEach(behaviorControl -> behaviorControl.tryStart(serverLevel, livingEntity, l));
			}
		};

		public abstract <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> stream, ServerLevel serverLevel, E livingEntity, long l);
	}
}
