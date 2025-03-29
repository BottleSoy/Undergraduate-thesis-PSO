package top.soy_bottle.pso.db.airportdb.example

import org.jetbrains.exposed.sql.and
import top.soy_bottle.pso.db.airportdb.Employee
import top.soy_bottle.pso.db.airportdb.EmployeeTable

fun login(username: String, password: String): Employee? {
	return Employee.find {
		(EmployeeTable.username eq username) and
			(EmployeeTable.password eq password)
	}.firstOrNull()
}
