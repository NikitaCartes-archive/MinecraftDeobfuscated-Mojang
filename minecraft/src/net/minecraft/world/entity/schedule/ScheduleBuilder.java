package net.minecraft.world.entity.schedule;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduleBuilder {
	private final Schedule schedule;
	private final List<ScheduleBuilder.ActivityTransition> transitions = Lists.<ScheduleBuilder.ActivityTransition>newArrayList();

	public ScheduleBuilder(Schedule schedule) {
		this.schedule = schedule;
	}

	public ScheduleBuilder changeActivityAt(int i, Activity activity) {
		this.transitions.add(new ScheduleBuilder.ActivityTransition(i, activity));
		return this;
	}

	public Schedule build() {
		((Set)this.transitions.stream().map(ScheduleBuilder.ActivityTransition::getActivity).collect(Collectors.toSet()))
			.forEach(this.schedule::ensureTimelineExistsFor);
		this.transitions.forEach(activityTransition -> {
			Activity activity = activityTransition.getActivity();
			this.schedule.getAllTimelinesExceptFor(activity).forEach(timeline -> timeline.addKeyframe(activityTransition.getTime(), 0.0F));
			this.schedule.getTimelineFor(activity).addKeyframe(activityTransition.getTime(), 1.0F);
		});
		return this.schedule;
	}

	static class ActivityTransition {
		private final int time;
		private final Activity activity;

		public ActivityTransition(int i, Activity activity) {
			this.time = i;
			this.activity = activity;
		}

		public int getTime() {
			return this.time;
		}

		public Activity getActivity() {
			return this.activity;
		}
	}
}
