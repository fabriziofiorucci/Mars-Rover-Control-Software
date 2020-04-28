# Mars-Rover-Control-Software
Control software for [Jakkra's Mars Rover](https://github.com/jakkra/Mars-Rover)

This software has been tested and is meant to be run on a Raspberry Pi Zero W

This is currently a work in progress


### u-blox NEO-6M GPS 

/dev/serial0

https://www.electroschematics.com/neo-6m-gps-module/
https://gpsd.gitlab.io/gpsd/


### I2C bus usage

```
root@rpi-rover:~# i2cdetect -y 1
     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
00:          -- -- -- -- -- -- -- -- -- -- -- -- -- 
10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
20: 20 -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
40: 40 41 -- -- -- -- -- -- -- -- -- 4b -- -- -- -- 
50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
60: -- -- -- -- -- -- -- -- 68 -- -- -- -- -- -- -- 
70: 70 -- -- -- -- -- -- --    

0x20 -> MCP23017 - wheels fwd/rev
0x40 -> PCA9685 - wheels PWM / steering PWM
0x41 -> PCA9685 - arm PWM / camera PWM
0x4b -> ADS1115 - distance sensors (ADDR=SCL addr 1001011)
0x68 -> MPU6050 - gyro/accelerometer
0x70 -> PCA9685 - all call
```

### MPU6050 0x68 -> 6DOF IMU
https://www.teachmemicro.com/beaglebone-black-mpu6050-i2c-tutorial-part-2/


### MCP23017 0x20 -> wheels direction fwd/rev/stop

```
                  GPA0-7    GPB0-7
reverse         : 01010101  00000101
forward         : 10101010  00001010
clockwise       : 01101010  00000101
counterclockwise: 10010101  00001010
stop            : 00000000  00000000

A0-A1 -> Center Left
A2-A3 -> Center Right
A4-A5 -> Rear Left
A6-A7 -> Rear Right
B0-B1 -> Front Left
B2-B3 -> Front Right
```

### ADS1115 0x4b -> Channels - distance sensors

```
0 - rear left
1 - rear right
2 - front left
3 - front right
```

### PCA9685 0x40 -> wheels PWM / steering PWM (only PWM used, VCC+GND straight from DC/DC stepdown converter)

```
0 - Speed front left (PWM) -> to L298N
1 - Speed front right (PWM) -> to L298N
2 - Speed center left (PWM) -> to L298N
3 - Speed center right (PWM) -> to L298N
4 - Speed rear left (PWM) -> to L298N
5 - Speed rear right (PWM) -> to L298N
6 - Steering rear left -> to MG996R
7 - Steering front left -> to MG996R
8 - Steering front right -> to MG996R
9 - Steering rear right -> to MG996R
10 -
11 -
12 -
13 -
14 -
15 -
```

### PCA9685 0x41 -> channels - arm PWM / camera PWM  (only PWM used, VCC+GND straight from DC/DC stepdown converter)

```
0 - Camera pan
1 - Camera tilt
2 - arm base pan
3 - arm section 1
4 - arm section 2
5 - arm section 3
6 - arm wrist
7 - arm claw
8 -
9 -
10 -
11 -
12 -
13 -
14 -
15 -
```

### Boards and testing

- Raspberry and sensors board

<img src="./Rover/images/sensorsboard.jpg"/>

- Rocker bogie control board

<img src="./Rover/images/rockerbogieboard.jpg"/>

- Sensors and rocker bogie test

<img src="./Rover/images/sensors_and_rockerbogie_test.jpg"/>

- Arm and head control board

<img src="./Rover/images/arm_and_head_board.jpg"/>

- Full test

<img src="./Rover/images/fulltest.jpg"/>
