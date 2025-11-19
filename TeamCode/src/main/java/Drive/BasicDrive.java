package Drive;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Basic Drive", group = "Tutorial")
public class BasicDrive extends LinearOpMode {

    // Declare motors
    private DcMotor leftMotor;
    private DcMotor rightMotor;

    @Override
    public void runOpMode() {

        // Connect motors to hardware names from the Robot Controller configuration
        leftMotor  = hardwareMap.get(DcMotor.class, "leftMotor");
        rightMotor = hardwareMap.get(DcMotor.class, "rightMotor");

        // Reverse one motor if needed so both move forward
        rightMotor.setDirection(DcMotor.Direction.REVERSE);

        // Tell Driver Station robot is ready
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for the game to start
        waitForStart();

        // Run until STOP is pressed
        while (opModeIsActive()) {
            // Tank drive control
            double leftPower  = -gamepad1.left_stick_y;  // forward/back
            double rightPower = -gamepad1.right_stick_y; // forward/back

            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);

            telemetry.addData("Left Power", leftPower);
            telemetry.addData("Right Power", rightPower);
            telemetry.update();
        }
    }
}
