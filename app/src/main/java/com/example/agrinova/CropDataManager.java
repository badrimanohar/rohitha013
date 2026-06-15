package com.example.agrinova;

import java.util.ArrayList;
import java.util.List;

public class CropDataManager {

    public static List<Crop> getAllCrops() {
        List<Crop> crops = new ArrayList<>();

        // 1. RICE
        Crop rice = new Crop();
        rice.name = "Rice";
        rice.scientificName = "Oryza sativa";
        rice.type = "Kharif";
        rice.category = "Grains";
        rice.description = "The staple food for more than half of the world's population, grown primarily in standing water.";
        rice.season = "June to December";
        rice.duration = "120 - 150 days";
        rice.soilType = "Clayey or Loamy soil (Deep fertile soils)";
        rice.soilPh = "5.5 - 6.5";
        rice.drainage = "Needs poor drainage (standing water)";
        rice.soilPreparation = "Plowing, Puddling (wet tillage), and Leveling.";
        rice.idealTemp = "20°C - 35°C";
        rice.humidity = "High (Above 70%)";
        rice.rainfall = "150cm - 300cm";
        rice.seedVarieties = "Basmati, Jasmine, IR64, Swarna, MTU 1010";
        rice.seedQuantity = "20-25 kg per acre (Transplanted)";
        rice.seedTreatment = "Salt water treatment and Carbendazim soaking";
        rice.sowingMonths = "June - July";
        rice.sowingProcedure = "Nursery bed preparation followed by transplanting 25-day old seedlings.";
        rice.organicFertilizer = "FYM (Farm Yard Manure), Green Manure (Dhaincha)";
        rice.chemicalFertilizer = "Urea, DAP, MOP";
        rice.npkRatio = "100:50:50 per hectare";
        rice.waterRequirement = "Very High (standing water 2-5cm)";
        rice.irrigationMethods = "Flood irrigation";
        rice.commonDiseases = "Blast, Bacterial Leaf Blight, Sheath Blight";
        rice.commonPests = "Stem Borer, Leaf Folder, Brown Plant Hopper";
        rice.yieldPerAcre = "20 - 25 Quintals";
        rice.marketPrice = "₹2,200 - ₹3,500 per Quintal";
        rice.profitEstimation = "Estimated ₹30,000 - ₹50,000 per acre";
        crops.add(rice);

        // 2. WHEAT
        Crop wheat = new Crop();
        wheat.name = "Wheat";
        wheat.scientificName = "Triticum aestivum";
        wheat.type = "Rabi";
        wheat.category = "Grains";
        wheat.description = "A globally important cereal grain, wheat is a key source of carbohydrates.";
        wheat.season = "October to April";
        wheat.duration = "110 - 140 days";
        wheat.soilType = "Well-drained Fertile Loamy soil";
        wheat.soilPh = "6.0 - 7.5";
        wheat.drainage = "Excellent drainage required; sensitive to waterlogging";
        wheat.idealTemp = "10°C - 25°C (Cool during growth, warm during ripening)";
        wheat.rainfall = "50cm - 100cm";
        wheat.seedVarieties = "HD-2967, PBW-343, Shriram 303, DBW-187";
        wheat.seedQuantity = "40-50 kg per acre";
        wheat.seedTreatment = "Carbendazim or Thiram treatment";
        wheat.sowingMonths = "November";
        wheat.sowingProcedure = "Drilling method using seed drills at 2-3cm depth.";
        wheat.organicFertilizer = "Compost, Poultry manure";
        wheat.chemicalFertilizer = "Urea, SSP, Potash";
        wheat.npkRatio = "120:60:40";
        wheat.waterRequirement = "Moderate (4-6 critical irrigation stages)";
        wheat.criticalStages = "Crown Root Initiation (CRI), Flowering, Milk stage";
        wheat.yieldPerAcre = "18 - 22 Quintals";
        wheat.marketPrice = "₹2,100 - ₹2,800 per Quintal";
        crops.add(wheat);

        // 3. TOMATO
        Crop tomato = new Crop();
        tomato.name = "Tomato";
        tomato.scientificName = "Solanum lycopersicum";
        tomato.type = "Annual / Perennial";
        tomato.category = "Vegetables";
        tomato.description = "A short-duration high-value vegetable crop widely used in various cuisines.";
        tomato.season = "Spring, Autumn, and Winter";
        tomato.duration = "90 - 120 days";
        tomato.soilType = "Well-drained Sandy Loam or Clay Loam";
        tomato.soilPh = "6.0 - 7.0";
        tomato.idealTemp = "18°C - 28°C";
        tomato.seedVarieties = "Arka Rakshak, Abhinav, Pusa Ruby, Vaishali";
        tomato.seedQuantity = "60-80 grams per acre (Hybrid)";
        tomato.sowingMonths = "June-July or Oct-Nov";
        tomato.organicFertilizer = "Vermicompost, Neem cake";
        tomato.waterRequirement = "Regular consistent moisture";
        tomato.irrigationMethods = "Drip Irrigation (Best)";
        tomato.commonDiseases = "Early Blight, Late Blight, Tomato Leaf Curl Virus";
        tomato.commonPests = "Fruit Borer, Whitefly, Leaf Miner";
        tomato.yieldPerAcre = "150 - 250 Quintals (Hybrid)";
        tomato.marketPrice = "Variable (₹10 - ₹60 per kg)";
        crops.add(tomato);

        // 4. COTTON
        Crop cotton = new Crop();
        cotton.name = "Cotton";
        cotton.scientificName = "Gossypium spp.";
        cotton.type = "Kharif";
        cotton.category = "Cash Crops";
        cotton.description = "The most important natural fiber, also known as 'White Gold'.";
        cotton.season = "May to October";
        cotton.duration = "150 - 180 days";
        cotton.soilType = "Deep Black soil (Regur) or Alluvial soil";
        cotton.soilPh = "6.0 - 8.0";
        cotton.idealTemp = "21°C - 30°C";
        cotton.rainfall = "50cm - 110cm";
        cotton.seedVarieties = "Bt Cotton (Bollgard II), MCU-5, Suvin";
        cotton.seedQuantity = "2 - 3 kg per acre (Hybrid)";
        cotton.sowingMonths = "May - June";
        cotton.sowingProcedure = "Dibbling at 90x60 cm or 60x60 cm spacing.";
        cotton.organicFertilizer = "Cotton seed cake, Well-rotten FYM";
        cotton.commonPests = "Bollworms (Pink, Spotted), Aphids, Whitefly";
        cotton.yieldPerAcre = "8 - 12 Quintals (Seed cotton)";
        cotton.marketPrice = "₹6,000 - ₹9,000 per Quintal";
        crops.add(cotton);

        // 5. MAIZE
        Crop maize = new Crop();
        maize.name = "Maize";
        maize.scientificName = "Zea mays";
        maize.type = "Kharif/Rabi";
        maize.category = "Grains";
        maize.description = "Versatile crop used for food, livestock feed, and industrial raw materials.";
        maize.season = "June-July or Oct-Nov";
        maize.duration = "90 - 110 days";
        maize.soilType = "Fertile Red Loam or Alluvial soil";
        maize.soilPh = "5.8 - 7.0";
        maize.idealTemp = "18°C - 30°C";
        maize.seedVarieties = "Deccan 103, Ganga 5, Pusa Early Hybrid";
        maize.seedQuantity = "8 - 10 kg per acre";
        maize.sowingDepth = "3 - 5 cm";
        maize.commonPests = "Fall Armyworm, Stem Borer";
        maize.yieldPerAcre = "20 - 30 Quintals";
        maize.marketPrice = "₹1,800 - ₹2,500 per Quintal";
        crops.add(maize);

        // 6. SUGARCANE
        Crop sugarcane = new Crop();
        sugarcane.name = "Sugarcane";
        sugarcane.scientificName = "Saccharum officinarum";
        sugarcane.type = "Perennial";
        sugarcane.category = "Cash Crops";
        sugarcane.description = "The main source of sugar and jaggery, requiring a long growing season.";
        sugarcane.season = "January to March or October";
        sugarcane.duration = "10 - 12 months (or up to 18)";
        sugarcane.soilType = "Deep rich loamy soil with good organic matter";
        sugarcane.soilPh = "6.5 - 7.5";
        sugarcane.idealTemp = "20°C - 32°C";
        sugarcane.waterRequirement = "Very High (Needs frequent irrigation)";
        sugarcane.seedVarieties = "Co 86032, Co 0238, CoV 09356";
        sugarcane.sowingProcedure = "Sett planting in furrows using 2 or 3 budded setts.";
        sugarcane.yieldPerAcre = "350 - 500 Quintals";
        sugarcane.marketPrice = "₹3,000 - ₹4,500 per Tonne";
        crops.add(sugarcane);

        // 7. CHICKPEA (PULSES)
        Crop chickpea = new Crop();
        chickpea.name = "Chickpea";
        chickpea.scientificName = "Cicer arietinum";
        chickpea.type = "Rabi";
        chickpea.category = "Pulses";
        chickpea.description = "An important pulse crop rich in protein, fixing nitrogen in the soil.";
        chickpea.season = "October to November";
        chickpea.duration = "100 - 120 days";
        chickpea.soilType = "Well-drained Sandy Loam to Clay Loam";
        chickpea.seedVarieties = "Pusa 256, Vijay, Digvijay, JG-11";
        chickpea.seedQuantity = "30-40 kg per acre";
        chickpea.organicFertilizer = "Rhizobium culture, Phosphate Solubilizing Bacteria";
        chickpea.yieldPerAcre = "8 - 12 Quintals";
        crops.add(chickpea);

        // 8. MANGO (FRUITS)
        Crop mango = new Crop();
        mango.name = "Mango";
        mango.scientificName = "Mangifera indica";
        mango.type = "Perennial Fruit";
        mango.category = "Fruits";
        mango.description = "The 'King of Fruits', mango is a major commercial fruit crop grown in tropical climates.";
        mango.season = "July to September (Planting)";
        mango.duration = "4-5 years for first bearing";
        mango.soilType = "Deep, well-drained alluvial or laterite soil";
        mango.idealTemp = "24°C - 30°C";
        mango.seedVarieties = "Alphonso, Kesar, Banganapalli, Totapuri, Dasheri";
        mango.irrigationMethods = "Basin irrigation or Drip irrigation";
        mango.yieldPerAcre = "8 - 10 Tonnes (Mature orchard)";
        mango.marketPrice = "₹30,000 - ₹80,000 per Tonne";
        crops.add(mango);

        // 9. BANANA (FRUITS)
        Crop banana = new Crop();
        banana.name = "Banana";
        banana.scientificName = "Musa sapientum";
        banana.type = "Tropical Perennial";
        banana.category = "Fruits";
        banana.description = "One of the most important fruit crops, grown for its high nutritional value and year-round availability.";
        banana.duration = "12 - 15 months";
        banana.soilType = "Rich well-drained loamy soil with high organic matter";
        banana.soilPh = "6.5 - 7.5";
        banana.waterRequirement = "Very High (regular watering)";
        banana.seedVarieties = "Grand Naine, Robusta, Dwarf Cavendish, Poovan";
        banana.yieldPerAcre = "20 - 40 Tonnes";
        crops.add(banana);

        // 10. SOYBEAN (PULSES/OILSEEDS)
        Crop soybean = new Crop();
        soybean.name = "Soybean";
        soybean.scientificName = "Glycine max";
        soybean.type = "Kharif";
        soybean.category = "Pulses";
        soybean.description = "A major source of protein and oil, used for food and industrial purposes.";
        soybean.season = "June to July (Sowing)";
        soybean.duration = "90 - 110 days";
        soybean.soilType = "Well-drained fertile loamy soil";
        soybean.seedVarieties = "JS 335, JS 93-05, NRC 37";
        soybean.yieldPerAcre = "8 - 10 Quintals";
        crops.add(soybean);

        // 11. POTATO
        Crop potato = new Crop();
        potato.name = "Potato";
        potato.scientificName = "Solanum tuberosum";
        potato.category = "Vegetables";
        potato.description = "World's fourth most important food crop after rice, wheat and maize.";
        potato.season = "October to November (Main crop)";
        potato.duration = "90 - 120 days";
        potato.soilType = "Well-drained sandy loam soil rich in organic matter";
        potato.idealTemp = "15°C - 25°C";
        potato.seedVarieties = "Kufri Jyoti, Kufri Bahar, Kufri Pukhraj";
        potato.yieldPerAcre = "100 - 150 Quintals";
        crops.add(potato);

        // 12. ONION
        Crop onion = new Crop();
        onion.name = "Onion";
        onion.scientificName = "Allium cepa";
        onion.category = "Vegetables";
        onion.description = "Essential commercial vegetable used globally for culinary and medicinal purposes.";
        onion.season = "Kharif, Late Kharif, and Rabi";
        onion.soilType = "Deep friable loamy soil";
        onion.seedVarieties = "Agrifound Dark Red, N-53, Bhima Super";
        onion.yieldPerAcre = "80 - 120 Quintals";
        crops.add(onion);

        // 13. GROUNDNUT
        Crop groundnut = new Crop();
        groundnut.name = "Groundnut";
        groundnut.scientificName = "Arachis hypogaea";
        groundnut.category = "Cash Crops";
        groundnut.description = "Also known as Peanut, it is a major oilseed and food crop.";
        groundnut.season = "June to July (Kharif)";
        groundnut.soilType = "Sandy loam or Loamy soil with good drainage";
        groundnut.seedVarieties = "GG-20, Kadiri-6, TAG-24";
        groundnut.yieldPerAcre = "8 - 12 Quintals";
        crops.add(groundnut);

        // 14. MUSTARD
        Crop mustard = new Crop();
        mustard.name = "Mustard";
        mustard.scientificName = "Brassica juncea";
        mustard.category = "Cash Crops";
        mustard.description = "Important Rabi oilseed crop, widely grown in North India.";
        mustard.season = "October to November";
        mustard.soilType = "Loamy or Sandy loam soil";
        mustard.idealTemp = "10°C - 25°C";
        mustard.seedVarieties = "Pusa Bold, Varuna, RH-30";
        mustard.yieldPerAcre = "6 - 10 Quintals";
        crops.add(mustard);

        // 15. POMEGRANATE
        Crop pomegranate = new Crop();
        pomegranate.name = "Pomegranate";
        pomegranate.scientificName = "Punica granatum";
        pomegranate.category = "Fruits";
        pomegranate.description = "Drought-tolerant high-value fruit crop, known for its health benefits.";
        pomegranate.soilType = "Well-drained sandy loam to deep loamy soil";
        pomegranate.idealTemp = "25°C - 35°C";
        pomegranate.seedVarieties = "Bhagwa, Bhagwa Plus, Mridula, Phule Arakta";
        pomegranate.yieldPerAcre = "4 - 5 Tonnes (Mature orchard)";
        crops.add(pomegranate);

        // 16. TURMERIC
        Crop turmeric = new Crop();
        turmeric.name = "Turmeric";
        turmeric.scientificName = "Curcuma longa";
        turmeric.category = "Cash Crops";
        turmeric.description = "Important spice crop known for its medicinal and coloring properties.";
        turmeric.duration = "7 - 9 months";
        turmeric.soilType = "Well-drained sandy or clayey loam";
        turmeric.seedVarieties = "Pragati, Prabha, Kedaram, IISR Alleppey Supreme";
        turmeric.yieldPerAcre = "10 - 15 Tonnes (Fresh rhizomes)";
        crops.add(turmeric);

        // 17. GINGER
        Crop ginger = new Crop();
        ginger.name = "Ginger";
        ginger.scientificName = "Zingiber officinale";
        ginger.category = "Cash Crops";
        ginger.description = "Valuable spice crop used fresh or in dried form (Sonth).";
        ginger.season = "April to May (Planting)";
        ginger.duration = "8 - 9 months";
        ginger.seedVarieties = "Varada, Mahima, Rejatha, Rio-de-Janeiro";
        ginger.yieldPerAcre = "6 - 10 Tonnes (Fresh)";
        crops.add(ginger);

        // 18. GARLIC
        Crop garlic = new Crop();
        garlic.name = "Garlic";
        garlic.scientificName = "Allium sativum";
        garlic.category = "Vegetables";
        garlic.description = "Important commercial bulbous spice and vegetable crop.";
        garlic.season = "September to October (Rabi)";
        garlic.soilType = "Rich loamy soil with good drainage";
        garlic.seedVarieties = "G-282, Yamuna Safed, Agrifound White";
        garlic.yieldPerAcre = "40 - 60 Quintals";
        crops.add(garlic);

        // 19. BAJRA
        Crop bajra = new Crop();
        bajra.name = "Bajra (Pearl Millet)";
        bajra.scientificName = "Pennisetum glaucum";
        bajra.category = "Grains";
        bajra.description = "Nutri-cereal highly resilient to drought and heat.";
        bajra.season = "June to July (Kharif)";
        bajra.soilType = "Light sandy or shallow black soil";
        bajra.idealTemp = "25°C - 35°C";
        bajra.seedVarieties = "Pusa 23, ICTP 8203, HHB 67";
        bajra.yieldPerAcre = "12 - 15 Quintals";
        crops.add(bajra);

        // 20. JOWAR
        Crop jowar = new Crop();
        jowar.name = "Jowar (Sorghum)";
        jowar.scientificName = "Sorghum bicolor";
        jowar.category = "Grains";
        jowar.description = "Versatile millet used for food, fodder and biofuel.";
        jowar.season = "Kharif and Rabi";
        jowar.soilType = "Deep fertile loamy or black soil";
        jowar.seedVarieties = "CSH-9, CSH-16, Maldandi (M35-1)";
        jowar.yieldPerAcre = "10 - 12 Quintals";
        crops.add(jowar);

        return crops;
    }
}
