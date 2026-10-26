package com.quimia.android.presentation.screens.catalog

import androidx.annotation.DrawableRes
import com.quimia.android.R

data class CatalogProduct(
    val id: String,
    val name: String,
    val category: String,
    @param:DrawableRes val imageRes: Int,
    @param:DrawableRes val logoRes: Int,
    @param:DrawableRes val detailImageRes: Int = imageRes,
    val description: String = "Descrição não disponível para este produto.",
    val barcode: String = "",
    val imageUrl: String? = null,
    val logoUrl: String? = null,
    val brandName: String = "",
    val safetyInformation: String? = null,
)

@androidx.compose.runtime.Composable
fun CatalogProduct.imagePainter(detail: Boolean = false): androidx.compose.ui.graphics.painter.Painter {
    val resource = if (detail) detailImageRes else imageRes
    return if (resource != 0) androidx.compose.ui.res.painterResource(resource) else coil.compose.rememberAsyncImagePainter(imageUrl)
}

@androidx.compose.runtime.Composable
fun CatalogProduct.logoPainter(): androidx.compose.ui.graphics.painter.Painter =
    if (logoRes != 0) androidx.compose.ui.res.painterResource(logoRes) else coil.compose.rememberAsyncImagePainter(logoUrl)

private val mrMusculo = CatalogProduct(
    id = "squeeze",
    name = "Limpador Cozinha Squeeze",
    category = "Limpador multiuso",
    imageRes = R.drawable.catalog_mr_musculo,
    logoRes = R.drawable.catalog_logo_mr_musculo,
    detailImageRes = R.drawable.catalog_mr_musculo_detail,
    description = "O Mr Músculo Tira Limo contém uma espuma para acabar com manchas de mofo e umidade que penetra e remove as sujeiras mais difíceis do banheiro, para você limpar menos e viver mais.",
    barcode = "4784784837498",
)

val sampleCatalogProducts = listOf(
    mrMusculo,
    CatalogProduct(
        id = "veja",
        name = "Limpador Multiuso Veja",
        category = "Limpador multiuso",
        imageRes = R.drawable.catalog_veja,
        logoRes = R.drawable.catalog_logo_veja,
    ),
    CatalogProduct(
        id = "omo",
        name = "Lava-Roupas Líquido Omo",
        category = "Lava-roupas",
        imageRes = R.drawable.catalog_omo,
        logoRes = R.drawable.catalog_logo_omo,
    ),
    mrMusculo.copy(id = "squeeze-2"),
    CatalogProduct(
        id = "omo-2",
        name = "Lava-Roupas Líquido Omo",
        category = "Lava-roupas",
        imageRes = R.drawable.catalog_omo,
        logoRes = R.drawable.catalog_logo_omo,
    ),
)
