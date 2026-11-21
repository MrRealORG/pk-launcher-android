# Accounts in PK Launcher for Android

**Rule:** playing needs a Microsoft account that owns Minecraft: Java Edition. An offline
nickname is allowed only while a signed-in Microsoft account on the same phone has a verified
Java Edition profile, the same rule as PK Launcher on desktop. There are no PK Cloud or other
accounts that can start the game.

## What MojoLauncher did (upstream, v3_openjdk @ 147e849)

| Path | Upstream behaviour |
|---|---|
| Microsoft sign-in | OAuth -> Xbox Live -> XSTS -> Minecraft services; `checkProfile` throws `mc_not_owned` when the account has no Java profile, so a saved Microsoft account always had a profile |
| **Local account** (`LocalLoginFragment` -> `MOJANG_LOGIN_TODO` -> `AccountSpinner`) | Anyone could type a nickname and play offline, no Microsoft account needed |
| **Ely.by** (`ElyByLoginFragment`, `AuthType.ELY_BY`) | Third-party auth server via authlib-injector; no Minecraft ownership involved |
| `GameRunner.launchGame` | If the auth server (`injectorUrl`) did not answer, the account was silently converted with `account.toLocal()` and the game started offline |

## What PK changed

| File | Change |
|---|---|
| `dev/pklauncher/android/OwnershipGate.java` (new) | One place for the rule. `isVerifiedMicrosoftOwner` = `AuthType.MICROSOFT`, real access token, refresh token and non-zero profile UUID (only written after `checkProfile` succeeded). `checkCanPlay`: Microsoft owner -> allowed; local nickname -> allowed only if a verified owner account is saved on the device; Ely.by / anything else -> refused |
| `fragments/SelectAuthFragment.java` | Ely.by button hidden and unwired. "Local account" renamed **Offline nickname (Java owners only)**; it opens only when a verified owner exists, otherwise a dialog points to Microsoft sign-in |
| `fragments/LocalLoginFragment.java` | Re-checks ownership before creating the nickname |
| `com/kdt/mcgui/AccountSpinner.java` | The nickname is created only if `findVerifiedOwner()` returns an account; the nickname stores `ownerProfileId` (the owner's profile id). The Ely.by login listener is no longer registered |
| `authenticator/accounts/Account.java` | New `ownerProfileId` field (informational link for nicknames) |
| `LauncherActivity.java` | Play is refused with a "Microsoft account needed" dialog when `checkCanPlay` fails |
| `utils/jre/GameRunner.java` | Defence in depth in the game process: `launchGame` throws `OwnershipGate.NotAllowedException` when `checkCanPlay` fails. The silent `toLocal()` downgrade is removed: if an auth server does not answer, the launch fails instead of going offline |

Kept from upstream: the Microsoft login itself (client id, token chain, ownership check),
automatic token refresh, and playing with an already verified Microsoft account when the phone
is offline (like the official launcher's offline mode).

## Known limits

- A Microsoft account whose ownership ends later (for example Game Pass expires) keeps its
  saved token until the next refresh fails; the refresh then errors and does not overwrite the
  account, but the old account stays saved. Removing the account is up to the user.
- Old Ely.by or local accounts already saved by a previous install are not deleted; they simply
  cannot start the game (Ely.by never; local only while an owner account is present).
- Upstream's `NoBlocklist` option (a Java agent that disables Mojang's server blocklist) is still
  present in Settings as in MojoLauncher; it is off by default. The owner may want it removed.
