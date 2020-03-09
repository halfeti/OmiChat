package com.quang.appchat.model

class User(
    val uid: String,
    var userName: String,
    val email: String,
    val password: String,
    val image: String,
    val check: String,
    val birthday: String,
    val gender: String,
    val phoneNumber: String,
    val status: String,
    val arrRoom: ArrayList<String>
) {
    var isSelected: Boolean = false
    constructor() : this("", "", "", "", "", "", "", "", "","", ArrayList())

    override fun toString(): String {
        return "User(uid='$uid', userName='$userName', email='$email', password='$password', image='$image', check='$check', birthday='$birthday', gender='$gender', phoneNumber='$phoneNumber', arrRoom=$arrRoom)"
    }
}