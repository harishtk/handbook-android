package com.handbook.app.core.designsystem.component.forms

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.fetch.Fetcher
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import coil.request.videoFrameOption
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.handbook.app.BuildConfig
import com.handbook.app.core.designsystem.HandbookIcons
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.dashedBorder
import com.handbook.app.ifDebug
import com.handbook.app.ui.cornerSizeMedium
import com.handbook.app.ui.cornerSizeSmall
import com.handbook.app.ui.defaultIconSize
import com.handbook.app.ui.insetMedium
import com.handbook.app.ui.insetSmall
import com.handbook.app.ui.insetVerySmall
import com.handbook.app.ui.largeIconSize
import com.handbook.app.ui.mediumIconSize
import com.handbook.app.ui.smallButtonHeightMax
import com.handbook.app.ui.smallIconSize
import com.handbook.app.ui.theme.LightGray100
import com.handbook.app.ui.theme.LightGray200
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.TextSecondary
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Response: {
 *     "id": 0,
 *     "videoOriginalName": "video_1702120010638.mp4",
 *     "prodTitle": "Earrings",
 *     "prodCategory": "test",
 *     "prodDesc": "test",
 *     "prodImage": "prodImage_1702120207082.png",
 *     "prodTags": "",
 *     "width": 1080,
 *     "height": 1920,
 *     "duration": 23,
 *     "section": [
 *         {
 *             "id": 0,
 *             "color": "",
 *             "image": "",
 *             "sizePriceQty": [
 *                 {
 *                 "id": 0,
 *                 "price": 3445,
 *                 "discount": 10,
 *                 "qty": 100,
 *                 "size": ""
 *                 }
 *             ]
 *         }
 *     ]
 * }
 */
data class ProductDetailDto(
    val id: Long,
    val videoOriginalName: String,
    val productTitle: String,
    val productCategory: String,
    val productDescription: String,
    val productImage: String,
    val productTags: String,
    val width: Int,
    val height: Int,
    val duration: Int,
    val sections: List<SectionDto>,
)
/**
 * {
 *  *             "id": 0,
 *  *             "color": "",
 *  *             "image": "",
 *  *             "sizePriceQty": [
 *  *                 {
 *  *                 "id": 0,
 *  *                 "price": 3445,
 *  *                 "discount": 10,
 *  *                 "qty": 100,
 *  *                 "size": ""
 *  *                 }
 *  *             ]
 *  *         }
 */
data class SectionDto(
    val id: Long,
    val color: String,
    val image: String,
    val sizePriceQtys: List<SizePriceQtyDto>,
)

data class SizePriceQtyDto(
    val id: Long,
    val price: Int,
    val discount: Int,
    val quantity: Int,
    val size: String,
)

enum class MediaType(
    val mimeType: String
) { Unknown("*"), Image("image/*"), Video("video/*") }

data class SellerMediaFile(
    val id: Long = -1,
    val uri: Uri = Uri.EMPTY,
    val mediaType: MediaType = MediaType.Unknown,
    val remoteFileName: String? = null,
    val createdAt: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val duration: Long = 0,
    val cachedFile: File? = null,
)

data class ProductFormData(
    val id: Long = -1,
    val title: String = "",
    val description: String = "",
    val categoryName: String = "",
    val price: String = "",
    val discount: String = "",
    val quantity: Int = 0,
    val videos: List<UploadPreviewUiModel> = emptyList(),
    val images: List<UploadPreviewUiModel> = emptyList(),
)

interface UploadPreviewUiModel {
    data class Item(val sellerMediaFile: SellerMediaFile) : UploadPreviewUiModel
    data class Placeholder(val position: Int) : UploadPreviewUiModel
}

private const val ProductNameLength = 50
private const val ProducPriceLength = 10
private const val ProductDiscountLength = 3
private const val ProductDescriptionLength = 300

private val TextFieldBackground = Color(0xFFFAFAFA)

/**
 * TODO: 1. Add product name input
 *       2. Add product category input
 *       3. Add product price and discount input
 *       4. Add product Quantity input
 *       5. Add product description
 *       6. A single upload and preview unit
 *       7. A multiple image upload and preview unit
 *       8. Form validation
 */
@Composable
internal fun ColumnScope.AddProductForm(
    formData: ProductFormData = ProductFormData(),
    launchMediaPicker: (productId: Long, type: MediaType) -> Unit = { _, _ -> },
    onDeleteMedia: (productId: Long, type: MediaType, uri: Uri) -> Unit = { _, _, _ -> },
) {
    val productNameState by rememberSaveable(stateSaver = ProductNameStateSaver) {
        mutableStateOf(ProductNameState(""))
    }
    val productPriceState by rememberSaveable(stateSaver = ProductPriceStateSaver) {
        mutableStateOf(ProductPriceState(""))
    }
    val productDiscountState by rememberSaveable(stateSaver = ProductDiscountSaver) {
        mutableStateOf(ProductDiscountState(""))
    }
    val productQuantityState by rememberSaveable(stateSaver = ProductDiscountStateSaver) {
        mutableStateOf(ProductQuantityState(""))
    }
    val productDescriptionState by rememberSaveable(stateSaver = ProductDescriptionStateSaver) {
        mutableStateOf(ProductDescriptionState(""))
    }

    ProductNameInput(productNameState = productNameState)
    ProductCategoryInput(provideCategoryName = { "" })

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ProductPriceInput(
            productPriceState = productPriceState,
            modifier = Modifier.weight(0.55F)
        )
        ProductDiscountInput(
            productDiscountState = productDiscountState,
            modifier = Modifier.weight(0.45F)
        )
    }

    ProductQuantityInput(productQuantityState = productQuantityState)
    ProductDescriptionInput(productDescriptionState = productDescriptionState)

    ProductVideoInput(
        mediaFiles = formData.videos,
        onPlaceHolderClick = { launchMediaPicker(formData.id, MediaType.Video) },
        onDeleteClick = { uri ->
            onDeleteMedia(formData.id, MediaType.Video, uri)
        }
    )
    ProductImageInput(
        mediaFiles = formData.images,
        onPlaceHolderClick = { launchMediaPicker(formData.id, MediaType.Image) },
        onDeleteClick = { uri ->
            onDeleteMedia(formData.id, MediaType.Image, uri)
        }
    )
}

@Composable
private fun ProductNameInput(
    modifier: Modifier = Modifier,
    productNameState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Product Title")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )


        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGray100,
            unfocusedContainerColor = LightGray100,
            disabledContainerColor = LightGray100,
            focusedBorderColor = LightGray200,
            unfocusedBorderColor = LightGray200,
        )
        OutlinedTextField(
            value = productNameState.text,
            onValueChange = { text ->
                productNameState.text = text.take(ProductNameLength)
                onValueChange(productNameState.text)
            },
            placeholder = {
                Text(
                    text = "Enter product title",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .padding(vertical = insetVerySmall),
        )
    }
}

@Composable
private fun ProductCategoryInput(
    modifier: Modifier = Modifier,
    provideCategoryName: () -> String,
    onDropDownClick: () -> Unit = {},
) {
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium
    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Product Category")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGray100,
            unfocusedContainerColor = LightGray100,
            disabledContainerColor = LightGray100,
            focusedBorderColor = LightGray200,
            unfocusedBorderColor = LightGray200,
        )

        val interactionSource = remember { MutableInteractionSource() }

        OutlinedTextField(
            value = provideCategoryName(),
            onValueChange = { _ ->

            },
            readOnly = true,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = "Enter your product category",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            suffix = {
                Icon(
                    imageVector = HandbookIcons.ChevronRight,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(90f)
                )
            },
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = insetVerySmall),
        )
        val pressedState = interactionSource.interactions.collectAsState(
            initial = PressInteraction.Cancel(PressInteraction.Press(Offset.Zero))
        )
        if (pressedState.value is PressInteraction.Release) {
            onDropDownClick()
            interactionSource.tryEmit(PressInteraction.Cancel(PressInteraction.Press(Offset.Zero)))
        }
    }
}

@Composable
private fun ProductPriceInput(
    modifier: Modifier = Modifier,
    productPriceState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Product Price")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )


        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGray100,
            unfocusedContainerColor = LightGray100,
            disabledContainerColor = LightGray100,
            focusedBorderColor = LightGray200,
            unfocusedBorderColor = LightGray200,
        )
        OutlinedTextField(
            value = productPriceState.text,
            onValueChange = { text ->
                productPriceState.text = text.take(ProducPriceLength)
                onValueChange(productPriceState.text)
            },
            placeholder = {
                Text(
                    text = "Enter Price",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .padding(vertical = insetVerySmall),
        )
    }
}

@Composable
private fun ProductDiscountInput(
    modifier: Modifier = Modifier,
    productDiscountState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Discount")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )


        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGray100,
            unfocusedContainerColor = LightGray100,
            disabledContainerColor = LightGray100,
            focusedBorderColor = LightGray200,
            unfocusedBorderColor = LightGray200,
        )
        OutlinedTextField(
            value = productDiscountState.text,
            onValueChange = { text ->
                productDiscountState.text = text.take(ProductDiscountLength)
                onValueChange(productDiscountState.text)
            },
            placeholder = {
                Text(
                    text = "Enter Discount",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .padding(vertical = insetVerySmall),
        )
    }
}

@Composable
private fun ProductQuantityInput(
    modifier: Modifier = Modifier,
    productQuantityState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Set Product Quantity")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGray100,
            unfocusedContainerColor = LightGray100,
            disabledContainerColor = LightGray100,
            focusedBorderColor = LightGray200,
            unfocusedBorderColor = LightGray200,
        )
        OutlinedTextField(
            value = productQuantityState.text,
            onValueChange = { text ->
                productQuantityState.text = text.take(30)
                onValueChange(productQuantityState.text)
            },
            placeholder = {
                Text(
                    text = "Enter product quantity",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .padding(vertical = insetVerySmall),
        )
    }
}

@Composable
private fun ProductDescriptionInput(
    modifier: Modifier = Modifier,
    productDescriptionState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Product Description")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )


        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGray100,
            unfocusedContainerColor = LightGray100,
            disabledContainerColor = LightGray100,
            focusedBorderColor = LightGray200,
            unfocusedBorderColor = LightGray200,
        )
        OutlinedTextField(
            value = productDescriptionState.text,
            onValueChange = { text ->
                productDescriptionState.text = text.take(ProductDescriptionLength)
                onValueChange(productDescriptionState.text)
            },
            placeholder = {
                Text(
                    text = "Enter product title",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 200.dp)
                .focusRequester(provideFocusRequester())
                .padding(vertical = insetVerySmall),
        )
    }
}

@Composable
private fun ProductVideoInput(
    modifier: Modifier = Modifier,
    mediaFiles: List<UploadPreviewUiModel> = listOf(UploadPreviewUiModel.Placeholder(0)),
    maxFiles: Int = 1,
    onItemClick: () -> Unit = {},
    onPlaceHolderClick: () -> Unit = {},
    onDeleteClick: (uri: Uri) -> Unit = {},
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Upload Product Video")
                /*withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }*/
            },
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            mediaFiles.forEach { mediaFile ->
                if (mediaFile is UploadPreviewUiModel.Placeholder) {
                    ProductMediaPlaceHolder(
                        onClick = onPlaceHolderClick
                    )
                } else if (mediaFile is UploadPreviewUiModel.Item) {
                    ProductMediaRowItem(
                        sellerMediaFile = mediaFile.sellerMediaFile,
                        onDeleteClick = onDeleteClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductImageInput(
    modifier: Modifier = Modifier,
    mediaFiles: List<UploadPreviewUiModel> = listOf(UploadPreviewUiModel.Placeholder(0)),
    maxFiles: Int = 1,
    onItemClick: () -> Unit = {},
    onPlaceHolderClick: () -> Unit = {},
    onDeleteClick: (uri: Uri) -> Unit = {},
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Upload Product Images")
                /*withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }*/
                append(" ")
                withStyle(
                    style = SpanStyle(
                        color = TextSecondary,
                        fontStyle = FontStyle.Italic,
                    ),
                ) {
                    append("(Optional)")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            mediaFiles.forEach { mediaFile ->
                if (mediaFile is UploadPreviewUiModel.Placeholder) {
                    ProductMediaPlaceHolder(
                        onClick = onPlaceHolderClick
                    )
                } else if (mediaFile is UploadPreviewUiModel.Item) {
                    ProductMediaRowItem(
                        sellerMediaFile = mediaFile.sellerMediaFile,
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductMediaRowItem(
    modifier: Modifier = Modifier,
    sellerMediaFile: SellerMediaFile,
    onClick: () -> Unit = {},
    onDeleteClick: (uri: Uri) -> Unit = {},
) {
    Box(
        modifier = modifier
            .padding(insetSmall)
            .widthIn(min = 80.dp, max = 150.dp)
            .aspectRatio(0.7F)
            .background(LightGray100)
            .clip(shape = RoundedCornerShape(cornerSizeMedium))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (sellerMediaFile.mediaType == MediaType.Video) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(sellerMediaFile.uri)
                    .videoFrameMillis(1_000)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product video",
                contentScale = ContentScale.Crop,
            )

            Row(
                modifier = Modifier
                    .padding(insetSmall)
                    .align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val minsUntil = TimeUnit.MILLISECONDS.toMinutes(sellerMediaFile.duration)
                val secondsUntil =
                    sellerMediaFile.duration - (TimeUnit.MINUTES.toMillis(minsUntil))
                val time = String.format(
                    "%02d:%02d",
                    minsUntil,
                    TimeUnit.MILLISECONDS.toSeconds(secondsUntil)
                )

                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.2F),
                            shape = RoundedCornerShape(cornerSizeSmall),
                        ),
                )
                if ((sellerMediaFile.height > sellerMediaFile.width)
                    && sellerMediaFile.width >= 720 || BuildConfig.DEBUG) {
                    Icon(
                        imageVector = HandbookIcons.Hd,
                        contentDescription = "Video quality",
                        modifier = Modifier
                            .width(mediumIconSize),
                    )
                }
            }

            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = HandbookIcons.Play,
                    contentDescription = "Video Preview",
                    tint = Color.Black.copy(alpha = 0.5F),
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(elevation = 1.dp)
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(sellerMediaFile.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product image",
                contentScale = ContentScale.Crop,
            )
        }
        Button(
            onClick = { onDeleteClick(sellerMediaFile.uri) },
            modifier = Modifier
                .padding(insetVerySmall)
                .align(Alignment.TopEnd)
                .heightIn(max = smallButtonHeightMax),
            contentPadding = ButtonDefaults.TextButtonContentPadding,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.White,
                containerColor = Color(0x78000000),
            )
        ) {
            Text(
                text = "Remove",
                style = MaterialTheme.typography.labelMedium
                    .copy(fontWeight = FontWeight.W600)
            )
        }


        ifDebug {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "${sellerMediaFile.width}x${sellerMediaFile.height}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .padding(insetVerySmall)
                        .background(
                            color = Color.Black.copy(alpha = 0.2F),
                            shape = RoundedCornerShape(cornerSizeSmall),
                        ),
                )
                Text(
                    text = "Remote File: ${sellerMediaFile.remoteFileName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .padding(insetVerySmall)
                        .background(
                            color = Color.Black.copy(alpha = 0.2F),
                            shape = RoundedCornerShape(cornerSizeSmall),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ProductMediaPlaceHolder(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .padding(insetSmall)
            .widthIn(min = 80.dp, max = 150.dp)
            .aspectRatio(0.7F)
            .background(LightGray100)
            .dashedBorder(
                border = BorderStroke(1.dp, LightGray200),
                shape = RoundedCornerShape(cornerSizeMedium),
                on = 10.dp,
                off = 10.dp,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = HandbookIcons.Id_PickMedia),
                contentDescription = "Pick from gallery",
                modifier = Modifier
                    .width(48.dp)
                    .aspectRatio(1F)
            )
            Text(
                text = "Tap to open Gallery",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun AddProductFormPreview() {
    HandbookTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            AddProductForm(onDeleteMedia = { _, _, _ -> })
        }
    }
}

@Composable
@Preview(group = "media preview", showBackground = true)
private fun ProductMediaRowItemPreview() {
    HandbookTheme {
        Column(
            modifier = Modifier
        ) {
            ProductMediaRowItem(
                sellerMediaFile = SellerMediaFile(
                    uri = "https://picsum.photos/id/101".toUri(),
                    mediaType = MediaType.Video
                )
            )
        }
    }
}