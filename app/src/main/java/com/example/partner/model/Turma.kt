package com.example.partner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Turma(
    var id: String?,
    var nameTurma: String?,
    var nameProfessor: String? = null,
    var serie: String?,
    var alunos: List<User>
): Parcelable, Serializable