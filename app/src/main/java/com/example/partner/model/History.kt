package com.example.partner.model
data class History(
    var id: String = "",
    var nameActivity: String = "",
    var dateActivity: String? = "",
    var infoActivity: Info = Info()
)
