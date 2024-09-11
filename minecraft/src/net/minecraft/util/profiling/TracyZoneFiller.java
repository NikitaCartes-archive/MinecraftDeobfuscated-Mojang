package net.minecraft.util.profiling;

import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.slf4j.Logger;

public class TracyZoneFiller implements ProfilerFiller {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(Option.RETAIN_CLASS_REFERENCE), 5);
	private final List<com.mojang.jtracy.Zone> activeZones = new ArrayList();
	private final Map<String, TracyZoneFiller.PlotAndValue> plots = new HashMap();
	private final String name = Thread.currentThread().getName();

	@Override
	public void startTick() {
	}

	@Override
	public void endTick() {
		for (TracyZoneFiller.PlotAndValue plotAndValue : this.plots.values()) {
			plotAndValue.set(0);
		}
	}

	@Override
	public void push(String string) {
		String string2 = "";
		String string3 = "";
		int i = 0;
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			Optional<StackFrame> optional = (Optional<StackFrame>)STACK_WALKER.walk(
				stream -> stream.filter(
							stackFramex -> stackFramex.getDeclaringClass() != TracyZoneFiller.class && stackFramex.getDeclaringClass() != ProfilerFiller.CombinedProfileFiller.class
						)
						.findFirst()
			);
			if (optional.isPresent()) {
				StackFrame stackFrame = (StackFrame)optional.get();
				string2 = stackFrame.getMethodName();
				string3 = stackFrame.getFileName();
				i = stackFrame.getLineNumber();
			}
		}

		com.mojang.jtracy.Zone zone = TracyClient.beginZone(string, string2, string3, i);
		this.activeZones.add(zone);
	}

	@Override
	public void push(Supplier<String> supplier) {
		this.push((String)supplier.get());
	}

	@Override
	public void pop() {
		if (this.activeZones.isEmpty()) {
			LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
		} else {
			com.mojang.jtracy.Zone zone = (com.mojang.jtracy.Zone)this.activeZones.removeLast();
			zone.close();
		}
	}

	@Override
	public void popPush(String string) {
		this.pop();
		this.push(string);
	}

	@Override
	public void popPush(Supplier<String> supplier) {
		this.pop();
		this.push((String)supplier.get());
	}

	@Override
	public void markForCharting(MetricCategory metricCategory) {
	}

	@Override
	public void incrementCounter(String string, int i) {
		((TracyZoneFiller.PlotAndValue)this.plots.computeIfAbsent(string, string2 -> new TracyZoneFiller.PlotAndValue(this.name + " " + string))).add(i);
	}

	@Override
	public void incrementCounter(Supplier<String> supplier, int i) {
		this.incrementCounter((String)supplier.get(), i);
	}

	private com.mojang.jtracy.Zone activeZone() {
		return (com.mojang.jtracy.Zone)this.activeZones.getLast();
	}

	@Override
	public void addZoneText(String string) {
		this.activeZone().addText(string);
	}

	@Override
	public void addZoneValue(long l) {
		this.activeZone().addValue(l);
	}

	@Override
	public void setZoneColor(int i) {
		this.activeZone().setColor(i);
	}

	static final class PlotAndValue {
		private final Plot plot;
		private int value;

		PlotAndValue(String string) {
			this.plot = TracyClient.createPlot(string);
			this.value = 0;
		}

		void set(int i) {
			this.value = i;
			this.plot.setValue((double)i);
		}

		void add(int i) {
			this.set(this.value + i);
		}
	}
}
