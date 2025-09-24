# Heartbeat Tactic for Fault Detection

This project is a prototype implementation of the "Heartbeat" fault detection tactic, designed for a critical system in an autonomous vehicle. The system consists of two independent processes: a Battery Management System (BMS) that acts as the heartbeat "emitter," and a main Autonomous Vehicle controller that acts as the heartbeat "monitor."

The core idea is that the BMS, a critical component, sends periodic status updates (the heartbeat) to the Autonomous Vehicle controller. If the controller does not receive a heartbeat within a predefined timeout period, it assumes the BMS has failed and triggers an emergency response.

## System Logic

`BatteryManagementSystem.java` (The Emitter)

This program simulates the BMS. It runs a continuous loop that performs the following actions:

- **Gathers Data:** It "reads" simulated battery data, including level, temperature, and health. This data is part of the heartbeat message.

- **Broadcasts Heartbeat:** It sends a UDP packet containing the battery status to the Autonomous Vehicle monitor on a specific port (65433).

- **Simulates Crash:** It includes a crash_on_random_condition() method that throws a RuntimeException on a non-deterministic basis. This simulates a realistic software or hardware failure, such as a stack overflow, without simply calling System.exit().


`AutonomousVehicle.java` (The Monitor)

This program simulates the main vehicle controller. Its logic is as follows:

- **Listens:** It binds to a specific port (65433) and listens for incoming UDP packets from the BMS.

- **Receives with Timeout:** It uses a socket timeout to avoid blocking indefinitely. If no packet is received within the timeout period, a SocketTimeoutException is thrown.

- **Fault Detection:** If the time elapsed since the last heartbeat exceeds the predefined TIMEOUT_MS (3 seconds), it determines that the BMS has failed and activates an "Emergency State," logging an error message and a planned emergency response.


## How to Run the Application

This system requires two separate processes to run simultaneously. You will need to open two terminal windows or command prompts.

Step 1: **Compile the Java Files.** In each terminal, navigate to the directory where you saved the .java files and compile them.

```
javac BatteryManagementSystem.java
javac AutonomousVehicle.java
```

This will create BatteryManagementSystem.class and AutonomousVehicle.class.

Step 2: **Run the Monitor Process.** In the first terminal, start the monitor process. It needs to be running and listening before the emitter begins sending data.

```
java AutonomousVehicle
```

You will see the message: "Autonomous Vehicle System started. Awaiting BMS heartbeat..."

Step 3: **Run the Emitter Process.** In the second terminal, start the emitter process.

```
java BatteryManagementSystem
```

You will see the message: "Battery Management System (BMS) started. Emitting battery statuses..."

Step 4: **Observe the System.** The two terminals will now communicate.

After a random period, the emitter will simulate a crash, and its terminal will show a fatal error. Once the emitter has crashed, the monitor will stop receiving heartbeats. After 3 seconds, the monitor's terminal will print the emergency state activation message, demonstrating successful fault detection.