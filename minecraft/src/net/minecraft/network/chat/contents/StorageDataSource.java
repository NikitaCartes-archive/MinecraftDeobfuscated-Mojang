package net.minecraft.network.chat.contents;

import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record StorageDataSource(ResourceLocation id) implements DataSource {
	@Override
	public Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) {
		CompoundTag compoundTag = commandSourceStack.getServer().getCommandStorage().get(this.id);
		return Stream.of(compoundTag);
	}

	public String toString() {
		return "storage=" + this.id;
	}
}
