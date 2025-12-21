package com.example.shopping.ui.home

import com.example.shopping.R

object ProductDataSource {

    val allProducts = listOf(
        // Nike
        Product("1","Nike 球鞋","nblack1", listOf(R.drawable.nblack1,
            R.drawable.nblack2),2500,"黑色 NIKE 球鞋，百搭耐穿，版型偏小建議大半號。"),
        Product("2","Nike 休閒鞋","nbrown1",listOf(R.drawable.nbrown1,
                R.drawable.nbrown2), 2200,"棕色款 NIKE 球鞋，秋冬必備穿搭。"),
        Product("3","Nike 透氣鞋款","ngd1",listOf(R.drawable.ngd1,
            R.drawable.ngd2), 1900,"GD 特別款 NIKE 球鞋，時尚個性。"),
        Product("4","Nike 鞋子","ngray1",listOf(R.drawable.ngray1,
            R.drawable.ngray2),2000,"灰色 NIKE 球鞋，簡約風格。"),
        Product("5","Nike 鞋子","nred1",listOf(R.drawable.nred1,
            R.drawable.nred2),2000,"紅色 NIKE 球鞋，亮眼吸睛。"),
        Product("6","Nike 鞋子","nspecial1",listOf(R.drawable.nspecial1,
            R.drawable.nspecial2),2000,"NIKE 特別版本球鞋，限量珍藏。"),

        // Puma
        Product("7","puma 球鞋","pblack1",listOf(R.drawable.pblack1,
            R.drawable.pblack2), 2500,"黑色 PUMA 球鞋，耐用舒適。"),
        Product("8","puma 休閒鞋","pbrown1", listOf(R.drawable.pbrown1,
            R.drawable.pbrown2),2200,"棕色 PUMA 球鞋，休閒百搭。"),
        Product("9","puma 透氣鞋款","pblue1",listOf(R.drawable.pblue1,
            R.drawable.pblue2), 1900,"藍色 PUMA 球鞋，清爽活潑。"),
        Product("10","puma 鞋子","pgray1",listOf(R.drawable.pgray1,
            R.drawable.pgray2),2000,"灰色 PUMA 球鞋，沈穩風格。"),
        Product("11","puma 鞋子","pred1",listOf(R.drawable.pred1,
            R.drawable.pred2),2000,"紅色 PUMA 球鞋，醒目造型。"),
        Product("12","puma 鞋子","ppink1",listOf(R.drawable.ppink1,
            R.drawable.ppink2),2000,"粉色 PUMA 球鞋，可愛氣質。"),

        // Adidas
        Product("13","Adidas 球鞋","black1", listOf(R.drawable.black1,
                R.drawable.black2), 2500,"黑色 ADIDAS 球鞋，百搭選擇。"),
        Product("14","Adidas 休閒鞋","green1",listOf(R.drawable.green1,
                R.drawable.green2), 2200,"綠色 ADIDAS 球鞋，亮眼風格。"),
        Product("15","Adidas 透氣鞋款","blue1",listOf(R.drawable.blue1,
            R.drawable.blue2), 1900,"藍色 ADIDAS 球鞋，清爽造型。"),
        Product("16","Adidas 鞋子","gray1",listOf(R.drawable.gray1,
            R.drawable.gray2),2000,"灰色 ADIDAS 球鞋，沈穩搭配。"),
        Product("17","Adidas 鞋子","red1",listOf(R.drawable.red1,
            R.drawable.red2),2000,"紅色 ADIDAS 球鞋，運動感十足。"),
        Product("18","Adidas 鞋子","pink1",listOf(R.drawable.pink1,
            R.drawable.pink2),2000,"粉色 ADIDAS 球鞋，可愛造型。"),
    )
}