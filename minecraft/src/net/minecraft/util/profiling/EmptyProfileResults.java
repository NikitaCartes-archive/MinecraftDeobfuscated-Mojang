package net.minecraft.util.profiling;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class EmptyProfileResults implements ProfileResults {
	public static final EmptyProfileResults EMPTY = new EmptyProfileResults();

	private EmptyProfileResults() {
	}

	@Override
	public List<ResultField> getTimes(String string) {
		return Collections.emptyList();
	}

	@Override
	public boolean saveResults(Path path) {
		return false;
	}

	@Override
	public long getStartTimeNano() {
		return 0L;
	}

	@Override
	public int getStartTimeTicks() {
		return 0;
	}

	@Override
	public long getEndTimeNano() {
		return 0L;
	}

	@Override
	public int getEndTimeTicks() {
		return 0;
	}

	@Override
	public String getProfilerResults() {
		return "";
	}
}
