package fr.gallonemilien.websocketcam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebsocketCamApplication

fun main(args: Array<String>) {
	runApplication<WebsocketCamApplication>(*args)
}
