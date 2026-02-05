package org.firstinspires.ftc.teamcode.core;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * Drive subsystem (mecanum).
 *
 * TeleOp:
 *  - Left stick: forward/back + strafe
 *  - Right stick X: turn
 *  - D-pad: slow "creep" mode
 *
 * Autonomous:
 *  - Encoders for distance
 *  - IMU for heading hold (drives straighter than encoders alone)
 *
 * Hardware names (must match RC config):
 *  - frontLeft, frontRight, backLeft, backRight
 *  - imu
 */
public class Drive {

    // ---------------- Hardware ----------------
    private final DcMotorEx fl, fr, bl, br;
    private final IMU imu;
    private final Telemetry telemetry;

    // ---------------- Feel / controls ----------------
    private static final double SLOW_POWER = 0.30;  // D-pad creep speed
    private static final double STRAFE_COMP = 1.10; // mecanum strafe compensation (optional)

    // ---------------- Encoder math ----------------
    // NeveRest Orbital 20: encoder is AFTER gearbox.
    // Base: 1120 ticks/rev. Your 24in test went 23in, so tuned:
    private static final double TICKS_PER_REV = 1168.0; // tuned (was 1120)
    private static final double WHEEL_DIAM_IN = 3.94;   // 100 mm
    private static final double EXTERNAL_GEAR_RATIO = 1.0; // chain/belt/gears external to motor (usually 1.0)

    private static final double TICKS_PER_IN =
            (TICKS_PER_REV * EXTERNAL_GEAR_RATIO) / (Math.PI * WHEEL_DIAM_IN);

    // ---------------- IMU hold (tune later) ----------------
    private static final double HEADING_KP = 0.02;      // raise if it curves, lower if it wiggles
    private static final double MAX_CORRECTION = 0.35;  // prevents violent over-correction

    private double headingOffsetDeg = 0.0;
    private double holdHeadingDeg = 0.0;

    // ---------------- Constructor ----------------
    public Drive(HardwareMap hw, Telemetry tel) {
        telemetry = tel;

        fl = hw.get(DcMotorEx.class, "frontLeft");
        fr = hw.get(DcMotorEx.class, "frontRight");
        bl = hw.get(DcMotorEx.class, "backLeft");
        br = hw.get(DcMotorEx.class, "backRight");

        // Typical mecanum directions (adjust if your bot drives backwards)
        fl.setDirection(DcMotor.Direction.FORWARD);
        bl.setDirection(DcMotor.Direction.FORWARD);
        fr.setDirection(DcMotor.Direction.REVERSE);
        br.setDirection(DcMotor.Direction.REVERSE);

        setBrake(true);
        resetEncoders();

        // IMU init: update orientation if your hub is mounted differently.
        imu = hw.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
                )
        ));
        imu.resetYaw();
        resetHeading();
    }

    // =========================================================
    // TeleOp
    // =========================================================

    /**
     * One-call TeleOp driving. Feed gamepad1 values directly.
     */
    public void teleOpDrive(double leftStickY, double leftStickX, double rightStickX,
                            boolean dpadUp, boolean dpadDown, boolean dpadLeft, boolean dpadRight) {

        // D-pad overrides sticks (slow creep)
        if (dpadUp || dpadDown || dpadLeft || dpadRight) {
            double y = 0, x = 0;
            if (dpadUp) y = SLOW_POWER;
            if (dpadDown) y = -SLOW_POWER;
            if (dpadRight) x = SLOW_POWER;
            if (dpadLeft) x = -SLOW_POWER;
            driveRobotCentric(y, x, 0);
            return;
        }

        // Sticks
        double y = -leftStickY;            // stick up = forward
        double x = leftStickX * STRAFE_COMP;
        double turn = rightStickX;

        driveRobotCentric(y, x, turn);
    }

    /**
     * Core mecanum math (robot-centric).
     */
    public void driveRobotCentric(double y, double x, double turn) {
        double flp = y + x + turn;
        double blp = y - x + turn;
        double frp = y - x - turn;
        double brp = y + x - turn;

        double max = Math.max(1.0,
                Math.max(Math.abs(flp),
                        Math.max(Math.abs(blp),
                                Math.max(Math.abs(frp), Math.abs(brp)))));

        fl.setPower(flp / max);
        bl.setPower(blp / max);
        fr.setPower(frp / max);
        br.setPower(brp / max);

        if (telemetry != null) telemetry.addData("Heading", "%.1f", getHeadingDeg());
    }

    // =========================================================
    // IMU heading
    // =========================================================

    /** Makes current heading become "0". Call at the start of auto. */
    public void resetHeading() {
        headingOffsetDeg = rawYawDeg();
    }

    /** Heading in degrees, wrapped to [-180, 180). */
    public double getHeadingDeg() {
        return wrapDeg(rawYawDeg() - headingOffsetDeg);
    }

    private double rawYawDeg() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    // =========================================================
    // Encoders / Autonomous movement
    // =========================================================

    /** Reset encoder counts to zero. */
    public void resetEncoders() {
        setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    /** Begin an encoder move while holding the current heading with IMU. */
    public void startDriveStraightIMU(double inches, double power) {
        holdHeadingDeg = getHeadingDeg();

        int ticks = (int) Math.round(inches * TICKS_PER_IN);

        fl.setTargetPosition(fl.getCurrentPosition() + ticks);
        fr.setTargetPosition(fr.getCurrentPosition() + ticks);
        bl.setTargetPosition(bl.getCurrentPosition() + ticks);
        br.setTargetPosition(br.getCurrentPosition() + ticks);

        setMode(DcMotor.RunMode.RUN_TO_POSITION);
        setAllPower(Math.abs(power));
    }

    /** Call repeatedly during an encoder move to keep it straight using IMU. */
    public void updateHeadingHold(double basePower) {
        double error = wrapDeg(holdHeadingDeg - getHeadingDeg());
        double correction = clamp(HEADING_KP * error, -MAX_CORRECTION, MAX_CORRECTION);

        double left = clamp(basePower + correction, 0, 1);
        double right = clamp(basePower - correction, 0, 1);

        fl.setPower(left);
        bl.setPower(left);
        fr.setPower(right);
        br.setPower(right);

        if (telemetry != null) {
            telemetry.addData("Hold", "target %.1f now %.1f err %.1f",
                    holdHeadingDeg, getHeadingDeg(), error);
        }
    }

    public boolean isBusy() {
        return fl.isBusy() || fr.isBusy() || bl.isBusy() || br.isBusy();
    }

    /**
     * Blocking helper for Autonomous.
     * Safe because it has a timeout and calls idle() correctly.
     */
    public void driveStraightBlocking(LinearOpMode opMode, double inches, double power, double timeoutS) {
        startDriveStraightIMU(inches, power);

        double start = opMode.getRuntime();
        while (opMode.opModeIsActive()
                && isBusy()
                && (opMode.getRuntime() - start) < timeoutS) {

            updateHeadingHold(Math.abs(power));
            if (telemetry != null) telemetry.update();
            opMode.idle();
        }

        stop();
    }

    // =========================================================
    // Turning (IMU-only, simple & reliable)
    // =========================================================

    /** Blocking turn to a target heading (degrees). */
    public void turnToHeadingBlocking(LinearOpMode opMode, double targetDeg, double maxTurnPower,
                                      double toleranceDeg, double timeoutS) {

        double start = opMode.getRuntime();
        while (opMode.opModeIsActive() && (opMode.getRuntime() - start) < timeoutS) {
            double error = wrapDeg(targetDeg - getHeadingDeg());

            if (Math.abs(error) <= toleranceDeg) break;

            // Proportional turn (simple)
            double turn = clamp(0.01 * error, -maxTurnPower, maxTurnPower);
            driveRobotCentric(0, 0, turn);

            if (telemetry != null) {
                telemetry.addData("Turn", "target %.1f now %.1f err %.1f", targetDeg, getHeadingDeg(), error);
                telemetry.update();
            }
            opMode.idle();
        }
        stop();
    }

    // =========================================================
    // Stop / utilities
    // =========================================================

    public void stop() {
        setAllPower(0);
        setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void setMode(DcMotor.RunMode mode) {
        fl.setMode(mode);
        fr.setMode(mode);
        bl.setMode(mode);
        br.setMode(mode);
    }

    private void setAllPower(double p) {
        fl.setPower(p);
        fr.setPower(p);
        bl.setPower(p);
        br.setPower(p);
    }

    private void setBrake(boolean on) {
        DcMotor.ZeroPowerBehavior z = on
                ? DcMotor.ZeroPowerBehavior.BRAKE
                : DcMotor.ZeroPowerBehavior.FLOAT;
        fl.setZeroPowerBehavior(z);
        fr.setZeroPowerBehavior(z);
        bl.setZeroPowerBehavior(z);
        br.setZeroPowerBehavior(z);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double wrapDeg(double d) {
        while (d >= 180) d -= 360;
        while (d < -180) d += 360;
        return d;
    }
}