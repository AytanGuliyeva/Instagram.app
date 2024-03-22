import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ConstValues
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth.currentUser!!.uid


    private val _postResult = MutableLiveData<Resource<List<Post>>>()
    val postResult: LiveData<Resource<List<Post>>>
        get() = _postResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading
    private val _followersCount = MutableLiveData<Int>()
    val followersCount: LiveData<Int>
        get() = _followersCount

    private val _followingCount = MutableLiveData<Int>()
    val followingCount: LiveData<Int>
        get() = _followingCount

    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    init {
        fetchFollowersCount()
        fetchFollowingCount()
    }
    fun fetchUserInformation() {
        _userInformation.postValue(Resource.Loading)
        firestore.collection("Users")
            .document(auth)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toUser()
                    if (user != null) {
                        _userInformation.postValue(Resource.Success(user))
                    } else {
                        _userInformation.postValue(Resource.Error(Exception("User data is null")))
                    }
                } else {
                    _userInformation.postValue(Resource.Error(Exception("User document does not exist")))
                }
            }
            .addOnFailureListener { exception ->
                _userInformation.postValue(Resource.Error(exception))
            }
    }
    private fun DocumentSnapshot.toUser(): Users? {
        return try {
            val userId = getString(ConstValues.USER_ID)
            val username = getString(ConstValues.USERNAME)
            val email = getString(ConstValues.EMAIL)
            val password = getString(ConstValues.PASSWORD)
            val bio = getString(ConstValues.BIO)
            val imageUrl = getString(ConstValues.IMAGE_URL)

            Users(
                userId ?: "",
                username ?: "",
                email ?: "",
                password ?: "",
                bio ?: "",
                imageUrl ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
    private fun fetchFollowersCount() {
        firestore.collection("Follow").document(auth)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val follow = documentSnapshot.data
                if (follow != null) {
                    val followers = (follow["followers"] as? HashMap<*, *>)?.size ?: 0
                    _followersCount.postValue(followers)
                } else {
                    _followersCount.postValue(0)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserDetailViewModel", "Error getting followers count: $exception")
            }
    }

    private fun fetchFollowingCount() {
        firestore.collection("Follow").document(auth)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val follow = documentSnapshot.data
                if (follow != null) {
                    val following = (follow["following"] as? HashMap<*, *>)?.size ?: 0
                    _followingCount.postValue(following)
                } else {
                    _followingCount.postValue(0)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserDetailViewModel", "Error getting following count: $exception")
            }
    }



    fun fetchPosts() {
        _loading.postValue(true)
        firestore.collection("Posts").get()
            .addOnSuccessListener { querySnapshot ->
                val postList = mutableListOf<Post>()
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        val formattedTimestamp = dateFormat.format(post.time!!.toDate())
                        val postWithFormattedTime = post.copy(time = Timestamp(Date(formattedTimestamp)))
                        if (Firebase.auth.currentUser?.uid==it.userId) postList.add(postWithFormattedTime)
                    }
                }
                _postResult.postValue(Resource.Success(postList))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed! ${exception.message}", exception)
            }
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}
