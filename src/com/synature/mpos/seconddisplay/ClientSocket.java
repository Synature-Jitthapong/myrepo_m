package com.synature.mpos.seconddisplay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocket implements ISocketConnection {
	private Socket mConnect;
	private PrintWriter mWriter;
	private BufferedReader mReader;

	public ClientSocket(String ip, int port) {
		try {
			InetAddress iNetAddr = InetAddress.getByName(ip);
			mConnect = new Socket(iNetAddr, port);
			mWriter = new PrintWriter(new OutputStreamWriter(
					mConnect.getOutputStream()));
			mReader = new BufferedReader(new InputStreamReader(
					mConnect.getInputStream()));
		} catch (UnknownHostException ex) {
			
		} catch (IOException ex) {
			
		}
	}

	@Override
	public void send(String msg) {
		mWriter.println(msg);
		mWriter.flush();
	}

	@Override
	public String receive() {
		try {
			return mReader.readLine();
		} catch (IOException ex) {
			return null;
		}
	}

}
