package org.firstinspires.ftc.teamcode.core;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Robot container: owns all subsystems.
 *
 * TeleOp / Auto should only talk to Robot, not raw hardware.
 */
public class Robot {

    public final Drive drive;
    public final Shooter shooter;
    public final CameraSystem camera;

    public Robot(HardwareMap hw, Telemetry tel) {
        drive = new Drive(hw, tel);
        shooter = new Shooter(hw, tel);
        camera = new CameraSystem(hw, tel);

        if (tel != null) {
            tel.addLine("Robot: initialized");
            tel.update();
        }
    }

    public void stopAll() {
        drive.stop();
        shooter.stop();
        camera.stop();
    }
}
