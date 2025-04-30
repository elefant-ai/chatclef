# ChatClef  
<p align="center">
<img src="https://github.com/user-attachments/assets/52afdd23-3bc9-47c1-9e16-d3b20a6e2e80" width="45%"/>
</p>

## ChatClef is a Minecraft AI copilot mod that can play the game for you or with you. 

Download the [Player2 app](https://player2.game/), and install Fabric API and this mod in Minecraft to play. You **do not** need to install AltoClef or Baritone separately.

It automates Minecraft tasks from start to finish, including beating the game completely solo. Press Z key to talk to the AI using voice chat.

👉 [Check releases](https://github.com/elefant-ai/chatclef/releases)

## What is ChatClef?

ChatClef is a client-side AI mod designed to be your Minecraft copilot/friend.
If you can open a second client, ChatClef can take over that instance and act as a second AI-controlled player in multiplayer.

To get the AI working, you need to have the [Player2 app](https://player2.game/), install Fabric API and this mod in Minecraft, and start Minecraft.
Once installed, the AI will be able to:

- Chat with you
- Complete tasks for you
- Beat the game solo
- Or just mess around with you like a chaotic Minecraft sidekick

It builds on top of [Player2](https://player2.game), [AltoClef](https://github.com/MiranCZ/altoclef) and [Baritone](https://github.com/cabaletta/baritone). It’s completely free, open-source, and constantly being improved.

---

## How it works
This mod adds the Player2 interface to [AltoClef](https://github.com/MiranCZ/altoclef) and [Baritone](https://github.com/cabaletta/baritone).
The Player2 App provides free STT, TTS, and LLM functions. Make sure the Player2 App is running while using this mod.
Press Z to talk to the AI using voice. You can change the key binding in settings.
Memory and conversation history are stored in the mod folder under the AI's name if you need to view them.
Be careful not to edit the file if you don't know what you are doing, we won't be responsible if your edit cause any problems.

---

## Download

**Note:** After installing, please move/delete your old Baritone configurations if you have any.  
Existing Baritone configs can interfere with ChatClef and introduce bugs (this will be fixed in the future).

---

## Development Setup

Simply open the project in a java IDE such as IntelliJ or Eclipse, then change your SDK to a valid version (we used temurin-21), then wait for the project to build. Once you do, you should be able to run Gradle tasks such as runClient to test the bot, and build to build the jar files. To collect all of the jar files for different versions into a single folder, run `project_root/gather_jars.sh`, which will copy all of the jar files to `project_root/build`.

---


