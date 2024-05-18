import com.google.firebase.Timestamp

data class Post(
    val caption: String = "",
    val postId: String = "",
    val userId: String = "",
    val time: Timestamp? = null,
    val postImageUrl: String = "",
    var isLiked:Boolean=false
)
