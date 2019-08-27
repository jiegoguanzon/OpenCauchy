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
 *     dist\i2c\production\ex_boot_i2c.production.elf
 * Using build configuration:
 *     i2c
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
    .error "Bootloader's ex_boot_i2c.merge.S/.gld files were generated for a different target processor."
    .error "Recompile this Application project or the ex_boot_i2c Bootloader project for the same hardware."
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
    ; 0x0850 to 0x0976, length 0x0126 (294 bytes)
    .pushsection    EZBL_RAM_AT_0x0850, address(0x0850), persist, keep
    .space      0x126
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
    .pword      0x040164, 0x000000                                                              /* 0x000000 d.....                   */
    .popsection

    ; Bootloader code block intended for program region 'ivt'
    ; 0x000004 to 0x000100, length 0x0000FC (378 bytes; needs 0 pages)
    .pushsection    EZBL_ROM_AT_0x000004, address(0x000004), code, keep
    .pword      0x001810, 0x001814, 0x001818, 0x00181C, 0x001820, 0x0019AC, 0x001824, 0x0019AC  /* 0x000004 ............ .....$..... */
    .pword      0x001828, 0x00182C, 0x001830, 0x000150, 0x001838, 0x00183C, 0x001840, 0x001844  /* 0x000014 (..,..0..P..8..<..@..D.. */
    .pword      0x001848, 0x00184C, 0x001850, 0x001854, 0x001858, 0x00185C, 0x001860, 0x001864  /* 0x000024 H..L..P..T..X..\..`..d.. */
    .pword      0x00015A, 0x00186C, 0x001870, 0x001874, 0x001878, 0x0019AC, 0x00187C, 0x001880  /* 0x000034 Z..l..p..t..x.....|..... */
    .pword      0x001884, 0x001888, 0x00188C, 0x001890, 0x001894, 0x001898, 0x00189C, 0x0018A0  /* 0x000044 ........................ */
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
    .pword      0xAF0850, 0x041834, 0x000000, 0x0413E8, 0x000000, 0xAF2850, 0x041868, 0x000000  /* 0x000150 P..4...........P(.h..... */
    .pword      0x040288, 0x000000, 0x20976F, 0x27FF0E, 0x88010E, 0x000000, 0x200000, 0xE00000  /* 0x000160 ......o. ..'........ ... */
    .pword      0x320002, 0x200100, 0x880220, 0x070014, 0x07010C, 0xE00000, 0x3A0003, 0x213420  /* 0x000170 ..2..  .............: 4! */
    .pword      0x200001, 0x370002, 0x200000, 0x200001, 0x700F81, 0x320001, 0x070014, 0x200000  /* 0x000180 .. ..7.. .. ..p..2.....  */
    .pword      0xE00000, 0x320002, 0x020000, 0x000000, 0x020CDA, 0x000000, 0xDA4000, 0xFE0000  /* 0x000190 .....2.............@.... */
    .pword      0xA94044, 0x200300, 0xE00000, 0x320003, 0x202000, 0x880190, 0xA84044, 0x060000  /* 0x0001A0 D@... .....2.  ...D@.... */
    .pword      0x781F8A, 0x781F8B, 0x060000, 0x8802A1, 0x780480, 0xEB0000, 0x37001F, 0x4484E2  /* 0x0001B0 ..x..x........x.....7..D */
    .pword      0xB4A054, 0xBA0599, 0x4484E2, 0xB4A054, 0xBA0699, 0x4484E2, 0xB4A054, 0xEB0600  /* 0x0001C0 T.......DT.......DT..... */
    .pword      0xDE6F47, 0xB207FD, 0x8801AE, 0xE16C63, 0x3A0003, 0x07FFEA, 0xEB0000, 0x37000E  /* 0x0001D0 Go.......cl...:........7 */
    .pword      0xE16C60, 0x3A0008, 0xEB4D00, 0xE8050A, 0x390002, 0xEC2034, 0xA0F00A, 0xE9058B  /* 0x0001E0 `l...:.M......94 ....... */
    .pword      0x3EFFF9, 0x370004, 0xE16861, 0x320001, 0xEB8600, 0x07000B, 0xBAC719, 0xBA0519  /* 0x0001F0 ..>..7ah...2............ */
    .pword      0xE0040E, 0x3A0003, 0xE0000A, 0x3AFFDB, 0x060000, 0xB207FE, 0x8802AE, 0x78048A  /* 0x000200 .....:.....:...........x */
    .pword      0x37FFF5, 0xA3D00C, 0x4D0FE2, 0x39001C, 0x780289, 0xBA4D35, 0xE8050A, 0x390002  /* 0x000210 ..7.....M..9..x5M......9 */
    .pword      0xEC2034, 0xA0F00A, 0xE9058B, 0x32000F, 0xBA4D25, 0xE8050A, 0x390002, 0xEC2034  /* 0x000220 4 .........2%M......94 . */
    .pword      0xA0F00A, 0xE9058B, 0x320008, 0xE0000C, 0x320006, 0xBACD15, 0xE8050A, 0x390002  /* 0x000230 ........2.....2........9 */
    .pword      0xEC2034, 0xA0F00A, 0xE9058B, 0xE88489, 0xB4A054, 0xE0000B, 0x3AFFE2, 0x060000  /* 0x000240 4 ..........T.......:... */
    .pword      0xBA0299, 0x785D05, 0xE9058B, 0x32FFF7, 0xDE2AC8, 0x785D05, 0xE9058B, 0x32FFF3  /* 0x000250 ....]x.....2.*..]x.....2 */
    .pword      0xE0000C, 0x32FFF1, 0xBADD19, 0x37FFEE, 0x0414E0, 0x000000, 0x041584, 0x000000  /* 0x000260 .....2.....7............ */
    .pword      0x0413E8, 0x000000, 0x0415CA, 0x000000, 0x0415D0, 0x000000, 0x04109E, 0x000000  /* 0x000270 ........................ */
    .pword      0x041410, 0x000000, 0x040F12, 0x000000, 0xBE9F80, 0xBE9F82, 0x781F84, 0xFA0008  /* 0x000280 ....................x... */
    .pword      0xA9008A, 0x8024F0, 0xA31800, 0x32003E, 0x8024A3, 0x780100, 0xB20242, 0x200201  /* 0x000290 ....$....>.2.$...xB....  */
    .pword      0x510F81, 0x3A0038, 0x8049B1, 0xA01001, 0x8849B1, 0x804921, 0x2092A4, 0x508F94  /* 0x0002A0 ..Q8.:.I.....I.!I... ..P */
    .pword      0x390004, 0x8049B1, 0xA04001, 0x8849B1, 0x37000B, 0x804931, 0x785883, 0x884931  /* 0x0002B0 ..9.I..@..I...71I..Xx1I. */
    .pword      0x804962, 0x410214, 0x508F84, 0x390001, 0x884932, 0x200012, 0x209241, 0x410891  /* 0x0002C0 bI...A..P..92I... A. ..A */
    .pword      0x2434D1, 0x9FBFC1, 0x250551, 0x9FBFD1, 0x243481, 0x9FBFE1, 0xB02051, 0x9FBFF1  /* 0x0002D0 .4$...Q.%....4$...Q .... */
    .pword      0x804AE2, 0x610167, 0x784083, 0x578268, 0x7941E4, 0x50CF83, 0x3A000C, 0xE80082  /* 0x0002E0 .J.g.a.@xh.W.Ay..P..:... */
    .pword      0x884AE1, 0x508FE8, 0x3A000F, 0x8049B1, 0xA07001, 0x8849B1, 0x209241, 0x884301  /* 0x0002F0 .J...P..:.I..p..I.A. .C. */
    .pword      0x209381, 0x884311, 0x370007, 0xEB0100, 0x884AE2, 0x97F90F, 0x50CF82, 0x3A0002  /* 0x000300 .. .C...7....J......P..: */
    .pword      0x200011, 0x884AE1, 0x6000E5, 0x508FE4, 0x3A0032, 0xA35800, 0x3A000C, 0x8049C0  /* 0x000310 .. .J...`..P2.:.X...:.I. */
    .pword      0x884AF0, 0xDE0048, 0x320002, 0x200FF0, 0x884AF0, 0x804AF4, 0x8824B4, 0x804A50  /* 0x000320 .J.H....2.. .J..J..$.PJ. */
    .pword      0xA0F000, 0x884A50, 0x370024, 0xE00000, 0x350022, 0x804AF0, 0xE00000, 0x3A000C  /* 0x000330 ...PJ.$.7...".5.J......: */
    .pword      0x8049C0, 0x884AF0, 0xDE0048, 0x320002, 0x200FF0, 0x884AF0, 0x804AF0, 0x8824B0  /* 0x000340 .I..J.H....2.. .J..J..$. */
    .pword      0x804A50, 0xA0F000, 0x884A50, 0x370013, 0x8049E0, 0xFB80B0, 0x8824B1, 0x8849E0  /* 0x000350 PJ....PJ...7.I.....$..I. */
    .pword      0x804A01, 0x2093E2, 0x408112, 0x500F82, 0x390001, 0x8849E1, 0x200011, 0x209380  /* 0x000360 .J... ..@..P..9.I... ..  */
    .pword      0x108810, 0x804A50, 0xA00000, 0x884A50, 0x804AF0, 0xE90000, 0x884AF0, 0x804B80  /* 0x000370 ...PJ....PJ..J.....J..K. */
    .pword      0x210001, 0x608090, 0x3A0001, 0xA0C010, 0xFA8000, 0x78024F, 0xBE014F, 0xBE004F  /* 0x000380 ..!..`..:......O.xO..O.. */
    .pword      0x064000, 0x050000, 0x000003, 0x00D6A0, 0x0076A0, 0x0066A0, 0x000008, 0x000666  /* 0x000390 .@...........v..f....f.. */
    .pword      0x001666, 0x002666, 0x003666, 0x004666, 0x005666, 0x006666, 0x007666, 0x007C00  /* 0x0003A0 f..f&.f6.fF.fV.ff.fv..|. */
    .pword      0x00FFFC, 0x00C793, 0x00FE07, 0x0077D8, 0x0057F8, 0x00AEBD, 0x00CD24, 0x00ADED  /* 0x0003B0 ..........w..W....$..... */
    .pword      0x004854, 0x000060, 0xB005CF, 0xBE9F88, 0xBE9F8A, 0xBE9F8C, 0x781F8E, 0x780500  /* 0x0003C0 TH.`................x..x */
    .pword      0x804300, 0xE00000, 0x3A0002, 0x780D00, 0x370269, 0x218002, 0x200003, 0x9F8FD2  /* 0x0003D0 .C......:..xi.7..!.. ... */
    .pword      0x9F8FE3, 0xBE0402, 0xEB0580, 0x804301, 0x780711, 0x78011A, 0xEB0180, 0x510FE8  /* 0x0003E0 ..........C...x..x.....Q */
    .pword      0x598FE0, 0x3E0254, 0x016002, 0x370008, 0x370033, 0x3700BF, 0x3700E5, 0x37011A  /* 0x0003F0 ..YT.>.`...73.7..7..7..7 */
    .pword      0x370177, 0x3701A7, 0x3701D0, 0x3701EB, 0x2002F0, 0x570F80, 0x36024E, 0x200302  /* 0x000400 w.7..7..7..7.. ..WN.6..  */
    .pword      0x2FFBE0, 0x40000F, 0x020F90, 0x000000, 0x2FFBE0, 0x40000F, 0xEB0600, 0x784130  /* 0x000410 ../..@......../..@...0Ax */
    .pword      0xB3C421, 0x514F81, 0x3A0011, 0x904090, 0xB2C321, 0xB3C4C3, 0x69C190, 0x70C083  /* 0x000420 !...OQ..:.@.!.......i..p */
    .pword      0x9041A0, 0x69C102, 0x70C082, 0x3A0008, 0x804311, 0x780111, 0xEB0000, 0x020F94  /* 0x000430 .A...i..p..:.C...x...... */
    .pword      0x000000, 0x20001D, 0x780D0D, 0x370005, 0xE8060C, 0x2002C1, 0x560F81, 0x3AFFE7  /* 0x000440 ..... ..x..7..... ..V..: */
    .pword      0xEB0680, 0x78010C, 0x804301, 0xEB0000, 0x020F94, 0x000000, 0x200013, 0x3701DE  /* 0x000450 .....x.C............ ..7 */
    .pword      0xEB0180, 0xEB0680, 0x2002F0, 0x570F80, 0x3601D9, 0x200302, 0x2FFBE0, 0x40000F  /* 0x000460 ........ ..W..6.. ../..@ */
    .pword      0x020F94, 0x000000, 0x200303, 0x2FFBE2, 0x41010F, 0xB80060, 0x0211C0, 0x000000  /* 0x000470 ........ ../..A`........ */
    .pword      0x980D40, 0x980D51, 0x97A01F, 0x97A0AF, 0xB10280, 0xB18001, 0x980D20, 0x980D31  /* 0x000480 @..Q.............. ..1.. */
    .pword      0x20001C, 0x98154C, 0x2FFBE0, 0x40000F, 0x4000E8, 0x200102, 0x283B20, 0x02153E  /* 0x000490 .. L..../..@..@..  ;(>.. */
    .pword      0x000000, 0xE00000, 0x320006, 0x2FFEA0, 0x981540, 0x200080, 0x780D00, 0x78018C  /* 0x0004A0 ........2../@.... ..x..x */
    .pword      0x3701B5, 0x4506E4, 0x97A83F, 0x97A8CF, 0x97A95F, 0x97A9EF, 0xBE9E80, 0xBE9682  /* 0x0004B0 ..7..E?....._........... */
    .pword      0x200124, 0xEB0080, 0xBE0108, 0x2FFAC0, 0x40000F, 0x0210EA, 0x000000, 0x200000  /* 0x0004C0 $. ......../..@........  */
    .pword      0xE00000, 0x320004, 0x212CD0, 0x9798EF, 0x508F80, 0x320004, 0x2FFDE0, 0x40000F  /* 0x0004D0 .....2.,!.....P..2../..@ */
    .pword      0x578174, 0x370014, 0x90022A, 0x9002BA, 0x90034A, 0x9003DA, 0x97906F, 0x9790FF  /* 0x0004E0 t.W..7*.....J.....o..... */
    .pword      0x97990F, 0x97999F, 0xE12000, 0xE1A801, 0xE1B002, 0xE1B803, 0x31FFEF, 0x2FFE90  /* 0x0004F0 ....... ............1../ */
    .pword      0x981540, 0x200070, 0x780D00, 0x78018C, 0x780683, 0x370188, 0x97A8FF, 0x7080D0  /* 0x000500 @..p. ..x..x..x..7.....p */
    .pword      0x9FAFF1, 0x500F82, 0x3AFFFB, 0xE00001, 0x3A00E9, 0x215840, 0xE00000, 0x32000D  /* 0x000510 .....P..:.....:@X!.....2 */
    .pword      0x90092A, 0x9009BA, 0xB00402, 0xB08003, 0x78020D, 0x804311, 0x804300, 0x021584  /* 0x000520 *.............x.C..C.... */
    .pword      0x000000, 0xE00000, 0x3C0002, 0x2FFE50, 0x3700FF, 0x804B9B, 0xE0000B, 0x320012  /* 0x000530 ........<P./..7.K......2 */
    .pword      0x200980, 0x200A82, 0x510100, 0xEB0080, 0x0215B0, 0x000000, 0xEF2850, 0xEF2852  /* 0x000540 .. .. ..Q.........P(.R(. */
    .pword      0xEB0000, 0x884B90, 0x021560, 0x000000, 0x804300, 0x0212B4, 0x000000, 0x804310  /* 0x000550 ....K.`......C........C. */
    .pword      0x0212B4, 0x000000, 0xEB0000, 0x981530, 0xB81160, 0x980D62, 0x980D73, 0xEB8000  /* 0x000560 .........0..`..b..s..... */
    .pword      0x981510, 0x200080, 0x980510, 0x200020, 0x780D00, 0x804310, 0xE00010, 0x3A0007  /* 0x000570 ..... ... . ..x.C......: */
    .pword      0x2FF131, 0x9F8FC1, 0x200022, 0x2FF981, 0x40808F, 0x021132, 0x000000, 0xE0000B  /* 0x000580 1./...". ../..@2........ */
    .pword      0x320001, 0xFE6000, 0x90001A, 0xB40864, 0xB13E70, 0x9008EA, 0x400001, 0x884320  /* 0x000590 ..2.`....d..p>......@ C. */
    .pword      0x200260, 0x40000A, 0x020A06, 0x000000, 0x780600, 0x200013, 0xEB0680, 0xE0000C  /* 0x0005A0 `. ..@........x.. ...... */
    .pword      0x3A0135, 0x2FF110, 0x9F8FC0, 0x200022, 0x2FF981, 0x40808F, 0x804310, 0x021132  /* 0x0005B0 5.:../...". ../..@.C.2.. */
    .pword      0x000000, 0x804300, 0x780710, 0x98152C, 0x200040, 0x780D00, 0x90082A, 0x9008BA  /* 0x0005C0 ....C...x,..@. ..x*..... */
    .pword      0x200243, 0x500F83, 0x588FE0, 0x3E0006, 0x200240, 0x981530, 0x200060, 0x980510  /* 0x0005D0 C. ..P..X..>@. 0..`. ... */
    .pword      0x200013, 0x370029, 0x90096A, 0x9009FA, 0x510FE0, 0x598FE0, 0x3A0006, 0x200080  /* 0x0005E0 .. ).7j.......Q..Y..:..  */
    .pword      0x981530, 0x200030, 0x980510, 0x200013, 0x37001E, 0x203DF0, 0xB42864, 0x200120  /* 0x0005F0 0..0. ..... ..7.= d(. .  */
    .pword      0x981530, 0x570F80, 0x360001, 0x98153E, 0x9010BA, 0x200300, 0x508F80, 0x360001  /* 0x000600 0....W..6>....... ..P..6 */
    .pword      0x981530, 0x90133A, 0xB83261, 0x520F82, 0x5A8F83, 0x390003, 0x90086A, 0x981530  /* 0x000610 0..:..a2...R..Z..9j..0.. */
    .pword      0x370005, 0x200062, 0x090011, 0xD88302, 0x530001, 0x981530, 0x200050, 0x980510  /* 0x000620 ..7b. ........S0..P. ... */
    .pword      0x200013, 0x370001, 0xEB0180, 0x90602A, 0x404FE8, 0x3A000B, 0x90102A, 0x900A2A  /* 0x000630 .. ..7...*`..O@..:*..*.. */
    .pword      0x900ABA, 0xB80361, 0x530F84, 0x5B8F85, 0x360001, 0x780004, 0xEB0680, 0x500F8E  /* 0x000640 ...a....S..[..6..x.....P */
    .pword      0x3A00E5, 0x200782, 0xE00002, 0x320021, 0x804310, 0x9000B0, 0x508090, 0x508FE5  /* 0x000650 ..:.. ...!.2.C......P..P */
    .pword      0x36001C, 0x804301, 0x9000B1, 0x510F81, 0x360001, 0x780101, 0x90122A, 0x508084  /* 0x000660 ..6.C......Q..6..x*....P */
    .pword      0x508F82, 0x310003, 0x90113A, 0x510F84, 0x360010, 0x50CFF3, 0x3A0001, 0xE90081  /* 0x000670 ..P..1:....Q..6..P..:... */
    .pword      0xDE0948, 0x514FF3, 0x3A0001, 0xB11001, 0x408204, 0x981524, 0x9F8FC1, 0x200022  /* 0x000680 H...OQ..:.....@$.....".  */
    .pword      0x2FF981, 0x40808F, 0x021132, 0x000000, 0x200013, 0x90113A, 0xEB0680, 0x570F82  /* 0x000690 ../..@2....... :.......W */
    .pword      0x3900BD, 0x90102A, 0x500002, 0x981520, 0x900E2A, 0x900EBA, 0x804301, 0x2FFBE0  /* 0x0006A0 ..9*....P ..*......C.../ */
    .pword      0x40000F, 0x020F94, 0x000000, 0x560600, 0x5E86E0, 0x980D2C, 0x980D3D, 0x9011BA  /* 0x0006B0 ..@........V..^,..=..... */
    .pword      0x2FFBE2, 0x41010F, 0x90084A, 0x9008DA, 0x0211C0, 0x000000, 0x980D40, 0x980D51  /* 0x0006C0 ../..AJ...........@..Q.. */
    .pword      0x90001A, 0x780D00, 0x500FE3, 0x3A00A0, 0x45007C, 0x90113A, 0x2FFBE1, 0x40808F  /* 0x0006D0 .....x..P..:|.E:..../..@ */
    .pword      0x0214A4, 0x000000, 0x9058FA, 0x90603A, 0x70C000, 0x320096, 0x2FFEB0, 0x370024  /* 0x0006E0 .......X.:`...p..2../$.7 */
    .pword      0xE0000B, 0x320001, 0xFE6000, 0xEB0300, 0xB82260, 0x9011BA, 0x2FFBE2, 0x41010F  /* 0x0006F0 .....2.`....`"....../..A */
    .pword      0x90100A, 0x90109A, 0x0208BC, 0x000000, 0xBE0600, 0x2FF9E4, 0x42020F, 0x9011BA  /* 0x000700 ................./..B... */
    .pword      0x2FFBE2, 0x41010F, 0x90100A, 0x90109A, 0x020AE6, 0x000000, 0x98150C, 0x98151D  /* 0x000710 ../..A.................. */
    .pword      0x9010BA, 0x90096A, 0x9009FA, 0x510101, 0x5981E0, 0x980D62, 0x980D73, 0x200031  /* 0x000720 ...j.......Q..Yb..s..1.  */
    .pword      0x780D01, 0xE00000, 0x3D0007, 0x2FFE80, 0x981540, 0x200070, 0x780D00, 0x200013  /* 0x000730 ..x.....=../@..p. ..x..  */
    .pword      0x780683, 0x37006C, 0xE00000, 0x320068, 0x2FFE70, 0x981540, 0x200013, 0x780683  /* 0x000740 ..xl.7...h.2p./@.... ..x */
    .pword      0x370065, 0x203DF0, 0xB42864, 0x90094A, 0x9009DA, 0x2DF1C0, 0x221441, 0x510F80  /* 0x000750 e.7.= d(.J.......-A."..Q */
    .pword      0x598F81, 0x3A001E, 0x212CD0, 0x9F9FE0, 0x90002A, 0x9000BA, 0x90014A, 0x9001DA  /* 0x000760 ..Y..:.,!...*.....J..... */
    .pword      0x9F97E0, 0x9F97F1, 0x9F9F82, 0x9F9F93, 0x97A85F, 0x97A8EF, 0x97A97F, 0x97B18F  /* 0x000770 ............_........... */
    .pword      0x9F9FA0, 0x9F9FB1, 0x9F9FC2, 0x9F9FD3, 0xEB0300, 0x2DEAD5, 0xEB0200, 0x200123  /* 0x000780 .................-...#.  */
    .pword      0x2FFAC2, 0x41010F, 0xBE0008, 0x0208BC, 0x000000, 0x02150E, 0x000000, 0x370002  /* 0x000790 ../..A.................7 */
    .pword      0x2FFE60, 0x981540, 0xEF2860, 0xEF2864, 0x200080, 0x780D00, 0x90104A, 0xDE80CF  /* 0x0007A0 `./@..`(.d(... ..xJ..... */
    .pword      0xDD00C0, 0x200000, 0x9F8FD0, 0x9F8FE1, 0x200042, 0x2FF9A1, 0x40808F, 0x804310  /* 0x0007B0 ..... ......B. ../..@.C. */
    .pword      0x021132, 0x000000, 0x8042A2, 0x8042B3, 0x804310, 0x02117A, 0x000000, 0x200FA0  /* 0x0007C0 2......B..B..C.z.......  */
    .pword      0x804341, 0xB80800, 0x021398, 0x000000, 0x200013, 0x780683, 0x37001F, 0xEB0180  /* 0x0007D0 AC............ ..x..7... */
    .pword      0xEB0680, 0xE0000E, 0x32001B, 0x90092A, 0x9009BA, 0xB87261, 0x520F82, 0x5A8F83  /* 0x0007E0 ........2*.....ar...R..Z */
    .pword      0x390005, 0x780702, 0xEB0000, 0x020F94, 0x000000, 0xEF2860, 0x78010E, 0x804301  /* 0x0007F0 ..9..x.........`(...x.C. */
    .pword      0xEB0000, 0x020F94, 0x000000, 0x90082A, 0x9008BA, 0x50000E, 0x5880E0, 0x980D20  /* 0x000800 .........*.......P..X .. */
    .pword      0x980D31, 0x200013, 0xEB0680, 0x370002, 0x200013, 0x780683, 0x9FB7F3, 0x0213C0  /* 0x000810 1.... .....7.. ..x...... */
    .pword      0x000000, 0xBE0200, 0x97B1FF, 0xE00003, 0x320002, 0x980D00, 0x980D11, 0x90090A  /* 0x000820 ..............2......... */
    .pword      0x90099A, 0x520102, 0x5A8183, 0x90006A, 0x9000FA, 0x510F80, 0x598F81, 0x360021  /* 0x000830 .....R..Zj.......Q..Y!.6 */
    .pword      0x2FFEC0, 0x981540, 0xE9801A, 0x500FE3, 0x3E0018, 0x200000, 0x2FFEC1, 0x9F8FD0  /* 0x000840 ../@.......P..>.. ../... */
    .pword      0x9F8FE1, 0x200042, 0x2FF9A1, 0x40808F, 0x804310, 0x9FB7F4, 0x9FBF85, 0x021132  /* 0x000850 ...B. ../..@.C.......2.. */
    .pword      0x000000, 0x8042A2, 0x8042B3, 0x804310, 0x02117A, 0x000000, 0xEB8100, 0x804301  /* 0x000860 ....B..B..C.z.........C. */
    .pword      0xEB0000, 0x020F94, 0x000000, 0x97BA8F, 0x97B27F, 0x980D04, 0x980D15, 0xEB0D00  /* 0x000870 ........................ */
    .pword      0xEF2860, 0x804300, 0xE00000, 0x320003, 0x76868B, 0x3AFDAD, 0x370009, 0xE0000B  /* 0x000880 `(..C......2..v..:..7... */
    .pword      0x320006, 0x90016A, 0x9001FA, 0x804310, 0x02117A, 0x000000, 0xFE0000, 0xEB0D00  /* 0x000890 ..2j......C.z........... */
    .pword      0xA7F000, 0xEA0000, 0xE90000, 0xDE004F, 0x370001, 0xEB0000, 0x78074F, 0xBE064F  /* 0x0008A0 .........O....7...O.xO.. */
    .pword      0xBE054F, 0xBE044F, 0xB105CF, 0x060000, 0xFE0000, 0xB82260, 0xE00003, 0x3200A1  /* 0x0008B0 O..O...........`"......2 */
    .pword      0xDE0BC8, 0x3A009F, 0x203E57, 0x781F87, 0xF80054, 0xBE9F88, 0xBE9F8A, 0xBE9F8C  /* 0x0008C0 .....:W> ..xT........... */
    .pword      0xA37801, 0x3A0006, 0xA3D804, 0x320002, 0xAF4761, 0xA26001, 0xA7C004, 0xA06001  /* 0x0008D0 .x...:.....2aG..`.....`. */
    .pword      0x24003B, 0x20002D, 0xA0F004, 0x20762C, 0xE0000C, 0x320006, 0xA1F004, 0x24000B  /* 0x0008E0 ;.$-. ...,v .....2.....$ */
    .pword      0x40CFE8, 0x320002, 0x20004D, 0x24001B, 0xFA0006, 0xD0060D, 0x47878C, 0x78038E  /* 0x0008F0 ..@..2M. ..$........G..x */
    .pword      0x781B8B, 0xBE9B80, 0x78040F, 0xEB0480, 0x2DEADC, 0xE1280C, 0x320011, 0xDD2AC2  /* 0x000900 ..x.....x.....-.(...2.*. */
    .pword      0x320003, 0xE90285, 0x098005, 0x781FB6, 0x200006, 0x8802A6, 0x2010C5, 0x2000C6  /* 0x000910 ..2........x.. ..... ..  */
    .pword      0xAFE761, 0x37FFFE, 0x370002, 0xBA1F95, 0xBA9FB5, 0xE98306, 0x31FFFC, 0x578488  /* 0x000920 a....7..7...........1..W */
    .pword      0xF80864, 0xEF2864, 0x370055, 0x78058F, 0xE9060D, 0x09800C, 0xEB9F80, 0xE90003  /* 0x000930 d..d(.U.7..x............ */
    .pword      0x098000, 0x785DB2, 0x558103, 0xEB0180, 0xD0008D, 0xA7000F, 0xEB0080, 0xD1000D  /* 0x000940 ....]x..U............... */
    .pword      0x090002, 0x785BB2, 0xEBDB80, 0xE90000, 0x3AFFFB, 0x578781, 0xE9060D, 0x09800C  /* 0x000950 ....[x........:..W...... */
    .pword      0x480FC7, 0x31003A, 0x7805C7, 0x780547, 0x45008D, 0x4D8660, 0x540364, 0x780389  /* 0x000960 ..H:.1..xG.x..E`.Md.T..x */
    .pword      0x430364, 0x5383E8, 0x390007, 0xE10836, 0xE1E036, 0x34FFFA, 0xE15036, 0xE1D836  /* 0x000970 d.C..S..96..6....46P.6.. */
    .pword      0x31FFF8, 0x37002A, 0x200000, 0x200FA1, 0x470362, 0xA7F004, 0xBE0036, 0x8802A1  /* 0x000980 ..1*.7.. .. b.G...6..... */
    .pword      0xD1008D, 0x78038E, 0x207606, 0xAFE761, 0x37FFFE, 0x090002, 0x781B37, 0xBB0837  /* 0x000990 .....x.v a....7...7.x7.. */
    .pword      0xBB9837, 0xE90081, 0x3AFFFC, 0x21326A, 0x97B0FE, 0x97B87F, 0xB80001, 0x50038A  /* 0x0009A0 7.......:j2!...........P */
    .pword      0x20000A, 0xA0001A, 0xEB0000, 0xA7001A, 0x37FFFE, 0xA7A004, 0xFE6000, 0x0215BA  /* 0x0009B0 .. ...........7....`.... */
    .pword      0x000000, 0x780B07, 0xB009B1, 0x780B01, 0xA8E761, 0x000000, 0x000000, 0xA3B804  /* 0x0009C0 .....x.....xa........... */
    .pword      0xAFE761, 0x3AFFFE, 0x0215DC, 0x000000, 0x4703E2, 0xEB0000, 0x469B97, 0x481B97  /* 0x0009D0 a....:........G.....F..H */
    .pword      0xD1000D, 0x468000, 0x518180, 0x31FFB2, 0x418180, 0x3AFFA5, 0x23FF00, 0xB62760  /* 0x0009E0 .....F..Q..1..A..:..#`'. */
    .pword      0xEB2780, 0x90001E, 0x9000AE, 0xFA8000, 0xBE064F, 0xBE054F, 0xBE044F, 0xF90054  /* 0x0009F0 .'..........O..O..O..T.. */
    .pword      0xEB2780, 0xEF2864, 0x060000, 0x4787EE, 0xBE9F88, 0xBE9F8A, 0xBE9F8C, 0x781F8E  /* 0x000A00 .'.d(......G...........x */
    .pword      0x780600, 0x20800D, 0x80432E, 0xEF2864, 0x201000, 0x200001, 0x9FB7B0, 0x9FB7C1  /* 0x000A10 ..x.. .C.d(... .. ...... */
    .pword      0x57807A, 0x021480, 0x000000, 0x9FB7D0, 0x9FB7E1, 0x9FB7F2, 0x9FBF83, 0x200089  /* 0x000A20 z.W....................  */
    .pword      0xEB0400, 0x37002E, 0x97EACF, 0x42CFE9, 0x360005, 0x200010, 0x020B96, 0x000000  /* 0x000A30 .....7.....B..6.. ...... */
    .pword      0xEB0000, 0x37004B, 0x97B57F, 0x97BD8F, 0xE9000D, 0x97B37F, 0x600086, 0x320005  /* 0x000A40 ...K.7..............`..2 */
    .pword      0xEB0080, 0x70000A, 0x70808B, 0x400561, 0x4885E0, 0xB86861, 0x97B15F, 0x97B1EF  /* 0x000A50 .....p..pa.@..Hah._..... */
    .pword      0x410000, 0x498081, 0x9FB7D0, 0x9FB7E1, 0x500F8A, 0x588F8B, 0x390010, 0x2010C0  /* 0x000A60 ..A..I........P..X..9..  */
    .pword      0x200001, 0x97B2BF, 0x528F80, 0x310030, 0x57807A, 0x021480, 0x000000, 0x9FB7D0  /* 0x000A70 .. .....R0.1z.W......... */
    .pword      0x9FB7E1, 0x9FB7F2, 0x9FBF83, 0x550F80, 0x5D8F81, 0x360001, 0xE90408, 0xE80408  /* 0x000A80 ...........U..]..6...... */
    .pword      0x78001C, 0x540F80, 0x39FFCF, 0xE80E00, 0x97B05F, 0x97B0EF, 0x021436, 0x000000  /* 0x000A90 ..x..T..9..._.....6..... */
    .pword      0xE00000, 0x3A0016, 0x97B25F, 0x97B2EF, 0x200042, 0x200003, 0x201180, 0x200001  /* 0x000AA0 .....:_.....B. .. .. ..  */
    .pword      0x021202, 0x000000, 0xE00000, 0x3A000C, 0x207BE0, 0x470700, 0x88432E, 0x97B05F  /* 0x000AB0 ...........:.{ ..G.C._.. */
    .pword      0x97B0EF, 0x021370, 0x000000, 0xEB0000, 0x020B96, 0x000000, 0x200010, 0x370005  /* 0x000AC0 ...p................ ..7 */
    .pword      0xE90489, 0x3AFFDE, 0x200010, 0x370001, 0xEB0000, 0x78074F, 0xBE064F, 0xBE054F  /* 0x000AD0 .....:.. ..7...O.xO..O.. */
    .pword      0xBE044F, 0xB100EF, 0x060000, 0xFA0016, 0xBE9F88, 0xBE9F8A, 0x781F8C, 0x780482  /* 0x000AE0 O...................x..x */
    .pword      0x780503, 0x780404, 0x2BF002, 0x2000A3, 0x9FB782, 0x9FB793, 0x97B10F, 0x97B19F  /* 0x000AF0 ..x..x..+.. ............ */
    .pword      0x9FBF92, 0x9FBFA3, 0xE00008, 0x3A0001, 0x57847C, 0xEB0600, 0xEB0580, 0x780288  /* 0x000B00 ...........:|.W........x */
    .pword      0x78020B, 0x78018A, 0x780109, 0x020D72, 0x000000, 0xE00000, 0x320036, 0xBE0218  /* 0x000B10 ..x..x..xr........6.2... */
    .pword      0x200082, 0x200003, 0x2011C0, 0x200001, 0x021202, 0x000000, 0xE00000, 0x3A001E  /* 0x000B20 .. .. .. .. ...........: */
    .pword      0xBE0218, 0x97B81F, 0x97B8AF, 0x500FE0, 0x588FE0, 0x32000F, 0xBE0004, 0x97B91F  /* 0x000B30 ...........P..X..2...... */
    .pword      0x97B9AF, 0x520F82, 0x5A8F83, 0x390009, 0xE0000B, 0x3A001C, 0x904128, 0x20009B  /* 0x000B40 .....R..Z..9.....:(A...  */
    .pword      0xE00402, 0x350001, 0x20004B, 0x9004A8, 0x37FFDA, 0x2000C2, 0x200003, 0x2010C0  /* 0x000B50 .....5K. .....7.. .. ..  */
    .pword      0x200001, 0x021202, 0x000000, 0xE00000, 0x32000F, 0x20001C, 0x550FE3, 0x36000D  /* 0x000B60 .. ...........2.. ..U..6 */
    .pword      0x900128, 0x550502, 0x550563, 0x44850A, 0x200024, 0x200005, 0x420038, 0x4A80A8  /* 0x000B70 (....Uc.U..D$. .. 8.B..J */
    .pword      0x4104E3, 0x37FFC5, 0x2FFFCC, 0x370001, 0xEB8600, 0x78000C, 0x78064F, 0xBE054F  /* 0x000B80 ..A..7../..7.....xO.xO.. */
    .pword      0xBE044F, 0xFA8000, 0x060000, 0xFA000A, 0xBE9F88, 0xBE9F8A, 0xBE9F8C, 0x980730  /* 0x000B90 O....................0.. */
    .pword      0x98074F, 0x2002C0, 0x200001, 0x0215C2, 0x000000, 0xBE0200, 0x780580, 0xE88080  /* 0x000BA0 O.... .. ...........x... */
    .pword      0xA10001, 0x478781, 0x90054E, 0xEB0080, 0x201242, 0x200003, 0x78000A, 0x0210EA  /* 0x000BB0 .....GN.....B. .. ..x... */
    .pword      0x000000, 0x370033, 0xEB4000, 0x984750, 0x78000A, 0x7840B0, 0x984721, 0x7840B0  /* 0x000BC0 ...3.7.@.PG...x.@x!G..@x */
    .pword      0x984731, 0x784010, 0x984740, 0x450063, 0x784F10, 0x90404A, 0x984710, 0x90402A  /* 0x000BD0 1G..@x@G.c.E.OxJ@..G.*@. */
    .pword      0x404FE8, 0x320004, 0x200014, 0xE00400, 0x3D0005, 0x370001, 0x200084, 0x90003E  /* 0x000BE0 .O@..2.. .....=..7.. >.. */
    .pword      0xE00000, 0x320016, 0x78061E, 0x4506E6, 0x90041E, 0x9004AE, 0xEB0280, 0x78018C  /* 0x000BF0 .....2..x..E...........x */
    .pword      0x78010D, 0xBE0008, 0x020D72, 0x000000, 0xE00000, 0x32000A, 0x203DF0, 0x884320  /* 0x000C00 ..x...r..........2.=  C. */
    .pword      0xEB0300, 0x2DEAD5, 0xEB0200, 0x78018C, 0x78010D, 0xBE0008, 0x020E94, 0x000000  /* 0x000C10 .....-.....x..x......... */
    .pword      0x78001E, 0x558580, 0x5585E6, 0x400066, 0x450500, 0x558FE6, 0x3EFFCB, 0xEF2864  /* 0x000C20 ..x..U..Uf.@..E..U..>d(. */
    .pword      0x9007CE, 0xBE064F, 0xBE054F, 0xBE044F, 0xFA8000, 0x060000, 0xBE9F88, 0x2009A1  /* 0x000C30 ...O..O..O.............  */
    .pword      0xEB0100, 0x200780, 0x201013, 0x784980, 0x784981, 0x784982, 0x800802, 0xA00002  /* 0x000C40 ..... .. .Ix.Ix.Ix...... */
    .pword      0x200571, 0x200460, 0x201003, 0x784980, 0x784981, 0x784982, 0xAB0100, 0x3AFFFE  /* 0x000C50 q. `. .. .Ix.Ix.Ix.....: */
    .pword      0x2B1200, 0x880810, 0xEF210C, 0x280000, 0x880830, 0x2009A1, 0x200012, 0x200780  /* 0x000C60 ..+....!...(0.... .. ..  */
    .pword      0x201013, 0x784980, 0x784981, 0x784982, 0x800802, 0xA00002, 0x200571, 0x200460  /* 0x000C70 .. .Ix.Ix.Ix......q. `.  */
    .pword      0x201003, 0x784980, 0x784981, 0x784982, 0xAB0100, 0x3AFFFE, 0x800800, 0xDE004C  /* 0x000C80 .. .Ix.Ix.Ix.....:...L.. */
    .pword      0x600065, 0x500FE1, 0x3A0004, 0x200201, 0x800802, 0x608002, 0x32FFFD, 0x224008  /* 0x000C90 e.`..P..:.. .....`..2.@" */
    .pword      0x200F49, 0x8842C8, 0x8842D9, 0x8842C8, 0x8842D9, 0x23E800, 0x8842A0, 0xEF2856  /* 0x000CA0 I. .B..B..B..B...#.B.V(. */
    .pword      0x200100, 0x884340, 0x070318, 0xA8A69F, 0xA8E69E, 0xA8C69E, 0xA9E6A6, 0xA9C6A6  /* 0x000CB0 .. @C................... */
    .pword      0xEF6666, 0xEF6662, 0xEF666A, 0xA8C673, 0xA8E673, 0x841E11, 0xEB0000, 0x07009B  /* 0x000CC0 ff.bf.jf.s..s........... */
    .pword      0x209240, 0x8842E0, 0xBE0008, 0xBE044F, 0x060000, 0xBE9F88, 0xBE9F8A, 0xBE9F8C  /* 0x000CD0 @. .B....O.............. */
    .pword      0x781F8E, 0xEF2972, 0xEF2864, 0xEF2860, 0xEF285E, 0xEF2850, 0xEF2852, 0x07FFA6  /* 0x000CE0 ..xr).d(.`(.^(.P(.R(.... */
    .pword      0x208E60, 0xEB0800, 0x8042C0, 0x8042D1, 0x884790, 0x8847A1, 0x8042A2, 0x8042B3  /* 0x000CF0 `. ....B..B..G..G..B..B. */
    .pword      0xDD1845, 0xDE10CB, 0x700081, 0xDD1045, 0xB82260, 0xB83360, 0x520200, 0x5A8281  /* 0x000D00 E.......pE..`".`3...R..Z */
    .pword      0x5B0360, 0x5B83E0, 0x214E01, 0x209100, 0x07019C, 0x070352, 0xBE0500, 0xBE0600  /* 0x000D10 `.[..[.N!.. ...R........ */
    .pword      0x208F2E, 0xFE6000, 0x07034D, 0xBE0400, 0x8042C0, 0x8042D1, 0xDD094C, 0xDE0044  /* 0x000D20 .. .`.M......B..B.L..D.. */
    .pword      0x710000, 0xDE08C4, 0x54010A, 0x5C818B, 0x510F80, 0x598F81, 0x360004, 0x40050A  /* 0x000D30 ..q.....T..\..Q..Y..6..@ */
    .pword      0x48858B, 0x200010, 0x070448, 0x8042E0, 0x900890, 0xE00001, 0x320004, 0xEB0080  /* 0x000D40 ..H.. H...B.........2... */
    .pword      0x980811, 0xBE0608, 0x37000B, 0x54040C, 0x5C848D, 0x540FBE, 0x5C8FAE, 0x360006  /* 0x000D50 ........7..T..\..T..\..6 */
    .pword      0x07028E, 0xE00000, 0x320003, 0x200FF0, 0x070404, 0x0703DC, 0xFE4001, 0x37FFD9  /* 0x000D60 ........2.. .......@...7 */
    .pword      0xB82260, 0xBE9F88, 0xBE9F8A, 0xF80054, 0xE00005, 0xAF2042, 0x78028F, 0x4282E4  /* 0x000D70 `".......T.....B ...x..B */
    .pword      0xA37801, 0x3A0006, 0xA3D804, 0x320002, 0xAF4761, 0xA26001, 0xA7C004, 0xA06001  /* 0x000D80 .x...:.....2aG..`.....`. */
    .pword      0x8802A1, 0xEB0080, 0xAFE761, 0x37FFFE, 0x370003, 0xE88000, 0xAF0042, 0xEC2054  /* 0x000D90 ......a....7..7...B..T . */
    .pword      0xE90183, 0x390029, 0x781A82, 0x2000D1, 0x2FFFF6, 0x200FF7, 0x784332, 0xE90183  /* 0x000DA0 ...).9..x.. ../.. 2Cx... */
    .pword      0x390005, 0x7848B2, 0xE90183, 0x390003, 0x7843B2, 0x370002, 0xA03004, 0xA02004  /* 0x000DB0 ..9.Hx.....9.Cx..7.0.. . */
    .pword      0xBA0410, 0xBA8490, 0xBE9A88, 0xBE9A86, 0x620FE3, 0x32000B, 0xEA8306, 0xEAC387  /* 0x000DC0 ..............b..2...... */
    .pword      0x630508, 0x638589, 0xA70004, 0xBE030A, 0x730508, 0x738589, 0xA71004, 0xBE030A  /* 0x000DD0 ..c..c........s..s...... */
    .pword      0xB84460, 0x530308, 0x5B8389, 0x620FEC, 0x320003, 0xEB0380, 0xA73004, 0xB20FF6  /* 0x000DE0 `D...S..[..b..2....0.... */
    .pword      0x5282EA, 0x730087, 0x32FFD2, 0x8002A6, 0x782286, 0x782280, 0x780001, 0xF90054  /* 0x000DF0 ..R..s..2...."x."x..xT.. */
    .pword      0xBE054F, 0xBE044F, 0x060000, 0xBE9F88, 0x781F8A, 0x780500, 0x780481, 0xB3C018  /* 0x000E00 O..O..........x..x..x... */
    .pword      0x2007F0, 0x548F80, 0x3C0001, 0xEB4400, 0x640461, 0xDD444A, 0xA06008, 0xA0C008  /* 0x000E10 .. ..T..<.D.a.dJD..`.... */
    .pword      0x200000, 0x2049A1, 0x700001, 0x884B80, 0xEB0800, 0xA8E178, 0xEB0000, 0x884AF0  /* 0x000E20 .. .I ..p.K....x......J. */
    .pword      0xA9E178, 0xEB0200, 0x215CA3, 0x200100, 0x200001, 0x780100, 0x2094C1, 0x209380  /* 0x000E30 x......\!.. .. ..x.. ..  */
    .pword      0x0214C4, 0x000000, 0x215D04, 0xEB0180, 0x200780, 0x200001, 0x780100, 0x2086E1  /* 0x000E40 .......]!..... .. ..x..  */
    .pword      0x209240, 0x0214C4, 0x000000, 0x200100, 0x884A40, 0x8849A0, 0x2109E0, 0x884A30  /* 0x000E50 @. ........ @J..I...!0J. */
    .pword      0x214100, 0x884990, 0x804B80, 0x780808, 0xEF249E, 0xEF24A2, 0x882509, 0x8824CA  /* 0x000E60 .A!.I..K...x.$..$..%..$. */
    .pword      0xA9008A, 0x200011, 0x60C0EF, 0xB3CF00, 0x200B02, 0x784112, 0x614000, 0x704001  /* 0x000E70 ..... ..`..... .Ax.@a.@p */
    .pword      0xB7E0B0, 0x0215E2, 0x000000, 0x804B80, 0xA0F010, 0x78054F, 0xBE044F, 0x060000  /* 0x000E80 ..........K....O.xO..... */
    .pword      0xFE0000, 0xB82260, 0xA37801, 0x3A0006, 0xA3D804, 0x320002, 0xAF4761, 0xA26001  /* 0x000E90 ...`"..x...:.....2aG..`. */
    .pword      0xA7C004, 0xA06001, 0xBE9F80, 0xBE9F82, 0xF80054, 0x8802A1, 0xE00003, 0x32002C  /* 0x000EA0 ....`.......T........,.2 */
    .pword      0x200067, 0x539FE3, 0x207626, 0xE00006, 0x320004, 0x9FBFF7, 0xA71000, 0xA0F004  /* 0x000EB0 g. ..S&v .....2......... */
    .pword      0xA11000, 0xA10000, 0xAFE761, 0x37FFFE, 0x97BBFF, 0xEB8080, 0xBAC090, 0xBA1F90  /* 0x000EC0 ......a....7............ */
    .pword      0x609FCF, 0xE88000, 0xE13866, 0x3A0005, 0xBAC090, 0x609FCF, 0xBA0090, 0x609FCF  /* 0x000ED0 ..`...f8...:.....`.....` */
    .pword      0xE88000, 0xE00000, 0xAF2042, 0xEC2054, 0xA7F004, 0x5383E3, 0xA1F004, 0xEA8FCF  /* 0x000EE0 ......B .T ......S...... */
    .pword      0x3A0004, 0x410107, 0x518187, 0x360007, 0x37FFE7, 0xEBD900, 0xE90183, 0x320003  /* 0x000EF0 ..:..A..Q..6..7........2 */
    .pword      0xE90387, 0x3AFFFB, 0x37FFE1, 0xE9878F, 0xF90054, 0xBE014F, 0xBE004F, 0x0408BC  /* 0x000F00 .....:..7...T..O..O..... */
    .pword      0x000000, 0xBFA86A, 0x32003A, 0x97B8CF, 0xDE08CD, 0x3A0037, 0xAF6044, 0x3A0035  /* 0x000F10 ...j..:.2......7.:D`.5.: */
    .pword      0xAC086C, 0x3A0033, 0xF80036, 0xF80032, 0xF80034, 0xF80000, 0xBE9F84, 0xBE9F86  /* 0x000F20 l..3.:6..2..4........... */
    .pword      0x804335, 0x804BA6, 0x804B37, 0x130295, 0x188337, 0x188397, 0xEF6042, 0x2086A4  /* 0x000F30 5C..K.7K....7.....B`...  */
    .pword      0x780184, 0x108214, 0x32001B, 0x420062, 0xE12830, 0xE1B030, 0xE1B830, 0x33FFF8  /* 0x000F40 ..x.....2b.B0(.0..0....3 */
    .pword      0xBE9F84, 0xBE9F86, 0x1082B0, 0x188330, 0x1883B0, 0xAF2042, 0x780994, 0x428A54  /* 0x000F50 .........0.....B ...xT.B */
    .pword      0x4B0A54, 0x4B8A54, 0x20F706, 0x200007, 0xBE9F86, 0x781FB0, 0x781FB0, 0x064000  /* 0x000F60 T.KT.K.. .. .....x..x.@. */
    .pword      0xEF6042, 0xBE034F, 0xBE024F, 0x980A10, 0xEB0080, 0x37FFE2, 0xBE034F, 0xBE024F  /* 0x000F70 B`.O..O..........7O..O.. */
    .pword      0xF90000, 0xF90034, 0xF90032, 0xF90036, 0xA9086C, 0xBE014F, 0x7800CF, 0x064000  /* 0x000F80 ...4..2..6..l..O....x.@. */
    .pword      0xEB0280, 0xA7000F, 0xEB8280, 0x4081E0, 0xAF2042, 0x050000, 0x781F85, 0xE81F80  /* 0x000F90 ...........@B ......x... */
    .pword      0x900003, 0xE10002, 0xAF0042, 0x780002, 0x900223, 0x9003B3, 0x900343, 0x430307  /* 0x000FA0 ......B....x#.....C....C */
    .pword      0xE900CF, 0x32001A, 0x530384, 0xE13800, 0xAF0042, 0x5003E0, 0x320003, 0xE90287  /* 0x000FB0 .....2..S.8.B....P..2... */
    .pword      0x098005, 0x7858B4, 0xE12006, 0xAF0042, 0x900243, 0x500387, 0x320003, 0xE90287  /* 0x000FC0 ....Xx. .B..C....P..2... */
    .pword      0x098005, 0x7858B4, 0x508080, 0xE0004F, 0x320002, 0x100993, 0x9801A4, 0x9003E3  /* 0x000FD0 ....Xx..PO....2......... */
    .pword      0xE00007, 0xAE2042, 0x010007, 0x060000, 0x420200, 0xE12006, 0xAF0042, 0x520207  /* 0x000FE0 ...B .........B. .B....R */
    .pword      0x780100, 0x37FFF1, 0xDE00C8, 0x32002C, 0x800212, 0xDD1148, 0xAF6044, 0xA07002  /* 0x000FF0 ..x..7...,.2...H..D`..p. */
    .pword      0x711FCF, 0xB80062, 0xB00140, 0x2FFFC2, 0xE00002, 0x330003, 0xAFE083, 0x400002  /* 0x001000 ..qb..@..../.....3.....@ */
    .pword      0x37001C, 0xAE0083, 0x37001A, 0x2BF102, 0x2000A1, 0x710181, 0x320016, 0x781F80  /* 0x001010 ..7.....7..+.. ..q..2..x */
    .pword      0x2BF100, 0x021594, 0x000000, 0xEA9F80, 0x2BF000, 0x2000A1, 0x021594, 0x000000  /* 0x001020 ..+...........+.. ...... */
    .pword      0x780100, 0xE901CF, 0x78004F, 0xEB0080, 0xA6F002, 0xA73002, 0x370006, 0x21FFF4  /* 0x001030 ..x...O.x.......0...7..! */
    .pword      0x618184, 0x208004, 0xB82103, 0x400002, 0x488083, 0x021594, 0x000000, 0xBE9F80  /* 0x001040 ..a.. .!...@..H......... */
    .pword      0x060000, 0xEB1F80, 0x781F81, 0x781F80, 0x781F86, 0x781F85, 0x781F84, 0x0214F8  /* 0x001050 ........x..x..x..x..x... */
    .pword      0x000000, 0x0213C0, 0x000000, 0xEB0380, 0x53824F, 0x5B82CF, 0x5B834F, 0x7801CF  /* 0x001060 ............O.S..[O.[..x */
    .pword      0x320014, 0x2086A7, 0x781997, 0x33000B, 0x401984, 0x489985, 0x491986, 0xBE9984  /* 0x001070 ..2.. ..x..3..@..H..I... */
    .pword      0x781986, 0x7819CF, 0x7819CF, 0x280000, 0x781980, 0x518BF4, 0x060000, 0x501984  /* 0x001080 ..x..x..x..(..x..Q.....P */
    .pword      0x589985, 0x591986, 0x090002, 0xEB1980, 0x37FFF4, 0x5787E4, 0x060000, 0xBE9F88  /* 0x001090 ..X..Y........7..W...... */
    .pword      0x781F8A, 0xBE0400, 0x780502, 0x370008, 0x780008, 0x021550, 0x000000, 0xE00000  /* 0x0010A0 ..x.....x..7..xP........ */
    .pword      0x320003, 0x900808, 0x020FF4, 0x000000, 0xE00018, 0x32000F, 0x0213C0, 0x000000  /* 0x0010B0 ..2..............2...... */
    .pword      0x500139, 0x5881A9, 0x510FBA, 0x598FAA, 0x36FFEF, 0x370007, 0x0213C0, 0x000000  /* 0x0010C0 9.P..X..Q..Y..6..7...... */
    .pword      0x500139, 0x5881A9, 0x510FBA, 0x598FAA, 0x3E0004, 0xAB049E, 0x3AFFF7, 0x200010  /* 0x0010D0 9.P..X..Q..Y..>.....:..  */
    .pword      0x370001, 0xEB0000, 0x78054F, 0xBE044F, 0x060000, 0xE00004, 0x320021, 0x780280  /* 0x0010E0 ..7...O.xO........!.2..x */
    .pword      0xB80862, 0x200036, 0x090011, 0xD88846, 0x410100, 0xB08003, 0xEB0000, 0x8002A7  /* 0x0010F0 b..6. ...F....A......... */
    .pword      0x8802A3, 0xAFE761, 0x37FFFE, 0xE00001, 0x32000C, 0xE90204, 0xA70002, 0x370006  /* 0x001100 ...a....7.....2........7 */
    .pword      0xBA5AD2, 0xE90204, 0x39000C, 0x370002, 0xBA5AB2, 0xBA5A92, 0xBADAC2, 0xE88102  /* 0x001110 .Z......9..7.Z..Z....... */
    .pword      0xB4A054, 0x520263, 0x31FFF9, 0x420262, 0x330002, 0x098004, 0xBA5AB2, 0x8802A7  /* 0x001120 T..c.R..1b.B..3....Z.... */
    .pword      0x060000, 0x4001E0, 0x780002, 0x320020, 0x9003C3, 0x900333, 0x900213, 0x530293  /* 0x001130 .....@..x .2...3.......S */
    .pword      0x438306, 0x530384, 0xE12800, 0xAE0042, 0x780005, 0x780280, 0xE13805, 0xAE0042  /* 0x001140 ..C..S.(.B....x..x.8.B.. */
    .pword      0x780287, 0xE90385, 0x390002, 0x098007, 0x785A31, 0xE12006, 0xAF0042, 0x900243  /* 0x001150 ..x.....9...1Zx. .B..C.. */
    .pword      0x500305, 0x320003, 0xE90386, 0x098007, 0x785A31, 0x400993, 0x980194, 0x9003D3  /* 0x001160 ..P..2......1Zx..@...... */
    .pword      0x508080, 0xE00007, 0xAE2042, 0x010007, 0x060000, 0xE00000, 0xAF2042, 0x050010  /* 0x001170 ..P...B ..........B .... */
    .pword      0x781F88, 0xBE9F82, 0x780400, 0x0213C0, 0x000000, 0xBE9F80, 0x9003F8, 0xE00007  /* 0x001180 ..x.....x............... */
    .pword      0x320007, 0x780008, 0x5780E4, 0x578168, 0x010007, 0x5787E8, 0x78044F, 0x060000  /* 0x001190 ..2..x..Wh.W.....WO.x... */
    .pword      0x200010, 0xE00018, 0x32FFFA, 0x0213C0, 0x000000, 0x97B97F, 0x97B9EF, 0x500002  /* 0x0011A0 .. .....2..............P */
    .pword      0x588083, 0x97B95F, 0x97B9CF, 0xE10002, 0xE18803, 0x34FFF2, 0xEB0000, 0x37FFED  /* 0x0011B0 ..X_.............4.....7 */
    .pword      0xEA8000, 0xEA8081, 0x200085, 0x283206, 0x2EDB87, 0x370010, 0x785AB2, 0x7852B2  /* 0x0011C0 ........ .2(.....7.Zx.Rx */
    .pword      0x680004, 0x200084, 0xD10081, 0xD38000, 0x390002, 0x680006, 0x688087, 0xD10081  /* 0x0011D0 ..h.. ........9..h..h... */
    .pword      0xD38000, 0x390002, 0x680006, 0x688087, 0xE90204, 0x3AFFF4, 0x5181E2, 0x3BFFEE  /* 0x0011E0 .....9..h..h.....:..Q..; */
    .pword      0x4181E2, 0x320004, 0x684012, 0x200044, 0x200003, 0x37FFEC, 0xEA8000, 0xEA8081  /* 0x0011F0 ..A..2.@hD. .. ..7...... */
    .pword      0x060000, 0xFA0004, 0x781F88, 0xBE9F8A, 0xBE0300, 0x9FBFB0, 0x9FBFC1, 0x780402  /* 0x001200 ........x..............x */
    .pword      0xBE0504, 0xEB0000, 0x530FE0, 0x5B8FE0, 0x3A000B, 0x37000F, 0x57806A, 0x021480  /* 0x001210 ........S..[..:..7j.W... */
    .pword      0x000000, 0x550F80, 0x5D8F81, 0x390003, 0x550F82, 0x5D8F83, 0x390005, 0x540464  /* 0x001220 .....U..]..9..U..]..9d.T */
    .pword      0xE00008, 0x3AFFF4, 0xEB0000, 0x370001, 0x200010, 0xBE054F, 0x78044F, 0xFA8000  /* 0x001230 .....:.....7.. O..O.x... */
    .pword      0x060000, 0xFE0000, 0xAFE761, 0x37FFFE, 0x883B00, 0x804327, 0xEF2864, 0xB83B01  /* 0x001240 ......a....7.;.'C.d(..;. */
    .pword      0x213261, 0xB009B7, 0x530081, 0x0215BA, 0x000000, 0x883B31, 0xEB0080, 0x883B37  /* 0x001250 a2!.....S......1;....7;. */
    .pword      0xA8E761, 0x000000, 0x000000, 0xA60002, 0x0215DC, 0x000000, 0xEB0380, 0xA30802  /* 0x001260 a....................... */
    .pword      0xAFE761, 0x3AFFFE, 0xA9C761, 0xA70002, 0x0215DC, 0x000000, 0x060000, 0x2180A0  /* 0x001270 a....:a................! */
    .pword      0x200001, 0x50016A, 0x5881E0, 0x32000A, 0x021594, 0x000000, 0xDE0048, 0xDD08C8  /* 0x001280 .. j.P..X..2......H..... */
    .pword      0x700001, 0x212CD2, 0x510000, 0xAE2042, 0x050000, 0x050010, 0x200000, 0x200001  /* 0x001290 ..p.,!..QB ......... ..  */
    .pword      0xA06001, 0x021594, 0x000000, 0xDE014C, 0xDD09C4, 0x710103, 0xEA8000, 0x20FFF4  /* 0x0012A0 .`.......L.......q.....  */
    .pword      0x600004, 0x37FFF0, 0x200011, 0xA7000F, 0x200001, 0x200982, 0x370007, 0x200011  /* 0x0012B0 ..`..7.. ..... .. ..7..  */
    .pword      0xA7000F, 0x200001, 0xE00000, 0xAF2042, 0x050000, 0x200882, 0x900980, 0xDE1848  /* 0x0012C0 ..... ...B ...... ...H.. */
    .pword      0xAE2042, 0x050000, 0xDE1A43, 0xA10004, 0x410104, 0x6181EF, 0xA51812, 0xD28000  /* 0x0012D0 B ....C.......A..a...... */
    .pword      0x4080E0, 0xAD9812, 0x060000, 0xEB0000, 0x884BA0, 0x209600, 0x884B30, 0x201900  /* 0x0012E0 ..@..........K... 0K...  */
    .pword      0x884330, 0x2086A0, 0xE00000, 0x320002, 0xEF286A, 0xEF686C, 0x200040, 0x880CA0  /* 0x0012F0 0C... .....2j(.lh.@. ... */
    .pword      0xEFA192, 0x200021, 0xDD0880, 0xBFC0A9, 0x60406F, 0x704001, 0xB7E0A9, 0xA86098  /* 0x001300 ...!. ......o@`.@p....`. */
    .pword      0xA8E195, 0x060000, 0xEB0100, 0xB82260, 0xE00001, 0x320011, 0xE91FB1, 0x33000E  /* 0x001310 .........`"......2.....3 */
    .pword      0x20FFF3, 0x780331, 0xDE33CC, 0x630303, 0x430302, 0xA5A800, 0xA53816, 0xAD2804  /* 0x001320 .. 1.x.3...c..C....8..(. */
    .pword      0xAA0042, 0xAE2042, 0xAD3816, 0xE80285, 0xE91FCF, 0x3BFFF3, 0xE9878F, 0x780004  /* 0x001330 B..B ..8.........;.....x */
    .pword      0x060000, 0x00085E, 0x000002, 0x000080, 0x000850, 0x000004, 0x000080, 0x00095C  /* 0x001340 ...^........P........\.. */
    .pword      0x000002, 0x000080, 0x000972, 0x000002, 0x000080, 0x000860, 0x000002, 0x000080  /* 0x001350 ......r........`........ */
    .pword      0x000862, 0x000002, 0x000080, 0x000864, 0x000002, 0x000080, 0x000000, 0xFE0000  /* 0x001360 b........d.............. */
    .pword      0xAFE761, 0x37FFFE, 0x8002A2, 0x8802A1, 0xBB0803, 0x8802A2, 0x883B10, 0x883B21  /* 0x001370 a....7.............;.!;. */
    .pword      0x240420, 0xA8C760, 0xAEC760, 0xB28410, 0xEB0100, 0x203E51, 0x021244, 0x000000  /* 0x001380  .$`..`........Q> D..... */
    .pword      0xAFE761, 0x37FFFE, 0xA9C761, 0x060000, 0xBE9F88, 0xBE9F8A, 0xBE0400, 0x0213C0  /* 0x001390 a....7a................. */
    .pword      0x000000, 0xBE0500, 0xEB0180, 0xE00003, 0xAE2042, 0xFE4001, 0x0213C0, 0x000000  /* 0x0013A0 ............B ..@....... */
    .pword      0x50020A, 0x58828B, 0x540104, 0x5C8185, 0x31FFF6, 0xBE054F, 0xBE044F, 0x060000  /* 0x0013B0 ..P..X..T..\..1O..O..... */
    .pword      0x804337, 0xEB0300, 0x209665, 0x804BA0, 0x780215, 0x100017, 0x1B00B4, 0x1B0134  /* 0x0013C0 7C....e. .K...x......4.. */
    .pword      0x1B0194, 0x520264, 0xE12015, 0x3AFFF7, 0x200884, 0xB00004, 0xA63014, 0x060000  /* 0x0013D0 ...d.R. ...:.. ....0.... */
    .pword      0x213E80, 0x0215A4, 0x000000, 0x37FFEC, 0x781F81, 0xBE9F82, 0xEB0180, 0x804B31  /* 0x0013E0 .>!........7..x......1K. */
    .pword      0x688168, 0xE81931, 0x499931, 0x498911, 0x510164, 0x884B32, 0x200881, 0xB00001  /* 0x0013F0 h.h1..1.I..Id.Q2K... ... */
    .pword      0xA13011, 0x20F121, 0xE00001, 0xAE2042, 0x014001, 0xBE014F, 0x7800CF, 0x064000  /* 0x001400 .0.!. ...B ..@.O....x.@. */
    .pword      0x781F88, 0x780400, 0x780118, 0x780088, 0xEB0000, 0x020F94, 0x000000, 0x900818  /* 0x001410 ..x..x..x..x............ */
    .pword      0xA14000, 0x980C10, 0x900818, 0xA15000, 0x980C10, 0x900818, 0xA16000, 0x980C10  /* 0x001420 .@........P........`.... */
    .pword      0x200010, 0x78044F, 0x060000, 0x8002A2, 0x8802A1, 0x207FF3, 0xEA8083, 0x608000  /* 0x001430 .. O.x........... .....` */
    .pword      0xEB8200, 0xAFE761, 0x37FFFE, 0xBAC210, 0xBA00B0, 0x620201, 0xEA8084, 0x3A0004  /* 0x001440 ...a....7........b.....: */
    .pword      0xE98183, 0x31FFF9, 0x8802A2, 0x050010, 0x8802A2, 0x050000, 0x781F83, 0x781F82  /* 0x001450 .....1..............x..x */
    .pword      0x781F80, 0x0213C0, 0x000000, 0xE883CF, 0xAC086C, 0x800216, 0x780237, 0x7802A7  /* 0x001460 ..x.........l.....7.x..x */
    .pword      0x401BCF, 0x489BCF, 0x4913E0, 0xA71006, 0xA9086C, 0x520047, 0x5A80D7, 0x060000  /* 0x001470 ..@..H..I...l..G.R..Z... */
    .pword      0x780230, 0x7802A0, 0x8002A7, 0x421864, 0x4A8860, 0x8802A5, 0xE88304, 0x4A82E0  /* 0x001480 0.x..x...d.B`.J........J */
    .pword      0xAFE761, 0x37FFFE, 0xBA0014, 0xBA8094, 0x8802A5, 0xBA0116, 0xBA8196, 0x8802A7  /* 0x001490 a....7.................. */
    .pword      0x060000, 0x000000, 0xE90202, 0x390005, 0x700181, 0xA60003, 0x370003, 0x098004  /* 0x0014A0 ...........9..p.....7... */
    .pword      0x785831, 0x060000, 0xD10182, 0xE90183, 0x390003, 0x098003, 0x781831, 0xA70002  /* 0x0014B0 1Xx...........9...1.x... */
    .pword      0x785831, 0x060000, 0xEB0280, 0x780805, 0x980032, 0x980041, 0x980011, 0x980021  /* 0x0014C0 1Xx........x2..A.....!.. */
    .pword      0x980053, 0x980064, 0x980075, 0xEB8080, 0x980801, 0x280001, 0x980811, 0x060000  /* 0x0014D0 S..d..u..........(...... */
    .pword      0xE20860, 0x320008, 0x208E60, 0x07F76E, 0x8042A2, 0x8042B3, 0xD10183, 0xD38102  /* 0x0014E0 `....2`. n...B..B....... */
    .pword      0x209100, 0x07FFB4, 0xEB0000, 0x060000, 0x2086A1, 0x370001, 0x780082, 0x780111  /* 0x0014F0 .. ........... ..7..x..x */
    .pword      0xE00002, 0x320004, 0x510F80, 0x3AFFFA, 0x780890, 0x050010, 0x050000, 0xAFE761  /* 0x001500 .....2..Q..:..x......a.. */
    .pword      0x37FFFE, 0xA9C761, 0xEFA762, 0xEFA764, 0x8002A1, 0xEFA054, 0x2FFFE2, 0xBB0902  /* 0x001510 ..7a..b..d.....T..../... */
    .pword      0x8802A1, 0x060000, 0x07FEAC, 0xE00000, 0x320006, 0xFC0102, 0x209720, 0xEB8800  /* 0x001520 ..............2... . ... */
    .pword      0x02180C, 0x000000, 0xFE0000, 0x060000, 0x7841B0, 0x51C1B1, 0x3A0003, 0xE90102  /* 0x001530 .............Ax..Q..:... */
    .pword      0x31FFFB, 0x050000, 0xEB8000, 0xAF0042, 0x050010, 0x060000, 0x200981, 0xA7000F  /* 0x001540 ..1......B.......... ... */
    .pword      0x200881, 0x900900, 0xDE11C3, 0x610167, 0x79C061, 0xDE0002, 0x600061, 0x060000  /* 0x001550 .. ......g.aa.y...a.`... */
    .pword      0x208660, 0xE00000, 0x320004, 0x200980, 0xB00000, 0xA43010, 0xAE0042, 0x050000  /* 0x001560 `. .....2.. ....0.B..... */
    .pword      0x050010, 0x781F80, 0xEB0000, 0x0215D6, 0x000000, 0x60004F, 0x0215D6, 0x000000  /* 0x001570 .....x.........O.`...... */
    .pword      0xEA8000, 0x060000, 0x8042F5, 0x200016, 0xE00005, 0x320002, 0x010005, 0x780300  /* 0x001580 .......B... .....2.....x */
    .pword      0x780006, 0x060000, 0x8002A2, 0x8802A1, 0xAFE761, 0x37FFFE, 0xBA8090, 0xBA0010  /* 0x001590 ..x.........a....7...... */
    .pword      0x8802A2, 0x060000, 0x800211, 0xDD08C8, 0xAF6044, 0xA07001, 0x709FCF, 0x014000  /* 0x0015A0 ............D`..p...p.@. */
    .pword      0xE90102, 0x390002, 0x098002, 0x785801, 0x060000, 0x200800, 0x701FCF, 0x800210  /* 0x0015B0 .....9....Xx..... ..p... */
    .pword      0x064000, 0xB80063, 0xD10081, 0xD38000, 0x060000, 0x2094A1, 0xA02011, 0x060000  /* 0x0015C0 .@.c............. . .... */
    .pword      0x209361, 0xA03011, 0x060000, 0x2839C1, 0x041314, 0x000000, 0xDD0048, 0x701FCF  /* 0x0015D0 a. .0.....9(......H....p */
    .pword      0x064000, 0xA8009A, 0x060000, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0015E0 .@...................... */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0015F0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001600 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001610 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001620 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001630 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001640 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001650 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001660 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001670 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001680 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001690 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0016A0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0016B0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0016C0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0016D0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0016E0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0016F0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001700 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001710 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001720 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001730 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001740 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001750 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001760 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001770 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001780 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x001790 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0017A0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0017B0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0017C0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0017D0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0017E0 ........................ */
    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF  /* 0x0017F0 ........................ */
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
    .weak       _I2C1_RxFifo
    .weak       _I2C1_RxFifoBuffer
    .type       _I2C1_RxFifoBuffer, @object
    .weak       _I2C1_TxFifo
    .type       _I2C1_TxFifo, @object
    .weak       _I2C1_TxFifoBuffer
    .type       _I2C1_TxFifoBuffer, @object
    .weak       _I2C1_wakeKeyIndex
    .type       _I2C1_wakeKeyIndex, @object
    .weak       _NOW_dispatcherActive
    .weak       _NOW_Fcy
    .weak       _NOW_internalCount
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
    .type       _COM_RX_FIFO_Flush, @function
    .weak       _COM_RX_FIFO_Flush
    .weak       _EZBL_AddrInTable
    .type       _EZBL_BootloaderTask, @function
    .weak       _EZBL_BootloaderTask
    .weak       _EZBL_CRC32
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
    .type       _I2C1_FIFO_EnableInterrupts, @function
    .weak       _I2C1_FIFO_EnableInterrupts
    .weak       _I2C1_RX_FIFO_Flush
    .type       _I2C1_RX_FIFO_OnRead, @function
    .weak       _I2C1_RX_FIFO_OnRead
    .weak       _I2C1_ResetBRG
    .type       _I2C1_TX_FIFO_Flush, @function
    .weak       _I2C1_TX_FIFO_Flush
    .type       _I2C1_TX_FIFO_OnWrite, @function
    .weak       _I2C1_TX_FIFO_OnWrite
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
    .type       _UART2_RX_FIFO_Flush, @function
    .weak       _UART2_RX_FIFO_Flush
    .type       _UART3_RX_FIFO_Flush, @function
    .weak       _UART3_RX_FIFO_Flush
    .type       _UART4_RX_FIFO_Flush, @function
    .weak       _UART4_RX_FIFO_Flush
    .type       _UART5_RX_FIFO_Flush, @function
    .weak       _UART5_RX_FIFO_Flush
    .type       _UART6_RX_FIFO_Flush, @function
    .weak       _UART6_RX_FIFO_Flush
    .type       _UART_RX_FIFO_Flush, @function
    .weak       _UART_RX_FIFO_Flush
    .type       __SI2C1Interrupt, @function
    .weak       __SI2C1Interrupt
    .weak       __T1Interrupt
    .type       _main, @function
    .weak       _main
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
    .weak       _EZBL_FORWARD_IRQ_SI2C1
    .weak       _EZBL_FORWARD_IRQ_T1
    .weak       _EZBL_FORWARD_MASK_SI2C1
    .weak       _EZBL_FORWARD_MASK_T1
    .weak       _EZBL_FORWARD_POS_SI2C1
    .weak       _EZBL_FORWARD_POS_T1
    .weak       _EZBL_FOSC
    .weak       _EZBL_FOSCSEL
    .weak       _EZBL_FPOR
    .weak       _EZBL_FSEC
    .weak       _EZBL_FSIGN
    .weak       _EZBL_FWDT
    .weak       _EZBL_i2cSlaveAddr
    .weak       _EZBL_IGT_BASE
    .weak       _EZBL_LEDMap
    .weak       _EZBL_LEDMapLen
    .weak       _EZBL_MAIN_FLASH_BASE
    .weak       _EZBL_MAIN_FLASH_END_ADDRESS
    .weak       _EZBL_RESERVED_BIT_ADDRESS
    .weak       _EZBL_RESERVED_BIT_MASK
    .weak       _EZBL_REVID_ADDRESS
    .weak       _EZBL_REVID_MASK
    .weak       EZBL_Dispatch_SI2C1Interrupt
    .weak       EZBL_Dispatch_T1Interrupt



;----Bootloader symbol addresses----
    .equ        _EZBL_appIsRunning, 0x000972
    .equ        _EZBL_AppPreInstall, 0x00085E
    .equ        _EZBL_bootCtx, 0x0008E6
    .equ        _EZBL_bootTask, 0x000910
    .equ        _EZBL_COM_RX, 0x000860
    .equ        _EZBL_COM_TX, 0x000862
    .equ        _EZBL_COMBootIF, 0x00085C
    .equ        _EZBL_ForwardBootloaderISR, 0x000850
    .equ        _EZBL_NVMKey, 0x000864
    .equ        _I2C1_RxFifo, 0x000924
    .equ        _I2C1_RxFifoBuffer, 0x00086E
    .equ        _I2C1_TxFifo, 0x000938
    .equ        _I2C1_TxFifoBuffer, 0x00094C
    .equ        _I2C1_wakeKeyIndex, 0x00095C
    .equ        _NOW_dispatcherActive, 0x00086C
    .equ        _NOW_Fcy, 0x000858
    .equ        _NOW_internalCount, 0x000960
    .equ        _NOW_microsecond, 0x000868
    .equ        _NOW_millisecond, 0x000854
    .equ        _NOW_ms, 0x000854
    .equ        _NOW_sec, 0x000858
    .equ        _NOW_second, 0x000858
    .equ        _NOW_systemFrequency, 0x000858
    .equ        _NOW_timeOffset, 0x000974
    .equ        _NOW_timerCallbackHead, 0x00086A
    .equ        _NOW_timerSFRPtr, 0x000866
    .equ        _NOW_us, 0x000868
    .equ        _COM_RX_FIFO_Flush, 0x001410
    .equ        _EZBL_AddrInTable, 0x001202
    .equ        _EZBL_BootloaderTask, 0x0014E0
    .equ        _EZBL_CRC32, 0x0011C0
    .equ        _EZBL_CallIRQ, 0x000FF4
    .equ        _EZBL_CallISR, 0x0015A4
    .equ        _EZBL_DisableInterrupts, 0x0015BA
    .equ        _EZBL_EraseAppSpace, 0x000A06
    .equ        _EZBL_ErasePage, 0x001370
    .equ        _EZBL_FIFOFlush, 0x00117A
    .equ        _EZBL_FIFOGetInt, 0x001550
    .equ        _EZBL_FIFOGetIntEn, 0x00154C
    .equ        _EZBL_FIFOIntClear, 0x0012C2
    .equ        _EZBL_FIFOIntDisable, 0x0012B8
    .equ        _EZBL_FIFOIntEnable, 0x0012B4
    .equ        _EZBL_FIFOIntEnableSet, 0x0012BA
    .equ        _EZBL_FIFOIntFlagSet, 0x0012C4
    .equ        _EZBL_FIFOIntRaise, 0x0012BE
    .equ        _EZBL_FIFOPeek, 0x000F90
    .equ        _EZBL_FIFORead, 0x000F94
    .equ        _EZBL_FIFOReset, 0x0014C4
    .equ        _EZBL_FIFOWrite, 0x001132
    .equ        _EZBL_Install2Flash, 0x0003C4
    .equ        _EZBL_IsAppPresent, 0x00127E
    .equ        _EZBL_IsPageErased, 0x001436
    .equ        _EZBL_MapInv, 0x001314
    .equ        _EZBL_MapInvEx, 0x001316
    .equ        _EZBL_Mul3Div2, 0x0015C2
    .equ        _EZBL_PreInstall, 0x001584
    .equ        _EZBL_RAMCompare, 0x00153E
    .equ        _EZBL_RAMCopy, 0x0014A4
    .equ        _EZBL_RAMCopyEx, 0x0014A4
    .equ        _EZBL_RAMSet, 0x0015B0
    .equ        _EZBL_RAMSetEx, 0x0015B0
    .equ        _EZBL_ROMCompare, 0x000D70
    .equ        _EZBL_ROMCompareEx, 0x000D72
    .equ        _EZBL_ReadFlash, 0x001594
    .equ        _EZBL_ReadFlashInstruction, 0x001594
    .equ        _EZBL_ReadPackedFlash, 0x0010EA
    .equ        _EZBL_ReadTablePair, 0x001480
    .equ        _EZBL_RestoreAppErasable, 0x000B96
    .equ        _EZBL_RestoreInterrupts, 0x0015DC
    .equ        _EZBL_VerifyROM, 0x000AE6
    .equ        _EZBL_WaitForNVMOpComplete, 0x00150E
    .equ        _EZBL_WriteNVMCON, 0x001244
    .equ        _EZBL_WriteROM, 0x0008BA
    .equ        _EZBL_WriteROMEx, 0x0008BC
    .equ        _EZBL_WriteROMOnlyOnce, 0x000E92
    .equ        _EZBL_WriteROMOnlyOnceEx, 0x000E94
    .equ        _I2C1_FIFO_EnableInterrupts, 0x0015E2
    .equ        _I2C1_RX_FIFO_Flush, 0x001410
    .equ        _I2C1_RX_FIFO_OnRead, 0x0015D0
    .equ        _I2C1_ResetBRG, 0x000E06
    .equ        _I2C1_TX_FIFO_Flush, 0x00109E
    .equ        _I2C1_TX_FIFO_OnWrite, 0x0015CA
    .equ        _I2C2_RX_FIFO_Flush, 0x001410
    .equ        _I2C3_RX_FIFO_Flush, 0x001410
    .equ        _I2C_RX_FIFO_Flush, 0x001410
    .equ        _InitializeBoard, 0x000C3C
    .equ        _LEDClr, 0x001572
    .equ        _LEDInv, 0x0015D6
    .equ        _LEDOff, 0x001572
    .equ        _LEDToggle, 0x0015D6
    .equ        _NOW_32, 0x0013C0
    .equ        _NOW_64, 0x0013C0
    .equ        _NOW_CreateTask, 0x001052
    .equ        _NOW_EnableInterrupts, 0x001560
    .equ        _NOW_EndTask, 0x0014F8
    .equ        _NOW_ISR, 0x0013E8
    .equ        _NOW_IntEnable, 0x001560
    .equ        _NOW_SetNextTaskTime, 0x00145C
    .equ        _NOW_TMR1_ResetConst, 0x0012E6
    .equ        _NOW_TaskDispatcher, 0x000F12
    .equ        _NOW_Wait, 0x001398
    .equ        _UART1_RX_FIFO_Flush, 0x001410
    .equ        _UART2_RX_FIFO_Flush, 0x001410
    .equ        _UART3_RX_FIFO_Flush, 0x001410
    .equ        _UART4_RX_FIFO_Flush, 0x001410
    .equ        _UART5_RX_FIFO_Flush, 0x001410
    .equ        _UART6_RX_FIFO_Flush, 0x001410
    .equ        _UART_RX_FIFO_Flush, 0x001410
    .equ        __SI2C1Interrupt, 0x000288
    .equ        __T1Interrupt, 0x0013E8
    .equ        _main, 0x000CDA
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
    .equ        _EZBL_bootIDHash, 0x0083B2
    .equ        _EZBL_bootIDHash0, 0x0083B2
    .equ        _EZBL_bootIDHash1, 0x0083B6
    .equ        _EZBL_bootIDHash2, 0x0083BA
    .equ        _EZBL_bootIDHash3, 0x0083BE
    .equ        _EZBL_BOOTLOADER_SIZE, 0x001800
    .equ        _EZBL_ButtonMap, 0x008394
    .equ        _EZBL_ButtonMapLen, 0x000003
    .equ        _EZBL_CODE_PROTECT_ADDRESS, 0x0ABF00
    .equ        _EZBL_CODE_PROTECT_MASK, 0x000E00
    .equ        _EZBL_COMBaud, 0x0083AE
    .equ        _EZBL_CONFIG_BASE, 0x0ABF00
    .equ        _EZBL_CONFIG_END_ADDRESS, 0x0AC000
    .equ        _EZBL_DEVID_ADDRESS, 0xFF0000
    .equ        _EZBL_DEVID_MASK, 0xFFFFFF
    .equ        _EZBL_DEVID_VALUE, 0x00601D
    .equ        _EZBL_FBOOT, 0x801800
    .equ        _EZBL_FBSLIM, 0x0ABF10
    .equ        _EZBL_FDEVOPT1, 0x0ABF2C
    .equ        _EZBL_FICD, 0x0ABF28
    .equ        _EZBL_FLOW_THRESHOLD, 0x000078
    .equ        _EZBL_FORWARD_IRQ_SI2C1, 0x000010
    .equ        _EZBL_FORWARD_IRQ_T1, 0x000003
    .equ        _EZBL_FORWARD_MASK_SI2C1, 0x000002
    .equ        _EZBL_FORWARD_MASK_T1, 0x000001
    .equ        _EZBL_FORWARD_POS_SI2C1, 0x000001
    .equ        _EZBL_FORWARD_POS_T1, 0x000000
    .equ        _EZBL_FOSC, 0x0ABF1C
    .equ        _EZBL_FOSCSEL, 0x0ABF18
    .equ        _EZBL_FPOR, 0x0ABF24
    .equ        _EZBL_FSEC, 0x0ABF00
    .equ        _EZBL_FSIGN, 0x0ABF14
    .equ        _EZBL_FWDT, 0x0ABF20
    .equ        _EZBL_i2cSlaveAddr, 0x0083C2
    .equ        _EZBL_IGT_BASE, 0x001810
    .equ        _EZBL_LEDMap, 0x00839C
    .equ        _EZBL_LEDMapLen, 0x000008
    .equ        _EZBL_MAIN_FLASH_BASE, 0x000000
    .equ        _EZBL_MAIN_FLASH_END_ADDRESS, 0x0AC000
    .equ        _EZBL_RESERVED_BIT_ADDRESS, 0x0ABF14
    .equ        _EZBL_RESERVED_BIT_MASK, 0x008000
    .equ        _EZBL_REVID_ADDRESS, 0xFF0002
    .equ        _EZBL_REVID_MASK, 0xFFFFFF
    .equ        EZBL_Dispatch_SI2C1Interrupt, 0x00015A
    .equ        EZBL_Dispatch_T1Interrupt, 0x000150

#endif
