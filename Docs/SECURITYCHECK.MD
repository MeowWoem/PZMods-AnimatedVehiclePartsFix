# Security & Integrity Verification Guide

This document is provided to ensure full transparency regarding the safety and integrity of **Animated Vehicle Parts Fix**. 

Because this mod utilizes a Java patch (`.jar`) to resolve issues that cannot be addressed purely through Lua scripting, we encourage users to independently verify the safety of the compiled binaries. Below are three simple methods you can use to verify that the mod is completely safe.

---

## 🛡️ Pre-Scanned Report
You can view the official automated scan report for the release binary directly on VirusTotal:
- **VirusTotal Scan:** [View Latest Detection Report](https://www.virustotal.com/gui/file/ad9d0775e8fa7d645ac01be16529e4244312cb596afa9d577003582b493d1cec)

---

## Method 1: Scan the `.jar` File Yourself on VirusTotal

You don't have to take my word or the static link for it—you can easily scan the file installed on your own computer.

1. Navigate to your local Steam Workshop directory for Project Zomboid:
   `Steam\steamapps\workshop\content\108600\3802266453\mods\AnimatedVehiclePartsFix\42\media\java\`
2. Locate the `.jar` file inside this folder.
3. Open [VirusTotal.com](https://www.virustotal.com) in your web browser.
4. Drag and drop the `.jar` file into the upload zone on VirusTotal.
5. Review the results: over 70 commercial antivirus engines will verify that the binary contains no malware, trojans, or malicious payloads.

---

## Method 2: Online Decompilation & Source Code Comparison

If you want to inspect the exact Java bytecode executed by the mod, you can decompile the `.jar` file directly in your browser without installing any developer tools.

1. Locate the `.jar` file in your Workshop directory (see path in Method 1).
2. Go to [jdec.app](https://jdec.app/) (a web-based Java decompiler).
3. Drag and drop the `.jar` file into the website.
4. Open the decompiled code and compare it line-by-line with my official open-source code on GitHub:
   - **Official GitHub Source Files:**
   + [AnimatedVehiclePartsFix.java](https://github.com/MeowWoem/PZMods-AnimatedVehiclePartsFix/blob/main/Sources/Java/src/io/ameno/AVPF/AnimatedVehiclePartsFix.java)
   + [Patch_BaseVehicle.java](https://github.com/MeowWoem/PZMods-AnimatedVehiclePartsFix/blob/main/Sources/Java/src/io/ameno/AVPF/Patch_BaseVehicle.java)
5. You will see that the compiled `.jar` matches the open-source repository exactly, containing no hidden network calls or disk operations.

---

## Method 3: Automated AI Behavior Analysis (Claude.ai)

Did you know that modern AI models like Claude can instantly analyze Java bytecode and explain its exact behavior in plain English? If you don't read Java code, you can ask an AI to inspect it for suspicious patterns.

1. Go to [Claude.ai](https://claude.ai/) and start a new conversation.
2. Drag and drop the `.jar` file (or the decompiled Java code from Method 2) directly into the chat prompt.
3. Type the following prompt:
   > *"Analyze the behavior of this `.jar` file. Check if it attempts to access network sockets, read sensitive files, execute external commands, or do anything malicious."*
4. Claude will inspect the code structure and confirm that the file only performs standard game memory/class patching for Project Zomboid.

---

*Transparency is my priority. If you have any technical questions or need further clarification, feel free to open an issue on my GitHub repository.*