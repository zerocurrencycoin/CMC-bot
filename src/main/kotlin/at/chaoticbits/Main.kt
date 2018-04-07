package at.chaoticbits

import at.chaoticbits.updateshandlers.CryptoHandler
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.logging.BotLogger
import org.telegram.telegrambots.logging.BotsFileHandler

import java.io.IOException
import java.util.logging.Level


private const val LOG_TAG = "MAIN"


/**
 *  Application entry point. Initializes Telegram Bot and BotLogger
 */
fun main(args: Array<String>) {

    BotLogger.setLevel(Level.ALL)
    try {
        BotLogger.registerLogger(BotsFileHandler("./TelegramBots%g.%u.log"))
    } catch (e: IOException) {
        BotLogger.severe(LOG_TAG, e)
    }

    // Exit if no Telegram Bot Token is specified
    if (System.getenv("CMBOT_TELEGRAM_TOKEN") == null) {
        BotLogger.error(LOG_TAG, "No Telegram Bot Token specified! Please declare a System Environment Variable with your Telegram API Key. CMBOT_TELEGRAM_TOKEN={YOUR_API_KEY}")
        return
    }

    initTelegramBot()
}


private fun initTelegramBot() {

    ApiContextInitializer.init()
    val telegramBotsApi = TelegramBotsApi()

    try {

        // Register long polling bots. They work regardless type of TelegramBotsApi
        telegramBotsApi.registerBot(CryptoHandler())

    } catch (e: TelegramApiException) {
        BotLogger.error(LOG_TAG, e)
    }
}