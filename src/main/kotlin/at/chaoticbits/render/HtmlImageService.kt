package at.chaoticbits.render

import at.chaoticbits.coinmarket.CurrencyDetails
import at.chaoticbits.config.DecimalFormatter
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode.HTML
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.xhtmlrenderer.swing.Java2DRenderer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.util.*
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory


object HtmlImageService {

    private val templateEngine = TemplateEngine()


    /**
     * Initialize TemplateResolver and TemplateEngine
     */
    init {

        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.prefix = "/"
        templateResolver.suffix = ".html"
        templateResolver.templateMode = HTML
        templateResolver.characterEncoding = "UTF-8"

        templateEngine.setTemplateResolver(templateResolver)
    }


    /**
     * Generates the rendered HTML with the given currency details
     * and converts it into an image InputStream
     *
     * @param currencyDetails [CurrencyDetails] Holding information about a crypto currency
     * @return [InputStream] Containing information about the rendered image
     */
    @Throws(IllegalStateException::class)
    fun generateCryptoDetailsImage(currencyDetails: CurrencyDetails): InputStream {
        val context = Context(Locale.forLanguageTag("de-AT"))

        context.setVariable("currencyDetails", currencyDetails)
        context.setVariable("DecimalFormatter", DecimalFormatter)
        context.setVariable("changeColors", getChangePercentageColor(currencyDetails))

        val html = templateEngine.process("html/currency-details.html", context)

        try {

            val htmlInputStream = ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8))

            // create a w3c document of the generated html input stream
            val b = DocumentBuilderFactory.newInstance()
            b.isNamespaceAware = false
            val db = b.newDocumentBuilder()
            val doc = db.parse(htmlInputStream)

            // write image into output stream
            val os = ByteArrayOutputStream()
            ImageIO.write(Java2DRenderer(doc, 1800).image, "png", os)
            return ByteArrayInputStream(os.toByteArray())

        } catch (e: Exception) {
            throw IllegalStateException("Error writing Image: " + e.message)
        }

    }


    /**
     * Populates a Map of colors according to negative and positive percentages
     *
     * @param currencyDetails Holding information about a crypto currency
     * @return [Map] Where the key is the name of the percentage and the value the color code.
     */
    private fun getChangePercentageColor(currencyDetails: CurrencyDetails): Map<String, String> {


        val changesPositive = mutableMapOf<String, BigDecimal>()
        val changesNegative = mutableMapOf<String, BigDecimal>()

        val colors = HashMap<String, String>()

        if (currencyDetails.change1h == null)
            colors["change1h"] = "#757575"
        else {
            if (currencyDetails.change1h > BigDecimal.ZERO)
                changesPositive["change1h"] = currencyDetails.change1h
            else
                changesNegative["change1h"] = currencyDetails.change1h
        }

        if (currencyDetails.change24h == null)
            colors["change24h"] = "#757575"
        else {
            if (currencyDetails.change24h > BigDecimal.ZERO)
                changesPositive["change24h"] = currencyDetails.change24h
            else
                changesNegative["change24h"] = currencyDetails.change24h
        }

        if (currencyDetails.change7d == null)
            colors["change7d"] = "#757575"
        else {
            if (currencyDetails.change7d > BigDecimal.ZERO)
                changesPositive["change7d"] = currencyDetails.change7d
            else
                changesNegative["change7d"] = currencyDetails.change7d
        }


        colors.putAll(getColors(changesPositive, true))
        colors.putAll(getColors(changesNegative, false))

        return colors
    }


    /**
     * Populates a Map with red or green colors
     * depending on the positive flag and the containing values in the map
     *
     * @param map [Map] Containing percentage values
     * @param positive [Boolean] Determines if the values in the map are positive or negative
     * @return [Map] Where the key is the name of the percentage and the value the color code.
     */
    private fun getColors(map: MutableMap<String, BigDecimal>, positive: Boolean): Map<String, String> {

        val colors = HashMap<String, String>()

        val min: String = getMinValue(map) ?: return colors
        colors[min] = if (positive) "#4CAF50" else "#BF360C"
        map.remove(min)

        val middle: String = getMinValue(map) ?: return colors
        colors[middle] = if (positive) "#388E3C" else "#E64A19"
        map.remove(middle)

        val max: String = getMinValue(map) ?: return colors
        colors[max] = if (positive) "#2E7D32" else "#FF7043"
        return colors
    }


    /**
     * Determines the minimum value in the given Map
     *
     * @param map [Map] Containing percentage values
     * @return [String] Name of the minimum value in the given map
     */
    private fun getMinValue(map: Map<String, BigDecimal>): String? {

        val min = map.minBy { entry -> entry.value }
        return when (min) {
            null -> null
            else -> min.key
        }
    }


}
