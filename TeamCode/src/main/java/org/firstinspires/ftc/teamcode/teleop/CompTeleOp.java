package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.core.SystemsManager;

@TeleOp(name = "CompTeleOp", group = "TeleOp")
public class CompTeleOp extends LinearOpMode {

    private SystemsManager systems;

    // Track last A state so we can detect a *press* (rising edge)
    private boolean lastAPressed = false;

    @Override
    public void runOpMode() {

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

                x *= 1.1; // optional small compensation

                systems.drive.driveRobotCentric(y, x, rx);
            }

            // ---------- SHOOT ONE BALL ON A PRESS ----------
            boolean aNow = gamepad1.a;

            // rising edge: was not pressed, now pressed
            if (aNow && !lastAPressed) {
                shootOneBall();
            }

            lastAPressed = aNow;

            telemetry.addData("Drive mode", dpadActive ? "SLOW (D-pad)" : "FAST (sticks)");
            telemetry.update();

            idle();
        }

        systems.stopAll();
    }

    /**
     * Fires exactly one ball by:
     *  1) Spinning up flywheel
     *  2) Running mid roller + indexers
     *  3) Stopping everything
     *
     * NOTE: This is blocking. During this time, drive will pause.
     */
    private void shootOneBall() {
        // 1) Spin up flywheel
        systems.shooter.setFlywheelOn(true);
        systems.shooter.setMidRollerOn(false); // keep roller off during spin-up
        systems.shooter.restIndexers();        // hold ball in place

        sleep(400); // ms – tune this spin-up time on your bot

        // 2) Feed ball: mid roller + indexers
        systems.shooter.setMidRollerOn(true);  // spins backwards (MID_ROLLER_POWER < 0)
        systems.shooter.feedIndexers();        // push ball into wheels

        sleep(750); // ms – tune for "just one ball"

        // 3) Stop everything
        systems.shooter.setFlywheelOn(true);
        systems.shooter.setMidRollerOn(false);

        sleep(2000);

        systems.shooter.restIndexers();

    }
}
