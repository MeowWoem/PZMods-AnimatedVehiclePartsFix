# [B42 SP Fix] Animated Vehicle Parts Fix — Building it yourself

This mod is **100% open source**. If you don't trust the Steam Workshop installer (or the provided `.jar`), you can recompile the Java code yourself in a few minutes. Here's how.

## Requirements

- **Project Zomboid Build 42** installed (via Steam).
- **ZombieBuddy** installed — required for compilation since the fix uses its bytecode-injection API. ZombieBuddy's `.jar` must be present in your Project Zomboid install folder.
- **JDK 25**, downloaded and installed from one of these:
  - Azul Zulu JDK 25: https://www.azul.com/downloads/?version=java-25-lts&package=jdk#zulu
  - Oracle JDK 25: https://www.oracle.com/java/technologies/downloads/#java25

After installing, make sure `javac` is accessible (ideally by adding it to your `PATH`, or by setting `JDK_BIN` — see below).

## Getting the source

Download the mod's source code:

👉 https://github.com/MeowWoem/PZMods-AnimatedVehiclePartsFix/archive/refs/heads/main.zip

Then extract the archive wherever you like.

## Building the mod

1. Open the extracted folder, then go to `src/java`.
2. Open `build.bat` in a text editor (Notepad, VS Code, etc.) and check/adjust the `LIBS_DIR` variable at the top of the script: it must point to the folder containing both `projectzomboid.jar` and `ZombieBuddy.jar`. Default value:

   ```bat
   set "LIBS_DIR=C:\Program Files (x86)\Steam\steamapps\common\ProjectZomboid"
   ```

   Adjust this path if your Steam install is located elsewhere.

3. (Optional) If you have several JDKs installed and the `javac` on your `PATH` doesn't point to JDK 25, set the path to your JDK 25's `bin` folder in the `JDK_BIN` variable:

   ```bat
   set "JDK_BIN=C:\Program Files\Zulu\zulu-25\bin"
   ```

4. Double-click `build.bat` to run the build.

The script will:
- Check that `javac` is available.
- Check that `projectzomboid.jar` and `ZombieBuddy.jar` exist in `LIBS_DIR`.
- Compile all `.java` files under `src`.
- Automatically generate `AnimatedVehiclePartsFix.jar` directly inside `Contents/mods/AnimatedVehiclePartsFix/42/media/java/`, replacing the one shipped in the repo.

If everything goes well, you'll see:

```
[OK] Compilation succeeded. ...
[OK] Jar generated: ...
```

## Troubleshooting

- **`javac not found`**: the JDK isn't installed or isn't on your `PATH`. Reinstall JDK 25 or set `JDK_BIN`.
- **`Not found: ...projectzomboid.jar` or `...ZombieBuddy.jar`**: `LIBS_DIR` doesn't point to the right folder. Fix it with the exact path to your Project Zomboid install (and make sure ZombieBuddy is installed there).
- **Compilation error like "wrong version" / "cannot access"**: this usually means the `javac` being used isn't JDK 25. Set `JDK_BIN` to the correct path.

## Once built

The generated `.jar` is placed directly inside the mod folder (`Contents/mods/AnimatedVehiclePartsFix/`), ready to be used as-is as a local mod in Project Zomboid.

---

If you run into trouble following these steps, open an issue on the GitHub repo instead of speculating about the mod's contents — the code is public, anyone is free to check it.
