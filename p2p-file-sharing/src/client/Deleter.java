package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author ned
 *
 */
public class Deleter {

	private int filesDownloaded;
	private DataOutputStream transmitter; 
	private DataInputStream receiver;
	
	public Deleter(String ip, int port, Socket sock) throws UnknownHostException, IOException {
		this.filesDownloaded = 0;
		this.transmitter = new DataOutputStream(sock.getOutputStream());
		this.receiver = new DataInputStream(sock.getInputStream());
	}

	public void requestDelete(String f) throws IOException {
		
		this.transmitter.writeInt(Constants.DELETE.getBytes().length);
		this.transmitter.write(Constants.DELETE.getBytes());
		
		byte[] buf = f.getBytes(); 

		this.transmitter.writeInt(buf.length);
		this.transmitter.write(buf);
		
		int length = receiver.readInt();    
	    byte[] message = new byte[length];
	    receiver.readFully(message, 0, message.length); 

	    String diskPath = new String(message);

	    length = receiver.readInt();    
	    message = new byte[length];
	    receiver.readFully(message, 0, message.length); 
	    	    
		while(!(new String(message).equals(Constants.DONE))) {
		    
		    try {
			    new File(diskPath.substring(0, diskPath.lastIndexOf("/"))).mkdirs();
			    new File(diskPath);
			    FileOutputStream fos = new FileOutputStream(new File(diskPath));
			    fos.write(message);
			    fos.close();
		    } catch (Exception e) {
		    	System.out.println(e.getMessage());
		    }
		    
		    length = receiver.readInt();    
		    message = new byte[length];
		    receiver.readFully(message, 0, message.length); 
		    
		    diskPath = new String(message);
		    
		    length = receiver.readInt();    
		    message = new byte[length];
		    receiver.readFully(message, 0, message.length); 
		    
		}
		
		System.out.println(f + " has been deleted!");
		
	}

	public int getFilesDeleted() {
		return this.filesDownloaded;
	}
	
	public void resetFilesDeleted() {
		this.filesDownloaded = 0;
	}
	
}
