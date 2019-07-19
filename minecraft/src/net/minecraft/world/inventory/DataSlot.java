package net.minecraft.world.inventory;

public abstract class DataSlot {
	private int prevValue;

	public static DataSlot forContainer(ContainerData containerData, int i) {
		return new DataSlot() {
			@Override
			public int get() {
				return containerData.get(i);
			}

			@Override
			public void set(int i) {
				containerData.set(i, i);
			}
		};
	}

	public static DataSlot shared(int[] is, int i) {
		return new DataSlot() {
			@Override
			public int get() {
				return is[i];
			}

			@Override
			public void set(int i) {
				is[i] = i;
			}
		};
	}

	public static DataSlot standalone() {
		return new DataSlot() {
			private int value;

			@Override
			public int get() {
				return this.value;
			}

			@Override
			public void set(int i) {
				this.value = i;
			}
		};
	}

	public abstract int get();

	public abstract void set(int i);

	public boolean checkAndClearUpdateFlag() {
		int i = this.get();
		boolean bl = i != this.prevValue;
		this.prevValue = i;
		return bl;
	}
}
