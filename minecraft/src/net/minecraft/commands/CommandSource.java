package net.minecraft.commands;

import net.minecraft.network.chat.Component;

public interface CommandSource {
	CommandSource NULL = new CommandSource() {
		@Override
		public void sendMessage(Component component) {
		}

		@Override
		public boolean acceptsSuccess() {
			return false;
		}

		@Override
		public boolean acceptsFailure() {
			return false;
		}

		@Override
		public boolean shouldInformAdmins() {
			return false;
		}
	};

	void sendMessage(Component component);

	boolean acceptsSuccess();

	boolean acceptsFailure();

	boolean shouldInformAdmins();
}
