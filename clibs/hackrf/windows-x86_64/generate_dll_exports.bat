@echo off
net.exe session 1>NUL 2>NUL || (pause>nul|set/p ="You must this script as Admin to work, press any key to exit..." & exit /b 1)
cd /d %~dp0

dumpbin help 1>nul 2>nul
IF "%errorlevel%" == "9009" (
    echo dumpbin not found in path.
	pause
    exit /b 1
)

lib 1>nul 2>nul
IF "%errorlevel%" == "9009" (
    echo linker not found in path.
	pause
    exit /b 1
)

set dialog="about:<input type=file id=FILE><script>FILE.click();new ActiveXObject
set dialog=%dialog%('Scripting.FileSystemObject').GetStandardStream(1).WriteLine(FILE.value);
set dialog=%dialog%close();resizeTo(0,0);</script>"

for /f "tokens=* delims=" %%p in ('mshta.exe %dialog%') do set "file=%%p"
if "%file:~-4%" neq ".dll" (
	echo Input must be a DLL file, aborting...
	pause
    exit /b 1
)
echo Selected file is : %file%

for %%x in (%file%) do set libname=%%~nx
echo Library name is: %libname%
set filename=%file:~0,-4%

dumpbin /exports %file% > %filename%.exports
echo LIBRARY %libname% > %filename%.def
echo EXPORTS >> %filename%.def
for /f "skip=19 tokens=4" %%A in (%filename%.exports) do echo %%A >> %filename%.def
lib /def:%filename%.def /out:%filename%.lib /machine:x64

rm %filename%.exports
rm %filename%.def
rm %filename%.exp