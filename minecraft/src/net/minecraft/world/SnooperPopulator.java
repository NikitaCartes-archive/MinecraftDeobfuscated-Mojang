package net.minecraft.world;

public interface SnooperPopulator {
	void populateSnooper(Snooper snooper);

	void populateSnooperInitial(Snooper snooper);

	boolean isSnooperEnabled();
}
