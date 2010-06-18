call setenv.bat

echo *
echo *
echo **********************************
echo *** building joystickjni1.dll
echo **********************************
echo *
echo *

%JDK%\bin\javah -classpath %CLASSDIR%\bluetoothdiscover equip.ect.components.bluetoothdiscover.BluetoothDiscover

set PLATFORM_SDK=C:\Program Files\Microsoft SDK
set LIB=%PLATFORM_SDK%\lib;%LIB%
set INCLUDE=%PLATFORM_SDK%\include;%INCLUDE%

cl ..\src\equip\ect\components\bluetoothdiscover\win32bluetooth.cpp /Od /I "C:\Program Files\Microsoft SDK\include" /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /Gm /EHsc /RTC1 /MLd /W3 /nologo /Wp64 /ZI /TP "-I." "-I%JDK%\INCLUDE"  "-I%JDK%\INCLUDE\WIN32" -Febluetoothdiscoverjni1.dll -MT -LD /link irprops.lib ws2_32.lib 

rem *** copy binaries
del ..\resources\common\bluetoothdiscoverjni1.dll
move bluetoothdiscoverjni1.dll ..\resources\common

rem *** clean up
del *.exp
del *.obj
del *.lib
del *.h
