package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;

@FunctionalInterface
public interface DataSource {
	Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) throws CommandSyntaxException;
}
