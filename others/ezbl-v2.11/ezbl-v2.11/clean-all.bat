::make --directory=ezbl_lib clean-all
::del ezbl_lib\ezbl_lib.a                             2>NUL
::del ezbl_lib32mm\ezbl_lib32mm.a                     2>NUL
::for /R %%i in (private,ezbl_cache) do @rmdir /s /q %%i 2>NUL
del /q /s *.bash                                    2>NUL
del /q /s Makefile-*                                2>NUL
for /R %%i in (build,dist,debug,disassembly) do @rmdir /s /q %%i 2>NUL
