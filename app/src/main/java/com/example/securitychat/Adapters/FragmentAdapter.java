package com.example.securitychat.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.example.securitychat.Fragments.ChatsFragment;
import com.example.securitychat.Fragments.StatusFragment;

public class FragmentAdapter extends FragmentPagerAdapter {

    public FragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }
    @NonNull

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new StatusFragment();
//            case 2:
//                return new CallFragment();
            default:
                return new ChatsFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    //@org.jetbrains.annotations.Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0) {
            title = "CHATS";
        }
        if (position == 1) {
            title = "STATUS";
        }
        if (position == 2) {
            title = "GROUP";
        }
        return title;
    }
}
