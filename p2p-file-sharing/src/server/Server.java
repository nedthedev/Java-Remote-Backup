package server;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * 
 * @author ned
 *
 */
public class Server {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		ServerSocket socketServer;
		
		// System.setProperty("javax.net.ssl.trustStore", "/home/ned/Programming/Java/p2p-file-sharing/src/server/keystore.jks");
	    // System.setProperty("javax.net.ssl.keyStorePassword", "password");
		
		if(args.length < 1) {
			System.out.println("Need to specify a port for the server to listen to.");
			return;
		}
		else {
			try {
				//SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			    //socketServer = ssf.createServerSocket(Integer.parseInt(args[0]));
				socketServer = new ServerSocket(Integer.parseInt(args[0]));
			}
			catch(NumberFormatException e) {
				System.out.println("Make sure the port is a number.");
				return;
			}
		}
				
		while(true) {
			
			System.out.println("Waiting for connection.");
			
			new ClientHandler(socketServer.accept()).start();
		}
			
	}
}
