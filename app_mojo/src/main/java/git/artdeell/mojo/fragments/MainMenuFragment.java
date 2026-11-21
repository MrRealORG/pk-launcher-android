package git.artdeell.mojo.fragments;

import static git.artdeell.mojo.Tools.openPath;
import static git.artdeell.mojo.Tools.shareLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.ProgressLayout;
import com.kdt.mcgui.mcVersionSpinner;

import git.artdeell.mojo.CustomControlsActivity;
import git.artdeell.mojo.R;

import git.artdeell.mojo.Tools;
import git.artdeell.mojo.contracts.OpenDocumentWithExtension;
import git.artdeell.mojo.extra.ExtraConstants;
import git.artdeell.mojo.extra.ExtraCore;
import git.artdeell.mojo.instances.Instance;
import git.artdeell.mojo.instances.Instances;
import git.artdeell.mojo.utils.FileUtils;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import dev.pklauncher.android.AboutFragment;
import dev.pklauncher.android.BugReportFragment;
import dev.pklauncher.android.HorizonFragment;
import dev.pklauncher.android.ModsFragment;
import dev.pklauncher.android.NewsFragment;
import dev.pklauncher.android.OwnershipGate;
import dev.pklauncher.android.PkSceneView;
import dev.pklauncher.android.PkTheme;
import dev.pklauncher.android.PkUi;
import dev.pklauncher.android.QuickJoin;
import dev.pklauncher.android.ServersFragment;
import dev.pklauncher.android.ThemesFragment;
import git.artdeell.mojo.MojoApplication;
import git.artdeell.mojo.authenticator.accounts.Account;
import git.artdeell.mojo.authenticator.accounts.Accounts;

import java.io.File;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;

    private final ActivityResultLauncher<Object> mModInstallerLauncher =
            registerForActivityResult(new OpenDocumentWithExtension("jar"), (data)->{
                if(data != null) Tools.launchModInstaller(requireContext(), data);
            });

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button mNewsButton = view.findViewById(R.id.news_button);
        Button mDiscordButton = view.findViewById(R.id.social_media_button);
        Button mCustomControlButton = view.findViewById(R.id.custom_control_button);
        Button mInstallJarButton = view.findViewById(R.id.install_jar_button);
        Button mShareLogsButton = view.findViewById(R.id.share_logs_button);
        Button mOpenDirectoryButton = view.findViewById(R.id.open_files_button);

        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        // PK screens
        view.findViewById(R.id.pk_servers_button).setOnClickListener(v -> openPk(ServersFragment.class, ServersFragment.TAG));
        view.findViewById(R.id.pk_mods_button).setOnClickListener(v -> openPk(ModsFragment.class, ModsFragment.TAG));
        view.findViewById(R.id.pk_themes_button).setOnClickListener(v -> openPk(ThemesFragment.class, ThemesFragment.TAG));
        view.findViewById(R.id.pk_bug_button).setOnClickListener(v -> openPk(BugReportFragment.class, BugReportFragment.TAG));
        view.findViewById(R.id.pk_about_button).setOnClickListener(v -> openPk(AboutFragment.class, AboutFragment.TAG));
        view.findViewById(R.id.pk_horizon_button).setOnClickListener(v -> openPk(HorizonFragment.class, HorizonFragment.TAG));
        mNewsButton.setOnClickListener(v -> openPk(NewsFragment.class, NewsFragment.TAG));
        // MojoLauncher's community link stays available (credit to upstream).
        mDiscordButton.setOnClickListener(v -> Tools.openURL(requireActivity(), getString(R.string.social_media_invite)));
        mCustomControlButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), CustomControlsActivity.class)));
        mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation());
        mEditProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));

        mPlayButton.setOnClickListener(v -> {
            QuickJoin.clear(); // a plain Play never auto-joins a server
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
        });

        mShareLogsButton.setOnClickListener((v) -> shareLog(requireContext()));

        mOpenDirectoryButton.setOnClickListener((v)-> openGameDirectory(v.getContext()));


        mNewsButton.setOnLongClickListener((v)->{
            Tools.swapFragment(requireActivity(), GamepadMapperFragment.class, GamepadMapperFragment.TAG, null);
            return true;
        });

        applyPkTheme(view);
    }

    private void openPk(Class<? extends Fragment> fragment, String tag) {
        Tools.swapFragment(requireActivity(), fragment, tag, null);
    }

    /** PK theme colours + animated header scene + entrance animation. */
    private void applyPkTheme(View view) {
        PkTheme theme = PkTheme.current(requireContext());
        view.setBackgroundColor(theme.bg);
        View bottom = view.findViewById(R.id._background_display_view);
        if (bottom != null) bottom.setBackgroundColor(theme.surface);
        Button play = view.findViewById(R.id.play_button);
        android.graphics.drawable.StateListDrawable playBg = new android.graphics.drawable.StateListDrawable();
        float r = PkUi.dp(requireContext(), 14);
        playBg.addState(new int[]{android.R.attr.state_pressed}, PkUi.rounded(darken(theme.primary), r));
        playBg.addState(new int[]{}, PkUi.rounded(theme.primary, r));
        play.setBackground(playBg);

        FrameLayout header = view.findViewById(R.id.pk_header);
        header.setBackground(PkUi.rounded(theme.surface, PkUi.dp(requireContext(), 18)));
        header.setClipToOutline(true);
        PkSceneView scene = new PkSceneView(requireContext(), theme);
        header.addView(scene, 0, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        ((TextView) view.findViewById(R.id.pk_header_title)).setTextColor(theme.text);
        TextView status = view.findViewById(R.id.pk_header_status);
        status.setTextColor(theme.muted);
        MojoApplication.sExecutorService.execute(() -> {
            Account current = Accounts.getCurrent();
            int deny = OwnershipGate.checkCanPlay(current);
            Tools.runOnUiThread(() -> {
                if (!isAdded()) return;
                status.setText(deny == 0 && current != null
                        ? getString(R.string.pk_home_greeting) + " · " + current.username
                        : getString(R.string.pk_home_signed_out));
            });
        });

        LinearLayout column = view.findViewById(R.id.pk_home_column);
        PkUi.enter(column);
        if (!PkTheme.reduceMotion(requireContext())) {
            play.setScaleX(0.96f);
            play.setScaleY(0.96f);
            play.setAlpha(0f);
            play.animate().scaleX(1f).scaleY(1f).alpha(1f).setStartDelay(150).setDuration(320)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();
        }
    }

    private static int darken(int color) {
        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(color, hsv);
        hsv[2] *= 0.82f;
        return android.graphics.Color.HSVToColor(hsv);
    }

    private void openGameDirectory(Context context) {
        Instance instance = Instances.loadSelectedInstance();
        if(instance == null) {
            Toast.makeText(context, R.string.no_instance, Toast.LENGTH_LONG).show();
            return;
        }
        File gameDirectory = instance.getGameDirectory();
        if(FileUtils.ensureDirectorySilently(gameDirectory)) {
            openPath(context, gameDirectory, false);
        }else {
            Toast.makeText(context, R.string.gamedir_open_failed, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true);
    }

    private void runInstallerWithConfirmation() {
        if(!ProgressLayout.ensureNoOngoingTasks(requireContext())) return;
        mModInstallerLauncher.launch(null);
    }
}
