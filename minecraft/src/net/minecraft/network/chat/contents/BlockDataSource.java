package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public record BlockDataSource(String posPattern, @Nullable Coordinates compiledPos) implements DataSource {
	public BlockDataSource(String string) {
		this(string, compilePos(string));
	}

	@Nullable
	private static Coordinates compilePos(String string) {
		try {
			return BlockPosArgument.blockPos().parse(new StringReader(string));
		} catch (CommandSyntaxException var2) {
			return null;
		}
	}

	@Override
	public Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) {
		if (this.compiledPos != null) {
			ServerLevel serverLevel = commandSourceStack.getLevel();
			BlockPos blockPos = this.compiledPos.getBlockPos(commandSourceStack);
			if (serverLevel.isLoaded(blockPos)) {
				BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
				if (blockEntity != null) {
					return Stream.of(blockEntity.saveWithFullMetadata());
				}
			}
		}

		return Stream.empty();
	}

	public String toString() {
		return "block=" + this.posPattern;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof BlockDataSource blockDataSource && this.posPattern.equals(blockDataSource.posPattern)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		return this.posPattern.hashCode();
	}
}
