package net.minecraft.server.level;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public interface ChunkResult<T> {
	static <T> ChunkResult<T> of(T object) {
		return new ChunkResult.Success<>(object);
	}

	static <T> ChunkResult<T> error(String string) {
		return error((Supplier<String>)(() -> string));
	}

	static <T> ChunkResult<T> error(Supplier<String> supplier) {
		return new ChunkResult.Fail<>(supplier);
	}

	boolean isSuccess();

	@Nullable
	T orElse(@Nullable T object);

	@Nullable
	static <R> R orElse(ChunkResult<? extends R> chunkResult, @Nullable R object) {
		R object2 = (R)chunkResult.orElse(null);
		return object2 != null ? object2 : object;
	}

	@Nullable
	String getError();

	ChunkResult<T> ifSuccess(Consumer<T> consumer);

	<R> ChunkResult<R> map(Function<T, R> function);

	<E extends Throwable> T orElseThrow(Supplier<E> supplier) throws E;

	public static record Fail<T>(Supplier<String> error) implements ChunkResult<T> {
		@Override
		public boolean isSuccess() {
			return false;
		}

		@Nullable
		@Override
		public T orElse(@Nullable T object) {
			return object;
		}

		@Override
		public String getError() {
			return (String)this.error.get();
		}

		@Override
		public ChunkResult<T> ifSuccess(Consumer<T> consumer) {
			return this;
		}

		@Override
		public <R> ChunkResult<R> map(Function<T, R> function) {
			return new ChunkResult.Fail(this.error);
		}

		@Override
		public <E extends Throwable> T orElseThrow(Supplier<E> supplier) throws E {
			throw (Throwable)supplier.get();
		}
	}

	public static record Success<T>(T value) implements ChunkResult<T> {
		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public T orElse(@Nullable T object) {
			return this.value;
		}

		@Nullable
		@Override
		public String getError() {
			return null;
		}

		@Override
		public ChunkResult<T> ifSuccess(Consumer<T> consumer) {
			consumer.accept(this.value);
			return this;
		}

		@Override
		public <R> ChunkResult<R> map(Function<T, R> function) {
			return (ChunkResult<R>)(new ChunkResult.Success<>(function.apply(this.value)));
		}

		@Override
		public <E extends Throwable> T orElseThrow(Supplier<E> supplier) throws E {
			return this.value;
		}
	}
}
