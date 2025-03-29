package top.soy_bottle.pso.db.airportdb.example

import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import top.soy_bottle.pso.createDatabase
import top.soy_bottle.pso.db.airportdb.Booking
import top.soy_bottle.pso.db.airportdb.BookingTable
import top.soy_bottle.pso.db.airportdb.Flight
import top.soy_bottle.pso.db.airportdb.Passenger
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.random.Random


fun queryBookingPrice(
	flightId: Int,
): BigDecimal {
	return transaction {
		val booking = Booking.find {
			BookingTable.flightId eq flightId
		}
		booking.sumOf { it.price }
	}
}

fun addBooking(
	passengerId: Int,
	flightId: Int,
	price: BigDecimal,
	seat: String?,
): Int {
	return transaction {
		BookingTable.insertAndGetId {
			it[this.price] = price
			it[this.passengerId] = passengerId
			it[this.flightId] = flightId
			it[this.seat] = seat
		}
	}.value
}

fun main() {
	val database = createDatabase()
	val passengerIds = transaction { Passenger.all().map { it.passengerId } }
	val flightIds = transaction { Flight.all().map { it.flightId } }
	val seatSet = {
		"" + Random.nextInt(0, 110) + "ABCDEFG".random()
	}
	val start = System.currentTimeMillis()
	println("init at $start")
	val jobs = arrayListOf<Job>()
	repeat(10000) {
		val passenger = passengerIds.random()
		val flight = flightIds.random()
		val price = BigDecimal(Random.nextDouble(40.0, 600.0))
		val seat = seatSet()
		jobs += GlobalScope.launch(Dispatchers.IO) {
			queryBookingPrice(flight)
			addBooking(passenger, flight, price, seat)
		}
	}
	val published = LocalDateTime.now()
	println("published at $published")
	
	runBlocking {
		var i = 0
		jobs.forEach {
			it.join()
			i++
			if (i % 1000 == 0) {
				println(i)
			}
		}
	}
	
	val end = System.currentTimeMillis()
	println("end at $end")
	println("time:${(end - start) / 1000.0}")
}