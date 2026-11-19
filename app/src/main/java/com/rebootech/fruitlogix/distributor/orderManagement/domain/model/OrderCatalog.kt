package com.rebootech.fruitlogix.distributor.orderManagement.domain.model

/** A commercial client of the distributor. [id] is the server's commercialClientId. */
data class CommercialClient(val id: Int, val name: String, val deliveryAddress: String)

/** A product that can be ordered. [id] is the server's productId; prices are per kg (soles). */
data class CatalogProduct(val id: Int, val name: String, val pricePerKg: Double)

/**
 * Fixed list of clients and products with their server ids.
 * The server stores only numbers (commercialClientId, productId) and has no endpoint to list
 * clients or products yet, so the app keeps this list. When the server adds those endpoints,
 * this becomes a repository call.
 */
object OrderCatalog {

    val clients: List<CommercialClient> = listOf(
        CommercialClient(101, "Supermercados Lima Norte", "Av. Alfredo Mendiola 3698, Independencia, Lima"),
        CommercialClient(102, "Supermercados Lima Centro", "Av. Alfonso Ugarte 1140, Breña, Lima"),
        CommercialClient(103, "Mercados Lima Sur", "Panamericana Sur Km 18.5, San Juan de Miraflores, Lima"),
        CommercialClient(104, "Plaza Vea Callao", "Av. Sáenz Peña 1250, Callao"),
        CommercialClient(105, "Distribuidora Andina", "Mercado Mayorista Santa Anita, Pabellón A, Lima"),
        CommercialClient(106, "Distribuidora Pacífico", "Av. Néstor Gambetta 6429, Callao")
    )

    val products: List<CatalogProduct> = listOf(
        CatalogProduct(1, "Palta Hass", 9.40),
        CatalogProduct(2, "Uva Red Globe", 8.60),
        CatalogProduct(3, "Mango Kent", 7.50),
        CatalogProduct(4, "Arándano", 18.50),
        CatalogProduct(5, "Fresa", 6.80),
        CatalogProduct(6, "Lima Tahití", 4.20)
    )

    fun client(id: Int?): CommercialClient? = clients.firstOrNull { it.id == id }

    fun clientByName(name: String): CommercialClient? = clients.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    fun product(name: String): CatalogProduct? = products.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
}
