package net.minecraft.world.level;

public enum TickPriority {
	EXTREMELY_HIGH(-3),
	VERY_HIGH(-2),
	HIGH(-1),
	NORMAL(0),
	LOW(1),
	VERY_LOW(2),
	EXTREMELY_LOW(3);

	private final int value;

	private TickPriority(int j) {
		this.value = j;
	}

	public static TickPriority byValue(int i) {
		for (TickPriority tickPriority : values()) {
			if (tickPriority.value == i) {
				return tickPriority;
			}
		}

		return i < EXTREMELY_HIGH.value ? EXTREMELY_HIGH : EXTREMELY_LOW;
	}

	public int getValue() {
		return this.value;
	}
}
