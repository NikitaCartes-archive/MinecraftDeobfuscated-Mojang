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
}
