package fr.isen.rastad.androiderestaurant

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import fr.isen.rastad.androiderestaurant.ui.theme.AndroidERestaurantTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.util.UUID
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.glide.GlideImage

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
fun CartQuantityObserver(context: Context, onQuantityChange: (Int) -> Unit) {
    val sharedPreferences = context.getSharedPreferences("PREFERENCES", MODE_PRIVATE)
    LaunchedEffect(key1 = sharedPreferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "cart_quantity") {
                onQuantityChange(sharedPreferences.getInt("cart_quantity", 0))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MyTopAppBar(activity: ComponentActivity) {
    var cartQuantity by remember { mutableStateOf(getCartQuantity(activity)) }

    CartQuantityObserver(context = activity) { quantity ->
        cartQuantity = quantity
    }

    TopAppBar(
        title = { Text("DroidRestaurant") },
        actions = {
            Text(text = cartQuantity.toString(), modifier = Modifier.padding(end = 8.dp))
            IconButton(onClick = {
                activity.startActivity(Intent(activity, CartActivity::class.java))
            }) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
            }
        },
        navigationIcon = {
            IconButton(onClick = { activity.onBackPressed() }) {
                Icon(Icons.Filled.ArrowBack, "Back")
            }
        }
    )
}

fun getCartQuantity(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences("PREFERENCES", MODE_PRIVATE)
    return sharedPreferences.getInt("cart_quantity", 0)
}



@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Greeting(activity: ComponentActivity) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DroidRestaurant") },
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
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dishJson = intent.getStringExtra("dish_details")
        val dish = Gson().fromJson(dishJson, Dish::class.java)

        setContent {
            AndroidERestaurantTheme {
                DishDetailScreen(dish = dish, onBack = { finish() }, activity = this, cartQuantity = mutableIntStateOf(getCartQuantity(this)))
            }
        }
    }

}




suspend fun saveDishToCartFile(dish: Dish, quantityToAdd: Int, activity: ComponentActivity) {
    val filename = "cart.json"
    val file = File(activity.filesDir, filename)
    val itemsArray = if (file.exists()) JSONArray(file.readText()) else JSONArray()
    var dishFound = false
    val gson = Gson()

    for (i in 0 until itemsArray.length()) {
        val item = itemsArray.getJSONObject(i)
        val existingDish = gson.fromJson(item.getString("dish"), Dish::class.java)
        if (existingDish.name_fr == dish.name_fr) {
            val newQuantity = item.getInt("quantity") + quantityToAdd
            item.put("quantity", newQuantity)
            dishFound = true
            break
        }
    }

    if (!dishFound) {
        val dishJson = gson.toJson(dish)
        val cartItem = JSONObject().apply {
            put("uuid", UUID.randomUUID().toString())
            put("dish", dishJson)
            put("quantity", quantityToAdd)
        }
        itemsArray.put(cartItem)
    }

    file.writeText(itemsArray.toString())

    updateCartQuantity(activity, quantityToAdd)
}

fun updateCartQuantity(activity: ComponentActivity, quantityToAdd: Int) {
    val sharedPreferences = activity.getSharedPreferences("PREFERENCES", MODE_PRIVATE)
    val currentQuantity = sharedPreferences.getInt("cart_quantity", 0) + quantityToAdd
    sharedPreferences.edit().putInt("cart_quantity", currentQuantity).apply()
}

suspend fun readCartFile(activity: ComponentActivity): List<Triple<String, Dish, Int>> {
    val filename = "cart.json"
    val file = File(activity.filesDir, filename)
    val cartItems = mutableListOf<Triple<String, Dish, Int>>()
    val gson = Gson()

    if (file.exists()) {
        val jsonArray = JSONArray(file.readText())
        for (i in 0 until jsonArray.length()) {
            try {
                val jsonObject = jsonArray.getJSONObject(i)
                val uuid = jsonObject.getString("uuid")
                val dishJson = jsonObject.getString("dish")
                val dish = gson.fromJson(dishJson, Dish::class.java)
                val quantity = jsonObject.getInt("quantity")
                cartItems.add(Triple(uuid, dish, quantity))
            } catch (e: Exception) {
                Log.e("readCartFile", "Erreur lors de la lecture d'un élément du tableau JSON", e)
            }
        }
    }

    return cartItems
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class, ExperimentalLayoutApi::class)
fun DishDetailScreen(dish: Dish, onBack: () -> Unit,  activity: ComponentActivity, cartQuantity: MutableState<Int>) {
    val pagerState = rememberPagerState()
    var quantity by remember { mutableStateOf(1) }
    val pricePerDish = dish.prices.first().price.toFloat()
    val totalPrice = pricePerDish * quantity
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            MyTopAppBar(activity)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }

    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)) {
            if (dish.images.isNotEmpty()) {
                HorizontalPager(
                    count = dish.images.size,
                    state = pagerState,
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                ) { page ->
                    Image(
                        painter = rememberImagePainter(
                            data = dish.images[page],
                            builder = {
                                crossfade(true)
                                error(R.drawable.plat)
                            }
                        ),
                        contentDescription = "Dish Image",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
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
                onClick = {
                    coroutineScope.launch {
                        saveDishToCartFile(dish, quantity, activity)
                        cartQuantity.value += quantity
                        val snackbarResult = snackbarHostState.showSnackbar(
                            message = "Plat ajouté au panier",
                            actionLabel = "Voir le panier",
                            duration = SnackbarDuration.Indefinite
                        )
                        if (snackbarResult == SnackbarResult.ActionPerformed) {
                            // L'utilisateur a choisi de voir le panier, naviguer vers CartScreen
                            activity.startActivity(Intent(activity, CartActivity::class.java))
                        }
                    }
                },
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
                data = dish.images.firstOrNull(),
                builder = {
                    crossfade(true)
                    fallback(R.drawable.plat)
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


@SuppressLint("UnrememberedMutableState")
class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidERestaurantTheme {
                val cartItems = remember { mutableStateOf(listOf<Triple<String, Dish, Int>>()) }
                val cartQuantity = remember { mutableStateOf(getCartQuantity(this)) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(true) {
                    coroutineScope.launch {
                        cartItems.value = readCartFile(this@CartActivity)
                    }
                }

                CartScreen(
                    cartItems = cartItems.value,
                    onRemoveItem = { uuidToRemove ->
                        coroutineScope.launch {
                            removeFromCartFile(uuidToRemove.toString(), this@CartActivity)
                            cartItems.value = readCartFile(this@CartActivity)
                            cartQuantity.value = getCartQuantity(this@CartActivity)
                        }
                    },
                    onPlaceOrder = {
                        Log.d("CartActivity", "Order placed.")
                        coroutineScope.launch {
                            clearCartFile(this@CartActivity)
                            val sharedPreferences =
                                getSharedPreferences("PREFERENCES", MODE_PRIVATE)
                            sharedPreferences.edit().putInt("cart_quantity", 0).apply()
                            cartQuantity.value = 0
                            showOrderPlacedSnackbarAndReturnHome()
                        }
                    },
                    onBack = { finish() },
                    cartQuantity = cartQuantity
                )
            }
        }
    }

    private fun ComponentActivity.showOrderPlacedSnackbarAndReturnHome() {
        val context = this
        runOnUiThread {
            setContent {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Snackbar {
                        Text("Commande passée avec succès")
                    }

                    val gifUrl = "https://c.tenor.com/xCxpdjzdSyMAAAAC/tenor.gif"
                    ShowGifFromInternet(gifUrl)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Vous allez être redirigé à la page d'accueil dans quelques secondes",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
            Handler(Looper.getMainLooper()).postDelayed({
                val homeIntent = Intent(context, HomeActivity::class.java)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(homeIntent)
            }, 3000)
        }


    @Composable
    fun ShowGifFromInternet(gifUrl: String) {
        GlideImage(
            imageModel = gifUrl,
            modifier = Modifier
                .fillMaxWidth()
                .size(400.dp),
            contentScale = ContentScale.Crop,
            requestOptions = {
                RequestOptions()
                    .error(R.drawable.plat)
            }
        )
    }


    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun CartScreen(
        cartItems: List<Triple<String, Dish, Int>>,
        onRemoveItem: (String) -> Unit,
        onPlaceOrder: () -> Unit,
        onBack: () -> Unit,
        cartQuantity: MutableState<Int>
    ) {
        Column {
            TopAppBar(
                title = { Text("Cart") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems.filter { it.third > 0 }) { (uuid, dish, quantity) ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "${dish.name_fr} x$quantity",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val painter = rememberImagePainter(
                                data = dish.images.firstOrNull(),
                                builder = {
                                    crossfade(true)
                                    fallback(R.drawable.plat)
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
                            // Affichage du prix
                            Text(
                                "Prix unitaire: ${dish.prices.first().price}€",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Total: ${"%.2f".format(dish.prices.first().price.toDouble() * quantity)}€",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row { // Commence une nouvelle Row pour les boutons
                                Button(
                                    onClick = { onRemoveItem(uuid) },
                                    modifier = Modifier
                                        .padding(end = 4.dp) // Ajoute un peu de padding entre les boutons
                                ) {
                                    Text("Supprimer 1 quantité")
                                }

                                Button(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .padding(start = 4.dp) // Padding pour éviter le chevauchement des boutons
                                ) {
                                    Text("Continuer les achats")
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onPlaceOrder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Place Order: Total price ${"%.2f".format(cartItems.sumOf { it.second.prices.first().price.toDouble() * it.third })}€")
            }
        }
    }



    private suspend fun removeFromCartFile(uuidToRemove: String, activity: ComponentActivity) {
        val filename = "cart.json"
        val file = File(activity.filesDir, filename)
        if (!file.exists()) return

        val itemsArray = JSONArray(file.readText())

        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            if (item.getString("uuid") == uuidToRemove) {
                val newQuantity = item.getInt("quantity") - 1
                if (newQuantity <= 0) {
                    itemsArray.remove(i)
                } else {
                    item.put("quantity", newQuantity)
                }
                break
            }
        }

        file.writeText(itemsArray.toString())

        val sharedPreferences = activity.getSharedPreferences("PREFERENCES", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("cart_quantity", sharedPreferences.getInt("cart_quantity", 0) - 1)
        editor.apply()
    }


    private fun clearCartFile(activity: ComponentActivity) {
        val file = File(activity.filesDir, "cart.json")
        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.d("CartActivity", "Le fichier cart.json a été supprimé.")
            } else {
                Log.d("CartActivity", "Échec de la suppression du fichier cart.json.")
            }
        }
    }
}
