EESchema Schematic File Version 4
LIBS:actuator-cache
EELAYER 26 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 1 1
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
L Transistor_FET:IRF6665 Q1
U 1 1 5D5770D0
P 5500 3350
F 0 "Q1" H 5706 3396 50  0000 L CNN
F 1 "NTMFS4935NT1G" H 5706 3305 50  0000 L CNN
F 2 "actuator:SO-FL-8" H 5500 3350 50  0001 C CIN
F 3 "https://www.infineon.com/dgdl/irf6665pbf.pdf?fileId=5546d462533600a4015355ec8dcb1a62" H 5500 3350 50  0001 L CNN
	1    5500 3350
	1    0    0    -1  
$EndComp
$Comp
L Transistor_FET:IRF6665 Q3
U 1 1 5D5771E5
P 6700 3350
F 0 "Q3" H 6906 3396 50  0000 L CNN
F 1 "NTMFS4935NT1G" H 6906 3305 50  0000 L CNN
F 2 "actuator:SO-FL-8" H 6700 3350 50  0001 C CIN
F 3 "https://www.infineon.com/dgdl/irf6665pbf.pdf?fileId=5546d462533600a4015355ec8dcb1a62" H 6700 3350 50  0001 L CNN
	1    6700 3350
	1    0    0    -1  
$EndComp
$Comp
L Transistor_FET:IRF6665 Q5
U 1 1 5D577231
P 7900 3350
F 0 "Q5" H 8106 3396 50  0000 L CNN
F 1 "NTMFS4935NT1G" H 8106 3305 50  0000 L CNN
F 2 "actuator:SO-FL-8" H 7900 3350 50  0001 C CIN
F 3 "https://www.infineon.com/dgdl/irf6665pbf.pdf?fileId=5546d462533600a4015355ec8dcb1a62" H 7900 3350 50  0001 L CNN
	1    7900 3350
	1    0    0    -1  
$EndComp
$Comp
L Transistor_FET:IRF6665 Q2
U 1 1 5D5773ED
P 5500 4050
F 0 "Q2" H 5706 4096 50  0000 L CNN
F 1 "NTMFS4935NT1G" H 5706 4005 50  0000 L CNN
F 2 "actuator:SO-FL-8" H 5500 4050 50  0001 C CIN
F 3 "https://www.infineon.com/dgdl/irf6665pbf.pdf?fileId=5546d462533600a4015355ec8dcb1a62" H 5500 4050 50  0001 L CNN
	1    5500 4050
	1    0    0    -1  
$EndComp
$Comp
L Transistor_FET:IRF6665 Q4
U 1 1 5D577437
P 6700 4050
F 0 "Q4" H 6906 4096 50  0000 L CNN
F 1 "NTMFS4935NT1G" H 6906 4005 50  0000 L CNN
F 2 "actuator:SO-FL-8" H 6700 4050 50  0001 C CIN
F 3 "https://www.infineon.com/dgdl/irf6665pbf.pdf?fileId=5546d462533600a4015355ec8dcb1a62" H 6700 4050 50  0001 L CNN
	1    6700 4050
	1    0    0    -1  
$EndComp
$Comp
L Transistor_FET:IRF6665 Q6
U 1 1 5D577471
P 7900 4050
F 0 "Q6" H 8106 4096 50  0000 L CNN
F 1 "NTMFS4935NT1G" H 8106 4005 50  0000 L CNN
F 2 "actuator:SO-FL-8" H 7900 4050 50  0001 C CIN
F 3 "https://www.infineon.com/dgdl/irf6665pbf.pdf?fileId=5546d462533600a4015355ec8dcb1a62" H 7900 4050 50  0001 L CNN
	1    7900 4050
	1    0    0    -1  
$EndComp
Text GLabel 5200 3150 0    50   Input ~ 0
GHA
Text GLabel 6400 3150 0    50   Input ~ 0
GHB
Text GLabel 7600 3150 0    50   Input ~ 0
GHC
Text GLabel 7600 3850 0    50   Input ~ 0
GLC
Text GLabel 6400 3850 0    50   Input ~ 0
GLB
Text GLabel 5200 3850 0    50   Input ~ 0
GLA
Wire Wire Line
	5200 3150 5250 3150
Wire Wire Line
	5250 3150 5250 3350
Wire Wire Line
	5250 3350 5300 3350
Wire Wire Line
	6400 3150 6450 3150
Wire Wire Line
	6450 3150 6450 3350
Wire Wire Line
	6450 3350 6500 3350
Wire Wire Line
	7600 3150 7650 3150
Wire Wire Line
	7650 3150 7650 3350
Wire Wire Line
	7650 3350 7700 3350
Wire Wire Line
	5200 3850 5250 3850
Wire Wire Line
	5250 3850 5250 4050
Wire Wire Line
	5250 4050 5300 4050
Wire Wire Line
	6400 3850 6450 3850
Wire Wire Line
	6450 3850 6450 4050
Wire Wire Line
	6450 4050 6500 4050
Wire Wire Line
	7600 3850 7650 3850
Wire Wire Line
	7650 3850 7650 4050
Wire Wire Line
	7650 4050 7700 4050
$Comp
L Device:R R6
U 1 1 5D583EC2
P 5600 4600
F 0 "R6" H 5670 4646 50  0000 L CNN
F 1 "1m" H 5670 4555 50  0000 L CNN
F 2 "Resistor_SMD:R_1210_3225Metric" V 5530 4600 50  0001 C CNN
F 3 "~" H 5600 4600 50  0001 C CNN
	1    5600 4600
	1    0    0    -1  
$EndComp
$Comp
L Device:R R7
U 1 1 5D5840EC
P 6800 4600
F 0 "R7" H 6870 4646 50  0000 L CNN
F 1 "1m" H 6870 4555 50  0000 L CNN
F 2 "Resistor_SMD:R_1210_3225Metric" V 6730 4600 50  0001 C CNN
F 3 "~" H 6800 4600 50  0001 C CNN
	1    6800 4600
	1    0    0    -1  
$EndComp
$Comp
L Device:R R8
U 1 1 5D584126
P 8000 4600
F 0 "R8" H 8070 4646 50  0000 L CNN
F 1 "1m" H 8070 4555 50  0000 L CNN
F 2 "Resistor_SMD:R_1210_3225Metric" V 7930 4600 50  0001 C CNN
F 3 "~" H 8000 4600 50  0001 C CNN
	1    8000 4600
	1    0    0    -1  
$EndComp
Wire Wire Line
	5600 4250 5600 4350
Wire Wire Line
	6800 4250 6800 4350
Wire Wire Line
	8000 4250 8000 4350
$Comp
L power:GND #PWR014
U 1 1 5D591C59
P 5600 4900
F 0 "#PWR014" H 5600 4650 50  0001 C CNN
F 1 "GND" H 5605 4727 50  0000 C CNN
F 2 "" H 5600 4900 50  0001 C CNN
F 3 "" H 5600 4900 50  0001 C CNN
	1    5600 4900
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR015
U 1 1 5D591C9B
P 6800 4900
F 0 "#PWR015" H 6800 4650 50  0001 C CNN
F 1 "GND" H 6805 4727 50  0000 C CNN
F 2 "" H 6800 4900 50  0001 C CNN
F 3 "" H 6800 4900 50  0001 C CNN
	1    6800 4900
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR016
U 1 1 5D591CCD
P 8000 4900
F 0 "#PWR016" H 8000 4650 50  0001 C CNN
F 1 "GND" H 8005 4727 50  0000 C CNN
F 2 "" H 8000 4900 50  0001 C CNN
F 3 "" H 8000 4900 50  0001 C CNN
	1    8000 4900
	1    0    0    -1  
$EndComp
Wire Wire Line
	5600 4750 5600 4800
Wire Wire Line
	6800 4750 6800 4800
Wire Wire Line
	8000 4750 8000 4800
Text GLabel 5200 4350 0    50   Input ~ 0
SPA
Text GLabel 6400 4350 0    50   Input ~ 0
SPB
Text GLabel 7600 4350 0    50   Input ~ 0
SPC
Text GLabel 5200 4800 0    50   Input ~ 0
SNA
Text GLabel 6400 4800 0    50   Input ~ 0
SNB
Text GLabel 7600 4800 0    50   Input ~ 0
SNC
Wire Wire Line
	5200 4350 5600 4350
Connection ~ 5600 4350
Wire Wire Line
	5600 4350 5600 4450
Wire Wire Line
	6400 4350 6800 4350
Connection ~ 6800 4350
Wire Wire Line
	6800 4350 6800 4450
Wire Wire Line
	7600 4350 8000 4350
Connection ~ 8000 4350
Wire Wire Line
	8000 4350 8000 4450
Wire Wire Line
	5200 4800 5600 4800
Connection ~ 5600 4800
Wire Wire Line
	5600 4800 5600 4900
Wire Wire Line
	6400 4800 6800 4800
Connection ~ 6800 4800
Wire Wire Line
	6800 4800 6800 4900
Wire Wire Line
	7600 4800 8000 4800
Connection ~ 8000 4800
Wire Wire Line
	8000 4800 8000 4900
Text GLabel 5200 3650 0    50   Input ~ 0
SHA
Text GLabel 6400 3650 0    50   Input ~ 0
SHB
Text GLabel 7600 3650 0    50   Input ~ 0
SHC
Wire Wire Line
	5200 3650 5600 3650
Wire Wire Line
	6400 3650 6800 3650
Wire Wire Line
	7600 3650 8000 3650
Connection ~ 5600 3650
Wire Wire Line
	5600 3650 5600 3850
Wire Wire Line
	5600 3550 5600 3650
Connection ~ 6800 3650
Wire Wire Line
	6800 3650 6800 3850
Wire Wire Line
	6800 3550 6800 3650
Connection ~ 8000 3650
Wire Wire Line
	8000 3650 8000 3850
Wire Wire Line
	8000 3550 8000 3650
Text GLabel 5600 2600 1    50   Input ~ 0
VM
Text GLabel 6800 2600 1    50   Input ~ 0
VM
Text GLabel 8000 2600 1    50   Input ~ 0
VM
Wire Wire Line
	6800 2600 6800 2750
Connection ~ 6800 2750
Wire Wire Line
	6800 2750 6800 3150
Text GLabel 5700 3650 2    50   Input ~ 0
A_OUT
Text GLabel 6900 3650 2    50   Input ~ 0
B_OUT
Text GLabel 8100 3650 2    50   Input ~ 0
C_OUT
Wire Wire Line
	5600 3650 5700 3650
Wire Wire Line
	6800 3650 6900 3650
Wire Wire Line
	8000 3650 8100 3650
$Comp
L Connector:Screw_Terminal_01x03 J1
U 1 1 5D5DAF6D
P 9550 3700
F 0 "J1" H 9630 3742 50  0000 L CNN
F 1 "Screw_Terminal_01x03" H 9630 3651 50  0000 L CNN
F 2 "TerminalBlock_4Ucon:TerminalBlock_4Ucon_1x03_P3.50mm_Horizontal" H 9550 3700 50  0001 C CNN
F 3 "~" H 9550 3700 50  0001 C CNN
	1    9550 3700
	1    0    0    -1  
$EndComp
Text GLabel 9250 3600 0    50   Input ~ 0
A_OUT
Text GLabel 9250 3700 0    50   Input ~ 0
B_OUT
Text GLabel 9250 3800 0    50   Input ~ 0
C_OUT
Wire Wire Line
	9250 3600 9350 3600
Wire Wire Line
	9250 3700 9350 3700
Wire Wire Line
	9250 3800 9350 3800
$Comp
L actuator-rescue:drv8323rs-custom_drv U1
U 1 1 5D5BEEB7
P 2700 3850
F 0 "U1" H 2700 5225 50  0000 C CNN
F 1 "drv8323rs" H 2700 5134 50  0000 C CNN
F 2 "Package_DFN_QFN:Texas_S-PVQFN-N48_EP5.15x5.15mm_ThermalVias" H 2700 3850 50  0001 C CNN
F 3 "" H 2700 3850 50  0001 C CNN
	1    2700 3850
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R2
U 1 1 5D5D3372
P 1050 2850
F 0 "R2" H 1109 2896 50  0000 L CNN
F 1 "8.45k" H 1109 2805 50  0000 L CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 1050 2850 50  0001 C CNN
F 3 "~" H 1050 2850 50  0001 C CNN
	1    1050 2850
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R1
U 1 1 5D5D33F0
P 1050 2550
F 0 "R1" H 1109 2596 50  0000 L CNN
F 1 "28k" H 1109 2505 50  0000 L CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 1050 2550 50  0001 C CNN
F 3 "~" H 1050 2550 50  0001 C CNN
	1    1050 2550
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR02
U 1 1 5D5D345B
P 1050 2950
F 0 "#PWR02" H 1050 2700 50  0001 C CNN
F 1 "GND" H 1055 2777 50  0000 C CNN
F 2 "" H 1050 2950 50  0001 C CNN
F 3 "" H 1050 2950 50  0001 C CNN
	1    1050 2950
	1    0    0    -1  
$EndComp
$Comp
L power:VCC #PWR01
U 1 1 5D5D34B2
P 1050 2450
F 0 "#PWR01" H 1050 2300 50  0001 C CNN
F 1 "VCC" H 1067 2623 50  0000 C CNN
F 2 "" H 1050 2450 50  0001 C CNN
F 3 "" H 1050 2450 50  0001 C CNN
	1    1050 2450
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR04
U 1 1 5D5DA76B
P 1450 2950
F 0 "#PWR04" H 1450 2700 50  0001 C CNN
F 1 "GND" H 1455 2777 50  0000 C CNN
F 2 "" H 1450 2950 50  0001 C CNN
F 3 "" H 1450 2950 50  0001 C CNN
	1    1450 2950
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C2
U 1 1 5D5DB8E5
P 1750 3100
F 0 "C2" H 1842 3146 50  0000 L CNN
F 1 "47uF" H 1842 3055 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric" H 1750 3100 50  0001 C CNN
F 3 "~" H 1750 3100 50  0001 C CNN
	1    1750 3100
	1    0    0    -1  
$EndComp
Wire Wire Line
	1050 2650 1050 2700
Wire Wire Line
	2200 2700 1050 2700
Connection ~ 1050 2700
Wire Wire Line
	1050 2700 1050 2750
Wire Wire Line
	1750 3000 2200 3000
Wire Wire Line
	1750 3200 2150 3200
Wire Wire Line
	2150 3200 2150 3150
Wire Wire Line
	2150 3150 2200 3150
$Comp
L Device:C_Small C3
U 1 1 5D5E98A4
P 1750 3400
F 0 "C3" H 1842 3446 50  0000 L CNN
F 1 "1uF" H 1842 3355 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric" H 1750 3400 50  0001 C CNN
F 3 "~" H 1750 3400 50  0001 C CNN
	1    1750 3400
	1    0    0    -1  
$EndComp
Wire Wire Line
	1750 3300 2200 3300
Wire Wire Line
	1750 3500 2150 3500
Wire Wire Line
	2150 3500 2150 3450
Wire Wire Line
	2150 3450 2200 3450
$Comp
L Device:C_Small C1
U 1 1 5D5EC9EF
P 1050 3600
F 0 "C1" H 1142 3646 50  0000 L CNN
F 1 "0.1uF" H 1142 3555 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric" H 1050 3600 50  0001 C CNN
F 3 "~" H 1050 3600 50  0001 C CNN
	1    1050 3600
	1    0    0    -1  
$EndComp
Wire Wire Line
	1050 3500 1750 3500
Connection ~ 1750 3500
$Comp
L power:GND #PWR03
U 1 1 5D5EE323
P 1050 3700
F 0 "#PWR03" H 1050 3450 50  0001 C CNN
F 1 "GND" H 1055 3527 50  0000 C CNN
F 2 "" H 1050 3700 50  0001 C CNN
F 3 "" H 1050 3700 50  0001 C CNN
	1    1050 3700
	1    0    0    -1  
$EndComp
Text GLabel 1050 3450 1    50   Input ~ 0
VM
Wire Wire Line
	1050 3450 1050 3500
Connection ~ 1050 3500
Text GLabel 2100 3600 0    50   Input ~ 0
VDRAIN
Wire Wire Line
	2100 3600 2200 3600
Text GLabel 2100 3750 0    50   Input ~ 0
GHA
Text GLabel 2100 3900 0    50   Input ~ 0
SHA
Text GLabel 2100 4050 0    50   Input ~ 0
GLA
Text GLabel 2100 4200 0    50   Input ~ 0
SPA
Text GLabel 2100 4350 0    50   Input ~ 0
SNA
Text GLabel 2100 4500 0    50   Input ~ 0
SNB
Text GLabel 2100 4650 0    50   Input ~ 0
SPB
Text GLabel 2100 4800 0    50   Input ~ 0
GLB
Text GLabel 2100 4950 0    50   Input ~ 0
SHB
Text GLabel 2100 5100 0    50   Input ~ 0
GHB
Text GLabel 2100 5250 0    50   Input ~ 0
GHC
Text GLabel 2100 5400 0    50   Input ~ 0
SHC
Text GLabel 2100 5550 0    50   Input ~ 0
GLC
Text GLabel 2100 5700 0    50   Input ~ 0
SPC
Text GLabel 2100 5850 0    50   Input ~ 0
SNC
Text GLabel 2100 6000 0    50   Input ~ 0
SOC
Text GLabel 2100 6150 0    50   Input ~ 0
SOB
Wire Wire Line
	2100 3750 2200 3750
Wire Wire Line
	2100 3900 2200 3900
Wire Wire Line
	2100 4050 2200 4050
Wire Wire Line
	2100 4200 2200 4200
Wire Wire Line
	2100 4350 2200 4350
Wire Wire Line
	2100 4500 2200 4500
Wire Wire Line
	2100 4650 2200 4650
Wire Wire Line
	2100 4800 2200 4800
Wire Wire Line
	2100 4950 2200 4950
Wire Wire Line
	2100 5100 2200 5100
Wire Wire Line
	2100 5250 2200 5250
Wire Wire Line
	2100 5400 2200 5400
Wire Wire Line
	2100 5550 2200 5550
Wire Wire Line
	2100 5700 2200 5700
Wire Wire Line
	2100 5850 2200 5850
Text GLabel 3300 2700 2    50   Input ~ 0
SOA
$Comp
L Device:C_Small C6
U 1 1 5D61A5CA
P 3600 2900
F 0 "C6" H 3692 2946 50  0000 L CNN
F 1 "1uF" H 3692 2855 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric" H 3600 2900 50  0001 C CNN
F 3 "~" H 3600 2900 50  0001 C CNN
	1    3600 2900
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR06
U 1 1 5D62004B
P 3600 3000
F 0 "#PWR06" H 3600 2750 50  0001 C CNN
F 1 "GND" H 3605 2827 50  0000 C CNN
F 2 "" H 3600 3000 50  0001 C CNN
F 3 "" H 3600 3000 50  0001 C CNN
	1    3600 3000
	1    0    0    -1  
$EndComp
$Comp
L power:VCC #PWR05
U 1 1 5D62009C
P 3600 2800
F 0 "#PWR05" H 3600 2650 50  0001 C CNN
F 1 "VCC" H 3617 2973 50  0000 C CNN
F 2 "" H 3600 2800 50  0001 C CNN
F 3 "" H 3600 2800 50  0001 C CNN
	1    3600 2800
	1    0    0    -1  
$EndComp
Wire Wire Line
	3200 2850 3500 2850
Wire Wire Line
	3500 2850 3500 2800
Wire Wire Line
	3500 2800 3600 2800
Connection ~ 3600 2800
Wire Wire Line
	3200 3000 3600 3000
Connection ~ 3600 3000
$Comp
L Device:R_Small R4
U 1 1 5D6345C4
P 3900 3000
F 0 "R4" H 3959 3046 50  0000 L CNN
F 1 "10k" H 3959 2955 50  0000 L CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 3900 3000 50  0001 C CNN
F 3 "~" H 3900 3000 50  0001 C CNN
	1    3900 3000
	1    0    0    -1  
$EndComp
Wire Wire Line
	3900 3150 3900 3100
Wire Wire Line
	3900 2900 3900 2850
$Comp
L power:VCC #PWR08
U 1 1 5D63A9DD
P 3900 2850
F 0 "#PWR08" H 3900 2700 50  0001 C CNN
F 1 "VCC" H 3917 3023 50  0000 C CNN
F 2 "" H 3900 2850 50  0001 C CNN
F 3 "" H 3900 2850 50  0001 C CNN
	1    3900 2850
	1    0    0    -1  
$EndComp
Text GLabel 4000 3150 2    50   Input ~ 0
nFAULT
Wire Wire Line
	3900 3150 4000 3150
Wire Wire Line
	3200 3150 3900 3150
Connection ~ 3900 3150
Wire Wire Line
	3200 3300 4400 3300
Wire Wire Line
	4400 3300 4400 3250
$Comp
L Device:R_Small R5
U 1 1 5D650BE1
P 4400 3150
F 0 "R5" H 4459 3196 50  0000 L CNN
F 1 "10k" H 4459 3105 50  0000 L CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 4400 3150 50  0001 C CNN
F 3 "~" H 4400 3150 50  0001 C CNN
	1    4400 3150
	1    0    0    -1  
$EndComp
Wire Wire Line
	4400 3050 4400 3000
$Comp
L power:VCC #PWR010
U 1 1 5D654102
P 4400 3000
F 0 "#PWR010" H 4400 2850 50  0001 C CNN
F 1 "VCC" H 4417 3173 50  0000 C CNN
F 2 "" H 4400 3000 50  0001 C CNN
F 3 "" H 4400 3000 50  0001 C CNN
	1    4400 3000
	1    0    0    -1  
$EndComp
Text GLabel 4500 3300 2    50   Input ~ 0
SDO
Wire Wire Line
	4400 3300 4500 3300
Connection ~ 4400 3300
Text GLabel 3300 3450 2    50   Input ~ 0
SDI
Text GLabel 3300 3600 2    50   Input ~ 0
SCLK
Text GLabel 3300 3750 2    50   Input ~ 0
nSCS
Text GLabel 3300 3900 2    50   Input ~ 0
ENABLE
Text GLabel 3300 4050 2    50   Input ~ 0
CAL
$Comp
L Device:C_Small C4
U 1 1 5D657DC2
P 3400 4250
F 0 "C4" H 3492 4296 50  0000 L CNN
F 1 "1uF" H 3492 4205 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric" H 3400 4250 50  0001 C CNN
F 3 "~" H 3400 4250 50  0001 C CNN
	1    3400 4250
	1    0    0    -1  
$EndComp
Wire Wire Line
	3200 4200 3300 4200
Wire Wire Line
	3300 4200 3300 4150
Wire Wire Line
	3300 4150 3400 4150
Wire Wire Line
	3200 4350 3400 4350
Wire Wire Line
	3400 4150 3900 4150
Connection ~ 3400 4150
$Comp
L power:GND #PWR09
U 1 1 5D662CD6
P 3900 4150
F 0 "#PWR09" H 3900 3900 50  0001 C CNN
F 1 "GND" H 3905 3977 50  0000 C CNN
F 2 "" H 3900 4150 50  0001 C CNN
F 3 "" H 3900 4150 50  0001 C CNN
	1    3900 4150
	1    0    0    -1  
$EndComp
Text GLabel 3450 4400 2    50   Input ~ 0
DVDD
Wire Wire Line
	3400 4350 3400 4400
Wire Wire Line
	3400 4400 3450 4400
Connection ~ 3400 4350
Text GLabel 3300 4500 2    50   Input ~ 0
INHA
Text GLabel 3300 4650 2    50   Input ~ 0
INLA
Text GLabel 3300 4800 2    50   Input ~ 0
INHB
Text GLabel 3300 4950 2    50   Input ~ 0
INLB
Text GLabel 3300 5100 2    50   Input ~ 0
INHC
Text GLabel 3300 5250 2    50   Input ~ 0
INLC
$Comp
L Device:C_Small C5
U 1 1 5D666E40
P 3450 5600
F 0 "C5" H 3542 5646 50  0000 L CNN
F 1 "0.1uF" H 3542 5555 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric" H 3450 5600 50  0001 C CNN
F 3 "~" H 3450 5600 50  0001 C CNN
	1    3450 5600
	1    0    0    -1  
$EndComp
Wire Wire Line
	3200 5550 3300 5550
Wire Wire Line
	3300 5550 3300 5500
Wire Wire Line
	3300 5500 3450 5500
Wire Wire Line
	3200 5700 3450 5700
Wire Wire Line
	3200 5400 3950 5400
$Comp
L Device:D_Schottky D1
U 1 1 5D673643
P 3950 5550
F 0 "D1" V 3996 5471 50  0000 R CNN
F 1 "D_Schottky" V 3905 5471 50  0000 R CNN
F 2 "Diode_SMD:D_SOD-123F" H 3950 5550 50  0001 C CNN
F 3 "~" H 3950 5550 50  0001 C CNN
	1    3950 5550
	0    -1   -1   0   
$EndComp
Connection ~ 3950 5400
$Comp
L power:GND #PWR013
U 1 1 5D677E15
P 4750 5450
F 0 "#PWR013" H 4750 5200 50  0001 C CNN
F 1 "GND" H 4755 5277 50  0000 C CNN
F 2 "" H 4750 5450 50  0001 C CNN
F 3 "" H 4750 5450 50  0001 C CNN
	1    4750 5450
	1    0    0    -1  
$EndComp
Wire Wire Line
	3450 5700 3950 5700
Connection ~ 3450 5700
$Comp
L Device:L 27uH1
U 1 1 5D680E8A
P 4250 5700
F 0 "27uH1" V 4100 5700 50  0000 C CNN
F 1 "L" V 4200 5700 50  0000 C CNN
F 2 "Inductor_SMD:L_1210_3225Metric" H 4250 5700 50  0001 C CNN
F 3 "~" H 4250 5700 50  0001 C CNN
	1    4250 5700
	0    1    -1   0   
$EndComp
Wire Wire Line
	3950 5700 4100 5700
Connection ~ 3950 5700
Wire Wire Line
	4400 5700 4550 5700
Wire Wire Line
	4550 5700 4550 5600
Wire Wire Line
	4750 5400 4750 5450
Wire Wire Line
	3950 5400 4750 5400
$Comp
L power:VCC #PWR011
U 1 1 5D6A55A9
P 4550 5600
F 0 "#PWR011" H 4550 5450 50  0001 C CNN
F 1 "VCC" H 4550 5750 50  0000 C CNN
F 2 "" H 4550 5600 50  0001 C CNN
F 3 "" H 4550 5600 50  0001 C CNN
	1    4550 5600
	1    0    0    -1  
$EndComp
$Comp
L Device:C_Small C8
U 1 1 5D6A56EE
P 4550 5800
F 0 "C8" H 4642 5846 50  0000 L CNN
F 1 "10uF" H 4642 5755 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric" H 4550 5800 50  0001 C CNN
F 3 "~" H 4550 5800 50  0001 C CNN
	1    4550 5800
	1    0    0    -1  
$EndComp
Connection ~ 4550 5700
$Comp
L power:GND #PWR012
U 1 1 5D6A5748
P 4550 5900
F 0 "#PWR012" H 4550 5650 50  0001 C CNN
F 1 "GND" H 4555 5727 50  0000 C CNN
F 2 "" H 4550 5900 50  0001 C CNN
F 3 "" H 4550 5900 50  0001 C CNN
	1    4550 5900
	1    0    0    -1  
$EndComp
$Comp
L Device:R_Small R3
U 1 1 5D6A5906
P 3450 6150
F 0 "R3" V 3550 6150 50  0000 C CNN
F 1 "100k" V 3650 6150 50  0000 C CNN
F 2 "Resistor_SMD:R_0603_1608Metric_Pad1.05x0.95mm_HandSolder" H 3450 6150 50  0001 C CNN
F 3 "~" H 3450 6150 50  0001 C CNN
	1    3450 6150
	0    1    1    0   
$EndComp
$Comp
L Device:C_Small C7
U 1 1 5D6A5B71
P 3700 6250
F 0 "C7" H 3792 6296 50  0000 L CNN
F 1 "10uF" H 3792 6205 50  0000 L CNN
F 2 "Capacitor_SMD:C_0603_1608Metric" H 3700 6250 50  0001 C CNN
F 3 "~" H 3700 6250 50  0001 C CNN
	1    3700 6250
	1    0    0    -1  
$EndComp
Wire Wire Line
	3200 6150 3350 6150
Wire Wire Line
	3550 6150 3700 6150
$Comp
L power:GND #PWR07
U 1 1 5D6AF5C7
P 3700 6350
F 0 "#PWR07" H 3700 6100 50  0001 C CNN
F 1 "GND" H 3705 6177 50  0000 C CNN
F 2 "" H 3700 6350 50  0001 C CNN
F 3 "" H 3700 6350 50  0001 C CNN
	1    3700 6350
	1    0    0    -1  
$EndComp
Wire Wire Line
	3200 6000 3700 6000
Wire Wire Line
	3700 6000 3700 6150
Connection ~ 3700 6150
Wire Wire Line
	3700 6000 3700 5950
Connection ~ 3700 6000
Text GLabel 3700 5950 1    50   Input ~ 0
VM
Wire Wire Line
	3200 5250 3300 5250
Wire Wire Line
	3200 5100 3300 5100
Wire Wire Line
	3200 4950 3300 4950
Wire Wire Line
	3200 4800 3300 4800
Wire Wire Line
	3200 4650 3300 4650
Wire Wire Line
	3200 4500 3300 4500
Wire Wire Line
	3200 4050 3300 4050
Wire Wire Line
	3200 3900 3300 3900
Wire Wire Line
	3200 3750 3300 3750
Wire Wire Line
	3200 3600 3300 3600
Wire Wire Line
	3200 3450 3300 3450
Wire Wire Line
	3200 2700 3300 2700
Wire Wire Line
	1450 2950 1450 2850
Wire Wire Line
	1450 2850 2200 2850
Wire Wire Line
	2100 6000 2200 6000
Wire Wire Line
	2100 6150 2200 6150
Text GLabel 6650 2750 0    50   Input ~ 0
VDRAIN
Wire Wire Line
	6650 2750 6800 2750
Wire Wire Line
	5600 2600 5600 3150
Wire Wire Line
	8000 2600 8000 3150
$EndSCHEMATC
