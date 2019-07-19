package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GateBehavior<E extends LivingEntity> extends Behavior<E> {
	private final Set<MemoryModuleType<?>> exitErasedMemories;
	private final GateBehavior.OrderPolicy orderPolicy;
	private final GateBehavior.RunningPolicy runningPolicy;
	private final WeightedList<Behavior<? super E>> behaviors = new WeightedList<>();

	public GateBehavior(
		Map<MemoryModuleType<?>, MemoryStatus> map,
		Set<MemoryModuleType<?>> set,
		GateBehavior.OrderPolicy orderPolicy,
		GateBehavior.RunningPolicy runningPolicy,
		List<Pair<Behavior<? super E>, Integer>> list
	) {
		super(map);
		this.exitErasedMemories = set;
		this.orderPolicy = orderPolicy;
		this.runningPolicy = runningPolicy;
		list.forEach(pair -> this.behaviors.add((Behavior<? super E>)pair.getFirst(), (Integer)pair.getSecond()));
	}

	@Override
	protected boolean canStillUse(ServerLevel serverLevel, E livingEntity, long l) {
		return this.behaviors
			.stream()
			.filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING)
			.anyMatch(behavior -> behavior.canStillUse(serverLevel, livingEntity, l));
	}

	@Override
	protected boolean timedOut(long l) {
		return false;
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		this.orderPolicy.apply(this.behaviors);
		this.runningPolicy.apply(this.behaviors, serverLevel, livingEntity, l);
	}

	@Override
	protected void tick(ServerLevel serverLevel, E livingEntity, long l) {
		this.behaviors
			.stream()
			.filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING)
			.forEach(behavior -> behavior.tickOrStop(serverLevel, livingEntity, l));
	}

	@Override
	protected void stop(ServerLevel serverLevel, E livingEntity, long l) {
		this.behaviors
			.stream()
			.filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING)
			.forEach(behavior -> behavior.doStop(serverLevel, livingEntity, l));
		this.exitErasedMemories.forEach(livingEntity.getBrain()::eraseMemory);
	}

	@Override
	public String toString() {
		Set<? extends Behavior<? super E>> set = (Set<? extends Behavior<? super E>>)this.behaviors
			.stream()
			.filter(behavior -> behavior.getStatus() == Behavior.Status.RUNNING)
			.collect(Collectors.toSet());
		return "(" + this.getClass().getSimpleName() + "): " + set;
	}

	static enum OrderPolicy {
		ORDERED(weightedList -> {
		}),
		SHUFFLED(WeightedList::shuffle);

		private final Consumer<WeightedList<?>> consumer;

		private OrderPolicy(Consumer<WeightedList<?>> consumer) {
			this.consumer = consumer;
		}

		public void apply(WeightedList<?> weightedList) {
			this.consumer.accept(weightedList);
		}
	}

	static enum RunningPolicy {
		RUN_ONE {
			@Override
			public <E extends LivingEntity> void apply(WeightedList<Behavior<? super E>> weightedList, ServerLevel serverLevel, E livingEntity, long l) {
				weightedList.stream()
					.filter(behavior -> behavior.getStatus() == Behavior.Status.STOPPED)
					.filter(behavior -> behavior.tryStart(serverLevel, livingEntity, l))
					.findFirst();
			}
		},
		TRY_ALL {
			@Override
			public <E extends LivingEntity> void apply(WeightedList<Behavior<? super E>> weightedList, ServerLevel serverLevel, E livingEntity, long l) {
				weightedList.stream()
					.filter(behavior -> behavior.getStatus() == Behavior.Status.STOPPED)
					.forEach(behavior -> behavior.tryStart(serverLevel, livingEntity, l));
			}
		};

		private RunningPolicy() {
		}

		public abstract <E extends LivingEntity> void apply(WeightedList<Behavior<? super E>> weightedList, ServerLevel serverLevel, E livingEntity, long l);
	}
}
