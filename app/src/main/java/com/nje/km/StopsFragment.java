package com.nje.km;

import androidx.fragment.app.Fragment;

public class StopsFragment extends Fragment {

    public StopsFragment() {
        super(R.layout.fragment_stops);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setToolbarTitle("Megállók", null);
    }
}
