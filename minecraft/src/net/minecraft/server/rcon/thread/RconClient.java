package net.minecraft.server.rcon.thread;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.rcon.PktUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RconClient extends GenericThread {
	private static final Logger LOGGER = LogManager.getLogger();
	private boolean authed;
	private Socket client;
	private final byte[] buf = new byte[1460];
	private final String rconPassword;
	private final ServerInterface serverInterface;

	RconClient(ServerInterface serverInterface, String string, Socket socket) {
		super("RCON Client " + socket.getInetAddress());
		this.serverInterface = serverInterface;
		this.client = socket;

		try {
			this.client.setSoTimeout(0);
		} catch (Exception var5) {
			this.running = false;
		}

		this.rconPassword = string;
	}

	public void run() {
		try {
			try {
				while (this.running) {
					BufferedInputStream bufferedInputStream = new BufferedInputStream(this.client.getInputStream());
					int i = bufferedInputStream.read(this.buf, 0, 1460);
					if (10 > i) {
						return;
					}

					int j = 0;
					int k = PktUtils.intFromByteArray(this.buf, 0, i);
					if (k != i - 4) {
						return;
					}

					j += 4;
					int l = PktUtils.intFromByteArray(this.buf, j, i);
					j += 4;
					int m = PktUtils.intFromByteArray(this.buf, j);
					j += 4;
					switch (m) {
						case 2:
							if (this.authed) {
								String string2 = PktUtils.stringFromByteArray(this.buf, j, i);

								try {
									this.sendCmdResponse(l, this.serverInterface.runCommand(string2));
								} catch (Exception var16) {
									this.sendCmdResponse(l, "Error executing: " + string2 + " (" + var16.getMessage() + ")");
								}
								break;
							}

							this.sendAuthFailure();
							break;
						case 3:
							String string = PktUtils.stringFromByteArray(this.buf, j, i);
							j += string.length();
							if (!string.isEmpty() && string.equals(this.rconPassword)) {
								this.authed = true;
								this.send(l, 2, "");
								break;
							}

							this.authed = false;
							this.sendAuthFailure();
							break;
						default:
							this.sendCmdResponse(l, String.format("Unknown request %s", Integer.toHexString(m)));
					}
				}

				return;
			} catch (SocketTimeoutException var17) {
			} catch (IOException var18) {
			} catch (Exception var19) {
				LOGGER.error("Exception whilst parsing RCON input", (Throwable)var19);
			}
		} finally {
			this.closeSocket();
			LOGGER.info("Thread {} shutting down", this.name);
			this.running = false;
		}
	}

	private void send(int i, int j, String string) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1248);
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		byte[] bs = string.getBytes("UTF-8");
		dataOutputStream.writeInt(Integer.reverseBytes(bs.length + 10));
		dataOutputStream.writeInt(Integer.reverseBytes(i));
		dataOutputStream.writeInt(Integer.reverseBytes(j));
		dataOutputStream.write(bs);
		dataOutputStream.write(0);
		dataOutputStream.write(0);
		this.client.getOutputStream().write(byteArrayOutputStream.toByteArray());
	}

	private void sendAuthFailure() throws IOException {
		this.send(-1, 2, "");
	}

	private void sendCmdResponse(int i, String string) throws IOException {
		int j = string.length();

		do {
			int k = 4096 <= j ? 4096 : j;
			this.send(i, 0, string.substring(0, k));
			string = string.substring(k);
			j = string.length();
		} while (0 != j);
	}

	@Override
	public void stop() {
		this.running = false;
		this.closeSocket();
		super.stop();
	}

	private void closeSocket() {
		if (null != this.client) {
			try {
				this.client.close();
			} catch (IOException var2) {
				LOGGER.warn("Failed to close socket", (Throwable)var2);
			}

			this.client = null;
		}
	}
}
