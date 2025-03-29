package top.soy_bottle.pso

import java.util.LinkedList
import kotlin.math.*
import kotlin.random.Random

// 优化的函数
fun f(x: Double, y: Double) = (abs(sin(x / 10)) * x).pow(2) + abs(x) + y.pow(2)

// 实现一个二维粒子
data class ConcreteParticle(
	val x: Double,
	val y: Double,
	val vx: Double,
	val vy: Double,
) : Particle<Double, ConcreteParticle> {
	override val dimensions = 2
	
	override fun distance(another: ConcreteParticle): Double {
		return sqrt((this.x - another.x).pow(2) + (this.y - another.y).pow(2))
	}
	
	override fun genVector(another: ConcreteParticle, weight: Double): List<Double> {
		return listOf((another.x - this.x) * weight, (another.y - this.y) * weight)
	}
	
	override fun getVector(): List<Double> {
		return listOf(vx, vy)
	}
	
	override fun calcRes(): Double {
		return f(x, y)
	}
	
	override fun toString(): String {
		return "ConcreteParticle(x=${String.format("%.2f", x)}, y=${String.format("%.2f", y)}, " +
			"vx=${String.format("%.2f", vx)}, vy=${String.format("%.2f", vy)})"
	}
}

// 粒子的状态和行为管理
class ConcreteParticleHolder(initParticle: ConcreteParticle) : ParticleHolder<Double, ConcreteParticle> {
	@Transient
	private var pBestIndex = 0
	
	@Transient
	private var pBestRes = initParticle.calcRes()
	private val history = LinkedList<Pair<Double, ConcreteParticle>>()
	
	init {
		history.add(initParticle.calcRes() to initParticle)
	}
	
	override val step: Int
		get() = history.size
	override val currentRes: Double get() = history.last().first
	override val current: ConcreteParticle get() = history.last().second
	
	override fun getPBest(): Pair<Double, ConcreteParticle> {
		return history[pBestIndex]
	}
	
	override fun stepAt(step: Int): Pair<Double, ConcreteParticle> {
		return history.getOrNull(step) ?: throw IndexOutOfBoundsException(step)
	}
	
	var preVWeight = 0.3
	var pbestWeight = 0.4
	val gBestWeight = 0.8
	override fun applyVector(doubles: List<Double>) {
		val newVx = current.vx * preVWeight +
			doubles[0] * gBestWeight / 10 +
			pbestWeight * (stepAt(pBestIndex).second.x - current.x) / 10
		val newVy = current.vy * preVWeight +
			doubles[1] * gBestWeight / 10 +
			pbestWeight * (stepAt(pBestIndex).second.y - current.y) / 10
		val newX = current.x + newVx
		val newY = current.y + newVy
		
		val newParticle = ConcreteParticle(newX, newY, newVx, newVy)
		val newRes = newParticle.calcRes()
		preVWeight = ((newRes - currentRes) / 20).withIn(0.8, 0.0)
		
		if (newRes < pBestRes) {
			pBestRes = newRes
			pBestIndex = step
		}
		history.addLast(newRes to newParticle)
	}
	
	override fun toString(): String {
		val a = StringBuilder("ConcreteParticleHolder(history=\n[");
		history.forEach { (first, second) ->
			a.append("(${String.format("%.2f", first)},$second),\n")
		}
		return a.append("],current=$current,currentRes=$currentRes)").toString()
	}
}

// 优化的空间
class ConcreteSpace : Space<ConcreteParticleHolder, Double, ConcreteParticle>() {
	override fun optimize() {
		val gBest = particles.minByOrNull { it.currentRes }!!
		
		val particleVectors = hashMapOf<ConcreteParticleHolder, List<Double>>()
		particles.forEach { me ->
			var newVector = listOf(0.0, 0.0)
			if (me != gBest) {
				val bestVec = me.current.genVector(gBest.current, 1.0)
				newVector = newVector.mapIndexed { index: Int, raw: Double ->
					raw + bestVec[index]
				}
			}
			
			//这段代码是用于粒子间交流的代码
//			particles.forEach { another ->
//				if (another.currentRes > me.currentRes) {
//					val oneVec =
//						me.current.genVector(
//							another.current,
//							(another.currentRes - me.currentRes) / 50
//						)
//					newVector = newVector.mapIndexed { index: Int, raw: Double ->
//						raw + oneVec[index]
//					}
//				}
//			}
			
			particleVectors[me] = newVector
		}
		
		particleVectors.forEach { (holder, vector) ->
			holder.applyVector(vector)
		}
	}
	
	fun optimize(iterations: Int) {
		for (i in 1..iterations) optimize()
	}
}

// 粒子初始化和优化
fun main() {
	val random = Random(0)
	val space = ConcreteSpace()
	for (i in 0 until 50) {  // 初始化50个粒子
		val x = random.nextDouble(-10.0, 10.0)
		val y = random.nextDouble(-10.0, 10.0)
		val particle = ConcreteParticle(x, y, .0, .0)
		space.particles.add(ConcreteParticleHolder(particle))
	}
	space.optimize(100)
	space.particles.forEach {
		println(it)
	}
	val totalDis =
		space.particles.sumOf { me -> space.particles.sumOf { another -> me.current.distance(another.current) } }
	println("distance in avg:" + totalDis / space.particles.size / space.particles.size)
	val bestParticle = space.particles.minByOrNull { it.getPBest().first }!!
	println("Best Particle: x=${bestParticle.current.x}, y=${bestParticle.current.y}, f(x, y)=${bestParticle.currentRes}")
}


fun Double.maxOf(d: Double) = if (d > this) this else d
fun Double.minOf(d: Double) = if (d < this) this else d
