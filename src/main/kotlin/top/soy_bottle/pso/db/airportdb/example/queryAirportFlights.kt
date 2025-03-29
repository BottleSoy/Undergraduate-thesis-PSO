package top.soy_bottle.pso.db.airportdb.example

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import top.soy_bottle.pso.db.airportdb.AirportTable
import top.soy_bottle.pso.db.airportdb.FlightTable

fun queryAirportFlights(airportId: Short): Pair<List<ResultRow>, List<ResultRow>> {
	return transaction {
		val departs = FlightTable.join(AirportTable,JoinType.INNER,AirportTable.airportId eq FlightTable.to).selectAll().where {
			FlightTable.from eq airportId
		}
		val arrival = FlightTable.join(AirportTable,JoinType.INNER,AirportTable.airportId eq FlightTable.from).selectAll().where{
			FlightTable.to eq airportId
		}
		departs.toList() to arrival.toList()
	}
}