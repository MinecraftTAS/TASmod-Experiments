package com.minecrafttas.tasmod.networking;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.exceptions.ClientAlreadyRunningException;
import com.minecrafttas.tasmod.networking.packets.Packet;

import net.minecraft.client.MinecraftClient;

/**
 * The TASmod itself has a custom connection running next to the minecraft one.
 * It's necessary since the integrated packet connection is tick-based and therefore cannot communicate inbetween ticks.
 *
 * IMPLEMENTATION NOTICE:
 * The client creates a separate thread to run off so that it's non-blocking. Therefore it uses a queue for outgoing packets.
 * @author Pancake
 */
public class Client {

	/**
	 * This is the thread that runs the client. It will exit once the client has disconnected.
	 */
	private static Thread instance;

	/**
	 * This is the client socket. Interrupting it will always close the connection and end the thread (instance).
	 */
	private static Socket clientSocket;

	/**
	 * This queue of packets is going to be sent by another thread.
	 */
	private static BlockingQueue<Packet> packetsToSend = new LinkedBlockingQueue<>(); // Initialize with something so this cannot cause a npe

	/**
	 * Adds a packet to the queue of packets to send
	 * @param packet Packet to send
	 */
	public static void sendPacket(Packet packet) {
		if (Client.instance == null)
			return;
		if (!Client.instance.isAlive())
			return;
		Client.packetsToSend.add(packet);
	}

	/**
	 * Once the client enters connect phase a separate client thread is created.
	 *
	 * IMPLEMENTATION NOTICE:
	 * Called from ClientTASmod.
	 *
	 * @throws IOException Fatal Exception, the socket couldn't be closed
	 * @throws If the last client wasn't succesfully shut down it will throw an exception and forcefully shut down the client
	 */
	public static void createClient() throws ClientAlreadyRunningException, IOException {
		boolean isRunning = Client.instance == null ? false : Client.instance.isAlive();
		// Cancel the currently running server
		if (isRunning)
			Client.clientSocket.close();

		// Clear the list of packets to send
		Client.packetsToSend = new LinkedBlockingQueue<>();
		// Start a client socket
		Client.instance = new Thread(() -> {
			// Find the address of the server we are currently connected to (in minecraft's connection)
			String serverIp = MinecraftClient.getInstance().isIntegratedServerRunning() ? "127.0.0.1" : MinecraftClient.getInstance().getCurrentServerEntry().address;
			if (serverIp.contains(":"))
				serverIp = serverIp.split(Pattern.quote(":"))[0];
			// Connect to the server
			try(Socket clientSocket = new Socket(serverIp, 3111)) {
				Client.clientSocket = clientSocket;
				// Handle the socket
				CommonHandler.handleSocket(clientSocket, Client.packetsToSend); // this will create a new thread for outstream and use the current thread for instream
			} catch (EOFException | SocketException | InterruptedIOException exception) {
				// The custom TASmod client was closed and the end of stream was reached. The socket was shut down properly.
				TASmod.LOGGER.debug("Custom TASmod client was shutdown");
			} catch (Exception exception) {
				TASmod.LOGGER.error("Custom TASmod client was unexpectedly shutdown {}", exception);
			}
		});
		Client.instance.setDaemon(true); // If daemon is set, the jvm will quit without waiting for this thread to finish
		Client.instance.start();

		// Make sure to throw an exception if the server was running
		if (isRunning)
			throw new ClientAlreadyRunningException();
	}

	/**
	 * Kills the custom TASmod client if is running
	 * @throws IOException Thrown if the socket couldn't be closed
	 */
	public static void killClient() throws IOException {
		if (Client.instance != null)
			Client.clientSocket.close();
	}

}
