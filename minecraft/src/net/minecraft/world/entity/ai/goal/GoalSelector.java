package net.minecraft.world.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.util.profiling.ProfilerFiller;

public class GoalSelector {
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
	private final Set<WrappedGoal> availableGoals = new ObjectLinkedOpenHashSet<>();
	private final Supplier<ProfilerFiller> profiler;
	private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);

	public GoalSelector(Supplier<ProfilerFiller> supplier) {
		this.profiler = supplier;
	}

	public void addGoal(int i, Goal goal) {
		this.availableGoals.add(new WrappedGoal(i, goal));
	}

	@VisibleForTesting
	public void removeAllGoals(Predicate<Goal> predicate) {
		this.availableGoals.removeIf(wrappedGoal -> predicate.test(wrappedGoal.getGoal()));
	}

	public void removeGoal(Goal goal) {
		for(WrappedGoal wrappedGoal : this.availableGoals) {
			if (wrappedGoal.getGoal() == goal && wrappedGoal.isRunning()) {
				wrappedGoal.stop();
			}
		}

		this.availableGoals.removeIf(wrappedGoalx -> wrappedGoalx.getGoal() == goal);
	}

	private static boolean goalContainsAnyFlags(WrappedGoal wrappedGoal, EnumSet<Goal.Flag> enumSet) {
		for(Goal.Flag flag : wrappedGoal.getFlags()) {
			if (enumSet.contains(flag)) {
				return true;
			}
		}

		return false;
	}

	private static boolean goalCanBeReplacedForAllFlags(WrappedGoal wrappedGoal, Map<Goal.Flag, WrappedGoal> map) {
		for(Goal.Flag flag : wrappedGoal.getFlags()) {
			if (!((WrappedGoal)map.getOrDefault(flag, NO_GOAL)).canBeReplacedBy(wrappedGoal)) {
				return false;
			}
		}

		return true;
	}

	public void tick() {
		ProfilerFiller profilerFiller = (ProfilerFiller)this.profiler.get();
		profilerFiller.push("goalCleanup");

		for(WrappedGoal wrappedGoal : this.availableGoals) {
			if (wrappedGoal.isRunning() && (goalContainsAnyFlags(wrappedGoal, this.disabledFlags) || !wrappedGoal.canContinueToUse())) {
				wrappedGoal.stop();
			}
		}

		this.lockedFlags.entrySet().removeIf(entry -> !((WrappedGoal)entry.getValue()).isRunning());
		profilerFiller.pop();
		profilerFiller.push("goalUpdate");

		for(WrappedGoal wrappedGoal : this.availableGoals) {
			if (!wrappedGoal.isRunning()
				&& !goalContainsAnyFlags(wrappedGoal, this.disabledFlags)
				&& goalCanBeReplacedForAllFlags(wrappedGoal, this.lockedFlags)
				&& wrappedGoal.canUse()) {
				for(Goal.Flag flag : wrappedGoal.getFlags()) {
					WrappedGoal wrappedGoal2 = (WrappedGoal)this.lockedFlags.getOrDefault(flag, NO_GOAL);
					wrappedGoal2.stop();
					this.lockedFlags.put(flag, wrappedGoal);
				}

				wrappedGoal.start();
			}
		}

		profilerFiller.pop();
		this.tickRunningGoals(true);
	}

	public void tickRunningGoals(boolean bl) {
		ProfilerFiller profilerFiller = (ProfilerFiller)this.profiler.get();
		profilerFiller.push("goalTick");

		for(WrappedGoal wrappedGoal : this.availableGoals) {
			if (wrappedGoal.isRunning() && (bl || wrappedGoal.requiresUpdateEveryTick())) {
				wrappedGoal.tick();
			}
		}

		profilerFiller.pop();
	}

	public Set<WrappedGoal> getAvailableGoals() {
		return this.availableGoals;
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
