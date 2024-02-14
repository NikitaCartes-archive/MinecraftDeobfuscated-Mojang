package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.world.level.block.Rotation;

public record TestFunction(
	String batchName,
	String testName,
	String structureName,
	Rotation rotation,
	int maxTicks,
	long setupTicks,
	boolean required,
	int maxAttempts,
	int requiredSuccesses,
	boolean skyAccess,
	Consumer<GameTestHelper> function
) {
	public TestFunction(String string, String string2, String string3, int i, long l, boolean bl, Consumer<GameTestHelper> consumer) {
		this(string, string2, string3, Rotation.NONE, i, l, bl, 1, 1, false, consumer);
	}

	public TestFunction(String string, String string2, String string3, Rotation rotation, int i, long l, boolean bl, Consumer<GameTestHelper> consumer) {
		this(string, string2, string3, rotation, i, l, bl, 1, 1, false, consumer);
	}

	public void run(GameTestHelper gameTestHelper) {
		this.function.accept(gameTestHelper);
	}

	public String toString() {
		return this.testName;
	}

	public boolean isFlaky() {
		return this.maxAttempts > 1;
	}
}
