EESchema Schematic File Version 4
LIBS:jointController-cache
EELAYER 26 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 3 6
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
L jointController:DRV8323RS U?
U 1 1 5E609BA8
P 5700 1500
F 0 "U?" H 5700 1575 50  0000 C CNN
F 1 "DRV8323RS" H 5700 1484 50  0000 C CNN
F 2 "" H 5700 1500 50  0001 C CNN
F 3 "" H 5700 1500 50  0001 C CNN
	1    5700 1500
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C_VCP?
U 1 1 5E609BAF
P 4900 2150
F 0 "C_VCP?" H 4992 2196 50  0000 L CNN
F 1 "47nF" H 4992 2105 50  0000 L CNN
F 2 "" H 4900 2150 50  0001 C CNN
F 3 "~" H 4900 2150 50  0001 C CNN
	1    4900 2150
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C_SW?
U 1 1 5E609BB6
P 4900 2550
F 0 "C_SW?" H 4992 2596 50  0000 L CNN
F 1 "1uF" H 4992 2505 50  0000 L CNN
F 2 "" H 4900 2550 50  0001 C CNN
F 3 "~" H 4900 2550 50  0001 C CNN
	1    4900 2550
	1    0    0    -1  
$EndComp
Wire Wire Line
	4150 1600 4150 1650
$Comp
L power:GND #PWR?
U 1 1 5E609BBE
P 4500 1950
F 0 "#PWR?" H 4500 1700 50  0001 C CNN
F 1 "GND" H 4505 1777 50  0000 C CNN
F 2 "" H 4500 1950 50  0001 C CNN
F 3 "" H 4500 1950 50  0001 C CNN
	1    4500 1950
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR?
U 1 1 5E609BC4
P 4150 1950
F 0 "#PWR?" H 4150 1700 50  0001 C CNN
F 1 "GND" H 4155 1777 50  0000 C CNN
F 2 "" H 4150 1950 50  0001 C CNN
F 3 "" H 4150 1950 50  0001 C CNN
	1    4150 1950
	1    0    0    -1  
$EndComp
$Comp
L power:+3.3V #PWR?
U 1 1 5E609BCA
P 4150 1350
F 0 "#PWR?" H 4150 1200 50  0001 C CNN
F 1 "+3.3V" H 4167 1523 50  0000 C CNN
F 2 "" H 4150 1350 50  0001 C CNN
F 3 "" H 4150 1350 50  0001 C CNN
	1    4150 1350
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R_FB?
U 1 1 5E609BD0
P 4150 1800
F 0 "R_FB?" H 4209 1846 50  0000 L CNN
F 1 "8.45k" H 4209 1755 50  0000 L CNN
F 2 "" H 4150 1800 50  0001 C CNN
F 3 "~" H 4150 1800 50  0001 C CNN
	1    4150 1800
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R_FB?
U 1 1 5E609BD7
P 4150 1500
F 0 "R_FB?" H 4209 1546 50  0000 L CNN
F 1 "28k" H 4209 1455 50  0000 L CNN
F 2 "" H 4150 1500 50  0001 C CNN
F 3 "~" H 4150 1500 50  0001 C CNN
	1    4150 1500
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C_VM?
U 1 1 5E609BDE
P 4500 2800
F 0 "C_VM?" H 4592 2846 50  0000 L CNN
F 1 "0.1uF" H 4592 2755 50  0000 L CNN
F 2 "" H 4500 2800 50  0001 C CNN
F 3 "~" H 4500 2800 50  0001 C CNN
	1    4500 2800
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C_VDD?
U 1 1 5E609BE5
P 6300 3750
F 0 "C_VDD?" H 6392 3796 50  0000 L CNN
F 1 "1uF" H 6392 3705 50  0000 L CNN
F 2 "" H 6300 3750 50  0001 C CNN
F 3 "~" H 6300 3750 50  0001 C CNN
	1    6300 3750
	1    0    0    -1  
$EndComp
$Comp
L power:VCC #PWR?
U 1 1 5E609BEC
P 4500 2600
F 0 "#PWR?" H 4500 2450 50  0001 C CNN
F 1 "VCC" H 4517 2773 50  0000 C CNN
F 2 "" H 4500 2600 50  0001 C CNN
F 3 "" H 4500 2600 50  0001 C CNN
	1    4500 2600
	1    0    0    -1  
$EndComp
Wire Wire Line
	4500 1850 4500 1950
Wire Wire Line
	4150 1900 4150 1950
Wire Wire Line
	4150 1400 4150 1350
Wire Wire Line
	4500 2600 4500 2650
$Comp
L power:GND #PWR?
U 1 1 5E609BF6
P 4500 2950
F 0 "#PWR?" H 4500 2700 50  0001 C CNN
F 1 "GND" H 4505 2777 50  0000 C CNN
F 2 "" H 4500 2950 50  0001 C CNN
F 3 "" H 4500 2950 50  0001 C CNN
	1    4500 2950
	1    0    0    -1  
$EndComp
Wire Wire Line
	4500 2650 4900 2650
Connection ~ 4500 2650
Wire Wire Line
	4500 2650 4500 2700
Connection ~ 4900 2650
Wire Wire Line
	4900 2650 5300 2650
Wire Wire Line
	4900 2450 5300 2450
Wire Wire Line
	4900 2250 5300 2250
Wire Wire Line
	4900 2050 5300 2050
Wire Wire Line
	4500 1850 5300 1850
Wire Wire Line
	5300 1650 4150 1650
Connection ~ 4150 1650
Wire Wire Line
	4150 1650 4150 1700
Wire Wire Line
	4500 2900 4500 2950
Text GLabel 5250 2850 0    50   Input ~ 0
VDRAIN
Text GLabel 5250 3050 0    50   Input ~ 0
GHA
Text GLabel 5250 3250 0    50   Input ~ 0
SHA
Text GLabel 5250 3450 0    50   Input ~ 0
GLA
Text GLabel 5250 3650 0    50   Input ~ 0
SPA
Text GLabel 5250 3850 0    50   Input ~ 0
SNA
Text GLabel 5250 4050 0    50   Input ~ 0
SNB
Text GLabel 5250 4250 0    50   Input ~ 0
SPB
Text GLabel 5250 4450 0    50   Input ~ 0
GLB
Text GLabel 5250 4650 0    50   Input ~ 0
SHB
Text GLabel 5250 4850 0    50   Input ~ 0
GHB
Text GLabel 5250 5050 0    50   Input ~ 0
GHC
Text GLabel 5250 5250 0    50   Input ~ 0
SHC
Text GLabel 5250 5450 0    50   Input ~ 0
GLC
Text GLabel 5250 5650 0    50   Input ~ 0
SPC
Text GLabel 5250 5850 0    50   Input ~ 0
SNC
Text GLabel 5250 6050 0    50   Input ~ 0
SOC
Text GLabel 5250 6250 0    50   Input ~ 0
SOB
Text GLabel 6150 1650 2    50   Input ~ 0
SOA
$Comp
L Device:C_Small C_VREF?
U 1 1 5E609C1C
P 6500 1950
F 0 "C_VREF?" H 6592 1996 50  0000 L CNN
F 1 "0.1uF" H 6592 1905 50  0000 L CNN
F 2 "" H 6500 1950 50  0001 C CNN
F 3 "~" H 6500 1950 50  0001 C CNN
	1    6500 1950
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R_PU?
U 1 1 5E609C23
P 7050 2150
F 0 "R_PU?" H 7109 2196 50  0000 L CNN
F 1 "10k" H 7109 2105 50  0000 L CNN
F 2 "" H 7050 2150 50  0001 C CNN
F 3 "~" H 7050 2150 50  0001 C CNN
	1    7050 2150
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R_PU?
U 1 1 5E609C2A
P 7500 2350
F 0 "R_PU?" H 7559 2396 50  0000 L CNN
F 1 "R_Small" H 7559 2305 50  0000 L CNN
F 2 "" H 7500 2350 50  0001 C CNN
F 3 "~" H 7500 2350 50  0001 C CNN
	1    7500 2350
	1    0    0    -1  
$EndComp
$Comp
L power:+3.3V #PWR?
U 1 1 5E609C31
P 7050 2050
F 0 "#PWR?" H 7050 1900 50  0001 C CNN
F 1 "+3.3V" H 7067 2223 50  0000 C CNN
F 2 "" H 7050 2050 50  0001 C CNN
F 3 "" H 7050 2050 50  0001 C CNN
	1    7050 2050
	1    0    0    -1  
$EndComp
$Comp
L power:+3.3V #PWR?
U 1 1 5E609C37
P 7500 2250
F 0 "#PWR?" H 7500 2100 50  0001 C CNN
F 1 "+3.3V" H 7517 2423 50  0000 C CNN
F 2 "" H 7500 2250 50  0001 C CNN
F 3 "" H 7500 2250 50  0001 C CNN
	1    7500 2250
	1    0    0    -1  
$EndComp
Wire Wire Line
	6100 1850 6500 1850
$Comp
L power:+3.3V #PWR?
U 1 1 5E609C3E
P 6500 1850
F 0 "#PWR?" H 6500 1700 50  0001 C CNN
F 1 "+3.3V" H 6517 2023 50  0000 C CNN
F 2 "" H 6500 1850 50  0001 C CNN
F 3 "" H 6500 1850 50  0001 C CNN
	1    6500 1850
	1    0    0    -1  
$EndComp
Connection ~ 6500 1850
$Comp
L power:GND #PWR?
U 1 1 5E609C45
P 6500 2050
F 0 "#PWR?" H 6500 1800 50  0001 C CNN
F 1 "GND" H 6500 1900 50  0000 C CNN
F 2 "" H 6500 2050 50  0001 C CNN
F 3 "" H 6500 2050 50  0001 C CNN
	1    6500 2050
	1    0    0    -1  
$EndComp
Wire Wire Line
	6100 2050 6500 2050
Connection ~ 6500 2050
Wire Wire Line
	6100 2250 7050 2250
Text GLabel 7100 2300 2    50   Input ~ 0
nFAULT
Text GLabel 7550 2500 2    50   Input ~ 0
DRV_SDO
Wire Wire Line
	7050 2250 7050 2300
Wire Wire Line
	7050 2300 7100 2300
Connection ~ 7050 2250
Wire Wire Line
	7500 2450 7500 2500
Wire Wire Line
	7500 2500 7550 2500
Connection ~ 7500 2450
Wire Wire Line
	6100 2450 7500 2450
Text GLabel 6150 2650 2    50   Input ~ 0
DRV_SDI
Text GLabel 6150 2850 2    50   Input ~ 0
DRV_SCLK
Text GLabel 6150 3050 2    50   Input ~ 0
DRV_nSCS
Text GLabel 6150 3250 2    50   Input ~ 0
DRV_ENABLE
Text GLabel 6150 3450 2    50   Input ~ 0
CAL
$Comp
L power:GND #PWR?
U 1 1 5E609C5C
P 6800 3650
F 0 "#PWR?" H 6800 3400 50  0001 C CNN
F 1 "GND" H 6800 3500 50  0000 C CNN
F 2 "" H 6800 3650 50  0001 C CNN
F 3 "" H 6800 3650 50  0001 C CNN
	1    6800 3650
	1    0    0    -1  
$EndComp
Text GLabel 6150 4050 2    50   Input ~ 0
PWM1H
Text GLabel 6150 4250 2    50   Input ~ 0
INLA
Text GLabel 6150 4450 2    50   Input ~ 0
PWM2H
Text GLabel 6150 4650 2    50   Input ~ 0
INLB
Text GLabel 6150 4850 2    50   Input ~ 0
PWM3H
Text GLabel 6150 5050 2    50   Input ~ 0
INLC
Text GLabel 6350 3900 2    50   Input ~ 0
DVDD
Wire Wire Line
	6100 2650 6150 2650
Wire Wire Line
	6100 2850 6150 2850
Wire Wire Line
	6100 3050 6150 3050
Wire Wire Line
	6100 3250 6150 3250
Wire Wire Line
	6100 3450 6150 3450
Wire Wire Line
	6100 3650 6300 3650
Connection ~ 6300 3650
Wire Wire Line
	6300 3650 6800 3650
Wire Wire Line
	6100 3850 6300 3850
Wire Wire Line
	6300 3850 6300 3900
Wire Wire Line
	6300 3900 6350 3900
Connection ~ 6300 3850
Wire Wire Line
	6100 4050 6150 4050
Wire Wire Line
	6100 4250 6150 4250
Wire Wire Line
	6100 4450 6150 4450
Wire Wire Line
	6100 4650 6150 4650
Wire Wire Line
	6100 4850 6150 4850
Wire Wire Line
	6100 5050 6150 5050
Wire Wire Line
	5300 6250 5250 6250
Wire Wire Line
	5300 6050 5250 6050
Wire Wire Line
	5250 5850 5300 5850
Wire Wire Line
	5250 5650 5300 5650
Wire Wire Line
	5250 5450 5300 5450
Wire Wire Line
	5250 5250 5300 5250
Wire Wire Line
	5250 5050 5300 5050
Wire Wire Line
	5250 4850 5300 4850
Wire Wire Line
	5250 4650 5300 4650
Wire Wire Line
	5250 4450 5300 4450
Wire Wire Line
	5250 4250 5300 4250
Wire Wire Line
	5250 4050 5300 4050
Wire Wire Line
	5250 3850 5300 3850
Wire Wire Line
	5250 3650 5300 3650
Wire Wire Line
	5250 3450 5300 3450
Wire Wire Line
	5250 3250 5300 3250
Wire Wire Line
	5250 3050 5300 3050
Wire Wire Line
	5250 2850 5300 2850
Wire Wire Line
	6100 1650 6150 1650
$Comp
L Device:C_Small C?
U 1 1 5E609C8E
P 6300 5550
F 0 "C?" H 6392 5596 50  0000 L CNN
F 1 "0.1uF" H 6392 5505 50  0000 L CNN
F 2 "" H 6300 5550 50  0001 C CNN
F 3 "~" H 6300 5550 50  0001 C CNN
	1    6300 5550
	1    0    0    -1  
$EndComp
$Comp
L Device:D_Schottky D?
U 1 1 5E609C95
P 6750 5450
F 0 "D?" V 6796 5371 50  0000 R CNN
F 1 "D_Schottky" V 6705 5371 50  0000 R CNN
F 2 "" H 6750 5450 50  0001 C CNN
F 3 "~" H 6750 5450 50  0001 C CNN
	1    6750 5450
	0    -1   -1   0   
$EndComp
$Comp
L Device:L_Small L?
U 1 1 5E609C9C
P 7100 5650
F 0 "L?" V 7150 5650 50  0000 C CNN
F 1 "27uH" V 7050 5650 50  0000 C CNN
F 2 "" H 7100 5650 50  0001 C CNN
F 3 "~" H 7100 5650 50  0001 C CNN
	1    7100 5650
	0    -1   -1   0   
$EndComp
Wire Wire Line
	6100 5650 6300 5650
Connection ~ 6300 5650
Wire Wire Line
	6300 5650 6750 5650
Wire Wire Line
	6750 5600 6750 5650
Connection ~ 6750 5650
Wire Wire Line
	6750 5650 7000 5650
Wire Wire Line
	6300 5450 6100 5450
Wire Wire Line
	6100 5250 6750 5250
Wire Wire Line
	6750 5250 6750 5300
$Comp
L power:GND #PWR?
U 1 1 5E609CAC
P 7100 5250
F 0 "#PWR?" H 7100 5000 50  0001 C CNN
F 1 "GND" H 7100 5100 50  0000 C CNN
F 2 "" H 7100 5250 50  0001 C CNN
F 3 "" H 7100 5250 50  0001 C CNN
	1    7100 5250
	1    0    0    -1  
$EndComp
Wire Wire Line
	6750 5250 7100 5250
Connection ~ 6750 5250
$Comp
L Device:C_Small C?
U 1 1 5E609CB4
P 7450 5750
F 0 "C?" H 7542 5796 50  0000 L CNN
F 1 "10uF" H 7542 5705 50  0000 L CNN
F 2 "" H 7450 5750 50  0001 C CNN
F 3 "~" H 7450 5750 50  0001 C CNN
	1    7450 5750
	1    0    0    -1  
$EndComp
$Comp
L power:+3.3V #PWR?
U 1 1 5E609CBB
P 7450 5650
F 0 "#PWR?" H 7450 5500 50  0001 C CNN
F 1 "+3.3V" H 7467 5823 50  0000 C CNN
F 2 "" H 7450 5650 50  0001 C CNN
F 3 "" H 7450 5650 50  0001 C CNN
	1    7450 5650
	1    0    0    -1  
$EndComp
Wire Wire Line
	7200 5650 7450 5650
Connection ~ 7450 5650
NoConn ~ 6100 5850
$Comp
L power:VCC #PWR?
U 1 1 5E609CC4
P 6500 6050
F 0 "#PWR?" H 6500 5900 50  0001 C CNN
F 1 "VCC" H 6517 6223 50  0000 C CNN
F 2 "" H 6500 6050 50  0001 C CNN
F 3 "" H 6500 6050 50  0001 C CNN
	1    6500 6050
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R?
U 1 1 5E609CCA
P 6300 6250
F 0 "R?" V 6200 6250 50  0000 C CNN
F 1 "100k" V 6400 6250 50  0000 C CNN
F 2 "" H 6300 6250 50  0001 C CNN
F 3 "~" H 6300 6250 50  0001 C CNN
	1    6300 6250
	0    1    1    0   
$EndComp
$Comp
L Device:C_Small C?
U 1 1 5E609CD1
P 6500 6350
F 0 "C?" H 6592 6396 50  0000 L CNN
F 1 "10uF" H 6592 6305 50  0000 L CNN
F 2 "" H 6500 6350 50  0001 C CNN
F 3 "~" H 6500 6350 50  0001 C CNN
	1    6500 6350
	1    0    0    -1  
$EndComp
Wire Wire Line
	6100 6250 6200 6250
Wire Wire Line
	6400 6250 6500 6250
Wire Wire Line
	6100 6050 6500 6050
Wire Wire Line
	6500 6050 6500 6250
Connection ~ 6500 6050
Connection ~ 6500 6250
$Comp
L power:GND #PWR?
U 1 1 5E609CDE
P 6500 6450
F 0 "#PWR?" H 6500 6200 50  0001 C CNN
F 1 "GND" H 6505 6277 50  0000 C CNN
F 2 "" H 6500 6450 50  0001 C CNN
F 3 "" H 6500 6450 50  0001 C CNN
	1    6500 6450
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR?
U 1 1 5E609CE4
P 7450 5850
F 0 "#PWR?" H 7450 5600 50  0001 C CNN
F 1 "GND" H 7455 5677 50  0000 C CNN
F 2 "" H 7450 5850 50  0001 C CNN
F 3 "" H 7450 5850 50  0001 C CNN
	1    7450 5850
	1    0    0    -1  
$EndComp
$EndSCHEMATC
