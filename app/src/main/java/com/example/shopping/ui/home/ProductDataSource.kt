package com.example.shopping.ui.home

import com.example.shopping.R

object ProductDataSource {

    val allProducts = listOf(
        // Nike
        Product("1","Nike 球鞋","nblack1", listOf(R.drawable.nblack1,
            R.drawable.nblack2),2500,"頂級材質、舒適耐穿"),
        Product("2","Nike 休閒鞋","nbrown1",listOf(R.drawable.nbrown1,
                R.drawable.nbrown2), 2200,"適合日常穿搭，百搭耐用"),
        Product("3","Nike 透氣鞋款","ngd1",listOf(R.drawable.ngd1,
            R.drawable.ngd2), 1900,"透氣網布，長時間行走不悶熱"),
        Product("4","Nike 鞋子","ngray1",listOf(R.drawable.ngray1,
            R.drawable.ngray2),2000,"輕量設計，運動首選"),
        Product("5","Nike 鞋子","nred1",listOf(R.drawable.nred1,
            R.drawable.nred2),2000,"新款上市，潮流必備"),
        Product("6","Nike 鞋子","nspecial1",listOf(R.drawable.nspecial1,
            R.drawable.nspecial2),2000,"限量特別款，值得收藏"),

        // Puma
        Product("7","puma 球鞋","pblack1",listOf(R.drawable.pblack1,
            R.drawable.pblack2), 2500,"限量特別款，值得收藏"),
        Product("8","puma 休閒鞋","pbrown1", listOf(R.drawable.pbrown1,
            R.drawable.pbrown2),2200,"限量特別款，值得收藏"),
        Product("9","puma 透氣鞋款","pblue1",listOf(R.drawable.pblue1,
            R.drawable.pblue2), 1900,"限量特別款，值得收藏"),
        Product("10","puma 鞋子","pgray1",listOf(R.drawable.pgray1,
            R.drawable.pgray2),2000,"限量特別款，值得收藏"),
        Product("11","puma 鞋子","pred1",listOf(R.drawable.pred1,
            R.drawable.pred2),2000,"限量特別款，值得收藏"),
        Product("12","puma 鞋子","ppink1",listOf(R.drawable.ppink1,
            R.drawable.ppink2),2000,"限量特別款，值得收藏"),

        // Adidas
        Product("13","Adidas 球鞋","black1", listOf(R.drawable.black1,
                R.drawable.black2), 2500,"限量特別款，值得收藏"),
        Product("14","Adidas 休閒鞋","green1",listOf(R.drawable.green1,
                R.drawable.green2), 2200,"限量特別款，值得收藏"),
        Product("15","Adidas 透氣鞋款","blue1",listOf(R.drawable.blue1,
            R.drawable.blue2), 1900,"限量特別款，值得收藏"),
        Product("16","Adidas 鞋子","gray1",listOf(R.drawable.gray1,
            R.drawable.gray2),2000,"限量特別款，值得收藏"),
        Product("17","Adidas 鞋子","red1",listOf(R.drawable.red1,
            R.drawable.red2),2000,"限量特別款，值得收藏"),
        Product("18","Adidas 鞋子","pink1",listOf(R.drawable.pink1,
            R.drawable.pink2),2000,"限量特別款，值得收藏"),
    )
}