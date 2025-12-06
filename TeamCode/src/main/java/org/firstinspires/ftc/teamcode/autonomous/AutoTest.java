package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.core.SystemsManager;

@Autonomous(name = "AutoTest", group = "Competition")
public class AutoTest extends LinearOpMode {

    // --- Tunable timings (all in milliseconds) ---
    private static final long MOVE_BACK_TIME_MS   = 500;  // how long to drive back
    private static final double MOVE_BACK_POWER   = -0.3; // negative = backwards with your Drive.y convention

    private static final long SHOOTER_SPINUP_MS   = 1500; // time to get flywheel up to speed
    private static final long INDEXER_FEED_MS     = 250;  // time indexer stays in FEED position
    private static final long INDEXER_REST_MS     = 250;  // time indexer stays in REST between shots
    private static final int  NUM_SHOTS           = 3;

    private SystemsManager systems;

    @Override
    public void runOpMode() throws InterruptedException {

        systems = new SystemsManager(hardwareMap, telemetry);

        telemetry.addLine("AutoTest initialized. Waiting for start...");
        telemetry.update();

        waitForStart();

        if (!opModeIsActive()) return;

        // -------- STEP 1: Move back ~half a foot --------
        telemetry.addLine("Step 1: Driving backwards");
        telemetry.update();

        systems.drive.driveRobotCentric(MOVE_BACK_POWER, 0, 0); // y = back, no strafe, no turn
        sleep(MOVE_BACK_TIME_MS);
        systems.drive.stop();

        // -------- STEP 2: Spin up shooter --------
        telemetry.addLine("Step 2: Spinning up shooter");
        telemetry.update();

        systems.shooter.setFlywheelPower(1.0);  // full power; adjust if needed
        sleep(SHOOTER_SPINUP_MS);

        // -------- STEP 3: Fire 3 shots --------
        telemetry.addLine("Step 3: Firing shots");
        telemetry.update();

        for (int i = 0; i < NUM_SHOTS && opModeIsActive(); i++) {
            // Feed ring/ball
            systems.shooter.feed();
            sleep(INDEXER_FEED_MS);

            // Return indexer
            systems.shooter.rest();
            sleep(INDEXER_REST_MS);
        }

        // -------- STEP 4: Shutdown --------
        systems.shooter.stop();
        systems.drive.stop();

        telemetry.addLine("AutoTest complete");
        telemetry.update();
        sleep(500); // brief pause so you can see telemetry
    }
}
