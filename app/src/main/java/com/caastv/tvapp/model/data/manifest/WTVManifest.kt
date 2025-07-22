package com.caastv.tvapp.model.data.manifest

import com.caastv.tvapp.model.data.genre.WTVGenre
import com.caastv.tvapp.model.data.language.WTVLanguage
import com.google.gson.annotations.SerializedName

data class WTVManifest(
    var appName:String?="WaveTVApp",
    var logo:String?="https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png",
    var splashUrl:String?="https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png",
    var baseUrl:String? = "https://api-demo.caastv.com/api/",
    var styleNavigation:StyleNavigation?=null,
    var tab:List<TabInfo>?= null,
    var contact:Contact?=null,
    var landingChannel:LandingChannel?=null,
    var language:List<WTVLanguage>?=null,
    var genre:List<WTVGenre>?=null
){
    fun shouldBannerVisible(tabName:String): Boolean {
        return tab?.find { it.name == tabName }?.components?.getOrNull(0)?.name.equals("Banner",true)
    }
}

data class Contact(
    @SerializedName("__v")
    val version: Int?=0,
    val _id: String?=null,
    val address: String?=null,
    val email: String?=null,
    val phone: String?=null,
    val website: String?=null
)

data class LandingChannel(
    val _id: String?=null,
    val title: String?=null,
    val channelId: String?=null,
    val videoUrl: String?=null,
    val createdAt: String?=null,
    val updatedAt: String?=null,
    @SerializedName("__v")
    val version: Int?=0,
)

data class StyleNavigation(var fontName:String?="")

data class TabInfo(
    @SerializedName("__v")
    val version: Int,
    val _id: String,
    val components: List<Component>,
    val displayName: String,
    val iconUrl: String,
    val isVisible: Boolean,
    val name: String,
    val sequence: Int,
    var categories:List<EPGCategory>?=null
)

data class Component(
    val _id: String,
    val isVisible: Boolean,
    val name: String
)
data class EPGCategory(var _id:String?="",
                       var name:String?="",
                       var published:Boolean=false,
                       @SerializedName("__v")
                       val version: Int,
                       var iconUrl:Any?=null)


fun provideTabInfo():List<TabInfo>{
    return arrayListOf(
        TabInfo(
            version= 0,
            _id= "67cadf16fa749384dacab726",
            name= "Home",
            displayName= "Home",
            isVisible= true,
            components = arrayListOf(Component(name= "banner",
                isVisible= true,
                _id= "67cf19176cc5bd8edec5bc40")),
            iconUrl= "https://nextwave.waveiontechnologies.com:5000/uploads/tab-icon/home-line.svg",
            sequence= 1),
        TabInfo(
            version= 0,
            _id= "67cadf55fa749384dacab792",
            name= "Search",
            displayName= "Search",
            isVisible= true,
            components = arrayListOf(),
            iconUrl= "https://nextwave.waveiontechnologies.com:5000/uploads/tab-icon/Search.svg",
            sequence= 2),
        TabInfo(
            version= 0,
            _id= "67cadf16fa749384dacab726",
            name= "Home",
            displayName= "Home",
            isVisible= true,
            components = arrayListOf(Component(name= "banner",
                isVisible= true,
                _id= "67cf19176cc5bd8edec5bc40")),
            iconUrl= "https://nextwave.waveiontechnologies.com:5000/uploads/tab-icon/home-line.svg",
            sequence= 3,
            categories= arrayListOf()
        ),
        TabInfo(
            version= 0,
            _id= "67cae840fa749384dacac430",
            name= "Live Tv",
            displayName= "Live Tv",
            isVisible= true,
            components = arrayListOf(Component(name= "banner",
                isVisible= true,
                _id= "67cee90e6cc5bd8edec54072")),
            iconUrl= "https://nextwave.waveiontechnologies.com:5000/uploads/tab-icon/tv-03.svg",
            sequence= 3,
            categories=  arrayListOf()),
        TabInfo(
            version= 0,
            _id= "67cadedafa749384dacab6c6",
            name= "Profile",
            displayName= "User",
            isVisible= true,
            components = arrayListOf(),
            iconUrl= "https://nextwave.waveiontechnologies.com:5000/uploads/tab-icon/Profile.svg",
            sequence= 4),
        TabInfo(
            version= 0,
            _id= "67cae034fa749384dacab946",
            name= "Setting",
            displayName= "My Watchlist",
            isVisible= true,
            components = arrayListOf(),
            iconUrl= "https://nextwave.waveiontechnologies.com:5000/uploads/tab-icon/Heart.svg",
            sequence= 5),
        TabInfo(
            version= 0,
            _id= "67d1503f861bf70aa06ec00e",
            name= "Movies",
            displayName= "Movies",
            isVisible= true,
            components = arrayListOf(),
            iconUrl= "https://nextwave.waveiontechnologies.com:5000/uploads/tab-icon/clapperboard.svg",
            sequence= 6)
    )

    /*
fun provideEPGCategory():List<EPGCategory>{
    return arrayListOf<EPGCategory>(
        EPGCategory("all", "ALL"), //R.drawable.all,
        EPGCategory("recent", "Recently Watched"),//R.drawable.recent
        EPGCategory("news", "News"),//R.drawable.news
        EPGCategory("face", "Entertainment"),//R.drawable.face
        EPGCategory("music", "Music"),//R.drawable.music
        EPGCategory("kid", "Kids"),//R.drawable.kid
        EPGCategory("spirit", "Spiritual"),//R.drawable.spirit
        EPGCategory("movie", "Movies"),//R.drawable.movie
        EPGCategory("star", "Lifestyle")//R.drawable.star
    )
}*/

}