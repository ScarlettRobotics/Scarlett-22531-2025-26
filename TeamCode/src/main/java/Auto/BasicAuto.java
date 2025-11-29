package Auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import Core.SystemsManager;

@Autonomous(name = "Basic Auto", group = "Tutorial")
public class BasicAuto extends LinearOpMode {

    private SystemsManager systems = new SystemsManager();

    @Override
    public void runOpMode() throws InterruptedException {

        systems.init(hardwareMap);

        telemetry.addLine("Basic Auto Ready");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {

            // Move forward
            systems.setAllPower(0.4);
            sleep(3000);

            // Stop
            systems.setAllPower(0);

            telemetry.addLine("Done!");
            telemetry.update();
        }
    }
}
