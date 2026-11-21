# PK Launcher for Android: changes from MojoLauncher

Base: MojoLauncher `v3_openjdk` @ 147e849742697327787c998d6f7f3e0db6a38444 (full history kept;
branch `upstream-mojo` mirrors it).

- Branding: app name PK Launcher, application id `dev.pklauncher.android` (Java namespace
  `git.artdeell.mojo` kept so upstream code and headers stay intact), PK icon/round icon/adaptive
  icon, PK colours in `res/values/colors.xml`, solid PK-red Play button.
- New PK code in `app_mojo/src/main/java/dev/pklauncher/android/`: OwnershipGate, QuickJoin,
  ServerPing, PkTheme, PkUi, PkSceneView, PkSplash, and the Servers, Mods, News, Themes,
  Bug report, About and Rust Edition screens.
- Home screen (`res/layout/fragment_launcher.xml`) redesigned as a PK header + tile grid; the
  landscape variant was removed so one layout serves both orientations.
- Fragment transitions (`res/anim/pk_*.xml`, `Tools.swapFragment`).
- Game boot overlay rebranded (PK mark with a pulse, PK-red spinner).
- Accounts: see ACCOUNTS.md.
- CI: `.github/workflows/android.yml` builds `assembleFullDebug` and uploads
  `pk-launcher-android-debug`. Upstream's Play Store upload, nightly release and AAB steps were
  removed.
