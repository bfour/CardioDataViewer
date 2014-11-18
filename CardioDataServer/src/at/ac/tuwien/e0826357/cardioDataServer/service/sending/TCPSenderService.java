package at.ac.tuwien.e0826357.cardioDataServer.service.sending;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import at.ac.tuwien.e0826357.cardioDataViewer.service.ServiceException;

public class TCPSenderService {

	private static TCPSenderService instance;
	private ServerSocket serverSocket;
	private PrintWriter outboundStream;

	public TCPSenderService(int port) throws ServiceException {
		try {
			this.serverSocket = new ServerSocket(port);
			System.out
					.println("waiting for connection request on port " + port);
			Socket socket = serverSocket.accept();
			System.out.println("connection request from "
					+ socket.getInetAddress().getHostAddress() + " accepted");
			this.outboundStream = new PrintWriter(socket.getOutputStream(),
					true);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	public static TCPSenderService getInstance(int port)
			throws ServiceException {
		if (instance == null)
			instance = new TCPSenderService(port);
		return instance;
	}

	public void start() {

	}

	public void stop() {
		outboundStream.close();
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("sorry, failed to close server socket: "
					+ e.getMessage());
		}
	}

	public void tell(String message) {
		outboundStream.print(message);
	}

}
