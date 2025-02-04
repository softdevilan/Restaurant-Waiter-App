package com.example.restaurante

data class Pedido(
    var nombre: String = "",  // Valores por defecto
    var precio: Double = 0.0
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this("", 0.0)
}
