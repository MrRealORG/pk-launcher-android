<h1 align="center">PK Launcher for Android</h1>

<p align="center"><img src="app_mojo/src/main/res/drawable-nodpi/pk_mark.png" width="120" alt="PK Launcher logo"></p>

<p align="center"><b>Play Minecraft: Java Edition on your phone, in PK style.</b><br>
Based on <a href="https://github.com/MojoLauncher/MojoLauncher">MojoLauncher</a> and <a href="https://github.com/PojavLauncherTeam/PojavLauncher">PojavLauncher</a> · GNU LGPL v3.0</p>

[![PK Launcher Android CI](https://github.com/MrRealORG/pk-launcher-android/actions/workflows/android.yml/badge.svg)](https://github.com/MrRealORG/pk-launcher-android/actions/workflows/android.yml)

> PK Launcher is not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.
> **Playing requires a Microsoft account that owns Minecraft: Java Edition.**

## Credits first

PK Launcher for Android is a fork of **[MojoLauncher](https://github.com/MojoLauncher/MojoLauncher)** by the MojoLauncher contributors, which is itself based on **[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)** by the PojavLauncherTeam. All of the hard parts (the Java runtime on Android, the renderers, input, controls, mod loader installers, modpack import) are their work. The full MojoLauncher history is kept in this repository, copyright headers are untouched, and the project stays under the [GNU LGPL v3.0](LICENSE). The upstream README is kept at [docs/MOJOLAUNCHER-README.md](docs/MOJOLAUNCHER-README.md). The app's **About** screen carries the same credit.

## What PK adds

| | |
|---|---|
| **PK branding** | PK Launcher name, app id `dev.pklauncher.android` (debug builds: `dev.pklauncher.android.debug`), the flat lightning-P icon, PK dark theme and colours |
| **Animated splash + loading** | PK mark pops in with a bolt sweep and a loading bar on cold start; pulsing PK mark while the game boots; smooth screen transitions; staggered list entrances. "Reduce motion" (and Android's animator scale 0) turns them off |
| **Servers** | Saved + popular servers with a real Server List Ping **from the phone** (latency, players, version, coloured MOTD, server icon; SRV records resolved over DNS-over-HTTPS). **Join** launches the selected instance straight into the server: `--quickPlayMultiplayer` on 1.20+, `--server/--port` on older versions |
| **Mods (Modrinth)** | Search Modrinth filtered to the selected instance's Minecraft version and loader, install the newest compatible file into the instance's `mods` folder with SHA-1 verification, required dependencies included. Modpacks still use MojoLauncher's Modrinth/CurseForge installer |
| **PK News** | PK Launcher releases (`MrRealORG/pk-updates`) and Minecraft: Java Edition patch notes, cached offline |
| **Themes** | PK Default, Halloween Night (pixel moon, bats, jack-o'-lantern), Rust Edition, Lush Caves, Deep Ocean, Neon Rain, Pixel Dusk, Aurora, Sakura Evening, Taiga Snow. Same tokens as PK desktop. A Halloween suggestion appears in season, never forced |
| **Report a bug** | Builds a prefilled GitHub issue URL (title, area, steps, device info) for this repo. Nothing is sent until you submit on GitHub |
| **Rust Edition** | An honest status screen: Horizon has no Android build yet (see [docs/HORIZON-ANDROID.md](docs/HORIZON-ANDROID.md)) |
| **Account rule** | Microsoft account that owns Java Edition required to play; offline nickname only for owners; Ely.by removed. See [docs/ACCOUNTS.md](docs/ACCOUNTS.md) |

## Download

Every push builds a debug APK in GitHub Actions: open the latest green run of **PK Launcher Android CI** and download the `pk-launcher-android-debug` artifact. Debug builds are signed with the debug key from upstream and are for testing only. There is no Play Store build and no release yet.

## Building

Same as MojoLauncher:

```
git clone --recurse-submodules https://github.com/MrRealORG/pk-launcher-android.git
cd pk-launcher-android
./gradlew :app_mojo:assembleFullDebug
```

The CI also downloads MojoLauncher's prebuilt LTW renderer and Mesa driver AARs into `app_mojo/libs` (see `.github/workflows/android.yml`).

## License

GNU Lesser General Public License v3.0, the same as MojoLauncher and PojavLauncher. See [LICENSE](LICENSE). PK's own code lives mostly in `app_mojo/src/main/java/dev/pklauncher/android/`; changes to upstream files are marked with `PK:` comments.

## Third party components, licenses and sources (when applicable)
- [MojoLauncher](https://github.com/MojoLauncher/MojoLauncher): [GNU LGPLv3 License](https://github.com/MojoLauncher/MojoLauncher/blob/v3_openjdk/LICENSE)
- [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher): [GNU LGPLv3 License](https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/LICENSE)
- Android Support Libraries: [Apache License 2.0](https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt).
- [Holy GL4ES](https://github.com/artdeell/gl4es_extra_extra/): [MIT License](https://github.com/ptitSeb/gl4es/blob/master/LICENSE).
- [OpenJDK](https://github.com/PojavLauncherTeam/openjdk-multiarch-jdk8u): [GNU GPLv2 License](https://openjdk.java.net/legal/gplv2+ce.html).
- [GLFW](https://github.com/MojoLauncher/glfw): [zlib license](https://github.com/MojoLauncher/glfw/blob/glfw34/LICENSE.md)
- [SDL](https://github.com/MojoLauncher/MojoSDL): [zlib license](https://github.com/MojoLauncher/MojoSDL/blob/main/LICENSE.txt)
- [LWJGL2-GLFW](https://github.com/MojoLauncher/lwjgl2-glfw): 3-Clause BSD license
- [LWJGL3](https://github.com/LWJGL/lwjgl3): [BSD-3 License](https://github.com/LWJGL/lwjgl3/blob/master/LICENSE.md).
- [mojoexec](https://github.com/MojoLauncher/mojoexec): [MIT License](https://github.com/MojoLauncher/mojoexec/blob/master/LICENSE)
- [Mesa 3D Graphics Library](https://gitlab.freedesktop.org/mesa/mesa): [MIT License](https://docs.mesa3d.org/license.html).
- [pro-grade](https://github.com/pro-grade/pro-grade) (Java sandboxing security manager): [Apache License 2.0](https://github.com/pro-grade/pro-grade/blob/master/LICENSE.txt).
- [Authlib-Injector](https://github.com/yushijinhun/authlib-injector) (Used for authorisation via ely.by): [AGPL-3.0](https://github.com/yushijinhun/authlib-injector/blob/develop/LICENSE).
- [alsoft](https://github.com/kcat/openal-soft/) (Audio output library): [GNU Library General Public License](https://github.com/kcat/openal-soft/blob/master/COPYING) and [Modified PFFFT](https://github.com/kcat/openal-soft/blob/master/LICENSE-pffft).
- [oboe](https://github.com/google/oboe): [Apache License 2.0](https://github.com/google/oboe/blob/main/LICENSE).
- Thanks to [Mineskin](https://mineskin.eu/) for providing Minecraft avatars.
- [Modrinth API](https://docs.modrinth.com/) (mod search and downloads; mods keep their own licences).
