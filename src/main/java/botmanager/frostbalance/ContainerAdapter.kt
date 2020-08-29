package botmanager.frostbalance

import botmanager.frostbalance.grid.Container
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ContainerAdapter : JsonDeserializer<Container> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Container {
        val container = context!!.deserialize<Container>(json, Container::class.java)
        println("$container is now adopting")
        container.adopt()
        return container
    }


}
