echo hi
device=HT9A4LV01921
#device=033c20c142017617
set -x
adb -s $device shell mkdir -p /mnt/sdcard/tmp
adb -s $device push build/jar/ssp.jar /cache/
adb -s $device shell dalvikvm -classpath /cache/ssp.jar SystemServerProbe
adb -s $device shell dvz -classpath /cache/ssp.jar SystemServerProbe
#adb -s $device shell app_process -classpath /mnt/sdcard/tmp/ssp.jar / SystemServerProbe
#adb -s $device shell app_process  /mnt/sdcard/tmp/  SystemServerProbe
