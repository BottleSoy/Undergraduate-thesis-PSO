package top.soy_bottle.pso.db.airportdb

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.math.BigDecimal

class AirportGeo(id: EntityID<Short>) : Entity<Short>(id) {
	val airport: Airport by Airport.referencedOn(AirportGeoTable.airportId)
	val name: String by AirportGeoTable.name
	val city: String? by AirportGeoTable.city
	val country: String? by AirportGeoTable.country
	val latitude: BigDecimal by AirportGeoTable.latitude
	val longitude: BigDecimal by AirportGeoTable.longitude
}

object AirportGeoTable : IdTable<Short>("airport_geo") {
	val airportId: Column<Short> = short("airport_id").references(AirportTable.id)
	val name: Column<String> = varchar("name", 50)
	val city: Column<String?> = varchar("city", 50).nullable()
	val country: Column<String?> = varchar("country", 50).nullable()
	val latitude: Column<BigDecimal> = decimal("latitude", 11, 8)
	val longitude: Column<BigDecimal> = decimal("longitude", 11, 8)
	
	override val id: Column<EntityID<Short>> = airportId.entityId()
	override val primaryKey: PrimaryKey = PrimaryKey(airportId)
}