package top.soy_bottle.pso.db.airportdb.example

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import top.soy_bottle.pso.createDatabase
import top.soy_bottle.pso.db.airportdb.*
import java.time.LocalDateTime

data class FlightInfo(
	val flightId: Int,
	val flightno: String,
	val availableSeats: Int,
	val from: String,
	val to: String,
	val departure: LocalDateTime,
	val arrival: LocalDateTime,
)


fun queryFlightUser(flightId: Int): FlightInfo {
	return transaction {
		val from = Alias(AirportTable, "from")
		val to = Alias(AirportTable, "to")
		val booking =
			BookingTable.select(listOf(BookingTable.flightId, BookingTable.flightId.count().alias("count")))
				.groupBy(BookingTable.flightId).alias("booking")
		val res = FlightTable
			.join(AirplaneTable, JoinType.INNER, FlightTable.airplaneId, AirplaneTable.airplaneId)
			.join(from, JoinType.INNER, FlightTable.from, AirportTable.airportId.aliasTable(from))
			.join(to, JoinType.INNER, FlightTable.to, AirportTable.airportId.aliasTable(to))
			.join(booking, JoinType.INNER, FlightTable.flightId, BookingTable.flightId)
			.select(
				FlightTable.flightno,
				AirportTable.name.aliasTable(from),
				AirportTable.name.aliasTable(to),
				FlightTable.departure,
				FlightTable.arrival,
				AirplaneTable.capacity,
				booking.fields[1]
			).where {
				FlightTable.flightId eq flightId
			}.single()
		
		FlightInfo(
			flightId,
			res[FlightTable.flightno],
			res[AirplaneTable.capacity].toInt() - res[booking.fields[1] as Expression<Int>],
			res[AirportTable.name.aliasTable(from)],
			res[AirportTable.name.aliasTable(to)],
			res[FlightTable.departure],
			res[FlightTable.arrival]
		)
	}
}

fun <T> Column<T>.aliasTable(to: Alias<*>): Expression<T> {
	return Column<T>(to, this.name, this.columnType)
}



fun main() {
	val db = createDatabase()
	
	val info = transaction(db) {
		addLogger(StdOutSqlLogger)
		queryFlightUser(54352)
	}
	println(info)
}