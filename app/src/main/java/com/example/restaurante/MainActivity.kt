package com.example.restaurante

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private val coroutineScope = CoroutineScope(Dispatchers.Main) // Corrutina para mejorar el el problema del tiempo de carga

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa la referencia a la base de datos de Firebase
        database = FirebaseDatabase.getInstance("https://restaurante-55cb8-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("servicio-actual")
        println(database.toString())

        // Cargar mesas
        checkTableAvailability()
    }

    // Cargar los botones de las mesas y comprobar su disponibilidad
    private fun checkTableAvailability() {
        val container = findViewById<LinearLayout>(R.id.containerButtons)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        progressBar.visibility = View.VISIBLE

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()

                for (data in snapshot.children) {
                    val key = data.key
                    val disponible = data.child("disponible").getValue(Boolean::class.java) ?: false
                    createButton(container, key, disponible)
                }

                progressBar.visibility = View.GONE

            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al leer los datos: ${error.message}")
            }
        })
    }

    // Crear botones (para cada mesa)
    private fun createButton(container: LinearLayout, key: String?, disponible: Boolean) {
        if (key == null) return

        val button = Button(this).apply {
            // Atributos del botón
            text = key
            setBackgroundColor(if (disponible) android.graphics.Color.GREEN else android.graphics.Color.RED)

            setOnClickListener {
                val intent = Intent(this@MainActivity, MesaActivity::class.java)
                intent.putExtra("mesa_key", key)
                startActivity(intent)
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 16) }
        }

        // Añadir botón al contenedor
        container.addView(button)
    }

    // Cancelar coroutines para evitar fugas de memoria
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

}
