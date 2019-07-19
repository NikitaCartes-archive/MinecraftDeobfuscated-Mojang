package net.minecraft.world.level.chunk;

interface PaletteResize<T> {
	int onResize(int i, T object);
}
