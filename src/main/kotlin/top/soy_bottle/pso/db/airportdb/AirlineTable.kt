package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class Airline(id: EntityID<Short>) : Entity<Short>(id) {
	val airlineId: Short by AirlineTable.airlineId
	val iata: String by AirlineTable.iata
	val airlinename: String? by AirlineTable.airlinename
	val baseAirPort: Airport by Airport.referencedOn(AirlineTable.baseAirport)
	
	companion object : EntityClass<Short, Airline>(AirlineTable)
}

object AirlineTable : IdTable<Short>("airline") {
	val airlineId: Column<Short> = short("airline_id").autoIncrement()
	val iata: Column<String> = char("iata", 2).uniqueIndex("iata_unq")
	val airlinename: Column<String?> = varchar("airlinename", 30).nullable()
	val baseAirport: Column<Short> = short("base_airport").references(AirportTable.id, fkName = "airline_ibfk_1")
	
	
	override val id: Column<EntityID<Short>> = airlineId.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(id)
}