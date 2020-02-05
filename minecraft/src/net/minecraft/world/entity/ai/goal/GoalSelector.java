package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GoalSelector {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final WrappedGoal NO_GOAL = new WrappedGoal(Integer.MAX_VALUE, new Goal() {
		@Override
		public boolean canUse() {
			return false;
		}
	}) {
		@Override
		public boolean isRunning() {
			return false;
		}
	};
	private final Map<Goal.Flag, WrappedGoal> lockedFlags = new EnumMap(Goal.Flag.class);
	private final Set<WrappedGoal> availableGoals = Sets.<WrappedGoal>newLinkedHashSet();
	private final Supplier<ProfilerFiller> profiler;
	private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);
	private int newGoalRate = 3;

	public GoalSelector(Supplier<ProfilerFiller> supplier) {
		this.profiler = supplier;
	}

	public void addGoal(int i, Goal goal) {
		this.availableGoals.add(new WrappedGoal(i, goal));
	}

	public void removeGoal(Goal goal) {
		this.availableGoals.stream().filter(wrappedGoal -> wrappedGoal.getGoal() == goal).filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
		this.availableGoals.removeIf(wrappedGoal -> wrappedGoal.getGoal() == goal);
	}

	public void tick() {
		ProfilerFiller profilerFiller = (ProfilerFiller)this.profiler.get();
		profilerFiller.push("goalCleanup");
		this.getRunningGoals()
			.filter(wrappedGoal -> !wrappedGoal.isRunning() || wrappedGoal.getFlags().stream().anyMatch(this.disabledFlags::contains) || !wrappedGoal.canContinueToUse())
			.forEach(Goal::stop);
		this.lockedFlags.forEach((flag, wrappedGoal) -> {
			if (!wrappedGoal.isRunning()) {
				this.lockedFlags.remove(flag);
			}
		});
		profilerFiller.pop();
		profilerFiller.push("goalUpdate");
		this.availableGoals
			.stream()
			.filter(wrappedGoal -> !wrappedGoal.isRunning())
			.filter(wrappedGoal -> wrappedGoal.getFlags().stream().noneMatch(this.disabledFlags::contains))
			.filter(
				wrappedGoal -> wrappedGoal.getFlags().stream().allMatch(flag -> ((WrappedGoal)this.lockedFlags.getOrDefault(flag, NO_GOAL)).canBeReplacedBy(wrappedGoal))
			)
			.filter(WrappedGoal::canUse)
			.forEach(wrappedGoal -> {
				wrappedGoal.getFlags().forEach(flag -> {
					WrappedGoal wrappedGoal2 = (WrappedGoal)this.lockedFlags.getOrDefault(flag, NO_GOAL);
					wrappedGoal2.stop();
					this.lockedFlags.put(flag, wrappedGoal);
				});
				wrappedGoal.start();
			});
		profilerFiller.pop();
		profilerFiller.push("goalTick");
		this.getRunningGoals().forEach(WrappedGoal::tick);
		profilerFiller.pop();
	}

	public Stream<WrappedGoal> getRunningGoals() {
		return this.availableGoals.stream().filter(WrappedGoal::isRunning);
	}

	public void disableControlFlag(Goal.Flag flag) {
		this.disabledFlags.add(flag);
	}

	public void enableControlFlag(Goal.Flag flag) {
		this.disabledFlags.remove(flag);
	}

	public void setControlFlag(Goal.Flag flag, boolean bl) {
		if (bl) {
			this.enableControlFlag(flag);
		} else {
			this.disableControlFlag(flag);
		}
	}
}
