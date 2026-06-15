package com.example.agrinova;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agrinova.databinding.ActivityCropDetailBinding;

public class CropDetailActivity extends AppCompatActivity {

    private ActivityCropDetailBinding binding;
    private Crop crop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCropDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        crop = (Crop) getIntent().getSerializableExtra("selected_crop");
        if (crop == null) {
            finish();
            return;
        }

        setupUI();
    }

    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_share) {
                shareCropInfo();
                return true;
            }
            return false;
        });

        binding.tvCropTitle.setText(crop.name);
        binding.tvScientificName.setText(crop.scientificName);
        binding.tvDescription.setText(crop.description);

        com.bumptech.glide.Glide.with(this)
                .load(R.drawable.ic_logo)
                .into(binding.ivCropDetail);

        // A. Basic Information
        StringBuilder basicInfo = new StringBuilder();
        if (crop.type != null) basicInfo.append("• Type: ").append(crop.type).append("\n\n");
        if (crop.season != null) basicInfo.append("• Season: ").append(crop.season).append("\n\n");
        if (crop.duration != null) basicInfo.append("• Duration: ").append(crop.duration);
        addSection("A. BASIC INFORMATION", basicInfo.toString(), true);

        // B. Soil Information
        StringBuilder soilInfo = new StringBuilder();
        if (crop.soilType != null) soilInfo.append("• Best Soil: ").append(crop.soilType).append("\n\n");
        if (crop.soilPh != null) soilInfo.append("• pH Value: ").append(crop.soilPh).append("\n\n");
        if (crop.drainage != null) soilInfo.append("• Drainage: ").append(crop.drainage).append("\n\n");
        if (crop.soilPreparation != null) soilInfo.append("• Preparation: ").append(crop.soilPreparation);
        if (soilInfo.length() > 0) addSection("B. SOIL REQUIREMENTS", soilInfo.toString());

        // C. Climate Information
        StringBuilder climateInfo = new StringBuilder();
        if (crop.idealTemp != null) climateInfo.append("• Ideal Temp: ").append(crop.idealTemp).append("\n\n");
        if (crop.humidity != null) climateInfo.append("• Humidity: ").append(crop.humidity).append("\n\n");
        if (crop.rainfall != null) climateInfo.append("• Rainfall: ").append(crop.rainfall).append("\n\n");
        if (crop.suitableClimate != null) climateInfo.append("• Climate: ").append(crop.suitableClimate);
        if (climateInfo.length() > 0) addSection("C. CLIMATE INFORMATION", climateInfo.toString());

        // D. Seed Information
        StringBuilder seedInfo = new StringBuilder();
        if (crop.seedVarieties != null) seedInfo.append("• Varieties: ").append(crop.seedVarieties).append("\n\n");
        if (crop.seedQuantity != null) seedInfo.append("• Qty per Acre: ").append(crop.seedQuantity).append("\n\n");
        if (crop.seedTreatment != null) seedInfo.append("• Treatment: ").append(crop.seedTreatment).append("\n\n");
        if (crop.sowingDepth != null) seedInfo.append("• Sowing Depth: ").append(crop.sowingDepth);
        if (seedInfo.length() > 0) addSection("D. SEED INFORMATION", seedInfo.toString());

        // E. Sowing Schedule
        StringBuilder sowingInfo = new StringBuilder();
        if (crop.sowingMonths != null) sowingInfo.append("• Best Months: ").append(crop.sowingMonths).append("\n\n");
        if (crop.sowingProcedure != null) sowingInfo.append("• Procedure: ").append(crop.sowingProcedure).append("\n\n");
        if (crop.germinationTime != null) sowingInfo.append("• Germination: ").append(crop.germinationTime);
        if (sowingInfo.length() > 0) addSection("E. SOWING SCHEDULE", sowingInfo.toString());

        // F. Fertilizer Information
        StringBuilder fertInfo = new StringBuilder();
        if (crop.organicFertilizer != null) fertInfo.append("• Organic: ").append(crop.organicFertilizer).append("\n\n");
        if (crop.chemicalFertilizer != null) fertInfo.append("• Chemical: ").append(crop.chemicalFertilizer).append("\n\n");
        if (crop.npkRatio != null) fertInfo.append("• NPK Ratio: ").append(crop.npkRatio).append("\n\n");
        if (crop.fertilizerQuantity != null) fertInfo.append("• Quantity/Acre: ").append(crop.fertilizerQuantity);
        if (fertInfo.length() > 0) addSection("F. FERTILIZER & NUTRITION", fertInfo.toString());

        // G. Irrigation Information
        StringBuilder irrInfo = new StringBuilder();
        if (crop.waterRequirement != null) irrInfo.append("• Water Requirement: ").append(crop.waterRequirement).append("\n\n");
        if (crop.irrigationMethods != null) irrInfo.append("• Methods: ").append(crop.irrigationMethods).append("\n\n");
        if (crop.criticalStages != null) irrInfo.append("• Critical Stages: ").append(crop.criticalStages);
        if (irrInfo.length() > 0) addSection("G. IRRIGATION MANAGEMENT", irrInfo.toString());

        // H & I. Disease & Pest Management
        StringBuilder pestInfo = new StringBuilder();
        if (crop.commonDiseases != null) pestInfo.append("• Common Diseases: ").append(crop.commonDiseases).append("\n\n");
        if (crop.commonPests != null) pestInfo.append("• Common Pests: ").append(crop.commonPests).append("\n\n");
        if (crop.diseasePrevention != null) pestInfo.append("• Prevention: ").append(crop.diseasePrevention);
        if (pestInfo.length() > 0) addSection("H & I. DISEASE & PEST CONTROL", pestInfo.toString());

        // J. Harvest Information
        StringBuilder harvestInfo = new StringBuilder();
        if (crop.yieldPerAcre != null) harvestInfo.append("• Yield per Acre: ").append(crop.yieldPerAcre).append("\n\n");
        if (crop.maturitySigns != null) harvestInfo.append("• Signs of Maturity: ").append(crop.maturitySigns).append("\n\n");
        if (crop.harvestingMethods != null) harvestInfo.append("• Methods: ").append(crop.harvestingMethods);
        if (harvestInfo.length() > 0) addSection("J. HARVEST INFORMATION", harvestInfo.toString());

        // K & L. Market & Profit Analysis
        StringBuilder profitInfo = new StringBuilder();
        if (crop.marketPrice != null) profitInfo.append("• Market Price: ").append(crop.marketPrice).append("\n\n");
        if (crop.profitEstimation != null) profitInfo.append("• Profit Estimate: ").append(crop.profitEstimation).append("\n\n");
        if (crop.riskFactors != null) profitInfo.append("• Risk Factors: ").append(crop.riskFactors);
        if (profitInfo.length() > 0) addSection("K & L. PROFIT & MARKET", profitInfo.toString());

        binding.fabBookmark.setOnClickListener(v -> {
            Toast.makeText(this, crop.name + " bookmarked!", Toast.LENGTH_SHORT).show();
        });
    }

    private void shareCropInfo() {
        String shareText = "AgriNova Crop Encyclopedia\n\n" +
                "Crop: " + crop.name + "\n" +
                "Scientific Name: " + crop.scientificName + "\n" +
                "Season: " + crop.season + "\n\n" +
                "Download AgriNova for full details!";
        
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
        startActivity(android.content.Intent.createChooser(intent, "Share Crop Info"));
    }

    private void addSection(String title, String content, boolean initiallyExpanded) {
        View sectionView = LayoutInflater.from(this).inflate(R.layout.layout_crop_detail_section, binding.containerSections, false);
        TextView tvTitle = sectionView.findViewById(R.id.tv_section_title);
        TextView tvContent = sectionView.findViewById(R.id.tv_section_content);
        View layoutHeader = sectionView.findViewById(R.id.layout_header);
        View layoutContent = sectionView.findViewById(R.id.layout_content);
        android.widget.ImageView ivArrow = sectionView.findViewById(R.id.iv_arrow);
        
        tvTitle.setText(title);
        tvContent.setText(content);

        if (initiallyExpanded) {
            layoutContent.setVisibility(View.VISIBLE);
            ivArrow.setRotation(180);
        }
        
        layoutHeader.setOnClickListener(v -> {
            if (layoutContent.getVisibility() == View.VISIBLE) {
                layoutContent.setVisibility(View.GONE);
                ivArrow.setRotation(0);
            } else {
                layoutContent.setVisibility(View.VISIBLE);
                ivArrow.setRotation(180);
            }
        });
        
        binding.containerSections.addView(sectionView);
    }

    private void addSection(String title, String content) {
        addSection(title, content, false);
    }
}
