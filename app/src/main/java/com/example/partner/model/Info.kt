package com.example.partner.model

data class Info(
    var nameActivity: String = "",
    var dateActivity: String = "",
    var descriptionInfo: String = "",
    var resultActivity: Double? // Adicione todos os atributos que a classe possui com valores padrão
) {
    // Construtor padrão
    constructor() : this("", "", "", null)
}