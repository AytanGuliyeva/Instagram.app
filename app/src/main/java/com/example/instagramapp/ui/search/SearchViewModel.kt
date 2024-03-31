import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ConstValues
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SearchViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _userResult = MutableLiveData<Resource<List<Users>>>()
    val userResult: LiveData<Resource<List<Users>>>
        get() = _userResult

    private val _postResult = MutableLiveData<Resource<List<Post>>>()
    val postResult: LiveData<Resource<List<Post>>>
        get() = _postResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    fun searchUsers(query: String) {
        _loading.value = true
        firestore.collection("Users")
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { value ->
                val userList = mutableListOf<Users>()
                for (user in value.documents) {
                    val userId = user.getString(ConstValues.USER_ID) ?: ""
                    val username = user.getString(ConstValues.USERNAME) ?: ""
                    val email = user.getString(ConstValues.EMAIL) ?: ""
                    val password = user.getString(ConstValues.PASSWORD) ?: ""
                    val bio = user.getString(ConstValues.BIO) ?: ""
                    val imageUrl = user.getString(ConstValues.IMAGE_URL) ?: ""
                    val currentUserUid = Firebase.auth.currentUser?.uid
                    if (currentUserUid != userId) {
                        val user = Users(userId, username, email, password, bio, imageUrl)
                        userList.add(user)
                    }
                }
                _userResult.value = Resource.Success(userList)
            }
            .addOnFailureListener { exception ->
                _userResult.value = Resource.Error(exception)
            }
            .addOnCompleteListener {
                _loading.value = false
            }
    }

    fun fetchOtherUsersPosts(userId: String) {
        _loading.value = true
        firestore.collection("Posts")
            .whereNotEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { value ->
                val postList = mutableListOf<Post>()
                val hashSet = hashSetOf<Post>()
                for (post in value.documents) {
                    val caption = post.getString(ConstValues.CAPTION) ?: ""
                    val postId = post.getString(ConstValues.POST_ID) ?: ""
                    val userId = post.getString(ConstValues.USER_ID) ?: ""
                    val time = post.getTimestamp(ConstValues.TIME)
                    val imageUrl = post.getString(ConstValues.POST_IMAGE_URL) ?: ""
                    val post = Post(postId, caption, userId, time, imageUrl)
                    hashSet.add(post)
                }
                postList.addAll(hashSet)
                _postResult.value = Resource.Success(postList)
            }
            .addOnFailureListener { exception ->
                _postResult.value = Resource.Error(exception)
            }
            .addOnCompleteListener {
                _loading.value = false
            }
    }
}
