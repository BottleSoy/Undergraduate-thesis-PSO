package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

enum class Weather {
	Nebel_Schneefall,//雾 雪
	Schneefall,//降雪
	Regen,//雨
	Regen_Schneefall,//雨 雪
	Nebel_Regen,//雨 雾
	Nebel_Regen_Gewitter,//雨 雾 雷暴
	Gewitter,//雷暴
	Nebel,//雾
	Regen_Gewitter,//雨 雷暴
}

data class WeatherData(
	val logDate: LocalDate,
	val time: LocalTime,
	val station: Int,
	val temp: BigDecimal,
	val humidity: BigDecimal,
	val airPressure: BigDecimal,
	val wind: BigDecimal,
	val weather: Weather?,
	val winddirection: Int
)
object WeatherDataTable : Table("weatherdata") {
	val logDate = date("log_date").index()
	val time = time("time")
	val station = integer("station")
	val temp = decimal("temp", 3, 1)
	val humidity = decimal("humidity", 4, 1)
	val airpressure = decimal("airpressure", 10, 2)
	val wind = decimal("wind", 5, 2)
	val weather = enumerationByName("weather", 255, Weather::class).nullable()
	val winddirection = integer("winddirection")
	
	override val primaryKey = PrimaryKey(logDate, time, station)
	
	fun fetch(date: LocalDate, time: LocalTime, stationId: Int): WeatherData? {
		return transaction {
			WeatherDataTable.select {
				(WeatherDataTable.logDate eq date) and
					(WeatherDataTable.time eq time) and
					(WeatherDataTable.station eq stationId)
			}.map {
				WeatherData(
					logDate = it[WeatherDataTable.logDate],
					time = it[WeatherDataTable.time],
					station = it[WeatherDataTable.station],
					temp = it[WeatherDataTable.temp],
					humidity = it[WeatherDataTable.humidity],
					airPressure = it[WeatherDataTable.airpressure],
					wind = it[WeatherDataTable.wind],
					weather = it[WeatherDataTable.weather],
					winddirection = it[WeatherDataTable.winddirection]
				)
			}.singleOrNull() // This returns null if no record is found, or the single result if one match is found
		}
	}
}