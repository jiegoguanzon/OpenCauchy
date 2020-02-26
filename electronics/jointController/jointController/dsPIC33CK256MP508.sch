EESchema Schematic File Version 4
LIBS:jointController-cache
EELAYER 26 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 4 6
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
L Device:C_Small C15
U 1 1 5E522504
P 3300 3800
F 0 "C15" H 3392 3846 50  0000 L CNN
F 1 "0.1uF" H 3392 3755 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 3300 3800 50  0001 C CNN
F 3 "~" H 3300 3800 50  0001 C CNN
	1    3300 3800
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C13
U 1 1 5E522564
P 2850 3800
F 0 "C13" H 2942 3846 50  0000 L CNN
F 1 "0.01uF" H 2942 3755 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 2850 3800 50  0001 C CNN
F 3 "~" H 2850 3800 50  0001 C CNN
	1    2850 3800
	1    0    0    -1  
$EndComp
Wire Wire Line
	2850 3700 3300 3700
Wire Wire Line
	3300 3700 3650 3700
Connection ~ 3300 3700
Wire Wire Line
	2850 3900 3300 3900
Wire Wire Line
	3300 3900 3650 3900
Connection ~ 3300 3900
$Comp
L power:GND #PWR039
U 1 1 5E52264B
P 2650 3700
F 0 "#PWR039" H 2650 3450 50  0001 C CNN
F 1 "GND" H 2650 3550 50  0000 C CNN
F 2 "" H 2650 3700 50  0001 C CNN
F 3 "" H 2650 3700 50  0001 C CNN
	1    2650 3700
	1    0    0    -1  
$EndComp
Wire Wire Line
	2650 3700 2850 3700
Connection ~ 2850 3700
$Comp
L power:+3.3V #PWR038
U 1 1 5E5226C1
P 2500 3800
F 0 "#PWR038" H 2500 3650 50  0001 C CNN
F 1 "+3.3V" H 2515 3973 50  0000 C CNN
F 2 "" H 2500 3800 50  0001 C CNN
F 3 "" H 2500 3800 50  0001 C CNN
	1    2500 3800
	1    0    0    -1  
$EndComp
Wire Wire Line
	2500 3800 2500 3900
Wire Wire Line
	2500 3900 2850 3900
Connection ~ 2850 3900
$Comp
L Device:C_Small C21
U 1 1 5E522A7F
P 6100 6000
F 0 "C21" V 6000 6000 50  0000 C CNN
F 1 "0.1uF" V 6200 6000 50  0000 C CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 6100 6000 50  0001 C CNN
F 3 "~" H 6100 6000 50  0001 C CNN
	1    6100 6000
	0    1    1    0   
$EndComp
$Comp
L Device:C_Small C22
U 1 1 5E522B5B
P 6100 6500
F 0 "C22" V 6000 6500 50  0000 C CNN
F 1 "0.01uF" V 6200 6500 50  0000 C CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 6100 6500 50  0001 C CNN
F 3 "~" H 6100 6500 50  0001 C CNN
	1    6100 6500
	0    1    1    0   
$EndComp
Wire Wire Line
	6000 5850 6000 6000
Wire Wire Line
	6200 5850 6200 6000
$Comp
L power:+3.3V #PWR048
U 1 1 5E522C98
P 5850 6500
F 0 "#PWR048" H 5850 6350 50  0001 C CNN
F 1 "+3.3V" H 5850 6650 50  0000 C CNN
F 2 "" H 5850 6500 50  0001 C CNN
F 3 "" H 5850 6500 50  0001 C CNN
	1    5850 6500
	1    0    0    -1  
$EndComp
Connection ~ 6000 6500
$Comp
L power:GND #PWR050
U 1 1 5E522D44
P 6350 6500
F 0 "#PWR050" H 6350 6250 50  0001 C CNN
F 1 "GND" H 6350 6350 50  0000 C CNN
F 2 "" H 6350 6500 50  0001 C CNN
F 3 "" H 6350 6500 50  0001 C CNN
	1    6350 6500
	1    0    0    -1  
$EndComp
Connection ~ 6200 6500
Wire Wire Line
	6200 6500 6350 6500
Wire Wire Line
	5850 6500 6000 6500
$Comp
L Device:C_Small C23
U 1 1 5E5230C6
P 8300 3600
F 0 "C23" H 8392 3646 50  0000 L CNN
F 1 "0.1uF" H 8392 3555 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 8300 3600 50  0001 C CNN
F 3 "~" H 8300 3600 50  0001 C CNN
	1    8300 3600
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C24
U 1 1 5E523118
P 8700 3600
F 0 "C24" H 8792 3646 50  0000 L CNN
F 1 "0.01uF" H 8792 3555 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 8700 3600 50  0001 C CNN
F 3 "~" H 8700 3600 50  0001 C CNN
	1    8700 3600
	1    0    0    -1  
$EndComp
Wire Wire Line
	8150 3500 8300 3500
Connection ~ 8300 3500
Wire Wire Line
	8300 3500 8700 3500
Wire Wire Line
	8150 3700 8300 3700
Connection ~ 8300 3700
Wire Wire Line
	8300 3700 8700 3700
$Comp
L power:GND #PWR052
U 1 1 5E5234B1
P 8700 3750
F 0 "#PWR052" H 8700 3500 50  0001 C CNN
F 1 "GND" H 8700 3600 50  0000 C CNN
F 2 "" H 8700 3750 50  0001 C CNN
F 3 "" H 8700 3750 50  0001 C CNN
	1    8700 3750
	1    0    0    -1  
$EndComp
Wire Wire Line
	8700 3700 8700 3750
Connection ~ 8700 3700
$Comp
L power:+3.3V #PWR051
U 1 1 5E523681
P 8700 3450
F 0 "#PWR051" H 8700 3300 50  0001 C CNN
F 1 "+3.3V" H 8715 3623 50  0000 C CNN
F 2 "" H 8700 3450 50  0001 C CNN
F 3 "" H 8700 3450 50  0001 C CNN
	1    8700 3450
	1    0    0    -1  
$EndComp
Wire Wire Line
	8700 3450 8700 3500
Connection ~ 8700 3500
$Comp
L Device:C_Small C20
U 1 1 5E523BAC
P 5900 1200
F 0 "C20" V 5800 1200 50  0000 C CNN
F 1 "0.1uF" V 6000 1200 50  0000 C CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 5900 1200 50  0001 C CNN
F 3 "~" H 5900 1200 50  0001 C CNN
	1    5900 1200
	0    1    1    0   
$EndComp
$Comp
L Device:C_Small C19
U 1 1 5E52411A
P 5900 900
F 0 "C19" V 5800 900 50  0000 C CNN
F 1 "0.01uF" V 6000 900 50  0000 C CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 5900 900 50  0001 C CNN
F 3 "~" H 5900 900 50  0001 C CNN
	1    5900 900 
	0    1    1    0   
$EndComp
Wire Wire Line
	5800 900  5800 1200
Connection ~ 5800 1200
Wire Wire Line
	5800 1200 5800 1350
Wire Wire Line
	6000 900  6000 1200
Connection ~ 6000 1200
Wire Wire Line
	6000 1200 6000 1350
$Comp
L power:+3.3V #PWR047
U 1 1 5E52462E
P 5700 900
F 0 "#PWR047" H 5700 750 50  0001 C CNN
F 1 "+3.3V" H 5715 1073 50  0000 C CNN
F 2 "" H 5700 900 50  0001 C CNN
F 3 "" H 5700 900 50  0001 C CNN
	1    5700 900 
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR049
U 1 1 5E524677
P 6100 900
F 0 "#PWR049" H 6100 650 50  0001 C CNN
F 1 "GND" H 6100 750 50  0000 C CNN
F 2 "" H 6100 900 50  0001 C CNN
F 3 "" H 6100 900 50  0001 C CNN
	1    6100 900 
	1    0    0    -1  
$EndComp
Wire Wire Line
	6000 900  6100 900 
Connection ~ 6000 900 
Wire Wire Line
	5800 900  5700 900 
Connection ~ 5800 900 
$Comp
L Device:L_Core_Ferrite_Small L2
U 1 1 5E5251A2
P 4550 6450
F 0 "L2" V 4755 6450 50  0000 C CNN
F 1 "600R" V 4664 6450 50  0000 C CNN
F 2 "Inductor_SMD:L_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 4550 6450 50  0001 C CNN
F 3 "~" H 4550 6450 50  0001 C CNN
	1    4550 6450
	0    -1   -1   0   
$EndComp
$Comp
L power:GNDA #PWR044
U 1 1 5E52535B
P 4800 6800
F 0 "#PWR044" H 4800 6550 50  0001 C CNN
F 1 "GNDA" H 4805 6627 50  0000 C CNN
F 2 "" H 4800 6800 50  0001 C CNN
F 3 "" H 4800 6800 50  0001 C CNN
	1    4800 6800
	1    0    0    -1  
$EndComp
$Comp
L power:+3.3V #PWR042
U 1 1 5E5253A6
P 4350 6400
F 0 "#PWR042" H 4350 6250 50  0001 C CNN
F 1 "+3.3V" H 4365 6573 50  0000 C CNN
F 2 "" H 4350 6400 50  0001 C CNN
F 3 "" H 4350 6400 50  0001 C CNN
	1    4350 6400
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C16
U 1 1 5E52540D
P 4800 6600
F 0 "C16" H 4892 6646 50  0000 L CNN
F 1 "10uF" H 4892 6555 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 4800 6600 50  0001 C CNN
F 3 "~" H 4800 6600 50  0001 C CNN
	1    4800 6600
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C18
U 1 1 5E525464
P 5200 6600
F 0 "C18" H 5292 6646 50  0000 L CNN
F 1 "0.1uF" H 5292 6555 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 5200 6600 50  0001 C CNN
F 3 "~" H 5200 6600 50  0001 C CNN
	1    5200 6600
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R7
U 1 1 5E5254F5
P 4550 6750
F 0 "R7" V 4650 6750 50  0000 C CNN
F 1 "0" V 4450 6750 50  0000 C CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 4550 6750 50  0001 C CNN
F 3 "~" H 4550 6750 50  0001 C CNN
	1    4550 6750
	0    -1   -1   0   
$EndComp
$Comp
L power:GND #PWR043
U 1 1 5E5255B1
P 4350 6800
F 0 "#PWR043" H 4350 6550 50  0001 C CNN
F 1 "GND" H 4355 6627 50  0000 C CNN
F 2 "" H 4350 6800 50  0001 C CNN
F 3 "" H 4350 6800 50  0001 C CNN
	1    4350 6800
	1    0    0    -1  
$EndComp
Wire Wire Line
	4350 6400 4350 6450
Wire Wire Line
	4350 6450 4450 6450
$Comp
L power:GNDA #PWR046
U 1 1 5E5269C7
P 5200 6800
F 0 "#PWR046" H 5200 6550 50  0001 C CNN
F 1 "GNDA" H 5205 6627 50  0000 C CNN
F 2 "" H 5200 6800 50  0001 C CNN
F 3 "" H 5200 6800 50  0001 C CNN
	1    5200 6800
	1    0    0    -1  
$EndComp
Wire Wire Line
	5200 6450 5200 6500
Wire Wire Line
	4650 6450 4800 6450
Wire Wire Line
	5200 6700 5200 6800
Wire Wire Line
	4800 6450 4800 6500
Connection ~ 4800 6450
Wire Wire Line
	4800 6450 5200 6450
Wire Wire Line
	4800 6700 4800 6750
Wire Wire Line
	4800 6750 4650 6750
Connection ~ 4800 6750
Wire Wire Line
	4800 6750 4800 6800
Wire Wire Line
	4350 6750 4350 6800
Wire Wire Line
	4350 6750 4450 6750
Wire Wire Line
	4800 6450 4800 6000
$Comp
L power:GNDA #PWR045
U 1 1 5E52A82F
P 5000 6200
F 0 "#PWR045" H 5000 5950 50  0001 C CNN
F 1 "GNDA" H 5005 6027 50  0000 C CNN
F 2 "" H 5000 6200 50  0001 C CNN
F 3 "" H 5000 6200 50  0001 C CNN
	1    5000 6200
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C17
U 1 1 5E52B541
P 4900 6000
F 0 "C17" V 4800 6000 50  0000 C CNN
F 1 "0.1uF" V 5000 6000 50  0000 C CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 4900 6000 50  0001 C CNN
F 3 "~" H 4900 6000 50  0001 C CNN
	1    4900 6000
	0    1    1    0   
$EndComp
Connection ~ 4800 6000
Wire Wire Line
	4800 6000 4800 5850
Wire Wire Line
	5000 5850 5000 6000
Connection ~ 5000 6000
Wire Wire Line
	5000 6000 5000 6200
$Comp
L Device:C_Small C14
U 1 1 5E52C75A
P 3000 3400
F 0 "C14" H 3092 3446 50  0000 L CNN
F 1 "470pF" H 3092 3355 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 3000 3400 50  0001 C CNN
F 3 "~" H 3000 3400 50  0001 C CNN
	1    3000 3400
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R4
U 1 1 5E52C7AE
P 3000 3200
F 0 "R4" H 3059 3246 50  0000 L CNN
F 1 "10k" H 3059 3155 50  0000 L CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 3000 3200 50  0001 C CNN
F 3 "~" H 3000 3200 50  0001 C CNN
	1    3000 3200
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R3
U 1 1 5E52C806
P 2750 3300
F 0 "R3" V 2650 3300 50  0000 C CNN
F 1 "0" V 2850 3300 50  0000 C CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 2750 3300 50  0001 C CNN
F 3 "~" H 2750 3300 50  0001 C CNN
	1    2750 3300
	0    1    1    0   
$EndComp
Wire Wire Line
	2850 3300 3000 3300
Connection ~ 3000 3300
Wire Wire Line
	3000 3300 3350 3300
Text GLabel 2550 3300 0    50   Input ~ 0
nMCLR
Wire Wire Line
	2550 3300 2650 3300
$Comp
L power:GND #PWR041
U 1 1 5E52F5AF
P 3000 3500
F 0 "#PWR041" H 3000 3250 50  0001 C CNN
F 1 "GND" H 2850 3450 50  0000 C CNN
F 2 "" H 3000 3500 50  0001 C CNN
F 3 "" H 3000 3500 50  0001 C CNN
	1    3000 3500
	1    0    0    -1  
$EndComp
$Comp
L power:+3.3V #PWR040
U 1 1 5E52F6CD
P 3000 3100
F 0 "#PWR040" H 3000 2950 50  0001 C CNN
F 1 "+3.3V" H 3015 3273 50  0000 C CNN
F 2 "" H 3000 3100 50  0001 C CNN
F 3 "" H 3000 3100 50  0001 C CNN
	1    3000 3100
	1    0    0    -1  
$EndComp
Text GLabel 3250 1700 0    50   Input ~ 0
PWM1H
Text GLabel 4400 1050 1    50   Input ~ 0
PWM2H
Text GLabel 5000 1050 1    50   Input ~ 0
PWM3H
$Comp
L Device:R_Small R5
U 1 1 5E534E96
P 3400 1700
F 0 "R5" V 3300 1700 50  0000 C CNN
F 1 "75" V 3500 1700 50  0000 C CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 3400 1700 50  0001 C CNN
F 3 "~" H 3400 1700 50  0001 C CNN
	1    3400 1700
	0    1    1    0   
$EndComp
Wire Wire Line
	3250 1700 3300 1700
Wire Wire Line
	3500 1700 3650 1700
$Comp
L Device:R_Small R6
U 1 1 5E53C54B
P 4400 1200
F 0 "R6" V 4325 1200 50  0000 C CNN
F 1 "75" V 4475 1200 50  0000 C CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 4400 1200 50  0001 C CNN
F 3 "~" H 4400 1200 50  0001 C CNN
	1    4400 1200
	-1   0    0    1   
$EndComp
$Comp
L Device:R_Small R8
U 1 1 5E53C5D7
P 5000 1200
F 0 "R8" V 4925 1200 50  0000 C CNN
F 1 "75" V 5060 1200 50  0000 C CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 5000 1200 50  0001 C CNN
F 3 "~" H 5000 1200 50  0001 C CNN
	1    5000 1200
	-1   0    0    1   
$EndComp
Wire Wire Line
	4400 1350 4400 1300
Wire Wire Line
	4400 1100 4400 1050
Wire Wire Line
	5000 1050 5000 1100
Wire Wire Line
	5000 1300 5000 1350
Text GLabel 8250 2700 2    50   Input ~ 0
PGD3
Wire Wire Line
	8150 2700 8250 2700
Text GLabel 8250 2500 2    50   Input ~ 0
PGC3
Wire Wire Line
	8150 2500 8250 2500
Text GLabel 5400 5900 3    50   Input ~ 0
DRV_SCLK
Text GLabel 5600 5900 3    50   Input ~ 0
DRV_MOSI
Text GLabel 5800 5900 3    50   Input ~ 0
DRV_MISO
Wire Wire Line
	6000 6000 6000 6500
Connection ~ 6000 6000
Wire Wire Line
	6200 6000 6200 6500
Connection ~ 6200 6000
Text GLabel 5200 5900 3    50   Input ~ 0
DRV_nSCS
Wire Wire Line
	5200 5900 5200 5850
Wire Wire Line
	5400 5900 5400 5850
Wire Wire Line
	5600 5900 5600 5850
Wire Wire Line
	5800 5900 5800 5850
Text GLabel 6400 1250 1    50   Input ~ 0
UART_TX
Text GLabel 6600 1250 1    50   Input ~ 0
UART_RX
Wire Wire Line
	6400 1250 6400 1350
Wire Wire Line
	6600 1250 6600 1350
Text GLabel 6400 5900 3    50   Input ~ 0
ENC_MISO
Text GLabel 6600 5900 3    50   Input ~ 0
ENC_MOSI
Text GLabel 6800 5900 3    50   Input ~ 0
ENC_SCLK
Text GLabel 7000 5900 3    50   Input ~ 0
ENC_nSCS
Wire Wire Line
	6400 5850 6400 5900
Wire Wire Line
	6600 5850 6600 5900
Wire Wire Line
	6800 5850 6800 5900
Wire Wire Line
	7000 5850 7000 5900
Text GLabel 7400 1250 1    50   Input ~ 0
CAN_TX
Text GLabel 7800 1250 1    50   Input ~ 0
CAN_RX
Wire Wire Line
	7400 1250 7400 1350
Wire Wire Line
	7800 1250 7800 1350
Text GLabel 3600 4100 0    50   Input ~ 0
DRV_ENABLE
Text GLabel 3600 4700 0    50   Input ~ 0
SOA
Text GLabel 3600 5100 0    50   Input ~ 0
SOB
Wire Wire Line
	3600 4700 3650 4700
Wire Wire Line
	3600 5100 3650 5100
Wire Wire Line
	3600 4100 3650 4100
Text GLabel 3600 2500 0    50   Input ~ 0
nFAULT
Wire Wire Line
	3600 2500 3650 2500
Text GLabel 8250 1700 2    50   Input ~ 0
STBY
Wire Wire Line
	8150 1700 8250 1700
Text GLabel 3350 3200 1    50   Input ~ 0
PWRGD
Wire Wire Line
	3350 3200 3350 3300
Wire Wire Line
	3650 3300 3350 3300
Connection ~ 3350 3300
NoConn ~ 3650 2300
NoConn ~ 3650 2700
NoConn ~ 3650 2900
NoConn ~ 3650 3100
NoConn ~ 3650 3500
NoConn ~ 3650 4300
NoConn ~ 3650 4500
NoConn ~ 3650 4900
NoConn ~ 3650 5300
NoConn ~ 3650 5500
NoConn ~ 4200 5850
NoConn ~ 4400 5850
NoConn ~ 4600 5850
NoConn ~ 7200 5850
NoConn ~ 7400 5850
NoConn ~ 7600 5850
NoConn ~ 7800 5850
NoConn ~ 8150 5500
NoConn ~ 8150 5300
NoConn ~ 8150 5100
NoConn ~ 8150 4900
NoConn ~ 8150 4700
NoConn ~ 8150 4500
NoConn ~ 8150 4300
NoConn ~ 8150 4100
NoConn ~ 8150 3900
NoConn ~ 8150 3300
NoConn ~ 8150 3100
NoConn ~ 8150 2900
NoConn ~ 8150 2300
NoConn ~ 8150 2100
NoConn ~ 8150 1900
NoConn ~ 7600 1350
NoConn ~ 7200 1350
NoConn ~ 7000 1350
NoConn ~ 6800 1350
NoConn ~ 6200 1350
NoConn ~ 5600 1350
NoConn ~ 5400 1350
NoConn ~ 5200 1350
NoConn ~ 4600 1350
NoConn ~ 4200 1350
$Comp
L Connector:TestPoint TP1
U 1 1 5E5A3685
P 4000 5950
F 0 "TP1" H 3942 5977 50  0000 R CNN
F 1 "DACOUT1" H 3942 6068 50  0000 R CNN
F 2 "Connector_PinHeader_1.00mm:PinHeader_1x01_P1.00mm_Vertical" H 4200 5950 50  0001 C CNN
F 3 "~" H 4200 5950 50  0001 C CNN
	1    4000 5950
	-1   0    0    1   
$EndComp
Wire Wire Line
	4000 5850 4000 5950
NoConn ~ 3650 2100
NoConn ~ 4000 1350
$Comp
L jointController:dsPIC33CK256MP508 U3
U 1 1 5E522387
P 5900 3600
F 0 "U3" H 5850 3700 50  0000 L CNN
F 1 "dsPIC33CK256MP508" H 5500 3600 50  0000 L CNN
F 2 "Package_QFP:TQFP-80_12x12mm_P0.5mm" H 5950 3650 50  0001 C CNN
F 3 "" H 5950 3650 50  0001 C CNN
	1    5900 3600
	1    0    0    -1  
$EndComp
NoConn ~ 4800 1350
Text GLabel 3600 1900 0    50   Input ~ 0
PWM_EN
Wire Wire Line
	3600 1900 3650 1900
$EndSCHEMATC
