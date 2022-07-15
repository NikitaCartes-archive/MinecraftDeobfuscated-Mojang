package net.minecraft.server.network;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public record FilteredText<T>(T raw, @Nullable T filtered) {
	public static final FilteredText<String> EMPTY_STRING = passThrough("");

	public static <T> FilteredText<T> passThrough(T object) {
		return new FilteredText<>(object, object);
	}

	public static <T> FilteredText<T> fullyFiltered(T object) {
		return new FilteredText<>(object, null);
	}

	public <U> FilteredText<U> map(Function<T, U> function) {
		return this.map(function, function);
	}

	public <U> FilteredText<U> map(Function<T, U> function, Function<T, U> function2) {
		return (FilteredText<U>)(new FilteredText<>(function.apply(this.raw), Util.mapNullable(this.filtered, function2)));
	}

	public <U> FilteredText<U> mapWithEquality(Function<T, U> function, Function<T, U> function2) {
		U object = (U)function.apply(this.raw);
		return this.raw.equals(this.filtered) ? passThrough(object) : new FilteredText<>(object, Util.mapNullable(this.filtered, function2));
	}

	public boolean isFiltered() {
		return !this.raw.equals(this.filtered);
	}

	public boolean isFullyFiltered() {
		return this.filtered == null;
	}

	public T filteredOrElse(T object) {
		return this.filtered != null ? this.filtered : object;
	}

	@Nullable
	public T filter(ServerPlayer serverPlayer, ServerPlayer serverPlayer2) {
		return serverPlayer.shouldFilterMessageTo(serverPlayer2) ? this.filtered : this.raw;
	}

	@Nullable
	public T filter(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer) {
		ServerPlayer serverPlayer2 = commandSourceStack.getPlayer();
		return serverPlayer2 != null ? this.filter(serverPlayer2, serverPlayer) : this.raw;
	}

	@Nullable
	public T select(boolean bl) {
		return bl ? this.filtered : this.raw;
	}
}
