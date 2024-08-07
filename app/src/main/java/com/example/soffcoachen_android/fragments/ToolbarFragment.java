package com.example.soffcoachen_android.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.soffcoachen_android.R;

public class ToolbarFragment extends Fragment {

    private Button cancelButton;
    private Button loginButton;
    private Button logoutButton;
    private Button newPostButton;
    private Button currentUserButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_toolbar, container, false);
    }
}