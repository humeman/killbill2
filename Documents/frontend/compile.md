### [Kill Bill 2](../../README.md) → [Docs](../README.md) → [Frontend](README.md) → Compile
---

# Compilation Instructions

Kill Bill 2 uses libGDX paired with Android Studio.

## Run desktop version via commandline
You can use `gradlew` to execute the project.
```sh
./gradlew desktop:run
```

## Run Android version via Android Studio
Create `Frontend/local.properties` with the following content, replacing `<YOUR USERNAME>` with your username (no brackets):
```properties
# Location of the android SDK
sdk.dir=C:/Users/<YOUR USERNAME>/AppData/Local/Android/Sdk
```

Then, open up the project (`Frontend` folder) in Android Studio. Use `Open Project`, NOT `New Project`.

Finally, follow the instructions [here](https://libgdx.com/wiki/start/import-and-running#desktop) to configure your working directory so assets load properly.

Once this one-time setup is finished, you can click the play icon at the top of the screen to emulate it on an Android device. If you have no devices configured yet, open the Device Manager and click the Plus icon.