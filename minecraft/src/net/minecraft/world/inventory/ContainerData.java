package net.minecraft.world.inventory;

public interface ContainerData {
	int get(int i);

	void set(int i, int j);

	int getCount();
}
