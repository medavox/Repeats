#!/bin/bash

#build, install and run the APK (debug version) on the connected device
#-------------
/Users/adamh/medi/gradlew app:installDebug
/Users/adamh/Library/Android/sdk/platform-tools/adb shell am start -a com.elucid.medi.activities.TabActivity
