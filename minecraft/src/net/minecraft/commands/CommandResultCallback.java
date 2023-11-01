package net.minecraft.commands;

@FunctionalInterface
public interface CommandResultCallback {
	CommandResultCallback EMPTY = new CommandResultCallback() {
		@Override
		public void onResult(boolean bl, int i) {
		}

		public String toString() {
			return "<empty>";
		}
	};

	void onResult(boolean bl, int i);

	default void onSuccess(int i) {
		this.onResult(true, i);
	}

	default void onFailure() {
		this.onResult(false, 0);
	}

	static CommandResultCallback chain(CommandResultCallback commandResultCallback, CommandResultCallback commandResultCallback2) {
		if (commandResultCallback == EMPTY) {
			return commandResultCallback2;
		} else {
			return commandResultCallback2 == EMPTY ? commandResultCallback : (bl, i) -> {
				commandResultCallback.onResult(bl, i);
				commandResultCallback2.onResult(bl, i);
			};
		}
	}
}
