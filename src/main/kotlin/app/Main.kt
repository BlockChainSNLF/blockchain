package app



fun main() {
    val config = AppConfig.fromEnv()
    val node = Node(config)
    node.start()
}