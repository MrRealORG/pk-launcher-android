/*
 * PK Launcher for Android
 * Copyright (C) 2026 Team RealHackers (MrRealORG)
 *
 * This file is part of PK Launcher for Android, a fork of MojoLauncher
 * (https://github.com/MojoLauncher/MojoLauncher), itself based on PojavLauncher.
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */
package dev.pklauncher.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import git.artdeell.mojo.R;
import git.artdeell.mojo.authenticator.AuthType;
import git.artdeell.mojo.authenticator.accounts.Account;
import git.artdeell.mojo.authenticator.accounts.Accounts;

/**
 * PK account rule (firm): playing Minecraft: Java Edition needs a Microsoft account that
 * owns the game.
 *
 * <ul>
 *   <li>A Microsoft account is a verified owner only when the Minecraft services login
 *   succeeded and returned a Java Edition profile (MicrosoftBackgroundLogin.checkProfile
 *   throws "mc_not_owned" otherwise), so it has a real access token, refresh token and
 *   profile UUID.</li>
 *   <li>An offline nickname ("local" account) may be created and used only while a verified
 *   Microsoft owner account is signed in on this device - the same rule as PK Launcher on
 *   desktop.</li>
 *   <li>Third-party auth servers (Ely.by) and anything else are not a play path.</li>
 * </ul>
 */
public final class OwnershipGate {
    private static final String NULL_UUID = "00000000-0000-0000-0000-000000000000";

    private OwnershipGate() {}

    /** @return true if this account is a Microsoft account with a verified Java Edition profile. */
    public static boolean isVerifiedMicrosoftOwner(@Nullable Account account) {
        if (account == null) return false;
        if (account.authType != AuthType.MICROSOFT) return false;
        if (account.isLocal()) return false;
        if (account.profileId == null || NULL_UUID.equals(account.profileId)) return false;
        if (account.refreshToken == null || "0".equals(account.refreshToken)) return false;
        return account.username != null && !account.username.isEmpty();
    }

    /** Find a verified Microsoft owner account saved on this device, or null. Do not call on the UI thread. */
    @Nullable
    public static Account findVerifiedOwner() {
        try {
            Accounts accounts = Accounts.load();
            for (Account account : accounts.accounts) {
                if (isVerifiedMicrosoftOwner(account)) return account;
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static boolean hasVerifiedOwner() {
        return findVerifiedOwner() != null;
    }

    /**
     * Check whether this account may start the game.
     * @return 0 when play is allowed, otherwise a string resource explaining why not.
     */
    public static int checkCanPlay(@Nullable Account account) {
        if (account == null) return R.string.pk_gate_no_account;
        switch (account.authType) {
            case MICROSOFT:
                return isVerifiedMicrosoftOwner(account) ? 0 : R.string.pk_gate_not_owner;
            case LOCAL:
                return hasVerifiedOwner() ? 0 : R.string.pk_gate_offline_needs_owner;
            case ELY_BY:
            default:
                return R.string.pk_gate_third_party;
        }
    }

    /** Thrown by the game runner when the gate refuses a launch. */
    public static final class NotAllowedException extends RuntimeException {
        public final int messageRes;
        public NotAllowedException(int messageRes, @NonNull String message) {
            super(message);
            this.messageRes = messageRes;
        }
    }
}
