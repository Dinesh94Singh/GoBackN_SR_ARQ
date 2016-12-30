package arq.sr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;

import arq.commons.ARQSocket;
import arq.gbn.GBNSender;

public class SRSender extends ARQSocket implements Runnable {

	int windowSize;
	int transDelay;
	int propDelay;
	int timeOut;
	int packetLossRate;
	int count; // no of packets
	int[] dataPackets;

	int currentPacketIndex = 0;
	int lastACK = -1;
	int lastIndex = 0;
	boolean transDone = false;

	public SRSender() throws Exception {
		acceptParams();
		sendPackets();
	}

	public void acceptParams() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));) {
			System.out.print("Enter Window Size: ");
			windowSize = Integer.parseInt(reader.readLine());
			System.out.print("Enter Transmission Delay(ms): ");
			transDelay = Integer.parseInt(reader.readLine());
			System.out.print("Enter Propagation Delay(ms): ");
			propDelay = Integer.parseInt(reader.readLine());
			System.out.print("Enter Tiem Out(ms): ");
			timeOut = Integer.parseInt(reader.readLine());
			do {
				System.out.print("Enter Packet Loss Rate(0 to 100): ");
				packetLossRate = Integer.parseInt(reader.readLine());
			} while (packetLossRate < 0 || packetLossRate > 100);
			System.out.print("Data Packets Count: ");
			count = Integer.parseInt(reader.readLine());

			dataPackets = new int[count];

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendPackets() throws Exception {

		BufferedReader buf = new BufferedReader(
				new InputStreamReader(System.in));

		System.out.println("Enter Data Packets ");
		for (int i = 0; i < count; i++) {
			System.out.print("Packet " + (i + 1) + " Value: ");
			dataPackets[i] = Integer.parseInt(buf.readLine());
		}

		s = new Socket("localhost", 1234);

		Random packetLossSimulator = new Random();
		Thread ackListener = new Thread(this);

		writeln("winSize:" + windowSize);
		writeln("count:" + count);

		ackListener.start();
		while (currentPacketIndex < dataPackets.length) {
			for (int i = currentPacketIndex; i < currentPacketIndex
					+ windowSize
					&& i < dataPackets.length; i++) {
				int percent = packetLossSimulator.nextInt(101);
				if (percent > packetLossRate) {
					writeln("#"+i+":"+dataPackets[i]);
					System.out.println("#"+i+":"+dataPackets[i] + "...Sent");
				} else {
					System.out.println("#"+i+":"+dataPackets[i]
							+ "...P.L.R");
				}
				Thread.sleep(transDelay + propDelay);
			}

			System.out.print("Running timer...");
			Thread.sleep(timeOut);
			System.out.println("TimeOut");
			currentPacketIndex = lastIndex;
		}

		writeln("stopServer");
		transDone = true;
		System.out.println("Transmission Accomplished!");
	}

	public void run() {
		while (!transDone) {
			String signal = readln();
			if (signal.startsWith("ack")) {
				int ack = Integer.parseInt(signal.substring(4));
				System.out.println("Received ACK " + ack);
				if (ack - lastACK == 1
						|| ((lastACK == -1 || lastACK == windowSize - 1) && ack == 0)) {
					lastIndex++;
					lastACK = ack;
				}else if (ack < 0) {
					int next = (ack * -1)+1;
					if (next < currentPacketIndex + windowSize
							&& next < dataPackets.length) {
						try {						
							writeln("#"+next+":"+dataPackets[next]);
							System.out.println(dataPackets[next] + "...Sent");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
			}
		}
	}
	public static void main(String ...a) throws Exception {
		SRSender srs = new SRSender();
		srs.acceptParams();
		srs.sendPackets();
	}
}
