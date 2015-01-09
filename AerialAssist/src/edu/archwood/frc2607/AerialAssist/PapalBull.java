/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.archwood.frc2607.AerialAssist;

import edu.archwood.frc2607.utils.TempCorrectedGyro;
import edu.archwood.frc2607.utils.robovikingStick;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.RobotDrive.MotorType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class PapalBull extends IterativeRobot implements Runnable, CompBotConstants {

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    private RobotDrive theDriveinator;
    private robovikingStick xboxSpeedRacer, coPilotOfDoom, overrideSwitches;
    private Compressor theCompressinator;
    private Thread driveThread;
    private Runnable shootRun;
    private Solenoid shifter, chaputpult, pickupArm;
    private Victor holyRollers;
    private Winder winder;
    private DigitalInput chaputpultReadySwitch;
    private boolean inHighGear = true;              // highGear is the default, therefore shifter solenoid gets set to !inHighGear
    private boolean changeGears = false;            // a one shot to detect if we need to change PID parameters
    private boolean isFlippedOhNo = true;          // flips forward and reverse for the driver....but WHY???!!!
    private boolean launchTheBallAndWin = false;    // launches the catapult
    private boolean deployTheHolyRollers = false;   // deploys and retracts the pickup; pickup solenoid gets set to this (default = false = retracted)
    private boolean disabledBothHot = false,
            disabledLeftOnly = false,
            disabledRightOnly = false;
    private TCPDashboard dashingTTS;
    private boolean ranTeleop = false;
    //private DualSONARs sonarSensors;
    private DriverStationLCD lcd = DriverStationLCD.getInstance();
    private DigitalOutput lED;
    private int tick;
    private boolean retract = false;
    LEDManager leds;
    boolean BROKEN = false;
    int brokenTalon = 0;
    WheelRPMController TalonTeamate[][] = new WheelRPMController[4][3];
    private TempCorrectedGyro gyro;
    private boolean useGyro = false;
    
    public void robotInit() {
        System.out.println("inSanity Check");
//        leds = new LEDManager();
        dashingTTS = new TCPDashboard();
        dashingTTS.start();
        dashingTTS.setAlliance(DriverStation.getInstance().getAlliance() == Alliance.kBlue);
        theCompressinator = new Compressor(diPressureSwitch, relayCompressor);
        theCompressinator.start();
        initAutoTimer();
        driveThread = new Thread(this);
        gyro = new TempCorrectedGyro(analogTempSensor, analogGyro);
        shootRun = (new Runnable() {
            public void run() {
                chaputpult.set(true);
                try {
                    Thread.sleep(1250);
                } catch (Exception e) {
                }
                chaputpult.set(false);
            }
        });
        lED = new DigitalOutput(digitalOutputLED);
        xboxSpeedRacer = new robovikingStick(1);
        coPilotOfDoom = new robovikingStick(2);
        overrideSwitches = new robovikingStick(3);

        shifter = new Solenoid(solenoidShifter);
        chaputpult = new Solenoid(solenoidLauncher);
        pickupArm = new Solenoid(solenoidPickup);

        holyRollers = new Victor(pwmHolyRollers);
        chaputpultReadySwitch = new DigitalInput(diLauncherSwitch);
        if (COMP) {
            winder = new Winder(new Victor(pwmChaputpultWinder));
        } else {
            winder = new Winder(new Relay(8));
        }
        //sonarSensors = new DualSONARs(diLeftRX,diRightRX,doLeftTX,doRightTX,analogLeftSonar,analogRightSonar);

        driveThread.start();
        Thread retractor = new Thread(new Runnable() {
            public void run() {
                int windCount = 0;
                while (true) {
                    if (isEnabled()) {
                        if ((retract) && (chaputpultReadySwitch.get()) && ( !chaputpult.get())) {
//                            chaputpultWinder.set(-1.00);
                            if (windCount < 40) {
                                windCount++;
                                winder.windSlowly();
                            } else {
                                winder.wind();
                            }
                        } else {
//                            chaputpultWinder.set(0);
                            winder.stop();
                            windCount = 0;
                        }
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        });
        retractor.start();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void disabledInit() {

    }

    public void disabledPeriodic() {
        String autonModes[] = new String[]{"Comm Test    ", "1 Ball Left  ",
            "1 Ball Right ", "2 Ball 1 Goal","2 ball 2 goal",
            "2 Ball Backup", "THREE BALL! OMG!", "Drive Only!!"};
        dashBoard();
        if (xboxSpeedRacer.getButtonToggle(8)) {
            autonMode++;
            useGyro = !useGyro;
        }
        if (autonMode > 7) {
            autonMode = 0;
        }

        disabledBothHot = dashingTTS.areBothHot();
        disabledLeftOnly = !disabledBothHot && dashingTTS.isLeftHot();
        disabledRightOnly = !disabledBothHot && dashingTTS.isRightHot();

        lcd.println(DriverStationLCD.Line.kUser1, 1, "Auton: " + autonModes[autonMode] + "        ");
        lcdGoalMessage(disabledBothHot, disabledLeftOnly, disabledRightOnly);
        lcd.println(DriverStationLCD.Line.kUser3, 1, "useGyro: " + useGyro + "      ");
        lcd.updateLCD();
    }

    private void lcdGoalMessage(boolean both, boolean left, boolean right) {
        String bothGoals = ((both) ? "T" : "F");
        String leftGoal = ((!both && left) ? "T" : "F");
        String rightGoal = ((!both && right) ? "T" : "F");
        lcd.println(DriverStationLCD.Line.kUser2, 1, "2G: " + bothGoals + " LG: "
                + leftGoal + " RG: " + rightGoal + "        ");

    }

    private void fireTheCatapult() {
        if (pickupArm.get() == true) {
            new Thread(shootRun).start();
        }
    }

    private Timer autoTimer;

    public void resetAutoTimer() {
        autoTimer.reset();
    }

    public int getAutoTime() {
        return (int) (autoTimer.get() * 50d);
    }

    public void startAutoTimer() {
        autoTimer.start();
        autoTimer.reset();
    }

    public void stopAutoTimer() {
        autoTimer.stop();
    }

    public void initAutoTimer() {
        autoTimer = new Timer();
    }

    private void nextAutoStep() {
        autoTimer.reset();
        ++step;
        tickAuto = 0;
    }
    public int step = 1;
    public int tickAuto = 0;
    public int autonMode = 0;
    public boolean backupAfterFirstShot = false;
    
    public void autonomousInit() {
        tickAuto = 0;
        step = 1;
        zGol = 0;
        yGol = 0;
        xGol = 0;
        startAutoTimer();
    }

    public void autonomousPeriodic() {
        tickAuto = getAutoTime();
        switch (autonMode) {  //Add numbers to DisabledPeriodic!
            case 0:
                commTest();
                break;
            case 1:
                oneBallLeftAuton();
                break;
            case 2:
                oneBallRightAuton();
                break;
            case 3:
                backupAfterFirstShot = false;
                twoBallSameGoalAuton();
                break;
            case 4:
                twoBallAuton();
                break;
            case 5:
//                twoBallLeftGoal();
                backupAfterFirstShot = true;
                twoBallSameGoalAuton();
                break;
            case 6:
                allianceAuton();
                break;
            case 7:
                driveOnly();
                break;
        }
           //John deprecated this
        //tickAuto++;
    }

    public void driveOnly() {
        switch (step) {
            case 1:
                yGol = -.5;
                if (tickAuto >= 50) {
                    nextAutoStep();
                }
                break;
            case 2:
                yGol = 0;
                break;
        }
    }
    
    public void commTest() {
        if (tickAuto >= 35) {
            System.out.print("hotLeft: " + dashingTTS.isLeftHot());
            System.out.print(" hotRight: " + dashingTTS.isRightHot());
            System.out.println(" both: " + dashingTTS.areBothHot());
            tickAuto = 0;
        }
    }

    public void oneBallLeftAuton() {
        switch (step) {
            case 1:
                pickupArm.set(true);
                shifter.set(true);
                if ((!dashingTTS.areBothHot()) || tickAuto >= 350) {
                    if ((dashingTTS.isLeftHot() && tickAuto > 100) || tickAuto >= 350) {
                        nextAutoStep();
                    }
                }
                break;
            case 2:
                fireTheCatapult();
                nextAutoStep();
                break;
            case 3:
                if (tickAuto >= 25) {
                    nextAutoStep();
                }
                break;
            case 4:
                yGol = -.5;
                if (tickAuto >= 50) {
                    nextAutoStep();
                }
                break;
            case 5:
                yGol = 0;
                break;
        }
    }

    public void oneBallRightAuton() {
        switch (step) {
            case 1:
                pickupArm.set(true);
                shifter.set(true);
                if ((!dashingTTS.areBothHot() && tickAuto > 100) || tickAuto >= 350) {
                    if (dashingTTS.isRightHot() || tickAuto > 350) {
                        nextAutoStep();
                    }
                }
                break;
            case 2:
                fireTheCatapult();
                nextAutoStep();
                break;
            case 3:
                if (tickAuto >= 25) {
                    nextAutoStep();
                }
                break;
            case 4:
                yGol = -.5;
                if (tickAuto >= 50) {
                    nextAutoStep();
                }
                break;
            case 5:
                yGol = 0;
                break;
        }
    }

    public void twoBallSameGoalAuton() {
        switch (step) {
            case 1:
                
                pickupArm.set(true);
                shifter.set(true);
                if (tickAuto >= 30) {
                    System.out.println("Starting auton!");
                    nextAutoStep();
                }
                break;
            case 2:
                System.out.println("PK FIRE!");
                fireTheCatapult();
                nextAutoStep();
                break;
            case 3:
                if (tickAuto >= 75 || !chaputpult.get()) {
                    retract = true;
                }
                if ((!chaputpultReadySwitch.get() && tickAuto >= 140) || tickAuto >= 220) {
                    retract = false;
                    System.out.println("Done retracting...");
                    nextAutoStep();
                }
                break;
            case 4:
                holyRollers.set(.65);
                if (tickAuto >= 225) {
                    System.out.println("Done sucking...");
                    holyRollers.set(0);
                    nextAutoStep();
                }
                break;
            case 5:
                if (backupAfterFirstShot) {
                    yGol = .15;
                    pickupArm.set(false);
                } else {
                    yGol = -.05;
                }
                if (tickAuto >= 25) {
                    System.out.println("Wait for itttt...");
                    yGol = 0;
                    if (backupAfterFirstShot) {
                        if (tickAuto >= 35) {
                            pickupArm.set(true);
                            nextAutoStep();
                        }
                    } else {
                        nextAutoStep();
                    }
                }
                break;
            case 6:
                fireTheCatapult();
                if (tickAuto >= 15) {
                    System.out.println("Shoot harder!");
                    nextAutoStep();
                }
                break;
            case 7:
                yGol = -.5;
                if (tickAuto > 65) {
                    System.out.println("Done jolting!");
                    nextAutoStep();
                }
                break;
            case 8:
                System.out.print("Excess auton time: ");
                System.out.print(tickAuto);
                System.out.println(" ticks.");
                yGol = 0;
                break;
        }
    }

    public void allianceAuton() {
        switch (step) {
            case 1:
                pickupArm.set(true);
                shifter.set(true);
                if (tickAuto >= 20) {
                    nextAutoStep();
                }
                break;
            case 2:
                fireTheCatapult();
                nextAutoStep();
                break;
            case 3:
                if (tickAuto >= 25 || !chaputpult.get()) {
                    retract = true;
                }
                if ((!chaputpultReadySwitch.get() && tickAuto >= 50) || tickAuto >= 200) {
                    retract = false;
                    nextAutoStep();
                }
                break;
            case 4:
                holyRollers.set(.65);
                if (tickAuto >= 25) {
                    holyRollers.set(0);
                    pickupArm.set(false);
                    nextAutoStep();
                }
                break;
            case 5:
                
                if (tickAuto >= 50) {
                    pickupArm.set(true);
                    if (tickAuto >= 60) {
                        nextAutoStep();
                    }
                }
                break;
            case 6:
                fireTheCatapult();
                if (tickAuto >= 15) {
                    nextAutoStep();
                }
                break;
            case 7:
                
                if (tickAuto >= 75 || !chaputpult.get()) {
                    retract = true;
                }
                if ((!chaputpultReadySwitch.get() && tickAuto >= 200) || tickAuto >= 300) {
                    retract = false;
                    nextAutoStep();
                }
                break;
            case 8:
                if (tickAuto >= 10 && tickAuto <= 45) {
                    yGol = .3;
                }
                holyRollers.set(.65);
                if (tickAuto >= 25) {
                    holyRollers.set(0);
                    //pickupArm.set(false);
                    nextAutoStep();
                }
                break;
            case 9:
                    yGol = -.3;
                    if (tickAuto >= 25) {
                    nextAutoStep();
                    }
                break;
            case 10:
                fireTheCatapult();
                if (tickAuto >= 15) {
                    nextAutoStep();
                }
                break;
            case 11:
                yGol = -.5;
                if (tickAuto > 50) {
                    nextAutoStep();
                }
                break;
            case 12:
                yGol = 0;
                break;
        }
    }

        
    public void twoBallLeftGoal() {
        switch (step) {
            case 1:
                if (dashingTTS.isLeftHot()) {
                    nextAutoStep();
                } else {
                    if (tickAuto > 200) {
                        nextAutoStep();
                    }
                }
                break;
            case 2:
                pickupArm.set(true);
                shifter.set(true);
                if (tickAuto >= 15) {
                    nextAutoStep();
                }
                break;
            case 3:
                fireTheCatapult();
                nextAutoStep();
                break;
            case 4:
                if (tickAuto >= 35 || !chaputpult.get()) {
                    retract = true;
                }
                if ((!chaputpultReadySwitch.get() && tickAuto >= 50) || tickAuto >= 200) {
                    retract = false;
                    nextAutoStep();
                }
                break;
            case 5:
                holyRollers.set(.65);
                if (tickAuto >= 25) {
                    holyRollers.set(0);
                    pickupArm.set(false);
                    nextAutoStep();
                }
                break;
            case 6:
                if (tickAuto >= 35) {
                    pickupArm.set(true);
                    if (tickAuto>=50)
                    {
                        nextAutoStep();
                    }
                }
                break;
            case 7:
                fireTheCatapult();
                if (tickAuto >= 15) {
                    nextAutoStep();
                }
                break;
            case 8:
                yGol = -.5;
                if (tickAuto > 15) {
                    nextAutoStep();
                }
                break;
            case 9:
                yGol = 0;
                break;
        }
    }

    public void twoBallAuton() {
        double rotateVal = 0.0;
        /*   to be finished....
         switch (step) {
         // drive forward just enough to deploy pickup without hitting ball
         case 1:             
         shifter.set(true);   // go into low gear
         yGol = -.1;
         if (tickAuto > 10) {
         yGol = 0.0;
         nextAutoStep();
         }
         break;
         // turn slightly towards hot goal
         case 2: 
         if (!dashingTTS.areBothHot() && tickAuto > 10) {
         if (dashingTTS.isLeftHot()) {
         rotateVal = .12;  
         }
         else if (dashingTTS.isRightHot()) {
         rotateVal = -.12;                            
         }  
         nextAutoStep();
         }
         break;
         case 3:
         zGol = rotateVal;
         if (tickAuto >= 10) {
         zGol = 0.0;
         nextAutoStep();
         }
         break;
                    
         // deploy the pickup    
         case 4:
         pickupArm.set(true);
         if (tickAuto >= 25) {
         nextAutoStep();
         }
         break;
                    
         // shoot
         case 5:
         fireTheCatapult();
         nextAutoStep();
         break;
                
         // wait for catapult to fire, then start retracting and reverse turn
         case 6:
         if (tickAuto >=75) {
         retract = true;
         zGol = -rotateVal;
         nextAutoStep();
         }
         break;
                    
         // stop turn
         case 7: 
         if (tickAuto > 10) {
         zGol =0.0;
         }
         // TODO:  finish.....
         break;
         case 8:  
         break;
         }
         */
    }

    public void teleopInit() {
        tick = 0;
        ranTeleop = true;
    }

    /**
     * This function is called periodically during operator control
     */
    double[] driveVal = new double[3];
    double[] deadZones = new double[]{0.15, 0.15, 0.15};
    double xVal, yVal, zVal, xGol, yGol, zGol, launchTrigger = 0, oldLaunchTrigger = 0;

    public void teleopPeriodic() {

        xGol = (isFlippedOhNo ? -1 : 1) * (xboxSpeedRacer.getX());
        yGol = (isFlippedOhNo ? -1 : 1) * (xboxSpeedRacer.getY());
        zGol = -(xboxSpeedRacer.getRawAxis(4));
        driveVal[0] = xGol;
        driveVal[1] = yGol;
        driveVal[2] = zGol;
        for (int i = 0; i < 2; i++) {
            if (Math.abs(driveVal[i]) <= deadZones[i]) {
                driveVal[i] = 0;
            }
            if (driveVal[i] > deadZones[i] && driveVal[i] <= deadZones[i] * 2) {
                driveVal[i] = (driveVal[i] - .15) * 2;
            }
            if (driveVal[i] < -deadZones[i] && driveVal[i] >= -2 * deadZones[i]) {
                driveVal[i] = (driveVal[i] + .15) * 2;
            }
        }

        xGol = driveVal[0];
        yGol = driveVal[1];
        zGol = driveVal[2] * .40;
        launchTrigger = xboxSpeedRacer.getZ() + coPilotOfDoom.getZ();
        launchTheBallAndWin = (launchTrigger < -.9 && oldLaunchTrigger > -.8);
        oldLaunchTrigger = launchTrigger;

        if ((!chaputpult.get()) && launchTheBallAndWin && (overrideSwitches.getRawButton(3) || !chaputpultReadySwitch.get())) {
            fireTheCatapult();
        }
        if (chaputpultReadySwitch.get()&&(xboxSpeedRacer.getButtonToggle(7)||coPilotOfDoom.getButtonToggle(7)))
        {
            chaputpult.set(!chaputpult.get());
        }
        if (xboxSpeedRacer.getButtonToggle(8) || coPilotOfDoom.getButtonToggle(8)) {
            isFlippedOhNo = !isFlippedOhNo;
        }

        if (xboxSpeedRacer.getButtonToggle(4) || coPilotOfDoom.getButtonToggle(4)) {   // deploy or retract pickup
            deployTheHolyRollers = !deployTheHolyRollers;
        }

        if (xboxSpeedRacer.getRawButton(5) || coPilotOfDoom.getRawButton(5)) {   // pickup motor (grab ball, or release ball)
            holyRollers.set(-.75);
        } else if (xboxSpeedRacer.getRawButton(2) || coPilotOfDoom.getRawButton(2) || (launchTrigger > .8)) {
            holyRollers.set(.75);
        } else {
            holyRollers.set(0);
        }

        if (xboxSpeedRacer.getButtonToggle(1)) {
            inHighGear = !inHighGear;
            shifter.set(!inHighGear);
            changeGears = true;
        }

        // wind the launcher
        retract = ((xboxSpeedRacer.getRawButton(3) || coPilotOfDoom.getRawButton(3)));
        pickupArm.set(deployTheHolyRollers);

    }

    double pctSpeed = 0.0;

    public void testInit() {
        tick = 0;
        gyro.reset();
    }

    public void dashBoard() {
        dashingTTS.setCatState(chaputpultReadySwitch.get());
        dashingTTS.setRollerState(pickupArm.get());
        dashingTTS.setShifterState(shifter.get());
        //sonarSensors.poll(System.currentTimeMillis()/1000d);
        //dashingTTS.setLeftSONAR(sonarSensors.getLeftRange());
        //dashingTTS.setRightSONAR(sonarSensors.getRightRange());
    }

    public void testPeriodic() {
        if (xboxSpeedRacer.getButtonToggle(8)) {
            inHighGear = !inHighGear;
            shifter.set(!inHighGear);
            changeGears = true;
        }

        if (xboxSpeedRacer.getRawButton(4) || coPilotOfDoom.getRawButton(4)) {
            yGol = -.5;
            xGol = 0.0;
            zGol = 0.0;
        } else if (xboxSpeedRacer.getRawButton(1) || coPilotOfDoom.getRawButton(1)) {
            yGol = .5;
            xGol = 0.0;
            zGol = 0.0;
        } else if (xboxSpeedRacer.getRawButton(3) || coPilotOfDoom.getRawButton(3)) {
            yGol = 0.0;
            xGol = -.5;
            zGol = 0.0;
        } else if (xboxSpeedRacer.getRawButton(2) || coPilotOfDoom.getRawButton(2)) {
            yGol = 0.0;
            xGol = .5;
            zGol = 0.0;
        } else {
            yGol = 0.0;
            xGol = 0.0;
            zGol = 0.0;
        }
        
        if (++tick >= 25) {
        	System.out.println("Gyro: " + gyro.getRelativeAngle());
        	tick = 0;
        }

    }

    public void run() {
        TalonDriveWithGyro();
    }

    // don't use (yet)
    private void TalonDriveWithGyro() {
    	// adds gyro correction for mecanum base
        WheelRPMController leftFrontMotors, rightFrontMotors, leftRearMotors, rightRearMotors;

        System.out.println("starting TalonDriveWithGyro thread...");
        leftFrontMotors = new WheelRPMController("leftFrontMotors", leftFrontIndex);
        rightFrontMotors = new WheelRPMController("rightFrontMotors", rightFrontIndex);
        leftRearMotors = new WheelRPMController("leftRearMotors", leftRearIndex);
        rightRearMotors = new WheelRPMController("rightRearMotors", rightRearIndex);

        leftFrontMotors.enable();
        leftRearMotors.enable();
        rightFrontMotors.enable();
        rightRearMotors.enable();
        System.out.println("TalonDriveWithGyro thread: WheelRPMControllers initialized");
        
        /*
         * zVal is the rotation commanded by the driver....if driver doesn't want to rotate, use
         * gyro to maintain last heading
         * 
         */
        int threadTick = 0;
        boolean gyroReset = false;
        while (true) {
        	boolean driveStraight = (Math.abs(zVal) <= .03) ? true : false;
        	
        	if (driveStraight && !gyroReset) {
        		gyroReset = true;
        		gyro.reset();
        	}
        	
        	if (!driveStraight) gyroReset = false;
        	
        	double rotVal = zVal;
        	if (driveStraight && useGyro) rotVal = gyro.getRelativeAngle() * -.0025;
        	
        	// send motor wheel speeds (range -1.0 to 1.0, WheelRPMController takes care of scaling to RPM values)
        	double frontLeftSpeed = xVal + yVal + rotVal,
        		   frontRightSpeed = -xVal + yVal - rotVal,
        		   rearLeftSpeed = -xVal + yVal + rotVal,
        		   rearRightSpeed = xVal + yVal - rotVal;
        	
        	double maxWheel = Math.abs(frontLeftSpeed);
        	if (Math.abs(frontRightSpeed) > maxWheel) maxWheel = Math.abs(frontRightSpeed);
        	if (Math.abs(rearLeftSpeed) > maxWheel) maxWheel = Math.abs(rearLeftSpeed);
        	if (Math.abs(rearRightSpeed) > maxWheel) maxWheel = Math.abs(rearRightSpeed);
        	
        	if (maxWheel > 1.0) {
        		frontLeftSpeed /= maxWheel;
        		frontRightSpeed /= maxWheel;
        		rearLeftSpeed /= maxWheel;
        		rearRightSpeed /= maxWheel;
        	}
        	
        	leftFrontMotors.set(frontLeftSpeed);
        	rightFrontMotors.set(frontRightSpeed);
        	leftRearMotors.set(rearLeftSpeed);
        	rightRearMotors.set(rearRightSpeed);
        	
        	if (++threadTick >= 25) {
        		System.out.println("Gyro: " + gyro.getRelativeAngle());
        		threadTick = 0;
        	}
        	
        	try {
        		Thread.sleep(20);
        	} catch (Exception e) {}
        }
    }
    
    
    private void TalonDriveTrain() {
        // NOTE:  do NOT use RobotDrive.setMaxOutput for the Talons....due to the 
        //          encoder max speeds being different on the one gearbox, we need 
        //          WheelRPMController to scale to the desired RPM setpoint.  We want
        //          RobotDrive to just set speed in range -1.0 to 1.0
        //          For the Jags onboard PID, RobotDrive sets the RPM setpoint directly
        WheelRPMController leftFrontMotors, rightFrontMotors, leftRearMotors, rightRearMotors;

        System.out.println("starting TalonDriveTrain thread...");
        leftFrontMotors = new WheelRPMController("leftFrontMotors", leftFrontIndex);
        rightFrontMotors = new WheelRPMController("rightFrontMotors", rightFrontIndex);
        leftRearMotors = new WheelRPMController("leftRearMotors", leftRearIndex);
        rightRearMotors = new WheelRPMController("rightRearMotors", rightRearIndex);

        /*BAD JAG LIST*\
        0 - drive + turn
        1 - drive + turn + strafe
        2 - strafe
        \*            */
        //BAD LEFT REAR
        TalonTeamate[0][0] = leftFrontMotors;
        TalonTeamate[0][1] = rightFrontMotors;
        TalonTeamate[0][2] = rightRearMotors;

        //BAD LEFT FRONT
        TalonTeamate[1][0] = leftRearMotors;
        TalonTeamate[1][1] = rightRearMotors;
        TalonTeamate[1][2] = rightFrontMotors;

        //BAD RIGHT REAR
        TalonTeamate[2][0] = rightFrontMotors;
        TalonTeamate[2][1] = leftFrontMotors;
        TalonTeamate[2][2] = leftRearMotors;

        //BAD RIGHT FRONT
        TalonTeamate[3][0] = rightRearMotors;
        TalonTeamate[3][1] = leftRearMotors;
        TalonTeamate[3][2] = leftFrontMotors;

        leftFrontMotors.enable();
        leftRearMotors.enable();
        rightFrontMotors.enable();
        rightRearMotors.enable();
        System.out.println("thread: WheelRPMControllers initialized");
        theDriveinator = new RobotDrive(leftFrontMotors, leftRearMotors, rightFrontMotors, rightRearMotors);
        theDriveinator.setInvertedMotor(MotorType.kFrontLeft, true);
        theDriveinator.setInvertedMotor(MotorType.kRearLeft, true);
        theDriveinator.setSafetyEnabled(false);

        System.out.println("thread: entering Talon drive loop...");
        int threadTick = 0;
        while (true) {
            if (isEnabled()) {
                lED.set(chaputpultReadySwitch.get());
                if (changeGears) {
                    System.out.println("Changing gears to " + ((inHighGear) ? "High" : "Low"));
                    changeGears = false;
                    leftFrontMotors.setGearPID(!inHighGear);
                    rightFrontMotors.setGearPID(!inHighGear);
                    leftRearMotors.setGearPID(!inHighGear);
                    rightRearMotors.setGearPID(!inHighGear);
                }
                scaleAxisInputs();
                if (++threadTick >= 50) {
                    leftFrontMotors.displayWheelRPM();
                    leftRearMotors.displayWheelRPM();
                    rightFrontMotors.displayWheelRPM();
                    rightRearMotors.displayWheelRPM();
                    System.out.println("======================================");
                    threadTick = 0;
                }
                WheelRPMController.off = !overrideSwitches.getRawButton(2);
                dashingTTS.setWarningMessage(leftFrontMotors.getError()+rightFrontMotors.getError()+leftRearMotors.getError()+rightRearMotors.getError());
                BROKEN = overrideSwitches.getRawButton(1);
                if (BROKEN) {
                    TalonTeamate[brokenTalon][0].set(yVal + zVal);
                    TalonTeamate[brokenTalon][1].set(yVal - zVal - xVal);
                    TalonTeamate[brokenTalon][2].set(xVal);
                } else {
                    theDriveinator.mecanumDrive_Cartesian(xVal, yVal, zVal, 0);
                }
            }
            try {
                Thread.sleep(20);
            } catch (Exception e) {
            }
            dashBoard();
        }
    }

    private void CANJaguarDriveTrain() {

        CANJaguarPair leftFrontMotors, rightFrontMotors, leftRearMotors,
                rightRearMotors;

        System.out.println("starting CANJaguarDriveTrain thread...");
        leftFrontMotors = new CANJaguarPair("leftFrontMotors", leftFrontIndex);
        rightFrontMotors = new CANJaguarPair("rightFrontMotors", rightFrontIndex);
        leftRearMotors = new CANJaguarPair("leftRearMotors", leftRearIndex);
        rightRearMotors = new CANJaguarPair("rightRearMotors", rightRearIndex);
        System.out.println("thread:  jags initialized");
        theDriveinator = new RobotDrive(leftFrontMotors, leftRearMotors, rightFrontMotors, rightRearMotors);
        theDriveinator.setMaxOutput(jagHighGearMaxSpeed);
        theDriveinator.setInvertedMotor(MotorType.kFrontLeft, true);
        theDriveinator.setInvertedMotor(MotorType.kRearLeft, true);
        theDriveinator.setSafetyEnabled(false);

        System.out.println("thread: entering CANJag drive loop...");
        int threadTick = 0;
        while (true) {

            if (isEnabled()) {
                try {
                    if (xboxSpeedRacer.getButtonToggle(7)) {
                        leftFrontMotors = new CANJaguarPair("leftFrontMotors", leftFrontIndex);
                        rightFrontMotors = new CANJaguarPair("rightFrontMotors", rightFrontIndex);
                        leftRearMotors = new CANJaguarPair("leftRearMotors", leftRearIndex);
                        rightRearMotors = new CANJaguarPair("rightRearMotors", rightRearIndex);
                    }
                    if (changeGears) {
                        System.out.println("Changing gears to + " + ((inHighGear) ? "High" : "Low"));
                        changeGears = false;
                        leftFrontMotors.setGearPID(inHighGear);
                        rightFrontMotors.setGearPID(inHighGear);
                        leftRearMotors.setGearPID(inHighGear);
                        rightRearMotors.setGearPID(inHighGear);
                        if (inHighGear) {
                            theDriveinator.setMaxOutput(jagHighGearMaxSpeed);
                        } else {
                            theDriveinator.setMaxOutput(jagLowGearMaxSpeed);
                        }
                    }

                    if (!isTest()) {
                        if (++threadTick >= 50) {
                            leftFrontMotors.checkForBrownOuts(inHighGear);
                            rightFrontMotors.checkForBrownOuts(inHighGear);
                            leftRearMotors.checkForBrownOuts(inHighGear);
                            rightRearMotors.checkForBrownOuts(inHighGear);
                            threadTick = 0;
                        }
                        scaleAxisInputs();
                        theDriveinator.mecanumDrive_Cartesian(xVal, yVal, zVal, 0);
                        //theDriveinator.mecanumDrive_Cartesian((Math.abs(xVal)>= .15)?xVal:0, (Math.abs(yVal)>= .15)?yVal:0, (Math.abs(zVal)>= .33)?-zVal/3.5:0, 0);                    
                    }

                    Thread.sleep(20);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            dashBoard();
        }
    }

    private boolean sign(double x1, double y1) {
        if (x1 == 0) {
            return y1 == 0;
        }
        if (y1 == 0) {
            return x1 == 0;
        }
        return ((x1 / Math.abs(x1) == y1 / Math.abs(y1)));
    }

    private void scaleAxisInputs() {
        if (Math.abs(zGol) < .1 && !isAutonomous()) {
            zGol = 0;
        }
        if (Math.abs(yGol) < .1 && !isAutonomous()) {
            yGol = 0;
        }
        if (Math.abs(xGol) < .1 && !isAutonomous()) {
            xGol = 0;
        }
        int dir;
        dir = yGol > yVal ? 1 : -1;
        yVal += dir * (!sign(yVal, yGol) ? joyAccel : joyAccel);
        yVal = ((yGol > yVal ? 1 : -1) != dir) ? yGol : yVal;
        dir = xGol > xVal ? 1 : -1;
        xVal += dir * (!sign(xVal, xGol) ? joyAccel : joyAccel);
        xVal = ((xGol > xVal ? 1 : -1) != dir) ? xGol : xVal;
        dir = zGol > zVal ? 1 : -1;
        zVal += dir * (!sign(zVal, zGol) ? joyAccel : joyAccel);
        zVal = ((zGol > zVal ? 1 : -1) != dir) ? zGol : zVal;
    }
}
