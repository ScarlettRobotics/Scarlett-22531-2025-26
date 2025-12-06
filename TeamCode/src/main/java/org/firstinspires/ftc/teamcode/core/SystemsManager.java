package org.firstinspires.ftc.teamcode.core;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class SystemsManager {

    public final Drive drive;
    public final Shooter shooter;

    public SystemsManager(HardwareMap hardwareMap, Telemetry telemetry) {
        // Create subsystems here
        drive   = new Drive(hardwareMap, telemetry);
        shooter = new Shooter(hardwareMap, telemetry);
    }

    public void stopAll() {
        drive.stop();
        shooter.stop();
    }
}
