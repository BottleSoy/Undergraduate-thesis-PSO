package top.soy_bottle.pso.db.airportdb.pso

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import top.soy_bottle.pso.*
import top.soy_bottle.pso.db.airportdb.*
import top.soy_bottle.pso.db.airportdb.example.addBooking
import top.soy_bottle.pso.db.airportdb.example.queryAirportFlights
import top.soy_bottle.pso.db.airportdb.example.queryFlightUser
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.random.Random

val vmPaths = listOf(
	"D:\\VMS\\GNS3\\Ubuntu1\\Ubuntu1.vmx",
	"D:\\VMS\\GNS3\\Ubuntu2\\Ubuntu2.vmx",
	"D:\\VMS\\GNS3\\Ubuntu3\\Ubuntu3.vmx",
	"D:\\VMS\\GNS3\\Ubuntu4\\Ubuntu4.vmx",
)
val vmrunPath = "C:\\Program Files (x86)\\VMware\\VMware Workstation\\vmrun.exe"
val snapshotName = "3"
fun randomSeat(): String = UUID.randomUUID().toString().substring(0, 4)

class AirportDBParticle(
	val space: AirportDBSpace,
) : Particle<Long, AirportDBParticle> {
	val dimValues = linkedMapOf<Dimension<*>, Any>()
	
	fun <S : Any> setDimValue(dim: Dimension<S>, value: S) {
		dimValues[dim] = value
	}
	
	fun <S : Any> getDimValue(dim: Dimension<S>): S {
		return dimValues[dim] as S
	}
	
	override fun distance(another: AirportDBParticle): Double {
		return dimValues.map { (t, u) ->
			t as Dimension<Any>
			t.distance(u, another.getDimValue(t)).sumOf { abs(it) }
		}.sum()
	}
	
	override val dimensions: Int = space.dimensions;
	var vec: List<Double> = arrayListOf()
	override fun getVector(): List<Double> = vec
	fun fillVector() {
		dimValues.forEach { (t, u) ->
			repeat(t.dims) {
				vec += 0.0
			}
		}
	}
	
	override fun calcRes(): Long {
		//重置虚拟机到原先快照
		runBlocking {
			vmPaths.map { vmPath ->
				GlobalScope.launch {
					val revertProcess = ProcessBuilder(vmrunPath, "-T", "snapshot", "revertToSnapshot", vmPath, snapshotName)
						.start()
					revertProcess.waitFor()

					val startProcess = ProcessBuilder(vmrunPath, "-T", "ws", "start", vmPath)
						.start()
					startProcess.waitFor()
				}
			}.forEach { job: Job ->
				job.join()
			}
		}
		val dataSource = HikariDataSource(config)
		
		val database = Database.connect(dataSource)
		
		val jobs = arrayListOf<Job>()
		
		dimValues.forEach { (t, u) ->

			jobs += GlobalScope.launch(Dispatchers.IO) {
				transaction {
					if (t.name.startsWith("prop:")) {
						var r = u
						if (u is Double) r = String.format("%.6f", u) //防止科学计数法
						else if (u is TypeName) r = u.type
						else if (t.name == "prop:tidb_load_based_replica_read_threshold") r =
							u.toString() + "ms"// replica read 的触发阈值属性用的是字符串...
						exec("set ${t.name.substring(5)} = $r;")
					}
				}
			}
		}
		runBlocking { jobs.forEach { it.join() } }
		jobs.clear()
		val start = System.currentTimeMillis()
		repeat(500) {
			jobs += GlobalScope.launch(Dispatchers.IO) {
				transaction {
					val id = addBooking(
						space.passengers.random(), space.flights.random(),
						BigDecimal(Random.nextDouble(40.0, 600.0)),
						randomSeat()
					)
					BookingTable.deleteWhere {
						BookingTable.bookingId eq id
					}
				}
			}
		}
		
		repeat(1000) {
			jobs += GlobalScope.launch(Dispatchers.IO) {
				transaction {
					queryFlightUser(space.flights.random())
				}
			}
		}
		repeat(800) {
			jobs += GlobalScope.launch(Dispatchers.IO) {
				transaction {
					queryAirportFlights(space.airports.random())
				}
			}
		}
		runBlocking {
			jobs.forEach {
				it.join()
			}
		}
		val end = System.currentTimeMillis()
		dataSource.close()
		Thread.sleep(4000)
		dimValues.values.forEach {
			print(it)
			print("\t")
		}
		print(end - start)
		println()
		return end - start
	}
	
	override fun genVector(another: AirportDBParticle, weight: Double): List<Double> {
		val newDims = arrayListOf<Double>()
		dimValues.forEach { (t, u) ->
			t as Dimension<Any>
			newDims.addAll(t.distance(u, another.getDimValue(t)).map { it * weight })
		}
		return newDims
	}
	
}



class AirportDBParticleHolder(initParticle: AirportDBParticle) : ParticleHolder<Long, AirportDBParticle> {
	@Transient
	private var pBestIndex = 0
	private val history = LinkedList<Pair<Long, AirportDBParticle>>()
	
	@Transient
	private var pBestRes: Long;
	
	init {
		val initRes = initParticle.calcRes()
		pBestRes = initRes
		history.addLast(initRes to initParticle)
	}
	
	override val step: Int get() = history.size
	override val current: AirportDBParticle get() = history.last.second
	override val currentRes: Long get() = history.last.first
	
	override fun getPBest(): Pair<Long, AirportDBParticle> = history[pBestIndex]
	
	override fun stepAt(step: Int) = history[step]
	
	var preVWeight = 0.18
	val pbestWeight = 0.3
	val gBestWeight = 0.5
	override fun applyVector(gvector: List<Double>) {
		val newParticle = AirportDBParticle(current.space)
		val pBestVec = current.genVector(history[pBestIndex].second, 1.0)
		val newVec = current.vec.mapIndexed { index, value ->
			value * preVWeight +
				pBestVec[index] * pbestWeight +
				gvector[index] * gBestWeight
		}
		var index = 0
		current.dimValues.forEach { (dim, v) ->
			dim as Dimension<Any>
			val newV = dim.applyMove(v, newVec.subList(index, index + dim.dims))
			newParticle.setDimValue(dim, newV)
			index += dim.dims
		}
		newParticle.vec = newVec
		val newRes = newParticle.calcRes()
		if (newRes < pBestRes) {
			pBestRes = newRes
			pBestIndex = step
		}
		history.addLast(newRes to newParticle)
	}
}

class AirportDBSpace : Space<AirportDBParticleHolder, Long, AirportDBParticle>() {
	
	val dimList = listOf<Dimension<*>>(
		IntDimension("prop:tidb_init_chunk_size", 1..32, 32),//这个变量用来设置执行过程中初始 chunk 的行数
		IntDimension("prop:tidb_max_chunk_size", 32..65536, 1024),//这个变量用来设置执行过程中一个 chunk 最大的行数
		IntDimension("prop:tidb_index_lookup_size", 1..1000000, 200),//这个变量用来设置 index lookup 操作的 batch 大小
		IntDimension("prop:tidb_index_serial_scan_concurrency", 1..256, 1),//这个变量用来设置顺序 scan 操作的并发度
		IntDimension("prop:tidb_load_based_replica_read_threshold", 0..3600 * 1000, 1000),//这个变量用来设置基于负载的 replica read 的触发阈值
		IntDimension("prop:tidb_min_paging_size", 1..65536, 128),//paging size 行数
		IntDimension("prop:tidb_max_paging_size", 1..1048576, 50000),//这个变量用来设置 coprocessor 协议中 paging size 的最大的行数。
		BooleanDimension("prop:tidb_opt_agg_push_down", false), //聚合函数下推
		IntDimension("prop:tidb_opt_broadcast_cartesian_join", 0..2, 1),//是否允许 Broadcast Cartesian Join 算法
		FloatDimension("prop:tidb_opt_concurrency_factor", 0.0..10000.0, 3.0),// Golang goroutine 的 CPU 开销
		FloatDimension("prop:tidb_opt_copcpu_factor", 0.0..10000.0, 3.0),// TiKV 协处理器处理一行数据的 CPU 开销
		IntDimension(
			"prop:tidb_opt_correlation_exp_factor",
			1..10000,
			1
		),//用来控制启发式方法的行为。当值为 0 时不用启发式估算方法，大于 0 时，该变量值越大，启发式估算方法越倾向 index scan，越小越倾向 table scan。
		FloatDimension("prop:tidb_opt_correlation_threshold", 0.0..1.0, 0.9),//设置优化器启用交叉估算 row count 方法的阈值。‘
		FloatDimension("prop:tidb_opt_cpu_factor", 0.0..10000.0, 3.0),//TiDB 处理一行数据的 CPU 开销
		FloatDimension("prop:tidb_opt_desc_factor", 0.0..10000.0, 3.0),//TiKV 在磁盘上扫描一行数据的开销。
		FloatDimension("prop:tidb_opt_disk_factor", 0.0..10000.0, 1.5),//TiDB 往临时磁盘读写一个字节数据的 I/O 开销
		IntDimension(
			"prop:tidb_opt_join_reorder_threshold",
			0..10000,
			0
		),//这个变量用来控制 TiDB Join Reorder 算法的选择。 大于该阈值时，TiDB 选择贪心算法，小于该阈值时 TiDB 选择动态规划
		IntDimension("prop:tidb_opt_limit_push_down_threshold", 0..100000, 100),//这个变量用来设置将 Limit 和 TopN 算子下推到 TiKV 的阈值。
		FloatDimension("prop:tidb_opt_memory_factor", 0.0..10000.0, 0.001),//表示 TiDB 存储一行数据的内存开销
		FloatDimension("prop:tidb_opt_network_factor", 0.0..10000.0, 1.0),//表示传输 1 比特数据的网络净开销
		FloatDimension("prop:tidb_opt_ordering_index_selectivity_threshold", 0.0..10000.0, 0.0),//优化器选择的索引率
		BooleanDimension("prop:tidb_opt_prefer_range_scan", false),//将该变量值设为 ON 后，优化器总是偏好区间扫描而不是全表扫描
		FloatDimension("prop:tidb_opt_tiflash_concurrency_factor", 0.0..10000.0, 24.0),//表示 TiFlash 计算的并发数。
		EnumDimension(
			"prop:tidb_replica_read",
			RelipaRead::class.java,
			RelipaRead.LEADER
		),//这个变量用于控制 TiDB 的 Follower Read 功能的行为。
		BooleanDimension("prop:tidb_enforce_mpp", false),
		IntDimension("prop:tidb_session_plan_cache_size", 1..100000, 100),//这个变量用来控制 Plan Cache 最多能够缓存的计划数量。
		IntDimension("prop:tidb_store_batch_size", 0..25000, 4),//设置 IndexLookUp 算子回表时多个 Coprocessor Task 的 batch 大小。
		BooleanDimension("prop:tidb_enable_non_prepared_plan_cache", false)//这个变量用来控制是否开启非 Prepare 语句执行计划缓存。
	)
	val dimensions = dimList.sumOf { it.dims }
	fun createDefaultParticle(): AirportDBParticle {
		return AirportDBParticle(this).apply {
			dimList.forEach { d ->
				d as Dimension<Any>
				setDimValue(d, d.default)
			}
			fillVector()
		}
	}
	
	val obj = ObjectInputStream(FileInputStream("pso-data"))
	
	val passengers = obj.readObject() as ArrayList<Int>
	
	//	= ArrayList(transaction { Passenger.all().map { passenger -> passenger.passengerId } })
	val flights = obj.readObject() as ArrayList<Int>
	
	//	= ArrayList(transaction { Flight.all().map { flight -> flight.flightId } })
	val airports = obj.readObject() as ArrayList<Short>
//	= ArrayList(transaction { Airport.all().map { flight -> flight.airportId } })

//	init {
//		val obj = ObjectOutputStream(FileOutputStream("pso-data"))
//		obj.writeObject(passengers)
//		obj.writeObject(flights)
//		obj.writeObject(airports)
//		obj.close()
//	}
	
	fun genParticle(): AirportDBParticle {
		return AirportDBParticle(this).apply {
			dimList.forEach { d ->
				d as Dimension<Any>
				setDimValue(d, d.random(random))
			}
			fillVector()
		}
	}
	
	
	override fun optimize() {
		val gBest = particles.minBy { it.currentRes }
		val particleVectors = hashMapOf<AirportDBParticleHolder, List<Double>>()
		particles.forEach { me ->
			var newVector = buildList<Double> {
				dimList.forEach {
					repeat(it.dims) {
						this.add(0.0)
					}
				}
			}
			if (me != gBest) {
				val bestVec = me.current.genVector(gBest.current, 1.0)
				newVector = newVector.mapIndexed { index: Int, raw: Double ->
					raw + bestVec[index]
				}
			}
			particleVectors[me] = newVector
		}
		particleVectors.forEach { (holder, vector) ->
			holder.applyVector(vector)
		}
	}
	
}

val random = Random(0)
fun main() {
	val space = AirportDBSpace()
	println("初始化...")
	space.particles.add(AirportDBParticleHolder(space.createDefaultParticle()))
	repeat(20) {
		space.particles.add(AirportDBParticleHolder(space.genParticle()))
	}
	
	repeat(25) {
		println()
		println("周期:${it + 1}")
		space.optimize()
	}
}