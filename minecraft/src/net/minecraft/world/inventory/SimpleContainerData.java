package net.minecraft.world.inventory;

public class SimpleContainerData implements ContainerData {
	private final int[] ints;

	public SimpleContainerData(int i) {
		this.ints = new int[i];
	}

	@Override
	public int get(int i) {
		return this.ints[i];
	}

	@Override
	public void set(int i, int j) {
		this.ints[i] = j;
	}

	@Override
	public int getCount() {
		return this.ints.length;
	}
}
