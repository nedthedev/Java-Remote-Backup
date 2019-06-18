package client;
import java.util.Scanner;

public class UserInputController {

	private Scanner reader;
	
	public UserInputController() {
		reader = new Scanner(System.in);
	}
	
	public String askQuestion(String question) {
		System.out.println(question);
		return reader.nextLine();
	}
	
}
