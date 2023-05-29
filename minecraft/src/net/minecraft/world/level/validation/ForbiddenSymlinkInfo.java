package net.minecraft.world.level.validation;

import java.nio.file.Path;

public record ForbiddenSymlinkInfo(Path link, Path target) {
}
