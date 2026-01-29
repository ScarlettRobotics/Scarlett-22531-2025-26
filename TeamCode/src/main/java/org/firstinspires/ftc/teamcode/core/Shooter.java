package org.firstinspires.ftc.teamcode.core;

import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Shooter subsystem.
 *
 * Simple and reliable:
 *  - ON: flywheel + mid roller + indexers to feed position
 *  - OFF: everything stops, indexers return to rest
 */
public class Shooter {

    private final DcMotorEx flywheel;
    private final CRServo midRoller;
    private final Servo leftIndexer;
    private final Servo rightIndexer;
    @SuppressWarnings("unused")
    private final Telemetry telemetry;

    // Tunables
    private static final double FLYWHEEL_POWER = 1.0;
    private static final double MID_ROLLER_POWER = 1.0;

    private static final double LEFT_REST = 0.50;
    private static final double RIGHT_REST = 0.50;
    private static final double LEFT_FEED = 0.05;
    private static final double RIGHT_FEED = 0.95;

    public Shooter(HardwareMap hw, Telemetry tel) {
        telemetry = tel;

        flywheel = hw.get(DcMotorEx.class, "shooter");
        midRoller = hw.get(CRServo.class, "midRoller");
        leftIndexer = hw.get(Servo.class, "leftIndexer");
        rightIndexer = hw.get(Servo.class, "rightIndexer");

        flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        stop();
    }

    public void on() {
        flywheel.setPower(FLYWHEEL_POWER);
        midRoller.setPower(MID_ROLLER_POWER);
        feed();
    }

    public void stop() {
        flywheel.setPower(0);
        midRoller.setPower(0);
        rest();
    }

    public void feed() {
        leftIndexer.setPosition(LEFT_FEED);
        rightIndexer.setPosition(RIGHT_FEED);
    }

    public void rest() {
        leftIndexer.setPosition(LEFT_REST);
        rightIndexer.setPosition(RIGHT_REST);
    }
}
