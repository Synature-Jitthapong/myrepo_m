package com.synature.mpos.seconddisplay;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class ClientSocket{

	public static final int DEFAULT_PORT = 6600;
	
	private String mServerIp;
	private int mPort = DEFAULT_PORT;
	private Socket mClientSocket;
	private Timer mTimerOpenSocket;
	
	public ClientSocket(String ip, int port){
		mServerIp = ip;
		if(port != 0)
			mPort = port;
	}
	
	public void send(String json) throws IOException{
		 PrintWriter out = new PrintWriter(
				 new BufferedWriter(
						 new OutputStreamWriter(mClientSocket.getOutputStream())),true);
         out.println(json);
	}
	
	public void connect(){
		try {
			InetAddress addr = InetAddress.getByName(mServerIp);
			mClientSocket = new Socket(addr, mPort); 
			stopTimer();
		} catch (UnknownHostException e) {
			tryOpenSocket();
			e.printStackTrace();
		} catch (IOException e) {
			tryOpenSocket();
			e.printStackTrace();
		}
	}
	
	private void stopTimer(){
		if(mTimerOpenSocket != null){
			mTimerOpenSocket.cancel();
			mTimerOpenSocket.purge();
		}
	}
	
	private void tryOpenSocket(){
		mTimerOpenSocket = new Timer();
		mTimerOpenSocket.schedule(new OpenSocketTimer(), 1000, 1000);
	}
	
	class OpenSocketTimer extends TimerTask{

		@Override
		public void run() {
			connect();
		}
		
	}
}
