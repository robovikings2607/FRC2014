/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.archwood.frc2607.AerialAssist;

/**
 *
 * @author frcdev
 */
public interface CompBotConstants {
    /*
    MAJOR DIFFERENCES BETWEEN COMPETITION BOT AND PRACTICE BOT
      - SHIFTERS ARE INVERTED
      - RELAY (illegal) IS USED INSTEAD OF VICTOR
      - THE DIFFERENT ENCODER WAS ON BACK LEFT INSTEAD OF FRONT RIGHT
    */
    
    // ==================================================================
    // Start of I/O Assignments
    //LEDs
    final int digitalOutputLED = 14;
    // Solenoid Module
    final int solenoidShifter = 2;
    final int solenoidLauncher = 3;
    final int solenoidPickup = 4;

    final boolean COMP = true;
    
    // Analog Module 
    final int analogLeftSonar = 6;
    final int analogRightSonar = 7;

    // Sidecar DIO
    final int diPressureSwitch = 1;
    final int diLauncherSwitch = 2;
    final int[][] diEncoders = new int[][] {
                                {3,4},              // rightFront Encoder (ChA, ChB)
                                {5,6},              // rightRear  Encoder (ChA, ChB)
                                {7,8},              // leftFront  Encoder (ChA, ChB)
                                {9,10}              // leftRear   Encoder (ChA, ChB)
    };
    
    final int diLeftRX = 14;
    final int diRightRX = 13;
    final int doLeftTX = 12;
    final int doRightTX = 11;    

    // Sidecar Relays
    final int relayCompressor = 1;
    
    // Sidecar PWM
    final int pwmHolyRollers = 10;
    final int pwmChaputpultWinder = 9;
    
    // if using y-splitters, just wire to the 1st PWM in each of the pairs below, i.e.
    //  1, 3, 5, and 7
    // if not using y-splitters, wire both PWMs in each pair
    final int[][] pwmMecanumAddresses = new int [][] {
                                        {1,2},          // rightFront Talons
                                        {3,4},          // rightRear  Talons
                                        {5,6},          // leftFront  Talons
                                        {7,8}           // leftRear   Talons
    };
    
    // End of I/O assignments
    // =================================================================
    
    final int speedJagIndex = 0;
    final int voltJagIndex = 1;

    final int rightFrontIndex = 0;
    final int rightRearIndex = 1;
    final int leftFrontIndex = 2;
    final int leftRearIndex = 3;
    
    final int jagHighGearMaxSpeed = 4400; //2800?
    final int jagLowGearMaxSpeed = 2200;  //1400?

    //Accel values
    final float joyAccel = 0.1f;
    
    final int[][] canMecanumAddresses = new int[][] { 
                                        {5,9},          // rightFront Jags (speed, volt)
                                        {7,6},          // rightRear  Jags (speed, volt)
                                        {8,2},          // leftFront  Jags (speed, volt)
                                        {4,3}           // leftRear   Jags (speed, volt)
    };
    
    final int[] jagEncoderLines = new int[] { 250, 360, 360, 360 };    // 747?  rightRear?
                                                                    // 1871? rightFront?
                                                                    // 2684? leftRear?
                                                                    // 54?   leftFront?
    
    //TODO:  need to confirm/adjust the following
    final double[] talonHighGearMaxSpeeds = new double[] {19230,  //rightFront //19230 on comp bot
                                                          26315,  //rightRear 
                                                          26315,  //leftFront 
                                                          26315}; //leftRear   //19230 on practice bot
        
    final double[] talonLowGearMaxSpeeds = new double[] {8620,   //rightFront  //8620 on comp bot
                                                         11904,   //rightRear 
                                                         11904,   //leftFront
                                                         11904};  //leftRear     //8620 on comp bot
    
    final double[][] talonHighGearPIDGains = new double[][] {
                                        {.00002, .00001, 0.0},  // rightFrontPID Gains
                                        {.00002, .00001, 0.0},  // rightRearPID Gains
                                        {.00002, .00001, 0.0},  // leftFrontPID Gains
                                        {.00002, .00001, 0.0}   // leftRearPID Gains
    };
    
    final double[][] talonLowGearPIDGains = new double[][] {
                                        {.00006, .00003, 0.0},  // rightFrontPID Gains
                                        {.00006, .00003, 0.0},  // rightRearPID Gains
                                        {.00006, .00003, 0.0},  // leftFrontPID Gains
                                        {.00006, .00003, 0.0},  // leftRearPID Gains                                        
    };
    
    final double[][] jagHighGearPIDGains = new double[][] {
                                        {.02,.005,.001},   // rightFront PID Gains
                                        {.02,.005,.001},   // rightRear PID Gains
                                        {.02,.005,.001},   // leftFront PID Gains
                                        {.02,.005,.001}    // leftRear PID Gains
    };
    
    final double[][] jagLowGearPIDGains = new double[][] {
                                        {.06,.005,.001},   // rightFront PID Gains
                                        {.06,.005,.001},   // rightRear PID Gains
                                        {.06,.005,.001},   // leftFront PID Gains
                                        {.06,.005,.001}    // leftRear PID Gains
        
    };

}
    

