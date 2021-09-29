package net.minecraft.world.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
	private int tickCount;
	private int newGoalRate = 3;

	public GoalSelector(Supplier<ProfilerFiller> supplier) {
		this.profiler = supplier;
	}

	public void addGoal(int i, Goal goal) {
		this.availableGoals.add(new WrappedGoal(i, goal));
	}

	@VisibleForTesting
	public void removeAllGoals() {
		this.availableGoals.clear();
	}

	public void removeGoal(Goal goal) {
		this.availableGoals.stream().filter(wrappedGoal -> wrappedGoal.getGoal() == goal).filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
		this.availableGoals.removeIf(wrappedGoal -> wrappedGoal.getGoal() == goal);
	}

	private static boolean goalContainsAnyFlags(WrappedGoal wrappedGoal, EnumSet<Goal.Flag> enumSet) {
		for (Goal.Flag flag : wrappedGoal.getFlags()) {
			if (enumSet.contains(flag)) {
				return true;
			}
		}

		return false;
	}

	private static boolean goalCanBeReplacedForAllFlags(WrappedGoal wrappedGoal, Map<Goal.Flag, WrappedGoal> map) {
		for (Goal.Flag flag : wrappedGoal.getFlags()) {
			if (!((WrappedGoal)map.getOrDefault(flag, NO_GOAL)).canBeReplacedBy(wrappedGoal)) {
				return false;
			}
		}

		return true;
	}

	public void tick() {
		ProfilerFiller profilerFiller = (ProfilerFiller)this.profiler.get();
		profilerFiller.push("goalCleanup");

		for (WrappedGoal wrappedGoal : this.availableGoals) {
			if (wrappedGoal.isRunning() && (goalContainsAnyFlags(wrappedGoal, this.disabledFlags) || !wrappedGoal.canContinueToUse())) {
				wrappedGoal.stop();
			}
		}

		Iterator<Entry<Goal.Flag, WrappedGoal>> iterator = this.lockedFlags.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<Goal.Flag, WrappedGoal> entry = (Entry<Goal.Flag, WrappedGoal>)iterator.next();
			if (!((WrappedGoal)entry.getValue()).isRunning()) {
				iterator.remove();
			}
		}

		profilerFiller.pop();
		profilerFiller.push("goalUpdate");

		for (WrappedGoal wrappedGoalx : this.availableGoals) {
			if (!wrappedGoalx.isRunning()
				&& !goalContainsAnyFlags(wrappedGoalx, this.disabledFlags)
				&& goalCanBeReplacedForAllFlags(wrappedGoalx, this.lockedFlags)
				&& wrappedGoalx.canUse()) {
				for (Goal.Flag flag : wrappedGoalx.getFlags()) {
					WrappedGoal wrappedGoal2 = (WrappedGoal)this.lockedFlags.getOrDefault(flag, NO_GOAL);
					wrappedGoal2.stop();
					this.lockedFlags.put(flag, wrappedGoalx);
				}

				wrappedGoalx.start();
			}
		}

		profilerFiller.pop();
		this.tickRunningGoals(true);
	}

	public void tickRunningGoals(boolean bl) {
		ProfilerFiller profilerFiller = (ProfilerFiller)this.profiler.get();
		profilerFiller.push("goalTick");

		for (WrappedGoal wrappedGoal : this.availableGoals) {
			if (wrappedGoal.isRunning() && (bl || wrappedGoal.requiresUpdateEveryTick())) {
				wrappedGoal.tick();
			}
		}

		profilerFiller.pop();
	}

	public Set<WrappedGoal> getAvailableGoals() {
		return this.availableGoals;
	}

	public Stream<WrappedGoal> getRunningGoals() {
		return this.availableGoals.stream().filter(WrappedGoal::isRunning);
	}

	public void setNewGoalRate(int i) {
		this.newGoalRate = i;
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
