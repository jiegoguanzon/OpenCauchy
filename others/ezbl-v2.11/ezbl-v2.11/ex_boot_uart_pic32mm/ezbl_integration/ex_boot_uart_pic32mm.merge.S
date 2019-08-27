/**
 * EZBL Bootloader Code and RAM Allocation
 *
 * Automatically generated file - not intended for manual editing. If changes 
 * are made here, they will normally be overwritten when you rebuild your 
 * Bootloader. If necessary, maintain a backup copy and manually merge your 
 * customizations back in.
 * 
 * Built for:
 *     PIC32MM0256GPM064
 * From:
 *     dist\uart\production\ex_boot_uart_pic32mm.production.elf
 * Using build configuration:
 *     uart
 * 
 * Tool paths:
 *     C:\Program Files (x86)\Microchip\xc32\vDefault
 *     C:\Program Files (x86)\Microchip\MPLABX\vDefault
 */

 # Validate matching target processors between Bootloader and Application projects.
 # If you get this error and wish to force the two dissimilar targets together anyway,
 # you can comment out these .error statements. However, something will likely be
 # broken at run time, so do so only if you are sure of what you are doing.
#if !defined(__32MM0256GPM064__)
    .error "Bootloader's ex_boot_uart_pic32mm.merge.S file was generated for a different target processor."
    .error "Recompile this Application project or the ex_boot_uart_pic32mm Bootloader project for the same hardware."
#endif


#----Target Bootloader ID and this Application Version meta data for .bl2 file header----
    .pushsection    .info.EZBL_metaParameters, info, keep
    .weak   _BOOTID_HASH0
    .weak   _BOOTID_HASH1
    .weak   _BOOTID_HASH2
    .weak   _BOOTID_HASH3
    .weak   _APPID_VER_BUILD
    .weak   _APPID_VER_MINOR
    .weak   _APPID_VER_MAJOR
_BOOTID_HASH0 = 0x7963B2E2
_BOOTID_HASH1 = 0x2DE540ED
_BOOTID_HASH2 = 0xEC4A3773
_BOOTID_HASH3 = 0xC02F3D41
EZBL_metaAppIDVerBuild:
    .long   _APPID_VER_BUILD
EZBL_metaAppIDVerMinor:
    .short  _APPID_VER_MINOR
EZBL_metaAppIDVerMajor:
    .short  _APPID_VER_MAJOR
    .popsection

#if !defined(EZBL_HIDE_BOOT_SYMBOLS)
    # Bootloader RAM block intended for 'kseg0_data_mem' region
    # 0x80000200 to 0x80000344, length 0x0144 (324 bytes)
    .pushsection    EZBL_RAM_AT_0x80000200, address(0x80000200), persist, keep
    .space      0x144
    .popsection
    # Bootloader RAM block intended for 'kseg0_data_mem' region
    # 0x80000348 to 0x80000358, length 0x0010 (16 bytes)
    .pushsection    EZBL_RAM_AT_0x80000348, address(0x80000348), persist, keep
    .space      0x10
    .popsection
#endif

#----Bootloader reserved flash space propagated from the bootloader project----
    .global EZBL_BOOTLOADER_SIZE
EZBL_BOOTLOADER_SIZE = 0x00002000



#----App erasable items that the Bootloader knows about----
    .pushsection    .EZBL_appBootloadState, address(0x9D002000), code, keep
    # EZBL_appBootloadState - Base address of EZBL_INSTALLED_FLAGS structure (24 bytes/6 instruction words)
    .global EZBL_appBootloadState
    .global EZBL_appIDVer
    .type   EZBL_appBootloadState, @object
    .type   EZBL_appIDVer, @object
EZBL_appBootloadState:
EZBL_appIDVer:
    .long   _APPID_VER_BUILD    # EZBL_appIDVer.appIDVerBuild
    .short  _APPID_VER_MINOR    # EZBL_appIDVer.appIDVerMinor
    .short  _APPID_VER_MAJOR    # EZBL_appIDVer.appIDVerMajor
    .size   EZBL_appIDVer, . - EZBL_appIDVer
    .long   0xFFFFFFFF          # hash32 - not defined at compile time, set when run-time programmed
    .long   0xFFFFFFFF          # CRC32  - not defined at compile time, set when run-time programmed
    .long   0xFFFF12CD          # appInstalled == EZBL_APP_INSTALLED (0x12CD)
    .long   0xFFFFFFFF          # padding to fit 3x flash double words
    .size   EZBL_appBootloadState, . - EZBL_appBootloadState
    .popsection



#----Bootloader reserved program memory----

    # Bootloader code block intended for program region 'kseg0_program_mem'
    # 0x9D000000 to 0x9D002000, length 0x002000 (8192 bytes; needs 3 pages)
    .pushsection    EZBL_ROM_AT_0x9D000000, address(0x9D000000), code, keep

    .ent        NOW_CORETMR_ResetConst
    .type       NOW_CORETMR_ResetConst, @function
NOW_CORETMR_ResetConst:         # 0x9D000000
    .set        micromips
    .insn
    .word       0x00FC004D
    .word       0xDECC0040
    .word       0x02FC004D
    .word       0x004BED7F
    .word       0xD68002FC
    .word       0x0C000D16
    .word       0x00000035
    .size       NOW_CORETMR_ResetConst, . - NOW_CORETMR_ResetConst   # 28
    .end        NOW_CORETMR_ResetConst

    .ent        __vector_dispatch_53
    .type       __vector_dispatch_53, @function
__vector_dispatch_53:           # 0x9D00001C
    .set        micromips
    .insn
    .word       0x08ABD680
    .word       0x0C000C00
    .word       0x00000036
    .size       __vector_dispatch_53, . - __vector_dispatch_53   # 12
    .end        __vector_dispatch_53

    .ent        __vector_dispatch_54
    .type       __vector_dispatch_54, @function
__vector_dispatch_54:           # 0x9D000028
    .set        micromips
    .insn
    .word       0x08ABD680
    .word       0x0C000C00
    .size       __vector_dispatch_54, . - __vector_dispatch_54   # 8
    .end        __vector_dispatch_54

    .ent        EZBL_ErasePage
    .type       EZBL_ErasePage, @function
EZBL_ErasePage:                 # 0x9D000030
    .set        micromips
    .insn
    .word       0xF80833BD
    .word       0xD7D0233D
    .word       0x0E844FD5
    .word       0x1FFF41B2
    .word       0xF8005252
    .word       0x92500244
    .word       0xA00041B6
    .word       0xB29002D2
    .word       0x8028FC5C
    .word       0x12343042
    .word       0x8028F85C
    .word       0x305D6C8D
    .word       0xF85D0818
    .word       0x0E110818
    .word       0x08003260
    .word       0x41BE0EA0
    .word       0x33DE9D00
    .word       0x41B703D0
    .word       0x32F70000
    .word       0x02360020
    .word       0x41A839D0
    .word       0x31209D00
    .word       0x0511FFFF
    .word       0x115002C2
    .word       0xE9006920
    .word       0x0CB70C9E
    .word       0x3048070E
    .word       0xF8FD1C3F
    .word       0xF91D081C
    .word       0x45C20820
    .word       0x0824F93D
    .word       0x081CFCFD
    .word       0x0820FD1D
    .word       0xFD3DAD07
    .word       0x4E780824
    .word       0x0000F930
    .word       0x123432A0
    .word       0xFC5D6C02
    .word       0xB4500818
    .word       0x0511FFDF
    .word       0x12343040
    .word       0x002AB455
    .word       0xFC5C0C00
    .word       0x30428028
    .word       0xF85CEDCC
    .word       0x869A8028
    .word       0x0A0F7680
    .word       0x0E020C00
    .word       0x8028FCDC
    .word       0x0C92C804
    .word       0x41A2EE84
    .word       0x3042BFC0
    .word       0x45E20031
    .word       0x02700CE0
    .word       0x94139A90
    .word       0x30C0000A
    .word       0x50400800
    .word       0xC844DEAD
    .word       0x869AC805
    .word       0x0EDC7680
    .word       0xF81C0CE0
    .word       0xCC058028
    .word       0x08003054
    .word       0x8028F81C
    .word       0x4C2DED7F
    .word       0x57D0233D
    .word       0x33BD459F
    .word       0xFFFF07F8
    .word       0x9D0041BA
    .word       0x1BA5335A
    .word       0x0C00459A
    .size       EZBL_ErasePage, . - EZBL_ErasePage   # 284
    .end        EZBL_ErasePage

    .ent        EZBL_FIFOReset
    .type       EZBL_FIFOReset, @function
EZBL_FIFOReset:                 # 0x9D00014C
    .set        micromips
    .insn
    .word       0xEB43E840
    .word       0xEAC1EAC4
    .word       0xEBC5EAC2
    .word       0xE9464844
    .word       0xE847459F
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .size       EZBL_FIFOReset, . - EZBL_FIFOReset   # 28
    .end        EZBL_FIFOReset

    .ent        LEDOff
    .type       LEDOff, @function
LEDOff:                         # 0x9D000168
    .set        micromips
    .insn
    .word       0xCBE54FF5
    .word       0x9D0041A5
    .word       0x0BF1F680
    .word       0x1A4030A5
    .word       0x4BE54412
    .word       0x4C0D459F
    .word       0x9D0041BA
    .word       0x1757335A
    .word       0x0C00459A
    .size       LEDOff, . - LEDOff   # 36
    .end        LEDOff

    .ent        EZBL_BootloaderTask
    .type       EZBL_BootloaderTask, @function
EZBL_BootloaderTask:            # 0x9D00018C
    .set        micromips
    .insn
    .word       0x45444FF5
    .word       0x9D0041A2
    .word       0x1A60FE02
    .word       0x001140D0
    .word       0x801CFC5C
    .word       0x800041A2
    .word       0x0200FC82
    .word       0xD0426949
    .word       0x8D070060
    .word       0x801CFC5C
    .word       0x083C7680
    .word       0xFC5C0CB0
    .word       0x8D08801C
    .word       0x800041A4
    .word       0x04B8F680
    .word       0x02083084
    .word       0x0C40CC7F
    .word       0x08FC006F
    .word       0x9D0041A2
    .word       0x00003042
    .word       0x0076B443
    .word       0x41A20C40
    .word       0xFC428000
    .word       0x69290200
    .word       0x000C40E2
    .word       0x0E9F7680
    .word       0x41A30C00
    .word       0xF8438000
    .word       0x41A2021C
    .word       0xFC428000
    .word       0xE8290200
    .word       0x0E9F7680
    .word       0x41A30C00
    .word       0x30638000
    .word       0x6A350208
    .word       0x69B40545
    .word       0x23900043
    .word       0x00549404
    .word       0x40D00C40
    .word       0x41A2000B
    .word       0x41A29D00
    .word       0xFC828000
    .word       0x76800200
    .word       0x0CB0083C
    .word       0x9D0041A2
    .word       0x20003042
    .word       0x2D2F2928
    .word       0x12CD3060
    .word       0x001E9462
    .word       0xB2CD5060
    .word       0x003AB462
    .word       0x41A20C40
    .word       0x30429D00
    .word       0x69A02800
    .word       0xB443ED7F
    .word       0x0C400011
    .word       0x0C00CC2F
    .word       0x002CB462
    .word       0x41A20C40
    .word       0x30429D00
    .word       0x69A02800
    .word       0xB443ED7F
    .word       0x0C000013
    .word       0x0C40CC21
    .word       0x00B4F680
    .word       0x00FF3080
    .word       0x9D0041A2
    .word       0x20003042
    .word       0x2D2F2928
    .word       0x12CD3060
    .word       0xFFE4B462
    .word       0xB2CD5060
    .word       0x477C0002
    .word       0x18000000
    .word       0x9D0041A2
    .word       0x20003042
    .word       0x0AFC004F
    .word       0x9D0041A2
    .word       0x28013042
    .word       0x0C0045E2
    .word       0x459F4504
    .word       0x0C004C0D
    .size       EZBL_BootloaderTask, . - EZBL_BootloaderTask   # 328
    .end        EZBL_BootloaderTask

    .ent        UART1_TX_FIFO_OnWrite
    .type       UART1_TX_FIFO_OnWrite, @function
UART1_TX_FIFO_OnWrite:          # 0x9D0002D4
    .set        micromips
    .insn
    .word       0x231D4FE5
    .word       0x0E24D014
    .word       0x0E460EC5
    .word       0x41A20E07
    .word       0x3042BF80
    .word       0x69A41800
    .word       0x0200D063
    .word       0x0EE2AD87
    .word       0x09627680
    .word       0x76800C87
    .word       0x0C900971
    .word       0x003D9651
    .word       0x800041A2
    .word       0x0E9FF680
    .word       0x800041B4
    .word       0xFE700EA2
    .word       0xE8050014
    .word       0x02360C90
    .word       0xF6802950
    .word       0x02320B69
    .word       0x04A231D0
    .word       0x0010FC57
    .word       0x0200D042
    .word       0x000E40A2
    .word       0x09717680
    .word       0x76800C90
    .word       0x0C900962
    .word       0x095D7680
    .word       0x40E20C90
    .word       0x76800003
    .word       0x0C000A9B
    .word       0x0004B651
    .word       0xCC110C00
    .word       0x0014FA70
    .word       0x0E9F7680
    .word       0x02A20C00
    .word       0xFC7411D0
    .word       0x25B50248
    .word       0x13900062
    .word       0x0C90AD54
    .word       0xFA70CC02
    .word       0x41A20014
    .word       0x30428000
    .word       0x346202F8
    .word       0xEE010024
    .word       0x108C0064
    .word       0x00243862
    .word       0x231D0C51
    .word       0x459F5014
    .short      0x4C1D
    .size       UART1_TX_FIFO_OnWrite, . - UART1_TX_FIFO_OnWrite   # 198
    .end        UART1_TX_FIFO_OnWrite

    .ent        LEDToggle
    .type       LEDToggle, @function
LEDToggle:                      # 0x9D00039A
    .set        micromips
    .insn
    .short      0x41A5
    .word       0xD6809D00
    .word       0x30A50BF9
    .word       0xFFFF1A40
    .word       0x8010FF5C
    .word       0x0C0045BA
    .word       0x8014FF5C
    .word       0x0C0045BA
    .word       0x9D002000
    .word       0x9D040000
    .word       0xBFC01740
    .word       0xBFC017C0
    .word       0xBFC017E8
    .word       0xBFC01800
    .word       0x9D000000
    .word       0x9D002000
    .word       0xBFC00000
    .word       0xBFC01740
    .word       0xBFC017C8
    .word       0xBFC017D0
    .word       0xBFC017E0
    .word       0xBFC017E8
    .word       0x9D000000
    .word       0x9D002018
    .word       0xBFC00000
    .word       0xBFC01740
    .word       0xBFC017C0
    .word       0xBFC017E8
    .word       0x9D002000
    .word       0x9D002018
    .word       0xBFC017C8
    .word       0xBFC017D0
    .word       0xBFC017E0
    .word       0xBFC017E8
    .word       0xBFC017C0
    .word       0x00000020
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFF0
    .word       0xFFFFFFFA
    .word       0xFFFF698A
    .word       0xFFFFFDF9
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .size       LEDToggle, . - LEDToggle   # 174
    .end        LEDToggle

    .ent        UART1_ResetBRG
    .type       UART1_ResetBRG, @function
UART1_ResetBRG:                 # 0x9D000448
    .set        micromips
    .insn
    .word       0x231D4FE1
    .word       0x0E84D01C
    .word       0x3260ED01
    .word       0x90640008
    .word       0x02630001
    .word       0x0262294C
    .word       0x02627BCC
    .word       0x0E40840C
    .word       0xBF8041A4
    .word       0x35C03084
    .word       0x0048FC64
    .word       0x000C0062
    .word       0x0048F864
    .word       0xBF8041A5
    .word       0x18003065
    .word       0xBF8041A2
    .word       0x19003042
    .word       0x0EE50EC4
    .word       0x0CA20EA3
    .word       0x800041B0
    .word       0x00059443
    .word       0x02D03210
    .word       0xCC03EE17
    .word       0xEE28EA08
    .word       0x2EDFEA08
    .word       0x19003080
    .word       0x000BB485
    .word       0x800041B1
    .word       0x0003B443
    .word       0xCC05ED38
    .word       0xED35E908
    .word       0x41B1E908
    .word       0x32318000
    .word       0x690802F8
    .word       0xE9186D20
    .word       0x0C91C804
    .word       0x800041A5
    .word       0x02B030A5
    .word       0x41A7EF20
    .word       0xF6809D00
    .word       0x30E700A6
    .word       0x41A202D5
    .word       0x30429D00
    .word       0xC84414AB
    .word       0x41A50C90
    .word       0x30A58000
    .word       0xEF600250
    .word       0x00A67680
    .word       0x41A20CE0
    .word       0x30429D00
    .word       0xE91715C3
    .word       0x0044FC76
    .word       0x0062ED01
    .word       0xF876000C
    .word       0x50600044
    .word       0xE9898000
    .word       0x0280E999
    .word       0x909419D0
    .word       0x00830000
    .word       0xFA95A018
    .word       0xFA770040
    .word       0x02421800
    .word       0x0242528C
    .word       0x0242630C
    .word       0xFA557BCC
    .word       0x0C900010
    .word       0x09577680
    .word       0x0C91EE81
    .word       0x09577680
    .word       0x7680EE81
    .word       0x0C900971
    .word       0x231D0C91
    .word       0xD680501C
    .word       0x4C210971
    .size       UART1_ResetBRG, . - UART1_ResetBRG   # 296
    .end        UART1_ResetBRG

    .ent        InitializeBoard
    .type       InitializeBoard, @function
InitializeBoard:                # 0x9D000570
    .set        micromips
    .insn
    .word       0xCBE54FF5
    .word       0x577C0002
    .word       0x016E41A3
    .word       0x36003063
    .word       0x800041A2
    .word       0x0240F862
    .word       0x00B741A3
    .word       0x1B003063
    .word       0x800041A2
    .word       0x0248F862
    .word       0xBB805060
    .word       0x800041A2
    .word       0x0244F862
    .word       0x41A2EDB0
    .word       0xF6808000
    .word       0x38620000
    .word       0x7680024C
    .word       0x0C8003FA
    .word       0xFFFF5080
    .word       0x9D0041A5
    .word       0x1A4030A5
    .word       0xBF8041A2
    .word       0x2BE03042
    .word       0xBF8041A6
    .word       0x2BC030C6
    .word       0x0BEF7680
    .word       0x3060072D
    .word       0x41A24000
    .word       0xF862BF80
    .word       0xED402CB4
    .word       0xBF8041A3
    .word       0x2C08F843
    .word       0xBF8041A3
    .word       0x2BB4F843
    .word       0x800041A2
    .word       0x0244FC62
    .word       0x25B82534
    .word       0x26260534
    .word       0x0E8E7680
    .word       0xF6800629
    .word       0x30800224
    .word       0x41A2FFE7
    .word       0x30428000
    .word       0xF85C02F8
    .word       0x41A2802C
    .word       0x30428000
    .word       0xF85C02D0
    .word       0x41A38030
    .word       0xF8438000
    .word       0x50600200
    .word       0x41A2E020
    .word       0xF862BF80
    .word       0x30402BE4
    .word       0x41A44400
    .word       0xF844BF80
    .word       0x41A42DE4
    .word       0xF864BF80
    .word       0x41A32BC4
    .word       0xF843BF80
    .word       0x30602DC4
    .word       0x41A22000
    .word       0xF862BF80
    .word       0x30602BB4
    .word       0x41A20100
    .word       0xF862BF80
    .word       0x30602DB8
    .word       0x41A20800
    .word       0xF862BF80
    .word       0x41A22BB8
    .word       0x3042016E
    .word       0x4BE53600
    .word       0x4C0D459F
    .size       InitializeBoard, . - InitializeBoard   # 288
    .end        InitializeBoard

    .ent        UART1_RXInterruptFunc
    .type       UART1_RXInterruptFunc, @function
UART1_RXInterruptFunc:          # 0x9D000690
    .set        micromips
    .insn
    .word       0x45444FF5
    .word       0x800041B0
    .word       0x095FF680
    .word       0x02D03090
    .word       0xBF8041A4
    .word       0x1800FC44
    .word       0x0CB02D20
    .word       0x18003084
    .word       0x3070AD11
    .word       0x310002D0
    .word       0x41A90002
    .word       0x31299D00
    .word       0x31401DC4
    .word       0x31600001
    .word       0x41AC0008
    .word       0xCC418000
    .word       0x02F8318C
    .word       0x00803040
    .word       0x3443E941
    .word       0xEE010024
    .word       0x7BCC0044
    .word       0x00243843
    .word       0x0C00CC60
    .word       0x6B446BCC
    .word       0x0004D1A6
    .word       0x003340AD
    .word       0x00025042
    .word       0x0008D1C6
    .word       0x004051A2
    .word       0x8F062F62
    .word       0x101801CD
    .word       0x0014F904
    .word       0x00105042
    .word       0x802414DC
    .word       0x01262F65
    .word       0x1DCD6950
    .word       0x01A70000
    .word       0xB5AE2B3C
    .word       0x6F60001D
    .word       0x0008B566
    .word       0x802418DC
    .word       0x00805042
    .word       0x801CF87C
    .word       0x8020F99C
    .word       0x02D0FDA5
    .word       0x00CD6B33
    .word       0x40A63390
    .word       0x5042000E
    .word       0x6B448010
    .word       0x40A62F61
    .word       0xCC24FFC9
    .word       0xCFCE0C00
    .word       0x00225042
    .word       0x195CCFED
    .word       0x6B318024
    .word       0x000131A6
    .word       0x0004F9A3
    .word       0x6B348BE0
    .word       0x07FC6BB3
    .word       0x0004FDA3
    .word       0x3B9000ED
    .word       0x000140A7
    .word       0x0006EB31
    .word       0x0000477C
    .word       0xFCC51800
    .word       0x6F6002D0
    .word       0x02D0F8C5
    .word       0x577C0006
    .word       0x0C00CFD8
    .word       0x44D46A39
    .word       0x4504E939
    .word       0x4C0D459F
    .word       0x000041A4
    .word       0x30844FF5
    .word       0x8E040000
    .word       0x45C4CBE5
    .word       0x00000000
    .word       0x0000D084
    .word       0x0000D0A5
    .word       0x9D0041A8
    .word       0x18C13108
    .word       0x000045C8
    .word       0x41A20000
    .word       0x30420000
    .word       0x94020000
    .word       0x0C000004
    .word       0x000045C2
    .word       0xCFF50000
    .word       0x0C000C00
    .size       UART1_RXInterruptFunc, . - UART1_RXInterruptFunc   # 356
    .end        UART1_RXInterruptFunc

    .ent        LEDSet
    .type       LEDSet, @function
LEDSet:                         # 0x9D0007F4
    .set        micromips
    .insn
    .word       0x9D0041A5
    .word       0x0BEDD680
    .word       0x1A4030A5
    .word       0x0404F680
    .word       0x00000000
    .word       0x00FC034C
    .word       0x04EC035A
    .word       0x0007941A
    .word       0x41BA0C00
    .word       0x335A9D00
    .word       0x459A1D7D
    .word       0x41BD0C00
    .word       0x33BD8000
    .word       0x41BC7FF0
    .word       0x339C8001
    .word       0x012C8310
    .word       0x000910FC
    .word       0x01495910
    .word       0x012A1EAC
    .word       0x012C498C
    .word       0x000012FC
    .word       0x039C1800
    .word       0x016CF17C
    .word       0x000012FC
    .word       0x41A81800
    .word       0x31089D00
    .word       0x45C81DD9
    .word       0x00000000
    .word       0x800041A8
    .word       0x03283108
    .word       0x800041A9
    .word       0x03443129
    .word       0x0C00CC0B
    .word       0x0000F808
    .word       0x0004F808
    .word       0x0008F808
    .word       0x000CF808
    .word       0x00103108
    .word       0x0B900128
    .word       0xFFF2B401
    .word       0x41A80C00
    .word       0x31089D00
    .word       0x45C8184D
    .word       0x00000000
    .word       0x000041A9
    .word       0x00003129
    .word       0x00019409
    .word       0x00090C00
    .word       0x314002FC
    .word       0x014BFFFF
    .word       0x41A802FC
    .word       0x014C0040
    .word       0x014800FC
    .word       0x014C5290
    .word       0x41A902FC
    .word       0x31299D00
    .word       0x00000000
    .word       0x012F1800
    .word       0x41A90AFC
    .word       0x31290000
    .word       0x41A80001
    .word       0x3108BF81
    .word       0xFD48F000
    .word       0x31400000
    .word       0x01490000
    .word       0x514AB40C
    .word       0xF9481000
    .word       0x31400000
    .word       0x01490000
    .word       0x014C494C
    .word       0x41A90AFC
    .word       0x012D0080
    .word       0x011002FC
    .word       0x012800FC
    .word       0x012905AC
    .word       0x010C8800
    .word       0x41A100FC
    .word       0x00280058
    .word       0x01094250
    .word       0x010C4290
    .word       0x000002FC
    .word       0x41A81800
    .word       0x31089D00
    .word       0x45C81DDD
    .word       0x00000000
    .word       0x00FC010C
    .word       0xFFBF41A1
    .word       0xFFFF5021
    .word       0x42500028
    .word       0x02FC010C
    .word       0x0000D084
    .word       0x0000D0A5
    .word       0x9D0041A8
    .word       0x07B13108
    .word       0x0C004588
    .size       LEDSet, . - LEDSet   # 380
    .end        LEDSet

    .ent        EZBL_Install2Flash
    .type       EZBL_Install2Flash, @function
EZBL_Install2Flash:             # 0x9D000970
    .set        micromips
    .insn
    .word       0x233D4FA9
    .word       0x0EA4D088
    .word       0x801CFC5C
    .word       0x0E00AD04
    .word       0x03769400
    .word       0x41B4E840
    .word       0x41B19D00
    .word       0x32319D00
    .word       0x02310971
    .word       0x026FE02C
    .word       0x41B208FC
    .word       0x32529D00
    .word       0x02530000
    .word       0xFCBC99D0
    .word       0xFEE5801C
    .word       0xFC550000
    .word       0xB0620000
    .word       0x94030009
    .word       0x3074034F
    .word       0x004309C4
    .word       0x45A21118
    .word       0x9D0009E9
    .word       0x9D000A59
    .word       0x9D000C17
    .word       0x9D000C87
    .word       0x9D000D17
    .word       0x9D000DF1
    .word       0x9D000E6F
    .word       0x9D000EF9
    .word       0x9D000F79
    .word       0x0030B2F7
    .word       0x033FB417
    .word       0x76806E0D
    .word       0xEF300EE9
    .word       0x0EC06D0D
    .word       0xEDACEE42
    .word       0xB4850AA0
    .word       0x0C00001A
    .word       0x70C60B22
    .word       0x0AA10032
    .word       0x004C70A5
    .word       0x0B2344EE
    .word       0x004270C6
    .word       0x2EDD44EE
    .word       0xFCBCAE8C
    .word       0x6B508020
    .word       0x0EE67680
    .word       0xED010C80
    .word       0x0000F855
    .word       0x32E0CC07
    .word       0x4EC20001
    .word       0xFFE0B476
    .word       0x0EE06D20
    .word       0xFCBC0C80
    .word       0x7680801C
    .word       0x0CD60EE6
    .word       0x02B49400
    .word       0x000132C0
    .word       0x0030B2F7
    .word       0x02ADB417
    .word       0x6E0D0EC0
    .word       0x0EE67680
    .word       0x0C80EF30
    .word       0x76806E8D
    .word       0xEF300E06
    .word       0x001CF855
    .word       0x30424847
    .word       0xF855FFD8
    .word       0xED010018
    .word       0x002C3855
    .word       0x9D0041B6
    .word       0x1DA83096
    .word       0x76806E91
    .word       0xEF100EAF
    .word       0x48AC8D0C
    .word       0xFFEA3040
    .word       0x002C3855
    .word       0xF855ED08
    .word       0x94000000
    .word       0x32C00287
    .word       0x488D0001
    .word       0x0008F8B5
    .word       0x000CF895
    .word       0x00003040
    .word       0x6D0D8D0D
    .word       0x9D0041A2
    .word       0x20003062
    .word       0x2F6F2B38
    .word       0x12CD3060
    .word       0x00049466
    .word       0x6D0D0C00
    .word       0x6E13CC13
    .word       0x2004FC62
    .word       0x2000FC42
    .word       0x33900064
    .word       0x000740A6
    .word       0xFFF3B483
    .word       0x00450C00
    .word       0x40E21390
    .word       0xCDA3FFEE
    .word       0xFFE93040
    .word       0x486E6AA9
    .word       0x6D2244DD
    .word       0xFFFAB482
    .word       0xB403C86E
    .word       0x30400198
    .word       0x41A2FFEB
    .word       0x30429D00
    .word       0x8D101B71
    .word       0x801CFC9C
    .word       0x0018FCD5
    .word       0x8020FCBC
    .word       0x004030C6
    .word       0x0DB8F680
    .word       0x000830F5
    .word       0x01834082
    .word       0xFFE53040
    .word       0x00279413
    .word       0x00020E13
    .word       0x0000477C
    .word       0x024F1800
    .word       0x41A20AFC
    .word       0x3042BF81
    .word       0x41A4F0C4
    .word       0x3084BF81
    .word       0xEEFFF140
    .word       0x1B900082
    .word       0x000340E3
    .word       0xCFFAEAA0
    .word       0x76806D28
    .word       0x0C800D38
    .word       0x577C0002
    .word       0x0D167680
    .word       0xF6800C00
    .word       0xFC9C0971
    .word       0xF680801C
    .word       0xFC9C0971
    .word       0x41A28020
    .word       0xF802BF80
    .word       0xFC962940
    .word       0x48671DA8
    .word       0x41A5445C
    .word       0x30A5AA99
    .word       0x445D6655
    .word       0x2940F862
    .word       0x1DA83076
    .word       0x48886B31
    .word       0x41A44474
    .word       0x50845566
    .word       0x447499AA
    .word       0x2940F8C2
    .word       0x800041A7
    .word       0x41A644F9
    .word       0xF8E6BF80
    .word       0xF8022990
    .word       0x6BB22940
    .word       0x447748C9
    .word       0xF8A2446E
    .word       0x6AB32940
    .word       0x445D486A
    .word       0xF8824463
    .word       0x50602940
    .word       0x41A28400
    .word       0xF862BF80
    .word       0xF81529A0
    .word       0xF8150020
    .word       0xED080024
    .word       0x0004F855
    .word       0xF855ED02
    .word       0xFC9C0000
    .word       0x69408020
    .word       0x5040AD08
    .word       0xF85DFF13
    .word       0x6EC10080
    .word       0x0B697680
    .word       0x8C07EF02
    .word       0x57433060
    .word       0xBF8041A2
    .word       0x39903042
    .word       0xFC7CA9A3
    .word       0xFC558028
    .word       0x05260004
    .word       0xFC195060
    .word       0xFC750534
    .word       0x05340020
    .word       0x8028F85C
    .word       0x09C8F680
    .word       0x00243095
    .word       0x01A9B402
    .word       0x000132C0
    .word       0xFF115040
    .word       0xFC9CC85F
    .word       0x6EBF8020
    .word       0x0B697680
    .word       0xFC5CEF02
    .word       0xFEE2801C
    .word       0x38150000
    .word       0xED040028
    .word       0x0000F855
    .word       0x0018FC55
    .word       0x0025B042
    .word       0xED248D09
    .word       0x002A3855
    .word       0xF855ED06
    .word       0xCC3A0004
    .word       0x000132C0
    .word       0x0020FC75
    .word       0xED08AD89
    .word       0x002A3855
    .word       0xF855ED03
    .word       0xCC2E0004
    .word       0x000132C0
    .word       0x8028FC5C
    .word       0x03DF3042
    .word       0x8028F85C
    .word       0x0011B057
    .word       0xED108D04
    .word       0x3855CC04
    .word       0x3AF5002A
    .word       0x3455002A
    .word       0xB042002A
    .word       0xAD030031
    .word       0x3855ED30
    .word       0x3455002A
    .word       0x0062002A
    .word       0xAD871B90
    .word       0x100C0040
    .word       0x00203455
    .word       0x3855CC04
    .word       0x3855002A
    .word       0xED05002A
    .word       0x0004F855
    .word       0x32C0CC03
    .word       0x0EC00001
    .word       0x00203060
    .word       0xFC9C8DB4
    .word       0x69408020
    .word       0x052B6AC3
    .word       0x0006B042
    .word       0xFC5CAD2C
    .word       0x6923801C
    .word       0x2B900062
    .word       0x181800A2
    .word       0x34750CA3
    .word       0x05350028
    .word       0x2B9000A2
    .word       0x2F2D8E87
    .word       0x002A34B5
    .word       0x2B9000A3
    .word       0x001740E5
    .word       0x70C66EAE
    .word       0x00C50013
    .word       0x00C21058
    .word       0x30A23A2C
    .word       0x70C6FF00
    .word       0x00C50013
    .word       0x05A61058
    .word       0x00283875
    .word       0x6EBDC85E
    .word       0x0B697680
    .word       0x32C0EF02
    .word       0x34D50001
    .word       0x00D7002A
    .word       0xB417BB90
    .word       0x6E0D0114
    .word       0x00283455
    .word       0x38550565
    .word       0xFED50028
    .word       0xF6800018
    .word       0xFCBC0EE6
    .word       0x0056801C
    .word       0xF85511D0
    .word       0xFC950018
    .word       0x6E8D001C
    .word       0x0E06F680
    .word       0x002A34D5
    .word       0x001CF855
    .word       0x0004FC55
    .word       0xB462ED83
    .word       0xF8550041
    .word       0x30950000
    .word       0x6E8D0020
    .word       0x0CF4F680
    .word       0x002A34D5
    .word       0x00231455
    .word       0x30408D35
    .word       0xCC2FFFEB
    .word       0x002C3855
    .word       0x30608C07
    .word       0x41A25743
    .word       0x3042BF80
    .word       0xA9A33990
    .word       0x0024FC95
    .word       0x002A34D5
    .word       0xC805C804
    .word       0x76806E8D
    .word       0x0CE00EDC
    .word       0xFC950EC2
    .word       0x6E8D0024
    .word       0x002A34D5
    .word       0x0B267680
    .word       0xFAD56FB1
    .word       0x34750024
    .word       0xFC95002A
    .word       0x05B90020
    .word       0x0020F875
    .word       0x4042ED83
    .word       0xF875000E
    .word       0x30400000
    .word       0x3855FFE8
    .word       0xED07002C
    .word       0x0000F855
    .word       0x000132C0
    .word       0x32E0CCB3
    .word       0x8D7A0001
    .word       0xFFE73040
    .word       0x002C3855
    .word       0x000132C0
    .word       0x32E0CCA9
    .word       0xFC5C0001
    .word       0x30428028
    .word       0xF85C03DF
    .word       0xFC758028
    .word       0x41A2001C
    .word       0x50422144
    .word       0xB443DF1C
    .word       0x30400033
    .word       0x3040FFE6
    .word       0x385D12CD
    .word       0x20550058
    .word       0x205D1008
    .word       0x484D9048
    .word       0x484EC854
    .word       0x5040C855
    .word       0xC844DEAD
    .word       0x41A4C805
    .word       0x30849D00
    .word       0x6EA52000
    .word       0x7680EF18
    .word       0x0CE00EDC
    .word       0xBF8041A3
    .word       0x2930FC43
    .word       0xAD7C2D2E
    .word       0xBF8041A2
    .word       0x40003060
    .word       0x29303042
    .word       0x41A3E9A1
    .word       0x30635555
    .word       0x41A25555
    .word       0xF862BF80
    .word       0xCC062950
    .word       0x801CF81C
    .word       0x002C3855
    .word       0x801CF81C
    .word       0x8028F81C
    .word       0xBF8041A2
    .word       0x2940F802
    .word       0xAA9941A4
    .word       0x66553084
    .word       0x2940F882
    .word       0x556641A3
    .word       0x99AA5063
    .word       0x2940F862
    .word       0x870050C0
    .word       0xBF8041A5
    .word       0x29A0F8C5
    .word       0x2940F802
    .word       0x2940F882
    .word       0x2940F862
    .word       0x800041A3
    .word       0x41A244D9
    .word       0xF862BF80
    .word       0xED082990
    .word       0x0000F855
    .word       0x002C3C55
    .word       0x80000042
    .word       0xFC9CC85D
    .word       0x6EBB8020
    .word       0x0B697680
    .word       0xFC9CEF04
    .word       0x41A28020
    .word       0xF6808000
    .word       0xFCA20CC8
    .word       0x32C00244
    .word       0xCC240001
    .word       0x000132E0
    .word       0x001F9417
    .word       0xFED50EC0
    .word       0x02D70018
    .word       0xAD081390
    .word       0x76800C80
    .word       0x0CD60EE6
    .word       0x801CF81C
    .word       0x0C800EF6
    .word       0x801CFCBC
    .word       0x0EE67680
    .word       0xFC550CD7
    .word       0x02E20018
    .word       0xFAF5B9D0
    .word       0x32C00018
    .word       0xCC040001
    .word       0xCC020EE0
    .word       0x0EE00EE0
    .word       0x0E9F7680
    .word       0x94160C00
    .word       0x0FC20003
    .word       0x0014F855
    .word       0x0014FC55
    .word       0x11D0005E
    .word       0x0010FC75
    .word       0x13900043
    .word       0xFC5C8D2C
    .word       0x3040801C
    .word       0x3855FFEC
    .word       0xFC55002C
    .word       0x4C5C0000
    .word       0x0004B042
    .word       0x41A28D18
    .word       0xC85CFFEC
    .word       0x8020FC9C
    .word       0x76806EB9
    .word       0xEF040B69
    .word       0x8020FC9C
    .word       0x800041A2
    .word       0x0CC8F680
    .word       0x0244FCA2
    .word       0xFCBC0C80
    .word       0xF680801C
    .word       0x50C00EE6
    .word       0xFBD5FFFF
    .word       0xF8150014
    .word       0xF81C0000
    .word       0xFC5C801C
    .word       0x8D09801C
    .word       0xBA9002F0
    .word       0xFCB5B417
    .word       0x801CFCBC
    .word       0xB042CC15
    .word       0x8C0C0001
    .word       0x8020FC9C
    .word       0x0CC8F680
    .word       0x0010FCB5
    .word       0x9D0041A2
    .word       0x1AB13042
    .word       0x0C0045E2
    .word       0x0000F815
    .word       0xB042CC03
    .word       0x0C400001
    .word       0x5088233D
    .word       0x4C59459F
    .size       EZBL_Install2Flash, . - EZBL_Install2Flash   # 1800
    .end        EZBL_Install2Flash

    .ent        EZBL_FIFOSetBaud
    .type       EZBL_FIFOSetBaud, @function
EZBL_FIFOSetBaud:               # 0x9D001078
    .set        micromips
    .insn
    .word       0x45554FF1
    .word       0x00679404
    .word       0x69480E24
    .word       0xB40325A1
    .word       0xB0620062
    .word       0x40A30038
    .word       0xB0420008
    .word       0xAD0D003B
    .word       0xBF8041B0
    .word       0x3210CC0E
    .word       0xB04219F0
    .word       0x8D050028
    .word       0xBF8041B0
    .word       0x3210CC06
    .word       0x41B01800
    .word       0x3210BF80
    .word       0x41A21900
    .word       0xF6808000
    .word       0xFC820E66
    .word       0xFCB00240
    .word       0x40820040
    .word       0x0C000022
    .word       0x0040F850
    .word       0x2D2A6900
    .word       0x002440E2
    .word       0x00606980
    .word       0xE980294C
    .word       0x2D2E6900
    .word       0x001C40E2
    .word       0x00606980
    .word       0xE9807BCC
    .word       0x00606980
    .word       0xE980294C
    .word       0xEE016980
    .word       0x7BCC0064
    .word       0x2988E980
    .word       0x528C0064
    .word       0xCC0AA988
    .word       0x00400C00
    .word       0xF85011D0
    .word       0x69800040
    .word       0x0062ED01
    .word       0xE980294C
    .word       0x00243471
    .word       0x0062ED01
    .word       0x38717BCC
    .word       0x34710024
    .word       0x00600024
    .word       0x3871294C
    .word       0x34710024
    .word       0x00600024
    .word       0x3871318C
    .word       0xCC020024
    .word       0xED7F0C45
    .word       0x459F4515
    .short      0x4C11
    .size       EZBL_FIFOSetBaud, . - EZBL_FIFOSetBaud   # 222
    .end        EZBL_FIFOSetBaud

    .ent        UART1_RXTXInterrupt
    .type       UART1_RXTXInterrupt, @function
UART1_RXTXInterrupt:            # 0x9D001156
    .set        micromips
    .insn
    .short      0x03BD
    .word       0x034EE17C
    .word       0x036C00FC
    .word       0x4FC500FC
    .word       0x034CCB5D
    .word       0xCB7C10FC
    .word       0x0360CB5B
    .word       0x537B784C
    .word       0x036C0400
    .word       0xCBF702FC
    .word       0xCB15CB36
    .word       0xC9F3CA14
    .word       0xC9B1C9D2
    .word       0xC96FC990
    .word       0xC92DC94E
    .word       0xC8EBC90C
    .word       0xC8A9C8CA
    .word       0xC867C888
    .word       0xC825C846
    .word       0xC8594642
    .word       0xC8784603
    .word       0xBF8141A2
    .word       0xF020FE02
    .word       0x41A22C0D
    .word       0xFC428000
    .word       0xB45002F0
    .word       0x41A20007
    .word       0x76808000
    .word       0x0C000348
    .word       0x800041A2
    .word       0x0318FC42
    .word       0x0004B450
    .word       0x76800C00
    .word       0x0C000A9B
    .word       0x00024859
    .word       0x48783D7C
    .word       0x2D7C0003
    .word       0x231D4BF7
    .word       0x21FD1054
    .word       0x21BD104C
    .word       0x217D1044
    .word       0x213D103C
    .word       0x20FD1034
    .word       0x20BD102C
    .word       0x207D1024
    .word       0x203D101C
    .word       0x00001014
    .word       0x0000477C
    .word       0x4B5D1800
    .word       0x034E4B7C
    .word       0x4B5B02FC
    .word       0x034C4C3D
    .word       0x03BD12FC
    .word       0x036CF17C
    .word       0x000002FC
    .short      0xF37C
    .size       UART1_RXTXInterrupt, . - UART1_RXTXInterrupt   # 220
    .end        UART1_RXTXInterrupt

    .ent        EZBL_RdIntFlag
    .type       EZBL_RdIntFlag, @function
EZBL_RdIntFlag:                 # 0x9D001232
    .set        micromips
    .insn
    .short      0xCC07
    .short      0xED80
    .size       EZBL_RdIntFlag, . - EZBL_RdIntFlag   # 4
    .end        EZBL_RdIntFlag

    .ent        EZBL_InvIntFlag
    .type       EZBL_InvIntFlag, @function
EZBL_InvIntFlag:                # 0x9D001236
    .set        micromips
    .insn
    .short      0xCC05
    .short      0xED8C
    .size       EZBL_InvIntFlag, . - EZBL_InvIntFlag   # 4
    .end        EZBL_InvIntFlag

    .ent        EZBL_ClrIntFlag
    .type       EZBL_ClrIntFlag, @function
EZBL_ClrIntFlag:                # 0x9D00123A
    .set        micromips
    .insn
    .short      0xEE80
    .size       EZBL_ClrIntFlag, . - EZBL_ClrIntFlag   # 2
    .end        EZBL_ClrIntFlag

    .ent        EZBL_WrIntFlag
    .type       EZBL_WrIntFlag, @function
EZBL_WrIntFlag:                 # 0x9D00123C
    .set        micromips
    .insn
    .word       0xED848E82
    .size       EZBL_WrIntFlag, . - EZBL_WrIntFlag   # 4
    .end        EZBL_WrIntFlag

    .ent        EZBL_SetIntFlag
    .type       EZBL_SetIntFlag, @function
EZBL_SetIntFlag:                # 0x9D001240
    .set        micromips
    .insn
    .word       0x41A2ED88
    .word       0x3042BF81
    .word       0x00C4F040
    .word       0x2768116C
    .word       0x69600764
    .word       0x00448D87
    .word       0x073C1050
    .word       0x00E4EF81
    .word       0xEBE03810
    .word       0x2D21459F
    .size       EZBL_SetIntFlag, . - EZBL_SetIntFlag   # 40
    .end        EZBL_SetIntFlag

    .ent        EZBL_RdIntEn
    .type       EZBL_RdIntEn, @function
EZBL_RdIntEn:                   # 0x9D001268
    .set        micromips
    .insn
    .word       0xED80CC07
    .size       EZBL_RdIntEn, . - EZBL_RdIntEn   # 4
    .end        EZBL_RdIntEn

    .ent        EZBL_InvIntEn
    .type       EZBL_InvIntEn, @function
EZBL_InvIntEn:                  # 0x9D00126C
    .set        micromips
    .insn
    .word       0xED8CCC05
    .size       EZBL_InvIntEn, . - EZBL_InvIntEn   # 4
    .end        EZBL_InvIntEn

    .ent        EZBL_ClrIntEn
    .type       EZBL_ClrIntEn, @function
EZBL_ClrIntEn:                  # 0x9D001270
    .set        micromips
    .insn
    .short      0xEE80
    .size       EZBL_ClrIntEn, . - EZBL_ClrIntEn   # 2
    .end        EZBL_ClrIntEn

    .ent        EZBL_WrIntEn
    .type       EZBL_WrIntEn, @function
EZBL_WrIntEn:                   # 0x9D001272
    .set        micromips
    .insn
    .short      0x8E82
    .short      0xED84
    .size       EZBL_WrIntEn, . - EZBL_WrIntEn   # 4
    .end        EZBL_WrIntEn

    .ent        EZBL_SetIntEn
    .type       EZBL_SetIntEn, @function
EZBL_SetIntEn:                  # 0x9D001276
    .set        micromips
    .insn
    .short      0xED88
    .word       0xBF8141A2
    .word       0x3042CFE6
    .short      0xF0C0
    .size       EZBL_SetIntEn, . - EZBL_SetIntEn   # 12
    .end        EZBL_SetIntEn

    .ent        EZBL_RdIntPri
    .type       EZBL_RdIntPri, @function
EZBL_RdIntPri:                  # 0x9D001282
    .set        micromips
    .insn
    .short      0xCC02
    .short      0xED81
    .size       EZBL_RdIntPri, . - EZBL_RdIntPri   # 4
    .end        EZBL_RdIntPri

    .ent        EZBL_WrIntPri
    .type       EZBL_WrIntPri, @function
EZBL_WrIntPri:                  # 0x9D001286
    .set        micromips
    .insn
    .short      0xED80
    .word       0xBF8141A6
    .word       0x25282545
    .word       0x06442E43
    .word       0x8D84074C
    .word       0xF1401466
    .word       0x2535459F
    .word       0x208C0065
    .word       0x1866CFFB
    .short      0xF140
    .size       EZBL_WrIntPri, . - EZBL_WrIntPri   # 36
    .end        EZBL_WrIntPri

    .ent        EZBL_FIFOGetIntPri
    .type       EZBL_FIFOGetIntPri, @function
EZBL_FIFOGetIntPri:             # 0x9D0012AA
    .set        micromips
    .insn
    .short      0xCC02
    .short      0xED81
    .size       EZBL_FIFOGetIntPri, . - EZBL_FIFOGetIntPri   # 4
    .end        EZBL_FIFOGetIntPri

    .ent        EZBL_FIFOSetIntPri
    .type       EZBL_FIFOSetIntPri, @function
EZBL_FIFOSetIntPri:             # 0x9D0012AE
    .set        micromips
    .insn
    .short      0xED80
    .word       0x9D0041A7
    .word       0x30E7CC0C
    .short      0x1289
    .size       EZBL_FIFOSetIntPri, . - EZBL_FIFOSetIntPri   # 12
    .end        EZBL_FIFOSetIntPri

    .ent        EZBL_FIFOGetInt
    .type       EZBL_FIFOGetInt, @function
EZBL_FIFOGetInt:                # 0x9D0012BA
    .set        micromips
    .insn
    .short      0xCC05
    .short      0xED80
    .size       EZBL_FIFOGetInt, . - EZBL_FIFOGetInt   # 4
    .end        EZBL_FIFOGetInt

    .ent        EZBL_FIFOIntClear
    .type       EZBL_FIFOIntClear, @function
EZBL_FIFOIntClear:              # 0x9D0012BE
    .set        micromips
    .insn
    .short      0xEE80
    .size       EZBL_FIFOIntClear, . - EZBL_FIFOIntClear   # 2
    .end        EZBL_FIFOIntClear

    .ent        EZBL_FIFOIntFlagSet
    .type       EZBL_FIFOIntFlagSet, @function
EZBL_FIFOIntFlagSet:            # 0x9D0012C0
    .set        micromips
    .insn
    .word       0xED848E82
    .size       EZBL_FIFOIntFlagSet, . - EZBL_FIFOIntFlagSet   # 4
    .end        EZBL_FIFOIntFlagSet

    .ent        EZBL_FIFOIntRaise
    .type       EZBL_FIFOIntRaise, @function
EZBL_FIFOIntRaise:              # 0x9D0012C4
    .set        micromips
    .insn
    .word       0x41A7ED88
    .word       0x30E79D00
    .word       0x8E4A1243
    .word       0x4587ED00
    .word       0x00201484
    .size       EZBL_FIFOIntRaise, . - EZBL_FIFOIntRaise   # 20
    .end        EZBL_FIFOIntRaise

    .ent        EZBL_FIFOGetIntEn
    .type       EZBL_FIFOGetIntEn, @function
EZBL_FIFOGetIntEn:              # 0x9D0012D8
    .set        micromips
    .insn
    .word       0xED80CC05
    .size       EZBL_FIFOGetIntEn, . - EZBL_FIFOGetIntEn   # 4
    .end        EZBL_FIFOGetIntEn

    .ent        EZBL_FIFOIntDisable
    .type       EZBL_FIFOIntDisable, @function
EZBL_FIFOIntDisable:            # 0x9D0012DC
    .set        micromips
    .insn
    .short      0xEE80
    .size       EZBL_FIFOIntDisable, . - EZBL_FIFOIntDisable   # 2
    .end        EZBL_FIFOIntDisable

    .ent        EZBL_FIFOIntEnableSet
    .type       EZBL_FIFOIntEnableSet, @function
EZBL_FIFOIntEnableSet:          # 0x9D0012DE
    .set        micromips
    .insn
    .short      0x8E82
    .short      0xED84
    .size       EZBL_FIFOIntEnableSet, . - EZBL_FIFOIntEnableSet   # 4
    .end        EZBL_FIFOIntEnableSet

    .ent        EZBL_FIFOIntEnable
    .type       EZBL_FIFOIntEnable, @function
EZBL_FIFOIntEnable:             # 0x9D0012E2
    .set        micromips
    .insn
    .short      0xED88
    .word       0x9D0041A7
    .word       0x30E7CFF2
    .word       0x4FE91279
    .word       0xD01022FD
    .word       0x00459405
    .word       0x0E840E05
    .word       0x0EC70E66
    .word       0x00C26950
    .word       0x02268B90
    .word       0x0E221058
    .word       0x0008FE45
    .word       0x0010FEA5
    .word       0x00556953
    .word       0x0255A950
    .word       0x02B1A9D0
    .word       0xAD0F1390
    .word       0x8E070C44
    .word       0x76800CB2
    .word       0x0CD50ECA
    .word       0x02B1CC05
    .word       0x0C40A9D0
    .word       0xA9D002B1
    .word       0xFE50CC03
    .word       0x0EB10010
    .word       0x000440E2
    .word       0x768086D4
    .word       0x0CD50ECA
    .word       0x000DB416
    .word       0x915002B2
    .word       0x0008FA50
    .word       0x477C0002
    .word       0x18000000
    .word       0x05156900
    .word       0x0002E900
    .word       0xFF30577C
    .word       0x40F90018
    .word       0x86F20008
    .word       0x22FD854C
    .word       0x45995010
    .word       0xCC024C19
    .word       0x0C510C40
    .word       0x501022FD
    .word       0x4C19459F
    .size       EZBL_FIFOIntEnable, . - EZBL_FIFOIntEnable   # 174
    .end        EZBL_FIFOIntEnable

    .ent        EZBL_EraseAppSpace
    .type       EZBL_EraseAppSpace, @function
EZBL_EraseAppSpace:             # 0x9D001390
    .set        micromips
    .insn
    .word       0x45444FF5
    .word       0x9D0041A2
    .word       0x03B83042
    .word       0x0C606820
    .word       0x9D0041A7
    .word       0x03D030E7
    .word       0x00A36AC0
    .word       0x8F1F3390
    .word       0x6AA16ED0
    .word       0x07FFD0C5
    .word       0x000340E6
    .word       0x07FF50A5
    .word       0x32106ED0
    .word       0x00B00800
    .word       0xAF0E3390
    .word       0x6F240CC2
    .word       0x439000E6
    .word       0x000440A8
    .word       0x8028F81C
    .word       0x0C40CC1D
    .word       0x00B06822
    .word       0x05D72B90
    .word       0xCFDE6DB0
    .word       0xEAC00C46
    .word       0x0D987680
    .word       0x40A20C90
    .word       0xFC7C000C
    .word       0x41A28028
    .word       0x3042FFFF
    .word       0x052607BE
    .word       0x8028F85C
    .word       0x00187680
    .word       0xF81C0C90
    .word       0xED018028
    .word       0x459F4504
    .short      0x4C0D
    .size       EZBL_EraseAppSpace, . - EZBL_EraseAppSpace   # 142
    .end        EZBL_EraseAppSpace

    .ent        EZBL_RestoreAppErasableInRAMPage
    .type       EZBL_RestoreAppErasableInRAMPage, @function
EZBL_RestoreAppErasableInRAMPage: # 0x9D00141E
    .set        micromips
    .insn
    .short      0x4FE5
    .word       0xD010233D
    .word       0x41B10EC5
    .word       0x52311FFF
    .word       0x448CF800
    .word       0x9D0041A3
    .word       0x04203063
    .word       0x9D0041B5
    .word       0x044832B5
    .word       0x41B70E00
    .word       0x52F71FFF
    .word       0x33D1F800
    .word       0x02A30800
    .word       0x8D261390
    .word       0x00083243
    .word       0xFE636A30
    .word       0x02E40004
    .word       0xB6221250
    .word       0x32930016
    .word       0x00440003
    .word       0x0233E02C
    .word       0x05A619D0
    .word       0x0801B063
    .word       0x000240A3
    .word       0x99D0005E
    .word       0x07FFD084
    .word       0xF680846A
    .word       0x00960ECA
    .word       0x02702150
    .word       0x30408150
    .word       0x0054FFFC
    .word       0xCFD91A50
    .word       0x19500072
    .word       0x233D0C50
    .word       0x459F5010
    .short      0x4C1D
    .size       EZBL_RestoreAppErasableInRAMPage, . - EZBL_RestoreAppErasableInRAMPage   # 140
    .end        EZBL_RestoreAppErasableInRAMPage

    .ent        UART1_RX_FIFO_OnRead
    .type       UART1_RX_FIFO_OnRead, @function
UART1_RX_FIFO_OnRead:           # 0x9D0014AA
    .set        micromips
    .insn
    .short      0x4FE9
    .word       0xD01022FD
    .word       0x94C40E04
    .word       0x0E460030
    .word       0x76800EC5
    .word       0x0E270E9F
    .word       0xFE710EA2
    .word       0xE8160018
    .word       0x800041B4
    .word       0x21500216
    .word       0xF6800CB1
    .word       0x02120EE6
    .word       0x042031D0
    .word       0x095D7680
    .word       0x40E20C91
    .word       0x76800003
    .word       0x0C000348
    .word       0x0004B612
    .word       0xCC110C00
    .word       0x0018FA71
    .word       0x0E9F7680
    .word       0x02A20C00
    .word       0xFC7411D0
    .word       0x00620248
    .word       0xAD621390
    .word       0x21500216
    .word       0xFA71CC02
    .word       0x41A20018
    .word       0x30428000
    .word       0x346202D0
    .word       0xEE010024
    .word       0x18CC0064
    .word       0x00243862
    .word       0x22FD0C50
    .word       0x459F5010
    .short      0x4C19
    .size       UART1_RX_FIFO_OnRead, . - UART1_RX_FIFO_OnRead   # 140
    .end        UART1_RX_FIFO_OnRead

    .ent        UART1_TXInterruptFunc
    .type       UART1_TXInterruptFunc, @function
UART1_TXInterruptFunc:          # 0x9D001536
    .set        micromips
    .insn
    .short      0x4FF5
    .word       0x41B04544
    .word       0xF6808000
    .word       0x3090095F
    .word       0x0C7002F8
    .word       0xBF8041A7
    .word       0x02F83050
    .word       0x00013100
    .word       0x18003087
    .word       0xD0A56AC4
    .word       0x40A50200
    .word       0xFCA3002D
    .word       0x40A502F8
    .word       0x41A40008
    .word       0x30848000
    .word       0x450402F8
    .word       0x096ED680
    .word       0x6B224C0D
    .word       0xEAA26EE0
    .word       0xEB480B60
    .word       0x6B236A24
    .word       0x00C50768
    .word       0x40A52B90
    .word       0xEA220001
    .word       0x477C0004
    .word       0x18000000
    .word       0x02F8FC83
    .word       0xF8836E4E
    .word       0x000402F8
    .word       0x3482577C
    .word       0x00880024
    .word       0x3882000C
    .word       0xCFD00024
    .word       0x18003087
    .word       0x459F4504
    .short      0x4C0D
    .size       UART1_TXInterruptFunc, . - UART1_TXInterruptFunc   # 140
    .end        UART1_TXInterruptFunc

    .ent        UART1_TX_FIFO_Flush
    .type       UART1_TX_FIFO_Flush, @function
UART1_TX_FIFO_Flush:            # 0x9D0015C2
    .set        micromips
    .insn
    .short      0x4FED
    .word       0xD01022BD
    .word       0x0E650E04
    .word       0x41B40E46
    .word       0x6900BF80
    .word       0x32348D32
    .word       0x69141800
    .word       0x0100D042
    .word       0x000640E2
    .word       0x09717680
    .word       0x76800C90
    .word       0x0C900962
    .word       0xD0426914
    .word       0x40E20100
    .word       0x69000006
    .word       0x000340E2
    .word       0x0A9B7680
    .word       0x76800C00
    .word       0x0C000E9F
    .word       0x0000FC73
    .word       0xFC920535
    .word       0x00440000
    .word       0x8DDB1B90
    .word       0xCC120C40
    .word       0x76800C00
    .word       0x0C000E9F
    .word       0x0000FC73
    .word       0xFC920535
    .word       0x00440000
    .word       0xAD861B90
    .word       0x69140C40
    .word       0x0100D042
    .word       0xED018D70
    .word       0x501022BD
    .word       0x4C15459F
    .size       UART1_TX_FIFO_Flush, . - UART1_TX_FIFO_Flush   # 138
    .end        UART1_TX_FIFO_Flush

    .ent        EZBL_VerifyROM
    .type       EZBL_VerifyROM, @function
EZBL_VerifyROM:                 # 0x9D00164C
    .set        micromips
    .insn
    .word       0x45754FED
    .word       0x0E660E25
    .word       0x41B00E47
    .word       0x44C48000
    .word       0x76808698
    .word       0x0C000EAF
    .word       0x02708D26
    .word       0x41A69950
    .word       0x30C69D00
    .word       0xCC1C0408
    .word       0x9D0041A7
    .word       0x042030E7
    .word       0x0A900A3F
    .word       0x001294A4
    .word       0x0C460C00
    .word       0x001594E2
    .word       0xFD220C00
    .word       0x6D240000
    .word       0x4B900069
    .word       0xFFF69409
    .word       0xFFFCFD02
    .word       0x43900068
    .word       0xFFF040A8
    .word       0x0E036C90
    .word       0xFFE6B670
    .word       0xCC0C6D80
    .word       0x94120C40
    .word       0xED7F0009
    .word       0x0000FA12
    .word       0x0008F892
    .word       0x0004FA32
    .word       0x000CF8B2
    .word       0x459F4535
    .short      0x4C15
    .size       EZBL_VerifyROM, . - EZBL_VerifyROM   # 134
    .end        EZBL_VerifyROM

    .ent        EZBL_FIFOWrite
    .type       EZBL_FIFOWrite, @function
EZBL_FIFOWrite:                 # 0x9D0016D2
    .set        micromips
    .insn
    .short      0x4FED
    .word       0xD01022BD
    .word       0x8E360E04
    .word       0x0E850E46
    .word       0x68C06943
    .word       0x00D10495
    .word       0x00661B90
    .word       0x6A418858
    .word       0x0010FE70
    .word       0x99500053
    .word       0x99D00093
    .word       0x13900271
    .word       0x000A40A2
    .word       0x0ECA7680
    .word       0x02740CD3
    .word       0x02712950
    .word       0x690431D0
    .word       0xE901CC02
    .word       0x7680841E
    .word       0x6A010ECA
    .word       0x0002E901
    .word       0x0000477C
    .word       0x69001800
    .word       0xE9000522
    .word       0x577C0002
    .word       0x0014FF30
    .word       0x000840F9
    .word       0x854A86F2
    .word       0x501022BD
    .word       0x4C154599
    .word       0x0C46CC02
    .word       0x22BD0C51
    .word       0x459F5010
    .short      0x4C15
    .size       EZBL_FIFOWrite, . - EZBL_FIFOWrite   # 132
    .end        EZBL_FIFOWrite

    .ent        _general_exception_context
    .type       _general_exception_context, @function
_general_exception_context:     # 0x9D001756
    .set        micromips
    .insn
    .short      0x4FD5
    .word       0xC842C821
    .word       0xC884C863
    .word       0xC8C6C8A5
    .word       0xC908C8E7
    .word       0xC94AC929
    .word       0xC98CC96B
    .word       0xC9CEC9AD
    .word       0xCB10C9EF
    .word       0xCBF2CB31
    .word       0xC9134648
    .word       0xC9144608
    .word       0x9D0041BA
    .word       0x1BD9335A
    .word       0x008D0C00
    .word       0x00AC00FC
    .word       0x45DA00FC
    .word       0x00000000
    .word       0x00084914
    .word       0x49132D7C
    .word       0x3D7C0008
    .word       0x48424821
    .word       0x48844863
    .word       0x48C648A5
    .word       0x490848E7
    .word       0x494A4929
    .word       0x498C496B
    .word       0x49CE49AD
    .word       0x4B1049EF
    .word       0x4BF24B31
    .word       0x00004C2D
    .word       0x00001800
    .short      0xF37C
    .size       _general_exception_context, . - _general_exception_context   # 128
    .end        _general_exception_context

    .ent        EZBL_MapWriteEx
    .type       EZBL_MapWriteEx, @function
EZBL_MapWriteEx:                # 0x9D0017D6
    .set        micromips
    .insn
    .short      0xCC0F
    .short      0xED80
    .size       EZBL_MapWriteEx, . - EZBL_MapWriteEx   # 4
    .end        EZBL_MapWriteEx

    .ent        EZBL_MapWrite
    .type       EZBL_MapWrite, @function
EZBL_MapWrite:                  # 0x9D0017DA
    .set        micromips
    .insn
    .short      0xCC0C
    .short      0xED80
    .size       EZBL_MapWrite, . - EZBL_MapWrite   # 4
    .end        EZBL_MapWrite

    .ent        EZBL_MapClrEx
    .type       EZBL_MapClrEx, @function
EZBL_MapClrEx:                  # 0x9D0017DE
    .set        micromips
    .insn
    .short      0xCC0B
    .short      0xED84
    .size       EZBL_MapClrEx, . - EZBL_MapClrEx   # 4
    .end        EZBL_MapClrEx

    .ent        EZBL_MapClr
    .type       EZBL_MapClr, @function
EZBL_MapClr:                    # 0x9D0017E2
    .set        micromips
    .insn
    .short      0xCC08
    .short      0xED84
    .size       EZBL_MapClr, . - EZBL_MapClr   # 4
    .end        EZBL_MapClr

    .ent        EZBL_MapSetEx
    .type       EZBL_MapSetEx, @function
EZBL_MapSetEx:                  # 0x9D0017E6
    .set        micromips
    .insn
    .short      0xCC07
    .short      0xED88
    .size       EZBL_MapSetEx, . - EZBL_MapSetEx   # 4
    .end        EZBL_MapSetEx

    .ent        EZBL_MapSet
    .type       EZBL_MapSet, @function
EZBL_MapSet:                    # 0x9D0017EA
    .set        micromips
    .insn
    .short      0xCC04
    .short      0xED88
    .size       EZBL_MapSet, . - EZBL_MapSet   # 4
    .end        EZBL_MapSet

    .ent        EZBL_MapInvEx
    .type       EZBL_MapInvEx, @function
EZBL_MapInvEx:                  # 0x9D0017EE
    .set        micromips
    .insn
    .short      0xCC03
    .short      0xED8C
    .size       EZBL_MapInvEx, . - EZBL_MapInvEx   # 4
    .end        EZBL_MapInvEx

    .ent        EZBL_MapInv
    .type       EZBL_MapInv, @function
EZBL_MapInv:                    # 0x9D0017F2
    .set        micromips
    .insn
    .short      0xED8C
    .word       0x8EA2EF00
    .word       0x0DE50D60
    .word       0x0000FD05
    .word       0xCC180DA0
    .word       0x00013140
    .word       0x01AF4DA2
    .word       0x00473918
    .word       0x00E0246C
    .word       0x07EEAC4C
    .word       0x01426AF0
    .word       0x44AA1010
    .word       0x01848E84
    .word       0x018B7250
    .word       0x8D8A5950
    .word       0x01C007BE
    .word       0xE9701058
    .word       0xFFE8B50D
    .word       0x6010014D
    .word       0x0C4B459F
    .word       0x01C0EE84
    .word       0x07DE2858
    .word       0xE971CFF5
    .word       0x41A80C7F
    .word       0x31089D00
    .word       0xFD281AF0
    .word       0x94090000
    .word       0x3108002D
    .word       0xFD480004
    .word       0x31080000
    .word       0xFD680004
    .word       0x940B0000
    .word       0x31080010
    .word       0x15880004
    .word       0x314A0000
    .word       0x3108FFFF
    .word       0x19890001
    .word       0xB40A0000
    .word       0x3129FFF6
    .word       0xCC090001
    .word       0x18090C00
    .word       0x314A0000
    .word       0xB40AFFFF
    .word       0x3129FFFA
    .word       0x31080001
    .word       0x31400003
    .word       0x010AFFFC
    .word       0xFD284250
    .word       0xB4090000
    .word       0x0C00FFD0
    .word       0x459F0FE3
    .word       0x0C000C00
    .size       EZBL_MapInv, . - EZBL_MapInv   # 206
    .end        EZBL_MapInv

    .ent        main
    .type       main, @function
main:                           # 0x9D0018C0
    .set        micromips
    .insn
    .word       0x45754FED
    .word       0x0D387680
    .word       0xF6800C80
    .word       0x41B002B8
    .word       0xF8108000
    .word       0x32100208
    .word       0x41A20208
    .word       0xFC428000
    .word       0x76800248
    .word       0xE9040E9F
    .word       0x0E9F7680
    .word       0x0E02E905
    .word       0xBF8041B3
    .word       0x39903273
    .word       0x57433240
    .word       0x800041B1
    .word       0x00063A53
    .word       0x0E9F7680
    .word       0xFC710C00
    .word       0x25B90248
    .word       0x00430505
    .word       0x40E42390
    .word       0x04300004
    .word       0x01CD7680
    .word       0x7680EE01
    .word       0x0C0000C6
    .word       0x0C00CFEB
    .size       main, . - main   # 108
    .end        main

    .ent        EZBL_ForwardIRQ
    .type       EZBL_ForwardIRQ, @function
EZBL_ForwardIRQ:                # 0x9D00192C
    .set        micromips
    .insn
    .word       0x800041A6
    .word       0x032830C6
    .word       0x000130E0
    .word       0x41A227F2
    .word       0x30429D00
    .word       0x40E40018
    .word       0x41A40006
    .word       0x30849D00
    .word       0xCC032018
    .word       0x0C8227F4
    .word       0x41A327F4
    .word       0x30638000
    .word       0x6FF20320
    .word       0x94A26940
    .word       0x0C000008
    .word       0x00C36DB2
    .word       0xAD781390
    .word       0x459F0678
    .word       0x69300C40
    .word       0x00846A41
    .word       0x008480C0
    .word       0x2642C82C
    .word       0x9D0041A5
    .word       0x44E56ED0
    .word       0x45BFEA30
    .size       EZBL_ForwardIRQ, . - EZBL_ForwardIRQ   # 100
    .end        EZBL_ForwardIRQ

    .ent        EZBL_FIFOFlush
    .type       EZBL_FIFOFlush, @function
EZBL_FIFOFlush:                 # 0x9D001990
    .set        micromips
    .insn
    .word       0x45464FF1
    .word       0x8E240E04
    .word       0x7680C8A9
    .word       0x0C000E9F
    .word       0x6907C844
    .word       0x0C90AD06
    .word       0x40A26900
    .word       0xCC140007
    .word       0x6E890C00
    .word       0x6F1345E2
    .word       0x0C00CC14
    .word       0x0E8EF680
    .word       0x0026349D
    .word       0x0E9F7680
    .word       0x48640C00
    .word       0x48890535
    .word       0x1B900044
    .word       0xFFE840E3
    .word       0xCC036900
    .word       0x0001B042
    .word       0x4506ED01
    .word       0x4C11459F
    .size       EZBL_FIFOFlush, . - EZBL_FIFOFlush   # 88
    .end        EZBL_FIFOFlush

    .ent        EZBL_RAMCopy
    .type       EZBL_RAMCopy, @function
EZBL_RAMCopy:                   # 0x9D0019E8
    .set        micromips
    .insn
    .word       0x1A900085
    .word       0xAD912DB3
    .word       0x0C640C44
    .word       0x073F07E8
    .word       0xFFFC3100
    .word       0x22500106
    .word       0x0C808E0A
    .word       0x6ED26DB2
    .word       0xFFFCFC85
    .word       0xF883CFF4
    .word       0x0C64FFFC
    .word       0x94C40C80
    .word       0x07CA0008
    .word       0x00001507
    .word       0x190707C6
    .word       0xCFF70000
    .word       0x45BF6E40
    .size       EZBL_RAMCopy, . - EZBL_RAMCopy   # 68
    .end        EZBL_RAMCopy

    .ent        NOW_EnableInterrupts
    .type       NOW_EnableInterrupts, @function
NOW_EnableInterrupts:           # 0x9D001A2C
    .type       NOW_IntEnable, @function
NOW_IntEnable:                  # 0x9D001A2C
    .set        micromips
    .insn
    .word       0x0C40459F
    .word       0x00000003
    .word       0xBF882DD0
    .word       0xBF862DD0
    .word       0xBF922CD0
    .word       0x00000007
    .word       0xBF802EE0
    .word       0xBF822EE0
    .word       0xBF842EE0
    .word       0xBF862EE0
    .word       0xBF882EE0
    .word       0xBF9C2CE0
    .word       0xBF9A2DE0
    .word       0xFFFC7C00
    .word       0x00000000
    .word       0x00000000
    .word       0x00000000
    .size       NOW_IntEnable, . - NOW_IntEnable   # 68
    .size       NOW_EnableInterrupts, . - NOW_EnableInterrupts   # 68
    .end        NOW_EnableInterrupts

    .ent        EZBL_ForwardAllIRQ
    .type       EZBL_ForwardAllIRQ, @function
EZBL_ForwardAllIRQ:             # 0x9D001A70
    .set        micromips
    .insn
    .word       0xCBE54FF5
    .word       0x41A20D24
    .word       0x30429D00
    .word       0x41A80018
    .word       0x31080000
    .word       0x01080008
    .word       0x31401040
    .word       0x014A0001
    .word       0x94081800
    .word       0x3162000A
    .word       0x0C890004
    .word       0x0C967680
    .word       0x014B6AA0
    .word       0xCFF51150
    .word       0x4BE54D1E
    .word       0x4C0D459F
    .size       EZBL_ForwardAllIRQ, . - EZBL_ForwardAllIRQ   # 64
    .end        EZBL_ForwardAllIRQ

    .ent        __pic32_software_reset
    .type       __pic32_software_reset, @function
__pic32_software_reset:         # 0x9D001AB0
    .set        micromips
    .insn
    .word       0x477C0000
    .word       0x18000000
    .word       0xAA9941A3
    .word       0xBF8041A2
    .word       0x66553063
    .word       0x3670F802
    .word       0x3670F862
    .word       0x556641A3
    .word       0x99AA5063
    .word       0x3670F862
    .word       0xBF8041A2
    .word       0xF862ED81
    .word       0x41A226F8
    .word       0xFC42BF80
    .word       0xD68026F0
    .word       0x0C000D5C
    .word       0x80000328
    .word       0x00000004
    .word       0x00000000
    .word       0x8000032C
    .word       0x00000004
    .word       0x00000000
    .word       0x80000330
    .word       0x00000004
    .word       0x00000000
    .word       0x80000334
    .word       0x00000001
    .word       0x00000000
    .word       0x00000000
    .word       0x22222222
    .word       0x22222222
    .word       0x22222222
    .size       __pic32_software_reset, . - __pic32_software_reset   # 128
    .end        __pic32_software_reset

    .ent        EZBL_IsPageErased
    .type       EZBL_IsPageErased, @function
EZBL_IsPageErased:              # 0x9D001B30
    .set        micromips
    .insn
    .word       0x5FFF41A2
    .word       0xF8005042
    .word       0x41A344A2
    .word       0x44E3A000
    .word       0x08003044
    .word       0x00FC006D
    .word       0x007CD063
    .word       0xEF7FEE9C
    .word       0x94A36E42
    .word       0xFCE4000A
    .word       0xB4C7FFFC
    .word       0x0C000006
    .word       0xFFF7B444
    .word       0x459F6E42
    .word       0x459FED01
    .word       0xFFFF0C40
    .size       EZBL_IsPageErased, . - EZBL_IsPageErased   # 64
    .end        EZBL_IsPageErased

    .ent        EZBL_PreInstall
    .type       EZBL_PreInstall, @function
EZBL_PreInstall:                # 0x9D001B70
    .set        micromips
    .insn
    .word       0x08FC006F
    .word       0x9D0041A2
    .word       0x00003042
    .word       0x000B9443
    .word       0x4FF50C00
    .word       0xFC5CCBE5
    .word       0x40E28018
    .word       0x45E20006
    .word       0xCC050C00
    .word       0x459F4BE5
    .word       0xED01ED01
    .word       0x459F4BE5
    .word       0x0C004C0D
    .size       EZBL_PreInstall, . - EZBL_PreInstall   # 52
    .end        EZBL_PreInstall

    .ent        _bootstrap_exception_handler
    .type       _bootstrap_exception_handler, @function
_bootstrap_exception_handler:   # 0x9D001BA4
    .set        micromips
    .insn
    .word       0x000041A2
    .word       0x00003042
    .word       0x41A28D08
    .word       0x30420000
    .word       0x8D050000
    .word       0x9D0041A2
    .word       0x41A246C0
    .word       0x30429D00
    .word       0x40E21AB1
    .word       0x4FF50005
    .word       0x45C2CBE5
    .word       0x00000000
    .word       0x0C00CFFF
    .size       _bootstrap_exception_handler, . - _bootstrap_exception_handler   # 52
    .end        _bootstrap_exception_handler

    .ent        _general_exception_handler
    .type       _general_exception_handler, @function
_general_exception_handler:     # 0x9D001BD8
    .set        micromips
    .insn
    .word       0x000041A2
    .word       0x00003042
    .word       0x41A28D08
    .word       0x30420000
    .word       0x8D050000
    .word       0x9D0041A2
    .word       0x41A246C0
    .word       0x30429D00
    .word       0x40E21AB1
    .word       0x4FF50005
    .word       0x45C2CBE5
    .word       0x00000000
    .word       0x0C00CFFF
    .size       _general_exception_handler, . - _general_exception_handler   # 52
    .end        _general_exception_handler

    .ent        EZBL_CRC32
    .type       EZBL_CRC32, @function
EZBL_CRC32:                     # 0x9D001C0C
    .set        micromips
    .insn
    .word       0x076A4414
    .word       0xEDB841A4
    .word       0x83205084
    .word       0x000F94C5
    .word       0x09DF6ED0
    .word       0xED884453
    .word       0x0001D102
    .word       0x00822523
    .word       0x6DBE3B10
    .word       0x0107ADF9
    .word       0xCFF01018
    .word       0x459F0C00
    .short      0x4412
    .size       EZBL_CRC32, . - EZBL_CRC32   # 50
    .end        EZBL_CRC32

    .ent        EZBL_AddrInTable
    .type       EZBL_AddrInTable, @function
EZBL_AddrInTable:               # 0x9D001C3E
    .set        micromips
    .insn
    .short      0xAE04
    .word       0xE02C00C6
    .word       0x0C40459F
    .word       0x948506CA
    .word       0x0C00FFFB
    .word       0x006369C0
    .word       0x6941E02C
    .word       0xE02C0042
    .word       0x1B900066
    .word       0x6E44ADF4
    .word       0x13900046
    .word       0xED018D70
    .word       0xFFFF45BF
    .size       EZBL_AddrInTable, . - EZBL_AddrInTable   # 50
    .end        EZBL_AddrInTable

    .ent        _DefaultInterrupt
    .type       _DefaultInterrupt, @function
_DefaultInterrupt:              # 0x9D001C70
    .set        micromips
    .insn
    .word       0x000041A2
    .word       0x00003042
    .word       0x41A28D08
    .word       0x30420000
    .word       0x40E20000
    .word       0x46C00002
    .word       0x41A245BF
    .word       0x30429D00
    .word       0x40E21AB1
    .word       0x45C20003
    .word       0x00000000
    .word       0x0C0045BF
    .size       _DefaultInterrupt, . - _DefaultInterrupt   # 48
    .end        _DefaultInterrupt

    .ent        EZBL_mon_putc
    .type       EZBL_mon_putc, @function
EZBL_mon_putc:                  # 0x9D001CA0
    .type       _mon_putc, @function
_mon_putc:                      # 0x9D001CA0
    .set        micromips
    .insn
    .word       0x45464FF1
    .word       0x000041A2
    .word       0x00003042
    .word       0x0E048D04
    .word       0x00007400
    .word       0x1A1D0C00
    .word       0xFC9C0010
    .word       0x6E89802C
    .word       0x0B697680
    .word       0x4506EF01
    .word       0x4C11459F
    .size       _mon_putc, . - _mon_putc   # 44
    .size       EZBL_mon_putc, . - EZBL_mon_putc   # 44
    .end        EZBL_mon_putc

    .ent        EZBL_CalcUxBRGH
    .type       EZBL_CalcUxBRGH, @function
EZBL_CalcUxBRGH:                # 0x9D001CCC
    .set        micromips
    .insn
    .word       0x0C408E93
    .word       0x00044045
    .word       0xCC03ED81
    .word       0x29D000A0
    .word       0x054A0C60
    .word       0x00A226D4
    .word       0x0005BB3C
    .word       0x4642703C
    .word       0x6D2E8D83
    .word       0x11D00040
    .short      0x45BF
    .size       EZBL_CalcUxBRGH, . - EZBL_CalcUxBRGH   # 42
    .end        EZBL_CalcUxBRGH

    .ent        EZBL_mon_getc
    .type       EZBL_mon_getc, @function
EZBL_mon_getc:                  # 0x9D001CF6
    .type       _mon_getc, @function
_mon_getc:                      # 0x9D001CF6
    .set        micromips
    .insn
    .short      0x4FF1
    .word       0xAE05CBE7
    .word       0x8030FCBC
    .word       0x40E26950
    .word       0x6E090007
    .word       0x0EE67680
    .word       0xCC03EF01
    .word       0x0010145D
    .word       0x4BE7ED7F
    .word       0x4C11459F
    .size       _mon_getc, . - _mon_getc   # 38
    .size       EZBL_mon_getc, . - EZBL_mon_getc   # 38
    .end        EZBL_mon_getc

    .ent        NOW_Wait
    .type       NOW_Wait, @function
NOW_Wait:                       # 0x9D001D1C
    .set        micromips
    .insn
    .word       0x45554FF1
    .word       0x0E9F7680
    .word       0x0E220E04
    .word       0x0E9F7680
    .word       0x05950C00
    .word       0x1B900203
    .word       0xFFF840A3
    .word       0x459F4515
    .short      0x4C11
    .size       NOW_Wait, . - NOW_Wait   # 34
    .end        NOW_Wait

    .ent        NOW_32
    .type       NOW_32, @function
NOW_32:                         # 0x9D001D3E
    .type       NOW_64, @function
NOW_64:                         # 0x9D001D3E
    .set        micromips
    .insn
    .short      0x0049
    .word       0x41A400FC
    .word       0xFCA48000
    .word       0xFC64023C
    .word       0x00620238
    .word       0x05D61B90
    .word       0x0238F844
    .word       0x023CF864
    .short      0x45BF
    .size       NOW_64, . - NOW_64   # 32
    .size       NOW_32, . - NOW_32   # 32
    .end        NOW_32

    .ent        EZBL_RAMCompare
    .type       EZBL_RAMCompare, @function
EZBL_RAMCompare:                # 0x9D001D5E
    .set        micromips
    .insn
    .short      0x0C60
    .word       0x000A94C3
    .word       0x05380C00
    .word       0x6DB00BA0
    .word       0x092F053A
    .word       0xFFF69447
    .word       0x45BF052F
    .word       0x0C40459F
    .word       0x00FC034C
    .word       0xFFBF41BB
    .word       0xFFFF537B
    .word       0xD250037A
    .word       0x02FC034C
    .word       0xF37C0000
    .size       EZBL_RAMCompare, . - EZBL_RAMCompare   # 54
    .end        EZBL_RAMCompare

    .ent        EZBL_RAMCopyEx
    .type       EZBL_RAMCopyEx, @function
EZBL_RAMCopyEx:                 # 0x9D001D94
    .set        micromips
    .insn
    .word       0xCBE54FF5
    .word       0x0CF47680
    .word       0x01220D26
    .word       0x4BE51150
    .word       0x4C0D459F
    .word       0x7963B2E2
    .word       0x2DE540ED
    .word       0xEC4A3773
    .word       0xC02F3D41
    .size       EZBL_RAMCopyEx, . - EZBL_RAMCopyEx   # 36
    .end        EZBL_RAMCopyEx

    .ent        EZBL_WriteROMEx
    .type       EZBL_WriteROMEx, @function
EZBL_WriteROMEx:                # 0x9D001DB8
    .set        micromips
    .insn
    .word       0xBFC041B9
    .word       0x015B3339
    .word       0xFFFF45B9
    .word       0x5055434D
    .word       0x454D4348
    .size       EZBL_WriteROMEx, . - EZBL_WriteROMEx   # 20
    .end        EZBL_WriteROMEx

    .ent        EZBL_FIFORead
    .type       EZBL_FIFORead, @function
EZBL_FIFORead:                  # 0x9D001DCC
    .set        micromips
    .insn
    .word       0x0977D680
    .short      0x0CE0
    .size       EZBL_FIFORead, . - EZBL_FIFORead   # 6
    .end        EZBL_FIFORead

    .ent        EZBL_FIFOPeek
    .type       EZBL_FIFOPeek, @function
EZBL_FIFOPeek:                  # 0x9D001DD2
    .set        micromips
    .insn
    .short      0xD680
    .word       0xEF810977
    .word       0x0C0045BF
    .word       0x0C0045BF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .size       EZBL_FIFOPeek, . - EZBL_FIFOPeek   # 558
    .end        EZBL_FIFOPeek
    .popsection

    # Bootloader code block intended for program region 'kseg1_boot_mem'
    # 0xBFC00000 to 0xBFC01700, length 0x001700 (5888 bytes; needs 2 pages)
    .pushsection    EZBL_ROM_AT_0xBFC00000, address(0xBFC00000), code, keep

    .ent        EZBL_VectorDispatch0000
    .type       EZBL_VectorDispatch0000, @function
EZBL_VectorDispatch0000:        # 0xBFC00000
    .set        micromips
    .insn
    .word       0x00FC034C
    .word       0x04EC035A
    .word       0x000540BA
    .word       0x9D0041BA
    .word       0x0801535A
    .word       0x034F45BA
    .word       0x335A08FC
    .word       0x035A0801
    .word       0x459ACE4C
    .word       0x034C0C00
    .word       0xFFFF00FC
    .word       0xFFFFFFFF
    .word       0x22DD4FE5
    .word       0x0E25D01C
    .word       0x0E470E66
    .word       0xE02C0284
    .word       0x1FFF41B5
    .word       0xFFFC52B5
    .word       0xAA5002A7
    .word       0xBF8041A3
    .word       0x2930FC43
    .word       0xAD7C2D2E
    .word       0xBF8041B0
    .word       0x2930FC50
    .word       0x2000D042
    .word       0x3060AD24
    .word       0x41A24000
    .word       0xF862BF80
    .word       0x41A22934
    .word       0xFA82BF80
    .word       0x41A22950
    .word       0xFAA2BF80
    .word       0x41A22980
    .word       0xFA42BF80
    .word       0x41A22960
    .word       0x4872BF80
    .word       0x2970F862
    .word       0x40005231
    .word       0x2930FA30
    .word       0x000041A2
    .word       0x00003062
    .word       0x41A4AD99
    .word       0xCC310F80
    .word       0x40F10C00
    .word       0xC804FFDC
    .word       0x30C08680
    .word       0x41A203DF
    .word       0x3042BFC0
    .word       0x45E20031
    .word       0xFC500CE0
    .word       0x2D2E2930
    .word       0x3060AD7C
    .word       0xCFCD4000
    .word       0xBF8041A2
    .word       0x0000FC42
    .word       0x41A344A2
    .word       0x8E080000
    .word       0x00003063
    .word       0x0083EE01
    .word       0xEA222010
    .word       0xEE01CC08
    .word       0xEE016AA0
    .word       0x20100083
    .word       0xEA2044E5
    .word       0x0083EE01
    .word       0x6A201810
    .word       0x40A444A3
    .word       0x0002FFFC
    .word       0x0000477C
    .word       0x41A21800
    .word       0xF802BF80
    .word       0x41A32940
    .word       0x3063AA99
    .word       0x00736276
    .word       0xFA629950
    .word       0xFC9C2940
    .word       0x41A38028
    .word       0x50635566
    .word       0x05C79D89
    .word       0x2940F862
    .word       0x80005060
    .word       0xBF8041A2
    .word       0x2938F862
    .word       0x577C0002
    .word       0x22DDED01
    .word       0x459F501C
    .short      0x4C1D
    .size       EZBL_VectorDispatch0000, . - EZBL_VectorDispatch0000   # 346
    .end        EZBL_VectorDispatch0000

    .ent        __EZBL_WriteROMEx
    .type       __EZBL_WriteROMEx, @function
__EZBL_WriteROMEx:              # 0xBFC0015A
    .set        micromips
    .insn
    .short      0x4FD5
    .word       0xD030233D
    .word       0x0E040FDD
    .word       0x0E260EA5
    .word       0x0064F8FE
    .word       0x1068209E
    .word       0x8028FE5C
    .word       0xFA5E4E42
    .word       0xF81C0020
    .word       0x2F4F8028
    .word       0xDEAD5040
    .word       0x00329446
    .word       0x41AC2546
    .word       0x318C0000
    .word       0x004C0018
    .word       0x30529150
    .word       0x0040000E
    .word       0x005D100C
    .word       0x327DE9D0
    .word       0x0CD30018
    .word       0x41A80C40
    .word       0x31080000
    .word       0x01080018
    .word       0x41AA1040
    .word       0x314A9D00
    .word       0x010203F0
    .word       0x40E73B90
    .word       0x6F620007
    .word       0x3918004A
    .word       0xFFFCF8E6
    .word       0x6D20CFF6
    .word       0x0C402642
    .word       0x000A9482
    .word       0x004527A4
    .word       0x07FC4118
    .word       0x0000F907
    .word       0x6D20CFF7
    .word       0x0E600E40
    .word       0x008E9411
    .word       0x07FFD050
    .word       0x08003100
    .word       0x41D00048
    .word       0x13900111
    .word       0x00480E91
    .word       0x41A2A058
    .word       0x50427FFF
    .word       0x0050FFF8
    .word       0x41A25250
    .word       0x004A8000
    .word       0x0291BA90
    .word       0xED7F89D0
    .word       0x205EEDFF
    .word       0x2E059018
    .word       0x00083160
    .word       0x59D0008B
    .word       0x13900174
    .word       0x00540ECB
    .word       0x0C40B018
    .word       0x0018307E
    .word       0x06A80646
    .word       0x31500055
    .word       0x8B500B60
    .word       0x02C26D20
    .word       0xAEF72B50
    .word       0x0018FCFE
    .word       0xB447ED7F
    .word       0xFDFE0005
    .word       0x94EF001C
    .word       0x0C000045
    .word       0x0CD786DC
    .word       0x9D0041A3
    .word       0x1C3F3043
    .word       0x0024F8FE
    .word       0xF9FE45C2
    .word       0xFCFE0028
    .word       0xAD360024
    .word       0x0028FDFE
    .word       0xA00041A4
    .word       0x22900097
    .word       0xB4E26940
    .word       0x6A410004
    .word       0x002A95E4
    .word       0xEEFF0C00
    .word       0x0026B4A2
    .word       0xB4440C00
    .word       0x0C000023
    .word       0x8028FC5C
    .word       0x0018FCFE
    .word       0xFC7E6D2E
    .word       0x05340020
    .word       0x8028F85C
    .word       0x0064FC7E
    .word       0x0400D043
    .word       0x41A38D07
    .word       0x3043BF80
    .word       0x30803990
    .word       0xAA235743
    .word       0x8028FCDC
    .word       0x001CFC5E
    .word       0x0C97C844
    .word       0xBFC041A3
    .word       0x00313043
    .word       0xEE8245E2
    .word       0x8028F81C
    .word       0x000832F7
    .word       0xA1D002D4
    .word       0xA95002D5
    .word       0xFF8DB414
    .word       0x815002D0
    .word       0x0C00CF71
    .word       0x0FBE0C50
    .word       0x5030233D
    .word       0x4C2D459F
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .size       __EZBL_WriteROMEx, . - __EZBL_WriteROMEx   # 550
    .end        __EZBL_WriteROMEx

    .ent        EZBL_VectorDispatch0380
    .type       EZBL_VectorDispatch0380, @function
EZBL_VectorDispatch0380:        # 0xBFC00380
    .set        micromips
    .insn
    .word       0x08FC034F
    .word       0x0141335A
    .word       0xCE4C035A
    .word       0x0C00459A
    .word       0x00FC034D
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .size       EZBL_VectorDispatch0380, . - EZBL_VectorDispatch0380   # 128
    .end        EZBL_VectorDispatch0380

    .ent        EZBL_VectorDispatch0400
    .type       EZBL_VectorDispatch0400, @function
EZBL_VectorDispatch0400:        # 0xBFC00400
    .set        micromips
    .insn
    .word       0x08FC034F
    .word       0x0201335A
    .word       0xCE4C035A
    .word       0x0C00459A
    .word       0x00FC034D
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .size       EZBL_VectorDispatch0400, . - EZBL_VectorDispatch0400   # 128
    .end        EZBL_VectorDispatch0400

    .ent        EZBL_VectorDispatch0480
    .type       EZBL_VectorDispatch0480, @function
EZBL_VectorDispatch0480:        # 0xBFC00480
    .set        micromips
    .insn
    .word       0x02FC037F
    .word       0x08FC036F
    .word       0x0161337B
    .word       0x0C00459B
    .word       0x00FC037F
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .size       EZBL_VectorDispatch0480, . - EZBL_VectorDispatch0480   # 4736
    .end        EZBL_VectorDispatch0480
    .popsection

    # Bootloader code block intended for program region 'configsfrs'
    # 0xBFC017C0 to 0xBFC017E0, length 0x000020 (32 bytes; needs 0 pages)
    .pushsection    EZBL_BTLDR_CONFIG_WORD_configsfrs, address(0xBFC017C0), code, keep
    .word       0xFFFFFFFF
    .word       0xFFFFBFF7
    .word       0xFFFFFFF3
    .word       0xFFFFFFFA
    .word       0xFFFF698A
    .word       0xFFFFFDF9
    .word       0xFFFFFFFF
    .word       0xFFFFFFFF
    .popsection


#if !defined(EZBL_HIDE_BOOT_SYMBOLS)

#----Bootloader symbol export table----
    .weak       EZBL_AppPreInstall
    .type       EZBL_AppPreInstall, @object
    .weak       EZBL_bootCtx
    .type       EZBL_bootCtx, @object
    .weak       EZBL_COM_RX
    .type       EZBL_COM_RX, @object
    .weak       EZBL_COM_TX
    .type       EZBL_COM_TX, @object
    .weak       EZBL_COMBootIF
    .type       EZBL_COMBootIF, @object
    .weak       EZBL_NVMKey
    .type       EZBL_NVMKey, @object
    .weak       EZBL_STDIN
    .type       EZBL_STDIN, @object
    .weak       EZBL_STDOUT
    .type       EZBL_STDOUT, @object
    .weak       EZBL_Vector53ISRPtr
    .weak       EZBL_Vector53ISRPtr
    .weak       EZBL_Vector54ISRPtr
    .weak       EZBL_Vector54ISRPtr
    .weak       NOW_Fcy
    .type       NOW_Fcy, @object
    .weak       NOW_microsecond
    .type       NOW_microsecond, @object
    .weak       NOW_millisecond
    .type       NOW_millisecond, @object
    .weak       NOW_ms
    .type       NOW_ms, @object
    .weak       NOW_sec
    .type       NOW_sec, @object
    .weak       NOW_second
    .type       NOW_second, @object
    .weak       NOW_systemFrequency
    .type       NOW_systemFrequency, @object
    .weak       NOW_us
    .type       NOW_us, @object
    .weak       UART1_RxFifo
    .type       UART1_RxFifo, @object
    .weak       UART1_RxFifoBuffer
    .type       UART1_RxFifoBuffer, @object
    .weak       UART1_TxFifo
    .type       UART1_TxFifo, @object
    .weak       UART1_TxFifoBuffer
    .type       UART1_TxFifoBuffer, @object
    .weak       EZBL_AddrInTable
    .weak       EZBL_BootloaderTask
    .type       EZBL_ButtonMap, @object
    .weak       EZBL_ButtonMap
    .type       EZBL_COMBaud, @object
    .weak       EZBL_COMBaud
    .weak       EZBL_CRC32
    .weak       EZBL_CalcUxBRGH
    .weak       EZBL_ClrIntEn
    .weak       EZBL_ClrIntFlag
    .weak       EZBL_EraseAppSpace
    .weak       EZBL_ErasePage
    .weak       EZBL_FIFOFlush
    .weak       EZBL_FIFOGetInt
    .weak       EZBL_FIFOGetIntEn
    .weak       EZBL_FIFOGetIntPri
    .weak       EZBL_FIFOIntClear
    .weak       EZBL_FIFOIntDisable
    .weak       EZBL_FIFOIntEnable
    .weak       EZBL_FIFOIntEnableSet
    .weak       EZBL_FIFOIntFlagSet
    .weak       EZBL_FIFOIntRaise
    .weak       EZBL_FIFOPeek
    .weak       EZBL_FIFORead
    .weak       EZBL_FIFOReset
    .weak       EZBL_FIFOSetBaud
    .weak       EZBL_FIFOSetIntPri
    .weak       EZBL_FIFOWrite
    .weak       EZBL_ForwardAllIRQ
    .weak       EZBL_ForwardIRQ
    .weak       EZBL_Install2Flash
    .weak       EZBL_InvIntEn
    .weak       EZBL_InvIntFlag
    .weak       EZBL_IsPageErased
    .type       EZBL_LEDMap, @object
    .weak       EZBL_LEDMap
    .weak       EZBL_MapClr
    .weak       EZBL_MapClrEx
    .weak       EZBL_MapInv
    .weak       EZBL_MapInvEx
    .weak       EZBL_MapSet
    .weak       EZBL_MapSetEx
    .weak       EZBL_MapWrite
    .weak       EZBL_MapWriteEx
    .weak       EZBL_PreInstall
    .weak       EZBL_RAMCompare
    .weak       EZBL_RAMCopy
    .weak       EZBL_RAMCopyEx
    .weak       EZBL_RdIntEn
    .weak       EZBL_RdIntFlag
    .weak       EZBL_RdIntPri
    .weak       EZBL_RestoreAppErasableInRAMPage
    .weak       EZBL_SetIntEn
    .weak       EZBL_SetIntFlag
    .weak       EZBL_VerifyROM
    .weak       EZBL_WrIntEn
    .weak       EZBL_WrIntFlag
    .weak       EZBL_WrIntPri
    .weak       EZBL_WriteROMEx
    .type       EZBL_bootIDHash, @object
    .weak       EZBL_bootIDHash
    .type       EZBL_bootIDHash0, @object
    .weak       EZBL_bootIDHash0
    .type       EZBL_bootIDHash1, @object
    .weak       EZBL_bootIDHash1
    .type       EZBL_bootIDHash2, @object
    .weak       EZBL_bootIDHash2
    .type       EZBL_bootIDHash3, @object
    .weak       EZBL_bootIDHash3
    .type       EZBL_bootloaderWakeKey, @object
    .weak       EZBL_bootloaderWakeKey
    .weak       EZBL_mon_getc
    .weak       EZBL_mon_putc
    .weak       InitializeBoard
    .weak       LEDOff
    .weak       LEDSet
    .weak       LEDToggle
    .weak       NOW_32
    .weak       NOW_64
    .weak       NOW_CORETMR_ResetConst
    .weak       NOW_EnableInterrupts
    .weak       NOW_IntEnable
    .weak       NOW_Wait
    .weak       UART1_RXInterruptFunc
    .weak       UART1_RXTXInterrupt
    .weak       UART1_RX_FIFO_OnRead
    .weak       UART1_ResetBRG
    .weak       UART1_TXInterruptFunc
    .weak       UART1_TX_FIFO_Flush
    .weak       UART1_TX_FIFO_OnWrite
    .weak       _DefaultInterrupt
    .type       __crt0_exit, @object
    .weak       __crt0_exit
    .weak       __pic32_software_reset
    .weak       __vector_dispatch_53
    .weak       __vector_dispatch_54
    .weak       _bootstrap_exception_handler
    .weak       _general_exception_context
    .weak       _general_exception_handler
    .weak       _mon_getc
    .weak       _mon_putc
    .weak       main
    .weak       EZBL_VectorDispatch0000
    .weak       EZBL_VectorDispatch0380
    .weak       EZBL_VectorDispatch0400
    .weak       EZBL_VectorDispatch0480
    .weak       __EZBL_WriteROMEx
    .weak       _gp
    .weak       EZBL_APP_ebase_address
    .weak       EZBL_AppEntry
    .weak       EZBL_BEV_EXCPT_EBASE_OFFSET
    .weak       EZBL_BOOT_ebase_address
    .weak       EZBL_BOOTLOADER_END
    .weak       EZBL_ButtonMapLen
    .type       EZBL_ButtonMapLen, @object
    .weak       EZBL_DBG_EXCPT_EBASE_OFFSET
    .weak       EZBL_FLOW_THRESHOLD
    .weak       EZBL_ISRPointers
    .weak       EZBL_LEDMapLen
    .type       EZBL_LEDMapLen, @object
    .weak       EZBL_RESET_EBASE_OFFSET
    .weak       EZBL_Vector53Original
    .weak       EZBL_Vector54Original



#----Bootloader symbol addresses----
    .equ        EZBL_AppPreInstall, 0x80000328
    .equ        EZBL_bootCtx, 0x80000208
    .equ        EZBL_COM_RX, 0x8000032C
    .equ        EZBL_COM_TX, 0x80000330
    .equ        EZBL_COMBootIF, 0x80000200
    .equ        EZBL_NVMKey, 0x80000338
    .equ        EZBL_STDIN, 0x80000340
    .equ        EZBL_STDOUT, 0x8000033C
    .equ        EZBL_Vector53ISRPtr, 0x80000320
    .equ        EZBL_Vector53ISRPtr, 0x80000320
    .equ        EZBL_Vector54ISRPtr, 0x80000324
    .equ        EZBL_Vector54ISRPtr, 0x80000324
    .equ        NOW_Fcy, 0x80000240
    .equ        NOW_microsecond, 0x8000024C
    .equ        NOW_millisecond, 0x80000244
    .equ        NOW_ms, 0x80000244
    .equ        NOW_sec, 0x80000248
    .equ        NOW_second, 0x80000248
    .equ        NOW_systemFrequency, 0x80000240
    .equ        NOW_us, 0x8000024C
    .equ        UART1_RxFifo, 0x800002D0
    .equ        UART1_RxFifoBuffer, 0x80000250
    .equ        UART1_TxFifo, 0x800002F8
    .equ        UART1_TxFifoBuffer, 0x800002B0
    .equ        EZBL_ButtonMap, 0x9D001A30
    .equ        EZBL_COMBaud, 0x9D001A60
    .equ        EZBL_LEDMap, 0x9D001A40
    .equ        EZBL_bootIDHash, 0x9D001DA8
    .equ        EZBL_bootIDHash0, 0x9D001DA8
    .equ        EZBL_bootIDHash1, 0x9D001DAC
    .equ        EZBL_bootIDHash2, 0x9D001DB0
    .equ        EZBL_bootIDHash3, 0x9D001DB4
    .equ        EZBL_bootloaderWakeKey, 0x9D001DC4
    .equ        __crt0_exit, 0x9D0007DA
    .equ        _gp, 0x80008310
    .equ        EZBL_APP_ebase_address, 0x9D002000
    .equ        EZBL_AppEntry, 0x9D002801
    .equ        EZBL_BEV_EXCPT_EBASE_OFFSET, 0x000140
    .equ        EZBL_BOOT_ebase_address, 0x9D000000
    .equ        EZBL_BOOTLOADER_END, 0x9D002000
    .equ        EZBL_ButtonMapLen, 0x000003
    .equ        EZBL_DBG_EXCPT_EBASE_OFFSET, 0x000160
    .equ        EZBL_FLOW_THRESHOLD, 0x000020
    .equ        EZBL_ISRPointers, 0x80000320
    .equ        EZBL_LEDMapLen, 0x000007
    .equ        EZBL_RESET_EBASE_OFFSET, 0x000800
    .equ        EZBL_Vector53Original, 0x9D00001C
    .equ        EZBL_Vector54Original, 0x9D000028

#endif
