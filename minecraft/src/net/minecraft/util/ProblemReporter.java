package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public interface ProblemReporter {
	ProblemReporter forChild(String string);

	void report(String string);

	public static class Collector implements ProblemReporter {
		private final Multimap<String, String> problems;
		private final Supplier<String> path;
		@Nullable
		private String pathCache;

		public Collector() {
			this(HashMultimap.create(), () -> "");
		}

		private Collector(Multimap<String, String> multimap, Supplier<String> supplier) {
			this.problems = multimap;
			this.path = supplier;
		}

		private String getPath() {
			if (this.pathCache == null) {
				this.pathCache = (String)this.path.get();
			}

			return this.pathCache;
		}

		@Override
		public ProblemReporter forChild(String string) {
			return new ProblemReporter.Collector(this.problems, () -> this.getPath() + string);
		}

		@Override
		public void report(String string) {
			this.problems.put(this.getPath(), string);
		}

		public Multimap<String, String> get() {
			return ImmutableMultimap.copyOf(this.problems);
		}
	}
}
