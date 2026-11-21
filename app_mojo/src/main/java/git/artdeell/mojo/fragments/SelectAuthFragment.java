package git.artdeell.mojo.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.ProgressLayout;

import androidx.appcompat.app.AlertDialog;

import dev.pklauncher.android.OwnershipGate;
import git.artdeell.mojo.MojoApplication;
import git.artdeell.mojo.R;
import git.artdeell.mojo.Tools;
import git.artdeell.mojo.progresskeeper.ProgressKeeper;

public class SelectAuthFragment extends Fragment {
    public static final String TAG = "AUTH_SELECT_FRAGMENT";

    public SelectAuthFragment(){
        super(R.layout.fragment_select_auth_method);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button mMicrosoftButton = view.findViewById(R.id.button_microsoft_authentication);
        Button mLocalButton = view.findViewById(R.id.button_local_authentication);
        Button mElyByButton = view.findViewById(R.id.button_elyby_authentication);

        mMicrosoftButton.setOnClickListener(v -> launchAuthFragment(MicrosoftLoginFragment.class, MicrosoftLoginFragment.TAG));
        // PK: Ely.by (third-party, no Minecraft ownership) is removed as a sign-in option.
        mElyByButton.setVisibility(View.GONE);
        mElyByButton.setOnClickListener(null);
        // PK: offline nicknames are only for players with a verified Microsoft owner account on this device.
        mLocalButton.setText(R.string.pk_auth_offline_owner_only);
        mLocalButton.setOnClickListener(v -> MojoApplication.sExecutorService.execute(() -> {
            boolean owner = OwnershipGate.hasVerifiedOwner();
            Tools.runOnUiThread(() -> {
                if (!isAdded()) return;
                if (owner) {
                    launchAuthFragment(LocalLoginFragment.class, LocalLoginFragment.TAG);
                } else {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.pk_gate_title)
                            .setMessage(R.string.pk_gate_offline_needs_owner)
                            .setPositiveButton(R.string.pk_gate_sign_in, (d, w) -> launchAuthFragment(MicrosoftLoginFragment.class, MicrosoftLoginFragment.TAG))
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
            });
        }));
    }

    private void launchAuthFragment(Class<? extends  Fragment> fragmentClass, String fragmentTag) {
        if(!ProgressLayout.ensureNoOngoingTasks(requireContext(), ProgressLayout.AUTHENTICATE)) return;
        Tools.swapFragment(requireActivity(), fragmentClass, fragmentTag, null);
    }
}
