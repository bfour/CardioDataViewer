/*
 * Copyright 2016 Florian Pollak (fpdevelop@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.ac.tuwien.e0826357.cardioapp.server.service.sending;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import at.ac.tuwien.e0826357.cardioapp.commons.service.ServiceException;

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
