package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.javatime.date

class Passengerdetails(id:EntityID<Int>): Entity<Int>(id) {
	var passengerId by PassengerdetailsTable.passengerId
	var birthdate by PassengerdetailsTable.birthdate
	var sex by PassengerdetailsTable.sex
	var street by PassengerdetailsTable.street
	var city by PassengerdetailsTable.city
	var zip by PassengerdetailsTable.zip
	var country by PassengerdetailsTable.country
	var emailaddress by PassengerdetailsTable.emailaddress
	var telephoneno by PassengerdetailsTable.telephoneno
	
	companion object : EntityClass<Int, Passengerdetails>(PassengerdetailsTable)
}

object PassengerdetailsTable : IdTable<Int>("passengerdetails") {
	val passengerId = integer("passenger_id").references(PassengerTable.passengerId, fkName = "passengerdetails_ibfk_1")
	val birthdate = date("birthdate")
	val sex = char("sex", 1).nullable()
	val street = varchar("street", 100)
	val city = varchar("city", 100)
	val zip = short("zip")
	val country = varchar("country", 100)
	val emailaddress = varchar("emailaddress", 120)
	val telephoneno = varchar("telephoneno", 30)
	
	override val id = passengerId.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(id)
}