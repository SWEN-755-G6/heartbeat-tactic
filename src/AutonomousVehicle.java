import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * This class represents the main vehicle control system, acting as the "heartbeat monitor."
 * Its primary responsibility is to passively listen for periodic status updates (heartbeats)
 * from the critical Battery Management System (BMS) module.
 */
public class AutonomousVehicle {

    // Network configuration
    private static final int PORT = 65433;
    private static final int TIMEOUT_MS = 3000; // 3 seconds timeout for a missed heartbeat

    private long lastHeartbeatTime;

    /**
     * The main execution loop of the Autonomous Vehicle's monitoring system.
     * This simulates a dedicated thread or process on the main vehicle computer
     * whose sole job is to ensure the BMS is alive and responsive.
     */
    public void run() {
        System.out.println("Autonomous Vehicle System started. Awaiting BMS heartbeat...");
        lastHeartbeatTime = System.currentTimeMillis();

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            // Setting a short timeout on the socket allows the receive operation to
            // return without blocking indefinitely. This is for detecting a missed heartbeat.
            socket.setSoTimeout(1000); // 1-second receive timeout

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                try {
                    // Attempt to receive a packet from the BMS
                    // This call will block for up to 1 second before throwing a SocketTimeoutException.
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());

                    // Heartbeat received successfully.
                    // This updates the last known time of communication.
                    lastHeartbeatTime = System.currentTimeMillis();
                    System.out.println("[HEARTBEAT] BMS Status Received: " + received);

                } catch (SocketTimeoutException e) {
                    // This exception is expected and signals that no heartbeat was received
                    // within the 1-second receive timeout. The main heartbeat check happens below.
                    // We don't need to do anything here except continue the loop.
                }

                // Check if the time since the last heartbeat exceeds the allowed timeout.
                // This is the core logic of the heartbeat fault detection.
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastHeartbeatTime > TIMEOUT_MS) {
                    System.err.println("------------------------------------------------------------------");
                    System.err.println("EMERGENCY STATE ACTIVATED: NO HEARTBEAT FROM BATTERY MANAGEMENT SYSTEM.");
                    System.err.println("ACTION: Seeking nearest safe location to stop.");
                    System.err.println("REASON: Potential battery damage detected (e.g., loss of power, severed connection).");
                    System.err.println("------------------------------------------------------------------");
                    // Reset the last heartbeat time to prevent continuous log spam
                    lastHeartbeatTime = currentTime;
                }

                // Pause to prevent high CPU usage, allowing other vehicle processes to run.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Monitor thread interrupted.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Socket error in Vehicle Monitor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Autonomous Vehicle System terminated.");
        }
    }

    public static void main(String[] args) {
        new AutonomousVehicle().run();
    }
}
