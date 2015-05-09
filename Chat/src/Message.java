import java.io.*;

public class Message implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// The different types of message sent by the Client
	// WHOISIN to receive the list of the users connected
	// MESSAGE an ordinary message
	// LOGOUT to disconnect from the Server
	static final int MESSAGE = 1, LOGOUT = 2;
	private int type;
	private String message;
	
	Message(int type, String message) {
		this.type = type;
		this.message = message;
	}
	int getType() {
		return type;
	}
	String getMessage() {
		return message;
	}
}

