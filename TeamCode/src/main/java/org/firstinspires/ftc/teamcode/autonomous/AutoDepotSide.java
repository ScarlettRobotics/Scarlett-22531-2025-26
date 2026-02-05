package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.core.Robot;

/**
 * Auto Depot Side:
 *  1) Reset encoders + heading
 *  2) Back up a little (encoder distance + IMU hold)
 *  3) Shoot for ~9 seconds
 *
 * This uses Drive's "blocking" helpers with timeouts,
 * which prevents the robot from getting stuck forever.
 */
@Autonomous(name = "Auto Depot Side", group = "Auto")
public class AutoDepotSide extends LinearOpMode {

    // Tunables (change these, not the logic)
    private static final double DRIVE_POWER = 0.35;  // if you see slip, drop to 0.30
    private static final double BACK_UP_IN = -2.5;
    private static final double BACKUP_TIMEOUT_S = 2.5;
    private static final double SHOOT_TIME_S = 9.0;

    @Override
    public void runOpMode() {
        Robot robot = new Robot(hardwareMap, telemetry);

        telemetry.addLine("Auto ready");
        telemetry.update();

        waitForStart();
        if (!opModeIsActive()) return;

        // Always reset references at the start of auto
        robot.drive.resetEncoders();
        robot.drive.resetHeading();

        // 1) Back up with IMU hold + timeout safety
        robot.drive.driveStraightBlocking(this, BACK_UP_IN, DRIVE_POWER, BACKUP_TIMEOUT_S);

        // 2) Shoot for fixed time
        double t0 = getRuntime();
        while (opModeIsActive() && (getRuntime() - t0) < SHOOT_TIME_S) {
            robot.shooter.on();
            telemetry.addData("Shooting", "%.1f / %.1f", (getRuntime() - t0), SHOOT_TIME_S);
            telemetry.update();
            idle();
        }

        robot.stopAll();
    }
}
