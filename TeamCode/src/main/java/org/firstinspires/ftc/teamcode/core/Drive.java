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

    // Tunables
    private static final double SLOW_POWER = 0.3;    // used for D-pad creeping

    public Drive(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // TODO: change these names to whatever you used in the Control Hub
        frontLeft  = hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        backLeft   = hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight  = hardwareMap.get(DcMotorEx.class, "backRight");

        // Directions – you may need to flip these based on how your motors are mounted
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
     * Exactly one direction should be pressed; if more, last one wins.
     */
    public void driveSlow(boolean up, boolean down, boolean left, boolean right) {
        double y = 0;
        double x = 0;

        if (up)    y = SLOW_POWER;
        if (down)  y = -SLOW_POWER;
        if (right) x = SLOW_POWER;
        if (left)  x = -SLOW_POWER;

        driveRobotCentric(y, x, 0);
    }

    public void stop() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
}
