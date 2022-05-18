package net.minecraft.util;

import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.logging.LogUtils;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import org.slf4j.Logger;

public interface SignatureValidator {
	SignatureValidator NO_VALIDATION = (signatureUpdater, bs) -> true;
	Logger LOGGER = LogUtils.getLogger();

	boolean validate(SignatureUpdater signatureUpdater, byte[] bs);

	default boolean validate(byte[] bs, byte[] cs) {
		return this.validate(output -> output.update(bs), cs);
	}

	private static boolean verifySignature(SignatureUpdater signatureUpdater, byte[] bs, Signature signature) throws SignatureException {
		signatureUpdater.update(signature::update);
		return signature.verify(bs);
	}

	static SignatureValidator from(PublicKey publicKey, String string) {
		return (signatureUpdater, bs) -> {
			try {
				Signature signature = Signature.getInstance(string);
				signature.initVerify(publicKey);
				return verifySignature(signatureUpdater, bs, signature);
			} catch (Exception var5) {
				LOGGER.error("Failed to verify signature", (Throwable)var5);
				return false;
			}
		};
	}

	static SignatureValidator from(ServicesKeyInfo servicesKeyInfo) {
		return (signatureUpdater, bs) -> {
			Signature signature = servicesKeyInfo.signature();

			try {
				return verifySignature(signatureUpdater, bs, signature);
			} catch (SignatureException var5) {
				LOGGER.error("Failed to verify Services signature", (Throwable)var5);
				return false;
			}
		};
	}
}
