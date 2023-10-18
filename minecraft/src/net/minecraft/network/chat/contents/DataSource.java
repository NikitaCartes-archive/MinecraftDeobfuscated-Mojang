package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.StringRepresentable;

public interface DataSource {
	MapCodec<DataSource> CODEC = ComponentSerialization.createLegacyComponentMatcher(
		new DataSource.Type[]{EntityDataSource.TYPE, BlockDataSource.TYPE, StorageDataSource.TYPE}, DataSource.Type::codec, DataSource::type, "source"
	);

	Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) throws CommandSyntaxException;

	DataSource.Type<?> type();

	public static record Type<T extends DataSource>(MapCodec<T> codec, String id) implements StringRepresentable {
		@Override
		public String getSerializedName() {
			return this.id;
		}
	}
}
