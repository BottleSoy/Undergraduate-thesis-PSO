package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class Airplane(id: EntityID<Int>) : Entity<Int>(id) {
	val airplaneId by AirplaneTable.airplaneId
	val capacity by AirplaneTable.capacity
	val typeId by AirplaneTable.typeId
	val airlineId by AirplaneTable.airlineId
	
	companion object : EntityClass<Int, Airplane>(AirplaneTable)
}

object AirplaneTable : IdTable<Int>("airplane") {
	val airplaneId = integer("airplane_id").autoIncrement()
	val capacity = ushort("capacity")
	val typeId = integer("type_id").references(AirPlaneTypeTable.typeId)
	val airlineId = integer("airline_id")
	
	override val id: Column<EntityID<Int>> = airplaneId.entityId()
	override val primaryKey = PrimaryKey(airplaneId)
}