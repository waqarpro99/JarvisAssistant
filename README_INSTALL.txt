JARVIS ASSISTANT - GITHUB APK BUILD

STATUS: Source inspected and build configuration corrected. No APK build or
phone test completed. This is a JARVIS voice-command app, not Disc Pool.

1. Extract JarvisAssistant-Corrected.zip on your computer.
2. Create/open your GitHub repository. Use Add file > Upload files.
3. Upload CONTENTS into repository root: app/, .github/ and the root files.
   Do not upload the ZIP itself or nest everything inside another folder.
4. Commit changes. Verify Code shows app/ and settings.gradle.kts at root.
5. Verify .github/workflows/build-apk.yml exists in GitHub. If .github was
   skipped, use Add file > Create new file, enter the full filename
   .github/workflows/build-apk.yml and paste the supplied workflow text.
6. Open Actions > Build Jarvis APK. A push starts it. To run manually, put
   the workflow on the default branch and select Run workflow.
   Enable Actions if the repository asks you to do so.
7. Open the run. Only a green successful build confirms APK compilation.
8. Under Artifacts, download jarvis-assistant-apk; extract app-debug.apk.
9. Transfer APK to an Android 7.0+ phone and install. Android may request
   permission to install from the app used to open the APK.
10. Open Jarvis, grant microphone permission and test its commands.

If a run fails, open the red step and copy its error log.
The output is a debug APK, not a Play Store release.
No Gradle wrapper is included. CI installs Gradle 8.7, JDK 17 and SDK 34.
Use gradle, not ./gradlew. Manual local builds require these tools too.

KNOWN RUNTIME LIMITATIONS (original application source retained):
- ur-PK recognition can return Urdu script; matching uses Latin words.
- A compatible speech provider and TTS engine are required. Availability,
  supported languages and offline operation depend on the phone.
- Camera/dialer/search launches need testing when no handler is installed.
- No general AI backend, wake-word listening or Disc Pool automation exists.
