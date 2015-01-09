package edu.archwood.frc2607.utils;

import edu.wpi.first.wpilibj.AnalogChannel;

public class TempCorrectedGyro {
	
	AnalogChannel temperatureSensor, gyro;
	double result, newRate, currentTemp;
	
	public TempCorrectedGyro(int temperatureChannel, int gyroChannel) {
		temperatureSensor = new AnalogChannel(temperatureChannel);
		gyro = new AnalogChannel(gyroChannel);
		result = 0;
	}		

	public void reset() {
		result = 0;
	}

	public double getRelativeAngle() {
		currentTemp =(((temperatureSensor.getAverageVoltage()-2.5)*200)+77);
		currentTemp = ((currentTemp-32)*5)/9;

		result += (gyro.getVoltage()-2.567675991)+((currentTemp-25)*.001);
		
		return result;
    }
}