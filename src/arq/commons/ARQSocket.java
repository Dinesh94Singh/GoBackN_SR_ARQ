package arq.commons;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public  abstract class ARQSocket {
	protected Socket s;

	public String readln() {
		String line = null;
		try {
			ObjectInputStream input = new ObjectInputStream(s.getInputStream());
			line = (String)input.readObject();
		} catch (Exception e) {
			line = e.toString();
		}
		return line;
	}

	public void writeln(String line) throws Exception {
		ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
		output.writeObject(line);
	}
}
