package org.techdown.anstagram.navigation.model

class ContentDTO(var explain:String?=null,
                 var imageUrl:String?=null,//이미지 주소
                 var uid:String?=null, // 어느 유저가 올렸는지
                 var userId:String?=null,
                 var timeStamp:Long?=null,
                 var favoriteCount:Int=0, //좋아요 개수
                 var favorites:MutableMap<String,Boolean> = HashMap()) //중복좋아요 방지할수있게


{
                    data class Comment(var uid:String?=null,
                                       var userId: String?=null,
                                       var comment:String?=null,
                                       var timeStamp: Long?=null) //덧글관리할 수 있는 클래스
}


