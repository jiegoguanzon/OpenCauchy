/**
 * EZBL Bootloader Code and RAM Allocation
 *
 * Automatically generated file - not intended for manual editing. If changes 
 * are made here, they will normally be overwritten when you rebuild your 
 * Bootloader. If necessary, maintain a backup copy and manually merge your 
 * customizations back in.
 * 
 * Built for:
 *     PIC24FJ1024GB610
 * From:
 *     dist\uart\production\ex_boot_uart.production.elf
 * Using build configuration:
 *     uart
 * 
 * Tool paths:
 *     C:\Program Files (x86)\Microchip\xc16\vDefault
 *     C:\Program Files (x86)\Microchip\MPLABX\vDefault
 */

 ; Validate matching target processors between Bootloader and Application projects.
 ; If you get this error and wish to force the two dissimilar targets together anyway,
 ; you can comment out these .error statements. However, something will likely be
 ; broken at run time, so do so only if you are sure of what you are doing.
    .ifndef __24FJ1024GB610
    .error "Bootloader's ex_boot_uart.merge.S/.gld files were generated for a different target processor."
    .error "Recompile this Application project or the ex_boot_uart Bootloader project for the same hardware."
    .endif


;----Target Bootloader ID and this Application Version meta data for .bl2 file header----
    .pushsection    .info.EZBL_metaParameters, info, keep
    .weak   _BOOTID_HASH0
    .weak   _BOOTID_HASH1
    .weak   _BOOTID_HASH2
    .weak   _BOOTID_HASH3
    .weak   _APPID_VER_BUILD
    .weak   _APPID_VER_MINOR
    .weak   _APPID_VER_MAJOR
_BOOTID_HASH0 = 0xFE07C793
_BOOTID_HASH1 = 0x57F877D8
_BOOTID_HASH2 = 0xCD24AEBD
_BOOTID_HASH3 = 0x4854ADED
EZBL_metaAppIDVerBuild:
    .long   _APPID_VER_BUILD
EZBL_metaAppIDVerMinor:
    .short  _APPID_VER_MINOR
EZBL_metaAppIDVerMajor:
    .short  _APPID_VER_MAJOR
    .popsection

;----Bootloader reserved static RAM----
#if defined(EZBL_HIDE_BOOT_SYMBOLS)
    ; Bootloader RAM block intended for data region
    ; 0x0850 to 0x0004, length 0x0004 (4 bytes)
    .pushsection    EZBL_RAM_AT_0x0850, address(0x0850), persist, keep
    .global     _EZBL_ForwardBootloaderISR
    .type       _EZBL_ForwardBootloaderISR, @object
_EZBL_ForwardBootloaderISR: ; This variable cannot be hidden since ISR dispatch code uses it
    .space      0x4
    .size       _EZBL_ForwardBootloaderISR, . - _EZBL_ForwardBootloaderISR
    .popsection
#endif

#if !defined(EZBL_HIDE_BOOT_SYMBOLS)
    ; Bootloader RAM block intended for 'data' region
    ; 0x0850 to 0x095E, length 0x010E (270 bytes)
    .pushsection    EZBL_RAM_AT_0x0850, address(0x0850), persist, keep
    .space      0x10E
    .popsection
    ; Bootloader RAM block intended for 'data' region
    ; 0x0960 to 0x0970, length 0x0010 (16 bytes)
    .pushsection    EZBL_RAM_AT_0x0960, address(0x0960), persist, keep
    .space      0x10
    .popsection
#endif

;----App erasable items that the Bootloader knows about----
    .pushsection    EZBL_AppErasable, address(0x001800), code, keep
    ; EZBL_appBootloadState - Base address of EZBL_INSTALLED_FLAGS structure (18 bytes/6 instruction words)
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0x12CDFF

    ; _EZBL_APP_RESET_BASE
    .extern     __reset
    goto        __reset

    ; Interrupt Goto Table follows for 0x1A0 addresses (0x001810 to 0x0019B0)
    ; The .igt section is defined in the linker script since this is an application
    ; erasable/updatable structure.
    .popsection


;----Bootloader reserved program memory----

    ; Bootloader code block intended for program region 'reset'
    ; 0x000000 to 0x000004, length 0x000004 (6 bytes; needs 0 pages)
    .pushsection    EZBL_ROM_AT_0x000000, address(0x000000), code, keep
    .pword      0x04016E, 0x000000                                                              /* 0x000000 n.....                   */
    .popsection

    ; Bootloader code block intended for program region 'ivt'
    ; 0x000004 to 0x000100, length 0x0000FC (378 bytes; needs 0 pages)
    .pushsection    EZBL_ROM_AT_0x000004, address(0x000004), code, keep
    .pword      0x001810, 0x001814, 0x001818, 0x00181C, 0x001820, 0x0019AC, 0x001824, 0x0019AC  /* 0x000004 ............ .....$..... */
    .pword      0x001828, 0x00182C, 0x001830, 0x000150, 0x001838, 0x00183C, 0x001840, 0x001844  /* 0x000014 (..,..0..P..8..<..@..D.. */
    .pword      0x001848, 0x00184C, 0x001850, 0x001854, 0x001858, 0x00185C, 0x001860, 0x001864  /* 0x000024 H..L..P..T..X..\..`..d.. */
    .pword      0x001868, 0x00186C, 0x001870, 0x001874, 0x001878, 0x0019AC, 0x00187C, 0x001880  /* 0x000034 h..l..p..t..x.....|..... */
    .pword      0x001884, 0x001888, 0x00188C, 0x001890, 0x001894, 0x001898, 0x00015A, 0x000164  /* 0x000044 ..................Z..d.. */
    .pword      0x0018A4, 0x0018A8, 0x0019AC, 0x0019AC, 0x0018AC, 0x0018B0, 0x0018B4, 0x0018B8  /* 0x000054 ........................ */
    .pword      0x0018BC, 0x0018C0, 0x0018C4, 0x0018C8, 0x0018CC, 0x0018D0, 0x0018D4, 0x0018D8  /* 0x000064 ........................ */
    .pword      0x0018DC, 0x0018E0, 0x0018E4, 0x0018E8, 0x0019AC, 0x0018EC, 0x0018F0, 0x0019AC  /* 0x000074 ........................ */
    .pword      0x0019AC, 0x0019AC, 0x0018F4, 0x0018F8, 0x0018FC, 0x001900, 0x001904, 0x001908  /* 0x000084 ........................ */
    .pword      0x00190C, 0x001910, 0x001914, 0x001918, 0x00191C, 0x001920, 0x001924, 0x001928  /* 0x000094 ............... ..$..(.. */
    .pword      0x00192C, 0x001930, 0x0019AC, 0x0019AC, 0x0019AC, 0x001934, 0x0019AC, 0x0019AC  /* 0x0000A4 ,..0...........4........ */
    .pword      0x0019AC, 0x001938, 0x00193C, 0x001940, 0x001944, 0x001948, 0x00194C, 0x001950  /* 0x0000B4 ...8..<..@..D..H..L..P.. */
    .pword      0x001954, 0x001958, 0x00195C, 0x001960, 0x0019AC, 0x0019AC, 0x001964, 0x001968  /* 0x0000C4 T..X..\..`........d..h.. */
    .pword      0x00196C, 0x001970, 0x001974, 0x001978, 0x0019AC, 0x00197C, 0x001980, 0x0019AC  /* 0x0000D4 l..p..t..x.....|........ */
    .pword      0x0019AC, 0x0019AC, 0x001984, 0x0019AC, 0x0019AC, 0x001988, 0x00198C, 0x001990  /* 0x0000E4 ........................ */
    .pword      0x001994, 0x001998, 0x00199C, 0x0019A0, 0x0019A4, 0x0019A8                      /* 0x0000F4 ..................       */
    .popsection

    ; Bootloader code block intended for program region 'program'
    ; 0x000100 to 0x001800, length 0x001700 (8832 bytes; needs 2 pages)
    .pushsection    EZBL_ROM_AT_0x000100, address(0x000100), code, keep
    .pword      0x001800, 0x0ABF00, 0x0ABF04, 0x0ABF14, 0x0ABF30, 0x0AC000, 0x000000, 0x00180C  /* 0x000100 ............0........... */
    .pword      0x0ABF00, 0x0ABF04, 0x0ABF18, 0x0ABF30, 0x000000, 0x001800, 0x001800, 0x00180C  /* 0x000110 .........0.............. */
    .pword      0x0ABF14, 0x0ABF18, 0x0ABF00, 0x000006, 0x00FFFF, 0xFFFFFF, 0x0ABF18, 0x000024  /* 0x000120 .....................$.. */
    .pword      0x00FF08, 0xFFFFFF, 0x00FF19, 0xFFFFFF, 0x00F7CA, 0xFFFFFF, 0x00FFFB, 0xFFFFFF  /* 0x000130 ........................ */
    .pword      0x007FDE, 0xFFFFFF, 0x00FFEF, 0xFFFFFF, 0x0ABF14, 0x000006, 0xFF7FFF, 0xFFFFFF  /* 0x000140 ........................ */
    .pword      0xAF0850, 0x041834, 0x000000, 0x04161A, 0x000000, 0xAF2850, 0x04189C, 0x000000  /* 0x000150 P..4...........P(....... */
    .pword      0x04029E, 0x000000, 0xAF4850, 0x0418A0, 0x000000, 0x040338, 0x000000, 0x20970F  /* 0x000160 ......PH.......8.......  */
    .pword      0x27FF0E, 0x88010E, 0x000000, 0x200000, 0xE00000, 0x320002, 0x200100, 0x880220  /* 0x000170 ..'........ .....2..  .. */
    .pword      0x070014, 0x070113, 0xE00000, 0x3A0003, 0x214780, 0x200001, 0x370002, 0x200000  /* 0x000180 ...........:.G!.. ..7..  */
    .pword      0x200001, 0x700F81, 0x320001, 0x070014, 0x200000, 0xE00000, 0x320002, 0x020000  /* 0x000190 .. ..p..2..... .....2... */
    .pword      0x000000, 0x020BDE, 0x000000, 0xDA4000, 0xFE0000, 0xA94044, 0x2002E0, 0xE00000  /* 0x0001A0 ..........@....D@... ... */
    .pword      0x320003, 0x202000, 0x880190, 0xA84044, 0x060000, 0x781F8A, 0x781F8B, 0x060000  /* 0x0001B0 ..2.  ...D@......x..x... */
    .pword      0x8802A1, 0x780480, 0xEB0000, 0x37001F, 0x4484E2, 0xB4A054, 0xBA0599, 0x4484E2  /* 0x0001C0 .....x.....7..DT.......D */
    .pword      0xB4A054, 0xBA0699, 0x4484E2, 0xB4A054, 0xEB0600, 0xDE6F47, 0xB207FD, 0x8801AE  /* 0x0001D0 T.......DT.....Go....... */
    .pword      0xE16C63, 0x3A0003, 0x07FFEA, 0xEB0000, 0x37000E, 0xE16C60, 0x3A0008, 0xEB4D00  /* 0x0001E0 cl...:........7`l...:.M. */
    .pword      0xE8050A, 0x390002, 0xEC2034, 0xA0F00A, 0xE9058B, 0x3EFFF9, 0x370004, 0xE16861  /* 0x0001F0 .....94 .........>..7ah. */
    .pword      0x320001, 0xEB8600, 0x07000B, 0xBAC719, 0xBA0519, 0xE0040E, 0x3A0003, 0xE0000A  /* 0x000200 ..2.................:... */
    .pword      0x3AFFDB, 0x060000, 0xB207FE, 0x8802AE, 0x78048A, 0x37FFF5, 0xA3D00C, 0x4D0FE2  /* 0x000210 ..:...........x..7.....M */
    .pword      0x39001C, 0x780289, 0xBA4D35, 0xE8050A, 0x390002, 0xEC2034, 0xA0F00A, 0xE9058B  /* 0x000220 ..9..x5M......94 ....... */
    .pword      0x32000F, 0xBA4D25, 0xE8050A, 0x390002, 0xEC2034, 0xA0F00A, 0xE9058B, 0x320008  /* 0x000230 ..2%M......94 .........2 */
    .pword      0xE0000C, 0x320006, 0xBACD15, 0xE8050A, 0x390002, 0xEC2034, 0xA0F00A, 0xE9058B  /* 0x000240 .....2........94 ....... */
    .pword      0xE88489, 0xB4A054, 0xE0000B, 0x3AFFE2, 0x060000, 0xBA0299, 0x785D05, 0xE9058B  /* 0x000250 ...T.......:.......]x... */
    .pword      0x32FFF7, 0xDE2AC8, 0x785D05, 0xE9058B, 0x32FFF3, 0xE0000C, 0x32FFF1, 0xBADD19  /* 0x000260 ..2.*..]x.....2.....2... */
    .pword      0x37FFEE, 0x041574, 0x000000, 0x0417A0, 0x000000, 0x04161A, 0x000000, 0x0411B8  /* 0x000270 ..7t.................... */
    .pword      0x000000, 0x04108C, 0x000000, 0x041642, 0x000000, 0x041260, 0x000000, 0x04029E  /* 0x000280 .........B.....`........ */
    .pword      0x000000, 0x040338, 0x000000, 0x040000, 0x000000, 0x04100E, 0x000000, 0xBE9F80  /* 0x000290 ...8.................... */
    .pword      0xBE9F82, 0xBE9F84, 0xFA0008, 0x200020, 0xA9C08B, 0x37003C, 0x801DA2, 0x801D81  /* 0x0002A0 ......... . ...<.7...... */
    .pword      0xA31801, 0x320002, 0xA923B0, 0xA04000, 0xA32801, 0x320001, 0xA05000, 0xA33801  /* 0x0002B0 .....2.#..@..(...2.P..8. */
    .pword      0x320001, 0xA06000, 0x2434D1, 0x9FBFC1, 0x250551, 0x9FBFD1, 0x243481, 0x9FBFE1  /* 0x0002C0 ..2.`..4$...Q.%....4$... */
    .pword      0xB02051, 0x9FBFF1, 0x804AA3, 0x6181E7, 0x5782E8, 0x79C265, 0x514F84, 0x3A000A  /* 0x0002D0 Q .....J...a..We.y.OQ..: */
    .pword      0xE80083, 0x884AA1, 0x508FE8, 0x3A000D, 0xA07000, 0x2092C1, 0x884301, 0x209401  /* 0x0002E0 ....J...P..:.p... .C...  */
    .pword      0x884311, 0x370007, 0xEB0180, 0x884AA3, 0x97F98F, 0x514F83, 0x3A0002, 0x200011  /* 0x0002F0 .C...7....J.....OQ..:..  */
    .pword      0x884AA1, 0x804961, 0x209323, 0x508F93, 0x390002, 0xA04000, 0x37000B, 0x804971  /* 0x000300 .J.aI.#. ..P..9.@...7qI. */
    .pword      0x785882, 0x884971, 0x8049A2, 0x410193, 0x508F83, 0x390001, 0x884972, 0x200012  /* 0x000310 .XxqI..I...A..P..9rI...  */
    .pword      0x2092C1, 0x410891, 0xAB03B0, 0x3AFFC2, 0x8049F1, 0x708000, 0x8849F0, 0xFA8000  /* 0x000320 .. ..A.....:.I...p.I.... */
    .pword      0xBE024F, 0xBE014F, 0xBE004F, 0x064000, 0xBE9F80, 0x781F82, 0xA9E08B, 0x370013  /* 0x000330 O..O..O...@......x.....7 */
    .pword      0x804A00, 0xE00000, 0x320012, 0x804A20, 0xFB80B0, 0x881D91, 0x884A20, 0x804A41  /* 0x000340 .J......2 J....... J.AJ. */
    .pword      0x209462, 0x408112, 0x500F82, 0x390001, 0x884A21, 0x200011, 0x209400, 0x108810  /* 0x000350 b. ..@..P..9!J... .. ... */
    .pword      0x804A90, 0xA00000, 0x884A90, 0xAB23B1, 0x32FFEB, 0x78014F, 0xBE004F, 0x064000  /* 0x000360 .J.....J..#...2O.xO...@. */
    .pword      0xBE9F88, 0x781F8A, 0xBE0400, 0x780502, 0x200000, 0xE00000, 0x320004, 0x78010A  /* 0x000370 .....x.....x.. .....2..x */
    .pword      0xBE0008, 0x020000, 0x000000, 0x540FE1, 0x3A0001, 0x804AD8, 0x78010A, 0xBE0008  /* 0x000380 ...........T..:.J...x... */
    .pword      0x0212F4, 0x000000, 0x78054F, 0xBE044F, 0x060000, 0x780181, 0xE00000, 0x3A0001  /* 0x000390 ......O.xO.......x.....: */
    .pword      0x804AE0, 0x780080, 0x780003, 0x0410FA, 0x000000, 0x050000, 0xB82260, 0x200206  /* 0x0003A0 .J...x..x.........`"...  */
    .pword      0xD00000, 0xD28081, 0xD28204, 0xD28285, 0xA00000, 0x520202, 0x5A8283, 0x310003  /* 0x0003B0 .................R..Z..1 */
    .pword      0x420202, 0x4A8283, 0xA10000, 0xE90306, 0x3AFFF3, 0x060000, 0x000003, 0x00D6A0  /* 0x0003C0 ..B..J........:......... */
    .pword      0x0076A0, 0x0066A0, 0x000008, 0x000666, 0x001666, 0x002666, 0x003666, 0x004666  /* 0x0003D0 .v..f....f..f..f&.f6.fF. */
    .pword      0x005666, 0x006666, 0x007666, 0x007C00, 0x00FFFC, 0x00C793, 0x00FE07, 0x0077D8  /* 0x0003E0 fV.ff.fv..|...........w. */
    .pword      0x0057F8, 0x00AEBD, 0x00CD24, 0x00ADED, 0x004854, 0xB005CF, 0xBE9F88, 0xBE9F8A  /* 0x0003F0 .W....$.....TH.......... */
    .pword      0xBE9F8C, 0x781F8E, 0x780500, 0x804300, 0xE00000, 0x3A0002, 0x780D00, 0x370269  /* 0x000400 .....x..x.C......:..xi.7 */
    .pword      0x218002, 0x200003, 0x9F8FD2, 0x9F8FE3, 0xBE0402, 0xEB0580, 0x804301, 0x780711  /* 0x000410 ..!.. .............C...x */
    .pword      0x78011A, 0xEB0180, 0x510FE8, 0x598FE0, 0x3E0254, 0x016002, 0x370008, 0x370033  /* 0x000420 ..x.....Q..YT.>.`...73.7 */
    .pword      0x3700BF, 0x3700E5, 0x37011A, 0x370177, 0x3701A7, 0x3701D0, 0x3701EB, 0x2002F0  /* 0x000430 ..7..7..7w.7..7..7..7..  */
    .pword      0x570F80, 0x36024E, 0x200302, 0x2FFBE0, 0x40000F, 0x0210F6, 0x000000, 0x2FFBE0  /* 0x000440 ..WN.6.. ../..@......../ */
    .pword      0x40000F, 0xEB0600, 0x784130, 0xB3C421, 0x514F81, 0x3A0011, 0x904090, 0xB2C321  /* 0x000450 ..@...0Ax!...OQ..:.@.!.. */
    .pword      0xB3C4C3, 0x69C190, 0x70C083, 0x9041A0, 0x69C102, 0x70C082, 0x3A0008, 0x804311  /* 0x000460 .....i..p.A...i..p..:.C. */
    .pword      0x780111, 0xEB0000, 0x0210FA, 0x000000, 0x20001D, 0x780D0D, 0x370005, 0xE8060C  /* 0x000470 ..x........... ..x..7... */
    .pword      0x2002C1, 0x560F81, 0x3AFFE7, 0xEB0680, 0x78010C, 0x804301, 0xEB0000, 0x0210FA  /* 0x000480 .. ..V..:.....x.C....... */
    .pword      0x000000, 0x200013, 0x3701DE, 0xEB0180, 0xEB0680, 0x2002F0, 0x570F80, 0x3601D9  /* 0x000490 ..... ..7........ ..W..6 */
    .pword      0x200302, 0x2FFBE0, 0x40000F, 0x0210FA, 0x000000, 0x200303, 0x2FFBE2, 0x41010F  /* 0x0004A0 .. ../..@........ ../..A */
    .pword      0xB80060, 0x021382, 0x000000, 0x980D40, 0x980D51, 0x97A01F, 0x97A0AF, 0xB10280  /* 0x0004B0 `........@..Q........... */
    .pword      0xB18001, 0x980D20, 0x980D31, 0x20001C, 0x98154C, 0x2FFBE0, 0x40000F, 0x4000E8  /* 0x0004C0 ... ..1.... L..../..@..@ */
    .pword      0x200102, 0x283EA0, 0x02175A, 0x000000, 0xE00000, 0x320006, 0x2FFEA0, 0x981540  /* 0x0004D0 .. .>(Z..........2../@.. */
    .pword      0x200080, 0x780D00, 0x78018C, 0x3701B5, 0x4506E4, 0x97A83F, 0x97A8CF, 0x97A95F  /* 0x0004E0 .. ..x..x..7..E?....._.. */
    .pword      0x97A9EF, 0xBE9E80, 0xBE9682, 0x200124, 0xEB0080, 0xBE0108, 0x2FFAC0, 0x40000F  /* 0x0004F0 .........$. ......../..@ */
    .pword      0x0212AC, 0x000000, 0x200000, 0xE00000, 0x320004, 0x212CD0, 0x9798EF, 0x508F80  /* 0x000500 ........ .....2.,!.....P */
    .pword      0x320004, 0x2FFDE0, 0x40000F, 0x578174, 0x370014, 0x90022A, 0x9002BA, 0x90034A  /* 0x000510 ..2../..@t.W..7*.....J.. */
    .pword      0x9003DA, 0x97906F, 0x9790FF, 0x97990F, 0x97999F, 0xE12000, 0xE1A801, 0xE1B002  /* 0x000520 ...o............ ....... */
    .pword      0xE1B803, 0x31FFEF, 0x2FFE90, 0x981540, 0x200070, 0x780D00, 0x78018C, 0x780683  /* 0x000530 .....1../@..p. ..x..x..x */
    .pword      0x370188, 0x97A8FF, 0x7080D0, 0x9FAFF1, 0x500F82, 0x3AFFFB, 0xE00001, 0x3A00E9  /* 0x000540 ..7.....p.....P..:.....: */
    .pword      0x217A00, 0xE00000, 0x32000D, 0x90092A, 0x9009BA, 0xB00402, 0xB08003, 0x78020D  /* 0x000550 .z!.....2*.............x */
    .pword      0x804311, 0x804300, 0x0217A0, 0x000000, 0xE00000, 0x3C0002, 0x2FFE50, 0x3700FF  /* 0x000560 .C..C............<P./..7 */
    .pword      0x804ABB, 0xE0000B, 0x320012, 0x200980, 0x200A82, 0x510100, 0xEB0080, 0x0217CC  /* 0x000570 .J......2.. .. ..Q...... */
    .pword      0x000000, 0xEF2850, 0xEF2852, 0xEB0000, 0x884AB0, 0x02177C, 0x000000, 0x804300  /* 0x000580 ...P(.R(.....J.|......C. */
    .pword      0x0214E6, 0x000000, 0x804310, 0x0214E6, 0x000000, 0xEB0000, 0x981530, 0xB81160  /* 0x000590 .......C..........0..`.. */
    .pword      0x980D62, 0x980D73, 0xEB8000, 0x981510, 0x200080, 0x980510, 0x200020, 0x780D00  /* 0x0005A0 b..s.......... ... . ..x */
    .pword      0x804310, 0xE00010, 0x3A0007, 0x2FF131, 0x9F8FC1, 0x200022, 0x2FF981, 0x40808F  /* 0x0005B0 .C......:1./...". ../..@ */
    .pword      0x0212F4, 0x000000, 0xE0000B, 0x320001, 0xFE6000, 0x90001A, 0xB40864, 0xB13E70  /* 0x0005C0 ...........2.`....d..p>. */
    .pword      0x9008EA, 0x400001, 0x884320, 0x200260, 0x40000A, 0x020A3C, 0x000000, 0x780600  /* 0x0005D0 .....@ C.`. ..@<.......x */
    .pword      0x200013, 0xEB0680, 0xE0000C, 0x3A0135, 0x2FF110, 0x9F8FC0, 0x200022, 0x2FF981  /* 0x0005E0 .. ......5.:../...". ../ */
    .pword      0x40808F, 0x804310, 0x0212F4, 0x000000, 0x804300, 0x780710, 0x98152C, 0x200040  /* 0x0005F0 ..@.C........C...x,..@.  */
    .pword      0x780D00, 0x90082A, 0x9008BA, 0x200243, 0x500F83, 0x588FE0, 0x3E0006, 0x200240  /* 0x000600 ..x*.....C. ..P..X..>@.  */
    .pword      0x981530, 0x200060, 0x980510, 0x200013, 0x370029, 0x90096A, 0x9009FA, 0x510FE0  /* 0x000610 0..`. ..... ).7j.......Q */
    .pword      0x598FE0, 0x3A0006, 0x200080, 0x981530, 0x200030, 0x980510, 0x200013, 0x37001E  /* 0x000620 ..Y..:.. 0..0. ..... ..7 */
    .pword      0x203DF0, 0xB42864, 0x200120, 0x981530, 0x570F80, 0x360001, 0x98153E, 0x9010BA  /* 0x000630 .= d(. . 0....W..6>..... */
    .pword      0x200300, 0x508F80, 0x360001, 0x981530, 0x90133A, 0xB83261, 0x520F82, 0x5A8F83  /* 0x000640 .. ..P..60..:..a2...R..Z */
    .pword      0x390003, 0x90086A, 0x981530, 0x370005, 0x200062, 0x090011, 0xD88302, 0x530001  /* 0x000650 ..9j..0....7b. ........S */
    .pword      0x981530, 0x200050, 0x980510, 0x200013, 0x370001, 0xEB0180, 0x90602A, 0x404FE8  /* 0x000660 0..P. ..... ..7...*`..O@ */
    .pword      0x3A000B, 0x90102A, 0x900A2A, 0x900ABA, 0xB80361, 0x530F84, 0x5B8F85, 0x360001  /* 0x000670 ..:*..*.....a....S..[..6 */
    .pword      0x780004, 0xEB0680, 0x500F8E, 0x3A00E5, 0x200202, 0xE00002, 0x320021, 0x804310  /* 0x000680 ..x.....P..:.. ...!.2.C. */
    .pword      0x9000B0, 0x508090, 0x508FE5, 0x36001C, 0x804301, 0x9000B1, 0x510F81, 0x360001  /* 0x000690 .....P..P..6.C......Q..6 */
    .pword      0x780101, 0x90122A, 0x508084, 0x508F82, 0x310003, 0x90113A, 0x510F84, 0x360010  /* 0x0006A0 ..x*....P..P..1:....Q..6 */
    .pword      0x50CFF3, 0x3A0001, 0xE90081, 0xDE0948, 0x514FF3, 0x3A0001, 0xB11001, 0x408204  /* 0x0006B0 ..P..:...H...OQ..:.....@ */
    .pword      0x981524, 0x9F8FC1, 0x200022, 0x2FF981, 0x40808F, 0x0212F4, 0x000000, 0x200013  /* 0x0006C0 $.....". ../..@........  */
    .pword      0x90113A, 0xEB0680, 0x570F82, 0x3900BD, 0x90102A, 0x500002, 0x981520, 0x900E2A  /* 0x0006D0 :.......W..9*....P ..*.. */
    .pword      0x900EBA, 0x804301, 0x2FFBE0, 0x40000F, 0x0210FA, 0x000000, 0x560600, 0x5E86E0  /* 0x0006E0 ....C.../..@........V..^ */
    .pword      0x980D2C, 0x980D3D, 0x9011BA, 0x2FFBE2, 0x41010F, 0x90084A, 0x9008DA, 0x021382  /* 0x0006F0 ,..=......./..AJ........ */
    .pword      0x000000, 0x980D40, 0x980D51, 0x90001A, 0x780D00, 0x500FE3, 0x3A00A0, 0x45007C  /* 0x000700 ...@..Q.......x..P..:|.E */
    .pword      0x90113A, 0x2FFBE1, 0x40808F, 0x0216D8, 0x000000, 0x9058FA, 0x90603A, 0x70C000  /* 0x000710 :..../..@.......X.:`...p */
    .pword      0x320096, 0x2FFEB0, 0x370024, 0xE0000B, 0x320001, 0xFE6000, 0xEB0300, 0xB82260  /* 0x000720 ..2../$.7.....2.`....`". */
    .pword      0x9011BA, 0x2FFBE2, 0x41010F, 0x90100A, 0x90109A, 0x0208F2, 0x000000, 0xBE0600  /* 0x000730 ...../..A............... */
    .pword      0x2FF9E4, 0x42020F, 0x9011BA, 0x2FFBE2, 0x41010F, 0x90100A, 0x90109A, 0x020C90  /* 0x000740 ../..B...../..A......... */
    .pword      0x000000, 0x98150C, 0x98151D, 0x9010BA, 0x90096A, 0x9009FA, 0x510101, 0x5981E0  /* 0x000750 ............j.......Q..Y */
    .pword      0x980D62, 0x980D73, 0x200031, 0x780D01, 0xE00000, 0x3D0007, 0x2FFE80, 0x981540  /* 0x000760 b..s..1. ..x.....=../@.. */
    .pword      0x200070, 0x780D00, 0x200013, 0x780683, 0x37006C, 0xE00000, 0x320068, 0x2FFE70  /* 0x000770 p. ..x.. ..xl.7...h.2p./ */
    .pword      0x981540, 0x200013, 0x780683, 0x370065, 0x203DF0, 0xB42864, 0x90094A, 0x9009DA  /* 0x000780 @.... ..xe.7.= d(.J..... */
    .pword      0x2DF1C0, 0x221441, 0x510F80, 0x598F81, 0x3A001E, 0x212CD0, 0x9F9FE0, 0x90002A  /* 0x000790 ..-A."..Q..Y..:.,!...*.. */
    .pword      0x9000BA, 0x90014A, 0x9001DA, 0x9F97E0, 0x9F97F1, 0x9F9F82, 0x9F9F93, 0x97A85F  /* 0x0007A0 ...J................._.. */
    .pword      0x97A8EF, 0x97A97F, 0x97B18F, 0x9F9FA0, 0x9F9FB1, 0x9F9FC2, 0x9F9FD3, 0xEB0300  /* 0x0007B0 ........................ */
    .pword      0x2DEAD5, 0xEB0200, 0x200123, 0x2FFAC2, 0x41010F, 0xBE0008, 0x0208F2, 0x000000  /* 0x0007C0 ..-...#. ../..A......... */
    .pword      0x02172A, 0x000000, 0x370002, 0x2FFE60, 0x981540, 0xEF2860, 0xEF2864, 0x200080  /* 0x0007D0 *.......7`./@..`(.d(...  */
    .pword      0x780D00, 0x90104A, 0xDE80CF, 0xDD00C0, 0x200000, 0x9F8FD0, 0x9F8FE1, 0x200042  /* 0x0007E0 ..xJ.......... ......B.  */
    .pword      0x2FF9A1, 0x40808F, 0x804310, 0x0212F4, 0x000000, 0x8042A2, 0x8042B3, 0x804310  /* 0x0007F0 ../..@.C........B..B..C. */
    .pword      0x02133C, 0x000000, 0x200FA0, 0x804341, 0xB80800, 0x0215CA, 0x000000, 0x200013  /* 0x000800 <....... AC............  */
    .pword      0x780683, 0x37001F, 0xEB0180, 0xEB0680, 0xE0000E, 0x32001B, 0x90092A, 0x9009BA  /* 0x000810 ..x..7...........2*..... */
    .pword      0xB87261, 0x520F82, 0x5A8F83, 0x390005, 0x780702, 0xEB0000, 0x0210FA, 0x000000  /* 0x000820 ar...R..Z..9..x......... */
    .pword      0xEF2860, 0x78010E, 0x804301, 0xEB0000, 0x0210FA, 0x000000, 0x90082A, 0x9008BA  /* 0x000830 `(...x.C..........*..... */
    .pword      0x50000E, 0x5880E0, 0x980D20, 0x980D31, 0x200013, 0xEB0680, 0x370002, 0x200013  /* 0x000840 ..P..X ..1.... .....7..  */
    .pword      0x780683, 0x9FB7F3, 0x0215F2, 0x000000, 0xBE0200, 0x97B1FF, 0xE00003, 0x320002  /* 0x000850 ..x....................2 */
    .pword      0x980D00, 0x980D11, 0x90090A, 0x90099A, 0x520102, 0x5A8183, 0x90006A, 0x9000FA  /* 0x000860 ..............R..Zj..... */
    .pword      0x510F80, 0x598F81, 0x360021, 0x2FFEC0, 0x981540, 0xE9801A, 0x500FE3, 0x3E0018  /* 0x000870 ..Q..Y!.6../@.......P..> */
    .pword      0x200000, 0x2FFEC1, 0x9F8FD0, 0x9F8FE1, 0x200042, 0x2FF9A1, 0x40808F, 0x804310  /* 0x000880 .. ../......B. ../..@.C. */
    .pword      0x9FB7F4, 0x9FBF85, 0x0212F4, 0x000000, 0x8042A2, 0x8042B3, 0x804310, 0x02133C  /* 0x000890 .............B..B..C.<.. */
    .pword      0x000000, 0xEB8100, 0x804301, 0xEB0000, 0x0210FA, 0x000000, 0x97BA8F, 0x97B27F  /* 0x0008A0 .......C................ */
    .pword      0x980D04, 0x980D15, 0xEB0D00, 0xEF2860, 0x804300, 0xE00000, 0x320003, 0x76868B  /* 0x0008B0 .........`(..C......2..v */
    .pword      0x3AFDAD, 0x370009, 0xE0000B, 0x320006, 0x90016A, 0x9001FA, 0x804310, 0x02133C  /* 0x0008C0 ..:..7.....2j......C.<.. */
    .pword      0x000000, 0xFE0000, 0xEB0D00, 0xA7F000, 0xEA0000, 0xE90000, 0xDE004F, 0x370001  /* 0x0008D0 ..................O....7 */
    .pword      0xEB0000, 0x78074F, 0xBE064F, 0xBE054F, 0xBE044F, 0xB105CF, 0x060000, 0xFE0000  /* 0x0008E0 ...O.xO..O..O........... */
    .pword      0xB82260, 0xE00003, 0x3200A1, 0xDE0BC8, 0x3A009F, 0x203E57, 0x781F87, 0xF80054  /* 0x0008F0 `"......2.....:W> ..xT.. */
    .pword      0xBE9F88, 0xBE9F8A, 0xBE9F8C, 0xA37801, 0x3A0006, 0xA3D804, 0x320002, 0xAF4761  /* 0x000900 ..........x...:.....2aG. */
    .pword      0xA26001, 0xA7C004, 0xA06001, 0x24003B, 0x20002D, 0xA0F004, 0x20762C, 0xE0000C  /* 0x000910 .`.....`.;.$-. ...,v ... */
    .pword      0x320006, 0xA1F004, 0x24000B, 0x40CFE8, 0x320002, 0x20004D, 0x24001B, 0xFA0006  /* 0x000920 ..2.....$..@..2M. ..$... */
    .pword      0xD0060D, 0x47878C, 0x78038E, 0x781B8B, 0xBE9B80, 0x78040F, 0xEB0480, 0x2DEADC  /* 0x000930 .....G..x..x.....x.....- */
    .pword      0xE1280C, 0x320011, 0xDD2AC2, 0x320003, 0xE90285, 0x098005, 0x781FB6, 0x200006  /* 0x000940 .(...2.*...2........x..  */
    .pword      0x8802A6, 0x2010C5, 0x2000C6, 0xAFE761, 0x37FFFE, 0x370002, 0xBA1F95, 0xBA9FB5  /* 0x000950 ..... .. a....7..7...... */
    .pword      0xE98306, 0x31FFFC, 0x578488, 0xF80864, 0xEF2864, 0x370055, 0x78058F, 0xE9060D  /* 0x000960 .....1..Wd..d(.U.7..x... */
    .pword      0x09800C, 0xEB9F80, 0xE90003, 0x098000, 0x785DB2, 0x558103, 0xEB0180, 0xD0008D  /* 0x000970 .............]x..U...... */
    .pword      0xA7000F, 0xEB0080, 0xD1000D, 0x090002, 0x785BB2, 0xEBDB80, 0xE90000, 0x3AFFFB  /* 0x000980 .............[x........: */
    .pword      0x578781, 0xE9060D, 0x09800C, 0x480FC7, 0x31003A, 0x7805C7, 0x780547, 0x45008D  /* 0x000990 ..W........H:.1..xG.x..E */
    .pword      0x4D8660, 0x540364, 0x780389, 0x430364, 0x5383E8, 0x390007, 0xE10836, 0xE1E036  /* 0x0009A0 `.Md.T..xd.C..S..96..6.. */
    .pword      0x34FFFA, 0xE15036, 0xE1D836, 0x31FFF8, 0x37002A, 0x200000, 0x200FA1, 0x470362  /* 0x0009B0 ..46P.6....1*.7.. .. b.G */
    .pword      0xA7F004, 0xBE0036, 0x8802A1, 0xD1008D, 0x78038E, 0x207606, 0xAFE761, 0x37FFFE  /* 0x0009C0 ...6..........x.v a....7 */
    .pword      0x090002, 0x781B37, 0xBB0837, 0xBB9837, 0xE90081, 0x3AFFFC, 0x21326A, 0x97B0FE  /* 0x0009D0 ...7.x7..7.......:j2!... */
    .pword      0x97B87F, 0xB80001, 0x50038A, 0x20000A, 0xA0001A, 0xEB0000, 0xA7001A, 0x37FFFE  /* 0x0009E0 ........P.. ...........7 */
    .pword      0xA7A004, 0xFE6000, 0x0217D6, 0x000000, 0x780B07, 0xB009B1, 0x780B01, 0xA8E761  /* 0x0009F0 ....`.........x.....xa.. */
    .pword      0x000000, 0x000000, 0xA3B804, 0xAFE761, 0x3AFFFE, 0x0217F2, 0x000000, 0x4703E2  /* 0x000A00 .........a....:........G */
    .pword      0xEB0000, 0x469B97, 0x481B97, 0xD1000D, 0x468000, 0x518180, 0x31FFB2, 0x418180  /* 0x000A10 .....F..H.....F..Q..1..A */
    .pword      0x3AFFA5, 0x23FF00, 0xB62760, 0xEB2780, 0x90001E, 0x9000AE, 0xFA8000, 0xBE064F  /* 0x000A20 ..:..#`'..'..........O.. */
    .pword      0xBE054F, 0xBE044F, 0xF90054, 0xEB2780, 0xEF2864, 0x060000, 0x4787EE, 0xBE9F88  /* 0x000A30 O..O..T...'.d(......G... */
    .pword      0xBE9F8A, 0xBE9F8C, 0x781F8E, 0x780600, 0x20800D, 0x80432E, 0xEF2864, 0x201000  /* 0x000A40 ........x..x.. .C.d(...  */
    .pword      0x200001, 0x9FB7B0, 0x9FB7C1, 0x57807A, 0x0216B4, 0x000000, 0x9FB7D0, 0x9FB7E1  /* 0x000A50 .. ......z.W............ */
    .pword      0x9FB7F2, 0x9FBF83, 0x200089, 0xEB0400, 0x37002E, 0x97EACF, 0x42CFE9, 0x360005  /* 0x000A60 ........ .....7.....B..6 */
    .pword      0x200010, 0x020D40, 0x000000, 0xEB0000, 0x37004B, 0x97B57F, 0x97BD8F, 0xE9000D  /* 0x000A70 .. @........K.7......... */
    .pword      0x97B37F, 0x600086, 0x320005, 0xEB0080, 0x70000A, 0x70808B, 0x400561, 0x4885E0  /* 0x000A80 .....`..2.....p..pa.@..H */
    .pword      0xB86861, 0x97B15F, 0x97B1EF, 0x410000, 0x498081, 0x9FB7D0, 0x9FB7E1, 0x500F8A  /* 0x000A90 ah._.......A..I........P */
    .pword      0x588F8B, 0x390010, 0x2010C0, 0x200001, 0x97B2BF, 0x528F80, 0x310030, 0x57807A  /* 0x000AA0 ..X..9.. .. .....R0.1z.W */
    .pword      0x0216B4, 0x000000, 0x9FB7D0, 0x9FB7E1, 0x9FB7F2, 0x9FBF83, 0x550F80, 0x5D8F81  /* 0x000AB0 ....................U..] */
    .pword      0x360001, 0xE90408, 0xE80408, 0x78001C, 0x540F80, 0x39FFCF, 0xE80E00, 0x97B05F  /* 0x000AC0 ..6........x..T..9..._.. */
    .pword      0x97B0EF, 0x021668, 0x000000, 0xE00000, 0x3A0016, 0x97B25F, 0x97B2EF, 0x200042  /* 0x000AD0 ...h..........:_.....B.  */
    .pword      0x200003, 0x201180, 0x200001, 0x0213C4, 0x000000, 0xE00000, 0x3A000C, 0x207BE0  /* 0x000AE0 .. .. .. ...........:.{  */
    .pword      0x470700, 0x88432E, 0x97B05F, 0x97B0EF, 0x0215A2, 0x000000, 0xEB0000, 0x020D40  /* 0x000AF0 ..G.C._..............@.. */
    .pword      0x000000, 0x200010, 0x370005, 0xE90489, 0x3AFFDE, 0x200010, 0x370001, 0xEB0000  /* 0x000B00 ..... ..7.....:.. ..7... */
    .pword      0x78074F, 0xBE064F, 0xBE054F, 0xBE044F, 0xB100EF, 0x060000, 0xBE9F88, 0x2009A1  /* 0x000B10 O.xO..O..O.............  */
    .pword      0xEB0100, 0x200780, 0x201013, 0x784980, 0x784981, 0x784982, 0x800802, 0xA00002  /* 0x000B20 ..... .. .Ix.Ix.Ix...... */
    .pword      0x200571, 0x200460, 0x201003, 0x784980, 0x784981, 0x784982, 0xAB0100, 0x3AFFFE  /* 0x000B30 q. `. .. .Ix.Ix.Ix.....: */
    .pword      0x2B1200, 0x880810, 0xEF210C, 0x280000, 0x880830, 0x2009A1, 0x200012, 0x200780  /* 0x000B40 ..+....!...(0.... .. ..  */
    .pword      0x201013, 0x784980, 0x784981, 0x784982, 0x800802, 0xA00002, 0x200571, 0x200460  /* 0x000B50 .. .Ix.Ix.Ix......q. `.  */
    .pword      0x201003, 0x784980, 0x784981, 0x784982, 0xAB0100, 0x3AFFFE, 0x800800, 0xDE004C  /* 0x000B60 .. .Ix.Ix.Ix.....:...L.. */
    .pword      0x600065, 0x500FE1, 0x3A0004, 0x200201, 0x800802, 0x608002, 0x32FFFD, 0x224008  /* 0x000B70 e.`..P..:.. .....`..2.@" */
    .pword      0x200F49, 0x8842C8, 0x8842D9, 0x8842C8, 0x8842D9, 0x23E800, 0x8842A0, 0xEF2856  /* 0x000B80 I. .B..B..B..B...#.B.V(. */
    .pword      0x200100, 0x884340, 0x0704C1, 0xA8A69F, 0xA8E69E, 0xA8C69E, 0xA9E6A6, 0xA9C6A6  /* 0x000B90 .. @C................... */
    .pword      0xEF6666, 0xEF6662, 0xEF666A, 0xA886D6, 0x2FFC00, 0xB607B6, 0xB300A0, 0x883DB0  /* 0x000BA0 ff.bf.jf....../.......=. */
    .pword      0x280FF1, 0x803F22, 0x608082, 0x205000, 0x700001, 0x883F20, 0x2008C0, 0x8042B2  /* 0x000BB0 ..("?...`.P ..p ?... .B. */
    .pword      0xB99100, 0x8042A1, 0xB80800, 0x410081, 0x070500, 0x2FFF00, 0x070157, 0x209400  /* 0x000BC0 ....B......A...../W....  */
    .pword      0x884AD0, 0x2092C0, 0x884AE0, 0x8842E0, 0xBE0008, 0xBE044F, 0x060000, 0x4787E6  /* 0x000BD0 .J... .J..B....O.......G */
    .pword      0xBE9F88, 0xBE9F8A, 0xBE9F8C, 0x781F8E, 0xEF2956, 0xEF2864, 0xEF2860, 0xEF285E  /* 0x000BE0 ...........xV).d(.`(.^(. */
    .pword      0xEF2850, 0xEF2852, 0x07FF93, 0x208CE0, 0xEB0800, 0x8042C0, 0x8042D1, 0x8846D0  /* 0x000BF0 P(.R(...... ....B..B..F. */
    .pword      0x8846E1, 0x8042A2, 0x8042B3, 0xDD1845, 0xDE10CB, 0x700081, 0xDD1045, 0xB82260  /* 0x000C00 .F..B..B.E.......pE..`". */
    .pword      0xB83360, 0x520200, 0x5A8281, 0x5B0360, 0x5B83E0, 0x215741, 0x209180, 0x0702FA  /* 0x000C10 `3...R..Z`.[..[AW!.. ... */
    .pword      0x0704E8, 0xBE0600, 0x9FB7F0, 0x9FBF81, 0x208DAE, 0x841F3A, 0x841F4B, 0xFE6000  /* 0x000C20 .............. :..K...`. */
    .pword      0x0704E0, 0xBE0400, 0x8042C0, 0x8042D1, 0xDD09CC, 0xDE0044, 0x718000, 0xDE08C4  /* 0x000C30 .......B..B....D....q... */
    .pword      0x54010C, 0x5C818D, 0x510F80, 0x598F81, 0x360004, 0x40060C, 0x48868D, 0x200010  /* 0x000C40 ..T..\..Q..Y..6..@..H..  */
    .pword      0x0705CD, 0x8042E0, 0x900890, 0xE00001, 0x320005, 0xEB0080, 0x980811, 0x9FB7F8  /* 0x000C50 ....B.........2......... */
    .pword      0x9FBF89, 0x370014, 0x97B17F, 0x97B98F, 0x540402, 0x5C8483, 0x540FBE, 0x5C8FAE  /* 0x000C60 .....7........T..\..T..\ */
    .pword      0x36000D, 0x550FE0, 0x5D8FE0, 0x3C0004, 0xE20860, 0x3A0002, 0xBE010A, 0x070145  /* 0x000C70 ..6..U..]..<`....:...E.. */
    .pword      0x070417, 0xE00000, 0x320003, 0x200FF0, 0x070582, 0x07055A, 0xFE4001, 0x37FFCF  /* 0x000C80 ........2.. ...Z...@...7 */
    .pword      0xFA0016, 0xBE9F88, 0xBE9F8A, 0x781F8C, 0x780482, 0x780503, 0x780404, 0x2BF002  /* 0x000C90 ...........x..x..x..x..+ */
    .pword      0x2000A3, 0x9FB782, 0x9FB793, 0x97B10F, 0x97B19F, 0x9FBF92, 0x9FBFA3, 0xE00008  /* 0x000CA0 .. ..................... */
    .pword      0x3A0001, 0x57847C, 0xEB0600, 0xEB0580, 0x780288, 0x78020B, 0x78018A, 0x780109  /* 0x000CB0 ..:|.W........x..x..x..x */
    .pword      0x020DE8, 0x000000, 0xE00000, 0x320036, 0xBE0218, 0x200082, 0x200003, 0x2011C0  /* 0x000CC0 .........6.2..... .. ..  */
    .pword      0x200001, 0x0213C4, 0x000000, 0xE00000, 0x3A001E, 0xBE0218, 0x97B81F, 0x97B8AF  /* 0x000CD0 .. ...........:......... */
    .pword      0x500FE0, 0x588FE0, 0x32000F, 0xBE0004, 0x97B91F, 0x97B9AF, 0x520F82, 0x5A8F83  /* 0x000CE0 ..P..X..2...........R..Z */
    .pword      0x390009, 0xE0000B, 0x3A001C, 0x904128, 0x20009B, 0xE00402, 0x350001, 0x20004B  /* 0x000CF0 ..9.....:(A... .....5K.  */
    .pword      0x9004A8, 0x37FFDA, 0x2000C2, 0x200003, 0x2010C0, 0x200001, 0x0213C4, 0x000000  /* 0x000D00 .....7.. .. .. .. ...... */
    .pword      0xE00000, 0x32000F, 0x20001C, 0x550FE3, 0x36000D, 0x900128, 0x550502, 0x550563  /* 0x000D10 .....2.. ..U..6(....Uc.U */
    .pword      0x44850A, 0x200024, 0x200005, 0x420038, 0x4A80A8, 0x4104E3, 0x37FFC5, 0x2FFFCC  /* 0x000D20 ..D$. .. 8.B..J..A..7../ */
    .pword      0x370001, 0xEB8600, 0x78000C, 0x78064F, 0xBE054F, 0xBE044F, 0xFA8000, 0x060000  /* 0x000D30 ..7.....xO.xO..O........ */
    .pword      0xFA000A, 0xBE9F88, 0xBE9F8A, 0xBE9F8C, 0x980730, 0x98074F, 0x2002C0, 0x200001  /* 0x000D40 ............0..O.... ..  */
    .pword      0x0217DE, 0x000000, 0xBE0200, 0x780580, 0xE88080, 0xA10001, 0x478781, 0x90054E  /* 0x000D50 ...........x........GN.. */
    .pword      0xEB0080, 0x201242, 0x200003, 0x78000A, 0x0212AC, 0x000000, 0x370033, 0xEB4000  /* 0x000D60 ...B. .. ..x......3.7.@. */
    .pword      0x984750, 0x78000A, 0x7840B0, 0x984721, 0x7840B0, 0x984731, 0x784010, 0x984740  /* 0x000D70 PG...x.@x!G..@x1G..@x@G. */
    .pword      0x450063, 0x784F10, 0x90404A, 0x984710, 0x90402A, 0x404FE8, 0x320004, 0x200014  /* 0x000D80 c.E.OxJ@..G.*@..O@..2..  */
    .pword      0xE00400, 0x3D0005, 0x370001, 0x200084, 0x90003E, 0xE00000, 0x320016, 0x78061E  /* 0x000D90 .....=..7.. >.......2..x */
    .pword      0x4506E6, 0x90041E, 0x9004AE, 0xEB0280, 0x78018C, 0x78010D, 0xBE0008, 0x020DE8  /* 0x000DA0 ..E...........x..x...... */
    .pword      0x000000, 0xE00000, 0x32000A, 0x203DF0, 0x884320, 0xEB0300, 0x2DEAD5, 0xEB0200  /* 0x000DB0 ........2.=  C......-... */
    .pword      0x78018C, 0x78010D, 0xBE0008, 0x020F90, 0x000000, 0x78001E, 0x558580, 0x5585E6  /* 0x000DC0 ..x..x...........x..U..U */
    .pword      0x400066, 0x450500, 0x558FE6, 0x3EFFCB, 0xEF2864, 0x9007CE, 0xBE064F, 0xBE054F  /* 0x000DD0 f.@..E..U..>d(....O..O.. */
    .pword      0xBE044F, 0xFA8000, 0x060000, 0xB82260, 0xBE9F88, 0xBE9F8A, 0xF80054, 0xE00005  /* 0x000DE0 O........`".......T..... */
    .pword      0xAF2042, 0x78028F, 0x4282E4, 0xA37801, 0x3A0006, 0xA3D804, 0x320002, 0xAF4761  /* 0x000DF0 B ...x..B.x...:.....2aG. */
    .pword      0xA26001, 0xA7C004, 0xA06001, 0x8802A1, 0xEB0080, 0xAFE761, 0x37FFFE, 0x370003  /* 0x000E00 .`.....`.......a....7..7 */
    .pword      0xE88000, 0xAF0042, 0xEC2054, 0xE90183, 0x390029, 0x781A82, 0x2000D1, 0x2FFFF6  /* 0x000E10 ...B..T ....).9..x.. ../ */
    .pword      0x200FF7, 0x784332, 0xE90183, 0x390005, 0x7848B2, 0xE90183, 0x390003, 0x7843B2  /* 0x000E20 .. 2Cx.....9.Hx.....9.Cx */
    .pword      0x370002, 0xA03004, 0xA02004, 0xBA0410, 0xBA8490, 0xBE9A88, 0xBE9A86, 0x620FE3  /* 0x000E30 ..7.0.. ...............b */
    .pword      0x32000B, 0xEA8306, 0xEAC387, 0x630508, 0x638589, 0xA70004, 0xBE030A, 0x730508  /* 0x000E40 ..2........c..c........s */
    .pword      0x738589, 0xA71004, 0xBE030A, 0xB84460, 0x530308, 0x5B8389, 0x620FEC, 0x320003  /* 0x000E50 ..s......`D...S..[..b..2 */
    .pword      0xEB0380, 0xA73004, 0xB20FF6, 0x5282EA, 0x730087, 0x32FFD2, 0x8002A6, 0x782286  /* 0x000E60 ....0......R..s..2...."x */
    .pword      0x782280, 0x780001, 0xF90054, 0xBE054F, 0xBE044F, 0x060000, 0xBE9F88, 0x781F8A  /* 0x000E70 ."x..xT..O..O..........x */
    .pword      0x780500, 0x200088, 0x200009, 0xB3C010, 0xE0000A, 0x340001, 0xEB4000, 0x600061  /* 0x000E80 ..x.. .. ........4.@.a.` */
    .pword      0xDD0045, 0x780088, 0xA15001, 0x700401, 0xA0A009, 0xA0C009, 0xA0F009, 0xA8C178  /* 0x000E90 E....x.P...p.........x.. */
    .pword      0xA9E09B, 0xA9C09B, 0xA9C08B, 0xA9E08B, 0xA9C178, 0x211B84, 0xEB0180, 0x200600  /* 0x000EA0 ............x....!.....  */
    .pword      0x200001, 0x780100, 0x2086E1, 0x2092C0, 0x0216F8, 0x000000, 0xEB0200, 0x2108C3  /* 0x000EB0 .. ..x.. .. ...........! */
    .pword      0x200200, 0x200001, 0x780100, 0x208F81, 0x209400, 0x0216F8, 0x000000, 0x2001E0  /* 0x000EC0 .. .. ..x.. .. ........  */
    .pword      0x8849E0, 0x2001F0, 0x884A80, 0x216420, 0x8849D0, 0x212600, 0x884A70, 0xA7F00A  /* 0x000ED0 .I... .J. d!.I..&!pJ.... */
    .pword      0xEA050A, 0x881DBA, 0x881D78, 0x881D89, 0xA0F008, 0x881D78, 0x881D89, 0x200011  /* 0x000EE0 ......x........x.......  */
    .pword      0x200040, 0xDD0800, 0x200021, 0x60C0EF, 0xB3CF02, 0x604002, 0x704001, 0xB7E0B7  /* 0x000EF0 @. ...!. ..`....@`.@p... */
    .pword      0x0217E6, 0x000000, 0x78054F, 0xBE044F, 0x060000, 0xBE9F88, 0x780480, 0xBE0202  /* 0x000F00 ......O.xO..........x... */
    .pword      0xEB8180, 0xE00009, 0x320038, 0x900889, 0xDE0848, 0x3A0035, 0x203988, 0x508FFD  /* 0x000F10 ......8.2...H..5.:.9 ..P */
    .pword      0x360011, 0x203AE8, 0x200510, 0x508F80, 0x36000D, 0x203C48, 0x400066, 0x508F80  /* 0x000F20 ..6.: .. ..P..6H< f.@..P */
    .pword      0x360009, 0x203D08, 0x400077, 0x508F80, 0x360005, 0x203E88, 0x400063, 0x508F80  /* 0x000F30 ..6.= w.@..P..6.> c.@..P */
    .pword      0x3E0001, 0x203DC8, 0xBE0104, 0x8042C0, 0x8042D1, 0x021440, 0x000000, 0x9001C8  /* 0x000F40 ..>.= ....B..B.@........ */
    .pword      0xE00000, 0x34000D, 0x980440, 0xA35818, 0x32000D, 0xA15018, 0xE00018, 0x3D000A  /* 0x000F50 .....4@...X...2.P......= */
    .pword      0xA1F018, 0xA15018, 0xA0F018, 0x900098, 0xA0A001, 0x980411, 0x370003, 0xEA0000  /* 0x000F60 ....P...............7... */
    .pword      0x980440, 0xA05018, 0x900899, 0xA0F001, 0x980C91, 0x900899, 0xA15001, 0x980C91  /* 0x000F70 @...P..............P.... */
    .pword      0x900899, 0xA16001, 0x980C91, 0x780003, 0xBE044F, 0x060000, 0xFE0000, 0xB82260  /* 0x000F80 ....`......xO........`". */
    .pword      0xA37801, 0x3A0006, 0xA3D804, 0x320002, 0xAF4761, 0xA26001, 0xA7C004, 0xA06001  /* 0x000F90 .x...:.....2aG..`.....`. */
    .pword      0xBE9F80, 0xBE9F82, 0xF80054, 0x8802A1, 0xE00003, 0x32002C, 0x200067, 0x539FE3  /* 0x000FA0 ......T........,.2g. ..S */
    .pword      0x207626, 0xE00006, 0x320004, 0x9FBFF7, 0xA71000, 0xA0F004, 0xA11000, 0xA10000  /* 0x000FB0 &v .....2............... */
    .pword      0xAFE761, 0x37FFFE, 0x97BBFF, 0xEB8080, 0xBAC090, 0xBA1F90, 0x609FCF, 0xE88000  /* 0x000FC0 a....7..............`... */
    .pword      0xE13866, 0x3A0005, 0xBAC090, 0x609FCF, 0xBA0090, 0x609FCF, 0xE88000, 0xE00000  /* 0x000FD0 f8...:.....`.....`...... */
    .pword      0xAF2042, 0xEC2054, 0xA7F004, 0x5383E3, 0xA1F004, 0xEA8FCF, 0x3A0004, 0x410107  /* 0x000FE0 B .T ......S........:..A */
    .pword      0x518187, 0x360007, 0x37FFE7, 0xEBD900, 0xE90183, 0x320003, 0xE90387, 0x3AFFFB  /* 0x000FF0 ..Q..6..7........2.....: */
    .pword      0x37FFE1, 0xE9878F, 0xF90054, 0xBE014F, 0xBE004F, 0x0408F2, 0x000000, 0xBFA86A  /* 0x001000 ..7...T..O..O........j.. */
    .pword      0x32003A, 0x97B8CF, 0xDE08CD, 0x3A0037, 0xAF6044, 0x3A0035, 0xAC086C, 0x3A0033  /* 0x001010 :.2......7.:D`.5.:l..3.: */
    .pword      0xF80036, 0xF80032, 0xF80034, 0xF80000, 0xBE9F84, 0xBE9F86, 0x804335, 0x804AC6  /* 0x001020 6..2..4...........5C..J. */
    .pword      0x804B37, 0x130295, 0x188337, 0x188397, 0xEF6042, 0x2086A4, 0x780184, 0x108214  /* 0x001030 7K....7.....B`... ..x... */
    .pword      0x32001B, 0x420062, 0xE12830, 0xE1B030, 0xE1B830, 0x33FFF8, 0xBE9F84, 0xBE9F86  /* 0x001040 ..2b.B0(.0..0....3...... */
    .pword      0x1082B0, 0x188330, 0x1883B0, 0xAF2042, 0x780994, 0x428A54, 0x4B0A54, 0x4B8A54  /* 0x001050 ...0.....B ...xT.BT.KT.K */
    .pword      0x2106C6, 0x200007, 0xBE9F86, 0x781FB0, 0x781FB0, 0x064000, 0xEF6042, 0xBE034F  /* 0x001060 ..!.. .....x..x.@.B`.O.. */
    .pword      0xBE024F, 0x980A10, 0xEB0080, 0x37FFE2, 0xBE034F, 0xBE024F, 0xF90000, 0xF90034  /* 0x001070 O..........7O..O.....4.. */
    .pword      0xF90032, 0xF90036, 0xA9086C, 0xBE014F, 0x7800CF, 0x064000, 0xBE9F88, 0xBE9F8A  /* 0x001080 2..6..l..O....x.@....... */
    .pword      0xBE9F8C, 0x780400, 0x780501, 0x780482, 0x780583, 0xAB23B1, 0x3A0001, 0xA8E08B  /* 0x001090 .....x..x..x..x.#...:... */
    .pword      0x540F89, 0x320021, 0x0215F2, 0x000000, 0xBE0600, 0xEB0000, 0x9805D0, 0xE2008A  /* 0x0010A0 ..T!.2.................. */
    .pword      0x3D0003, 0x203380, 0x0217C0, 0x000000, 0x548108, 0x450088, 0x78000B, 0x0212F4  /* 0x0010B0 ..=.3 ........T..E..x... */
    .pword      0x000000, 0x440400, 0x540F89, 0x32000D, 0x0215F2, 0x000000, 0x50010C, 0x58818D  /* 0x0010C0 .....D..T..2........P..X */
    .pword      0x8042C0, 0x8042D1, 0xDD0A4E, 0xDE0042, 0x720000, 0xDE08C2, 0x510F80, 0x598F81  /* 0x0010D0 .B..B.N..B....r.....Q..Y */
    .pword      0x39FFE6, 0x2108C0, 0x9805D0, 0x804A90, 0xA02000, 0x884A90, 0x780008, 0xBE064F  /* 0x0010E0 ..9..!....J.. ..J...xO.. */
    .pword      0xBE054F, 0xBE044F, 0x060000, 0xEB0280, 0xA7000F, 0xEB8280, 0x4081E0, 0xAF2042  /* 0x0010F0 O..O................@B . */
    .pword      0x050000, 0x781F85, 0xE81F80, 0x900003, 0xE10002, 0xAF0042, 0x780002, 0x900223  /* 0x001100 .....x.........B....x#.. */
    .pword      0x9003B3, 0x900343, 0x430307, 0xE900CF, 0x32001A, 0x530384, 0xE13800, 0xAF0042  /* 0x001110 ...C....C.....2..S.8.B.. */
    .pword      0x5003E0, 0x320003, 0xE90287, 0x098005, 0x7858B4, 0xE12006, 0xAF0042, 0x900243  /* 0x001120 ..P..2.......Xx. .B..C.. */
    .pword      0x500387, 0x320003, 0xE90287, 0x098005, 0x7858B4, 0x508080, 0xE0004F, 0x320002  /* 0x001130 ..P..2.......Xx..PO....2 */
    .pword      0x100993, 0x9801A4, 0x9003E3, 0xE00007, 0xAE2042, 0x010007, 0x060000, 0x420200  /* 0x001140 ............B .........B */
    .pword      0xE12006, 0xAF0042, 0x520207, 0x780100, 0x37FFF1, 0xDE00C8, 0x32002C, 0x800212  /* 0x001150 . .B....R..x..7...,.2... */
    .pword      0xDD1148, 0xAF6044, 0xA07002, 0x711FCF, 0xB80062, 0xB00140, 0x2FFFC2, 0xE00002  /* 0x001160 H..D`..p...qb..@..../... */
    .pword      0x330003, 0xAFE083, 0x400002, 0x37001C, 0xAE0083, 0x37001A, 0x2BF102, 0x2000A1  /* 0x001170 ..3.....@..7.....7..+..  */
    .pword      0x710181, 0x320016, 0x781F80, 0x2BF100, 0x0217B0, 0x000000, 0xEA9F80, 0x2BF000  /* 0x001180 ..q..2..x..+...........+ */
    .pword      0x2000A1, 0x0217B0, 0x000000, 0x780100, 0xE901CF, 0x78004F, 0xEB0080, 0xA6F002  /* 0x001190 .. ........x...O.x...... */
    .pword      0xA73002, 0x370006, 0x21FFF4, 0x618184, 0x208004, 0xB82103, 0x400002, 0x488083  /* 0x0011A0 .0...7..!..a.. .!...@..H */
    .pword      0x0217B0, 0x000000, 0xBE9F80, 0x060000, 0xBE9F88, 0xBE9F8A, 0xBE9F8C, 0x780400  /* 0x0011B0 .......................x */
    .pword      0x780501, 0x780482, 0x780583, 0x540F89, 0x32001D, 0x0215F2, 0x000000, 0xBE0600  /* 0x0011C0 ..x..x..x..T..2......... */
    .pword      0xEB0000, 0x9805E0, 0xABC08B, 0x320003, 0x2029E0, 0x0217C0, 0x000000, 0x548108  /* 0x0011D0 ...........2.) ........T */
    .pword      0x450008, 0x78008B, 0x0210FA, 0x000000, 0x440400, 0x548F88, 0x320009, 0x0215F2  /* 0x0011E0 ..E..x........D..T..2... */
    .pword      0x000000, 0x50010C, 0x58818D, 0x8042C0, 0x8042D1, 0x510F80, 0x598F81, 0x39FFEA  /* 0x0011F0 .....P..X.B..B...Q..Y..9 */
    .pword      0x211B80, 0x9805E0, 0x8049F0, 0xA03000, 0x8849F0, 0x780008, 0xBE064F, 0xBE054F  /* 0x001200 ..!....I..0..I...xO..O.. */
    .pword      0xBE044F, 0x060000, 0xEB1F80, 0x781F81, 0x781F80, 0x781F86, 0x781F85, 0x781F84  /* 0x001210 O..........x..x..x..x..x */
    .pword      0x021714, 0x000000, 0x0215F2, 0x000000, 0xEB0380, 0x53824F, 0x5B82CF, 0x5B834F  /* 0x001220 ...............O.S..[O.[ */
    .pword      0x7801CF, 0x320014, 0x2086A7, 0x781997, 0x33000B, 0x401984, 0x489985, 0x491986  /* 0x001230 ..x..2.. ..x..3..@..H..I */
    .pword      0xBE9984, 0x781986, 0x7819CF, 0x7819CF, 0x280000, 0x781980, 0x518BF4, 0x060000  /* 0x001240 .....x..x..x..(..x..Q... */
    .pword      0x501984, 0x589985, 0x591986, 0x090002, 0xEB1980, 0x37FFF4, 0x5787E4, 0x060000  /* 0x001250 ..P..X..Y........7..W... */
    .pword      0xBE9F88, 0x781F8A, 0xBE0400, 0x780502, 0x370008, 0x780008, 0x02176C, 0x000000  /* 0x001260 .....x.....x..7..xl..... */
    .pword      0xE00000, 0x320003, 0x900808, 0x02115A, 0x000000, 0xE00018, 0x32000F, 0x0215F2  /* 0x001270 .....2...Z..........2... */
    .pword      0x000000, 0x500139, 0x5881A9, 0x510FBA, 0x598FAA, 0x36FFEF, 0x370007, 0x0215F2  /* 0x001280 ...9.P..X..Q..Y..6..7... */
    .pword      0x000000, 0x500139, 0x5881A9, 0x510FBA, 0x598FAA, 0x3E0004, 0xAB03B1, 0x32FFF7  /* 0x001290 ...9.P..X..Q..Y..>.....2 */
    .pword      0x200010, 0x370001, 0xEB0000, 0x78054F, 0xBE044F, 0x060000, 0xE00004, 0x320021  /* 0x0012A0 .. ..7...O.xO........!.2 */
    .pword      0x780280, 0xB80862, 0x200036, 0x090011, 0xD88846, 0x410100, 0xB08003, 0xEB0000  /* 0x0012B0 ..xb..6. ...F....A...... */
    .pword      0x8002A7, 0x8802A3, 0xAFE761, 0x37FFFE, 0xE00001, 0x32000C, 0xE90204, 0xA70002  /* 0x0012C0 ......a....7.....2...... */
    .pword      0x370006, 0xBA5AD2, 0xE90204, 0x39000C, 0x370002, 0xBA5AB2, 0xBA5A92, 0xBADAC2  /* 0x0012D0 ..7.Z......9..7.Z..Z.... */
    .pword      0xE88102, 0xB4A054, 0x520263, 0x31FFF9, 0x420262, 0x330002, 0x098004, 0xBA5AB2  /* 0x0012E0 ...T..c.R..1b.B..3....Z. */
    .pword      0x8802A7, 0x060000, 0x4001E0, 0x780002, 0x320020, 0x9003C3, 0x900333, 0x900213  /* 0x0012F0 ........@..x .2...3..... */
    .pword      0x530293, 0x438306, 0x530384, 0xE12800, 0xAE0042, 0x780005, 0x780280, 0xE13805  /* 0x001300 ..S..C..S.(.B....x..x.8. */
    .pword      0xAE0042, 0x780287, 0xE90385, 0x390002, 0x098007, 0x785A31, 0xE12006, 0xAF0042  /* 0x001310 B....x.....9...1Zx. .B.. */
    .pword      0x900243, 0x500305, 0x320003, 0xE90386, 0x098007, 0x785A31, 0x400993, 0x980194  /* 0x001320 C....P..2......1Zx..@... */
    .pword      0x9003D3, 0x508080, 0xE00007, 0xAE2042, 0x010007, 0x060000, 0xE00000, 0xAF2042  /* 0x001330 .....P...B ..........B . */
    .pword      0x050010, 0x781F88, 0xBE9F82, 0x780400, 0x0215F2, 0x000000, 0xBE9F80, 0x9003F8  /* 0x001340 .....x.....x............ */
    .pword      0xE00007, 0x320007, 0x780008, 0x5780E4, 0x578168, 0x010007, 0x5787E8, 0x78044F  /* 0x001350 .....2..x..Wh.W.....WO.x */
    .pword      0x060000, 0x200010, 0xE00018, 0x32FFFA, 0x0215F2, 0x000000, 0x97B97F, 0x97B9EF  /* 0x001360 ..... .....2............ */
    .pword      0x500002, 0x588083, 0x97B95F, 0x97B9CF, 0xE10002, 0xE18803, 0x34FFF2, 0xEB0000  /* 0x001370 ..P..X_.............4... */
    .pword      0x37FFED, 0xEA8000, 0xEA8081, 0x200085, 0x283206, 0x2EDB87, 0x370010, 0x785AB2  /* 0x001380 ..7........ .2(.....7.Zx */
    .pword      0x7852B2, 0x680004, 0x200084, 0xD10081, 0xD38000, 0x390002, 0x680006, 0x688087  /* 0x001390 .Rx..h.. ........9..h..h */
    .pword      0xD10081, 0xD38000, 0x390002, 0x680006, 0x688087, 0xE90204, 0x3AFFF4, 0x5181E2  /* 0x0013A0 ........9..h..h.....:..Q */
    .pword      0x3BFFEE, 0x4181E2, 0x320004, 0x684012, 0x200044, 0x200003, 0x37FFEC, 0xEA8000  /* 0x0013B0 ..;..A..2.@hD. .. ..7... */
    .pword      0xEA8081, 0x060000, 0xFA0004, 0x781F88, 0xBE9F8A, 0xBE0300, 0x9FBFB0, 0x9FBFC1  /* 0x0013C0 ...........x............ */
    .pword      0x780402, 0xBE0504, 0xEB0000, 0x530FE0, 0x5B8FE0, 0x3A000B, 0x37000F, 0x57806A  /* 0x0013D0 ..x........S..[..:..7j.W */
    .pword      0x0216B4, 0x000000, 0x550F80, 0x5D8F81, 0x390003, 0x550F82, 0x5D8F83, 0x390005  /* 0x0013E0 ........U..]..9..U..]..9 */
    .pword      0x540464, 0xE00008, 0x3AFFF4, 0xEB0000, 0x370001, 0x200010, 0xBE054F, 0x78044F  /* 0x0013F0 d.T.....:.....7.. O..O.x */
    .pword      0xFA8000, 0x060000, 0xFE0000, 0xAFE761, 0x37FFFE, 0x883B00, 0x804327, 0xEF2864  /* 0x001400 .........a....7.;.'C.d(. */
    .pword      0xB83B01, 0x213261, 0xB009B7, 0x530081, 0x0217D6, 0x000000, 0x883B31, 0xEB0080  /* 0x001410 .;.a2!.....S......1;.... */
    .pword      0x883B37, 0xA8E761, 0x000000, 0x000000, 0xA60002, 0x0217F2, 0x000000, 0xEB0380  /* 0x001420 7;.a.................... */
    .pword      0xA30802, 0xAFE761, 0x3AFFFE, 0xA9C761, 0xA70002, 0x0217F2, 0x000000, 0x060000  /* 0x001430 ...a....:a.............. */
    .pword      0x781F88, 0xEB0200, 0x510FE0, 0x598FE0, 0x320014, 0xEB0400, 0x510FE0, 0x598FE0  /* 0x001440 ..x.....Q..Y..2.....Q..Y */
    .pword      0x3D0003, 0x110160, 0x1981E0, 0x200018, 0x400002, 0x488083, 0xDD1A42, 0xDE12CE  /* 0x001450 ..=`....... ..@..HB..... */
    .pword      0x720285, 0xDD1242, 0xBE0104, 0x0203AC, 0x000000, 0xE90200, 0xE00008, 0x320001  /* 0x001460 ..rB...................2 */
    .pword      0xEA0204, 0x780004, 0x78044F, 0x060000, 0x00085E, 0x000002, 0x000080, 0x000850  /* 0x001470 .....xO.x...^........P.. */
    .pword      0x000004, 0x000080, 0x000954, 0x000002, 0x000080, 0x000956, 0x000002, 0x000080  /* 0x001480 ......T........V........ */
    .pword      0x000860, 0x000002, 0x000080, 0x000862, 0x000002, 0x000080, 0x000864, 0x000002  /* 0x001490 `........b........d..... */
    .pword      0x000080, 0x00095A, 0x000002, 0x000080, 0x00095C, 0x000002, 0x000080, 0x000000  /* 0x0014A0 ...Z........\........... */
    .pword      0x2180A0, 0x200001, 0x50016A, 0x5881E0, 0x32000A, 0x0217B0, 0x000000, 0xDE0048  /* 0x0014B0 ..!.. j.P..X..2......H.. */
    .pword      0xDD08C8, 0x700001, 0x212CD2, 0x510000, 0xAE2042, 0x050000, 0x050010, 0x200000  /* 0x0014C0 .....p.,!..QB .........  */
    .pword      0x200001, 0xA06001, 0x0217B0, 0x000000, 0xDE014C, 0xDD09C4, 0x710103, 0xEA8000  /* 0x0014D0 .. .`.......L.......q... */
    .pword      0x20FFF4, 0x600004, 0x37FFF0, 0x200011, 0xA7000F, 0x200001, 0x200982, 0x370007  /* 0x0014E0 .. ..`..7.. ..... .. ..7 */
    .pword      0x200011, 0xA7000F, 0x200001, 0xE00000, 0xAF2042, 0x050000, 0x200882, 0x900980  /* 0x0014F0 .. ..... ...B ...... ... */
    .pword      0xDE1848, 0xAE2042, 0x050000, 0xDE1A43, 0xA10004, 0x410104, 0x6181EF, 0xA51812  /* 0x001500 H..B ....C.......A..a... */
    .pword      0xD28000, 0x4080E0, 0xAD9812, 0x060000, 0xEB0000, 0x884AC0, 0x209600, 0x884B30  /* 0x001510 .....@..........J... 0K. */
    .pword      0x201900, 0x884330, 0x2086A0, 0xE00000, 0x320002, 0xEF286A, 0xEF686C, 0x200040  /* 0x001520 .. 0C... .....2j(.lh.@.  */
    .pword      0x880CA0, 0xEFA192, 0x200021, 0xDD0880, 0xBFC0A9, 0x60406F, 0x704001, 0xB7E0A9  /* 0x001530 ......!. ......o@`.@p... */
    .pword      0xA86098, 0xA8E195, 0x060000, 0xEB0100, 0xB82260, 0xE00001, 0x320011, 0xE91FB1  /* 0x001540 .`..........`"......2... */
    .pword      0x33000E, 0x20FFF3, 0x780331, 0xDE33CC, 0x630303, 0x430302, 0xA5A800, 0xA53816  /* 0x001550 ..3.. 1.x.3...c..C....8. */
    .pword      0xAD2804, 0xAA0042, 0xAE2042, 0xAD3816, 0xE80285, 0xE91FCF, 0x3BFFF3, 0xE9878F  /* 0x001560 .(.B..B ..8.........;... */
    .pword      0x780004, 0x060000, 0x841F32, 0x841F43, 0x510FE0, 0x598FE0, 0x3C0005, 0x8042E0  /* 0x001570 ..x...2..C....Q..Y..<.B. */
    .pword      0x900890, 0xB20601, 0x320001, 0x07FCC1, 0xE20860, 0x320008, 0x208CE0, 0x07F735  /* 0x001580 ........2...`....2.. 5.. */
    .pword      0x8042A2, 0x8042B3, 0xD10183, 0xD38102, 0x209180, 0x070079, 0xEB0000, 0x060000  /* 0x001590 .B..B......... y........ */
    .pword      0xFE0000, 0xAFE761, 0x37FFFE, 0x8002A2, 0x8802A1, 0xBB0803, 0x8802A2, 0x883B10  /* 0x0015A0 ...a....7.............;. */
    .pword      0x883B21, 0x240420, 0xA8C760, 0xAEC760, 0xB28410, 0xEB0100, 0x203E51, 0x021406  /* 0x0015B0 !;. .$`..`........Q> ... */
    .pword      0x000000, 0xAFE761, 0x37FFFE, 0xA9C761, 0x060000, 0xBE9F88, 0xBE9F8A, 0xBE0400  /* 0x0015C0 ...a....7a.............. */
    .pword      0x0215F2, 0x000000, 0xBE0500, 0xEB0180, 0xE00003, 0xAE2042, 0xFE4001, 0x0215F2  /* 0x0015D0 ...............B ..@.... */
    .pword      0x000000, 0x50020A, 0x58828B, 0x540104, 0x5C8185, 0x31FFF6, 0xBE054F, 0xBE044F  /* 0x0015E0 .....P..X..T..\..1O..O.. */
    .pword      0x060000, 0x804337, 0xEB0300, 0x209665, 0x804AC0, 0x780215, 0x100017, 0x1B00B4  /* 0x0015F0 ...7C....e. .J...x...... */
    .pword      0x1B0134, 0x1B0194, 0x520264, 0xE12015, 0x3AFFF7, 0x200884, 0xB00004, 0xA63014  /* 0x001600 4.....d.R. ...:.. ....0. */
    .pword      0x060000, 0x2161A0, 0x0217C0, 0x000000, 0x37FFEC, 0x781F81, 0xBE9F82, 0xEB0180  /* 0x001610 ....a!........7..x...... */
    .pword      0x804B31, 0x688168, 0xE81931, 0x499931, 0x498911, 0x510164, 0x884B32, 0x200881  /* 0x001620 1K.h.h1..1.I..Id.Q2K...  */
    .pword      0xB00001, 0xA13011, 0x2100E1, 0xE00001, 0xAE2042, 0x014001, 0xBE014F, 0x7800CF  /* 0x001630 ....0...!...B ..@.O....x */
    .pword      0x064000, 0x781F88, 0x780400, 0x780118, 0x780088, 0xEB0000, 0x0210FA, 0x000000  /* 0x001640 .@...x..x..x..x......... */
    .pword      0x900818, 0xA14000, 0x980C10, 0x900818, 0xA15000, 0x980C10, 0x900818, 0xA16000  /* 0x001650 ....@........P........`. */
    .pword      0x980C10, 0x200010, 0x78044F, 0x060000, 0x8002A2, 0x8802A1, 0x207FF3, 0xEA8083  /* 0x001660 ..... O.x........... ... */
    .pword      0x608000, 0xEB8200, 0xAFE761, 0x37FFFE, 0xBAC210, 0xBA00B0, 0x620201, 0xEA8084  /* 0x001670 ..`...a....7........b... */
    .pword      0x3A0004, 0xE98183, 0x31FFF9, 0x8802A2, 0x050010, 0x8802A2, 0x050000, 0x781F83  /* 0x001680 ..:.....1..............x */
    .pword      0x781F82, 0x781F80, 0x0215F2, 0x000000, 0xE883CF, 0xAC086C, 0x800216, 0x780237  /* 0x001690 ..x..x.........l.....7.x */
    .pword      0x7802A7, 0x401BCF, 0x489BCF, 0x4913E0, 0xA71006, 0xA9086C, 0x520047, 0x5A80D7  /* 0x0016A0 ..x..@..H..I...l..G.R..Z */
    .pword      0x060000, 0xFFFFFF, 0x780230, 0x7802A0, 0x8002A7, 0x421864, 0x4A8860, 0x8802A5  /* 0x0016B0 ......0.x..x...d.B`.J... */
    .pword      0xE88304, 0x4A82E0, 0xAFE761, 0x37FFFE, 0xBA0014, 0xBA8094, 0x8802A5, 0xBA0116  /* 0x0016C0 .....Ja....7............ */
    .pword      0xBA8196, 0x8802A7, 0x060000, 0x000000, 0xE90202, 0x390005, 0x700181, 0xA60003  /* 0x0016D0 .................9..p... */
    .pword      0x370003, 0x098004, 0x785831, 0x060000, 0xD10182, 0xE90183, 0x390003, 0x098003  /* 0x0016E0 ..7...1Xx...........9... */
    .pword      0x781831, 0xA70002, 0x785831, 0x060000, 0xEB0280, 0x780805, 0x980032, 0x980041  /* 0x0016F0 1.x...1Xx........x2..A.. */
    .pword      0x980011, 0x980021, 0x980053, 0x980064, 0x980075, 0xEB8080, 0x980801, 0x280001  /* 0x001700 ...!..S..d..u..........( */
    .pword      0x980811, 0x060000, 0x2086A1, 0x370001, 0x780082, 0x780111, 0xE00002, 0x320004  /* 0x001710 ........ ..7..x..x.....2 */
    .pword      0x510F80, 0x3AFFFA, 0x780890, 0x050010, 0x050000, 0xAFE761, 0x37FFFE, 0xA9C761  /* 0x001720 ..Q..:..x......a....7a.. */
    .pword      0xEFA762, 0xEFA764, 0x8002A1, 0xEFA054, 0x2FFFE2, 0xBB0902, 0x8802A1, 0x060000  /* 0x001730 b..d.....T..../......... */
    .pword      0x07FEB7, 0xE00000, 0x320006, 0xFC0102, 0x209560, 0xEB8800, 0x02180C, 0x000000  /* 0x001740 ........2...`. ......... */
    .pword      0xFE0000, 0x060000, 0x7841B0, 0x51C1B1, 0x3A0003, 0xE90102, 0x31FFFB, 0x050000  /* 0x001750 .......Ax..Q..:.....1... */
    .pword      0xEB8000, 0xAF0042, 0x050010, 0x060000, 0x200981, 0xA7000F, 0x200881, 0x900900  /* 0x001760 ...B.......... ..... ... */
    .pword      0xDE11C3, 0x610167, 0x79C061, 0xDE0002, 0x600061, 0x060000, 0x208660, 0xE00000  /* 0x001770 ...g.aa.y...a.`...`. ... */
    .pword      0x320004, 0x200980, 0xB00000, 0xA43010, 0xAE0042, 0x050000, 0x050010, 0x781F80  /* 0x001780 ..2.. ....0.B..........x */
    .pword      0xEB0000, 0x0217EC, 0x000000, 0x60004F, 0x0217EC, 0x000000, 0xEA8000, 0x060000  /* 0x001790 .........O.`............ */
    .pword      0x8042F5, 0x200016, 0xE00005, 0x320002, 0x010005, 0x780300, 0x780006, 0x060000  /* 0x0017A0 .B... .....2.....x..x... */
    .pword      0x8002A2, 0x8802A1, 0xAFE761, 0x37FFFE, 0xBA8090, 0xBA0010, 0x8802A2, 0x060000  /* 0x0017B0 ......a....7............ */
    .pword      0x800211, 0xDD08C8, 0xAF6044, 0xA07001, 0x709FCF, 0x014000, 0xE90102, 0x390002  /* 0x0017C0 ......D`..p...p.@......9 */
    .pword      0x098002, 0x785801, 0x060000, 0x200800, 0x701FCF, 0x800210, 0x064000, 0xB80063  /* 0x0017D0 ....Xx..... ..p....@.c.. */
    .pword      0xD10081, 0xD38000, 0x060000, 0xA8C09B, 0xA8E09B, 0x060000, 0x283D41, 0x041546  /* 0x0017E0 ..................A=(F.. */
    .pword      0x000000, 0xDD0048, 0x701FCF, 0x064000, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0017F0 ...H....p.@............. */
    .popsection

    ; Bootloader code block intended for program region 'FSEC'
    ; 0x0ABF00 to 0x0ABF04, length 0x000004 (6 bytes; needs 0 pages)
    .pushsection    EZBL_BTLDR_CONFIG_WORD_FSEC, address(0x0ABF00), code, keep
    .pword      0x00FFFF, 0xFFFFFF                                                              /* 0x0ABF00 ......                   */
    .popsection

    ; Bootloader code block intended for program region 'FOSCSEL'
    ; 0x0ABF18 to 0x0ABF1C, length 0x000004 (6 bytes; needs 0 pages)
    .pushsection    EZBL_BTLDR_CONFIG_WORD_FOSCSEL, address(0x0ABF18), code, keep
    .pword      0x00FF08, 0xFFFFFF                                                              /* 0x0ABF18 ......                   */
    .popsection

    ; Bootloader code block intended for program region 'FOSC'
    ; 0x0ABF1C to 0x0ABF20, length 0x000004 (6 bytes; needs 0 pages)
    .pushsection    EZBL_BTLDR_CONFIG_WORD_FOSC, address(0x0ABF1C), code, keep
    .pword      0x00FF19, 0xFFFFFF                                                              /* 0x0ABF1C ......                   */
    .popsection

    ; Bootloader code block intended for program region 'FWDT'
    ; 0x0ABF20 to 0x0ABF24, length 0x000004 (6 bytes; needs 0 pages)
    .pushsection    EZBL_BTLDR_CONFIG_WORD_FWDT, address(0x0ABF20), code, keep
    .pword      0x00F7CA, 0xFFFFFF                                                              /* 0x0ABF20 ......                   */
    .popsection

    ; Bootloader code block intended for program region 'FPOR'
    ; 0x0ABF24 to 0x0ABF28, length 0x000004 (6 bytes; needs 0 pages)
    .pushsection    EZBL_BTLDR_CONFIG_WORD_FPOR, address(0x0ABF24), code, keep
    .pword      0x00FFFB, 0xFFFFFF                                                              /* 0x0ABF24 ......                   */
    .popsection

    ; Bootloader code block intended for program region 'FICD'
    ; 0x0ABF28 to 0x0ABF2C, length 0x000004 (6 bytes; needs 0 pages)
    .pushsection    EZBL_BTLDR_CONFIG_WORD_FICD, address(0x0ABF28), code, keep
    .pword      0x007FDE, 0xFFFFFF                                                              /* 0x0ABF28 ......                   */
    .popsection

    ; Bootloader code block intended for program region 'FDEVOPT1'
    ; 0x0ABF2C to 0x0ABF30, length 0x000004 (6 bytes; needs 0 pages)
    .pushsection    EZBL_BTLDR_CONFIG_WORD_FDEVOPT1, address(0x0ABF2C), code, keep
    .pword      0x00FFEF, 0xFFFFFF                                                              /* 0x0ABF2C ......                   */
    .popsection


#if !defined(EZBL_HIDE_BOOT_SYMBOLS)

;----Bootloader symbol export table----
    .weak       _EZBL_appIsRunning
    .weak       _EZBL_AppPreInstall
    .type       _EZBL_AppPreInstall, @object
    .weak       _EZBL_bootCtx
    .type       _EZBL_bootCtx, @object
    .weak       _EZBL_bootTask
    .type       _EZBL_bootTask, @object
    .weak       _EZBL_COM_RX
    .weak       _EZBL_COM_TX
    .weak       _EZBL_COMBootIF
    .type       _EZBL_COMBootIF, @object
    .global     _EZBL_ForwardBootloaderISR
    .weak       _EZBL_NVMKey
    .weak       _EZBL_STDIN
    .weak       _EZBL_STDOUT
    .weak       _NOW_dispatcherActive
    .weak       _NOW_Fcy
    .weak       _NOW_microsecond
    .type       _NOW_microsecond, @object
    .weak       _NOW_millisecond
    .type       _NOW_millisecond, @object
    .weak       _NOW_ms
    .weak       _NOW_sec
    .weak       _NOW_second
    .type       _NOW_second, @object
    .weak       _NOW_systemFrequency
    .type       _NOW_systemFrequency, @object
    .weak       _NOW_timeOffset
    .weak       _NOW_timerCallbackHead
    .weak       _NOW_timerSFRPtr
    .weak       _NOW_us
    .weak       _UART_RxFifo
    .type       _UART_RxFifo, @object
    .weak       _UART_RxFifoBuffer
    .type       _UART_RxFifoBuffer, @object
    .weak       _UART_TxFifo
    .type       _UART_TxFifo, @object
    .weak       _UART_TxFifoBuffer
    .type       _UART_TxFifoBuffer, @object
    .weak       _UART_wakeKeyIndex
    .type       _UART_wakeKeyIndex, @object
    .weak       _UART2_RxFifo
    .weak       _UART2_RxFifoBuffer
    .type       _UART2_RxFifoBuffer, @object
    .weak       _UART2_TxFifo
    .weak       _UART2_TxFifoBuffer
    .type       _UART2_TxFifoBuffer, @object
    .weak       _UART2_wakeKeyIndex
    .type       _UART2_wakeKeyIndex, @object
    .weak       _NOW_internalCount
    .type       _COM_RX_FIFO_Flush, @function
    .weak       _COM_RX_FIFO_Flush
    .weak       _EZBL_AddrInTable
    .type       _EZBL_BootloaderTask, @function
    .weak       _EZBL_BootloaderTask
    .weak       _EZBL_CRC32
    .weak       _EZBL_CalcUxBRGH
    .weak       _EZBL_CallIRQ
    .weak       _EZBL_CallISR
    .weak       _EZBL_DisableInterrupts
    .weak       _EZBL_EraseAppSpace
    .weak       _EZBL_ErasePage
    .weak       _EZBL_FIFOFlush
    .weak       _EZBL_FIFOGetInt
    .type       _EZBL_FIFOGetIntEn, @function
    .weak       _EZBL_FIFOGetIntEn
    .type       _EZBL_FIFOIntClear, @function
    .weak       _EZBL_FIFOIntClear
    .type       _EZBL_FIFOIntDisable, @function
    .weak       _EZBL_FIFOIntDisable
    .weak       _EZBL_FIFOIntEnable
    .type       _EZBL_FIFOIntEnableSet, @function
    .weak       _EZBL_FIFOIntEnableSet
    .type       _EZBL_FIFOIntFlagSet, @function
    .weak       _EZBL_FIFOIntFlagSet
    .type       _EZBL_FIFOIntRaise, @function
    .weak       _EZBL_FIFOIntRaise
    .weak       _EZBL_FIFOPeek
    .weak       _EZBL_FIFORead
    .weak       _EZBL_FIFOReset
    .weak       _EZBL_FIFOSetBaud
    .weak       _EZBL_FIFOWrite
    .weak       _EZBL_Install2Flash
    .weak       _EZBL_IsAppPresent
    .weak       _EZBL_IsPageErased
    .weak       _EZBL_MapInv
    .type       _EZBL_MapInvEx, @function
    .weak       _EZBL_MapInvEx
    .weak       _EZBL_Mul3Div2
    .type       _EZBL_PreInstall, @function
    .weak       _EZBL_PreInstall
    .weak       _EZBL_RAMCompare
    .weak       _EZBL_RAMCopy
    .type       _EZBL_RAMCopyEx, @function
    .weak       _EZBL_RAMCopyEx
    .weak       _EZBL_RAMSet
    .type       _EZBL_RAMSetEx, @function
    .weak       _EZBL_RAMSetEx
    .type       _EZBL_ROMCompare, @function
    .weak       _EZBL_ROMCompare
    .weak       _EZBL_ROMCompareEx
    .weak       _EZBL_ReadFlash
    .type       _EZBL_ReadFlashInstruction, @function
    .weak       _EZBL_ReadFlashInstruction
    .weak       _EZBL_ReadPackedFlash
    .weak       _EZBL_ReadTablePair
    .weak       _EZBL_RestoreAppErasable
    .weak       _EZBL_RestoreInterrupts
    .weak       _EZBL_VerifyROM
    .weak       _EZBL_WaitForNVMOpComplete
    .weak       _EZBL_WriteNVMCON
    .type       _EZBL_WriteROM, @function
    .weak       _EZBL_WriteROM
    .weak       _EZBL_WriteROMEx
    .type       _EZBL_WriteROMOnlyOnce, @function
    .weak       _EZBL_WriteROMOnlyOnce
    .weak       _EZBL_WriteROMOnlyOnceEx
    .weak       _EZBL_read
    .weak       _EZBL_write
    .type       _I2C1_RX_FIFO_Flush, @function
    .weak       _I2C1_RX_FIFO_Flush
    .type       _I2C2_RX_FIFO_Flush, @function
    .weak       _I2C2_RX_FIFO_Flush
    .type       _I2C3_RX_FIFO_Flush, @function
    .weak       _I2C3_RX_FIFO_Flush
    .type       _I2C_RX_FIFO_Flush, @function
    .weak       _I2C_RX_FIFO_Flush
    .type       _InitializeBoard, @function
    .weak       _InitializeBoard
    .type       _LEDClr, @function
    .weak       _LEDClr
    .type       _LEDInv, @function
    .weak       _LEDInv
    .weak       _LEDOff
    .weak       _LEDToggle
    .weak       _NOW_32
    .weak       _NOW_64
    .weak       _NOW_CreateTask
    .type       _NOW_EnableInterrupts, @function
    .weak       _NOW_EnableInterrupts
    .weak       _NOW_EndTask
    .weak       _NOW_ISR
    .weak       _NOW_IntEnable
    .weak       _NOW_SetNextTaskTime
    .weak       _NOW_TMR1_ResetConst
    .weak       _NOW_TaskDispatcher
    .weak       _NOW_Wait
    .type       _UART1_RX_FIFO_Flush, @function
    .weak       _UART1_RX_FIFO_Flush
    .type       _UART2_FIFO_EnableInterrupts, @function
    .weak       _UART2_FIFO_EnableInterrupts
    .weak       _UART2_RX_FIFO_Flush
    .type       _UART2_RX_FIFO_OnRead, @function
    .weak       _UART2_RX_FIFO_OnRead
    .weak       _UART2_ResetBRG
    .type       _UART2_TX_FIFO_Flush, @function
    .weak       _UART2_TX_FIFO_Flush
    .type       _UART2_TX_FIFO_OnWrite, @function
    .weak       _UART2_TX_FIFO_OnWrite
    .type       _UART3_RX_FIFO_Flush, @function
    .weak       _UART3_RX_FIFO_Flush
    .type       _UART4_RX_FIFO_Flush, @function
    .weak       _UART4_RX_FIFO_Flush
    .type       _UART5_RX_FIFO_Flush, @function
    .weak       _UART5_RX_FIFO_Flush
    .type       _UART6_RX_FIFO_Flush, @function
    .weak       _UART6_RX_FIFO_Flush
    .type       _UART_FIFO_EnableInterrupts, @function
    .weak       _UART_FIFO_EnableInterrupts
    .type       _UART_RX_FIFO_Flush, @function
    .weak       _UART_RX_FIFO_Flush
    .type       _UART_RX_FIFO_OnRead, @function
    .weak       _UART_RX_FIFO_OnRead
    .type       _UART_ResetBRG, @function
    .weak       _UART_ResetBRG
    .type       _UART_TX_FIFO_Flush, @function
    .weak       _UART_TX_FIFO_Flush
    .type       _UART_TX_FIFO_OnWrite, @function
    .weak       _UART_TX_FIFO_OnWrite
    .weak       __T1Interrupt
    .type       __U2RXInterrupt, @function
    .weak       __U2RXInterrupt
    .type       __U2TXInterrupt, @function
    .weak       __U2TXInterrupt
    .type       __URXInterrupt, @function
    .weak       __URXInterrupt
    .type       __UTXInterrupt, @function
    .weak       __UTXInterrupt
    .weak       ___udivsi3
    .type       _main, @function
    .weak       _main
    .type       _read, @function
    .weak       _read
    .type       _write, @function
    .weak       _write
    .weak       __FSEC
    .weak       ___FSEC
    .weak       __FOSCSEL
    .weak       ___FOSCSEL
    .weak       __FOSC
    .weak       ___FOSC
    .weak       __FWDT
    .weak       ___FWDT
    .weak       __FPOR
    .weak       ___FPOR
    .weak       __FICD
    .weak       ___FICD
    .weak       __FDEVOPT1
    .weak       ___FDEVOPT1
    .weak       _EZBL_ADDRESSES_PER_SECTOR
    .weak       _EZBL_APP_RESET_BASE
    .weak       _EZBL_appBootloadState
    .weak       _EZBL_BACKBUG_ADDRESS
    .weak       _EZBL_BACKBUG_MASK
    .weak       _EZBL_bootIDHash
    .weak       _EZBL_bootIDHash0
    .weak       _EZBL_bootIDHash1
    .weak       _EZBL_bootIDHash2
    .weak       _EZBL_bootIDHash3
    .weak       _EZBL_BOOTLOADER_SIZE
    .weak       _EZBL_ButtonMap
    .weak       _EZBL_ButtonMapLen
    .weak       _EZBL_CODE_PROTECT_ADDRESS
    .weak       _EZBL_CODE_PROTECT_MASK
    .weak       _EZBL_COMBaud
    .type       _EZBL_COMBaud, @object
    .weak       _EZBL_CONFIG_BASE
    .weak       _EZBL_CONFIG_END_ADDRESS
    .weak       _EZBL_DEVID_ADDRESS
    .weak       _EZBL_DEVID_MASK
    .weak       _EZBL_DEVID_VALUE
    .weak       _EZBL_FBOOT
    .weak       _EZBL_FBSLIM
    .weak       _EZBL_FDEVOPT1
    .weak       _EZBL_FICD
    .weak       _EZBL_FLOW_THRESHOLD
    .weak       _EZBL_FORWARD_IRQ_T1
    .weak       _EZBL_FORWARD_IRQ_U2RX
    .weak       _EZBL_FORWARD_IRQ_U2TX
    .weak       _EZBL_FORWARD_MASK_T1
    .weak       _EZBL_FORWARD_MASK_U2RX
    .weak       _EZBL_FORWARD_MASK_U2TX
    .weak       _EZBL_FORWARD_POS_T1
    .weak       _EZBL_FORWARD_POS_U2RX
    .weak       _EZBL_FORWARD_POS_U2TX
    .weak       _EZBL_FOSC
    .weak       _EZBL_FOSCSEL
    .weak       _EZBL_FPOR
    .weak       _EZBL_FSEC
    .weak       _EZBL_FSIGN
    .weak       _EZBL_FWDT
    .weak       _EZBL_IGT_BASE
    .weak       _EZBL_LEDMap
    .weak       _EZBL_LEDMapLen
    .weak       _EZBL_MAIN_FLASH_BASE
    .weak       _EZBL_MAIN_FLASH_END_ADDRESS
    .weak       _EZBL_RESERVED_BIT_ADDRESS
    .weak       _EZBL_RESERVED_BIT_MASK
    .weak       _EZBL_REVID_ADDRESS
    .weak       _EZBL_REVID_MASK
    .weak       EZBL_Dispatch_T1Interrupt
    .weak       EZBL_Dispatch_U2RXInterrupt
    .weak       EZBL_Dispatch_U2TXInterrupt



;----Bootloader symbol addresses----
    .equ        _EZBL_appIsRunning, 0x000956
    .equ        _EZBL_AppPreInstall, 0x00085E
    .equ        _EZBL_bootCtx, 0x0008CE
    .equ        _EZBL_bootTask, 0x000918
    .equ        _EZBL_COM_RX, 0x000860
    .equ        _EZBL_COM_TX, 0x000862
    .equ        _EZBL_COMBootIF, 0x00085C
    .equ        _EZBL_ForwardBootloaderISR, 0x000850
    .equ        _EZBL_NVMKey, 0x000864
    .equ        _EZBL_STDIN, 0x00095C
    .equ        _EZBL_STDOUT, 0x00095A
    .equ        _NOW_dispatcherActive, 0x00086C
    .equ        _NOW_Fcy, 0x000858
    .equ        _NOW_microsecond, 0x000868
    .equ        _NOW_millisecond, 0x000854
    .equ        _NOW_ms, 0x000854
    .equ        _NOW_sec, 0x000858
    .equ        _NOW_second, 0x000858
    .equ        _NOW_systemFrequency, 0x000858
    .equ        _NOW_timeOffset, 0x000958
    .equ        _NOW_timerCallbackHead, 0x00086A
    .equ        _NOW_timerSFRPtr, 0x000866
    .equ        _NOW_us, 0x000868
    .equ        _UART_RxFifo, 0x00092C
    .equ        _UART_RxFifoBuffer, 0x00086E
    .equ        _UART_TxFifo, 0x000940
    .equ        _UART_TxFifoBuffer, 0x0008F8
    .equ        _UART_wakeKeyIndex, 0x000954
    .equ        _UART2_RxFifo, 0x00092C
    .equ        _UART2_RxFifoBuffer, 0x00086E
    .equ        _UART2_TxFifo, 0x000940
    .equ        _UART2_TxFifoBuffer, 0x0008F8
    .equ        _UART2_wakeKeyIndex, 0x000954
    .equ        _NOW_internalCount, 0x000960
    .equ        _COM_RX_FIFO_Flush, 0x001642
    .equ        _EZBL_AddrInTable, 0x0013C4
    .equ        _EZBL_BootloaderTask, 0x001574
    .equ        _EZBL_CRC32, 0x001382
    .equ        _EZBL_CalcUxBRGH, 0x001440
    .equ        _EZBL_CallIRQ, 0x00115A
    .equ        _EZBL_CallISR, 0x0017C0
    .equ        _EZBL_DisableInterrupts, 0x0017D6
    .equ        _EZBL_EraseAppSpace, 0x000A3C
    .equ        _EZBL_ErasePage, 0x0015A2
    .equ        _EZBL_FIFOFlush, 0x00133C
    .equ        _EZBL_FIFOGetInt, 0x00176C
    .equ        _EZBL_FIFOGetIntEn, 0x001768
    .equ        _EZBL_FIFOIntClear, 0x0014F4
    .equ        _EZBL_FIFOIntDisable, 0x0014EA
    .equ        _EZBL_FIFOIntEnable, 0x0014E6
    .equ        _EZBL_FIFOIntEnableSet, 0x0014EC
    .equ        _EZBL_FIFOIntFlagSet, 0x0014F6
    .equ        _EZBL_FIFOIntRaise, 0x0014F0
    .equ        _EZBL_FIFOPeek, 0x0010F6
    .equ        _EZBL_FIFORead, 0x0010FA
    .equ        _EZBL_FIFOReset, 0x0016F8
    .equ        _EZBL_FIFOSetBaud, 0x000F0A
    .equ        _EZBL_FIFOWrite, 0x0012F4
    .equ        _EZBL_Install2Flash, 0x0003FA
    .equ        _EZBL_IsAppPresent, 0x0014B0
    .equ        _EZBL_IsPageErased, 0x001668
    .equ        _EZBL_MapInv, 0x001546
    .equ        _EZBL_MapInvEx, 0x001548
    .equ        _EZBL_Mul3Div2, 0x0017DE
    .equ        _EZBL_PreInstall, 0x0017A0
    .equ        _EZBL_RAMCompare, 0x00175A
    .equ        _EZBL_RAMCopy, 0x0016D8
    .equ        _EZBL_RAMCopyEx, 0x0016D8
    .equ        _EZBL_RAMSet, 0x0017CC
    .equ        _EZBL_RAMSetEx, 0x0017CC
    .equ        _EZBL_ROMCompare, 0x000DE6
    .equ        _EZBL_ROMCompareEx, 0x000DE8
    .equ        _EZBL_ReadFlash, 0x0017B0
    .equ        _EZBL_ReadFlashInstruction, 0x0017B0
    .equ        _EZBL_ReadPackedFlash, 0x0012AC
    .equ        _EZBL_ReadTablePair, 0x0016B4
    .equ        _EZBL_RestoreAppErasable, 0x000D40
    .equ        _EZBL_RestoreInterrupts, 0x0017F2
    .equ        _EZBL_VerifyROM, 0x000C90
    .equ        _EZBL_WaitForNVMOpComplete, 0x00172A
    .equ        _EZBL_WriteNVMCON, 0x001406
    .equ        _EZBL_WriteROM, 0x0008F0
    .equ        _EZBL_WriteROMEx, 0x0008F2
    .equ        _EZBL_WriteROMOnlyOnce, 0x000F8E
    .equ        _EZBL_WriteROMOnlyOnceEx, 0x000F90
    .equ        _EZBL_read, 0x00039A
    .equ        _EZBL_write, 0x000370
    .equ        _I2C1_RX_FIFO_Flush, 0x001642
    .equ        _I2C2_RX_FIFO_Flush, 0x001642
    .equ        _I2C3_RX_FIFO_Flush, 0x001642
    .equ        _I2C_RX_FIFO_Flush, 0x001642
    .equ        _InitializeBoard, 0x000B1C
    .equ        _LEDClr, 0x00178E
    .equ        _LEDInv, 0x0017EC
    .equ        _LEDOff, 0x00178E
    .equ        _LEDToggle, 0x0017EC
    .equ        _NOW_32, 0x0015F2
    .equ        _NOW_64, 0x0015F2
    .equ        _NOW_CreateTask, 0x001214
    .equ        _NOW_EnableInterrupts, 0x00177C
    .equ        _NOW_EndTask, 0x001714
    .equ        _NOW_ISR, 0x00161A
    .equ        _NOW_IntEnable, 0x00177C
    .equ        _NOW_SetNextTaskTime, 0x00168E
    .equ        _NOW_TMR1_ResetConst, 0x001518
    .equ        _NOW_TaskDispatcher, 0x00100E
    .equ        _NOW_Wait, 0x0015CA
    .equ        _UART1_RX_FIFO_Flush, 0x001642
    .equ        _UART2_FIFO_EnableInterrupts, 0x0017E6
    .equ        _UART2_RX_FIFO_Flush, 0x001642
    .equ        _UART2_RX_FIFO_OnRead, 0x0011B8
    .equ        _UART2_ResetBRG, 0x000E7C
    .equ        _UART2_TX_FIFO_Flush, 0x001260
    .equ        _UART2_TX_FIFO_OnWrite, 0x00108C
    .equ        _UART3_RX_FIFO_Flush, 0x001642
    .equ        _UART4_RX_FIFO_Flush, 0x001642
    .equ        _UART5_RX_FIFO_Flush, 0x001642
    .equ        _UART6_RX_FIFO_Flush, 0x001642
    .equ        _UART_FIFO_EnableInterrupts, 0x0017E6
    .equ        _UART_RX_FIFO_Flush, 0x001642
    .equ        _UART_RX_FIFO_OnRead, 0x0011B8
    .equ        _UART_ResetBRG, 0x000E7C
    .equ        _UART_TX_FIFO_Flush, 0x001260
    .equ        _UART_TX_FIFO_OnWrite, 0x00108C
    .equ        __T1Interrupt, 0x00161A
    .equ        __U2RXInterrupt, 0x00029E
    .equ        __U2TXInterrupt, 0x000338
    .equ        __URXInterrupt, 0x00029E
    .equ        __UTXInterrupt, 0x000338
    .equ        ___udivsi3, 0x0003AC
    .equ        _main, 0x000BDE
    .equ        _read, 0x00039A
    .equ        _write, 0x000370
    .equ        __FSEC, 0x0ABF00
    .equ        ___FSEC, 0x0ABF00
    .equ        __FOSCSEL, 0x0ABF18
    .equ        ___FOSCSEL, 0x0ABF18
    .equ        __FOSC, 0x0ABF1C
    .equ        ___FOSC, 0x0ABF1C
    .equ        __FWDT, 0x0ABF20
    .equ        ___FWDT, 0x0ABF20
    .equ        __FPOR, 0x0ABF24
    .equ        ___FPOR, 0x0ABF24
    .equ        __FICD, 0x0ABF28
    .equ        ___FICD, 0x0ABF28
    .equ        __FDEVOPT1, 0x0ABF2C
    .equ        ___FDEVOPT1, 0x0ABF2C
    .equ        _EZBL_ADDRESSES_PER_SECTOR, 0x000800
    .equ        _EZBL_APP_RESET_BASE, 0x00180C
    .equ        _EZBL_appBootloadState, 0x001800
    .equ        _EZBL_BACKBUG_ADDRESS, 0x0ABF28
    .equ        _EZBL_BACKBUG_MASK, 0x000080
    .equ        _EZBL_bootIDHash, 0x0083EA
    .equ        _EZBL_bootIDHash0, 0x0083EA
    .equ        _EZBL_bootIDHash1, 0x0083EE
    .equ        _EZBL_bootIDHash2, 0x0083F2
    .equ        _EZBL_bootIDHash3, 0x0083F6
    .equ        _EZBL_BOOTLOADER_SIZE, 0x001800
    .equ        _EZBL_ButtonMap, 0x0083CC
    .equ        _EZBL_ButtonMapLen, 0x000003
    .equ        _EZBL_CODE_PROTECT_ADDRESS, 0x0ABF00
    .equ        _EZBL_CODE_PROTECT_MASK, 0x000E00
    .equ        _EZBL_COMBaud, 0x0083E6
    .equ        _EZBL_CONFIG_BASE, 0x0ABF00
    .equ        _EZBL_CONFIG_END_ADDRESS, 0x0AC000
    .equ        _EZBL_DEVID_ADDRESS, 0xFF0000
    .equ        _EZBL_DEVID_MASK, 0xFFFFFF
    .equ        _EZBL_DEVID_VALUE, 0x00601D
    .equ        _EZBL_FBOOT, 0x801800
    .equ        _EZBL_FBSLIM, 0x0ABF10
    .equ        _EZBL_FDEVOPT1, 0x0ABF2C
    .equ        _EZBL_FICD, 0x0ABF28
    .equ        _EZBL_FLOW_THRESHOLD, 0x000020
    .equ        _EZBL_FORWARD_IRQ_T1, 0x000003
    .equ        _EZBL_FORWARD_IRQ_U2RX, 0x00001E
    .equ        _EZBL_FORWARD_IRQ_U2TX, 0x00001F
    .equ        _EZBL_FORWARD_MASK_T1, 0x000001
    .equ        _EZBL_FORWARD_MASK_U2RX, 0x000002
    .equ        _EZBL_FORWARD_MASK_U2TX, 0x000004
    .equ        _EZBL_FORWARD_POS_T1, 0x000000
    .equ        _EZBL_FORWARD_POS_U2RX, 0x000001
    .equ        _EZBL_FORWARD_POS_U2TX, 0x000002
    .equ        _EZBL_FOSC, 0x0ABF1C
    .equ        _EZBL_FOSCSEL, 0x0ABF18
    .equ        _EZBL_FPOR, 0x0ABF24
    .equ        _EZBL_FSEC, 0x0ABF00
    .equ        _EZBL_FSIGN, 0x0ABF14
    .equ        _EZBL_FWDT, 0x0ABF20
    .equ        _EZBL_IGT_BASE, 0x001810
    .equ        _EZBL_LEDMap, 0x0083D4
    .equ        _EZBL_LEDMapLen, 0x000008
    .equ        _EZBL_MAIN_FLASH_BASE, 0x000000
    .equ        _EZBL_MAIN_FLASH_END_ADDRESS, 0x0AC000
    .equ        _EZBL_RESERVED_BIT_ADDRESS, 0x0ABF14
    .equ        _EZBL_RESERVED_BIT_MASK, 0x008000
    .equ        _EZBL_REVID_ADDRESS, 0xFF0002
    .equ        _EZBL_REVID_MASK, 0xFFFFFF
    .equ        EZBL_Dispatch_T1Interrupt, 0x000150
    .equ        EZBL_Dispatch_U2RXInterrupt, 0x00015A
    .equ        EZBL_Dispatch_U2TXInterrupt, 0x000164

#endif
