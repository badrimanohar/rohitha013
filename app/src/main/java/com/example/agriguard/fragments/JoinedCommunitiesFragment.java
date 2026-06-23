package com.example.agriguard.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.agriguard.activities.CommunityChatActivity;
import com.example.agriguard.adapters.JoinedCommunityAdapter;
import com.example.agriguard.databinding.FragmentJoinedCommunitiesBinding;
import com.example.agriguard.viewmodels.CommunityViewModel;

public class JoinedCommunitiesFragment extends Fragment {

    private FragmentJoinedCommunitiesBinding binding;
    private CommunityViewModel viewModel;
    private JoinedCommunityAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentJoinedCommunitiesBinding.inflate(inflater, container, false);
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
        adapter = new JoinedCommunityAdapter(new java.util.ArrayList<>(), community -> {
            Intent intent = new Intent(getContext(), CommunityChatActivity.class);
            intent.putExtra("communityId", community.getId());
            intent.putExtra("communityName", community.getName());
            startActivity(intent);
        });
        binding.rvJoined.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvJoined.setAdapter(adapter);

        binding.btnExplore.setOnClickListener(v -> {
            // Communicate with parent to switch tab
            if (getParentFragment() instanceof CommunityFragment) {
                ((CommunityFragment) getParentFragment()).switchToExploreTab();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getJoinedCommunities().observe(getViewLifecycleOwner(), communities -> {
            adapter.updateData(communities);
            binding.emptyState.setVisibility(communities.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
