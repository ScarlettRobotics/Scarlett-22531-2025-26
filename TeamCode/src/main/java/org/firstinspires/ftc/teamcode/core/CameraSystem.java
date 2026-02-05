package org.firstinspires.ftc.teamcode.core;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

// Vision SDK imports are included to keep this subsystem "ready",
// but NOTHING is initialized unless enable() is explicitly called.
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;

/**
 * Camera / Vision subsystem.
 *
 * Design goals:
 *  - Keep robot startup deterministic (no hidden USB/camera bring-up).
 *  - Centralize all vision lifecycle code in one place.
 *  - Make it safe to call from any OpMode (no side effects unless enabled).
 *  - Provide a clean expansion path for AprilTags / custom pipelines later.
 *
 * Current behavior:
 *  - By default, this subsystem is "OFF" and performs no camera initialization.
 *  - If the team chooses to turn vision on later, call enable() once (typically in init-loop).
 */
public final class CameraSystem {

    // ---------------- Configuration ----------------
    public static final class Config {
        /** Must match the RC configuration name */
        public String webcamName = "Webcam 1";

        /** If false, the subsystem remains OFF and never touches VisionPortal. */
        public boolean enableVision = false;

        /** Optional: if enabled later, controls stream/camera resource policy. */
        public boolean closeOnStop = true;

        /** Optional: log extra telemetry. */
        public boolean verboseTelemetry = true;
    }

    // ---------------- State / lifecycle ----------------
    public enum State {
        OFF,            // vision disabled by config
        IDLE,           // vision enabled but not initialized yet
        INITIALIZING,   // building VisionPortal
        ACTIVE,         // portal active
        FAULTED,        // failed to initialize
        STOPPED         // closed and no longer active
    }

    private final HardwareMap hardwareMap;
    private final Telemetry telemetry;
    private final Config config;

    private State state = State.OFF;

    // VisionPortal is intentionally null unless enable() is called.
    private VisionPortal portal = null;

    // ---------------- Simple health bookkeeping ----------------
    private String lastError = "";
    private long lastStateChangeMs = 0;

    // We track "observability" without implying vision is running.
    private int enableRequests = 0;
    private int initAttempts = 0;

    public CameraSystem(HardwareMap hw, Telemetry tel) {
        this(hw, tel, new Config());
    }

    public CameraSystem(HardwareMap hw, Telemetry tel, Config cfg) {
        hardwareMap = hw;
        telemetry = tel;
        config = (cfg == null) ? new Config() : cfg;

        // Important: constructor has NO side effects (no USB, no camera init).
        // We only set the initial state based on configuration.
        setState(config.enableVision ? State.IDLE : State.OFF);

        if (telemetry != null && config.verboseTelemetry) {
            telemetry.addData("Vision", "Ready (%s)", state);
        }
    }

    // =========================================================
    // Public API
    // =========================================================

    /** Current subsystem state (safe to call anytime). */
    public State getState() {
        return state;
    }

    /** Returns true only if VisionPortal is actually active. */
    public boolean isActive() {
        return state == State.ACTIVE && portal != null;
    }

    /**
     * Explicitly enable vision. This is the ONLY method that may initialize VisionPortal.
     * If you never call this, the subsystem will do nothing.
     */
    public void enable() {
        enableRequests++;

        if (!config.enableVision) {
            // Configuration lock: keep deterministic behavior close to competition.
            setState(State.OFF);
            return;
        }

        // If already active/initializing/faulted, do not re-init repeatedly.
        if (state == State.ACTIVE || state == State.INITIALIZING || state == State.FAULTED) return;

        // Lazy init: build the portal only when the OpMode decides to.
        initPortal();
    }

    /** Optional: explicit disable for matches where vision is unwanted. */
    public void disable() {
        config.enableVision = false;
        stop();
        setState(State.OFF);
    }

    /**
     * Periodic telemetry/health update. Safe to call in init-loop or run-loop.
     * Does not initialize anything.
     */
    public void updateTelemetry() {
        if (telemetry == null) return;

        telemetry.addData("Vision state", state);
        telemetry.addData("Enable requests", enableRequests);
        telemetry.addData("Init attempts", initAttempts);

        if (state == State.FAULTED && lastError != null && !lastError.isEmpty()) {
            telemetry.addData("Vision error", lastError);
        }

        if (portal != null) {
            // This will only appear if enable() was called successfully.
            telemetry.addData("Camera state", portal.getCameraState());
            telemetry.addData("FPS", "%.1f", portal.getFps());
        }
    }

    /** Clean shutdown. Safe to call even if vision was never enabled. */
    public void stop() {
        if (portal != null && config.closeOnStop) {
            try {
                portal.close();
            } catch (Exception ignored) {
                // Closing failures should never crash an OpMode.
            }
        }
        portal = null;

        if (state == State.ACTIVE || state == State.INITIALIZING) {
            setState(State.STOPPED);
        }
    }

    // =========================================================
    // Internal implementation
    // =========================================================

    private void initPortal() {
        initAttempts++;
        setState(State.INITIALIZING);

        try {
            // NOTE: No processors are added here.
            // When you decide to use AprilTags or a custom pipeline, you add a processor
            // and store it as a member (then read its detections during Autonomous).
            portal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, config.webcamName))
                    // .addProcessor(aprilTagProcessor)
                    // .setStreamFormat(VisionPortal.StreamFormat.YUY2)
                    .build();

            lastError = "";
            setState(State.ACTIVE);

        } catch (Exception e) {
            portal = null;
            lastError = (e.getMessage() == null) ? e.toString() : e.getMessage();
            setState(State.FAULTED);
        }
    }

    private void setState(State newState) {
        state = newState;
        lastStateChangeMs = System.currentTimeMillis();

        if (telemetry != null && config.verboseTelemetry) {
            telemetry.addData("Vision", "State -> %s", state);
        }
    }
}
