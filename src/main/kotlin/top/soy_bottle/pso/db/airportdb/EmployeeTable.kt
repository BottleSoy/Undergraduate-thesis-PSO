package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.date
import java.math.BigDecimal
import java.time.LocalDate

class Employee(id: EntityID<Int>) : Entity<Int>(id) {
	var employeeId by EmployeeTable.employeeId
	var firstname by EmployeeTable.firstname
	var lastname by EmployeeTable.lastname
	var birthdate by EmployeeTable.birthdate
	var sex by EmployeeTable.sex
	var street by EmployeeTable.street
	var city by EmployeeTable.city
	var zip by EmployeeTable.zip
	var country by EmployeeTable.country
	var emailaddress by EmployeeTable.emailaddress
	var telephoneno by EmployeeTable.telephoneno
	var salary by EmployeeTable.salary
	var department by EmployeeTable.department
	var username by EmployeeTable.username
	var password by EmployeeTable.password
	
	companion object : EntityClass<Int, Employee>(EmployeeTable)
}

enum class Department {
	Marketing, Buchhaltung, Management, Logistik, Flugfeld;
}

object EmployeeTable : IdTable<Int>("employee") {
	val employeeId: Column<Int> = integer("employee_id").autoIncrement()
	val firstname: Column<String> = varchar("firstname", 100)
	val lastname: Column<String> = varchar("lastname", 100)
	val birthdate: Column<LocalDate> = date("birthdate")
	val sex: Column<String?> = char("sex", 1).nullable()
	val street: Column<String> = varchar("street", 100)
	val city: Column<String> = varchar("city", 100)
	val zip: Column<Short> = short("zip")
	val country: Column<String> = varchar("country", 100)
	val emailaddress: Column<String?> = varchar("emailaddress", 120).nullable()
	val telephoneno: Column<String?> = varchar("telephoneno", 30).nullable()
	val salary: Column<BigDecimal?> = decimal("salary", 8, 2).nullable()
	val department: Column<Department?> = enumeration("department", Department::class).nullable()
	val username: Column<String?> = varchar("username", 20).uniqueIndex("user_unq").nullable()
	val password: Column<String?> = char("password", 32).nullable()
	
	override val id: Column<EntityID<Int>> = employeeId.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(id)
}
