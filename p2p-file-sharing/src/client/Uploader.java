package client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * 
 * @author ned
 *
 */
public class Uploader {

	private int filesUploaded;
	private DataOutputStream transmitter; 
	private DataInputStream receiver;
	private ArrayList<String> failed;
	
	public Uploader(String ip, int port, Socket sock) throws UnknownHostException, IOException {
		this.filesUploaded = 0;
		this.transmitter = new DataOutputStream(sock.getOutputStream());
		this.receiver = new DataInputStream(sock.getInputStream());
		this.failed = new ArrayList<String>();
	}

	public void uploadFile(File f) throws IOException {
		
		this.transmitter.writeInt(Constants.UPLOAD.getBytes().length);
		this.transmitter.write(Constants.UPLOAD.getBytes());
		
		byte[] buf = f.getPath().getBytes(); 

		this.transmitter.writeInt(buf.length);
		this.transmitter.write(buf);
		
		buf = new byte[(int) f.length()]; 
		
		this.filesUploaded++;
		
		try {
			FileInputStream fis = new FileInputStream(f);
			fis.read(buf);
			fis.close();
			
			this.transmitter.writeInt(buf.length);
			this.transmitter.write(buf);
			if(this.receiver.readInt() != 1) {
				this.failed.add(f.getPath());
			}
			System.out.println(f.getPath()+ " has been uploaded!");
			
		} catch (FileNotFoundException e) {
			failed.add(f.getPath());
			System.out.println(f.getPath() + " was not uploaded. " + e.getMessage());
		}
	}
	
	public ArrayList<String> getFailed() {
		return this.failed;
	}
	
	public int getFilesUploaded() {
		return this.filesUploaded;
	}
	
	public void resetFilesUploaded() {
		this.filesUploaded = 0;
	}
	
}
