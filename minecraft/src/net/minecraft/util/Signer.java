package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.security.PrivateKey;
import java.security.Signature;
import org.slf4j.Logger;

public interface Signer {
	Logger LOGGER = LogUtils.getLogger();

	byte[] sign(SignatureUpdater signatureUpdater);

	default byte[] sign(byte[] bs) {
		return this.sign(output -> output.update(bs));
	}

	static Signer from(PrivateKey privateKey, String string) {
		return signatureUpdater -> {
			try {
				Signature signature = Signature.getInstance(string);
				signature.initSign(privateKey);
				signatureUpdater.update(signature::update);
				return signature.sign();
			} catch (Exception var4) {
				throw new IllegalStateException("Failed to sign message", var4);
			}
		};
	}
}
