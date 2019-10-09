package net.minecraft.gametest.framework;

import java.util.function.Consumer;

public class TestFunction {
	private final String batchName;
	private final String testName;
	private final String structureName;
	private final boolean required;
	private final Consumer<GameTestHelper> function;
	private final int maxTicks;
	private final long setupTicks;

	public void run(GameTestHelper gameTestHelper) {
		this.function.accept(gameTestHelper);
	}

	public String getTestName() {
		return this.testName;
	}

	public String getStructureName() {
		return this.structureName;
	}

	public String toString() {
		return this.testName;
	}

	public int getMaxTicks() {
		return this.maxTicks;
	}

	public boolean isRequired() {
		return this.required;
	}

	public String getBatchName() {
		return this.batchName;
	}

	public long getSetupTicks() {
		return this.setupTicks;
	}
}
