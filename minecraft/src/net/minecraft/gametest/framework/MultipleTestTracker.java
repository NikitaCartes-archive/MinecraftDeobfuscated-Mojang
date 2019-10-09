package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import javax.annotation.Nullable;

public class MultipleTestTracker {
	private final Collection<GameTestInfo> tests = Lists.<GameTestInfo>newArrayList();
	@Nullable
	private GameTestListener listener;

	public MultipleTestTracker() {
	}

	public MultipleTestTracker(Collection<GameTestInfo> collection) {
		this.tests.addAll(collection);
	}

	public void add(GameTestInfo gameTestInfo) {
		this.tests.add(gameTestInfo);
		if (this.listener != null) {
			gameTestInfo.addListener(this.listener);
		}
	}

	public void setListener(GameTestListener gameTestListener) {
		this.listener = gameTestListener;
		this.tests.forEach(gameTestInfo -> gameTestInfo.addListener(gameTestListener));
	}

	public int getFailedRequiredCount() {
		return (int)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isRequired).count();
	}

	public int getFailedOptionalCount() {
		return (int)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isOptional).count();
	}

	public int getDoneCount() {
		return (int)this.tests.stream().filter(GameTestInfo::isDone).count();
	}

	public boolean hasFailedRequired() {
		return this.getFailedRequiredCount() > 0;
	}

	public boolean hasFailedOptional() {
		return this.getFailedOptionalCount() > 0;
	}

	public int getTotalCount() {
		return this.tests.size();
	}

	public boolean isDone() {
		return this.getDoneCount() == this.getTotalCount();
	}

	public String getProgressBar() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append('[');
		this.tests.forEach(gameTestInfo -> {
			if (!gameTestInfo.hasStarted()) {
				stringBuffer.append(' ');
			} else if (gameTestInfo.hasSucceeded()) {
				stringBuffer.append('+');
			} else if (gameTestInfo.hasFailed()) {
				stringBuffer.append((char)(gameTestInfo.isRequired() ? 'X' : 'x'));
			} else {
				stringBuffer.append('_');
			}
		});
		stringBuffer.append(']');
		return stringBuffer.toString();
	}

	public String toString() {
		return this.getProgressBar();
	}
}
