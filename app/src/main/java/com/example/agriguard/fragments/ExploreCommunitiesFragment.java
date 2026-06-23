package com.example.agriguard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.agriguard.adapters.ExploreCommunityAdapter;
import com.example.agriguard.databinding.FragmentExploreCommunitiesBinding;
import com.example.agriguard.viewmodels.CommunityViewModel;

public class ExploreCommunitiesFragment extends Fragment {

    private FragmentExploreCommunitiesBinding binding;
    private CommunityViewModel viewModel;
    private ExploreCommunityAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreCommunitiesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(CommunityViewModel.class);

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ExploreCommunityAdapter(new java.util.ArrayList<>(), community -> {
            viewModel.joinCommunity(community);
            Toast.makeText(getContext(), "Joining " + community.getName() + "...", Toast.LENGTH_SHORT).show();
        });
        binding.rvExplore.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvExplore.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getExploreCommunities().observe(getViewLifecycleOwner(), communities -> {
            adapter.updateData(communities);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
