package com.example.agriguard.utils

import com.example.agriguard.R
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit Tests for CropIconHelper utility class.
 */
class CropIconHelperTest {

    @Test
    fun testKnownCropIconsReturnGalleryIcon() {
        val knownCrops = listOf(
            "Rice paddy guild",
            "Wheat cultivators",
            "Corn growers association",
            "Tomato farming group",
            "Cotton belt farmers",
            "Banana plantation society",
            "Organic Coffee cultivators"
        )

        for (crop in knownCrops) {
            val iconRes = CropIconHelper.getCropIcon(crop)
            assertEquals("Failed for crop: $crop", android.R.drawable.ic_menu_gallery, iconRes)
        }
    }

    @Test
    fun testUnknownCropReturnsDefaultLauncherIcon() {
        val unknownCommunity = "General Agricultural Machinery Group"
        val iconRes = CropIconHelper.getCropIcon(unknownCommunity)
        assertEquals(R.drawable.ic_launcher_foreground, iconRes)
    }

    @Test
    fun testCaseInsensitiveCropMatching() {
        val iconResLower = CropIconHelper.getCropIcon("rice farming")
        val iconResUpper = CropIconHelper.getCropIcon("RICE FARMING")
        val iconResMixed = CropIconHelper.getCropIcon("RiCe FaRmInG")

        assertEquals(android.R.drawable.ic_menu_gallery, iconResLower)
        assertEquals(iconResLower, iconResUpper)
        assertEquals(iconResLower, iconResMixed)
    }
}
