package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class AirportReachable(id: EntityID<Short>) : Entity<Short>(id) {
	val airport by Airport.referencedOn(AirportReachableTable.airportId)
	val hops by AirportReachableTable.hops
}

object AirportReachableTable : IdTable<Short>("airport_reachable") {
	val airportId: Column<Short> = short("airport_id").references(AirportTable.id)
	val hops: Column<Int?> = integer("hops").nullable()
	
	override val id: Column<EntityID<Short>> = airportId.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(id)
}