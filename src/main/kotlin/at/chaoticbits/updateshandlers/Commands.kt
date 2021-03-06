package at.chaoticbits.updateshandlers

import at.chaoticbits.coinmarket.CoinMarketCapService
import at.chaoticbits.coinmarket.CurrencyDetails
import at.chaoticbits.coinmarket.CurrencyNotFoundException
import at.chaoticbits.render.HtmlImageService
import mu.KotlinLogging
import net.logstash.logback.marker.Markers
import org.telegram.telegrambots.api.methods.send.SendPhoto
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.exceptions.TelegramApiException
import java.io.UnsupportedEncodingException


private val log = KotlinLogging.logger {}



/**
 * Returns the welcome message for first time bot users
 *
 * @return [String] Containing the start message
 */
fun startCommand() =
        "*Welcome!*\n\n" +
                "My name is *cmbot* and I am programmed to give you the newest price information about all crypto currencies from CoinMarketCap.\n\n" +
                "Use /help to see a list of my supported commands"



/**
 * Returns the bot's help command
 *
 * @param botUsername [String] Username of this bot
 * @return [String] Containing the help message
 */
fun helpCommand(botUsername: String): String =
        "You can control me by sending the following commands:\n\n" +
                "*Commands*\n" +
                "/coin currency *-* Request a coin from CoinMarketCap. *(i.e. /coin eth)*\n" +
                "/help *-* Display the current help\n" +
                "/start *-* Display the welcome message\n\n" +
                "*Inline Queries*\n" +
                "This is the recommended way to request price information. " +
                "Just use @$botUsername to search through all coins on CoinMarketCap.\n\n" +
                "For more information visit [cmbot](https://github.com/yashino91/cmbot/)"



/**
 * Returns a command not found message
 *
 * @param command [String] The requested user command
 * @return [String] Containing a command not found message
 */
fun commandNotFound(command: String): String = "Command not found: *$command*. Use /help for a list of supported commands"



/**
 * Queries the given currency from CoinMarketCap and returns the result as a rendered image
 *
 * @param message [Message] The requested user message
 * @param currency [String] The requested currency
 * @return [SendPhoto] Object containing a rendered image that displays price information about the requested coin
 */
@Throws(IllegalStateException::class, UnsupportedEncodingException::class, CurrencyNotFoundException::class)
fun coinCommand(message: Message, currency: String): SendPhoto {

    val currencyDetails: CurrencyDetails = CoinMarketCapService.fetchCurrency(currency)

    logCurrencyRequest(message, currencyDetails, "image")

    val photo = SendPhoto()
    photo.setChatId(message.chatId)
    photo.setNewPhoto(currency, HtmlImageService.generateCryptoDetailsImage(currencyDetails))

    return photo
}



/**
 * Logs a currency request in json format for logging frameworks like logstash
 *
 * @param message [Message] The requested user message
 * @param currencyDetails [CurrencyDetails] Holding price details about a coin
 * @param type [String] Determines if the currency was requested as a string or image
 */
fun logCurrencyRequest(message: Message, currencyDetails: CurrencyDetails, type: String) {
    val loggingObjects = mapOf(
            "from" to message.from,
            "chat" to message.chat,
            "currency" to currencyDetails,
            "requestType" to type)

    log.info(Markers.appendEntries(loggingObjects), "${message.from} from ${message.chat} requested $currencyDetails, type='$type'}")
}
