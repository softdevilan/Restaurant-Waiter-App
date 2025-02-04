package com.example.restaurante

data class Plato(
    val id: String,
    val nombre: String,
    val foto: String, // Nombre de la imagen (sin extensión, solo el nombre)
    val descripcion: String,
    val alergenos: List<String>,
    val precio: Double
)

