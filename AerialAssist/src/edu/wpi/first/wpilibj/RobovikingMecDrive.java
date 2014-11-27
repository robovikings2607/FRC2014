/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.first.wpilibj;

import edu.wpi.first.wpilibj.can.CANNotInitializedException;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.communication.UsageReporting;

/**
 *
 * @author frcdev
 */
public class RobovikingMecDrive extends RobotDrive{

    public RobovikingMecDrive(SpeedController frontLeftMotor, SpeedController rearLeftMotor, SpeedController frontRightMotor, SpeedController rearRightMotor) {
        super(frontLeftMotor, rearLeftMotor, frontRightMotor, rearRightMotor);
    }
    
    double FLSpeed, FRSpeed, BRSpeed, BLSpeed;
    short tick;

    public void mecanumDrive_Cartesian(double x, double y, double rotation, double gyroAngle)
    {
        if(!kMecanumCartesian_Reported){
            UsageReporting.report(UsageReporting.kResourceType_RobotDrive, getNumMotors(), UsageReporting.kRobotDrive_MecanumCartesian);
            kMecanumCartesian_Reported = true;
        }
        double xIn = x;
        double yIn = y;
        // Negate y for the joystick.
        yIn = -yIn;
        // Compenstate for gyro angle.
        double rotated[] = rotateVector(xIn, yIn, gyroAngle);
        xIn = rotated[0];
        yIn = rotated[1];

        double wheelSpeeds[] = new double[kMaxNumberOfMotors];
        wheelSpeeds[MotorType.kFrontLeft_val] = xIn + yIn + rotation;
        wheelSpeeds[MotorType.kFrontRight_val] = -xIn + yIn - rotation;
        wheelSpeeds[MotorType.kRearLeft_val] = -xIn + yIn + rotation;
        wheelSpeeds[MotorType.kRearRight_val] = xIn + yIn - rotation;

        normalize(wheelSpeeds);

        byte syncGroup = (byte)0x80;

        FLSpeed = wheelSpeeds[MotorType.kFrontLeft_val] * m_invertedMotors[MotorType.kFrontLeft_val] * m_maxOutput;
        FRSpeed = wheelSpeeds[MotorType.kFrontRight_val] * m_invertedMotors[MotorType.kFrontRight_val] * m_maxOutput;
        BLSpeed = wheelSpeeds[MotorType.kRearLeft_val] * m_invertedMotors[MotorType.kRearLeft_val] * m_maxOutput;
        BRSpeed = wheelSpeeds[MotorType.kRearRight_val] * m_invertedMotors[MotorType.kRearRight_val] * m_maxOutput;
        
        if ( FLSpeed != 0 || FRSpeed != 0 || BLSpeed != 0 || BRSpeed != 0){
            System.out.println("FLSpeed: " + FLSpeed + " FRSpeed: " + FRSpeed + " BLSpeed: " + BLSpeed + " BRSpeed: " + BRSpeed);
        }
        try
        {
            m_frontLeftMotor.set(FLSpeed, syncGroup);
        }
        catch (Exception e)
        {

        }
        try
        {
            m_frontRightMotor.set(FRSpeed, syncGroup);
       
        }
        catch (Exception e)
        {

        }
        try
        {
            m_rearLeftMotor.set(BLSpeed, syncGroup);
    
        }
        catch (Exception e)
        {

        }
        try
        {
                m_rearRightMotor.set(BRSpeed, syncGroup);
        }
        catch (Exception e)
        {

        }
        
        if (m_isCANInitialized) {
            try {
                CANJaguar.updateSyncGroup(syncGroup);
            } catch (CANNotInitializedException e) {
                m_isCANInitialized = false;
            } catch (CANTimeoutException e) {}
        }

        if (m_safetyHelper != null) m_safetyHelper.feed();
    }
    
}
