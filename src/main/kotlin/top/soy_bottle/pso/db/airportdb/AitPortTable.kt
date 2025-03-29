package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class Airport(id: EntityID<Short>) : Entity<Short>(id) {
	val airportId: Short by AirportTable.airportId
	val iata: String? by AirportTable.iata
	val icao: String by AirportTable.icao
	val name: String by AirportTable.name
	
	companion object : EntityClass<Short, Airport>(AirportTable)
}

object AirportTable : IdTable<Short>("airport") {
	val airportId: Column<Short> = short("airport_id").autoIncrement()
	val iata: Column<String?> = char("iata", 3).index("iata_idx").nullable()
	val icao: Column<String> = char("icao", 4).uniqueIndex("icao_unq")
	val name: Column<String> = varchar("name", 50).index("name_idx")
	
	override val id: Column<EntityID<Short>> = airportId.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(id)
}

