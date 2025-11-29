package Drive;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Core.SystemsManager;

@TeleOp(name = "Basic Drive", group = "Tutorial")
public class BasicDrive extends LinearOpMode {

    private SystemsManager systems = new SystemsManager();

    @Override
    public void runOpMode() {

        systems.init(hardwareMap);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            double y = -gamepad1.left_stick_y;     // forward/back
            double x = gamepad1.left_stick_x;      // strafe
            double rx = -gamepad1.right_stick_x;   // rotate

            // D-Pad overrides
            if (gamepad1.dpad_up)    y = 0.25;
            if (gamepad1.dpad_down)  y = -0.25;
            if (gamepad1.dpad_left)  x = -0.25;
            if (gamepad1.dpad_right) x = 0.25;

            // Mecanum math
            double fl = y + x + rx;
            double bl = y - x + rx;
            double fr = y - x - rx;
            double br = y + x - rx;

            // Normalize powers
            double max = Math.max(Math.max(Math.abs(fl), Math.abs(bl)),
                    Math.max(Math.abs(fr), Math.abs(br)));
            if (max > 1) {
                fl /= max;
                bl /= max;
                fr /= max;
                br /= max;
            }

            systems.frontLeft.setPower(fl);
            systems.backLeft.setPower(bl);
            systems.frontRight.setPower(fr);
            systems.backRight.setPower(br);

            telemetry.addData("FL", fl);
            telemetry.addData("FR", fr);
            telemetry.addData("BL", bl);
            telemetry.addData("BR", br);
            telemetry.update();
        }
    }
}
