EESchema Schematic File Version 4
LIBS:jointController-cache
EELAYER 26 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 6 6
Title ""
Date ""
Rev ""
Comp ""
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
$Comp
L jointController:MA703 U?
U 1 1 5E579108
P 5850 4000
F 0 "U?" H 5450 4550 50  0000 R CNN
F 1 "MA703" H 6350 4550 50  0000 R CNN
F 2 "" H 6650 4450 50  0001 C CNN
F 3 "" H 6650 4450 50  0001 C CNN
	1    5850 4000
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C?
U 1 1 5E5792AC
P 5000 4000
F 0 "C?" H 5092 4046 50  0000 L CNN
F 1 "1uF" H 5092 3955 50  0000 L CNN
F 2 "" H 5000 4000 50  0001 C CNN
F 3 "~" H 5000 4000 50  0001 C CNN
	1    5000 4000
	1    0    0    -1  
$EndComp
Wire Wire Line
	5000 3900 5250 3900
Wire Wire Line
	5000 4100 5250 4100
$Comp
L power:+3.3V #PWR?
U 1 1 5E579308
P 5000 3450
F 0 "#PWR?" H 5000 3300 50  0001 C CNN
F 1 "+3.3V" H 5015 3623 50  0000 C CNN
F 2 "" H 5000 3450 50  0001 C CNN
F 3 "" H 5000 3450 50  0001 C CNN
	1    5000 3450
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR?
U 1 1 5E579343
P 5000 4650
F 0 "#PWR?" H 5000 4400 50  0001 C CNN
F 1 "GND" H 5005 4477 50  0000 C CNN
F 2 "" H 5000 4650 50  0001 C CNN
F 3 "" H 5000 4650 50  0001 C CNN
	1    5000 4650
	1    0    0    -1  
$EndComp
Connection ~ 5000 4100
Connection ~ 5000 3900
Text GLabel 6550 3700 2    50   Input ~ 0
ENC_SDO
Text GLabel 6550 3800 2    50   Input ~ 0
ENC_SDI
Text GLabel 6550 3900 2    50   Input ~ 0
ENC_SCLK
Text GLabel 6550 4000 2    50   Input ~ 0
ENC_nSCS
Wire Wire Line
	6450 3700 6550 3700
Wire Wire Line
	6450 3800 6550 3800
Wire Wire Line
	6450 3900 6550 3900
Wire Wire Line
	6450 4000 6550 4000
NoConn ~ 5650 3400
NoConn ~ 5750 3400
NoConn ~ 5850 3400
NoConn ~ 5950 3400
NoConn ~ 6050 3400
NoConn ~ 6450 4100
NoConn ~ 6450 4200
NoConn ~ 6450 4300
NoConn ~ 6050 4600
$Comp
L power:GND #PWR?
U 1 1 5E579FD7
P 5650 4650
F 0 "#PWR?" H 5650 4400 50  0001 C CNN
F 1 "GND" H 5655 4477 50  0000 C CNN
F 2 "" H 5650 4650 50  0001 C CNN
F 3 "" H 5650 4650 50  0001 C CNN
	1    5650 4650
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR?
U 1 1 5E579FFD
P 5850 4650
F 0 "#PWR?" H 5850 4400 50  0001 C CNN
F 1 "GND" H 5855 4477 50  0000 C CNN
F 2 "" H 5850 4650 50  0001 C CNN
F 3 "" H 5850 4650 50  0001 C CNN
	1    5850 4650
	1    0    0    -1  
$EndComp
Wire Wire Line
	5650 4600 5650 4650
Wire Wire Line
	5850 4600 5850 4650
Wire Wire Line
	5000 4100 5000 4650
Wire Wire Line
	5000 3450 5000 3900
$EndSCHEMATC