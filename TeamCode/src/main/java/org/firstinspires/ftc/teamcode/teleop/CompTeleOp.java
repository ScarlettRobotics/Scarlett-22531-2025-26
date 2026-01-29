package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.core.Robot;

/**
 * Competition TeleOp:
 *  - Gamepad 1: driver (mecanum)
 *  - Gamepad 2: operator (shooter)
 */
@TeleOp(name = "CompTeleOp", group = "TeleOp")
public class CompTeleOp extends LinearOpMode {

    @Override
    public void runOpMode() {
        Robot robot = new Robot(hardwareMap, telemetry);

        boolean shooterOn = false;
        boolean lastA = false;
        boolean lastB = false;

        telemetry.addLine("TeleOp ready");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // ---------------- Driver (gamepad1) ----------------
            robot.drive.teleOpDrive(
                    gamepad1.left_stick_y,
                    gamepad1.left_stick_x,
                    gamepad1.right_stick_x,
                    gamepad1.dpad_up,
                    gamepad1.dpad_down,
                    gamepad1.dpad_left,
                    gamepad1.dpad_right
            );

            // ---------------- Operator (gamepad2) ----------------
            // A toggles ON, B toggles OFF (edge-detected so holding doesn't spam)
            boolean aNow = gamepad2.a;
            boolean bNow = gamepad2.b;

            if (aNow && !lastA) shooterOn = true;
            if (bNow && !lastB) shooterOn = false;

            lastA = aNow;
            lastB = bNow;

            if (shooterOn) robot.shooter.on();
            else robot.shooter.stop();

            telemetry.addData("Shooter", shooterOn ? "ON" : "OFF");
            telemetry.update();

            idle();
        }

        robot.stopAll();
    }
}
