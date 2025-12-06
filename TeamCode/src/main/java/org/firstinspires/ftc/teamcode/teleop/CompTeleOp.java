package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.core.SystemsManager;

@TeleOp(name = "CompTeleOp", group = "TeleOp")
public class CompTeleOp extends LinearOpMode {

    private SystemsManager systems;

    @Override
    public void runOpMode() throws InterruptedException {

        // Create SystemsManager with the FTC hardwareMap + telemetry
        systems = new SystemsManager(hardwareMap, telemetry);

        telemetry.addLine("CompTeleOp initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // ---------- DRIVE (Gamepad 1) ----------
            boolean dpadActive = gamepad1.dpad_up || gamepad1.dpad_down
                    || gamepad1.dpad_left || gamepad1.dpad_right;

            if (dpadActive) {
                // Slow, precise movement with D-pad
                systems.drive.driveSlow(
                        gamepad1.dpad_up,
                        gamepad1.dpad_down,
                        gamepad1.dpad_left,
                        gamepad1.dpad_right
                );
            } else {
                // Fast mecanum drive with sticks
                double y  = -gamepad1.left_stick_y;  // forward/back (invert)
                double x  = gamepad1.left_stick_x;   // strafe
                double rx = gamepad1.right_stick_x;  // rotate

                // Optional small scale on strafe to help with mecanum drift
                x *= 1.1;

                systems.drive.driveRobotCentric(y, x, rx);
            }

            // ---------- SHOOTER (Gamepad 1) ----------
            double shooterPower = gamepad1.right_trigger;
            systems.shooter.setFlywheelPower(shooterPower);

            if (gamepad1.a) {
                systems.shooter.feed();
            } else if (gamepad1.b) {
                systems.shooter.rest();
            }

            // ---------- TELEMETRY ----------
            telemetry.addData("Drive mode", dpadActive ? "SLOW (D-pad)" : "FAST (sticks)");
            telemetry.addData("Shooter power", "%.2f", shooterPower);
            telemetry.update();

            idle();
        }

        systems.stopAll();
    }
}
