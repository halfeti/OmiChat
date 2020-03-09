package com.ominext.appchat.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class Room(
    val arrAdmin: ArrayList<String>,
    val nameRoom: String,
    var idRoom: String,
    val uidMember: ArrayList<String>,
    val image: String,
    val time: Long,
    val lastContent: HashMap<String,String>,
    val arrUserRead: ArrayList<String>
 ) {

    constructor() : this(java.util.ArrayList(),"", "", java.util.ArrayList(), "", System.currentTimeMillis()/1000,
        HashMap(),ArrayList()
    )

    override fun toString(): String {
        return "Room(arrAdmin=$arrAdmin, nameRoom='$nameRoom', idRoom='$idRoom', uidMember=$uidMember, image='$image', time=$time, lastContent=$lastContent)"
    }


}
