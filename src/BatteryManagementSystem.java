import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * Represents the critical Battery Management System (BMS) in an autonomous vehicle.
 * This class acts as the "heartbeat emitter," periodically broadcasting the battery's
 * status to the main vehicle controller.
 */
public class BatteryManagementSystem {

    // Network configuration for communication with the main vehicle controller
    private static final String HOST = "127.0.0.1";
    private static final int MONITOR_PORT = 65433;

    private final Battery battery = new Battery();
    private final Random random = new Random();

    /**
     * The main execution loop of the BMS. This simulates the continuous operation
     * of the battery management module on a dedicated processor in the car.
     */
    public void run() {
        System.out.println("Battery Management System (BMS) started. Emitting battery statuses...");

        try (DatagramSocket socket = new DatagramSocket()) {
            while (true) {
                // 1. Get real-time battery data
                double level = battery.getBatteryLevel();
                double temperature = battery.getTemperature();
                boolean isCharging = battery.getIsCharging();
                double health = battery.getHealth();

                // 2. Format the status message for a "network broadcast"
                // This mimics a data frame sent over a vehicle's internal network
                String statusMessage = String.format("Level=%.2f, Temp=%.2f, Charging=%b, Health=%.2f",
                        level, temperature, isCharging, health);
                byte[] buffer = statusMessage.getBytes();

                // 3. Create and send the UDP packet to the main vehicle controller (the monitor)
                InetAddress address = InetAddress.getByName(HOST);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, MONITOR_PORT);
                socket.send(packet);

                System.out.println("[BMS] AV Battery Status: " + statusMessage);

                // 4. Introduce a non-deterministic failure
                // This simulates a rare and unpredictable event, such as a hardware failure,
                // a software bug leading to a stack overflow, or a power loss to the module.
                if (random.nextInt(100) > 95) {
                    System.err.println("--- FATAL ERROR: SIMULATED HARDWARE FAILURE ---");
                    throw new RuntimeException("CRITICAL: DATA CORRUPTION OR STACK OVERFLOW.");
                }

                // 5. Wait for the next status update cycle
                // This pause represents the fixed periodic nature of status broadcasts.
                try {
                    Thread.sleep(1000); // 1-second interval, common for vehicle systems
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("BMS thread interrupted.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Socket error in BMS: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("BMS process terminated.");
        }
    }

    /**
     * This inner class simulates the physical battery's state.
     * The values it returns are simplified to mimic real-world changes.
     */
    private static class Battery {
        private double batteryLevel = 100.0; // Starts at full charge
        private double temperature = 25.0;   // Starts at a normal temperature in Celsius
        private double health = 100.0;       // Starts at 100% health
        private final Random random = new Random();
        private final Object lock = new Object();

        public double getBatteryLevel() {
            synchronized (lock) {
                if (batteryLevel > 0.0) {
                    // Simulates gradual discharge over time
                    batteryLevel -= random.nextDouble() * 0.5;
                }
                return batteryLevel;
            }
        }

        public double getTemperature() {
            synchronized (lock) {
                // Simulates random temperature fluctuations
                temperature += (random.nextDouble() - 0.5) * 2;
                return temperature;
            }
        }

        public boolean getIsCharging() {
            // A simplified constant for the simulation; in a real car, this would change.
            return false;
        }

        public double getHealth() {
            synchronized (lock) {
                // Simulates a very low chance of battery degradation over time
                if (random.nextInt(1000) == 0 && health > 0) {
                    health -= 1.0;
                }
                return health;
            }
        }
    }

    public static void main(String[] args) {
        new BatteryManagementSystem().run();
    }
}
