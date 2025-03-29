package top.soy_bottle.pso.db.airportdb
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class Passenger(id: EntityID<Int>) : Entity<Int>(id) {
	companion object : EntityClass<Int, Passenger>(PassengerTable)
	
	val passengerId by PassengerTable.passengerId
	val passportno by PassengerTable.passportno
	val firstname by PassengerTable.firstname
	val lastname by PassengerTable.lastname
	
}

object PassengerTable : IdTable<Int>("passenger") {
	val passengerId: Column<Int> = integer("passenger_id").autoIncrement()
	val passportno: Column<String> = char("passportno", 9).uniqueIndex("pass_unq")
	val firstname: Column<String> = varchar("firstname", 100)
	val lastname: Column<String> = varchar("lastname", 100)
	
	override val id: Column<EntityID<Int>> = passengerId.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(passengerId)
}