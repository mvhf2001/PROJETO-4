package com.example.partner.model

data class Activity(
    var id: String = "",
    var nameActivity: String = "",
    var turma: String = "",
    var infoActivity: Info = Info()
)
