EESchema Schematic File Version 4
LIBS:jointController-cache
EELAYER 26 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 5 6
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
L Interface_CAN_LIN:MCP2562-E-MF U4
U 1 1 5E5788A7
P 5800 3950
F 0 "U4" H 5500 4300 50  0000 C CNN
F 1 "MCP2562-E-MF" H 6150 4300 50  0000 C CNN
F 2 "Package_DFN_QFN:DFN-8-1EP_3x3mm_P0.65mm_EP1.55x2.4mm" H 5800 3450 50  0001 C CIN
F 3 "http://ww1.microchip.com/downloads/en/DeviceDoc/25167A.pdf" H 5800 3950 50  0001 C CNN
	1    5800 3950
	1    0    0    -1  
$EndComp
$Comp
L power:+5V #PWR055
U 1 1 5E5788AE
P 5800 3050
F 0 "#PWR055" H 5800 2900 50  0001 C CNN
F 1 "+5V" H 5815 3223 50  0000 C CNN
F 2 "" H 5800 3050 50  0001 C CNN
F 3 "" H 5800 3050 50  0001 C CNN
	1    5800 3050
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR056
U 1 1 5E5788B4
P 5800 4350
F 0 "#PWR056" H 5800 4100 50  0001 C CNN
F 1 "GND" H 5805 4177 50  0000 C CNN
F 2 "" H 5800 4350 50  0001 C CNN
F 3 "" H 5800 4350 50  0001 C CNN
	1    5800 4350
	1    0    0    -1  
$EndComp
Text GLabel 5250 3750 0    50   Input ~ 0
CAN_TX
Text GLabel 5250 3850 0    50   Input ~ 0
CAN_RX
Text GLabel 6750 3850 2    50   Input ~ 0
CANH
Text GLabel 6750 4050 2    50   Input ~ 0
CANL
NoConn ~ 5700 4350
$Comp
L Device:C_Small C26
U 1 1 5E5788BF
P 5950 3200
F 0 "C26" H 6042 3246 50  0000 L CNN
F 1 "0.1uF" H 6042 3155 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 5950 3200 50  0001 C CNN
F 3 "~" H 5950 3200 50  0001 C CNN
	1    5950 3200
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR057
U 1 1 5E5788C6
P 5950 3300
F 0 "#PWR057" H 5950 3050 50  0001 C CNN
F 1 "GND" H 5950 3150 50  0000 C CNN
F 2 "" H 5950 3300 50  0001 C CNN
F 3 "" H 5950 3300 50  0001 C CNN
	1    5950 3300
	1    0    0    -1  
$EndComp
Wire Wire Line
	5800 3050 5800 3100
Wire Wire Line
	5800 3100 5950 3100
Connection ~ 5800 3100
Wire Wire Line
	5800 3100 5800 3550
Text GLabel 5250 4150 0    50   Input ~ 0
STBY
$Comp
L power:+3.3V #PWR053
U 1 1 5E5788D1
P 4650 4000
F 0 "#PWR053" H 4650 3850 50  0001 C CNN
F 1 "+3.3V" H 4665 4173 50  0000 C CNN
F 2 "" H 4650 4000 50  0001 C CNN
F 3 "" H 4650 4000 50  0001 C CNN
	1    4650 4000
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C25
U 1 1 5E5788D7
P 4650 4200
F 0 "C25" H 4742 4246 50  0000 L CNN
F 1 "0.1uF" H 4742 4155 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 4650 4200 50  0001 C CNN
F 3 "~" H 4650 4200 50  0001 C CNN
	1    4650 4200
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR054
U 1 1 5E5788DE
P 4650 4350
F 0 "#PWR054" H 4650 4100 50  0001 C CNN
F 1 "GND" H 4655 4177 50  0000 C CNN
F 2 "" H 4650 4350 50  0001 C CNN
F 3 "" H 4650 4350 50  0001 C CNN
	1    4650 4350
	1    0    0    -1  
$EndComp
Wire Wire Line
	4650 4000 4650 4050
Wire Wire Line
	4650 4300 4650 4350
Wire Wire Line
	4650 4050 5300 4050
Connection ~ 4650 4050
Wire Wire Line
	4650 4050 4650 4100
Wire Wire Line
	5250 3750 5300 3750
Wire Wire Line
	5250 3850 5300 3850
Wire Wire Line
	5250 4150 5300 4150
$Comp
L Device:R_Small R9
U 1 1 5E5788EC
P 6500 3950
F 0 "R9" H 6559 3996 50  0000 L CNN
F 1 "120" H 6559 3905 50  0000 L CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 6500 3950 50  0001 C CNN
F 3 "~" H 6500 3950 50  0001 C CNN
	1    6500 3950
	1    0    0    -1  
$EndComp
Wire Wire Line
	6300 3850 6500 3850
Connection ~ 6500 3850
Wire Wire Line
	6500 3850 6750 3850
Wire Wire Line
	6300 4050 6500 4050
Connection ~ 6500 4050
Wire Wire Line
	6500 4050 6750 4050
$EndSCHEMATC
