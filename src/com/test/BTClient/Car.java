package com.test.BTClient;

public class Car {
	public static int speed = 50;
	public static int control = 0;
	public static String handleCmd(){
		return String.valueOf((char)((speed >> 3 << 3) + control));
	}
	

}
