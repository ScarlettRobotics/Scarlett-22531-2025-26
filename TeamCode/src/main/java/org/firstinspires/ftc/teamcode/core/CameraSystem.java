package org.firstinspires.ftc.teamcode.core;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;

/**
 * Camera system: initializes a webcam.
 * No processing yet (AprilTags / pipelines can be added later).
 *
 * Hardware name must match RC config: "Webcam 1"
 */
public class CameraSystem {

    private VisionPortal portal;
    private final Telemetry telemetry;

    public CameraSystem(HardwareMap hw, Telemetry tel) {
        telemetry = tel;

        try {
            portal = new VisionPortal.Builder()
                    .setCamera(hw.get(WebcamName.class, "Webcam 1"))
                    .build();

            if (telemetry != null) telemetry.addLine("Camera: initialized");
        } catch (Exception e) {
            if (telemetry != null) telemetry.addData("Camera init failed", e.getMessage());
        }
    }

    public void stop() {
        if (portal != null) {
            portal.close();
            portal = null;
        }
    }
}
