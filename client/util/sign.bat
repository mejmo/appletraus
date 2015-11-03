@echo off

set KEYSTORE=keystore-ots1
set SIGFILE=keyentry
set ALIAS=keyentry-ots1

echo Using %KEYSTORE% as location for keystore
echo Using %SIGFILE% as name of .SF/.DSA file
echo Using %ALIAS% as alias

rem Sign JARs
rem ---------
echo Signing %JARNAME%...
"C:\Program Files\Java\jdk1.7.0_71\bin\jarsigner.exe" -tsa "https://timestamp.geotrust.com/tsa" -keystore "O:\certs\%KEYSTORE%" -sigfile %SIGFILE% "client-jar-with-dependencies.jar" -signedjar "appletraus-client.jar" %ALIAS% < "D:\jarsigner\pwd1.txt"

GOTO:EOF
