package com.fusion.fusionchat.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fusion.fusionchat.fragments.ChatFragment;
import com.fusion.fusionchat.fragments.CommunityFragment;
import com.fusion.fusionchat.fragments.StatusFragment;

public class FragmentsAdapter extends FragmentStateAdapter {
    public FragmentsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ChatFragment();
                case 1:
                return new StatusFragment();
            default:
                return new CommunityFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
