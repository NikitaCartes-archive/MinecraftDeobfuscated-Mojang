package net.minecraft.util.profiling;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilledProfileResults implements ProfileResults {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<String, Long> times;
	private final Map<String, Long> counts;
	private final long startTimeNano;
	private final int startTimeTicks;
	private final long endTimeNano;
	private final int endTimeTicks;
	private final int tickDuration;

	public FilledProfileResults(Map<String, Long> map, Map<String, Long> map2, long l, int i, long m, int j) {
		this.times = map;
		this.counts = map2;
		this.startTimeNano = l;
		this.startTimeTicks = i;
		this.endTimeNano = m;
		this.endTimeTicks = j;
		this.tickDuration = j - i;
	}

	@Override
	public List<ResultField> getTimes(String string) {
		String string2 = string;
		long l = this.times.containsKey("root") ? (Long)this.times.get("root") : 0L;
		long m = (Long)this.times.getOrDefault(string, -1L);
		long n = (Long)this.counts.getOrDefault(string, 0L);
		List<ResultField> list = Lists.<ResultField>newArrayList();
		if (!string.isEmpty()) {
			string = string + '\u001e';
		}

		long o = 0L;

		for (String string3 : this.times.keySet()) {
			if (string3.length() > string.length() && string3.startsWith(string) && string3.indexOf(30, string.length() + 1) < 0) {
				o += this.times.get(string3);
			}
		}

		float f = (float)o;
		if (o < m) {
			o = m;
		}

		if (l < o) {
			l = o;
		}

		Set<String> set = Sets.<String>newHashSet(this.times.keySet());
		set.addAll(this.counts.keySet());

		for (String string4 : set) {
			if (string4.length() > string.length() && string4.startsWith(string) && string4.indexOf(30, string.length() + 1) < 0) {
				long p = (Long)this.times.getOrDefault(string4, 0L);
				double d = (double)p * 100.0 / (double)o;
				double e = (double)p * 100.0 / (double)l;
				String string5 = string4.substring(string.length());
				long q = (Long)this.counts.getOrDefault(string4, 0L);
				list.add(new ResultField(string5, d, e, q));
			}
		}

		for (String string4x : this.times.keySet()) {
			this.times.put(string4x, (Long)this.times.get(string4x) * 999L / 1000L);
		}

		if ((float)o > f) {
			list.add(new ResultField("unspecified", (double)((float)o - f) * 100.0 / (double)o, (double)((float)o - f) * 100.0 / (double)l, n));
		}

		Collections.sort(list);
		list.add(0, new ResultField(string2, 100.0, (double)o * 100.0 / (double)l, n));
		return list;
	}

	@Override
	public long getStartTimeNano() {
		return this.startTimeNano;
	}

	@Override
	public int getStartTimeTicks() {
		return this.startTimeTicks;
	}

	@Override
	public long getEndTimeNano() {
		return this.endTimeNano;
	}

	@Override
	public int getEndTimeTicks() {
		return this.endTimeTicks;
	}

	@Override
	public boolean saveResults(File file) {
		file.getParentFile().mkdirs();
		Writer writer = null;

		boolean var4;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			writer.write(this.getProfilerResults(this.getNanoDuration(), this.getTickDuration()));
			return true;
		} catch (Throwable var8) {
			LOGGER.error("Could not save profiler results to {}", file, var8);
			var4 = false;
		} finally {
			IOUtils.closeQuietly(writer);
		}

		return var4;
	}

	protected String getProfilerResults(long l, int i) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("---- Minecraft Profiler Results ----\n");
		stringBuilder.append("// ");
		stringBuilder.append(getComment());
		stringBuilder.append("\n\n");
		stringBuilder.append("Version: ").append(SharedConstants.getCurrentVersion().getId()).append('\n');
		stringBuilder.append("Time span: ").append(l / 1000000L).append(" ms\n");
		stringBuilder.append("Tick span: ").append(i).append(" ticks\n");
		stringBuilder.append("// This is approximately ")
			.append(String.format(Locale.ROOT, "%.2f", (float)i / ((float)l / 1.0E9F)))
			.append(" ticks per second. It should be ")
			.append(20)
			.append(" ticks per second\n\n");
		stringBuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
		this.appendProfilerResults(0, "root", stringBuilder);
		stringBuilder.append("--- END PROFILE DUMP ---\n\n");
		return stringBuilder.toString();
	}

	private void appendProfilerResults(int i, String string, StringBuilder stringBuilder) {
		List<ResultField> list = this.getTimes(string);
		if (list.size() >= 3) {
			for (int j = 1; j < list.size(); j++) {
				ResultField resultField = (ResultField)list.get(j);
				stringBuilder.append(String.format("[%02d] ", i));

				for (int k = 0; k < i; k++) {
					stringBuilder.append("|   ");
				}

				stringBuilder.append(resultField.name)
					.append('(')
					.append(resultField.count)
					.append('/')
					.append(String.format(Locale.ROOT, "%.0f", (float)resultField.count / (float)this.tickDuration))
					.append(')')
					.append(" - ")
					.append(String.format(Locale.ROOT, "%.2f", resultField.percentage))
					.append("%/")
					.append(String.format(Locale.ROOT, "%.2f", resultField.globalPercentage))
					.append("%\n");
				if (!"unspecified".equals(resultField.name)) {
					try {
						this.appendProfilerResults(i + 1, string + '\u001e' + resultField.name, stringBuilder);
					} catch (Exception var8) {
						stringBuilder.append("[[ EXCEPTION ").append(var8).append(" ]]");
					}
				}
			}
		}
	}

	private static String getComment() {
		String[] strings = new String[]{
			"Shiny numbers!",
			"Am I not running fast enough? :(",
			"I'm working as hard as I can!",
			"Will I ever be good enough for you? :(",
			"Speedy. Zoooooom!",
			"Hello world",
			"40% better than a crash report.",
			"Now with extra numbers",
			"Now with less numbers",
			"Now with the same numbers",
			"You should add flames to things, it makes them go faster!",
			"Do you feel the need for... optimization?",
			"*cracks redstone whip*",
			"Maybe if you treated it better then it'll have more motivation to work faster! Poor server."
		};

		try {
			return strings[(int)(Util.getNanos() % (long)strings.length)];
		} catch (Throwable var2) {
			return "Witty comment unavailable :(";
		}
	}

	@Override
	public int getTickDuration() {
		return this.tickDuration;
	}
}
