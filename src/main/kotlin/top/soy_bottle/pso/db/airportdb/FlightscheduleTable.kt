package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.time
import java.time.LocalTime

class Flightschedule(id: EntityID<String>) : Entity<String>(id) {
	val flightno: String by FlightscheduleTable.flightno
	val from: Short by FlightscheduleTable.from
	val to: Short by FlightscheduleTable.to
	val departure: LocalTime by FlightscheduleTable.departure
	val arrival: LocalTime by FlightscheduleTable.arrival
	val airlineId: Short by FlightscheduleTable.airlineId
	val monday: Boolean by FlightscheduleTable.monday.boolean()
	val tuesday: Boolean by FlightscheduleTable.tuesday.boolean()
	val wednesday: Boolean by FlightscheduleTable.wednesday.boolean()
	val thursday: Boolean by FlightscheduleTable.thursday.boolean()
	val friday: Boolean by FlightscheduleTable.friday.boolean()
	val saturday: Boolean by FlightscheduleTable.saturday.boolean()
	val sunday: Boolean by FlightscheduleTable.sunday.boolean()
	
	
	fun Column<Byte>.boolean(): ColumnWithTransform<Byte, Boolean> {
		return transform<Byte, Boolean>({ if (it) 1 else 0 }, { it == 0.toByte() })
	}
	
	companion object : EntityClass<String, Flightschedule>(FlightscheduleTable)
}

object FlightscheduleTable : IdTable<String>("flightschedule") {
	val flightno: Column<String> = char("flightno", 8)
	val from: Column<Short> = short("from")
	val to: Column<Short> = short("to").references(AirportTable.airportId)
	val departure: Column<LocalTime> = time("departure")
	val arrival: Column<LocalTime> = time("arrival")
	val airlineId: Column<Short> = short("airline_id").references(AirlineTable.airlineId, fkName = "flightschedule_ibfk_3")
	val monday: Column<Byte> = byte("monday").default(0)
	val tuesday: Column<Byte> = byte("tuesday").default(0)
	val wednesday: Column<Byte> = byte("wednesday").default(0)
	val thursday: Column<Byte> = byte("thursday").default(0)
	val friday: Column<Byte> = byte("friday").default(0)
	val saturday: Column<Byte> = byte("saturday").default(0)
	val sunday: Column<Byte> = byte("sunday").default(0)
	
	
	override val id: Column<EntityID<String>> = flightno.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(id)
}