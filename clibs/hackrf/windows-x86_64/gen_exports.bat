@echo off
net.exe session 1>NUL 2>NUL || (pause>nul|set/p ="You must this script as Admin to work, press any key to exit..." & Exit /b 1)
cd /d %~dp0

dumpbin help 1>nul 2>nul
IF "%errorlevel%" == "9009" (
    echo dumpbin not found in path.
    Exit /b 1
)

lib 1>nul 2>nul
IF "%errorlevel%" == "9009" (
    echo linker not found in path.
    Exit /b 1
)

dumpbin /exports hackrf.dll > hackrf.exports
echo LIBRARY hackrf > hackrf.def
echo EXPORTS >> hackrf.def
for /f "skip=19 tokens=4" %%A in (hackrf.exports) do echo %%A >> hackrf.def
rm hackrf.exports
lib /def:hackrf.def /out:hackrf.lib /machine:x64
rm hackrf.def
rm hackrf.exp