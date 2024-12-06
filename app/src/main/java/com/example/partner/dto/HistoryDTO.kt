package com.example.partner.dto

import com.example.partner.model.Activity
import com.example.partner.model.Info

data class HistoryDTO(
    var id: String = "",
    var nameActivity: String = "",
    var date: String = "",
    var info: Info = Info(),
    var aluno: String = "",
    var turma: String = ""
)