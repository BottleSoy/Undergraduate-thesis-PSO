package top.soy_bottle.pso.db.airportdb.example

import top.soy_bottle.pso.db.airportdb.*
import java.math.BigDecimal
import java.time.LocalDateTime

class EflightInfo(
	val flightId: Int,
	val flightno: String,
	val depart: String,
	val departTime: LocalDateTime,
	val arrival: String,
	val arrivalTime: LocalDateTime,
	
	val passengerInfos: List<PassengerInfo>,
)

class PassengerInfo(
	val seat: String?,
	val price: BigDecimal,
	val firstName: String,
	val lastName: String,
	val passport: String,
	val gender: String,
	val telephone: String,
)

fun qureyFlightEmployee(flightId: Int): EflightInfo {
	val passengers = Booking.find {
		BookingTable.flightId eq flightId
	}.map {
		val passenger = it.passenger
		val detail = Passengerdetails.get(passenger.id)
		PassengerInfo(
			it.seat,
			it.price,
			passenger.firstname,
			passenger.lastname,
			passenger.passportno,
			detail.sex ?: "",
			detail.telephoneno,
		)
	}
	val flight = Flight[flightId]
	return EflightInfo(
		flightId,
		flight.flightno,
		flight.from.name,
		flight.departure,
		flight.to.name,
		flight.arrival,
		passengers
	)
}