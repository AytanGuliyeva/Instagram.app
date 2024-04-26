//package com.example.instagramapp.di
//
//import com.google.firebase.Firebase
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.auth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.firestore
//import com.google.firebase.storage.FirebaseStorage
//import com.google.firebase.storage.storage
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object FireBaseModule {
//
//    @Provides
//    @Singleton
//    fun provideFirebase(): Firebase {
//        return Firebase
//    }
//
//    @Provides
//    @Singleton
//    fun provideFirebaseAuth(firebase: Firebase): FirebaseAuth {
//        return firebase.auth
//    }
//
//    @Provides
//    @Singleton
//    fun provideFirebaseStore(firebase: Firebase): FirebaseFirestore {
//        return firebase.firestore
//    }
//    @Provides
//    @Singleton
//    fun provideFireBaseStorage(firebase: Firebase): FirebaseStorage {
//        return firebase.storage
//    }
//
//}