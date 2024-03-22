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
import java.lang.NullPointerException

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
        _loading.postValue(true)
        firestore.collection("Users")
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { value ->
                val userList = ArrayList<Users>()
                for (user in value.documents) {
                    val userId = user.get(ConstValues.USER_ID) as String
                    val username = user.get(ConstValues.USERNAME) as String
                    val email = user.get(ConstValues.EMAIL) as String
                    val password = user.get(ConstValues.PASSWORD) as String
                    val bio = user.get(ConstValues.BIO) as String
                    val imageUrl = user.get(ConstValues.IMAGE_URL) as String
                    val user = Users(userId, username, email, password, bio, imageUrl)
                    if (Firebase.auth.currentUser?.uid != userId) userList.add(user)

                }
                _userResult.postValue(Resource.Success(userList))
            }
            .addOnFailureListener { exception ->
                _userResult.postValue(Resource.Error(exception))
            }
            .addOnCompleteListener {
                _loading.postValue(false)
            }
    }


    fun fetchOtherUsersPosts(userId: String) {
        firestore.collection("Posts")
            .whereNotEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { value ->
                if (value != null) {
                    val postList = ArrayList<Post>()
                    val hashSet = hashSetOf<Post>()
                    _postResult.postValue(Resource.Loading)
                    for (post in value.documents) {
                        val caption = post.get(ConstValues.CAPTION) as String
                        val postId = post.get(ConstValues.POST_ID) as String
                        val userId = post.get(ConstValues.USER_ID) as String
                        val time = post.get(ConstValues.TIME) as Timestamp
                        val imageUrl = post.get(ConstValues.POST_IMAGE_URL ) as String
                        val post = Post(postId, caption, userId, time, imageUrl)
                        hashSet.add(post)
                    }
                    postList.addAll(hashSet)
                    _postResult.postValue(Resource.Success(postList))
                }
            }
            .addOnFailureListener { exception ->
                _postResult.postValue(Resource.Error(exception))
            }
    }

}
