package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class AirPlaneType(id: EntityID<Int>) : Entity<Int>(id) {
	val typeId by AirPlaneTypeTable.typeId
	val identifier by AirPlaneTypeTable.identifier
	val description by AirPlaneTypeTable.description
	
	companion object : EntityClass<Int, AirPlaneType>(AirPlaneTypeTable)
}

object AirPlaneTypeTable : IdTable<Int>("airplane_type") {
	val typeId: Column<Int> = integer("type_id").autoIncrement()
	val identifier: Column<String?> = varchar("identifier", 50).nullable()
	val description: Column<String> = text("description")
	
	override val id: Column<EntityID<Int>> = typeId.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(id)
}