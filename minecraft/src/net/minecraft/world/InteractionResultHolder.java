package net.minecraft.world;

public class InteractionResultHolder<T> {
	private final InteractionResult result;
	private final T object;

	public InteractionResultHolder(InteractionResult interactionResult, T object) {
		this.result = interactionResult;
		this.object = object;
	}

	public InteractionResult getResult() {
		return this.result;
	}

	public T getObject() {
		return this.object;
	}

	public static <T> InteractionResultHolder<T> success(T object) {
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, object);
	}

	public static <T> InteractionResultHolder<T> consume(T object) {
		return new InteractionResultHolder<>(InteractionResult.CONSUME, object);
	}

	public static <T> InteractionResultHolder<T> pass(T object) {
		return new InteractionResultHolder<>(InteractionResult.PASS, object);
	}

	public static <T> InteractionResultHolder<T> fail(T object) {
		return new InteractionResultHolder<>(InteractionResult.FAIL, object);
	}
}
