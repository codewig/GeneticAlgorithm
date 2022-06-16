import java.util.logging.Logger

interface Loggable {
    fun logger(): Logger {
        return Logger.getLogger(unwrapCompanionClass(this.javaClass).name)
    }

    fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
        return ofClass.enclosingClass?.takeIf {
            ofClass.enclosingClass.kotlin.objectInstance?.javaClass == ofClass
        } ?: ofClass
    }
}