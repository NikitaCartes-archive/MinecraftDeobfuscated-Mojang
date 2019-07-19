package net.minecraft.world.level.newbiome.area;

public interface AreaFactory<A extends Area> {
	A make();
}
