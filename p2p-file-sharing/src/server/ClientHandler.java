package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * 
 * @author ned
 *
 */
public class ClientHandler extends Thread {
	
	private Socket s;
	private final String slash = "/";
	private final String basePath = "/home/minty/BackupData/";
	DataInputStream receiver;
	DataOutputStream transmitter;
	
	public ClientHandler(Socket s) {
		
		this.receiver = null;
		this.transmitter = null;
		System.out.println("Connection found.");
		this.s = s;
		
	}
	
	public void run() {
				
		try {
			receiver = new DataInputStream(s.getInputStream());
			transmitter = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error generating input/output stream. " + e.getMessage());
			return;
		}

		while(true) {
			
			try {
				byte[] message = null;
				
				int length = receiver.readInt();
				message = new byte[length];
				receiver.readFully(message, 0, message.length);
				if(new String(message).equals(Constants.UPLOAD)) {
									
					length = receiver.readInt();
					message = new byte[length];
					receiver.readFully(message, 0, message.length);
					String diskPath = new String(message);
					
					length = receiver.readInt();    
				    message = new byte[length];
				    receiver.readFully(message, 0, message.length); 
				    
				    try {
					    new File(this.basePath + diskPath.substring(0, diskPath.lastIndexOf("/"))).mkdirs();
					    new File(this.basePath + diskPath);
					    FileOutputStream fos = new FileOutputStream(new File(this.basePath + diskPath));
					    fos.write(message);
					    fos.close();
					    transmitter.writeInt(1);
				    } catch (Exception e) {
				    	transmitter.writeInt(0);
				    }
				    
				}
				else if(new String(message).equals(Constants.DOWNLOAD)) {
					
					length = receiver.readInt();
					message = new byte[length];
					receiver.readFully(message, 0, message.length);
					String diskPath = this.basePath.substring(0, this.basePath.length()-1) + new String(message);
					
					System.out.println("Fetching " + diskPath);
					
					File f = new File(diskPath);
					String fname = diskPath;
										
					try {
						if(f.isFile()) {
							System.out.println("Attempting to upload the file: " + fname);
							this.uploadFile(new File(fname));
							sendFinish();
						}
						else if(f.isDirectory()){
							uploadDirectory(fname, true, f.list());
							System.out.println("The directory " + fname + " has been successfully uploaded, including all subdirectories!");
							sendFinish();
						}
						
					} catch (FileNotFoundException e) {
						System.out.println(f.getPath() + " was not uploaded, permission was denied!");
						sendFinish();
					}
					
				}
				else if(new String(message).equals(Constants.DELETE)) {
					
					length = receiver.readInt();
					message = new byte[length];
					receiver.readFully(message, 0, message.length);
					String diskPath = this.basePath.substring(0, this.basePath.length()-1) + new String(message);
					
					System.out.println("Deleting " + diskPath);
					
					File f = new File(diskPath);
					String fname = diskPath;
										
					try {
						if(f.isFile()) {
							System.out.println("Attempting to delete the file: " + fname);
							this.deleteFile(new File(fname));
							sendFinish();
						}
						else if(f.isDirectory()){
							this.deleteDirectory(fname, true, f.list());
							System.out.println("The directory " + fname + " has been successfully uploaded, including all subdirectories!");
							sendFinish();
						}
						
					} catch (FileNotFoundException e) {
						System.out.println(f.getPath() + " was not deleted, permission was denied!");
						sendFinish();
					}
					
				}
				
			} catch (IOException e) {
				System.out.println("Error receiving message. Stopping thread. " + e.getMessage() + "."); 
				this.interrupt();
				return;
			}
			
		}
	}
	
	public void uploadFile(File f) throws IOException {
		
		String original = f.getPath();
		
		try {
			
			FileInputStream fis = new FileInputStream(f);
			
			f = new File("/"+f.getPath().replaceAll(this.basePath, ""));
			
			byte[] buf = f.getPath().getBytes(); 			
			this.transmitter.writeInt(buf.length);
			this.transmitter.write(buf);
			
			f = new File(original);
			
			buf = new byte[(int) f.length()]; 
			fis.read(buf);
			fis.close();
			
			this.transmitter.writeInt(buf.length);
			this.transmitter.write(buf);
			
		} catch (FileNotFoundException e) {
			System.out.println(f.getPath() + " was not uploaded, permission was denied! " + e.getMessage());
		}
	}
	
	public void uploadDirectory(String dir, boolean recursive, String[] files) throws IOException {
		File tmp = null;
		if(files != null) {
			for(int i = 0; i < files.length; i++) {
				tmp = new File(dir + slash + files[i]);
				if(tmp.isDirectory()) {
					if(recursive) {
						this.uploadDirectory(dir + slash + files[i], recursive, tmp.list());
					}
				}
				else {
					this.uploadFile(new File(dir + slash + files[i]));
				}
			}
		}
		else {
			System.out.println(dir + " is null. Not uploaded.");
		}
	}
	
	public void deleteFile(File f) throws IOException {
				
		String fname = f.getPath();
					
		if(f.delete()) {
			System.out.println("Deleted: " + fname);
		}
		else {
			System.out.println("Can't delete: " + fname);
		}
	}
	
	public void deleteDirectory(String dir, boolean recursive, String[] files) throws IOException {
		File tmp = null;
		System.out.println("Attempting to delete " + dir);
		if(files != null) {
			for(int i = 0; i < files.length; i++) {
				tmp = new File(dir + slash + files[i]);
				if(tmp.isDirectory()) {
					if(recursive) {
						this.deleteDirectory(dir + slash + files[i], recursive, tmp.list());
					}
				}
				else {
					this.deleteFile(new File(dir + slash + files[i]));
				}
			}
			new File(dir).delete();
		}
		else {
			System.out.println(dir + " is null. Not deleted.");
		}
	}
	
	public void sendFinish() throws IOException {
		this.transmitter.writeInt(Constants.DONE.getBytes().length);
		this.transmitter.write(Constants.DONE.getBytes());
		this.transmitter.writeInt(Constants.DONE.getBytes().length);
		this.transmitter.write(Constants.DONE.getBytes());
		System.out.println("Sending finished notification to client.");
	}
	
}
