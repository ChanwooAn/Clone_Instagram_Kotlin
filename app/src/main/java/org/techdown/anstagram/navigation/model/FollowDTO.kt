package org.techdown.anstagram.navigation.model

data class FollowDTO (
    var followerCount:Int=0,
    var follwers:MutableMap<String,Boolean> =HashMap(),

    var follwingCount:Int=0,
    var followings:MutableMap<String,Boolean> =HashMap()



//map으로 중복 방지









        )