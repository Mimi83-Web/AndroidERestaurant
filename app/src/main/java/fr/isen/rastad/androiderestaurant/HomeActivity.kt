package fr.isen.rastad.androiderestaurant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import fr.isen.rastad.androiderestaurant.ui.theme.AndroidERestaurantTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.gson.Gson
import org.json.JSONObject
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidERestaurantTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting(this)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("HomeActivity", "Activity Destroyed")
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Greeting(activity: ComponentActivity) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "DroidRestaurant") },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Bienvenue chez",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "DroidRestaurant",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(id = R.drawable.chef),
                contentDescription = "Chef"
            )
            Spacer(modifier = Modifier.height(32.dp))
            MenuSection(title = "Entrées") {
                navigateToCategory(activity, "Entrées")
            }
            MenuSection(title = "Plats") {
                navigateToCategory(activity, "Plats")
            }
            MenuSection(title = "Desserts") {
                navigateToCategory(activity, "Desserts")
            }
        }
    }
}


private fun navigateToCategory(activity: ComponentActivity, category: String) {
    val intent = Intent(activity, CategoryActivity::class.java).apply {
        putExtra("category_name", category)
    }
    activity.startActivity(intent)
}

@Composable
fun MenuSection(title: String, onClick: () -> Unit) {
    Column(modifier = Modifier
        .clickable(onClick = onClick)
        .fillMaxWidth()
        .padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// Data classes to represent the JSON response structure
data class MenuResponse(val data: List<Category>)
data class Category(val name_fr: String, val items: List<Dish>)
data class Dish(val name_fr: String, val images: List<String>, val prices: List<Price>, val ingredients: List<Ingredient>)
data class Price(val price: String)
data class Ingredient(
    val id: String,
    val id_shop: String,
    val name_fr: String,
    val name_en: String,
    val create_date: String,
    val update_date: String,
    val id_pizza: String
)

object SimpleCache {
    private val categoryCache = mutableMapOf<String, List<Dish>>()

    fun getCategory(categoryName: String): List<Dish>? = categoryCache[categoryName]

    fun setCategory(categoryName: String, dishes: List<Dish>) {
        categoryCache[categoryName] = dishes
    }

    fun invalidateCategory(categoryName: String) {
        categoryCache.remove(categoryName)
    }

    fun invalidateAll() {
        categoryCache.clear()
    }
}

class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categoryName = intent.getStringExtra("category_name") ?: "Category"
        fetchMenu(categoryName, forceRefresh = false)
    }

    private fun fetchMenu(categoryName: String, forceRefresh: Boolean) {
        if (forceRefresh) SimpleCache.invalidateCategory(categoryName)
        SimpleCache.getCategory(categoryName)?.let { cachedDishes ->
            updateUI(categoryName, cachedDishes)
            return
        }
        val queue = Volley.newRequestQueue(this)
        val url = "http://test.api.catering.bluecodegames.com/menu"

        val postData = JSONObject().apply {
            put("id_shop", "1")
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, postData,
            { response ->
                val menuResponse = Gson().fromJson(response.toString(), MenuResponse::class.java)
                val dishes = menuResponse.data.firstOrNull { it.name_fr == categoryName }?.items ?: listOf()
                SimpleCache.setCategory(categoryName, dishes)
                updateUI(categoryName, dishes)
            },
            { error ->
                Log.e("CategoryActivity", "Error fetching menu", error)
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun updateUI(categoryName: String, dishes: List<Dish>) {
        setContent {
            AndroidERestaurantTheme {
                CategoryScreen(categoryName, dishes, onBack = { finish() }, onRefresh = { fetchMenu(categoryName, true) })
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(categoryName: String, dishes: List<Dish>, onBack: () -> Unit,  onRefresh: () -> Unit) {
    val context = LocalContext.current
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = categoryName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        SwipeRefresh(state = swipeRefreshState, onRefresh = onRefresh) {
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(dishes) { dish ->
                    DishItem(dish = dish) {
                        // Gestionnaire de clic pour chaque plat
                        val dishJson = Gson().toJson(dish)
                        val intent = Intent(context, DishDetailActivity::class.java).apply {
                            putExtra("dish_details", dishJson)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}
class DishDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dishJson = intent.getStringExtra("dish_details")
        val dish = Gson().fromJson(dishJson, Dish::class.java)

        setContent {
            AndroidERestaurantTheme {
                DishDetailScreen(dish = dish, onBack = { finish() })
            }
        }
    }
}







@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class, ExperimentalLayoutApi::class)
fun DishDetailScreen(dish: Dish, onBack: () -> Unit) {
    val pagerState = rememberPagerState()
    var quantity by remember { mutableStateOf(1) } // Correction ici
    val pricePerDish = dish.prices.first().price.toFloat() // Supposons que prices.first().price contienne le prix unitaire sous forme de chaîne
    val totalPrice = pricePerDish * quantity // Calcule le prix total


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = dish.name_fr) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            if (dish.images.isNotEmpty()) {
                HorizontalPager(
                    count = dish.images.size,
                    state = pagerState,
                    modifier = Modifier.height(200.dp).fillMaxWidth()
                ) { page ->
                    Image(
                        painter = rememberImagePainter(
                            data = dish.images[page],
                            builder = {
                                crossfade(true)
                                error(R.drawable.plat) // Image d'erreur si le chargement échoue
                            }
                        ),
                        contentDescription = "Dish Image",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Affiche une image par défaut si aucune image n'est présente
                Image(
                    painter = painterResource(id = R.drawable.plat),
                    contentDescription = "Default Image",
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${dish.name_fr}", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                //espace entre les textes
                maxItemsInEachRow = 4,
            ) {
                dish.ingredients.forEach { ingredient ->
                    Chip(label = ingredient.name_fr)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Quantité : ", style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (quantity > 1) quantity -= 1 }) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Moins")
                }
                Text("$quantity", style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { quantity += 1 }) {
                    Icon(Icons.Filled.KeyboardArrowUp, "Plus")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* Ajoutez votre action ici */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter au panier - Total : ${"%.2f".format(totalPrice)}€")
            }

        }
    }
}

@Composable
fun Chip(label: String) {
    Surface(
        modifier = Modifier.padding(4.dp),
        color = MaterialTheme.colorScheme.secondary,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun DishItem(dish: Dish, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = dish.name_fr, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            val painter = rememberImagePainter(
                data = dish.images.firstOrNull(), // Utilisez firstOrNull() pour éviter une exception si la liste est vide
                builder = {
                    crossfade(true)
                    fallback(R.drawable.plat)
                    //tester les autres images de la liste si la première est nulle
                  dish.images.drop(1).forEach {
                        if (it.isNotEmpty()) {
                            data(it)
                            return@forEach
                        }
                    }
                    error(R.drawable.plat)
                }
            )
                Image(
                    painter = painter,
                    contentDescription = "Dish Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Prix: ${dish.prices.first().price}€", style = MaterialTheme.typography.bodyMedium)

        }
    }
}
