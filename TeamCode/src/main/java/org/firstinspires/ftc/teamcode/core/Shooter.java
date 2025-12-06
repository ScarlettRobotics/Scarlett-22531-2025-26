package org.firstinspires.ftc.teamcode.core;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Shooter {

    private final DcMotorEx flywheel;
    private final Servo indexer;
    private final Telemetry telemetry;

    // Tunables – you NEED to adjust these for your robot
    public static final double FLYWHEEL_MAX_POWER = 1.0;
    public static final double INDEXER_REST_POS   = 0.0;
    public static final double INDEXER_FEED_POS   = 0.5;

    public Shooter(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // TODO: change these to match your configuration names
        flywheel = hardwareMap.get(DcMotorEx.class, "shooter");
        indexer  = hardwareMap.get(Servo.class, "indexer");

        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        indexer.setPosition(INDEXER_REST_POS);
    }

    /** Set flywheel power from 0–1. */
    public void setFlywheelPower(double power) {
        power = Math.max(0, Math.min(FLYWHEEL_MAX_POWER, power));
        flywheel.setPower(power);
        if (telemetry != null) {
            telemetry.addData("Shooter Power", "%.2f", power);
        }
    }

    /** Simple on/off helper. */
    public void setFlywheelOn(boolean on) {
        setFlywheelPower(on ? FLYWHEEL_MAX_POWER : 0);
    }

    /** Move indexer into feed position. You’ll likely pulse this from TeleOp. */
    public void feed() {
        indexer.setPosition(INDEXER_FEED_POS);
    }

    /** Return indexer to safe/resting position. */
    public void rest() {
        indexer.setPosition(INDEXER_REST_POS);
    }

    public void stop() {
        setFlywheelPower(0);
        rest();
    }
}
