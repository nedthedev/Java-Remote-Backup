package client;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 * 
 * @author ned
 * This is the main class which will create a socket to the designated IP and port. 
 * 
 */
public class Client {

	private String slash;
	private String ip;
	private int port;
	private Downloader downloader;
	private Uploader uploader;
	private Deleter deleter;
	private UserInputController uic;
	private String autoRecurse;
	
	public Client(String ip, int port) throws UnknownHostException, IOException {
		this.ip = ip;
		this.port = port;
		this.uic = new UserInputController();
		this.slash = "/";
		this.autoRecurse = null;
//		SocketFactory sf = SSLSocketFactory.getDefault();
//	    Socket s = sf.createSocket(this.ip, this.port);
//		System.out.println("Making SSL connection to " + ip + ":" + port + ".");
	    
		Socket s = new Socket(this.ip, this.port);
		System.out.println("Making connection to " + ip + ":" + port + ".");

		this.deleter = new Deleter(this.ip, this.port, s);
		this.downloader = new Downloader(this.ip, this.port, s);
		this.uploader = new Uploader(this.ip, this.port, s);
	}
	
	public String getSlash() {
		return this.slash;
	}
	
	public void attemptFetch(String fname) throws IOException {
		this.downloader.requestDownload(fname);
	}
	
	public void attemptDelete(String fname) throws IOException {
		this.deleter.requestDelete(fname);
	}

	public void uploadFile(File f) throws IOException {
		this.uploader.uploadFile(f);
	}
	
	public void uploadDirectory(String dir, boolean recursive, String[] files) throws IOException {
		File tmp = null;
		if(files != null) {
			for(int i = 0; i < files.length; i++) {
				tmp = new File(dir + slash + files[i]);
				if(tmp.isDirectory() && files[i].charAt(0) != '.' && !files[i].equals("Applications")) {
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
	
	public void attemptUpload(String fname) throws IOException {
		if(fname.charAt(fname.length()-1) == '\\' || fname.charAt(fname.length()-1) == '/') {
			fname = fname.substring(0, fname.length()-1);
		}
		
		File f = new File(fname);
		
		if(fname.contains("/")) {
			this.slash = "/";
		}
		else {
			this.slash = "\\";
		}
		
		if(f.isFile()) {
			System.out.println("Attempting to upload the file: " + fname);
			this.uploadFile(new File(fname));
			printStats();
		}
		else if(f.isDirectory()){
			String recurse = "";
			if(this.autoRecurse == null || this.autoRecurse.equals("")) {
				recurse = uic.askQuestion("Upload all sub folders as well (Y/N)? ");
				this.autoRecurse = "";
			}
			if(this.autoRecurse.equals("true") || recurse.equals("Y") || recurse.equals("y")) {
				uploadDirectory(fname, true, f.list());
				printStats();
				System.out.println("The directory " + fname + " has been successfully uploaded, including all subdirectories!");
			}
			else if(this.autoRecurse.equals("false") || recurse.equals("N") || recurse.equals("n")){
				uploadDirectory(fname, false, f.list());
				printStats();
				System.out.println("The directory " + fname + " has been successfully uploaded, not including subdirectories!");
			}
			else {
				System.out.println("Huh? You need to answer with Y or N.");
			}
		}
		else {
			System.out.println("Sorry, can't find what you wanted to upload... Let's try again.");
		}
	}
	
	public void printStats() {
		System.out.println(this.uploader.getFilesUploaded()-this.uploader.getFailed().size() + " of " + this.uploader.getFilesUploaded() + " were uploaded successfully!");
		if(this.uploader.getFailed().size() != 0) {
			System.out.println("List of files that didn't go through: ");
			for(int i = 0; i < this.uploader.getFailed().size(); i++) {
				System.out.println(this.uploader.getFailed().get(i));
			}
		}
	}
	
	public int getFilesUploaded() {
		return this.uploader.getFilesUploaded();
	}
	
	public void resetFilesUploaded() {
		this.uploader.resetFilesUploaded();
	}
	
	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		
		if(args.length < 2) {
			System.out.println("Must provide two arguments.\n\tArgument 1: IP Address\n\tArgument 2: Port to listen to and send on.\nExiting.");
			return;
		}
		
		Client p = new Client(args[0], Integer.parseInt(args[1]));
		UserInputController uic = new UserInputController();
		
		String fname = "";
		
		if(args.length == 5) {
			p.autoRecurse = args[4];
			if(args[2].equals("-f")) {
				p.attemptFetch(args[3]);
			}
			else if(args[2].equals("-u")) {
				p.attemptUpload(args[3]);
			}
			return;
		}
		else {
		
			while(true) {
				
				fname = uic.askQuestion("Fetch(F) or Upload(U) or Delete(D): ");
				
				if(fname.equals("F") || fname.equals("f")) {
					fname = uic.askQuestion("Enter the file name or directory you wish to download: ");
					p.attemptFetch(fname);
				}
				else if(fname.equals("U") || fname.equals("u")) {
					fname = uic.askQuestion("Enter the file name or directory you wish to upload: ");
					p.attemptUpload(fname);
					p.resetFilesUploaded();
				}
				else if(fname.equals("D") || fname.equals("d")) {
					fname = uic.askQuestion("Enter the file name or directory you wish to upload: ");
					p.attemptDelete(fname);
					p.resetFilesUploaded();
				}
				else {
					System.out.println("What's that? Enter F or U or D as option.");
				}
				
			}
		}
	}
	
}
