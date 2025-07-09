package devoid.secure.dm.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.fleeksoft.ksoup.model.MetaData

class UrlDetector(onUrlFound: (startIndex: Int?, endIndex: Int?, url: String?) -> Unit) {
    private val urlRegex = "(https?://|www\\.)\\S+".toRegex()
    val textWatcher = object : TextWatcher() {
        override fun watch(input: String) {
            val match = urlRegex.find(input)
            if (match == null) {
                onUrlFound(null, null, null)
                return
            }
            val url = match.value
            val startIndex = match.range.first
            val endIndex = (match.range.last)
            onUrlFound(startIndex, endIndex, url)
        }
    }
}

@Composable
fun rememberUrlDetector(onUrlFound: (startIndex: Int?, endIndex: Int?, url: String?) -> Unit): UrlDetector {
    return remember {
        UrlDetector(onUrlFound)
    }
}

abstract class TextWatcher {
    abstract fun watch(input: String)
}

fun getUrlAnnotatedString(input: String, startIndex: Array<Int>, endIndex: Array<Int>): AnnotatedString {
   return buildAnnotatedString {
        var startIndexTmp = 0
        startIndex.forEachIndexed { index, value ->
            val url = input.substring(startIndex = value, endIndex = endIndex[index])
            append(input.substring(startIndexTmp, value))
            withLink(
                LinkAnnotation.Url(
                    url = url,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            fontWeight = FontWeight.Medium,
                            color = Color.Cyan,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                ),
                block = {
                    append(url)
                }
            )
//            //appendEndIndex will be startIndex[index+1] if exists else input.length-1
            val appendEndIndex = if (index >= startIndex.size-1) input.length else startIndex[index+1]
            startIndexTmp = appendEndIndex
                append(input.substring(startIndex = endIndex[index], endIndex = appendEndIndex))
        }
    }

//    return buildAnnotatedString {
//        append(input.substring(0, startIndex))
//        withLink(
//            LinkAnnotation.Url(
//                url = url,
//                styles = TextLinkStyles(
//                    style = SpanStyle(
//                        fontWeight = FontWeight.Medium,
//                        color = Color.Cyan,
//                        textDecoration = TextDecoration.Underline
//                    )
//                )
//            ),
//            block = {
//                append(url)
//            }
//        )
//        append(input.substring(startIndex = endIndex, endIndex = input.length - 1))
//    }
}

data class UrlMetadata(val url: String, val title: String?, val subTitle: String?, val image: String?)

fun MetaData.toUrlMetaData(): UrlMetadata {
    return UrlMetadata(url = this.ogUrl ?: "", title = title, subTitle = description, image = ogImage)
}

fun getUrlFromString(input: String): UrlResult {
    val urlRegex = "(https?://|www\\.)\\S+".toRegex()
    val matches = urlRegex.findAll(input)
    val urls = ArrayList<String>(matches.count())
    val startIndex = ArrayList<Int>(matches.count())
    val endIndex = ArrayList<Int>(matches.count())
    matches.forEach { match ->
        urls.add(match.value)
        startIndex.add(match.range.first)
        endIndex.add(match.range.last)
        val url = match.value
    }
//    val startIndex = matches?.range?.first
//    val endIndex = (matches?.range?.last)
    return UrlResult(startIndex.toTypedArray(), endIndex.toTypedArray(), urls.toTypedArray(), input)
}

data class UrlResult(
    val startIndex: Array<Int>, val endIndex: Array<Int>, val urls: Array<String>, val originalString: String,
)