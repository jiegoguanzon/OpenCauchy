#  Makes Microchip Easy Bootloader Library ezbl_comm host communications utility
#  
#     build                    builds the Release/ezbl_comm executable
#     clean                    remove all object and executable files created by 
#                              the 'build' objective
#     help                     print this help message

build: Release/ezbl_comm

Debug/logger.o: ezbl_comm/logger.cpp
	gcc -c -o Debug/logger.o ezbl_comm/logger.cpp
	
Debug/ezbl_update.o: ezbl_comm/ezbl_update.cpp
	gcc -c -o Debug/ezbl_update.o ezbl_comm/ezbl_update.cpp

Release/ezbl_comm: Release Debug Debug/logger.o Debug/ezbl_update.o
	gcc -o Release/ezbl_comm Debug/logger.o Debug/ezbl_update.o
	
Release:
	mkdir Release

Debug:
	mkdir Debug	

.PHONY: clean help
clean:
	-rm -rf ./build
	-rm -rf ./Debug
	-rm -rf ./Release


help:
	@echo Makes Microchip Easy Bootloader Library ezbl_comm host communications utility
	@echo 
	@echo build                    builds the Release/ezbl_comm executable
	@echo clean                    remove all object and executable files created by 
	@echo                          the 'build' objective
	@echo help                     print this help message