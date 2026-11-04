package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public static void main(String[] args) {
		InetAddress addr;
		Socket client;
		try{
			client = new Socket("localhost", 4444);
			var in = new BufferedReader(
				new InputStreamReader(
					System.in));
			System.out.println(in.readLine());
			var out = new PrintWriter(client.getOutputStream(),true);
			out.println("est-ce que tu me sens hé ho");
			String userInput;
			while ((userInput = in.readLine()) != null) {
				out.println(userInput);				
			}
		}
		catch(UnknownHostException e){
			e.printStackTrace();
		}
		catch(IOException ioe){}
	}
}