package net.minecraft.network;

import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public record DisconnectionDetails(Component reason, Optional<Path> report, Optional<String> bugReportLink) {
	public DisconnectionDetails(Component component) {
		this(component, Optional.empty(), Optional.empty());
	}
}
