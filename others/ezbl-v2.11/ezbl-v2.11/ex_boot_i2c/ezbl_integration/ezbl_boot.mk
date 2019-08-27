################################################################################
# Easy Bootloader for PIC24, dsPIC33, and PIC32MM Bootloader make script       #
################################################################################
# To use, put "include ezbl_integration/ezbl_boot.mk" at the very bottom
# of your Bootloader project's Makefile, immediately after the
# nbproject/Makefile-local-${CONF}.mk file has been included.

# Bootloader product identification data to avoid bricking through upload of 
# non-applicable Application firmware images. These strings will combine into a 
# static, globally unique identification hash to represent this booloader.
#
# Any Application firmware sent to the Bootloader will be tested for 
# compatibility with the system hardware (via the BOOTID_HASH) in advance of 
# erasing or programming anything.
BOOTID_VENDOR = "Microchip Technology"
BOOTID_MODEL  = "ezbl_product"
BOOTID_NAME   = "Microchip development board"
BOOTID_OTHER  = "PIC24/dsPIC"


# List of directories where you want the .merge.gld/.merge.S EZBL output files
# to be copied to after building this Bootloader. Normally, this should be the
# ezbl_integration folder in your Application project(s) that will use this
# Bootloader. This Bootloader's ezbl_integration folder is also used as a
# destination to have a backup if the Clean command is issued or the project
# dist folder is deleted.
appMergeDestFolders = ${thisMakefileDir}                        \
                      ../ex_app_led_blink/ezbl_integration



# Dynamically find the path to this ezbl_boot.mk file so we can pass it to the
# ezbl_tools.jar --make_editor -updateid="" option. This handles different
# directory structures better across projects and user preferences.
thisMakefilePath := ${word ${words ${MAKEFILE_LIST}},${MAKEFILE_LIST}}
thisMakefileDir  := ${dir ${thisMakefilePath}}


# Pre-build code to compute the BOOTID hash and do other build-time
# processing need to create an EZBL bootloader.
ifeq (${MAKE_RESTARTS},)
ifeq (${MAKECMDGOALS},)

${thisMakefilePath}:
	@${MP_JAVA_PATH}java -jar "${thisMakefileDir}ezbl_tools.jar" --make_editor -updateid="${thisMakefilePath}" || (echo EZBL: BOOTID_HASH updated in ${thisMakefilePath} && rm ${FINAL_IMAGE})

ezbl_start_build:
	@echo EZBL: Editing MakeFile-${CONF}.mk to enable EZBL Bootloader generation
	${MP_JAVA_PATH}java -jar "${thisMakefileDir}ezbl_tools.jar" --make_editor -conf=${CONF} -min_free_psv=-1 -warn_free_psv=8192 -mcpu=${MP_PROCESSOR_OPTION} -linkscript=${MP_LINKER_FILE_OPTION} -compiler_folder=${MP_CC_DIR} -java=${MP_JAVA_PATH} -project_name="${PROJECTNAME}" -path_to_ide_bin="${PATH_TO_IDE_BIN}" -artifact="${FINAL_IMAGE}"

endif
endif



# Post-build code to copy generated .merge.S/.merge.gld files to Application projects that will be built for this bootloader.
ezbl_post_build: .build-impl
	$(foreach dest,$(appMergeDestFolders), $(call COPY_FILES_TO_DIR,"${DISTDIR}/${PROJECTNAME}.merge.gld" "${DISTDIR}/${PROJECTNAME}.merge.S", "${dest}"))


# Pre-clean code (helps MPLAB X IDE continue if clean fails normally on Windows due to file system file locks). Must have "Use "clean" target from the makefile." checked in the MPLAB X IDE Embedded Project settings to invoke this.
ezbl_clean:
	(test -e "${DISTDIR}" && rm -f "${DISTDIR}/*" 2>${NUL}) && (test "dist/${CONF}" 1>${NUL} && rm -rf "dist/${CONF}") | (test "build/${CONF}/${IMAGE_TYPE}" && rm -rfd "build/${CONF}/${IMAGE_TYPE}/*" 1>${NUL} && rm -rf "build/${CONF}" 1>${NUL}) | (test "debug/${CONF}" && rm -rf "debug/${CONF}/${IMAGE_TYPE}/*" 1>${NUL} && rm -rfd "debug/${CONF}" 1>${NUL}) | (test "disassembly" && rm -rfd "disassembly" 1>${NUL}) || true



.PHONY: ezbl_clean ezbl_start_build  ${thisMakefilePath}
.build-impl: ezbl_start_build
.build-post: ezbl_post_build
.clean-pre: ezbl_clean



# Auto-generated SHA-256 hash of vendor, model, name and other BOOTID strings
# at the top of this file, including the quotations marks. This must be kept
# here so the computed values may be passed on the command line to the linker
# to be tested by the code when the App uploads a .bl2 file. Also, this data
# is passed along in the .merge.S file so the Application is built to target 
# this bootloader.
BOOTID_HASH  = 4F7D8C3F53754D1B5401E4C9D1E87E8E4854ADEDCD24AEBD57F877D8FE07C793
BOOTID_HASH3 = 0x4854ADED
BOOTID_HASH2 = 0xCD24AEBD
BOOTID_HASH1 = 0x57F877D8
BOOTID_HASH0 = 0xFE07C793



# Required addition to pass the BOOTID hash computed values to the linker for 
# .bl2 metadata inclusion and run-time Bootloader upload testing.
MP_EXTRA_LD_POST += ,--defsym=_BOOTID_HASH0=${BOOTID_HASH0},--defsym=_BOOTID_HASH1=${BOOTID_HASH1},--defsym=_BOOTID_HASH2=${BOOTID_HASH2},--defsym=_BOOTID_HASH3=${BOOTID_HASH3}
unexport MP_EXTRA_LD_POST


# Suppress make[x] Entering directory messages
MAKEFLAGS += --no-print-directory


# Macro to copy one or more files to a destination folder, following symbolic
# links (-L)
# ${1} - space separated source file(s)
# ${2} - dest directory
define COPY_FILES_TO_DIR =
	-cp -L ${1} ${2}

endef

ifeq ($(shell uname), windows32)
NUL := NUL
else
NUL := /dev/null
endif



# Miscellaneous includes so we can use the ${CONF}, ${MP_JAVA_PATH},
# ${MP_PROCESSOR_OPTION}, ${DISTDIR}, etc. MPLAB generated make variables in
# this make script.
ifeq (,$(findstring nbproject/Makefile-impl.mk,$(MAKEFILE_LIST)))
include nbproject/Makefile-impl.mk
endif
ifeq (,$(findstring nbproject/Makefile-variables.mk,$(MAKEFILE_LIST)))
include nbproject/Makefile-variables.mk
endif
ifeq (,$(findstring nbproject/Makefile-${CONF}.mk,$(MAKEFILE_LIST)))
include nbproject/Makefile-local-${CONF}.mk
endif
ifeq (,$(findstring nbproject/Makefile-${CONF}.mk,$(MAKEFILE_LIST)))
IGNORE_LOCAL:=TRUE
include nbproject/Makefile-${CONF}.mk
IGNORE_LOCAL:=FALSE
endif
export
unexport IGNORE_LOCAL
