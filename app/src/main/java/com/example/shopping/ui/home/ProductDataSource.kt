package com.example.shopping.ui.home

import com.example.shopping.R

object ProductDataSource {

    val allProducts = listOf(
        // Nike
        Product("1","NIKE 球鞋（黑色）","nblack1", listOf(R.drawable.nblack1,
            R.drawable.nblack2),2590,"黑色 NIKE 球鞋，百搭耐穿，版型偏小建議大半號。"),
        Product("2","NIKE 球鞋（棕色）","nbrown1",listOf(R.drawable.nbrown1,
                R.drawable.nbrown2), 2890,"棕色款 NIKE 球鞋，秋冬必備穿搭。"),
        Product("3","NIKE 球鞋（GD款）","ngd1",listOf(R.drawable.ngd1,
            R.drawable.ngd2), 3800,"GD 特別款 NIKE 球鞋，時尚個性。"),
        Product("4","NIKE 球鞋（灰色）","ngray1",listOf(R.drawable.ngray1,
            R.drawable.ngray2),2590,"灰色 NIKE 球鞋，簡約風格。"),
        Product("5","NIKE 球鞋（紅色）","nred1",listOf(R.drawable.nred1,
            R.drawable.nred2),2690,"紅色 NIKE 球鞋，亮眼吸睛。"),
        Product("6","NIKE 球鞋（特別款）","nspecial1",listOf(R.drawable.nspecial1,
            R.drawable.nspecial2),4190,"NIKE 特別版本球鞋，限量珍藏。"),

        // Puma
        Product("7","PUMA 球鞋（黑色）","pblack1",listOf(R.drawable.pblack1,
            R.drawable.pblack2), 2890,"黑色 PUMA 球鞋，耐用舒適。"),
        Product("8","PUMA 球鞋（棕色）","pbrown1", listOf(R.drawable.pbrown1,
            R.drawable.pbrown2),2890,"棕色 PUMA 球鞋，休閒百搭。"),
        Product("9","PUMA 球鞋（藍色）","pblue1",listOf(R.drawable.pblue1,
            R.drawable.pblue2), 2990,"藍色 PUMA 球鞋，清爽活潑。"),
        Product("10","PUMA 球鞋（灰色）","pgray1",listOf(R.drawable.pgray1,
            R.drawable.pgray2),2890,"灰色 PUMA 球鞋，沈穩風格。"),
        Product("11","PUMA 球鞋（紅色）","pred1",listOf(R.drawable.pred1,
            R.drawable.pred2),2990,"紅色 PUMA 球鞋，醒目造型。"),
        Product("12","PUMA 球鞋（粉色）","ppink1",listOf(R.drawable.ppink1,
            R.drawable.ppink2),2990,"粉色 PUMA 球鞋，可愛氣質。"),

        // Adidas
        Product("13","ADIDAS 球鞋（黑色）","black1", listOf(R.drawable.black1,
                R.drawable.black2), 2590,"黑色 ADIDAS 球鞋，百搭選擇。"),
        Product("14","ADIDAS 球鞋（綠色）","green1",listOf(R.drawable.green1,
                R.drawable.green2), 2890,"綠色 ADIDAS 球鞋，亮眼風格。"),
        Product("15","ADIDAS 球鞋（藍色）","blue1",listOf(R.drawable.blue1,
            R.drawable.blue2), 2890,"藍色 ADIDAS 球鞋，清爽造型。"),
        Product("16","ADIDAS 球鞋（灰色）","gray1",listOf(R.drawable.gray1,
            R.drawable.gray2),2590,"灰色 ADIDAS 球鞋，沈穩搭配。"),
        Product("17","ADIDAS 球鞋（紅色）","red1",listOf(R.drawable.red1,
            R.drawable.red2),2790,"紅色 ADIDAS 球鞋，運動感十足。"),
        Product("18","ADIDAS 球鞋（粉色）","pink1",listOf(R.drawable.pink1,
            R.drawable.pink2),2890,"粉色 ADIDAS 球鞋，可愛造型。"),
    )
    fun findById(id: String): Product? {
        return allProducts.find { it.id == id }
    }
}