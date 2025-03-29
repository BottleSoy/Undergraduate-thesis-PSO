package top.soy_bottle.pso

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.ResultSet

inline fun <T> ResultSet.mapLines(action: ResultSet.() -> T): List<T> {
	val res = arrayListOf<T>()
	while (this.next()) {
		res.add(action(this))
	}
	this.close()
	return res
}
val config = HikariConfig().apply {
	jdbcUrl = "jdbc:mysql://192.168.91.171:4000/airportdb?useSSL=false"
	driverClassName = "com.mysql.jdbc.Driver"
	username = "root"
	password = "475-9i^K6DUjT@_x3t"
	
	maximumPoolSize = 64
	// as of version 0.46.0, if these options are set here, they do not need to be duplicated in DatabaseConfig
	isReadOnly = false
}
fun createDatabase() = Database.connect(
	datasource = HikariDataSource(config),
)
fun main() {
	transaction {
		val tables = this.exec("show tables;", explicitStatementType = StatementType.EXEC) { result ->
			result.mapLines {
				this.getString(1)
			}
		}!!
		println(tables)
//		tables.forEach { table ->
//			this.exec("show create table $table;", explicitStatementType = StatementType.EXEC) {
//				it.next()
//				println(it.getString(2) + ";")
//			}
//		}
	}
}

class TableRelativeAnalyzer(val db: Database) {
	data class Table(val name: String) {
		val primaryColums = arrayListOf<String>()
		val linkTo = arrayListOf<Table>()
		val linkBy = arrayListOf<Table>()
	}
	
	lateinit var tables: List<Table>
	fun start() {
		transaction(db) {
			tables = this.exec("show tables;", explicitStatementType = StatementType.EXEC) { result ->
				result.mapLines {
					this.getString(1)
				}
			}!!.map(::Table)
			
			println(tables)
			
			this.exec(
				"select * from information_schema.REFERENTIAL_CONSTRAINTS where CONSTRAINT_SCHEMA=(select DATABASE());",
				explicitStatementType = StatementType.EXEC
			) { result ->
				result.mapLines {
					this.getString(1)
				}
			}
			
		}
	}
}

