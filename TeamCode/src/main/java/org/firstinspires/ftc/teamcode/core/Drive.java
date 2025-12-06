package org.firstinspires.ftc.teamcode.core;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Drive {

    private final DcMotorEx frontLeft;
    private final DcMotorEx frontRight;
    private final DcMotorEx backLeft;
    private final DcMotorEx backRight;

    private final Telemetry telemetry;

    // ====== TUNABLES ======

    // Slow speed for D-pad creeping
    private static final double SLOW_POWER = 0.3;

    // If forward/backwards feels flipped, change this to -1
    private static final double Y_DIR = 1.0;

    // If strafe feels flipped (right goes left), change this to -1
    private static final double X_DIR = 1.0;

    // If turn feels flipped, change this to -1
    private static final double TURN_DIR = 1.0;

    public Drive(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // Make sure these names match your Control Hub config
        frontLeft  = hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        backLeft   = hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight  = hardwareMap.get(DcMotorEx.class, "backRight");

        // Directions – these are a good starting point for most mecanum bots.
        // If something is off after you swapped motors, FIRST try flipping
        // these directions before touching the math.
        frontLeft.setDirection(DcMotor.Direction.FORWARD);
        backLeft.setDirection(DcMotor.Direction.FORWARD);
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        backRight.setDirection(DcMotor.Direction.REVERSE);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    /**
     * Normal mecanum drive using sticks (fast mode).
     * robotY: forward/back  (+ forward)
     * robotX: strafe left/right (+ right)
     * turn:   rotate CCW (+ CCW)
     */
    public void driveRobotCentric(double robotY, double robotX, double turn) {
        // Apply global direction corrections (if something feels flipped)
        robotY *= Y_DIR;
        robotX *= X_DIR;
        turn   *= TURN_DIR;

        // Standard mecanum math
        double fl = robotY + robotX + turn;
        double bl = robotY - robotX + turn;
        double fr = robotY - robotX - turn;
        double br = robotY + robotX - turn;

        // Normalize so no value exceeds 1.0
        double max = Math.max(1.0,
                Math.max(Math.abs(fl),
                        Math.max(Math.abs(bl),
                                Math.max(Math.abs(fr), Math.abs(br)))));

        fl /= max;
        bl /= max;
        fr /= max;
        br /= max;

        frontLeft.setPower(fl);
        backLeft.setPower(bl);
        frontRight.setPower(fr);
        backRight.setPower(br);

        if (telemetry != null) {
            telemetry.addData("Drive", "FL:%.2f FR:%.2f BL:%.2f BR:%.2f",
                    fl, fr, bl, br);
        }
    }

    /**
     * Slow movement using D-pad.
     * Up/Down = forward/back
     * Left/Right = strafe
     */
    public void driveSlow(boolean up, boolean down, boolean left, boolean right) {
        double y = 0;
        double x = 0;

        if (up)    y =  SLOW_POWER;
        if (down)  y = -SLOW_POWER;
        if (right) x =  SLOW_POWER;   // strafe right
        if (left)  x = -SLOW_POWER;   // strafe left

        driveRobotCentric(y, x, 0);
    }

    /**
     * High-level helper for TeleOp:
     *  - Joysticks = fast mecanum drive (including strafe)
     *  - D-pad = slow creep (overrides joysticks)
     *
     * Pass gamepad1 sticks and D-pad directly into this.
     */
    public void driveFromInputs(double leftStickY,
                                double leftStickX,
                                double rightStickX,
                                boolean dpadUp,
                                boolean dpadDown,
                                boolean dpadLeft,
                                boolean dpadRight) {

        // If any D-pad is pressed, use slow mode
        if (dpadUp || dpadDown || dpadLeft || dpadRight) {
            driveSlow(dpadUp, dpadDown, dpadLeft, dpadRight);
            return;
        }

        // Otherwise, use full-speed mecanum with joysticks
        double y = -leftStickY;      // forward = stick up
        double x =  leftStickX;      // strafe = stick left/right
        double turn = rightStickX;   // rotation

        driveRobotCentric(y, x, turn);
    }

    public void stop() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
}
