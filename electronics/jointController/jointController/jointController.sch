EESchema Schematic File Version 4
LIBS:jointController-cache
EELAYER 26 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 1 6
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
L Connector:Conn_01x02_Female J1
U 1 1 5E54FF31
P 900 1250
F 0 "J1" H 794 925 50  0000 C CNN
F 1 "PWR_IN" H 794 1016 50  0000 C CNN
F 2 "Connector_Wire:SolderWirePad_1x02_P5.08mm_Drill1.5mm" H 900 1250 50  0001 C CNN
F 3 "~" H 900 1250 50  0001 C CNN
	1    900  1250
	-1   0    0    1   
$EndComp
$Comp
L power:VCC #PWR03
U 1 1 5E55018E
P 1500 1150
F 0 "#PWR03" H 1500 1000 50  0001 C CNN
F 1 "VCC" H 1517 1323 50  0000 C CNN
F 2 "" H 1500 1150 50  0001 C CNN
F 3 "" H 1500 1150 50  0001 C CNN
	1    1500 1150
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR04
U 1 1 5E5501E7
P 1500 1250
F 0 "#PWR04" H 1500 1000 50  0001 C CNN
F 1 "GND" H 1505 1077 50  0000 C CNN
F 2 "" H 1500 1250 50  0001 C CNN
F 3 "" H 1500 1250 50  0001 C CNN
	1    1500 1250
	1    0    0    -1  
$EndComp
Wire Wire Line
	1100 1150 1300 1150
Wire Wire Line
	1100 1250 1300 1250
$Comp
L power:PWR_FLAG #FLG01
U 1 1 5E553CD8
P 1300 1050
F 0 "#FLG01" H 1300 1125 50  0001 C CNN
F 1 "PWR_FLAG" H 1300 1224 50  0000 C CNN
F 2 "" H 1300 1050 50  0001 C CNN
F 3 "~" H 1300 1050 50  0001 C CNN
	1    1300 1050
	1    0    0    -1  
$EndComp
$Comp
L power:PWR_FLAG #FLG02
U 1 1 5E553D23
P 1300 1350
F 0 "#FLG02" H 1300 1425 50  0001 C CNN
F 1 "PWR_FLAG" H 1300 1523 50  0000 C CNN
F 2 "" H 1300 1350 50  0001 C CNN
F 3 "~" H 1300 1350 50  0001 C CNN
	1    1300 1350
	-1   0    0    1   
$EndComp
Wire Wire Line
	1300 1150 1300 1050
Connection ~ 1300 1150
Wire Wire Line
	1300 1150 1500 1150
Wire Wire Line
	1300 1250 1300 1350
Connection ~ 1300 1250
Wire Wire Line
	1300 1250 1500 1250
$Comp
L Device:C_Small C1
U 1 1 5E557D49
P 2000 1200
F 0 "C1" H 2088 1246 50  0000 L CNN
F 1 "10uF" H 2088 1155 50  0000 L CNN
F 2 "Capacitor_SMD:C_1206_3216Metric_Pad1.42x1.75mm_HandSolder" H 2000 1200 50  0001 C CNN
F 3 "~" H 2000 1200 50  0001 C CNN
	1    2000 1200
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C2
U 1 1 5E557DD7
P 2400 1200
F 0 "C2" H 2488 1246 50  0000 L CNN
F 1 "10uF" H 2488 1155 50  0000 L CNN
F 2 "Capacitor_SMD:C_1206_3216Metric_Pad1.42x1.75mm_HandSolder" H 2400 1200 50  0001 C CNN
F 3 "~" H 2400 1200 50  0001 C CNN
	1    2400 1200
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C3
U 1 1 5E557E1B
P 2800 1200
F 0 "C3" H 2888 1246 50  0000 L CNN
F 1 "10uF" H 2888 1155 50  0000 L CNN
F 2 "Capacitor_SMD:C_1206_3216Metric_Pad1.42x1.75mm_HandSolder" H 2800 1200 50  0001 C CNN
F 3 "~" H 2800 1200 50  0001 C CNN
	1    2800 1200
	1    0    0    -1  
$EndComp
Wire Wire Line
	2000 1100 2400 1100
Connection ~ 2400 1100
Wire Wire Line
	2400 1100 2600 1100
Wire Wire Line
	2000 1300 2400 1300
Connection ~ 2400 1300
Wire Wire Line
	2400 1300 2600 1300
$Comp
L power:GND #PWR06
U 1 1 5E55C016
P 2600 1300
F 0 "#PWR06" H 2600 1050 50  0001 C CNN
F 1 "GND" H 2605 1127 50  0000 C CNN
F 2 "" H 2600 1300 50  0001 C CNN
F 3 "" H 2600 1300 50  0001 C CNN
	1    2600 1300
	1    0    0    -1  
$EndComp
$Comp
L power:VCC #PWR05
U 1 1 5E55C09F
P 2600 1100
F 0 "#PWR05" H 2600 950 50  0001 C CNN
F 1 "VCC" H 2617 1273 50  0000 C CNN
F 2 "" H 2600 1100 50  0001 C CNN
F 3 "" H 2600 1100 50  0001 C CNN
	1    2600 1100
	1    0    0    -1  
$EndComp
Wire Notes Line
	800  750  800  1600
Text Notes 1300 750  2    50   ~ 0
POWER INPUT\n
Text GLabel 8400 3350 0    50   Input ~ 0
A_OUT
Text GLabel 8400 3550 0    50   Input ~ 0
B_OUT
Text GLabel 8400 3750 0    50   Input ~ 0
C_OUT
Wire Wire Line
	8400 3350 8500 3350
Wire Wire Line
	8400 3550 8500 3550
Wire Wire Line
	8400 3750 8500 3750
$Sheet
S 6000 3150 1350 900 
U 5E5F8AF6
F0 "Three Phase Power Inverter" 50
F1 "three_phase_inverter.sch" 50
$EndSheet
$Sheet
S 3600 4250 1350 900 
U 5E60949F
F0 "DRV8323" 50
F1 "drv8323.sch" 50
$EndSheet
$Sheet
S 3600 3150 1350 850 
U 5E60C0F3
F0 "dsPIC33CK256MP508" 50
F1 "dsPIC33CK256MP508.sch" 50
$EndSheet
$Sheet
S 6000 4350 700  450 
U 5E578538
F0 "MCP2562" 50
F1 "MCP2562.sch" 50
$EndSheet
$Sheet
S 6800 4350 550  450 
U 5E5790A1
F0 "MA702" 50
F1 "MA702.sch" 50
$EndSheet
$Comp
L Connector:Conn_01x06_Male J2
U 1 1 5E57B0A7
P 1750 2150
F 0 "J2" H 1722 2123 50  0000 R CNN
F 1 "ICSP" H 1722 2032 50  0000 R CNN
F 2 "jointController:PinHeader_1x06_P2.54mm_Vertical_Staggered" H 1750 2150 50  0001 C CNN
F 3 "~" H 1750 2150 50  0001 C CNN
	1    1750 2150
	-1   0    0    -1  
$EndComp
$Comp
L Connector:Conn_01x03_Female J7
U 1 1 5E57B1DC
P 8800 4550
F 0 "J7" H 8828 4576 50  0000 L CNN
F 1 "UART1" H 8828 4485 50  0000 L CNN
F 2 "Connector_JST:JST_SH_BM03B-SRSS-TB_1x03-1MP_P1.00mm_Vertical" H 8800 4550 50  0001 C CNN
F 3 "~" H 8800 4550 50  0001 C CNN
	1    8800 4550
	1    0    0    -1  
$EndComp
$Comp
L Connector:Conn_01x03_Female J3
U 1 1 5E57B266
P 8650 5150
F 0 "J3" H 8678 5176 50  0000 L CNN
F 1 "CAN" H 8678 5085 50  0000 L CNN
F 2 "Connector_JST:JST_SH_BM03B-SRSS-TB_1x03-1MP_P1.00mm_Vertical" H 8650 5150 50  0001 C CNN
F 3 "~" H 8650 5150 50  0001 C CNN
	1    8650 5150
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR012
U 1 1 5E57B8AA
P 8500 4650
F 0 "#PWR012" H 8500 4400 50  0001 C CNN
F 1 "GND" H 8350 4600 50  0000 C CNN
F 2 "" H 8500 4650 50  0001 C CNN
F 3 "" H 8500 4650 50  0001 C CNN
	1    8500 4650
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR011
U 1 1 5E57B9E8
P 8350 5250
F 0 "#PWR011" H 8350 5000 50  0001 C CNN
F 1 "GND" H 8200 5200 50  0000 C CNN
F 2 "" H 8350 5250 50  0001 C CNN
F 3 "" H 8350 5250 50  0001 C CNN
	1    8350 5250
	1    0    0    -1  
$EndComp
NoConn ~ 1550 2450
Text GLabel 1450 2350 0    50   Input ~ 0
PGC3
Text GLabel 1450 2250 0    50   Input ~ 0
PGD3
Text GLabel 1450 1950 0    50   Input ~ 0
nMCLR
$Comp
L power:+3.3V #PWR01
U 1 1 5E57BEC9
P 950 2050
F 0 "#PWR01" H 950 1900 50  0001 C CNN
F 1 "+3.3V" H 965 2223 50  0000 C CNN
F 2 "" H 950 2050 50  0001 C CNN
F 3 "" H 950 2050 50  0001 C CNN
	1    950  2050
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR02
U 1 1 5E57BF10
P 950 2150
F 0 "#PWR02" H 950 1900 50  0001 C CNN
F 1 "GND" H 955 1977 50  0000 C CNN
F 2 "" H 950 2150 50  0001 C CNN
F 3 "" H 950 2150 50  0001 C CNN
	1    950  2150
	1    0    0    -1  
$EndComp
Wire Wire Line
	950  2050 1550 2050
Wire Wire Line
	950  2150 1550 2150
Text GLabel 8500 4450 0    50   Input ~ 0
UART_TX
Text GLabel 8500 4550 0    50   Input ~ 0
UART_RX
Text GLabel 8350 5050 0    50   Input ~ 0
CANH
Text GLabel 8350 5150 0    50   Input ~ 0
CANL
Wire Wire Line
	8500 4450 8600 4450
Wire Wire Line
	8500 4550 8600 4550
Wire Wire Line
	8500 4650 8600 4650
Wire Wire Line
	8350 5250 8450 5250
Wire Wire Line
	1450 2350 1550 2350
Wire Wire Line
	1450 2250 1550 2250
Wire Wire Line
	1450 1950 1550 1950
Wire Notes Line
	800  1800 800  2550
Wire Notes Line
	800  2550 2000 2550
Wire Notes Line
	2000 2550 2000 1800
Wire Notes Line
	2000 1800 800  1800
Text Notes 800  1800 0    50   ~ 0
ICSP / PICKit
Wire Notes Line
	8050 4350 8050 4800
Wire Notes Line
	8050 4800 9100 4800
Wire Notes Line
	9100 4800 9100 4350
Wire Notes Line
	9100 4350 8050 4350
Text Notes 8050 4350 0    50   ~ 0
UART
$Comp
L jointController:MCP1755-3v3 U1
U 1 1 5E54F45F
P 5450 1250
F 0 "U1" H 5450 1675 50  0000 C CNN
F 1 "MCP1755-3v3" H 5450 1584 50  0000 C CNN
F 2 "Package_TO_SOT_SMD:SOT-23-5" H 5350 2050 50  0001 C CNN
F 3 "" H 5350 2050 50  0001 C CNN
	1    5450 1250
	1    0    0    -1  
$EndComp
Wire Wire Line
	5050 1050 5000 1050
Wire Wire Line
	5000 1050 5000 1250
Wire Wire Line
	5000 1250 5050 1250
Text GLabel 5000 1450 0    50   Input ~ 0
PWRGD
Wire Wire Line
	5000 1450 5050 1450
$Comp
L Device:C_Small C5
U 1 1 5E550325
P 4450 1250
F 0 "C5" H 4542 1296 50  0000 L CNN
F 1 "10uF" H 4542 1205 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 4450 1250 50  0001 C CNN
F 3 "~" H 4450 1250 50  0001 C CNN
	1    4450 1250
	1    0    0    -1  
$EndComp
Wire Wire Line
	4450 1050 5000 1050
Connection ~ 5000 1050
Wire Wire Line
	4450 1050 4450 1150
$Comp
L power:GND #PWR08
U 1 1 5E55130E
P 4450 1500
F 0 "#PWR08" H 4450 1250 50  0001 C CNN
F 1 "GND" H 4455 1327 50  0000 C CNN
F 2 "" H 4450 1500 50  0001 C CNN
F 3 "" H 4450 1500 50  0001 C CNN
	1    4450 1500
	1    0    0    -1  
$EndComp
Wire Wire Line
	4450 1350 4450 1500
$Comp
L power:+5V #PWR07
U 1 1 5E551EEF
P 4450 1000
F 0 "#PWR07" H 4450 850 50  0001 C CNN
F 1 "+5V" H 4465 1173 50  0000 C CNN
F 2 "" H 4450 1000 50  0001 C CNN
F 3 "" H 4450 1000 50  0001 C CNN
	1    4450 1000
	1    0    0    -1  
$EndComp
Connection ~ 4450 1050
$Comp
L Device:C_Small C6
U 1 1 5E5520B9
P 6000 1250
F 0 "C6" H 6092 1296 50  0000 L CNN
F 1 "10uF" H 6092 1205 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 6000 1250 50  0001 C CNN
F 3 "~" H 6000 1250 50  0001 C CNN
	1    6000 1250
	1    0    0    -1  
$EndComp
Wire Wire Line
	5850 1050 6000 1050
Wire Wire Line
	6000 1050 6000 1150
Wire Wire Line
	6000 1350 6000 1450
Wire Wire Line
	6000 1450 5850 1450
$Comp
L power:GND #PWR010
U 1 1 5E553017
P 6000 1500
F 0 "#PWR010" H 6000 1250 50  0001 C CNN
F 1 "GND" H 6005 1327 50  0000 C CNN
F 2 "" H 6000 1500 50  0001 C CNN
F 3 "" H 6000 1500 50  0001 C CNN
	1    6000 1500
	1    0    0    -1  
$EndComp
Wire Wire Line
	6000 1450 6000 1500
Connection ~ 6000 1450
$Comp
L power:+3.3V #PWR09
U 1 1 5E5538BA
P 6000 1000
F 0 "#PWR09" H 6000 850 50  0001 C CNN
F 1 "+3.3V" H 6015 1173 50  0000 C CNN
F 2 "" H 6000 1000 50  0001 C CNN
F 3 "" H 6000 1000 50  0001 C CNN
	1    6000 1000
	1    0    0    -1  
$EndComp
Wire Wire Line
	4450 1000 4450 1050
Wire Wire Line
	6000 1000 6000 1050
Connection ~ 6000 1050
Wire Notes Line
	4350 750  4350 1750
Wire Notes Line
	4350 1750 6300 1750
Wire Notes Line
	6300 1750 6300 750 
Wire Notes Line
	6300 750  4350 750 
Text Notes 4350 750  0    50   ~ 0
3.3V Regulator
$Comp
L Device:C_Small C4
U 1 1 5E5E29CE
P 3200 1200
F 0 "C4" H 3288 1246 50  0000 L CNN
F 1 "10uF" H 3288 1155 50  0000 L CNN
F 2 "Capacitor_SMD:C_1206_3216Metric_Pad1.42x1.75mm_HandSolder" H 3200 1200 50  0001 C CNN
F 3 "~" H 3200 1200 50  0001 C CNN
	1    3200 1200
	1    0    0    -1  
$EndComp
Wire Wire Line
	2800 1100 3200 1100
Connection ~ 2800 1100
Wire Wire Line
	2800 1300 3200 1300
Connection ~ 2800 1300
Connection ~ 2600 1100
Wire Wire Line
	2600 1100 2800 1100
Connection ~ 2600 1300
Wire Wire Line
	2600 1300 2800 1300
Wire Notes Line
	3500 750  3500 1600
Wire Notes Line
	800  1600 3500 1600
Wire Notes Line
	800  750  3500 750 
Text Notes 8050 4950 0    50   ~ 0
CAN-FD
Wire Notes Line
	8850 4950 8050 4950
Wire Notes Line
	8850 5400 8850 4950
Wire Notes Line
	8050 5400 8850 5400
Wire Notes Line
	8050 4950 8050 5400
Wire Wire Line
	8350 5050 8450 5050
Wire Wire Line
	8350 5150 8450 5150
$Comp
L Connector:Conn_01x03_Female J8
U 1 1 5E5F40B4
P 9550 5150
F 0 "J8" H 9578 5176 50  0000 L CNN
F 1 "CAN" H 9578 5085 50  0000 L CNN
F 2 "Connector_JST:JST_SH_BM03B-SRSS-TB_1x03-1MP_P1.00mm_Vertical" H 9550 5150 50  0001 C CNN
F 3 "~" H 9550 5150 50  0001 C CNN
	1    9550 5150
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR013
U 1 1 5E5F40BA
P 9250 5250
F 0 "#PWR013" H 9250 5000 50  0001 C CNN
F 1 "GND" H 9100 5200 50  0000 C CNN
F 2 "" H 9250 5250 50  0001 C CNN
F 3 "" H 9250 5250 50  0001 C CNN
	1    9250 5250
	1    0    0    -1  
$EndComp
Text GLabel 9250 5050 0    50   Input ~ 0
CANH
Text GLabel 9250 5150 0    50   Input ~ 0
CANL
Wire Wire Line
	9250 5250 9350 5250
Text Notes 8950 4950 0    50   ~ 0
CAN-FD
Wire Notes Line
	9750 4950 8950 4950
Wire Notes Line
	9750 5400 9750 4950
Wire Notes Line
	8950 5400 9750 5400
Wire Notes Line
	8950 4950 8950 5400
Wire Wire Line
	9250 5050 9350 5050
Wire Wire Line
	9250 5150 9350 5150
$Comp
L Connector:Conn_01x01_Female J4
U 1 1 5E5F72AB
P 8700 3350
F 0 "J4" H 8727 3376 50  0000 L CNN
F 1 "A_OUT" H 8727 3285 50  0000 L CNN
F 2 "Connector_Wire:SolderWirePad_1x01_Drill1.5mm" H 8700 3350 50  0001 C CNN
F 3 "~" H 8700 3350 50  0001 C CNN
	1    8700 3350
	1    0    0    -1  
$EndComp
$Comp
L Connector:Conn_01x01_Female J5
U 1 1 5E5F7319
P 8700 3550
F 0 "J5" H 8728 3576 50  0000 L CNN
F 1 "B_OUT" H 8728 3485 50  0000 L CNN
F 2 "Connector_Wire:SolderWirePad_1x01_Drill1.5mm" H 8700 3550 50  0001 C CNN
F 3 "~" H 8700 3550 50  0001 C CNN
	1    8700 3550
	1    0    0    -1  
$EndComp
$Comp
L Connector:Conn_01x01_Female J6
U 1 1 5E5F7349
P 8700 3750
F 0 "J6" H 8728 3776 50  0000 L CNN
F 1 "C_OUT" H 8728 3685 50  0000 L CNN
F 2 "Connector_Wire:SolderWirePad_1x01_Drill1.5mm" H 8700 3750 50  0001 C CNN
F 3 "~" H 8700 3750 50  0001 C CNN
	1    8700 3750
	1    0    0    -1  
$EndComp
Wire Notes Line
	8050 3250 8050 3900
Wire Notes Line
	8050 3900 9000 3900
Wire Notes Line
	9000 3900 9000 3250
Wire Notes Line
	8050 3250 9000 3250
Text Notes 8050 3250 0    50   ~ 0
MOTOR_OUT
$EndSCHEMATC
