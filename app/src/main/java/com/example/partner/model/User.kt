package com.example.partner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class User(
    var matricula: String? = "",
    var id: String? = "",
    var name: String? = "",
    var email: String? = "",
    var turma: String? = "",
    var turno: String? = "",
    var data_criacao: String? = "",
    var data_nasc: String? = "",
    var phone: String? = null
) : Serializable, Parcelable
