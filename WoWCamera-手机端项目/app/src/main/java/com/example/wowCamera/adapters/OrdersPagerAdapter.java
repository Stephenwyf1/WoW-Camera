package com.example.wowCamera.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.wowCamera.FolderFragment;
import com.example.wowCamera.photoFragment;

import java.util.List;

public class OrdersPagerAdapter extends FragmentStateAdapter {
    //public class OrdersPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList;
    //    public OrdersPagerAdapter(@NonNull FragmentManager fragmentManager, List<Fragment>fragmentList) {
//        super(fragmentManager);
//        this.fragmentList = fragmentList;
//    }
    public OrdersPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
//        this.fragmentList = fragmentList;
    }
    public Fragment getItem(int position){
        return fragmentList.get(position);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new photoFragment();
            case 1:
                return new FolderFragment();

        }
        return new photoFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }

//    @Override
//    public int getCount() {
//        return fragmentList.size();
//    }
}
