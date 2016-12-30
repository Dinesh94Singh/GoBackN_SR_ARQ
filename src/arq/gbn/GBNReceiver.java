package arq.gbn;

import java.net.ServerSocket;

import arq.commons.ARQSocket;
import arq.sr.SRReceiver;

public class GBNReceiver extends ARQSocket implements Runnable {
	ServerSocket ss;
	boolean stopFlag = false;

	Thread tServer = new Thread(this);

	int windowSize = 0;
	int count;// no of packets
	int[] dataContent;

	
	@Override
	public void run() {
		try {
			while (!stopFlag) {
				if (s == null) {
					s = ss.accept();
					System.out.println("Client Connected!");
					int lastReceivedId = -1;
					int index = 0;
					while (s.isConnected()) {
						String signal = readln();
						System.out.print("Received From Cleint: " + signal);

						if ("stopServer".equalsIgnoreCase(signal)) {
							stopFlag = true;
							System.out.println("...Transmission Accomplished");
							System.out.println("Data Received: ");
							for (int ele : dataContent)
								System.out.println(ele);
							break;
						} else if (signal.startsWith("winSize")) {
							windowSize = Integer.parseInt(((String) signal)
									.substring(8));
							dataContent = new int[windowSize];
						} else if (signal.startsWith("count")) {
							count = Integer.parseInt(((String) signal)
									.substring(6));
							dataContent = new int[count];
						} else if (signal.startsWith("#")) {
							int packIndex = Integer.parseInt(signal.substring(
									1, signal.indexOf(":")));
							int dataPacket = Integer.parseInt(signal
									.substring(signal.indexOf(":") + 1));
							if (packIndex == 0
									|| (packIndex - lastReceivedId) == 1) {
								dataContent[index] = dataPacket;
								writeln("ack:" + packIndex);
								System.out.println("...Accepted And ACKed!");
								lastReceivedId = packIndex;
								index++;
							} else {
								System.out.println("...Ignored!");
							}
						}
					}
					System.out.println("...Server Stopped!");
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	public static void main(String ...a) throws Exception {
		GBNReceiver gbn = new GBNReceiver();
		gbn.ss = new ServerSocket(1234);
		gbn.tServer.start();
		System.out.println("Waiting for sender...");
	}
}