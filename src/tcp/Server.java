package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) {
		ServerSocket server;
		Socket client;
		try{
			server = new ServerSocket(4444);
			client = server.accept();
			var out = new PrintWriter(client.getOutputStream(),true);
			out.println("est-ce que tu m'entends hé ho");
			var in = new BufferedReader(
				new InputStreamReader(
					client.getInputStream()));
			System.out.println(in.readLine());
		}
		catch(IOException i){
			System.out.println("Socket 4444 couldn't be reached");
		}
	}
}