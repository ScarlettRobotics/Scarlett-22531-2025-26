package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.core.SystemsManager;

@Autonomous(name = "Unpark", group = "Competition")
public class Unpark extends LinearOpMode {

    // --- Tunable timings (all in milliseconds) ---
    private static final long MOVE_BACK_TIME_MS   = 500;   // how long to drive back
    private static final double MOVE_BACK_POWER   = -0.5;  // negative = backwards with your Drive.y convention

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

        if (!opModeIsActive()) return;

        systems.drive.stop();

        telemetry.addLine("Good luck lmao");
        telemetry.update();
        sleep(500); // brief pause so you can see telemetry
    }
}
