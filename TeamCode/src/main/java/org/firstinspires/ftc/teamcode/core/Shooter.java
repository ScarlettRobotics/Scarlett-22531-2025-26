package org.firstinspires.ftc.teamcode.core;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.CRServo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Shooter {

    private final DcMotorEx flywheel;
    private final CRServo   midRoller;
    private final Servo     leftIndexer;
    private final Servo     rightIndexer;
    private final Telemetry telemetry;

    // ========= TUNABLE CONSTANTS =========
    public static final double FLYWHEEL_MAX_POWER = 1.0;

    // CRServo power for mid roller (negative if you want it to spin "backwards")
    public static final double MID_ROLLER_POWER   = 0.6;

    // Indexer positions – tune on the bot
    public static final double LEFT_REST_POS      = 0.45;
    public static final double RIGHT_REST_POS     = 0.55;
    public static final double LEFT_FEED_POS      = 0.15;
    public static final double RIGHT_FEED_POS     = 0.85;

    public Shooter(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // ---- NAMES MUST MATCH ROBOT CONFIG ----
        flywheel     = hardwareMap.get(DcMotorEx.class, "shooter");
        midRoller    = hardwareMap.get(CRServo.class,   "midRoller");
        leftIndexer  = hardwareMap.get(Servo.class,     "leftIndexer");
        rightIndexer = hardwareMap.get(Servo.class,     "rightIndexer");

        // Shooter motor direction – flip if wrong
        flywheel.setDirection(DcMotor.Direction.FORWARD);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Safe default state: nothing moving, indexers holding
        restIndexers();
        midRoller.setPower(0);
        flywheel.setPower(0);
    }

    // ---------- FLYWHEEL ----------

    /** Set flywheel power from 0–1 (clamped). */
    public void setFlywheelPower(double power) {
        double clipped = Math.max(0, Math.min(FLYWHEEL_MAX_POWER, power));
        flywheel.setPower(clipped);

        if (telemetry != null) {
            telemetry.addData("Flywheel Power", "%.2f", clipped);
        }
    }

    /** Simple on/off helper. */
    public void setFlywheelOn(boolean on) {
        setFlywheelPower(on ? FLYWHEEL_MAX_POWER : 0);
    }

    // ---------- MID ROLLER ----------

    /** Mid roller on/off. Direction set by MID_ROLLER_POWER sign. */
    public void setMidRollerOn(boolean on) {
        midRoller.setPower(on ? MID_ROLLER_POWER : 0);
        if (telemetry != null) {
            telemetry.addData("Mid Roller", on ? "ON (%.2f)" : "OFF", MID_ROLLER_POWER);
        }
    }

    // ---------- INDEXER SERVOS ----------

    /** Move both indexers into feed position (push ball into flywheel). */
    public void feedIndexers() {
        leftIndexer.setPosition(LEFT_FEED_POS);
        rightIndexer.setPosition(RIGHT_FEED_POS);

        if (telemetry != null) {
            telemetry.addData("Indexers", "FEED (L=%.2f R=%.2f)",
                    LEFT_FEED_POS, RIGHT_FEED_POS);
        }
    }

    /** Return both indexers to resting/holding position. */
    public void restIndexers() {
        leftIndexer.setPosition(LEFT_REST_POS);
        rightIndexer.setPosition(RIGHT_REST_POS);

        if (telemetry != null) {
            telemetry.addData("Indexers", "REST (L=%.2f R=%.2f)",
                    LEFT_REST_POS, RIGHT_REST_POS);
        }
    }

    // ---------- EMERGENCY STOP ----------

    public void stop() {
        setFlywheelOn(false);
        setMidRollerOn(false);
        restIndexers();
    }
}
