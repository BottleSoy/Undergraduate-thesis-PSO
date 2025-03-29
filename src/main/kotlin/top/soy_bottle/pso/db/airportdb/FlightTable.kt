package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime

class Flight(id: EntityID<Int>) : Entity<Int>(id) {
	val flightId by FlightTable.flightId
	val flightno by FlightTable.flightno
	val from by Airport.referencedOn(FlightTable.from)
	val to by Airport.referencedOn(FlightTable.to)
	val departure by FlightTable.departure
	val arrival by FlightTable.arrival
	val airline by Airline.referencedOn(FlightTable.airlineId)
	val airplane by Airplane.referencedOn(FlightTable.airplaneId)
	
	fun queryRemain(): Int {
		return airplane.capacity.toInt() - BookingTable.select {
			BookingTable.flightId eq this@Flight.flightId
		}.count().toInt()
	}
	
	companion object : EntityClass<Int, Flight>(FlightTable)
}

object FlightTable : IdTable<Int>("flight") {
	val flightId = integer("flight_id").autoIncrement()
	val flightno = char("flightno", 8).index().references(FlightscheduleTable.flightno, fkName = "flight_ibfk_5")
	val from = short("from").references(AirportTable.airportId)
	val to = short("to").references(AirportTable.airportId)
	val departure = datetime("departure")
	val arrival = datetime("arrival")
	val airlineId = short("airline_id").references(AirlineTable.airlineId)
	val airplaneId = integer("airplane_id").references(AirplaneTable.airplaneId)
	
	
	override val id: Column<EntityID<Int>> = flightId.entityId()
	
}