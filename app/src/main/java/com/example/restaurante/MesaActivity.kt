package com.example.restaurante

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class MesaActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var mesaId: String? = null

    private lateinit var adapterArroces: PlatoAdapter
    private lateinit var adapterPostres: PlatoAdapter
    private lateinit var adapterPedido: PedidoAdapter

    private lateinit var backButton: ImageButton // Referencia al botón de retroceso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mesa)

        // Obtener ID de la mesa desde el Intent
        mesaId = intent.getStringExtra("mesa_key")

        // Inicializa la referencia a Firebase
        database = FirebaseDatabase.getInstance("https://restaurante-55cb8-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("servicio-actual")

        // Mostrar el ID de la mesa en el TextView
        findViewById<TextView>(R.id.mesaIdTextView).text = mesaId ?: "Mesa desconocida"

        // Configurar botón de retroceso
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Configurar recyclers
        val recyclerArroces = findViewById<RecyclerView>(R.id.recyclerArroces)
        val recyclerPostres = findViewById<RecyclerView>(R.id.recyclerPostres)
        val recyclerPedido =  findViewById<RecyclerView>(R.id.recyclerPedido)

        // Inicializar adaptadores
        adapterArroces = PlatoAdapter(this, arroces) { handlePlatoClick(it) }
        adapterPostres = PlatoAdapter(this, postres) { handlePlatoClick(it) }
        adapterPedido = PedidoAdapter(emptyList())

        recyclerArroces.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerArroces.adapter = adapterArroces

        recyclerPostres.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerPostres.adapter = adapterPostres

        recyclerPedido.layoutManager =  LinearLayoutManager(this@MesaActivity)
        recyclerPedido.adapter = adapterPedido

        // Referenciar botones
        val btnExpandArroces = findViewById<ImageButton>(R.id.btnExpandArroces)
        val btnExpandPostres = findViewById<ImageButton>(R.id.btnExpandPostres)
        val btnExpandPedido = findViewById<ImageButton>(R.id.btnExpandPedido)
        val btnPagar = findViewById<Button>(R.id.btnPagar)

        // Asignar el OnClickListener a los TextView
        val labelArroces = findViewById<TextView>(R.id.labelArroces)
        labelArroces.setOnClickListener {
            toggleSection(recyclerArroces, btnExpandArroces)
        }

        val labelPostres = findViewById<TextView>(R.id.labelPostres)
        labelPostres.setOnClickListener {
            toggleSection(recyclerPostres, btnExpandPostres)
        }

        val labelPedido = findViewById<TextView>(R.id.labelPedido)
        labelPedido.setOnClickListener {
            toggleSection(recyclerPedido, btnExpandPedido)
        }

        // Mostrar el pedido
        mostrarPedido(mesaId!!, database)

        // Pagar cuenta
        btnPagar.setOnClickListener {
            pagarCuenta()
        }
    }

    private fun toggleSection(recyclerView: RecyclerView, button: ImageButton) {
        if (recyclerView.visibility == View.VISIBLE) {
            recyclerView.visibility = View.GONE
            button.setImageResource(android.R.drawable.arrow_up_float)
        } else {
            recyclerView.visibility = View.VISIBLE
            button.setImageResource(android.R.drawable.arrow_down_float)
        }
    }

    private fun handlePlatoClick(plato: Plato) {
        println("Agregando plato: ${plato.nombre}, Precio: ${plato.precio}")

        mesaId?.let { id ->
            val mesaRef = database.child(id)
            val pedido = Pedido(plato.nombre, plato.precio)

            mesaRef.child("pedido").get().addOnSuccessListener { snapshot ->
                val listaPedidos = snapshot.getValue<List<Pedido>>()?.toMutableList() ?: mutableListOf()
                val total = snapshot.child("total").getValue(Double::class.java) ?: 0.0
                listaPedidos.add(pedido)

                mesaRef.child("pedido").setValue(listaPedidos)
                mesaRef.child("total").setValue(total + plato.precio)
                mesaRef.child("disponible").setValue(false)
            }
        }

    }

    private fun mostrarPedido(mesaId: String, database: DatabaseReference) {

        var totalPedido: Double? = 0.0
        val btnPagar = findViewById<Button>(R.id.btnPagar)

        // Mostrar u ocultar sección de pedido
        mesaId.let { id ->
            val mesaRef = database.child(id)
            mesaRef.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    totalPedido = snapshot.child("total").getValue(Double::class.java)
                    if(totalPedido == null || totalPedido == 0.0){
                        findViewById<LinearLayout>(R.id.sectionPedido).visibility = View.GONE
                    }else{
                        findViewById<LinearLayout>(R.id.sectionPedido).visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MesaActivity, "Error al cargar el pedido", Toast.LENGTH_SHORT).show()
                }
            })

        }

        // Referencia al pedido de la mesa
        val pedidoMesaRef = database.child(mesaId).child("pedido")

        pedidoMesaRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // Por si acaso no hay pedido y no se ha ocultado anteriormente
                if (!snapshot.exists()) {
                    findViewById<LinearLayout>(R.id.sectionPedido).visibility = View.GONE
                    return
                }

                val pedidoMap = mutableMapOf<String, Pair<Int, Double>>() // Nombre -> (Cantidad, Precio unitario)

                for (pedidoSnapshot in snapshot.children) {
                    val nombre = pedidoSnapshot.child("nombre").getValue(String::class.java)
                    val precio = pedidoSnapshot.child("precio").getValue(Double::class.java)
                    var total = 0.0 // Total del producto, no del pedido entero

                    if (nombre != null && precio != null) {
                        totalPedido = totalPedido?.plus(precio) // Sumamos el precio al total de la mesa
                        mesaId.let { id ->
                            val mesaRef = database.child(id)
                            mesaRef.child("total").setValue(totalPedido)
                        }

                        // Actualizamos el botón de pagar
                        btnPagar.setText("Pagar cuenta - " + String.format("%.2f", totalPedido))

                        // Si hay más de uno de un producto se suman (ej. 2 Arroz Negro)
                        pedidoMap[nombre] = pedidoMap.getOrDefault(nombre, Pair(0, precio)).let {
                            Pair(it.first + 1, precio)
                        }
                        total += precio
                    }
                }

                val pedidoLista = pedidoMap.toList()
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerPedido)
                recyclerView.adapter = PedidoAdapter(pedidoLista)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MesaActivity, "Error al cargar el pedido", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun pagarCuenta() {

        mesaId?.let { id ->
            val mesaRef = database.child(id)

            mesaRef.child("pedido").removeValue()
            mesaRef.child("total").setValue(0.0)
            mesaRef.child("disponible").setValue(true)

            Toast.makeText(this, "Cuenta pagada", Toast.LENGTH_SHORT).show()
            println("Cuenta pagada de la mesa $id")

            findViewById<LinearLayout>(R.id.sectionPedido).visibility = View.GONE
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    // Lista de Arroces
    private val arroces = listOf(
        Plato("ARR01", "Arroz del senyoret", "arroz_senyoret", "Arroz mediterráneo con mariscos pelados para mayor comodidad.", listOf("Pescado", "Crustaceos"), 12.50),
        Plato("ARR02", "Paella Valenciana", "paella_valenciana", "Auténtica receta de paella con pollo, conejo y verduras frescas.", listOf("Apio"), 12.00),
        Plato("ARR03", "Arroz Negro", "arroz_negro", "Delicioso arroz con tinta de calamar y alioli.", listOf("Crustaceos", "Pescado", "Huevo"), 13.50),
        Plato("ARR04", "Paella de Secreto", "paella_secreto", "Paella de Secreto Ibérico, Alcachofas y Boletus.", listOf("Crustaceos", "Pescado"), 18.00),
        Plato("ARR05", "Arroz con Verduras", "paella_verduras", "Arroz vegetariano con verduras de temporada.", listOf("Apio"), 9.00),
        Plato("ARR06", "Arroz con Setas", "arroz_setas", "Paella con Pollo deshuesado junto a un cóctel de setas variadas.", listOf("Frutos-secos", "Lacteos"), 10.50)
    )

    // Lista de Postres
    private val postres = listOf(
        Plato("POS01", "Muerte por chocolate", "muerte_chocolate", "Tarta 100% sin gluten con 3 capas de bizcocho montadas en crema chocolate y trufa.", listOf("Huevo", "Lacteos"), 5.20),
        Plato("POS02", "Tarta de Oreo", "tarta_oreo", "Elegante tarta sin gluten con galletas y chocolate.", listOf("Huevo"), 5.20),
        Plato("POS03", "Tarta Red Velvet", "red_velvet", "Tarta sin gluten Red Velvet.", listOf("Huevo", "Lacteos"), 5.20),
        Plato("POS04", "Strudel de Manzana", "strudel_manzana", "Strudel crujiente con manzanas y almendras.", listOf("Huevo", "Frutos-secos"), 6.30),
        Plato("POS05", "Tarta de Queso", "tarta_queso", "Tarta de queso artesanal sin gluten.", listOf("Huevo", "Lacteos"), 16.00),
        Plato("POS06", "Tarta de Zanahoria", "tarta_zanahoria", "Carrot cake sin gluten con queso crema.", listOf("Huevo", "Lacteos"), 5.20)
    )

}
