package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class Booking(id: EntityID<Int>) : Entity<Int>(id) {
	val bookingId by BookingTable.bookingId
	val flightId by BookingTable.flightId
	val seat by BookingTable.seat
	val passenger by Passenger.referencedOn(BookingTable.passengerId)
	val price by BookingTable.price
	companion object : EntityClass<Int, Booking>(BookingTable)
}

object BookingTable : IdTable<Int>("booking") {
	val bookingId = integer("booking_id").autoIncrement()
	val flightId = integer("flight_id").references(FlightTable.flightId)
	val seat = char("seat", 4).nullable()
	val passengerId = integer("passenger_id").references(PassengerTable.passengerId)
	val price = decimal("price", 10, 2)
	
	init {
		uniqueIndex(flightId, seat)
	}
	
	override val id: Column<EntityID<Int>> = bookingId.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(bookingId)
}