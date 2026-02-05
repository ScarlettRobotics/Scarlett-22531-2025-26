package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.core.Robot;

/**
 * Simple timed autonomous (NO SENSORS for the first part):
 *  1) Drive forward 4.0 seconds
 *  2) Timed spin (small amount)
 *  3) Drive forward 3.0 seconds
 *  4) Back up a little using encoders + IMU hold
 *  5) Shoot for ~9 seconds
 */
@Autonomous(name = "Auto Loading Zone Side Blue", group = "Auto")
public class AutoLoadingZoneSideBlue extends LinearOpMode {

    // Timed drive section
    private static final double DRIVE_POWER = 0.35;
    private static final double TURN_POWER  = -0.25;  // negative = spin right with your driveRobotCentric()
    private static final double TURN_TIME_S = 1.0;

    private static final double FORWARD1_TIME_S = 4.0;
    private static final double FORWARD2_TIME_S = 3.0;

    // Depot-side add-on section (encoders + IMU)
    private static final double BACK_UP_IN = -2.5;
    private static final double BACKUP_TIMEOUT_S = 2.5;
    private static final double SHOOT_TIME_S = 9.0;

    @Override
    public void runOpMode() {

        Robot robot = new Robot(hardwareMap, telemetry);

        telemetry.addLine("Auto Loading Zone Side Blue ready");
        telemetry.update();

        waitForStart();
        if (!opModeIsActive()) return;

        // ---------------- 1) Forward (timed) ----------------
        double t0 = getRuntime();
        while (opModeIsActive() && (getRuntime() - t0) < FORWARD1_TIME_S) {
            robot.drive.driveRobotCentric(DRIVE_POWER, 0.0, 0.0);
            idle();
        }
        robot.drive.stop();
        sleep(150);

        // ---------------- 2) Timed spin (RIGHT, because TURN_POWER is negative) ----------------
        double tTurn = getRuntime();
        while (opModeIsActive() && (getRuntime() - tTurn) < TURN_TIME_S) {
            robot.drive.driveRobotCentric(0.0, 0.0, TURN_POWER);
            idle();
        }
        robot.drive.stop();
        sleep(150);

        // ---------------- 3) Forward (timed) ----------------
        double t1 = getRuntime();
        while (opModeIsActive() && (getRuntime() - t1) < FORWARD2_TIME_S) {
            robot.drive.driveRobotCentric(DRIVE_POWER, 0.0, 0.0);
            idle();
        }
        robot.drive.stop();
        sleep(150);

        // ===================== AFTER STEP 3: DEPOT-SIDE ADD-ON =====================

        // Reset references before encoder move (prevents weird "carry over" encoder state)
        robot.drive.resetEncoders();
        robot.drive.resetHeading();

        // 4) Back up a little with IMU hold + timeout safety
        robot.drive.driveStraightBlocking(this, BACK_UP_IN, DRIVE_POWER, BACKUP_TIMEOUT_S);

        // 5) Shoot for fixed time
        double tShoot = getRuntime();
        while (opModeIsActive() && (getRuntime() - tShoot) < SHOOT_TIME_S) {
            robot.shooter.on();
            telemetry.addData("Shooting", "%.1f / %.1f", (getRuntime() - tShoot), SHOOT_TIME_S);
            telemetry.update();
            idle();
        }

        robot.stopAll();
    }
}
