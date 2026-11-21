# Rust Edition (Horizon) on Android: findings

Checked 2026-11-21 against github.com/horizon-rust/horizon (main @ 1d2f619, 2026-11-17) and its
releases.

**Verdict: not integrated. No Android build exists, and a port is not feasible inside this
launcher today.** The app shows an honest "Rust Edition" status screen with a link to Horizon's
roadmap instead.

## Evidence

- **Releases:** the newest releases (`launcher@0.2.96` to `launcher@0.2.98`, 2026-11-21) ship only
  desktop assets: Windows x64/arm64 (`.exe`, `.zip`), macOS arm64/x64 (`.dmg`, `.zip`), Linux x86_64
  (`.AppImage`, `.tar.gz`). No APK, AAB or Android library.
- **Roadmap (`ROADMAP.md`, "Android, iOS and the browser"):** "No official build. Android was first
  closed as not planned, with touch controls, a mobile interface, packaging and loading the
  player's own assets all missing; the roadmap now includes it, iOS and a build for the browser as
  long-term goals."
- **Code:** the only Android-specific code is a `cfg(target_os = "android")` dependency line in
  `crates/horizon-threads` (issue #1751, helper-thread limits). There is no Android target in the
  workspace, no NDK/cargo-ndk setup, no `android-activity` entry point and no Android CI job.
- **Issues:** #8069 "Android Port" (open), #9134 touch controls for Android (open, linked to the
  roadmap; a maintainer note says "It isn't started in this repository: the desktop game comes
  first" and any port must keep Microsoft sign-in and ownership). #1750 (prepare a version without
  Java on the player's machine) is still open and matters for phones.

## What a port would take

1. A Rust Android target (`aarch64-linux-android`) for the client crates via `cargo-ndk`, with an
   `android-activity`/winit entry point packaged as a native activity or a library this launcher
   loads.
2. wgpu on Vulkan (Android) or GLES: Horizon pins a patched `wgpu-hal`; mobile GPU coverage
   (Adreno/Mali) would need testing.
3. Touch controls and a mobile UI inside Horizon (it has none).
4. Asset preparation without desktop Java (#1750), because Horizon builds its data from the
   player's own Java Edition copy.
5. The same Microsoft sign-in + Java ownership check.

Items 3 and 4 are design work in Horizon itself, not something a launcher can add. When the
Horizon Team publishes an Android build, PK can add it behind the same `OwnershipGate`.
